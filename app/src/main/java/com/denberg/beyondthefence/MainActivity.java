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
  SurvivalMusic music;
  boolean musicEnabled;
  boolean paused = true, storageBlocked;
  private boolean foreground, ioBusy;
  private int selectedCrop, buildTab, selectedWall;
  private boolean buildOpen;
  private long lastFrame, lastUi, lastSave;
  private TextView stats, status, notice, saving;
  private GameButton play, phase, harvest, plantAll, repair, build, seedsToggle, hireButton, repairOverlay, materialsButton, patrolButton;
  private final GameButton[] cropButtons = new GameButton[3],
      plotButtons = new GameButton[9], upgradeButtons = new GameButton[4],
      wallButtons = new GameButton[4], buildingButtons = new GameButton[3],
      wallPurchase = new GameButton[4], construction = new GameButton[3], buildTabs = new GameButton[4], rallyButtons = new GameButton[4];
  private LinearLayout seedTray, buildItems;
  private TextView buildResources, roster;
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
          boolean running = !paused && !storageBlocked && !ioBusy;
          if (running) {
            float dt = Math.min(.1f, (now - lastFrame) / 1000f);
            game.tick(dt);
            if (game.defeated) {
              paused = true;
              save();
            }
            if (now - lastSave >= 3000) save();
          }
          lastFrame = now;
          if (running && (now - lastUi >= 250 || game.defeated)) {
            refresh();
            lastUi = now;
          }
          // Paused screens are static. Actions and save callbacks update their own controls;
          // repeatedly rebuilding text/layout here starves input on older Android devices.
          if (running) scene.invalidate();
          handler.postDelayed(this, !running || game.reducedMotion ? 100 : 33);
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
      buildTab = Math.max(0, Math.min(3, state.getInt("buildTab")));
      selectedWall = Math.max(0, Math.min(3, state.getInt("wall")));
      selectedCrop = Math.max(0, Math.min(game.unlockedCrops() - 1, state.getInt("seed")));
    }
    musicEnabled = getPreferences(MODE_PRIVATE).getBoolean("music", true);
    music = new SurvivalMusic(this);
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
    state.putInt("buildTab", buildTab);
    state.putInt("wall", selectedWall);
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
    if (!hasFocus && music != null) music.update(false);
    if (!hasFocus && game != null && !paused) {
      paused = true;
      save();
      refresh();
    }
  }

  @Override
  protected void onPause() {
    foreground = false;
    if (music != null) music.update(false);
    paused = true;
    if (session.observer == this) session.observer = null;
    handler.removeCallbacks(frame);
    if (!storageBlocked) save();
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    handler.removeCallbacksAndMessages(null);
    if (music != null) music.close();
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
    repair = button("Squad", () -> showBuild(2));
    repair.setTag("squad");
    repair.setTextSize(11);
    hud.addView(repair, new LinearLayout.LayoutParams(dp(64), -2));
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

    seedTray = row();
    seedTray.setVisibility(View.GONE);
    for (int i = 0; i < 3; i++) {
      final int type = i;
      cropButtons[i] = button("", () -> {
        if (storageBlocked || ioBusy || buildOpen) return;
        selectedCrop = type;
        seedTray.setVisibility(View.GONE);
        game.notice = Game.CROPS[type] + " seeds selected. Tap a farm plot inside the fence.";
        refresh();
      });
      cropButtons[i].setTag("seed" + i);
      cropButtons[i].setCompoundDrawablesRelativeWithIntrinsicBounds(new CropIcon(i, dp(26)), null, null, null);
      cropButtons[i].setCompoundDrawablePadding(dp(3));
      cropButtons[i].setTextSize(11);
      LinearLayout.LayoutParams card = new LinearLayout.LayoutParams(0, -2, 1);
      card.setMargins(dp(2), dp(4), dp(2), dp(4));
      seedTray.addView(cropButtons[i], card);
    }


    board = new GameBoard(this, () -> game, () -> paused);
    scene = board.artwork;
    for (int i = 0; i < 9; i++) {
      final int plot = i;
      plotButtons[i] = button("", () -> {
        if (!canAct() || buildOpen) return;
        if (plot >= game.plots()) { showBuild(3); return; }
        if (game.crop[plot] < 0) game.plant(plot, selectedCrop);
        else if (!game.harvest(plot)) game.notice = "Still growing · " + game.harvestSeconds(plot) + "s to harvest.";
        changed();
      });
      plotButtons[i].setTag("plot" + i);
      plotButtons[i].setTextSize(11);
      plotButtons[i].setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
      board.plot(i, plotButtons[i]);
    }
    for (int i = 0; i < 4; i++) {
      final int side = i;
      wallButtons[i] = button("", () -> { selectedWall = side; showBuild(0); });
      wallButtons[i].setTag("wall" + i);
      wallButtons[i].setTextSize(10);
      board.wall(i, wallButtons[i]);
    }
    for (int i = 0; i < 3; i++) {
      final int kind = i;
      buildingButtons[i] = button("", () -> showBuild(kind == 2 && game.buildings[2] && game.buildingHealth[2] >= Game.BUILDING_HEALTH ? 2 : 1));
      buildingButtons[i].setTag("building" + i);
      buildingButtons[i].setTextSize(11);
      buildingButtons[i].setPadding(dp(2), dp(4), dp(2), dp(4));
      buildingButtons[i].setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
      board.building(i, buildingButtons[i]);
    }
    boardContent.addView(board, new LinearLayout.LayoutParams(-1, board.minimumBoardHeight(), 1));
    notice = text("", 12, 0xfff1d692);
    notice.setPadding(dp(6), dp(4), dp(6), dp(4));
    notice.setMaxLines(2);
    notice.setEllipsize(TextUtils.TruncateAt.END);
    notice.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
    boardContent.addView(notice);

    boardContent.addView(seedTray);
    LinearLayout dock = row();
    seedsToggle = button("Seeds", () -> {
      if (storageBlocked || ioBusy) return;
      seedTray.setVisibility(seedTray.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    });
    seedsToggle.setTag("seeds");
    build = button("Build", this::showBuild);
    build.setTag("build");
    plantAll = button("Plant all", () -> { if (canAct()) { game.plantAll(selectedCrop); changed(); } });
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
    for (GameButton action : new GameButton[] {seedsToggle, build, harvest, phase}) {
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
    TextView title = text("FORTRESS", 20, INK);
    title.setTypeface(null, Typeface.BOLD);
    bar.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
    GameButton close = button("✕", this::closeBuild);
    close.setTag("closeBuild");
    close.setContentDescription("Close upgrades and return to the paused game");
    bar.addView(close, new LinearLayout.LayoutParams(dp(48), -2));
    card.addView(bar);
    buildResources = text("", 12, MUTED);
    buildResources.setPadding(dp(4), 0, 0, dp(6));
    card.addView(buildResources);
    LinearLayout navigation = row();
    String[] labels = {"Fence", "Farm", "Squad", "Gear"};
    for (int i = 0; i < 4; i++) {
      final int page = i;
      buildTabs[i] = button(labels[i], () -> { buildTab = page; rebuildBuildItems(); refresh(); });
      buildTabs[i].setTag("buildTab" + i);
      navigation.addView(buildTabs[i], new LinearLayout.LayoutParams(0, -2, 1));
    }
    card.addView(navigation);
    ScrollView scroll = new ScrollView(this);
    buildItems = column();
    scroll.addView(buildItems);
    card.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
    rebuildBuildItems();
    int cardWidth = Math.min(dp(480), getResources().getDisplayMetrics().widthPixels - dp(40));
    FrameLayout.LayoutParams centered = new FrameLayout.LayoutParams(cardWidth, -1, Gravity.CENTER);
    buildOverlay.addView(card, centered);
    window.addView(buildOverlay, new FrameLayout.LayoutParams(-1, -1));
  }

  private void addBuildAction(GameButton action) {
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
    params.setMargins(0, dp(4), 0, 0);
    buildItems.addView(action, params);
  }

  private void rebuildBuildItems() {
    buildItems.removeAllViews();
    java.util.Arrays.fill(upgradeButtons, null);
    java.util.Arrays.fill(wallPurchase, null);
    java.util.Arrays.fill(construction, null);
    hireButton = null; repairOverlay = null; materialsButton = null; patrolButton = null; roster = null;
    java.util.Arrays.fill(rallyButtons, null);
    if (buildTab == 0) {
      materialsButton = button("Buy 10 timber · 12 coins", () -> { if (canAct()) { game.buyTimber(); changed(); } });
      materialsButton.setTag("buyTimber"); addBuildAction(materialsButton);
      repairOverlay = button("", () -> { if (canAct()) { game.repair(); changed(); } });
      repairOverlay.setTag("repair"); addBuildAction(repairOverlay);
      for (int i = 0; i < 4; i++) {
        final int side = i;
        wallPurchase[i] = button("", () -> { if (canAct()) { if (game.wallDamaged(side)) game.repairWall(side); else game.buildWall(side); changed(); } });
        wallPurchase[i].setTag("constructWall" + i); addBuildAction(wallPurchase[i]);
      }
    } else if (buildTab == 1) {
      for (int i = 0; i < 3; i++) {
        final int kind = i;
        construction[i] = button("", () -> { if (canAct()) { if (game.buildings[kind]) game.repairBuilding(kind); else game.construct(kind); changed(); } });
        construction[i].setTag("construct" + i); addBuildAction(construction[i]);
      }
    } else if (buildTab == 2) {
      hireButton = button("", () -> { if (canAct()) { game.hire(); changed(); } });
      hireButton.setTag("hire"); addBuildAction(hireButton);
      roster = text("", 13, INK); roster.setPadding(dp(6), dp(8), dp(6), dp(8));
      buildItems.addView(roster);
      LinearLayout rally = row();
      for (int i = 0; i < 4; i++) {
        final int side = i;
        GameButton action = button(Game.SIDES[i].substring(0, 1), () -> {
          if (canAct()) { game.rally(side); changed(); }
        });
        action.setContentDescription("Move mercenaries to defend the " + Game.SIDES[i] + " fence");
        rallyButtons[i] = action;
        action.setTag("rally" + i);
        rally.addView(action, new LinearLayout.LayoutParams(0, -2, 1));
      }
      buildItems.addView(text("Rally guards to a border", 12, MUTED));
      buildItems.addView(rally);
      patrolButton = button("Resume perimeter patrol", () -> { if (canAct()) { game.resumePatrol(); changed(); } });
      patrolButton.setTag("patrol"); addBuildAction(patrolButton);
    } else {
      for (int i = 0; i < 4; i++) {
        final int kind = i;
        upgradeButtons[i] = button("", () -> { if (canAct()) { game.upgrade(kind); changed(); } });
        upgradeButtons[i].setTag("upgrade" + i); addBuildAction(upgradeButtons[i]);
      }
      plantAll.setTag("plantAll"); addBuildAction(plantAll);
    }
  }

  private void showBuild(int page) { buildTab = page; showBuild(); }

  private void showBuild() {
    if (ioBusy) return;
    paused = true;
    seedTray.setVisibility(View.GONE);
    buildOpen = true;
    rebuildBuildItems();
    save();
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

  @android.annotation.SuppressLint("SetTextI18n") // English-only arcade candidate.
  void refresh() {
    if (stats == null) return;
    boolean available = !storageBlocked && !ioBusy && !game.defeated;
    label(stats, game.coins + " coins · " + game.wood + " timber");
    label(status, "Day " + game.day + " · " + Game.WEATHER[game.weather] + " · Farm "
        + Math.round(game.health) + "/" + game.maxHealth());
    stats.setContentDescription(game.coins + " coins, " + game.wood + " timber");
    label(play, game.defeated ? "Rebuild" : paused ? "Play" : "Pause");
    play.setContentDescription(game.defeated ? "Recover homestead free and resume" : paused ? "Resume game" : "Pause game");
    play.setEnabled(!storageBlocked && !ioBusy && !buildOpen);
    play.colors(paused ? GOLD : 0xff314e40, GOLD, paused ? INK : CREAM);
    label(repair, "Crew\n" + game.mercenaries.size() + "/" + Game.MAX_MERCS);
    repair.setContentDescription("Open mercenary recruitment, training and rally controls");
    label(seedsToggle, "Seeds\n" + Game.CROPS[selectedCrop]);
    label(notice, game.notice);
    label(saving, saveMessage + (paused ? " · Paused" : ""));
    int count = game.plantablePlots(selectedCrop);
    label(plantAll, "Plant all · " + count + " plots · " + count * Game.SEED[selectedCrop] + " coins");
    plantAll.setEnabled(available && count > 0);
    label(harvest, "Harvest\n" + game.readyPlots() + " ready");
    harvest.setEnabled(available && game.readyPlots() > 0);
    harvest.colors(game.readyPlots() > 0 ? GOLD : 0xffe9e5d3, 0xffd2cba9, INK);
    label(phase, game.defeated ? "Rebuild\nFree" : game.night ? "Raid " + game.day + "\n" + game.enemies.size() + " foes" : "Raid in\n" + game.raidInSeconds() + "s");
    phase.setContentDescription(game.defeated ? "Rebuild fortress free" : game.night ? "Raid in progress" : "Start the next zombie raid now, automatic raid in " + game.raidInSeconds() + " seconds");
    phase.setEnabled(!storageBlocked && !ioBusy && (!game.night || game.defeated));
    phase.colors(0xff536f48, 0xffa8bc87, CREAM);
    for (int i = 0; i < 3; i++) {
      boolean unlocked = i < game.unlockedCrops(), selected = selectedCrop == i;
      label(cropButtons[i], unlocked ? Game.CROPS[i] + "\n" + Game.SEED[i] + "c · sell " + game.sale(i)
          : Game.CROPS[i] + "\nDay " + (i + 1));
      cropButtons[i].setEnabled(unlocked && !storageBlocked && !ioBusy);
      cropButtons[i].setSelected(selected);
      cropButtons[i].setContentDescription(Game.CROPS[i] + (unlocked ? ", seed " + Game.SEED[i] + " coins, sells for " + game.sale(i) + " coins and " + (2 + 2 * i) + " timber" + (selected ? ", selected" : "") : ", available on day " + (i + 1)));
      cropButtons[i].colors(selected ? GOLD : 0xffe9e5d3, selected ? GOLD : 0xff9baf83, INK);
      label(buildingButtons[i], new String[] {"Mill", "Shop", "Camp"}[i]
          + (game.buildingRuins[i] ? "\nRuins" : game.buildings[i] && game.buildingHealth[i] < Game.BUILDING_HEALTH
              ? "\n" + Math.round(game.buildingHealth[i]) + "HP" : ""));
      buildingButtons[i].setEnabled(available);
      buildingButtons[i].setContentDescription(Game.BUILDINGS[i] + (game.buildings[i] ? ", health " + Math.round(game.buildingHealth[i]) + " of100, tap to manage" : ", construction plot, tap to build for " + Game.BUILD_COINS[i] + " coins and " + Game.BUILD_WOOD[i] + " timber"));
      buildingButtons[i].colors(0x00583c28, game.buildings[i] ? GOLD : 0xffb6c68b, INK);
      if (construction[i] != null) {
        boolean damaged = game.buildings[i] && game.buildingHealth[i] < Game.BUILDING_HEALTH;
        label(construction[i], Game.BUILDINGS[i] + (damaged ? " · REPAIR\n10 coins · 5 timber · +60 health"
            : game.buildings[i] ? " · BUILT · 100 health" : (game.buildingRuins[i] ? " · REBUILD" : " · BUILD")
                + "\n" + Game.BUILD_COINS[i] + " coins · " + Game.BUILD_WOOD[i] + " timber")
            + "\n" + new String[] {"Produces4 timber /12 active seconds", "+25% crop sale value", "Unlocks mercenary recruitment"}[i]);
        construction[i].setEnabled(available && (damaged ? game.coins >= 10 && game.wood >= 5
            : !game.buildings[i] && game.coins >= Game.BUILD_COINS[i] && game.wood >= Game.BUILD_WOOD[i]));
      }
    }
    for (int i = 0; i < 4; i++) {
      int tier = game.walls[i];
      boolean damaged = game.wallDamaged(i), breached = tier > 0 && game.wallHealth[i] <= 0;
      String marker = tier == 0 ? "Gap" : Math.round(game.wallHealth[i]) + "HP";
      label(wallButtons[i], Game.SIDES[i].substring(0, 1)
          + (getResources().getConfiguration().fontScale >= 1.5f ? "" : "\n" + marker));
      wallButtons[i].setContentDescription(Game.SIDES[i] + " fence, " + (tier == 0 ? "open gap" : Game.FENCE_MATERIALS[tier] + " material, tier " + tier
          + ", health " + Math.round(game.wallHealth[i]) + " of " + Math.round(game.maxWallHealth(i))
          + (breached ? ", breached, zombies can enter" : "")) + ", tap to build or repair");
      wallButtons[i].colors(breached ? 0xdd853c32 : damaged ? 0xdd77532e : tier == 0 ? 0xcc5f4630 : 0xcc263e32,
          selectedWall == i ? GOLD : 0xffc0a274, CREAM);
      if (wallPurchase[i] != null) {
        label(wallPurchase[i], Game.SIDES[i] + " · " + Game.FENCE_MATERIALS[tier] + " · " + Math.round(game.wallHealth[i]) + "/" + Math.round(game.maxWallHealth(i)) + "HP\n" + (damaged ? "REPAIR · " + Math.round(game.wallHealth[i]) + "/"
            + Math.round(game.maxWallHealth(i)) + "HP\n8 coins · 4 timber · +50 health"
            : tier == 0 ? "BUILD WOOD FENCE\n" + game.wallCoinCost(i) + " coins · " + game.wallWoodCost(i) + " timber"
            : tier >= 3 ? "Steel · MAX · " + Math.round(game.wallHealth[i]) + "HP"
            : "UPGRADE " + Game.FENCE_MATERIALS[tier] + " → " + Game.FENCE_MATERIALS[tier + 1] + "\n" + game.wallCoinCost(i) + " coins · " + game.wallWoodCost(i) + " timber")
            + "\n" + (damaged ? "Closes the breach for new zombies; fight intruders inside" : "Material kits strengthen fences; coins buy supplies and timber frames them"));
        wallPurchase[i].setEnabled(available && (damaged ? game.coins >= 8 && game.wood >= 4
            : tier < 3 && game.coins >= game.wallCoinCost(i) && game.wood >= game.wallWoodCost(i)));
        wallPurchase[i].colors(selectedWall == i ? GOLD : 0xffe9e5d3, 0xffb6a274, INK);
      }
      buildTabs[i].colors(buildTab == i ? GOLD : 0xffe9e5d3, 0xffb6a274, INK);
    }
    for (int i = 0; i < 9; i++) {
      boolean unlocked = i < game.plots(); int crop = game.crop[i]; boolean ripe = crop >= 0 && game.growth[i] >= 1;
      label(plotButtons[i], !unlocked ? "Land +" : crop < 0 ? game.scorched[i] ? "Replant" : "+" : ripe ? "+" + game.sale(crop) + "c" : game.harvestSeconds(i) + "s");
      plotButtons[i].setEnabled(available);
      plotButtons[i].setContentDescription("Farm plot " + (i + 1) + ", " + (!unlocked ? "locked, tap for expansion" : crop < 0
          ? (game.scorched[i] ? "crop destroyed, replant " : "empty, plant ") + Game.CROPS[selectedCrop] + " for " + Game.SEED[selectedCrop] + " coins"
          : Game.CROPS[crop] + ", health " + Math.round(game.cropHealth[i]) + " of40" + (ripe ? ", tap to harvest " + game.sale(crop) + " coins and " + (2 + crop * 2) + " timber" : ", growing, " + game.harvestSeconds(i) + " seconds")));
      plotButtons[i].colors(0x00583c28, ripe ? GOLD : unlocked ? 0xffb9965c : 0xff8cb061, ripe ? GOLD : CREAM);
    }
    if (materialsButton != null) materialsButton.setEnabled(available && game.coins >= 12 && game.wood <= 9_999_990);
    if (repairOverlay != null) {
      label(repairOverlay, "Repair homestead · 8 coins\n+40 health · " + Math.round(game.health) + "/" + game.maxHealth());
      repairOverlay.setEnabled(available && game.health < game.maxHealth() && game.coins >= 8);
    }
    if (hireButton != null) {
      label(hireButton, !game.buildings[2] ? "Build Barracks first in Farm" : game.mercenaries.size() >= Game.MAX_MERCS ? "Squad full · 3 survivors" : "Hire mercenary · " + game.hireCost() + " coins · 6 timber");
      hireButton.setEnabled(available && game.buildings[2] && game.mercenaries.size() < Game.MAX_MERCS && game.coins >= game.hireCost() && game.wood >= 6);
      label(roster, "Mercenaries " + game.mercenaries.size() + "/3 · " + Game.UNIFORMS[game.weapon]
          + "\n" + game.guardDamage() + " damage per shot · " + (game.lane < 0 ? "Patrolling" : "Rally: " + Game.SIDES[game.lane])
          + "\nShort range: rally to threatened borders. Breached zombies can kill guards. Crew heals during preparation.");
      for (int side = 0; side < 4; side++) if (rallyButtons[side] != null)
        rallyButtons[side].colors(game.lane == side ? GOLD : 0xffe9e5d3, 0xffb6a274, INK);
      if (patrolButton != null) patrolButton.setEnabled(available && game.lane >= 0);
    }
    String[] upgrades = {"Uniform · " + Game.UNIFORMS[game.weapon] + "\n+3 damage · +20 crew health · armor · faster fire", "Fortify · tier " + (game.fence + 1) + "\n+50 farm health · +15 health per built fence", "Irrigation · tier " + game.irrigation + "\n+20% growth", "Farm expansion · 6 → 9 plots"};
    for (int i = 0; i < 4; i++) if (upgradeButtons[i] != null) {
      label(upgradeButtons[i], upgrades[i] + (game.maxed(i) ? " · MAX" : "\n" + game.cost(i) + " coins"));
      upgradeButtons[i].setEnabled(available && !game.maxed(i) && game.coins >= game.cost(i));
    }
    label(buildResources, game.coins + " coins · " + game.wood + " timber · game paused");
    if (music != null) music.update(musicEnabled && foreground && hasWindowFocus() && !paused && game.night && !game.defeated && !storageBlocked && !ioBusy);
    scene.invalidate();
  }

  private void welcome() {
    paused = true;
    new AlertDialog.Builder(this)
        .setTitle("Your farm. Your last safe place.")
        .setMessage(
            "1  Open Seeds, then tap farm plots inside your fortress. Harvest ripe crops for coins and timber.\n\n"
                + "2  Fence bars and HP show damage. Upgrade Wood to Stone to Steel. Zombies damage individual fences. A breach lets them enter and destroy crops and buildings. Tap N/E/S/W to build or repair; Fence sells 10 timber for 12 harvest coins.\n\n"
                + "3  One volunteer guard starts with you. Your mercenaries are the only fighters. Harvest to build barracks and hire more through Crew. Maximum three mercenaries. Gear upgrades their uniforms, firepower and armor; Crew can rally them to a border or resume patrol.\n\n"
                + "4  Zombies roam the terrain outside your compact farm. Each day brings a faster, stronger horde. Runners rush, Brutes smash, Spitters attack at range. Raids end only when every zombie is killed. A breach can wipe out your crew and farm. Tap Raid in to start a raid early. Mercenaries fight automatically within range. Progress advances only while playing.")
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
      game.relaxed ? "Relaxed mode: ON (half zombie damage)" : "Relaxed mode: OFF",
      game.reducedMotion ? "Reduced motion: ON" : "Reduced motion: OFF",
      "Export save backup",
      "Import save backup",
      "New farm (reset progress)",
      "Resume game",
      musicEnabled ? "Action music: ON" : "Action music: OFF"
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
              else if (which == 7) {
                musicEnabled = !musicEnabled;
                getPreferences(MODE_PRIVATE).edit().putBoolean("music", musicEnabled).apply();
                refresh(); options();
              } else if (!storageBlocked && !ioBusy) {
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

  // TextView.setText requests layout even when the displayed characters are unchanged.
  // Keep animated raids responsive on older devices and large-font layouts.
  private void label(TextView view, CharSequence value) {
    if (!TextUtils.equals(view.getText(), value)) view.setText(value);
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
