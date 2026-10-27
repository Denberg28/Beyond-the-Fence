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
    runOnMainSync(() -> {
      View v = control(a, tag);
      if (v == null || !v.isEnabled() || !v.isShown()) throw new AssertionError("Unavailable " + tag);
      v.requestRectangleOnScreen(new Rect(0, 0, v.getWidth(), v.getHeight()), true);
    });
    awaitIdle();
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
    awaitIdle();
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
      for (String prefix : new String[] {"plot", "wall", "building"}) {
        int count = prefix.equals("plot") ? 9 : prefix.equals("wall") ? 4 : 3;
        for (int i = 0; i < count; i++) {
          View tile = control(a, prefix + i);
          check(tile.getParent() == field, "Native action belongs to the game board");
          check(tile.getWidth() >= min && tile.getHeight() >= min, "Tile target at least 48dp");
          check(tile.getLeft() >= 0 && tile.getTop() >= 0 && tile.getRight() <= field.getWidth()
              && tile.getBottom() <= field.getHeight(), "Tile stays in artwork bounds");
          check(tile.getContentDescription() != null, "Tile has an accessible action label");
        }
      }
    });
  }

  private void screenshot(String filename) throws IOException {
    awaitIdle();
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
      awaitIdle();
      runOnMainSync(() -> a.paused = true);
      awaitUi(a::hasWindowFocus, "Game window ready");
      checkBoard(a);
      // A paused farm must stop continuous rendering, including while a panel is open.
      assertPausedStatic(a);
      float roamingProgress = a.game.roamers.get(0).progress;
      runOnMainSync(() -> { for (int i = 0; i < 20; i++) a.game.tick(.05f); a.refresh(); });
      check(onUi(() -> !a.game.night && a.game.roamers.get(0).progress > roamingProgress
          && a.game.health == 100 && a.game.coins == 80), "Daytime zombies visibly roam without harming the farm");
      check(onUi(() -> ((android.widget.TextView) control(a, "night")).getText().toString().contains("Raid in")),
          "Raid countdown is visible in the action dock");
      screenshot("smoke-roaming.png");
      touch(a, "seeds");
      check(!control(a, "seed1").isEnabled(), "Locked seed is visibly unavailable");
      touch(a, "seed0");
      touch(a, "plot6");
      check(onUi(() -> a.paused && a.game.coins == 80 && control(a, "upgrade3").isShown()), "Locked soil opens expansion without spending");
      touch(a, "closeBuild");
      touch(a, "plot0");
      check(onUi(() -> a.game.crop[0] == 0 && a.game.coins == 76), "Pointer plants inside fortress");
      touch(a, "plot0");
      check(onUi(() -> a.game.coins == 76 && a.game.crop[0] == 0), "Growing tile cannot be harvested early");
      touch(a, "build"); touch(a, "buildTab3"); touch(a, "plantAll"); touch(a, "closeBuild");
      check(onUi(() -> a.game.coins == 56 && a.game.crop[5] == 0 && a.game.crop[6] == -1), "Bulk planting keeps locked plots intact");
      runOnMainSync(() -> { for (int p = 0; p < 6; p++) a.game.growth[p] = 1; a.refresh(); });
      touch(a, "plot0");
      check(onUi(() -> a.game.coins == 68 && a.game.wood == 26), "Direct harvest gives coins and timber");
      touch(a, "harvest");
      check(onUi(() -> a.game.coins == 128 && a.game.wood == 36 && a.game.harvested == 6), "Bulk harvest credits resources once");
      screenshot("smoke-farm.png");
      touch(a, "wall1");
      assertPausedStatic(a);
      touch(a, "constructWall1");
      check(onUi(() -> a.game.walls[1] == 1 && a.game.coins == 108 && a.game.wood == 28 && a.paused), "Build missing east fence with explicit coin/timber debit");
      touch(a, "closeBuild");
      touch(a, "building0"); touch(a, "construct0"); touch(a, "closeBuild");
      check(onUi(() -> a.game.buildings[0] && a.game.coins == 58 && a.game.wood == 18), "Construct the interior timber mill");
      farmCycle(a);
      touch(a, "building2"); touch(a, "construct2"); touch(a, "closeBuild");
      check(onUi(() -> a.game.buildings[2] && a.game.coins == 36), "Construct barracks using harvest income");
      farmCycle(a);
      touch(a, "squad"); touch(a, "hire");
      check(onUi(() -> a.game.mercenaries.size() == 1 && a.game.coins == 29 && a.paused), "Hire a real patrol through Squad");
      touch(a, "rally3");
      check(onUi(() -> a.game.lane == 3), "Rally supports the west border");
      touch(a, "closeBuild");
      float progress = a.game.mercenaries.get(0).progress;
      runOnMainSync(() -> { for (int i = 0; i < 20; i++) a.game.tick(.05f); a.refresh(); });
      check(onUi(() -> a.game.mercenaries.get(0).progress > progress), "Patrol physically moves around the border");
      touch(a, "night");
      check(onUi(() -> a.game.night && !a.paused), "Start night deliberately begins four-border wave");
      screenshot("smoke-night.png");
      runOnMainSync(() -> a.onWindowFocusChanged(false));
      check(onUi(() -> a.paused), "Focus loss pauses game");
      runOnMainSync(() -> a.onWindowFocusChanged(true));
      check(onUi(() -> a.paused), "Focus return does not auto-resume");
      touch(a, "play");
      awaitUi(() -> a.hasWindowFocus() && !a.paused, "Game running before Home");
      shell("input keyevent KEYCODE_HOME");
      awaitUi(() -> !a.hasWindowFocus() && a.paused, "Home pauses game");
      float[] elapsed = new float[1];
      runOnMainSync(() -> elapsed[0] = a.game.elapsed);
      SystemClock.sleep(400);
      check(onUi(() -> a.game.elapsed == elapsed[0]), "No background simulation");
      getTargetContext().startActivity(new Intent(getTargetContext(), MainActivity.class)
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
      awaitUi(a::hasWindowFocus, "Game foreground return");
      check(onUi(() -> a.paused), "Foreground return requires explicit resume");
      runOnMainSync(() -> { a.game.defeated = true; a.game.health = 0; a.refresh(); });
      touch(a, "night");
      check(onUi(() -> !a.game.defeated && a.game.health == a.game.maxHealth() && a.paused), "Free recovery keeps player paused");
      runOnMainSync(() -> { a.game.xp = 50; a.refresh(); });
      int[] seedBudget = new int[1];
      runOnMainSync(() -> seedBudget[0] = a.game.coins);
      touch(a, "seeds"); touch(a, "seed1");
      touch(a, "plot0");
      check(onUi(() -> a.game.crop[0] == 1 && a.game.coins == seedBudget[0] - Game.SEED[1]), "Seed card changes the planted crop and debit");
      touch(a, "building0");
      MainActivity landscape = rotate(a, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      check(onUi(() -> landscape.game == a.game && landscape.paused), "Rotation retains farm and pause");
      check(control(landscape, "closeBuild").isShown(), "Upgrade overlay survives rotation");
      screenshot("smoke-upgrades-landscape.png");
      touch(landscape, "closeBuild");
      check(control(landscape, "seed1").isSelected(), "Selected seed survives rotation");
      check(onUi(() -> landscape.game.buildings[0] && landscape.game.buildings[2] && landscape.game.mercenaries.size() == 1), "Buildings and squad survive rotation");
      checkBoard(landscape);
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
      checkBoard(portrait);
      ActivityMonitor fontMonitor = addMonitor(MainActivity.class.getName(), null, false);
      shell("settings put system font_scale 2.0");
      MainActivity large = (MainActivity) fontMonitor.waitForActivityWithTimeout(10000);
      removeMonitor(fontMonitor);
      check(large != null, "Font change recreates board");
      awaitIdle();
      check(large.getResources().getConfiguration().fontScale >= 1.9f, "200 percent system font active");
      checkBoard(large);
      touch(large, "squad");
      touch(large, "closeBuild");
      touch(large, "wall0");
      check(control(large, "constructWall0").isShown(), "Large-font fence actions remain reachable");
      touch(large, "closeBuild");
      screenshot("smoke-large-font.png");
      int expected = large.game.coins;
      SystemClock.sleep(600);
      runOnMainSync(large::finish);
      SystemClock.sleep(500);
      MainActivity reopened = launch();
      awaitIdle();
      check(onUi(() -> reopened.game.coins == expected && reopened.game.crop[0] == 1), "Farm persists after activity restart");
      check(onUi(() -> reopened.paused), "Restart requires explicit resume");
      runOnMainSync(() -> {
        reopened.game.xp = 450;
        reopened.game.night = false;
        reopened.game.enemies.clear(); reopened.game.roamers.clear(); reopened.game.populateRoamers();
        reopened.game.elapsed = 29.95f; reopened.refresh();
      });
      touch(reopened, "play");
      awaitUi(() -> reopened.game.night, "Level10 automatically starts a raid after30 active seconds");
      check(onUi(() -> reopened.game.roamers.isEmpty() && !reopened.game.enemies.isEmpty()
          && reopened.game.spawned <= reopened.game.quota()), "Roamers become bounded, real attacking enemies");
      touch(reopened, "play");
      Game threatened = new Game();
      threatened.coins = 100; threatened.wood = 0;
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
      touch(reopened, "wall0"); touch(reopened, "buyTimber");
      check(onUi(() -> threatened.coins == 88 && threatened.wood == 12), "Harvest coins buy a timber bundle atomically");
      touch(reopened, "constructWall0");
      check(onUi(() -> threatened.wallHealth[0] == 50 && threatened.coins == 80 && threatened.wood == 8
          && threatened.enemies.get(0).breached && reopened.paused), "Repair closes a breach without deleting an existing intruder");
      touch(reopened, "closeBuild"); touch(reopened, "squad"); touch(reopened, "hire");
      check(onUi(() -> threatened.mercenaries.size() == 1 && threatened.coins == 25 && threatened.wood == 2),
          "Harvest economy funds an actual mercenary after repair");
      touch(reopened, "closeBuild");
      screenshot("smoke-rebuilt.png");
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
    touch(a, "build"); touch(a, "buildTab3"); touch(a, "plantAll"); touch(a, "closeBuild");
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
