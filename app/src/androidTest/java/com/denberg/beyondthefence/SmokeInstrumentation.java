package com.denberg.beyondthefence;

import android.app.*;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.UiAutomation;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.content.pm.ActivityInfo;
import android.view.MotionEvent;
import android.view.InputDevice;
import android.os.*;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.io.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/** Runs against the installed APK using framework instrumentation; no test runtime dependency. */
public final class SmokeInstrumentation extends Instrumentation {
  private int checks;

  @Override
  public void onCreate(Bundle args) {
    super.onCreate(args);
    start();
  }

  private void check(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
    checks++;
  }

  /** Same main-queue idle condition as waitForIdleSync, with an explicit failure deadline. */
  private void awaitIdle() {
    CountDownLatch idle = new CountDownLatch(1);
    new Handler(Looper.getMainLooper()).post(() -> Looper.myQueue().addIdleHandler(() -> {
      idle.countDown();
      return false;
    }));
    try {
      check(idle.await(10, TimeUnit.SECONDS), "Main queue becomes idle within 10 seconds");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AssertionError("Interrupted waiting for main queue", e);
    }
  }

  /** Animated worlds need a completed draw, while paused screens must actually become idle. */
  private void settle(MainActivity a) {
    if (!onUi(() -> !a.paused)) { awaitIdle(); return; }
    long[] before = new long[1];
    runOnMainSync(() -> before[0] = ((GameBoard) control(a, "field")).artwork.renderedFrames);
    awaitUi(() -> ((GameBoard) control(a, "field")).artwork.renderedFrames > before[0],
        "Running game renders another frame within10 seconds");
  }

  private void awaitAndroidFrame() {
    CountDownLatch frame = new CountDownLatch(1);
    new Handler(Looper.getMainLooper()).post(() ->
        android.view.Choreographer.getInstance().postFrameCallback(time -> frame.countDown()));
    try {
      check(frame.await(10, TimeUnit.SECONDS), "Android delivers a frame within10 seconds");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt(); throw new AssertionError("Frame wait interrupted", e);
    }
  }

  private void click(MainActivity a, String tag) {
    runOnMainSync(
        () -> {
          View v = a.findViewById(android.R.id.content).findViewWithTag(tag);
          if (v == null) throw new AssertionError("Missing control " + tag);
          if (!v.isEnabled() || !v.performClick())
            throw new AssertionError("Control unavailable " + tag);
        });
    awaitIdle();
  }

