package com.denberg.beyondthefence;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Insets;
import android.text.TextUtils;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
  static final int INK = 0xff203e36,
      GREEN = 0xff356859,
      CREAM = 0xfff5f2e9,
      MUTED = 0xff52665b,
      GOLD = 0xfff0cd7b;
  Game game;
  boolean paused = true, storageBlocked;
  private boolean foreground, ioBusy;
  private int selectedCrop;
  private boolean buildOpen;
  private long lastFrame, lastUi, lastSave;
  private TextView stats, status, notice, saving;
  private GameButton play, phase, harvest, plantAll, repair, build;
  private final GameButton[] cropButtons = new GameButton[3],
      plotButtons = new GameButton[9], upgradeButtons = new GameButton[4],
      laneButtons = new GameButton[3];
  private FrameLayout window, buildOverlay;
  private LinearLayout boardContent;
  private GameBoard board;
  private FarmView scene;
  private SaveStore store;
  private FarmApp session;
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final ExecutorService fileWorker = Executors.newSingleThreadExecutor();
  private String saveMessage = "Local save ready";
  private final Runnable frame =
      new Runnable() {
        @Override
        public void run() {
          if (!foreground) return;
          long now = SystemClock.elapsedRealtime();
          if (!paused && !storageBlocked && !ioBusy) {
            float dt = Math.min(.1f, (now - lastFrame) / 1000f);
            game.tick(dt);
            if (game.defeated) {
              paused = true;
              save();
            }
            if (now - lastSave >= 3000) save();
          }
          lastFrame = now;
          if (now - lastUi >= 250) {
            refresh();
            lastUi = now;
          }
          scene.invalidate();
          handler.postDelayed(this, game.reducedMotion ? 100 : 33);
        }
      };

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    session = (FarmApp) getApplication();
    store = session.store;
    game = session.game;
    storageBlocked = session.damaged;
    saveMessage = session.saveMessage;
    if (state != null) {
      buildOpen = state.getBoolean("buildOpen");
      selectedCrop = Math.max(0, Math.min(game.unlockedCrops() - 1, state.getInt("seed")));
    }
    buildUi();
    if (buildOpen) showBuild();
    refresh();
    if (storageBlocked)
      handler.post(
          () ->
              new AlertDialog.Builder(this)
                  .setTitle("Save needs attention")
                  .setMessage(
                      "Both local checkpoints are damaged or unsupported. They have not been"
                          + " overwritten. Import a backup in Options, or explicitly create a new"
                          + " farm.")
                  .setPositiveButton("Options", (d, w) -> options())
                  .setCancelable(false)
                  .show());
    else if (!game.tutorialDone) handler.post(this::welcome);
  }

  @Override
  protected void onSaveInstanceState(Bundle state) {
    state.putBoolean("buildOpen", buildOpen);
    state.putInt("seed", selectedCrop);
    super.onSaveInstanceState(state);
  }

  @Override
  protected void onResume() {
    super.onResume();
    immersive();
    session.observer = this;
    saveMessage = session.saveMessage;
    foreground = true;
    paused = true;
    lastFrame = SystemClock.elapsedRealtime();
    handler.post(frame);
    refresh();
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    // System overlays can remove focus before onPause (or without pausing the Activity).
    if (hasFocus) immersive();
    if (!hasFocus && game != null && !paused) {
      paused = true;
      save();
      refresh();
    }
  }

  @Override
  protected void onPause() {
    foreground = false;
    paused = true;
    if (session.observer == this) session.observer = null;
    handler.removeCallbacks(frame);
    if (!storageBlocked) save();
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    handler.removeCallbacksAndMessages(null);
    if (session.observer == this) session.observer = null;
    fileWorker.shutdown();
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    paused = true;
    if (buildOpen) closeBuild();
    else options();
  }

  @SuppressWarnings("deprecation") // Legacy immersive flags are required on API26–29.
  private void immersive() {
    if (Build.VERSION.SDK_INT >= 30) {
      getWindow().setDecorFitsSystemWindows(false);
      WindowInsetsController controller = getWindow().getInsetsController();
      if (controller != null) {
        controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        controller.hide(WindowInsets.Type.systemBars());
      }
    } else {
      getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
          | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
  }

  private void buildUi() {
    window = new FrameLayout(this);
    window.setBackgroundColor(INK);
    setContentView(window);
    window.setOnApplyWindowInsetsListener((v, insets) -> {
      int l = 0, t = 0, r = 0, b = 0;
      if (Build.VERSION.SDK_INT >= 30) {
        Insets safe = insets.getInsets(WindowInsets.Type.displayCutout() | WindowInsets.Type.captionBar());
        l = safe.left; t = safe.top; r = safe.right; b = safe.bottom;
      } else if (Build.VERSION.SDK_INT >= 28 && insets.getDisplayCutout() != null) {
        l = insets.getDisplayCutout().getSafeInsetLeft();
        t = insets.getDisplayCutout().getSafeInsetTop();
        r = insets.getDisplayCutout().getSafeInsetRight();
        b = insets.getDisplayCutout().getSafeInsetBottom();
      }
      window.setPadding(l, t, r, b);
      return insets;
    });
    ScrollView viewport = new ScrollView(this);
    viewport.setFillViewport(true);
    viewport.setVerticalScrollBarEnabled(false);
    boardContent = column();
    boardContent.setPadding(dp(6), dp(4), dp(6), dp(4));
    viewport.addView(boardContent);
    window.addView(viewport, new FrameLayout.LayoutParams(-1, -1));

    LinearLayout hud = row();
    LinearLayout resources = column();
    stats = text("", 14, CREAM);
    stats.setTypeface(null, Typeface.BOLD);
    status = text("", 11, 0xffd0dfc5);
    resources.addView(stats);
    resources.addView(status);
    hud.addView(resources, new LinearLayout.LayoutParams(0, -2, 1));
    repair = button("Fence", () -> { if (canAct()) { game.repair(); changed(); } });
    repair.setTag("repair");
    repair.setTextSize(11);
    hud.addView(repair, new LinearLayout.LayoutParams(dp(88), -2));
    play = button("Play", () -> {
      if (storageBlocked || ioBusy || buildOpen) return;
      if (game.defeated) { game.recover(); changed(); }
      paused = !paused;
      lastFrame = SystemClock.elapsedRealtime();
      refresh();
    });
    play.setTag("play");
    hud.addView(play, new LinearLayout.LayoutParams(dp(58), -2));
    GameButton menu = button("☰", this::options);
    menu.setContentDescription("Pause and open options, help and save backups");
    menu.setTag("options");
    hud.addView(menu, new LinearLayout.LayoutParams(dp(48), -2));
    boardContent.addView(hud);

    LinearLayout seeds = row();
    for (int i = 0; i < 3; i++) {
      final int type = i;
      cropButtons[i] = button("", () -> {
        if (storageBlocked || ioBusy || buildOpen) return;
        selectedCrop = type;
        game.notice = Game.CROPS[type] + " selected. Tap an empty soil tile.";
        refresh();
      });
      cropButtons[i].setTag("seed" + i);
      cropButtons[i].setCompoundDrawablesRelativeWithIntrinsicBounds(new CropIcon(i, dp(26)), null, null, null);
      cropButtons[i].setCompoundDrawablePadding(dp(3));
      cropButtons[i].setTextSize(11);
      LinearLayout.LayoutParams card = new LinearLayout.LayoutParams(0, -2, 1);
      card.setMargins(dp(2), dp(4), dp(2), dp(4));
      seeds.addView(cropButtons[i], card);
    }
    boardContent.addView(seeds);

    board = new GameBoard(this, () -> game, () -> paused);
    scene = board.artwork;
    for (int i = 0; i < 9; i++) {
      final int plot = i;
      plotButtons[i] = button("", () -> {
        if (!canAct() || buildOpen) return;
        if (plot >= game.plots()) { showBuild(); return; }
        if (game.crop[plot] < 0) game.plant(plot, selectedCrop);
        else if (!game.harvest(plot)) game.notice = "Still growing · " + game.harvestSeconds(plot) + "s to harvest.";
        changed();
      });
      plotButtons[i].setTag("plot" + i);
      plotButtons[i].setTextSize(11);
      plotButtons[i].setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
      board.plot(i, plotButtons[i]);
    }
    for (int i = 0; i < 3; i++) {
      final int lane = i;
      laneButtons[i] = button("", () -> {
        if (!canAct() || buildOpen) return;
        game.focus(lane);
        changed();
      });
      laneButtons[i].setTag("lane" + i);
      laneButtons[i].setTextSize(11);
      laneButtons[i].setGravity(Gravity.BOTTOM | Gravity.END);
      board.lane(i, laneButtons[i]);
    }
    boardContent.addView(board, new LinearLayout.LayoutParams(-1, board.minimumBoardHeight(), 1));
    notice = text("", 12, 0xfff1d692);
    notice.setPadding(dp(6), dp(4), dp(6), dp(4));
    notice.setMaxLines(2);
    notice.setEllipsize(TextUtils.TruncateAt.END);
    notice.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
    boardContent.addView(notice);

    LinearLayout dock = row();
    build = button("Build", this::showBuild);
    build.setTag("build");
    plantAll = button("Plant all", () -> { if (canAct() && !buildOpen) { game.plantAll(selectedCrop); changed(); } });
    plantAll.setTag("plantAll");
    harvest = button("Harvest", () -> { if (canAct() && !buildOpen) { game.harvestAll(); changed(); } });
    harvest.setTag("harvest");
    phase = button("Night", () -> {
      if (storageBlocked || ioBusy || buildOpen) return;
      if (game.defeated) { game.recover(); paused = true; }
      else { game.startNight(); paused = false; lastFrame = SystemClock.elapsedRealtime(); }
      changed();
    });
    phase.setTag("night");
    for (GameButton action : new GameButton[] {build, plantAll, harvest, phase}) {
      LinearLayout.LayoutParams item = new LinearLayout.LayoutParams(0, -2, 1);
      item.setMargins(dp(2), 0, dp(2), 0);
      dock.addView(action, item);
    }
    boardContent.addView(dock);
    saving = text(saveMessage, 10, 0xffc2d2be);
    saving.setPadding(dp(6), dp(2), 0, 0);
    boardContent.addView(saving);
    createBuildOverlay();
  }

  private void createBuildOverlay() {
    buildOverlay = new FrameLayout(this);
    buildOverlay.setBackgroundColor(0xd010241e);
    buildOverlay.setClickable(true);
    buildOverlay.setVisibility(View.GONE);
    buildOverlay.setPadding(dp(20), dp(16), dp(20), dp(16));
    LinearLayout card = column();
    GradientDrawable background = new GradientDrawable();
    background.setColor(CREAM);
    background.setCornerRadius(dp(18));
    card.setBackground(background);
    card.setPadding(dp(12), dp(8), dp(12), dp(12));
    LinearLayout bar = row();
    TextView title = text("Build & upgrade", 20, INK);
    title.setTypeface(null, Typeface.BOLD);
    bar.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
    GameButton close = button("✕", this::closeBuild);
    close.setTag("closeBuild");
    close.setContentDescription("Close upgrades and return to the paused game");
    bar.addView(close, new LinearLayout.LayoutParams(dp(48), -2));
    card.addView(bar);
    TextView help = text("Permanent upgrades · game paused", 12, MUTED);
    help.setPadding(dp(4), 0, 0, dp(8));
    card.addView(help);
    ScrollView scroll = new ScrollView(this);
    LinearLayout items = column();
    for (int i = 0; i < 4; i++) {
      final int kind = i;
      upgradeButtons[i] = button("", () -> { if (canAct()) { game.upgrade(kind); changed(); } });
      upgradeButtons[i].setTag("upgrade" + i);
      LinearLayout.LayoutParams row = new LinearLayout.LayoutParams(-1, -2);
      row.setMargins(0, dp(3), 0, dp(3));
      items.addView(upgradeButtons[i], row);
    }
    scroll.addView(items);
    card.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
    int cardWidth = Math.min(dp(480), getResources().getDisplayMetrics().widthPixels - dp(40));
    FrameLayout.LayoutParams centered = new FrameLayout.LayoutParams(cardWidth, -1, Gravity.CENTER);
    buildOverlay.addView(card, centered);
    window.addView(buildOverlay, new FrameLayout.LayoutParams(-1, -1));
  }

  private void showBuild() {
    if (ioBusy) return;
    paused = true;
    buildOpen = true;
    boardContent.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
    buildOverlay.setVisibility(View.VISIBLE);
    buildOverlay.findViewWithTag("closeBuild").requestFocus();
    refresh();
  }

  private void closeBuild() {
    buildOpen = false;
    buildOverlay.setVisibility(View.GONE);
    boardContent.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
    build.requestFocus();
    refresh();
  }

  private boolean canAct() {
    if (storageBlocked || ioBusy) return false;
    if (game.defeated) {
      game.notice = "Rebuild your fence first. Tap Rebuild in the action dock.";
      refresh();
      return false;
    }
    return true;
  }

  private void changed() {
    save();
    refresh();
    scene.invalidate();
  }

  void showSaveStatus(String message) {
    saveMessage = message;
    if (saving != null) saving.setText(message);
  }

  private void save() {
    if (storageBlocked) return;
    session.game = game;
    session.damaged = storageBlocked;
    lastSave = SystemClock.elapsedRealtime();
    session.saveMessage = saveMessage = "Saving locally…";
    store.save(game);
  }

  @android.annotation.SuppressLint("SetTextI18n") // English-only MVP, matching the existing game strings.
  void refresh() {
    if (stats == null) return;
    boolean available = !storageBlocked && !ioBusy && !game.defeated;
    stats.setText(game.coins + " coins  ·  Lv " + game.level());
    String timing = game.defeated ? "Fence down" : game.night
        ? "Night · " + game.enemies.size() + " threats"
        : "Night in " + Math.max(0, (int) Math.ceil(Game.DAY_SECONDS - game.elapsed)) + "s";
    status.setText("Day " + game.day + " · " + Game.WEATHER[game.weather] + " · " + timing);
    stats.setContentDescription(game.coins + " coins, level " + game.level() + ", " + game.xp % 50 + " of 50 XP");
    play.setText(game.defeated ? "Rebuild" : paused ? "Play" : "Pause");
    play.setContentDescription(game.defeated ? "Rebuild fence free and resume" : paused ? "Resume game" : "Pause game");
    play.setEnabled(!storageBlocked && !ioBusy && !buildOpen);
    repair.setText("Fence " + Math.round(game.health) + "/" + game.maxHealth()
        + (game.health < game.maxHealth() ? "\nRepair · 8c" : ""));
    repair.setContentDescription("Fence strength " + Math.round(game.health) + " of " + game.maxHealth()
        + ". Repair costs 8 coins and restores up to 40 strength.");
    repair.setEnabled(available && game.health < game.maxHealth() && game.coins >= 8);
    repair.colors(game.health < game.maxHealth() * .3f ? 0xff744c37 : 0xff314e40,
        0xff77916d, CREAM);
    play.colors(paused ? GOLD : 0xff314e40, GOLD, paused ? INK : CREAM);
    notice.setText(game.notice);
    saving.setText(saveMessage + (paused ? " · Paused" : ""));
    int count = game.plantablePlots(selectedCrop);
    plantAll.setText("Plant all\n" + count + " · " + count * Game.SEED[selectedCrop] + "c");
    plantAll.setContentDescription("Plant " + count + " empty plots with " + Game.CROPS[selectedCrop]
        + " for " + count * Game.SEED[selectedCrop] + " coins");
    plantAll.setEnabled(available && count > 0);
    harvest.setText("Harvest\n" + game.readyPlots() + " ready");
    harvest.setEnabled(available && game.readyPlots() > 0);
    harvest.colors(game.readyPlots() > 0 ? GOLD : 0xffe9e5d3, 0xffd2cba9, INK);
    phase.setText(game.defeated ? "Rebuild\nFree" : game.night
        ? "Night watch\n" + (int) Math.ceil(game.focusClock) + "s focus" : "Start night");
    phase.setEnabled(!storageBlocked && !ioBusy && (!game.night || game.defeated));
    phase.colors(0xff536f48, 0xffa8bc87, CREAM);
    for (int i = 0; i < 3; i++) {
      boolean unlocked = i < game.unlockedCrops(), selected = selectedCrop == i;
      cropButtons[i].setText(unlocked ? Game.CROPS[i] + "\n" + Game.SEED[i] + "c · sell " + Game.SALE[i]
          : Game.CROPS[i] + "\nLevel " + (i + 1));
      cropButtons[i].setEnabled(unlocked && !storageBlocked && !ioBusy);
      cropButtons[i].setSelected(selected);
      cropButtons[i].setContentDescription(Game.CROPS[i] + (unlocked
          ? ", seed " + Game.SEED[i] + " coins, sells for " + Game.SALE[i] + " coins" + (selected ? ", selected" : ", tap to select")
          : ", unlocks at level " + (i + 1)));
      cropButtons[i].colors(selected ? GOLD : 0xffe9e5d3, selected ? 0xffffe6a6 : 0xff9baf83, INK);
      int threats = 0;
      for (Game.Enemy enemy : game.enemies) if (enemy.lane == i) threats++;
      String laneName = new String[] {"Top", "Middle", "Bottom"}[i];
      laneButtons[i].setText(laneName + (game.lane == i ? " ✓" : "") + " · " + threats
          + (game.night ? "\n" + (game.focusClock > 0 ? (int) Math.ceil(game.focusClock) + "s" : "Tap to focus") : "\nDefense lane"));
      laneButtons[i].setContentDescription(laneName + " defense lane, " + threats + " threats, "
          + (game.lane == i ? "selected, " : "") + (game.focusClock > 0 ? "focus recharging" : "tap for focus shot at night"));
      laneButtons[i].setEnabled(available);
      laneButtons[i].colors(game.lane == i ? 0x22f0cd7b : 0x001f392a,
          game.lane == i ? GOLD : 0x667e9b69, game.night ? CREAM : INK);
    }
    for (int i = 0; i < 9; i++) {
      boolean unlocked = i < game.plots();
      int crop = game.crop[i];
      boolean ripe = crop >= 0 && game.growth[i] >= 1;
      String label = !unlocked ? "Expand\n200c" : crop < 0 ? "+ Plant"
          : ripe ? "Harvest +" + Game.SALE[crop] + "c" : Game.CROPS[crop] + " · " + game.harvestSeconds(i) + "s";
      plotButtons[i].setText(label);
      plotButtons[i].setEnabled(available);
      plotButtons[i].setContentDescription("Plot " + (i + 1) + ", " + (!unlocked ? "locked, tap to open land expansion" : crop < 0
          ? "empty, plant " + Game.CROPS[selectedCrop] + " for " + Game.SEED[selectedCrop] + " coins"
          : Game.CROPS[crop] + (ripe ? ", ready, tap to harvest for " + Game.SALE[crop] + " coins"
              : ", growing, " + game.harvestSeconds(i) + " seconds remaining")));
      plotButtons[i].colors(0x00583c28, ripe ? GOLD : unlocked ? 0xffac9365 : 0xff667e59,
          ripe ? 0xffffe59a : unlocked ? CREAM : 0xffd3dfc2);
    }
    String[] upgrades = {
      "Rifle · " + Game.WEAPONS[game.weapon] + (game.maxed(0) ? "" : " → " + Game.WEAPONS[game.weapon + 1]) + "\n+7 damage · faster auto-fire",
      "Fence · tier " + (game.fence + 1) + "\n+50 maximum strength",
      "Irrigation · tier " + game.irrigation + "\n+20% base growth speed",
      "Land expansion\n6 → 9 plots"
    };
    for (int i = 0; i < 4; i++) {
      upgradeButtons[i].setText(upgrades[i] + (game.maxed(i) ? " · MAX" : "\n" + game.cost(i) + " coins"
          + (game.coins < game.cost(i) ? " · need " + (game.cost(i) - game.coins) + " more" : "")));
      upgradeButtons[i].setEnabled(available && !game.maxed(i) && game.coins >= game.cost(i));
    }
    scene.invalidate();
  }

  private void welcome() {
    paused = true;
    new AlertDialog.Builder(this)
        .setTitle("Your farm. Your last safe place.")
        .setMessage(
            "1  Tap a seed card at the top, then an empty soil tile. Tap a golden harvest tile to sell it.\n\n"
                + "2  Open Build in the bottom dock for upgrades. Tap the fence badge to repair.\n\n"
                + "3  Nights bring zombies. Tap a defense lane in the world for a"
                + " focus shot.\n\n"
                + "Pause any time. No energy timers, ads or internet. Your farm grows only while"
                + " you play.")
        .setPositiveButton(
            "Plant the first seed",
            (d, w) -> {
              game.tutorialDone = true;
              paused = false;
              lastFrame = SystemClock.elapsedRealtime();
              changed();
            })
        .setNegativeButton("Later", (d, w) -> {})
        .show();
  }

  private void options() {
    paused = true;
    if (buildOpen) closeBuild();
    refresh();
    String[] items = {
      "How to play",
      game.relaxed ? "Relaxed mode: ON (half fence damage)" : "Relaxed mode: OFF",
      game.reducedMotion ? "Reduced motion: ON" : "Reduced motion: OFF",
      "Export save backup",
      "Import save backup",
      "New farm (reset progress)",
      "Resume game"
    };
    new AlertDialog.Builder(this)
        .setTitle("Take a breather")
        .setItems(
            items,
            (dialog, which) -> {
              if (which == 0) welcome();
              else if (which == 1) {
                game.relaxed = !game.relaxed;
                changed();
                options();
              } else if (which == 2) {
                game.reducedMotion = !game.reducedMotion;
                changed();
                options();
              } else if (which == 3) exportSave();
              else if (which == 4)
                new AlertDialog.Builder(this)
                    .setTitle("Replace this farm?")
                    .setMessage(
                        "Import replaces current progress. Export a backup first if you want to"
                            + " keep it.")
                    .setPositiveButton("Choose backup", (d, w) -> selectImport())
                    .setNegativeButton("Cancel", null)
                    .show();
              else if (which == 5)
                new AlertDialog.Builder(this)
                    .setTitle("Start a new farm?")
                    .setMessage(
                        "This replaces local progress. Export a backup to keep your current farm.")
                    .setPositiveButton(
                        "Create new farm",
                        (d, w) -> {
                          long rev = game.revision;
                          game = new Game();
                          game.revision = rev;
                          storageBlocked = false;
                          selectedCrop = 0;
                          closeBuild();
                          changed();
                          welcome();
                        })
                    .setNegativeButton("Cancel", null)
                    .show();
              else if (!storageBlocked && !ioBusy) {
                paused = false;
                lastFrame = SystemClock.elapsedRealtime();
                refresh();
              }
            })
        .setNegativeButton("Close", null)
        .show();
  }

  private void exportSave() {
    if (storageBlocked || ioBusy) {
      toast("Resolve the save error by importing or creating a new farm first.");
      return;
    }
    Intent intent =
        new Intent(Intent.ACTION_CREATE_DOCUMENT)
            .setType("application/octet-stream")
            .addCategory(Intent.CATEGORY_OPENABLE)
            .putExtra(Intent.EXTRA_TITLE, "Beyond-the-Fence-Day-" + game.day + ".btfs");
    try {
      startActivityForResult(intent, 41);
    } catch (ActivityNotFoundException e) {
      toast("No file provider is installed on this phone.");
    }
  }

  private void selectImport() {
    if (ioBusy) return;
    Intent intent =
        new Intent(Intent.ACTION_OPEN_DOCUMENT)
            .setType("*/*")
            .addCategory(Intent.CATEGORY_OPENABLE);
    try {
      startActivityForResult(intent, 42);
    } catch (ActivityNotFoundException e) {
      toast("No file provider is installed on this phone.");
    }
  }

  @Override
  protected void onActivityResult(int request, int result, Intent data) {
    super.onActivityResult(request, result, data);
    paused = true;
    if (result != RESULT_OK || data == null || data.getData() == null) return;
    Uri uri = data.getData();
    if (request != 41 && request != 42) return;
    ioBusy = true;
    refresh();
    final byte[] snapshot;
    try {
      snapshot = SaveCodec.encode(game);
    } catch (IOException e) {
      ioBusy = false;
      toast("Could not prepare save.");
      return;
    }
    fileWorker.execute(
        () -> {
          try {
            if (request == 41) {
              try (OutputStream out = getContentResolver().openOutputStream(uri, "wt")) {
                if (out == null) throw new IOException();
                out.write(snapshot);
                out.flush();
              }
              handler.post(
                  () -> {
                    ioBusy = false;
                    if (!isDestroyed()) toast("Backup exported. Keep it before uninstalling.");
                    refresh();
                  });
            } else {
              Game loaded;
              try (InputStream in = getContentResolver().openInputStream(uri)) {
                loaded = SaveCodec.decode(SaveCodec.readBounded(in));
              }
              handler.post(
                  () -> {
                    if (isDestroyed()) return;
                    ioBusy = false;
                    loaded.revision = Math.max(game.revision, loaded.revision);
                    game = loaded;
                    storageBlocked = false;
                    selectedCrop = 0;
                    closeBuild();
                    changed();
                    toast("Backup restored. Tap Play when ready.");
                  });
            }
          } catch (IOException | SecurityException e) {
            handler.post(
                () -> {
                  ioBusy = false;
                  if (!isDestroyed()) {
                    toast(
                        request == 41
                            ? "Export failed. Try a different location."
                            : "Import rejected. Current farm kept.");
                    refresh();
                  }
                });
          }
        });
  }

  private void toast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

  private LinearLayout column() {
    LinearLayout l = new LinearLayout(this);
    l.setOrientation(LinearLayout.VERTICAL);
    return l;
  }

  private LinearLayout row() {
    LinearLayout l = new LinearLayout(this);
    l.setOrientation(LinearLayout.HORIZONTAL);
    l.setGravity(Gravity.CENTER_VERTICAL);
    return l;
  }

  private TextView text(String value, int size, int color) {
    TextView t = new TextView(this);
    t.setText(value);
    t.setTextSize(size);
    t.setTextColor(color);
    return t;
  }

  private GameButton button(String label, Runnable click) {
    GameButton b = new GameButton(this);
    b.setText(label);
    b.setOnClickListener(v -> click.run());
    return b;
  }

  private int dp(int value) {
    return Math.round(value * getResources().getDisplayMetrics().density);
  }
}