  private MainActivity launch() {
    return (MainActivity)
        startActivitySync(
            new Intent(getTargetContext(), MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  }

  private boolean onUi(BooleanSupplier condition) {
    boolean[] value = new boolean[1];
    runOnMainSync(() -> value[0] = condition.getAsBoolean());
    return value[0];
  }

  private void awaitUi(BooleanSupplier condition, String message) {
    long deadline = SystemClock.uptimeMillis() + 10000;
    while (SystemClock.uptimeMillis() < deadline) {
      dismissSystemHint();
      if (onUi(condition)) {
        check(true, message);
        return;
      }
      SystemClock.sleep(100);
    }
    check(false, message);
  }

  private void shell(String command) throws IOException {
    // Read through EOF: closing the descriptor immediately does not await command completion.
    try (InputStream in =
        new ParcelFileDescriptor.AutoCloseInputStream(getUiAutomation().executeShellCommand(command))) {
      byte[] buffer = new byte[1024];
      while (in.read(buffer) != -1) {}
    }
  }

  private View control(MainActivity a, String tag) {
    return a.findViewById(android.R.id.content).findViewWithTag(tag);
  }

  private void dialogAction(String text) {
    long deadline = SystemClock.uptimeMillis() + 10000;
    while (SystemClock.uptimeMillis() < deadline) {
      AccessibilityNodeInfo root = getUiAutomation().getRootInActiveWindow();
      if (root != null) for (AccessibilityNodeInfo node : root.findAccessibilityNodeInfosByText(text)) {
        if (node.getText() != null && text.equalsIgnoreCase(node.getText().toString()) && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
          check(true, "Dialog action: " + text); awaitIdle(); return;
        }
      }
      SystemClock.sleep(100);
    }
    check(false, "Dialog action reachable: " + text);
  }

  private void dismissSystemHint() {
    for (AccessibilityWindowInfo window : getUiAutomation().getWindows()) {
      AccessibilityNodeInfo root = window.getRoot();
      if (root == null) continue;
      CharSequence pkg = root.getPackageName();
      if (pkg == null || !(pkg.toString().equals("android") || pkg.toString().equals("com.android.systemui"))) continue;
      for (AccessibilityNodeInfo node : root.findAccessibilityNodeInfosByText("Got it")) {
        AccessibilityNodeInfo button = node;
        for (int i = 0; button != null && i < 4; i++, button = button.getParent()) {
          if (button.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            SystemClock.sleep(200);
            return;
          }
        }
      }
    }
  }

  /** Real pointer injection checks the rendered tile hit areas, not just a listener call. */
  private void touch(MainActivity a, String tag) {
    dismissSystemHint();
    awaitUi(a::hasWindowFocus, "Touched game control has window focus");
    runOnMainSync(() -> {
      View v = control(a, tag);
      if (v == null || !v.isEnabled() || !v.isShown()) throw new AssertionError("Unavailable " + tag);
      v.requestRectangleOnScreen(new Rect(0, 0, v.getWidth(), v.getHeight()), true);
    });
    settle(a);
    SystemClock.sleep(100);
    int[] xy = new int[2];
    runOnMainSync(() -> {
      View v = control(a, tag);
      Rect visible = new Rect();
      if (!v.getLocalVisibleRect(visible) || visible.isEmpty()) throw new AssertionError("Not visible " + tag);
      v.getLocationOnScreen(xy);
      xy[0] += visible.centerX(); xy[1] += visible.centerY();
      android.util.Log.i("BTFSmoke", tag + " at " + xy[0] + "," + xy[1] + " focus=" + a.hasWindowFocus());
    });
    long now = SystemClock.uptimeMillis();
    MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, xy[0], xy[1], 0);
    MotionEvent up = MotionEvent.obtain(now, now + 50, MotionEvent.ACTION_UP, xy[0], xy[1], 0);
    down.setSource(InputDevice.SOURCE_TOUCHSCREEN);
    up.setSource(InputDevice.SOURCE_TOUCHSCREEN);
    check(getUiAutomation().injectInputEvent(down, true), "Pointer down delivered");
    check(getUiAutomation().injectInputEvent(up, true), "Pointer up delivered");
    down.recycle(); up.recycle();
    settle(a);
  }

  private void checkBoard(MainActivity a) {
    runOnMainSync(() -> {
      View field = control(a, "field");
      GameBoard map = (GameBoard) field;
      check(map.geometry.fortLeft >= 55 * a.getResources().getDisplayMetrics().density,
          "Compact farm leaves visible roaming terrain on each side");
      check(map.geometry.fortTop >= 47 * a.getResources().getDisplayMetrics().density,
          "Compact farm leaves roaming terrain above and below");
      float min = 48 * a.getResources().getDisplayMetrics().density - 1;
      for (String prefix : new String[] {"plot", "building"}) {
        int count = prefix.equals("plot") ? 9 : 3;
        for (int i = 0; i < count; i++) {
          View tile = control(a, prefix + i);
          check(tile.getParent() == field, "Native action belongs to the game board");
          check(tile.getWidth() >= min && tile.getHeight() >= min, "Tile target at least 48dp");
          check(tile.getLeft() >= 0 && tile.getTop() >= 0 && tile.getRight() <= field.getWidth()
              && tile.getBottom() <= field.getHeight(), "Tile stays in artwork bounds");
          check(tile.getContentDescription() != null, "Tile has an accessible action label");
        }
      }
      View cross = control(a, "fenceCross");
      View n = control(a, "wall0"), e = control(a, "wall1"), s = control(a, "wall2"), w = control(a, "wall3");
      for (View command : new View[] {n, e, s, w}) {
        check(command.getParent() == cross && command.getWidth() >= min && command.getHeight() >= min,
            "Fence command belongs to the bottom cross and is at least48dp");
        check(command.getContentDescription() != null, "Direct fence command describes health and coin price");
      }
      check(n.getLeft() == s.getLeft() && n.getTop() < e.getTop() && e.getTop() == w.getTop()
          && w.getLeft() < n.getLeft() && n.getLeft() < e.getLeft() && e.getTop() < s.getTop(), "N/W/E/S forms a compass cross");
      int[] at = new int[2]; cross.getLocationOnScreen(at);
      View center = control(a, "fenceUpgrade");
      check(center.getParent() == cross && center.getWidth() >= min && center.getHeight() >= min
          && center.getLeft() == n.getLeft() && center.getTop() == e.getTop(), "Center upgrade fills the middle48dp target");
      check(!control(a, "notice").isShown(), "Instruction banner is absent from the playing field");
      check(((android.widget.TextView) control(a, "quickUniform")).getText().toString().equals("Merc ↑"), "Compact Merc upgrade label");
      View commands = control(a, "commands"); int[] dockAt = new int[2]; commands.getLocationOnScreen(dockAt);
      check(Math.abs(at[0] + cross.getWidth() / 2 - (dockAt[0] + commands.getWidth() / 2)) <= 2,
          "Repair cross is horizontally centered in its rendered bar: cross=" + at[0] + "/" + cross.getWidth()
              + ", bar=" + dockAt[0] + "/" + commands.getWidth());
      for (String tag : new String[] {"quickHire", "quickUniform", "itemSlot0", "itemSlot1"}) {
        View shortcut = control(a, tag);
        check(shortcut.getWidth() >= min && shortcut.getHeight() >= min && shortcut.getContentDescription() != null,
            "Direct mercenary command is accessible and at least48dp");
      }
      View content = a.findViewById(android.R.id.content); int[] contentAt = new int[2]; content.getLocationOnScreen(contentAt);
      check(at[1] >= contentAt[1] && at[1] + cross.getHeight() <= contentAt[1] + content.getHeight(),
          "Repair cross is pinned within the actual window bounds");
    });
  }

  private void screenshot(String filename) throws IOException {
    awaitAndroidFrame();
    SystemClock.sleep(250); // Include a completed draw after the native layout traversal.
    Bitmap bitmap = getUiAutomation().takeScreenshot();
    check(bitmap != null, "Screenshot available");
    try (FileOutputStream out = new FileOutputStream(new File(getTargetContext().getFilesDir(), filename))) {
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
    } finally { bitmap.recycle(); }
  }

  private MainActivity rotate(MainActivity a, int orientation) {
    ActivityMonitor monitor = addMonitor(MainActivity.class.getName(), null, false);
    runOnMainSync(() -> a.setRequestedOrientation(orientation));
    MainActivity next = (MainActivity) monitor.waitForActivityWithTimeout(10000);
    removeMonitor(monitor);
    check(next != null, "Orientation recreates activity within 10 seconds");
    awaitIdle();
    awaitUi(next::hasWindowFocus, "Rotated board gains focus");
    return next;
  }

  @Override
  public void onStart() {
    Bundle result = new Bundle();
    try {
      UiAutomation automation = getUiAutomation();
      AccessibilityServiceInfo info = automation.getServiceInfo();
      info.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
      automation.setServiceInfo(info);
      MainActivity a = launch();
      awaitIdle();
      boolean welcomed = false;
      long deadline = SystemClock.uptimeMillis() + 10000;
      while (!welcomed && SystemClock.uptimeMillis() < deadline) {
        dismissSystemHint();
        AccessibilityNodeInfo root = automation.getRootInActiveWindow();
        if (root != null) {
          List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText("Plant the first seed");
          for (AccessibilityNodeInfo node : nodes)
            welcomed |= node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        if (!welcomed) SystemClock.sleep(100);
      }
      check(welcomed, "Welcome visible and actionable within 10 seconds");
      settle(a);
      runOnMainSync(() -> a.paused = true);
      awaitUi(a::hasWindowFocus, "Game window ready");
      checkBoard(a);
      check(!control(a, "fenceUpgrade").isEnabled(), "Fence upgrade requires an explicitly selected direction");
      check(onUi(() -> {
        for (int side = 0; side < 4; side++) {
          View button = control(a, "wall" + side);
          Bitmap pixel = Bitmap.createBitmap(button.getWidth(), button.getHeight(), Bitmap.Config.ARGB_8888);
          try {
            button.draw(new Canvas(pixel)); int inset = (int)(8 * a.getResources().getDisplayMetrics().density);
            if (Color.alpha(pixel.getPixel(inset, inset)) != 0) return false;
          } finally { pixel.recycle(); }
        }
        return true;
      }), "Compass backgrounds are truly transparent at rest");
      // A paused farm must stop continuous rendering, including while a panel is open.
      assertPausedStatic(a);
      float roamingProgress = a.game.roamers.get(0).progress;
      runOnMainSync(() -> { for (int i = 0; i < 20; i++) a.game.tick(.05f); a.refresh(); });
      check(onUi(() -> !a.game.night && a.game.roamers.get(0).progress > roamingProgress
          && a.game.health == 100 && a.game.coins == 80), "Daytime zombies visibly roam without harming the farm");
      check(onUi(() -> ((android.widget.TextView) control(a, "night")).getText().toString().contains("Raid in")),
          "Raid countdown is visible in the action dock");
      touch(a, "play");
      awaitUi(() -> a.music.isPlaying() && !a.music.isNightTrack() && a.music.effectsReady(), "Sunny daytime score and all effects load");
      touch(a, "play");
      checkFenceHealthPixels(a);
      screenshot("smoke-roaming.png");
      touch(a, "seeds");
      check(!control(a, "seed1").isEnabled(), "Locked seed is visibly unavailable");
      check(onUi(() -> {
        View seed = control(a, "seed0"); Rect visible = new Rect();
        return seed.getGlobalVisibleRect(visible) && visible.height() == seed.getHeight();
      }), "Seeds opens a visible tray above the fixed commands without scrolling");
      touch(a, "seed0");
      touch(a, "plot6");
      check(onUi(() -> a.paused && a.game.coins == 80 && control(a, "upgrade3").isShown()), "Locked soil opens expansion without spending");
      touch(a, "closeBuild");
      touch(a, "plot0");
      check(onUi(() -> a.game.crop[0] == 0 && a.game.coins == 76), "Pointer plants inside fortress");
      touch(a, "plot0");
      check(onUi(() -> a.game.coins == 76 && a.game.crop[0] == 0), "Growing tile cannot be harvested early");
      touch(a, "build"); touch(a, "buildTab2"); touch(a, "shopPlant"); touch(a, "closeBuild");
      check(onUi(() -> a.game.coins == 56 && a.game.crop[5] == 0 && a.game.crop[6] == -1), "Bulk planting keeps locked plots intact");
      runOnMainSync(() -> { for (int p = 0; p < 6; p++) a.game.growth[p] = 1; a.refresh(); });
      touch(a, "plot0");
      check(onUi(() -> a.game.coins == 68 && a.game.wood == 26), "Direct harvest gives coins and timber");
      touch(a, "harvest");
      check(onUi(() -> a.game.coins == 128 && a.game.wood == 36 && a.game.harvested == 6), "Bulk harvest credits resources once");
      screenshot("smoke-farm.png");
      touch(a, "build"); touch(a, "buildTab0");
      assertPausedStatic(a);
      touch(a, "constructWall1");
      check(onUi(() -> a.game.walls[1] == 1 && a.game.coins == 108 && a.game.wood == 28 && a.paused), "Build missing east fence with explicit coin/timber debit");
      touch(a, "closeBuild");
      touch(a, "building0");
      check(((android.widget.TextView) control(a, "panelTitle")).getText().toString().equals("MILL")
          && control(a, "buyTimber").isShown() && control(a, "construct1") == null,
          "Mill opens its timber features without unrelated building actions");
      touch(a, "construct0"); touch(a, "closeBuild");
      check(onUi(() -> a.game.buildings[0] && a.game.coins == 58 && a.game.wood == 18), "Construct the interior timber mill");
      farmCycle(a);
      touch(a, "building2");
      check(((android.widget.TextView) control(a, "panelTitle")).getText().toString().equals("BARRACKS")
          && control(a, "hire").isShown() && control(a, "upgrade0") != null,
          "Camp opens construction, recruitment and mercenary upgrades");
      touch(a, "construct2"); touch(a, "closeBuild");
      check(onUi(() -> a.game.buildings[2] && a.game.coins == 36), "Construct barracks using harvest income");
      farmCycle(a);
      touch(a, "quickHire");
      check(onUi(() -> !control(a, "closeBuild").isShown()), "Quick hire does not open a panel");
      touch(a, "squad");
      check(onUi(() -> a.game.mercenaries.size() == 2 && a.game.coins == 4 && a.paused), "Hire a real patrol through Squad");
      touch(a, "rally3");
      check(onUi(() -> a.game.lane == 3), "Rally sends mercenaries to the west border");
      touch(a, "closeBuild");
      float progress = a.game.mercenaries.get(0).progress;
      runOnMainSync(() -> { for (int i = 0; i < 20; i++) a.game.tick(.05f); a.refresh(); });
      check(onUi(() -> Math.abs(a.game.mercenaries.get(0).progress - progress) > .01f), "Patrol physically moves around the border");
      touch(a, "night");
      check(onUi(() -> a.game.night && !a.paused), "Start night deliberately begins four-border wave");
      awaitUi(() -> a.music.isNightTrack(), "Creepy score plays during the active raid");
      screenshot("smoke-night.png");
      runOnMainSync(() -> a.onWindowFocusChanged(false));
      check(onUi(() -> a.paused && !a.music.isPlaying() && a.music.effectStreams() == 0), "Focus loss pauses simulation and music");
      runOnMainSync(() -> a.onWindowFocusChanged(true));
      check(onUi(() -> a.paused), "Focus return does not auto-resume");
      touch(a, "play");
      awaitUi(() -> a.hasWindowFocus() && !a.paused, "Game running before Home");
      shell("input keyevent KEYCODE_HOME");
      awaitUi(() -> !a.hasWindowFocus() && a.paused, "Home pauses game");
      float[] elapsed = new float[1];
      runOnMainSync(() -> elapsed[0] = a.game.elapsed);
      SystemClock.sleep(400);
      check(onUi(() -> a.game.elapsed == elapsed[0] && !a.music.isPlaying()), "No background simulation or audio");
      getTargetContext().startActivity(new Intent(getTargetContext(), MainActivity.class)
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
      awaitUi(a::hasWindowFocus, "Game foreground return");
      check(onUi(() -> a.paused), "Foreground return requires explicit resume");
      touch(a, "options"); dialogAction("Background music: ON");
      check(onUi(() -> !a.musicEnabled && !a.music.isPlaying()), "Options mutes the action score");
      dialogAction("Sound effects: ON");
      check(onUi(() -> !a.effectsEnabled && a.music.effectStreams() == 0), "Effects toggle stops all voices");
      dialogAction("Close"); touch(a, "play");
      check(onUi(() -> !a.paused && !a.music.isPlaying()), "Muted raid continues without playback");
      touch(a, "play");
      runOnMainSync(() -> { a.game.defeated = true; a.game.health = 0; a.refresh(); });
      touch(a, "night");
      check(onUi(() -> !a.game.defeated && a.game.health == a.game.maxHealth() && a.paused), "Free recovery keeps player paused");
      runOnMainSync(() -> { a.game.day = 2; a.refresh(); });
      int[] seedBudget = new int[1];
      runOnMainSync(() -> seedBudget[0] = a.game.coins);
      touch(a, "seeds"); touch(a, "seed1");
      touch(a, "plot0");
      check(onUi(() -> a.game.crop[0] == 1 && a.game.coins == seedBudget[0] - Game.SEED[1]), "Seed card changes the planted crop and debit");
      touch(a, "building0");
      MainActivity landscape = rotate(a, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      check(onUi(() -> landscape.game == a.game && landscape.paused && !landscape.musicEnabled && !landscape.effectsEnabled), "Rotation retains farm and pause");
      check(control(landscape, "closeBuild").isShown(), "Upgrade overlay survives rotation");
      check(((android.widget.TextView) control(landscape, "panelTitle")).getText().toString().equals("MILL")
          && control(landscape, "buyTimber").isShown(), "Building-specific features survive rotation");
      screenshot("smoke-upgrades-landscape.png");
      touch(landscape, "closeBuild");
      check(control(landscape, "seed1").isSelected(), "Selected seed survives rotation");
      check(onUi(() -> landscape.game.buildings[0] && landscape.game.buildings[2] && landscape.game.mercenaries.size() == 2), "Buildings and squad survive rotation");
      checkBoard(landscape); checkDock(landscape);
      runOnMainSync(() -> {
        GameBoard field = (GameBoard) control(landscape, "field");
        Bitmap target = Bitmap.createBitmap(field.getWidth(), field.getHeight(), Bitmap.Config.ARGB_8888);
        try {
          for (int weather : new int[] {1, 2}) {
            landscape.game.weather = weather;
            field.artwork.draw(new Canvas(target));
            target.eraseColor(Color.TRANSPARENT);
            field.artwork.draw(new Canvas(target));
            check(Color.alpha(target.getPixel(target.getWidth() - 1, 0)) == 255,
                "Rain/fog cannot fade the next background frame");
          }
          landscape.game.weather = 1;
          landscape.refresh();
        } finally { target.recycle(); }
      });
      screenshot("smoke-landscape.png");
      MainActivity portrait = rotate(landscape, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      checkBoard(portrait); checkDock(portrait);
      ActivityMonitor fontMonitor = addMonitor(MainActivity.class.getName(), null, false);
      shell("settings put system font_scale 2.0");
      MainActivity large = (MainActivity) fontMonitor.waitForActivityWithTimeout(10000);
      removeMonitor(fontMonitor);
      check(large != null, "Font change recreates board");
      awaitIdle();
      check(large.getResources().getConfiguration().fontScale >= 1.9f, "200 percent system font active");
      checkBoard(large); checkDock(large);
      runOnMainSync(() -> {
        for (int i = 0; i < 4; i++) {
          android.widget.TextView marker = (android.widget.TextView) control(large, "wall" + i);
          check(marker.getLineCount() == 1 && marker.getLayout().getHeight()
              <= marker.getHeight() - marker.getPaddingTop() - marker.getPaddingBottom(),
              "Large-font fence marker fits without clipped health text");
        }
      });
      touch(large, "squad");
      touch(large, "closeBuild");
      int fullBudget = large.game.coins;
      touch(large, "wall0");
      check(onUi(() -> large.game.coins == fullBudget && !control(large, "closeBuild").isShown()),
          "Intact fence tap at200 percent font spends nothing and opens no settings");
      screenshot("smoke-large-font.png");
      int expected = large.game.coins;
      SystemClock.sleep(600);
      runOnMainSync(large::finish);
      SystemClock.sleep(500);
      MainActivity reopened = launch();
      awaitIdle();
      check(onUi(() -> reopened.game.coins == expected && reopened.game.crop[0] == 1), "Farm persists after activity restart");
      check(onUi(() -> reopened.paused && !reopened.musicEnabled && !reopened.effectsEnabled), "Restart retains mute and requires explicit resume");
      runOnMainSync(() -> {
        reopened.game.day = 11;
        reopened.game.night = false;
        reopened.game.enemies.clear(); reopened.game.roamers.clear(); reopened.game.populateRoamers();
        reopened.game.elapsed = 34.95f; reopened.refresh();
      });
      touch(reopened, "play");
      awaitUi(() -> reopened.game.night, "Day11 automatically starts a raid after35 active seconds");
      check(onUi(() -> reopened.game.roamers.isEmpty() && !reopened.game.enemies.isEmpty()
          && reopened.game.spawned <= reopened.game.quota()), "Roamers become bounded, real attacking enemies");
      touch(reopened, "play");
      Game threatened = new Game();
      threatened.coins = 100; threatened.wood = 0; threatened.mercenaries.clear();
      threatened.buildings[2] = true; threatened.buildingHealth[2] = Game.BUILDING_HEALTH;
      threatened.plant(0, 0); threatened.plant(1, 0); threatened.growth[1] = 1;
      threatened.cropHealth[0] = 1; threatened.wallHealth[0] = .1f;
      threatened.night = true; threatened.roamers.clear(); threatened.spawned = threatened.quota();
      threatened.enemies.add(new Game.Enemy(2, 0, 0, 200, 200)); threatened.shotClock = 1;
      runOnMainSync(() -> { reopened.game = threatened; reopened.paused = true; reopened.refresh(); });
      touch(reopened, "seeds"); touch(reopened, "seed0");
      touch(reopened, "play");
      awaitUi(() -> threatened.lostCrops == 1, "Actual frame loop breaks a fence and destroys a crop");
      touch(reopened, "play");
      check(onUi(() -> threatened.wallHealth[0] == 0 && threatened.enemies.get(0).breached
          && threatened.scorched[0] && threatened.crop[0] == -1 && threatened.harvested == 0
          && threatened.coins == 92 && threatened.wood == 0), "Breach and crop loss award no harvest resources");
      screenshot("smoke-breach.png");
      touch(reopened, "plot0");
      check(onUi(() -> threatened.crop[0] == 0 && !threatened.scorched[0]
          && threatened.cropHealth[0] == Game.CROP_HEALTH && threatened.coins == 88), "Ruined farm tile can be replanted for seed cost");
      touch(reopened, "harvest");
      check(onUi(() -> threatened.coins == 100 && threatened.wood == 2 && threatened.harvested == 1),
          "Surviving ripe crop funds repairs through normal harvest");
      touch(reopened, "build"); touch(reopened, "buildTab0"); touch(reopened, "buyTimber");
      check(onUi(() -> threatened.coins == 88 && threatened.wood == 12), "Harvest coins buy a timber bundle atomically");
      touch(reopened, "constructWall0");
      check(onUi(() -> threatened.wallHealth[0] == 50 && threatened.coins == 80 && threatened.wood == 12
          && threatened.enemies.get(0).breached && reopened.paused), "Repair closes a breach without deleting an existing intruder");
      touch(reopened, "closeBuild"); touch(reopened, "quickHire");
      check(onUi(() -> threatened.mercenaries.size() == 1 && threatened.coins == 25 && threatened.wood == 6),
          "Harvest economy funds an actual mercenary after repair");
      check(!control(reopened, "closeBuild").isShown(), "Direct recruitment stays on the field");
      screenshot("smoke-rebuilt.png");
      runOnMainSync(() -> { threatened.coins = 200; threatened.wood = 100; reopened.refresh(); });
      touch(reopened, "build"); touch(reopened, "buildTab0"); touch(reopened, "constructWall0");
      touch(reopened, "constructWall0"); touch(reopened, "constructWall0");
      check(onUi(() -> threatened.walls[0] == 3 && threatened.wallHealth[0] == 160
          && threatened.coins == 116 && threatened.wood == 66), "Wood to Stone to Steel upgrades spend both resources and restore fence health");
      touch(reopened, "constructWall2"); touch(reopened, "constructWall1");
      check(onUi(() -> threatened.walls[2] == 2 && threatened.walls[1] == 1
          && threatened.coins == 64 && threatened.wood == 44), "Each border independently selects its next material kit");
      touch(reopened, "closeBuild");
      float enemyHealth = threatened.enemies.get(0).hp;
      touch(reopened, "squad"); touch(reopened, "rally0");
      check(onUi(() -> threatened.lane == 0 && threatened.enemies.get(0).hp == enemyHealth),
          "Rally changes guard orders without an instant shot");
      touch(reopened, "patrol");
      check(onUi(() -> threatened.lane == -1 && threatened.enemies.get(0).hp == enemyHealth), "Patrol resumes without hidden damage");
      touch(reopened, "closeBuild");
      runOnMainSync(() -> {
        threatened.night = false; threatened.enemies.clear(); threatened.roamers.clear();
        threatened.day = 4; threatened.xp = 150; threatened.elapsed = 0; threatened.populateRoamers(); reopened.refresh();
      });
      boolean[] variants = new boolean[4];
      for (Game.Roamer roamer : threatened.roamers) variants[roamer.type] = true;
      for (boolean visible : variants) check(visible, "Redesigned zombie variant is present in the materials scene");
      screenshot("smoke-materials.png");
      runOnMainSync(() -> { threatened.coins = 2000; threatened.wood = 200; reopened.refresh(); });
      touch(reopened, "quickHire"); touch(reopened, "quickHire");
      check(onUi(() -> threatened.mercenaries.size() == 3 && !control(reopened, "quickHire").isEnabled()),
          "Three-mercenary cap is visible and enforced");
      touch(reopened, "quickUniform"); touch(reopened, "quickUniform");
      check(onUi(() -> threatened.weapon == 2 && threatened.mercenaries.get(0).health == threatened.guardMaxHealth()),
          "Uniform purchase improves existing crew armor and health");
      check(!control(reopened, "closeBuild").isShown(), "Direct uniforms stay on the field");
      // Every direction repairs only its own side, with no timber or pause/menu side effect.
      runOnMainSync(() -> {
        threatened.coins = 100; threatened.wood = 0; threatened.walls[3] = 1;
        for (int side = 0; side < 4; side++) threatened.wallHealth[side] = 0;
        reopened.refresh();
      });
      touch(reopened, "play");
      for (int side = 0; side < 4; side++) {
        int before = threatened.coins;
        touch(reopened, "wall" + side);
        final int border = side;
        check(onUi(() -> threatened.wallHealth[border] == 50 && threatened.coins == before - 8
            && threatened.wood == 0 && !reopened.paused && !control(reopened, "closeBuild").isShown()),
            "Cross direction repairs with coins alone and no settings");
      }
      touch(reopened, "play");
      screenshot("smoke-survival.png");
      Game audioFight = new Game(); audioFight.night = true; audioFight.roamers.clear();
      audioFight.spawned = audioFight.quota(); audioFight.lane = 0;
      audioFight.enemies.add(new Game.Enemy(2, 0, 0, 400, 400));
      audioFight.enemies.add(new Game.Enemy(2, 0, .5f, 400, 400));
      runOnMainSync(() -> {
        reopened.game = audioFight; reopened.musicEnabled = true; reopened.effectsEnabled = true;
        reopened.paused = true; reopened.refresh();
      });
      int shotsBefore = reopened.music.shotSounds, zombieBefore = reopened.music.zombieSounds;
      int owlBefore = reopened.music.owlSounds, cricketBefore = reopened.music.cricketSounds;
      touch(reopened, "play");
      awaitUi(() -> reopened.music.shotSounds > shotsBefore && reopened.music.zombieSounds > zombieBefore
          && reopened.music.owlSounds > owlBefore && reopened.music.cricketSounds > cricketBefore,
          "Real guard fire, nearby zombies and both nocturnal voices reach SoundPool");
      touch(reopened, "play");
      check(onUi(() -> !reopened.music.isPlaying() && reopened.music.effectStreams() == 0), "Pause stops every music/effect voice");
      // Selected direction owns the center arrow; no settings or pause side effects.
      Game compact = new Game(); compact.coins = 200; compact.wood = 100;
      runOnMainSync(() -> { reopened.game = compact; reopened.paused = true; reopened.refresh(); });
      touch(reopened, "wall1");
      check(control(reopened, "wall1").isSelected() && !control(reopened, "wall0").isSelected(), "East selection is explicit");
      touch(reopened, "fenceUpgrade");
      check(onUi(() -> compact.walls[1] == 1 && compact.walls[0] == 1 && compact.coins == 180 && compact.wood == 92
          && !control(reopened, "closeBuild").isShown()), "Center builds only the selected gap with exact coin/timber debit");
      touch(reopened, "fenceUpgrade"); touch(reopened, "fenceUpgrade");
      check(onUi(() -> compact.walls[1] == 3 && compact.coins == 104 && compact.wood == 58
          && control(reopened, "fenceUpgrade").isEnabled()), "Steel unlocks additional reinforcement ranks");
      touch(reopened, "fenceUpgrade");
      check(onUi(() -> compact.coins == 104 && compact.wood == 58 && compact.wallRanks[1] == 0
          && ((android.widget.TextView)control(reopened, "fenceUpgrade")).getText().toString().contains("120c")),
          "Insufficient reinforcement funds reveal required price inside the same button without spending");
      touch(reopened, "wall2"); touch(reopened, "play"); touch(reopened, "fenceUpgrade");
      check(onUi(() -> compact.walls[2] == 2 && compact.walls[1] == 3 && compact.walls[0] == 1
          && compact.coins == 72 && compact.wood == 44 && !reopened.paused), "Changing direction upgrades that side while play continues");
      touch(reopened, "play");
      runOnMainSync(() -> { compact.coins = 0; reopened.refresh(); });
      touch(reopened, "wall0");
      touch(reopened, "fenceUpgrade");
      check(onUi(() -> compact.coins == 0 && compact.walls[0] == 1
          && ((android.widget.TextView)control(reopened, "fenceUpgrade")).getText().toString().contains("32c")),
          "Unaffordable fence upgrade shows coin requirement in the button with no panel or debit");
      touch(reopened, "quickUniform");
      check(onUi(() -> compact.weapon == 0 && compact.coins == 0
          && ((android.widget.TextView)control(reopened, "quickUniform")).getText().toString().contains("80c")),
          "Unaffordable mercenary upgrade shows its price inside the button");
      runOnMainSync(() -> { compact.coins = 200; compact.wood = 100; compact.plant(0, 0); compact.growth[0] = 1; reopened.refresh(); });
      touch(reopened, "building1");
      check(((android.widget.TextView) control(reopened, "panelTitle")).getText().toString().equals("SHOP")
          && control(reopened, "shopHarvest").isShown() && control(reopened, "upgrade2") != null,
          "Shop opens crop sales and farm upgrades");
      touch(reopened, "construct1"); touch(reopened, "shopHarvest");
      check(onUi(() -> compact.buildings[1] && compact.coins == 131 && compact.wood == 84 && compact.harvested == 1),
          "Shop market bonus and direct harvest sale apply exactly once");
      touch(reopened, "shopPlant");
      check(onUi(() -> compact.coins == 107 && compact.crop[5] == 0 && compact.crop[6] == -1), "Shop planting respects existing plot limits and seed prices");
      screenshot("smoke-shop.png"); touch(reopened, "closeBuild");
      screenshot("smoke-compact.png");
      runOnMainSync(() -> { compact.coins = 2000; compact.wood = 500; compact.health = 40; compact.mercenaries.get(0).health = 20; reopened.refresh(); });
      touch(reopened, "building2"); touch(reopened, "construct2"); touch(reopened, "buyHero");
      check(onUi(() -> compact.hero != null && compact.hero.health == 220 && compact.mercenaries.size() == 1),
          "Barracks recruits a real hero separate from mercenary slots");
      touch(reopened, "advanced1"); touch(reopened, "advanced2");
      check(onUi(() -> compact.drill == 1 && compact.heroLevel == 1 && compact.hero.health == 260), "Barracks drill and hero training change real combat stats");
      screenshot("smoke-barracks.png"); touch(reopened, "closeBuild");
      touch(reopened, "building0"); touch(reopened, "construct0"); touch(reopened, "advanced0");
      check(onUi(() -> compact.millLevel == 1 && compact.millYield() == 6 && compact.millPeriod() == 11), "Mill popup buys a working production upgrade");
      touch(reopened, "closeBuild"); touch(reopened, "building1"); touch(reopened, "buyItem0"); touch(reopened, "buyItem1");
      check(onUi(() -> compact.elixirs == 1 && compact.enhancers == 1), "Shop stocks both side-slot consumables");
      screenshot("smoke-items.png"); touch(reopened, "closeBuild"); touch(reopened, "itemSlot0");
      check(onUi(() -> compact.elixirs == 0 && compact.health == 75 && compact.mercenaries.get(0).health == 90), "Left elixir slot heals surviving actors exactly once");
      touch(reopened, "itemSlot1");
      check(onUi(() -> compact.enhancers == 0 && compact.enhancerTime == 20 && !control(reopened, "itemSlot1").isEnabled()), "Right enhancer slot starts a bounded boost without consuming twice");
      for (int day : new int[] {6, 11}) {
        runOnMainSync(() -> { compact.day = day; compact.elapsed = 0; reopened.refresh(); });
        settle(reopened); checkBoard(reopened);
        check(onUi(() -> compact.settlementStyle() == (day == 6 ? 1 : 2) && compact.buildings[1] && compact.crop[0] == 0),
            "Five-day layout transition preserves crops and purchases");
        screenshot(day == 6 ? "smoke-village.png" : "smoke-castle.png");
      }
      // Reproduce the old stale-scroll/mismatched-building tab transition on the real panel.
      touch(reopened, "building2");
      for (int page : new int[] {1, 2, 3, 0, 3, 2, 1}) {
        runOnMainSync(() -> ((android.widget.ScrollView) control(reopened, "panelScroll")).fullScroll(View.FOCUS_DOWN));
        awaitIdle(); touch(reopened, "buildTab" + page);
        check(onUi(() -> ((android.widget.ScrollView) control(reopened, "panelScroll")).getScrollY() == 0),
            "Changed tab starts at top instead of inheriting old scroll");
        String title = new String[] {"FENCE", "MILL", "SHOP", "BARRACKS"}[page];
        check(onUi(() -> ((android.widget.TextView)control(reopened, "panelTitle")).getText().toString().equals(title)
            && control(reopened, "buildTab" + page).isSelected()), "Title and selected tab match actual page");
        check(onUi(() -> {
          android.widget.TextView tab = (android.widget.TextView) control(reopened, "buildTab" + page);
          return tab.getLineCount() == 1 && tab.getPaint().measureText(tab.getText().toString())
              <= tab.getWidth() - tab.getPaddingLeft() - tab.getPaddingRight();
        }), "Building tab label fits as a complete word at200 percent font");
        check(onUi(() -> page == 0 ? control(reopened, "constructWall0") != null && control(reopened, "buyHero") == null
            : control(reopened, "construct" + (page - 1)) != null
                && (page != 1 || control(reopened, "buyItem0") == null)
                && (page != 2 || control(reopened, "hire") == null)
                && (page != 3 || control(reopened, "buyTimber") == null)), "New tab contains only its building actions");
      }
      screenshot("smoke-tabs.png"); touch(reopened, "closeBuild"); checkDock(reopened);
      testStoreRecovery();
      result.putString("stream", "\nOK (" + checks + " checks)\n");
      finish(Activity.RESULT_OK, result);
    } catch (Throwable e) {
      try { screenshot("smoke-failure.png"); } catch (Throwable ignored) { }
      try (InputStream in = new ParcelFileDescriptor.AutoCloseInputStream(
              getUiAutomation().executeShellCommand("dumpsys window windows"));
          OutputStream out = new FileOutputStream(new File(getTargetContext().getFilesDir(), "smoke-window.txt"))) {
        byte[] buffer = new byte[4096];
        int count;
        while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
      } catch (Throwable ignored) { }
      result.putString("stream", "\nFAILURES!!!\n" + android.util.Log.getStackTraceString(e));
      finish(Activity.RESULT_CANCELED, result);
    }
  }

  private void checkDock(MainActivity a) {
    runOnMainSync(() -> {
      View hire = control(a, "quickHire"), uniform = control(a, "quickUniform");
      View left = control(a, "itemSlot0"), right = control(a, "itemSlot1");
      check(hire.getWidth() == uniform.getWidth() && hire.getHeight() == uniform.getHeight(), "Mercenary command sizes match");
      check(left.getWidth() == right.getWidth() && left.getHeight() == right.getHeight(), "Item slot sizes remain symmetric during boost");
      int[] l = new int[2], r = new int[2]; left.getLocationOnScreen(l); right.getLocationOnScreen(r);
      check(l[1] == r[1], "Item slots share one baseline");
      for (String tag : new String[] {"seeds", "build", "harvest", "night"}) {
        android.widget.TextView label = (android.widget.TextView) control(a, tag);
        check(label.getLineCount() <= 2 && label.getLayout().getHeight() <= label.getHeight() - label.getPaddingTop() - label.getPaddingBottom(),
            "Compact action caption fits in two unclipped lines");
      }
      int height = control(a, "seeds").getHeight();
      for (String tag : new String[] {"build", "harvest", "night"})
        check(control(a, tag).getHeight() == height, "Bottom action heights match at every font/orientation");
      View terrain = control(a, "terrain");
      Bitmap image = Bitmap.createBitmap(terrain.getWidth(), terrain.getHeight(), Bitmap.Config.ARGB_8888);
      try {
        terrain.draw(new Canvas(image));
        check(image.getPixel(0, terrain.getHeight()-1) == (a.game.night ? 0xff213e32 : 0xff4b793a),
            "Scenery extends beneath transparent bottom padding");
      } finally { image.recycle(); }
      check(control(a, "commands").getBackground() == null && control(a, "actions").getBackground() == null,
          "Command/action containers have no opaque padding background");
    });
  }

  private void checkFenceHealthPixels(MainActivity a) {
    runOnMainSync(() -> {
      GameBoard map = (GameBoard) control(a, "field");
      Bitmap bitmap = Bitmap.createBitmap(map.getWidth(), map.getHeight(), Bitmap.Config.ARGB_8888);
      float original = a.game.wallHealth[0];
      int[] filled = new int[3];
      try {
        float d = map.geometry.density;
        float y = map.geometry.fortTop;
        for (int phase = 0; phase < 3; phase++) {
          a.game.wallHealth[0] = a.game.maxWallHealth(0) * (1 - phase * .5f);
          map.artwork.draw(new Canvas(bitmap));
          for (int px = (int) (map.geometry.fortLeft + 10 * d); px < (int) (map.geometry.fortRight - 10 * d); px++)
            for (int py = (int) (y - d); py < (int) (y + d); py++)
              if (bitmap.getPixel(px, py) == 0xffb4d176) filled[phase]++;
        }
        check(filled[0] > filled[1] && filled[1] > filled[2] && filled[2] == 0,
            "Fence durability bar follows actual HP: full, half and breached");
      } finally { a.game.wallHealth[0] = original; bitmap.recycle(); a.refresh(); }
    });
  }

  private void assertPausedStatic(MainActivity a) {
    // Allow one-off ripples, focus changes and checkpoint labels to finish first.
    SystemClock.sleep(1000);
    long[] before = new long[1];
    runOnMainSync(() -> {
      check(a.paused, "Static rendering check runs while paused");
      before[0] = ((GameBoard) control(a, "field")).artwork.renderedFrames;
    });
    SystemClock.sleep(600);
    check(onUi(() -> ((GameBoard) control(a, "field")).artwork.renderedFrames - before[0] <= 1),
        "Paused farm renderer does not continuously redraw");
  }

  private void farmCycle(MainActivity a) {
    touch(a, "build"); touch(a, "buildTab2"); touch(a, "shopPlant"); touch(a, "closeBuild");
    runOnMainSync(() -> { for (int i = 0; i < a.game.plots(); i++) if (a.game.crop[i] >= 0) a.game.growth[i] = 1; a.refresh(); });
    touch(a, "harvest");
  }

  private void testStoreRecovery() throws Exception {
    File dir = new File(getTargetContext().getCacheDir(), "checkpoint-test");
    if (!dir.exists() && !dir.mkdirs()) throw new IOException("Cannot create test directory");
    for (File f : dir.listFiles()) if (!f.delete()) throw new IOException("Cannot clear test file");
    Context isolated =
        new ContextWrapper(getTargetContext()) {
          @Override
          public File getFilesDir() {
            return dir;
          }
        };
    CountDownLatch first = new CountDownLatch(1), second = new CountDownLatch(1);
    SaveStore store =
        new SaveStore(
            isolated,
            (rev, ok) -> {
              if (!ok) throw new AssertionError("Checkpoint write");
              if (rev == 1) first.countDown();
              else second.countDown();
            });
    Game g = new Game();
    store.save(g);
    check(first.await(5, TimeUnit.SECONDS), "First checkpoint durable");
    g.coins = 123;
    store.save(g);
    check(second.await(5, TimeUnit.SECONDS), "Second checkpoint durable");
    check(store.load().game.coins == 123, "Newest checkpoint selected");
    try (FileOutputStream out = new FileOutputStream(new File(dir, "farm-b.btfs"))) {
      out.write(0);
    }
    SaveStore.Loaded loaded = store.load();
    check(loaded.recovered && loaded.game.coins == 80, "Corruption recovers previous checkpoint");
    try (FileOutputStream out = new FileOutputStream(new File(dir, "farm-a.btfs"))) {
      out.write(0);
    }
    check(store.load().damaged, "Both corrupt checkpoints block reset");
    store.close();
  }
}
