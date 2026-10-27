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
  boolean musicEnabled, effectsEnabled;
  boolean paused = true, storageBlocked;
  private boolean foreground, ioBusy;
  private int selectedCrop, buildTab, selectedWall = -1, selectedBuilding = -1;
  private boolean buildOpen;
  private long lastFrame, lastUi, lastSave;
  private TextView survived, stats, status, notice, saving, buildTitle;
  private GameButton quickUniform, fenceUpgrade, shopHarvest, shopPlant, heroBuy;
  private final GameButton[] itemSlots = new GameButton[3], itemBuy = new GameButton[4], advanced = new GameButton[3];
  private final int[] slotIcons = {-1, -1, -1};
  private GameButton costButton;
  private String costText;
  private long costToken;
  private GameButton play, phase, harvest, repair, build, seedsToggle, hireButton, repairOverlay, materialsButton, patrolButton;
  private final GameButton[] cropButtons = new GameButton[3],
      plotButtons = new GameButton[9], upgradeButtons = new GameButton[4],
      wallButtons = new GameButton[4], buildingButtons = new GameButton[3],
      wallPurchase = new GameButton[4], construction = new GameButton[3], buildTabs = new GameButton[5], rallyButtons = new GameButton[4];
  private LinearLayout seedTray, buildItems;
  private TextView buildResources, roster;
  private FrameLayout window, buildOverlay;
  private LinearLayout boardContent, gameShell;
  private GameBoard board;
  private FarmView scene;
  private TerrainBackdrop terrain;
  private ScrollView buildScroll;
  private int panelRevision;
  private AlertDialog arenaDialog;
  private TextView arenaStatus;
  private ArenaView arenaView;
  private GameButton arenaStrike, arenaPause;
  private final GameButton[] arenaItems = new GameButton[3];
  private boolean adventureDialog, eventQueued, portalShown;
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
            boolean arenaWasActive = game.arenaActive;
            game.tick(dt);
            if (arenaWasActive && !game.arenaActive) { paused = true; save(); refresh(); }
            updateAudio(dt);
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
      buildTab = Math.max(0, Math.min(4, state.getInt("buildTab")));
      selectedWall = Math.max(-1, Math.min(3, state.getInt("wall", -1)));
      selectedBuilding = Math.max(-1, Math.min(2, state.getInt("building", -1)));
      selectedCrop = Math.max(0, Math.min(game.unlockedCrops() - 1, state.getInt("seed")));
    }
    musicEnabled = getPreferences(MODE_PRIVATE).getBoolean("music", true);
    effectsEnabled = getPreferences(MODE_PRIVATE).getBoolean("effects", true);
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
    state.putInt("building", selectedBuilding);
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
    if (!hasFocus && music != null) music.pause();
    if (!hasFocus && game != null && !paused) {
      paused = true;
      save();
      refresh();
    }
  }

  @Override
  protected void onPause() {
    foreground = false;
    if (music != null) music.pause();
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
    terrain = new TerrainBackdrop(this);
    window.addView(terrain, new FrameLayout.LayoutParams(-1, -1));
    gameShell = column();
    window.addView(gameShell, new FrameLayout.LayoutParams(-1, -1));
    ScrollView viewport = new ScrollView(this);
    viewport.setFillViewport(true);
    viewport.setVerticalScrollBarEnabled(false);
    boardContent = column();
    boardContent.setPadding(dp(8), dp(8), dp(8), 0);
    viewport.addView(boardContent);
    gameShell.addView(viewport, new LinearLayout.LayoutParams(-1, 0, 1));

    LinearLayout hud = column();
    LinearLayout hudActions = row(); hudActions.setTag("hudActions"); hudActions.setPadding(0, dp(8), 0, dp(8));
    LinearLayout resources = column();
    survived = text("", 16, CREAM); survived.setTag("survived"); survived.setTypeface(null, Typeface.BOLD);
    resources.addView(survived);
    stats = text("", 12, CREAM);
    stats.setTypeface(null, Typeface.BOLD);
    status = text("", 11, 0xffd0dfc5);
    resources.addView(stats);
    resources.addView(status);
    hud.addView(resources, new LinearLayout.LayoutParams(-1, -2));
    hud.addView(hudActions);
    repair = button("Squad", () -> showBuild(3));
    repair.setTag("squad");
    repair.setTextSize(11);
    hudActions.addView(repair, new LinearLayout.LayoutParams(0, -1, 1));
    play = button("Play", () -> {
      if (storageBlocked || ioBusy || buildOpen) return;
      if (game.defeated) { game.recover(); changed(); }
      paused = !paused;
      lastFrame = SystemClock.elapsedRealtime();
      refresh();
    });
    play.setTag("play");
    LinearLayout.LayoutParams playSize = new LinearLayout.LayoutParams(0, -1, 1);
    playSize.setMargins(dp(4), 0, dp(4), 0); hudActions.addView(play, playSize);
    GameButton menu = button("☰", this::options);
    menu.setContentDescription("Pause and open options, help and save backups");
    menu.setTag("options");
    hudActions.addView(menu, new LinearLayout.LayoutParams(0, -1, 1));
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
        if (plot >= game.plots()) { showBuild(2); return; }
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
      final int kind = i;
      buildingButtons[i] = button("", () -> showBuilding(kind));
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
    notice.setTag("notice");
    notice.setVisibility(View.GONE);
    boardContent.addView(notice);

    gameShell.addView(seedTray);
    createCommandDock();
    LinearLayout dock = row(); dock.setTag("actions"); dock.setPadding(dp(4), 0, dp(4), 0);
    seedsToggle = button("Seeds", () -> {
      if (storageBlocked || ioBusy) return;
      seedTray.setVisibility(seedTray.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    });
    seedsToggle.setTag("seeds");
    build = button("Build", () -> { selectedBuilding = -1; showBuild(); });
    build.setTag("build");
    harvest = button("Harvest", () -> { if (canAct() && !buildOpen) { game.harvestAll(); changed(); } });
    harvest.setTag("harvest");
    phase = button("Night", () -> {
      if (storageBlocked || ioBusy || buildOpen) return;
      if (game.worldComplete) { portalShown = false; showAdventure(); return; }
      if (game.defeated) { game.recover(); paused = true; }
      else { game.startNight(); paused = false; lastFrame = SystemClock.elapsedRealtime(); }
      changed();
    });
    phase.setTag("night");
    for (GameButton action : new GameButton[] {seedsToggle, build, harvest, phase}) {
      action.setTextSize(compactTextSize(12, 18));
      LinearLayout.LayoutParams item = new LinearLayout.LayoutParams(0, -1, 1);
      item.setMargins(dp(4), 0, dp(4), 0);
      dock.addView(action, item);
    }
    gameShell.addView(dock);
    saving = text(saveMessage, 10, 0xffc2d2be);
    saving.setPadding(dp(8), dp(4), dp(8), dp(4));
    for (TextView label : new TextView[] {survived, stats, status, saving}) label.setShadowLayer(dp(2), 0, dp(1), INK);
    gameShell.addView(saving);
    createBuildOverlay();
  }

  private void createCommandDock() {
    LinearLayout controls = row(); controls.setPadding(dp(8), dp(12), dp(8), dp(12));
    controls.setTag("commands");
    LinearLayout leftSlots = column(); leftSlots.setGravity(Gravity.CENTER);
    addItemSlot(leftSlots, 2);
    addItemSlot(leftSlots, 0); controls.addView(leftSlots, new LinearLayout.LayoutParams(0, -1, 1));
    FrameLayout cross = new FrameLayout(this); cross.setTag("fenceCross");
    LinearLayout.LayoutParams crossParams = new LinearLayout.LayoutParams(dp(144), dp(144));
    crossParams.setMargins(dp(8), 0, dp(8), 0); controls.addView(cross, crossParams);
    int[] col = {1, 2, 1, 0}, rows = {0, 1, 2, 1};
    for (int side = 0; side < 4; side++) {
      final int border = side;
      wallButtons[side] = button(Game.SIDES[side].substring(0, 1), () -> {
        if (canAct() && !buildOpen) { clearHint(); selectedWall = border; game.repairWall(border); changed(); }
      });
      wallButtons[side].setTag("wall" + side); wallButtons[side].setTextSize(14);
      FrameLayout.LayoutParams tile = new FrameLayout.LayoutParams(dp(48), dp(48));
      tile.leftMargin = dp(col[side] * 48); tile.topMargin = dp(rows[side] * 48);
      cross.addView(wallButtons[side], tile);
    }
    fenceUpgrade = button("↑", () -> {
      if (canAct() && !buildOpen && selectedWall >= 0) {
        if (!game.upgradeBorder(selectedWall)) showPrice(fenceUpgrade, game.borderCoinCost(selectedWall), game.borderWoodCost(selectedWall)); else clearHint();
        changed();
      }
    });
    fenceUpgrade.setTag("fenceUpgrade"); fenceUpgrade.setTextSize(16);
    FrameLayout.LayoutParams middle = new FrameLayout.LayoutParams(dp(48), dp(48));
    middle.leftMargin = dp(48); middle.topMargin = dp(48); cross.addView(fenceUpgrade, middle);
    quickUniform = button("", () -> { if (canAct() && !buildOpen) {
      if (!game.upgrade(0)) showPrice(quickUniform, game.cost(0), 0); else clearHint(); changed();
    } });
    quickUniform.setTag("quickUniform"); quickUniform.setTextSize(11);
    LinearLayout rightSlots = column(); rightSlots.setGravity(Gravity.CENTER);
    rightSlots.addView(quickUniform, new LinearLayout.LayoutParams(-1, commandButtonHeight()));
    addItemSlot(rightSlots, 1); controls.addView(rightSlots, new LinearLayout.LayoutParams(0, -1, 1));
    gameShell.addView(controls);
  }

  // Keep short dock captions whole while still enlarging them by up to50% with system text.
  private float compactTextSize(float normalSp, float maximumScaledSp) {
    return Math.min(normalSp, maximumScaledSp / getResources().getConfiguration().fontScale);
  }

  private int commandButtonHeight() {
    return dp(Math.max(48, (int)Math.ceil(28 * getResources().getConfiguration().fontScale + 8)));
  }

  private void addItemSlot(LinearLayout side, int slotIndex) {
    GameButton slot = button("", () -> { if (canAct() && !buildOpen) {
      int type = game.equipped[slotIndex];
      if (game.itemStock(type) == 0) { chooseItem(slotIndex); return; }
      if (!game.useItem(type)) showHint(itemSlots[slotIndex], type == 0 ? "Full HP" : type == 1 ? "Active" : "No foes"); else clearHint(); changed();
    } });
    itemSlots[slotIndex] = slot; slot.setTag("itemSlot" + slotIndex); slot.setTextSize(11);
    slot.setCompoundDrawablesRelativeWithIntrinsicBounds(new ItemIcon(game.equipped[slotIndex], dp(30)), null, null, null);
    slot.setCompoundDrawablePadding(dp(4));
    LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(-1, commandButtonHeight()); size.topMargin = slotIndex == 2 ? 0 : dp(8); side.addView(slot, size);
    slot.setOnLongClickListener(v -> { if (canAct() && !buildOpen) chooseItem(slotIndex); return true; });
  }

  private void chooseItem(int slot) {
    paused = true; clearHint(); refresh();
    String[] choices = new String[Game.ITEMS.length];
    for (int i = 0; i < choices.length; i++) choices[i] = Game.ITEMS[i] + " · " + game.itemStock(i) + " owned";
    new AlertDialog.Builder(this).setTitle("Quick slot " + (slot + 1))
        .setItems(choices, (d, choice) -> { game.equipItem(slot, choice); changed(); })
        .setNegativeButton("Close", null).show();
  }

  private void assignButtons() {
    paused = true; refresh();
    String[] slots = new String[3];
    for (int i = 0; i < 3; i++) slots[i] = "Button " + (i + 1) + " · " + Game.ITEMS[game.equipped[i]];
    new AlertDialog.Builder(this).setTitle("Potion or magic")
        .setItems(slots, (d, slot) -> chooseItem(slot)).setNegativeButton("Close", null).show();
  }

  private void addEquipment() {
    int found = 0; for (boolean owned : game.collected) if (owned) found++;
    TextView summary = text(found + "/45 relics · " + game.directorDescription()
        + "\nAttack +" + Math.round(game.equipmentBonus(0) * 100) + "% · Protection +" + Math.round(game.equipmentBonus(1) * 100)
        + "%\nMagic +" + Math.round(game.equipmentBonus(2) * 100) + "% · Speed +" + Math.round(game.equipmentBonus(3) * 100) + "%", 13, INK);
    summary.setPadding(dp(4), dp(8), dp(4), dp(8)); buildItems.addView(summary);
    if (game.lastDrop >= 0) buildItems.addView(text("Latest find · " + Equipment.name(game.lastDrop), 12, MUTED));
    GameButton best = button("Equip best", () -> { if (canAct()) { game.equipBest(); rebuildBuildItems(); changed(); } });
    best.setTag("equipBest"); best.setEnabled(!game.arenaActive && !game.defeated); addBuildAction(best);
    for (int i = 0; i < game.gear.length; i++) {
      final int slot = i; int code = game.gear[i];
      GameButton action = button(Equipment.SLOTS[i] + " · " + (code < 0 ? "Empty" : Equipment.RARITIES[Equipment.rarity(code)]
          + "\n" + Equipment.name(code) + " · " + Equipment.effect(code)), () -> chooseEquipment(slot));
      action.setTag("gear" + i); action.setEnabled(!game.arenaActive && !game.defeated);
      action.setCompoundDrawablesRelativeWithIntrinsicBounds(new EquipmentIcon(i, code < 0 ? -1 : Equipment.rarity(code), dp(32)), null, null, null);
      action.setCompoundDrawablePadding(dp(8)); addBuildAction(action);
    }
    TextView rarity = text("Common · Uncommon · Rare · Epic · Legendary\nScarce zombie finds. Bosses may carry relics. Arena wins can reveal higher tiers. Duplicate relics never stack.\nMaximum bonuses: attack12%, protection12%, magic10%, speed6%.", 12, MUTED);
    rarity.setPadding(dp(4), dp(12), dp(4), dp(8)); buildItems.addView(rarity);
    if (game.arenaOfferDay > 0) {
      GameButton arena = button("Day " + game.arenaOfferDay + " · Arena invitation", () -> { closeBuild(); showAdventure(); });
      arena.setTag("arenaInvitation"); addBuildAction(arena);
    }
  }

  private void chooseEquipment(int slot) {
    if (game.arenaActive || !canAct()) return;
    java.util.ArrayList<Integer> codes = new java.util.ArrayList<>(); codes.add(-1);
    java.util.ArrayList<String> labels = new java.util.ArrayList<>(); labels.add("Unequip");
    int selected = game.gear[slot] == -1 ? 0 : -1;
    for (int rarity = 4; rarity >= 0; rarity--) {
      int code = slot * 5 + rarity;
      if (game.collected[code]) {
        if (code == game.gear[slot]) selected = codes.size();
        codes.add(code); labels.add(Equipment.RARITIES[rarity] + " · " + Equipment.name(code) + "\n" + Equipment.effect(code));
      }
    }
    new AlertDialog.Builder(this).setTitle(Equipment.SLOTS[slot])
        .setSingleChoiceItems(labels.toArray(new String[0]), selected, (d, choice) -> {
          game.equipGear(slot, codes.get(choice)); d.dismiss(); rebuildBuildItems(); changed();
        }).setNegativeButton("Close", null).show();
  }

  private void scheduleAdventure() {
    if (eventQueued || adventureDialog || buildOpen || !foreground || storageBlocked || ioBusy) return;
    if (!game.arenaActive && game.arenaOfferDay == 0 && (!game.worldComplete || portalShown)) return;
    eventQueued = true;
    handler.post(() -> { eventQueued = false; if (!isFinishing() && !isDestroyed() && foreground && !buildOpen && !adventureDialog) showAdventure(); });
  }

  private void showAdventure() {
    if (adventureDialog || storageBlocked || ioBusy) return;
    if (game.arenaActive) { showArena(); return; }
    paused = true; adventureDialog = true;
    if (game.arenaOfferDay > 0) {
      adventurePrompt("A champion awaits",
          "Day " + game.arenaOfferDay + " survived. One foe. One attempt.\n\nWin for a 55% relic chance, including Epic and Legendary finds.\n\nYour farm pauses. Entry is free; used supplies are spent.",
          new String[] {"Enter arena", "Decline", "Equipment"}, new Runnable[] {
            () -> { game.acceptArena(); save(); },
            () -> { game.declineArena(); changed(); },
            () -> showBuild(4)
          });
    } else if (game.worldComplete) {
      portalShown = true;
      adventurePrompt("The rift is open",
          "One hundred dawns. Another sky.\n\nEnter World " + Math.min(999, game.world + 1) + " with your relics, equipment and button assignments.\n\nA fresh farm begins at Day1. Buildings, coins, crew and supplies reset.",
          new String[] {"Cross the rift", "Stay here"}, new Runnable[] {
            () -> {
              Game next = game.crossRift(); if (next != null) {
                game = next; selectedCrop = 0; selectedWall = selectedBuilding = -1; portalShown = false;
                paused = true; changed();
              }
            }, () -> { }
          });
    } else adventureDialog = false;
    refresh();
  }

  private void adventurePrompt(String title, String message, String[] labels, Runnable[] actions) {
    Dialog dialog = new Dialog(this); dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    LinearLayout card = column(); card.setPadding(dp(16), dp(16), dp(16), dp(16));
    GradientDrawable skin = new GradientDrawable(); skin.setColor(CREAM); skin.setCornerRadius(dp(12)); card.setBackground(skin);
    TextView heading = text(title, 20, INK); heading.setTextSize(compactTextSize(20, 28)); heading.setTypeface(null, Typeface.BOLD); card.addView(heading);
    ScrollView scroll = new ScrollView(this);
    TextView copy = text(message, 16, INK); copy.setPadding(0, dp(12), 0, dp(12)); scroll.addView(copy);
    card.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
    int width = getResources().getDisplayMetrics().widthPixels, height = getResources().getDisplayMetrics().heightPixels;
    boolean horizontal = width > height;
    LinearLayout choices = horizontal ? row() : column();
    for (int i = 0; i < labels.length; i++) {
      final int choice = i;
      GameButton action = button(labels[i], () -> { actions[choice].run(); dialog.dismiss(); });
      action.setTextSize(compactTextSize(12, 18)); action.setTag("adventureAction" + i);
      if (i == 0) action.colors(GOLD, GOLD, INK);
      LinearLayout.LayoutParams size = horizontal ? new LinearLayout.LayoutParams(0, -1, 1) : new LinearLayout.LayoutParams(-1, -2);
      size.setMargins(horizontal && i > 0 ? dp(6) : 0, dp(6), 0, 0); choices.addView(action, size);
    }
    card.addView(choices);
    dialog.setContentView(card); dialog.setCancelable(false);
    dialog.setOnDismissListener(d -> { adventureDialog = false; refresh(); });
    dialog.show();
    Window popup = dialog.getWindow(); popup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
    float scale = getResources().getConfiguration().fontScale;
    int preferred = dp(380 + Math.round(170 * Math.min(1, Math.max(0, scale - 1))));
    popup.setLayout(Math.min(dp(480), width - dp(32)), Math.min(preferred, height - dp(128)));
  }

  private void showArena() {
    if (isFinishing() || isDestroyed() || !foreground) return;
    if (arenaDialog != null && arenaDialog.isShowing()) return;
    adventureDialog = true; paused = true;
    LinearLayout content = column(); content.setPadding(dp(16), dp(8), dp(16), dp(8));
    arenaView = new ArenaView(this, game); content.addView(arenaView, new LinearLayout.LayoutParams(-1, dp(148)));
    arenaStatus = text("", 13, INK); arenaStatus.setTag("arenaStatus"); content.addView(arenaStatus);
    arenaStrike = button("Strike", () -> { if (!paused && game.arenaStrike()) changed(); });
    arenaStrike.setTag("arenaStrike"); content.addView(arenaStrike);
    for (int i = 0; i < 3; i++) {
      final int slot = i;
      arenaItems[i] = button("", () -> { if (!paused && game.arenaActive && game.useItem(game.equipped[slot])) changed(); });
      arenaItems[i].setTag("arenaItem" + i); content.addView(arenaItems[i]);
    }
    arenaPause = button("Fight", () -> {
      if (!game.arenaActive) { arenaDialog.dismiss(); arenaDialog = null; refresh(); return; }
      paused = !paused; lastFrame = SystemClock.elapsedRealtime(); refresh();
    }); arenaPause.setTag("arenaPause"); content.addView(arenaPause);
    ScrollView scroll = new ScrollView(this); scroll.addView(content);
    arenaDialog = new AlertDialog.Builder(this).setTitle(Game.CHAMPIONS[game.arenaChampion]).setView(scroll)
        .setNegativeButton("Retreat", null).setCancelable(false)
        .setOnDismissListener(d -> { paused = true; adventureDialog = false; }).create();
    arenaDialog.setOnShowListener(d -> arenaDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
      if (game.arenaActive) game.retreatArena(); paused = true; changed();
    }));
    arenaDialog.show();
    arenaDialog.getWindow().getDecorView().getViewTreeObserver().addOnWindowFocusChangeListener(focused -> {
      if (!focused && game.arenaActive && !paused) {
        paused = true; save(); refresh();
      }
    });
    refresh();
  }

  private void refreshArena() {
    if (arenaDialog == null || !arenaDialog.isShowing()) return;
    label(arenaStatus, game.arenaActive ? "You " + Math.round(game.arenaHealth) + "/" + Math.round(game.arenaMaxHealth())
        + " HP · Champion " + Math.round(game.arenaEnemyHealth) + "/" + Math.round(game.arenaMaxEnemyHealth())
        + " HP\n" + (45 - (int)game.arenaElapsed) + "s remain · Farm paused" : game.notice);
    label(arenaStrike, game.strikeClock > 0 ? "Strike · " + (int)Math.ceil(game.strikeClock) + "s" : "Strike");
    arenaStrike.setEnabled(game.arenaActive && !paused && game.strikeClock == 0);
    for (int i = 0; i < 3; i++) {
      int type = game.equipped[i]; label(arenaItems[i], Game.ITEMS[type] + " · " + game.itemStock(type) + " owned");
      arenaItems[i].setEnabled(game.arenaActive && !paused && game.itemStock(type) > 0);
    }
    label(arenaPause, !game.arenaActive ? "Return to haven" : paused ? "Fight" : "Pause");
    arenaDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(game.arenaActive);
    arenaView.invalidate();
  }

  private void clearHint() { costButton = null; costToken++; }

  private void showPrice(GameButton button, int coins, int timber) {
    showHint(button, coins + "c" + (timber > 0 ? "\n" + timber + "w" : ""));
  }
  private void showHint(GameButton button, String value) {
    costButton = button; costText = value; long token = ++costToken;
    handler.postDelayed(() -> { if (costToken == token) { costButton = null; refresh(); } }, 1800);
  }

  private void updateAudio(float dt) {
    boolean focused = hasWindowFocus() || arenaDialog != null && arenaDialog.isShowing()
        && arenaDialog.getWindow() != null && arenaDialog.getWindow().getDecorView().hasWindowFocus();
    if (music != null) music.update(foreground && focused && !paused && !game.defeated
        && !storageBlocked && !ioBusy, game, musicEnabled, effectsEnabled, dt);
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
    buildTitle = text("FORTRESS", 20, INK);
    buildTitle.setTag("panelTitle"); buildTitle.setTypeface(null, Typeface.BOLD);
    bar.addView(buildTitle, new LinearLayout.LayoutParams(0, -2, 1));
    GameButton close = button("✕", this::closeBuild);
    close.setTag("closeBuild");
    close.setContentDescription("Close upgrades and return to the paused game");
    bar.addView(close, new LinearLayout.LayoutParams(dp(48), -2));
    card.addView(bar);
    buildResources = text("", 12, MUTED);
    buildResources.setPadding(dp(4), 0, 0, dp(6));
    card.addView(buildResources);
    LinearLayout navigation = row();
    String[] labels = {"Fence", "Mill", "Shop", "Barracks"};
    for (int i = 0; i < 4; i++) {
      final int page = i;
      buildTabs[i] = button(labels[i], () -> selectBuildPage(page));
      buildTabs[i].setTag("buildTab" + i);
      buildTabs[i].setTextSize(compactTextSize(11, 16));
      buildTabs[i].setMaxLines(1); buildTabs[i].setPadding(dp(2), dp(4), dp(2), dp(4));
      LinearLayout.LayoutParams tab = new LinearLayout.LayoutParams(0, -1, 1);
      tab.setMargins(dp(2), 0, dp(2), 0); navigation.addView(buildTabs[i], tab);
    }
    card.addView(navigation);
    LinearLayout loadout = row();
    buildTabs[4] = button("Equipment", () -> selectBuildPage(4));
    buildTabs[4].setTag("buildTab4");
    loadout.addView(buildTabs[4], new LinearLayout.LayoutParams(0, -2, 1));
    GameButton assignments = button("Assign buttons", this::assignButtons);
    assignments.setTag("assignButtons");
    LinearLayout.LayoutParams assignSize = new LinearLayout.LayoutParams(0, -2, 1);
    assignSize.setMargins(dp(4), 0, 0, 0); loadout.addView(assignments, assignSize);
    card.addView(loadout);
    buildScroll = new ScrollView(this); buildScroll.setTag("panelScroll");
    buildScroll.setSmoothScrollingEnabled(false);
    buildItems = column(); buildItems.setTag("panelItems");
    buildScroll.addView(buildItems);
    card.addView(buildScroll, new LinearLayout.LayoutParams(-1, 0, 1));
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
    final int revision = ++panelRevision;
    buildItems.removeAllViews();
    selectedBuilding = buildTab > 0 && buildTab < 4 ? buildTab - 1 : -1;
    java.util.Arrays.fill(upgradeButtons, null);
    java.util.Arrays.fill(wallPurchase, null);
    java.util.Arrays.fill(construction, null);
    hireButton = null; repairOverlay = null; materialsButton = null; patrolButton = null; roster = null;
    java.util.Arrays.fill(rallyButtons, null);
    shopHarvest = null; shopPlant = null; heroBuy = null; costButton = null;
    java.util.Arrays.fill(itemBuy, null); java.util.Arrays.fill(advanced, null);
    label(buildTitle, buildTab == 4 ? "EQUIPMENT" : selectedBuilding < 0 ? "FENCE" : new String[] {"MILL", "SHOP", "BARRACKS"}[selectedBuilding]);
    if (selectedBuilding >= 0) {
      addConstruction(selectedBuilding);
      if (selectedBuilding == 0) { addTimber(); addAdvanced(0); addUpgrade(1); }
      else if (selectedBuilding == 1) {
        shopHarvest = button("", () -> { if (canAct()) { game.harvestAll(); changed(); } });
        shopHarvest.setTag("shopHarvest"); addBuildAction(shopHarvest);
        shopPlant = button("", () -> { if (canAct()) { game.plantAll(selectedCrop); changed(); } });
        shopPlant.setTag("shopPlant"); addBuildAction(shopPlant);
        addUpgrade(2); addUpgrade(3);
        for (int type = 0; type < Game.ITEMS.length; type++) {
          final int item = type;
          itemBuy[type] = button("", () -> { if (canAct()) { if (!game.buyItem(item)) showPrice(itemBuy[item], game.itemPrice(item), 0); else clearHint(); changed(); } });
          itemBuy[type].setTag("buyItem" + type);
          itemBuy[type].setCompoundDrawablesRelativeWithIntrinsicBounds(new ItemIcon(type, dp(28)), null, null, null);
          itemBuy[type].setCompoundDrawablePadding(dp(6)); addBuildAction(itemBuy[type]);
        }
      } else {
        addSquad(); addUpgrade(0); addAdvanced(1);
        heroBuy = button("", () -> { if (canAct()) { if (!game.buyHero()) showPrice(heroBuy, 360, 20); else clearHint(); changed(); } });
        heroBuy.setTag("buyHero"); addBuildAction(heroBuy); addAdvanced(2);
      }
    } else if (buildTab == 0) {
      addTimber();
      repairOverlay = button("", () -> { if (canAct()) { game.repair(); changed(); } });
      repairOverlay.setTag("repair"); addBuildAction(repairOverlay);
      for (int i = 0; i < 4; i++) {
        final int side = i;
        wallPurchase[i] = button("", () -> { if (canAct()) { if (game.wallDamaged(side)) { if (!game.repairWall(side)) showPrice(wallPurchase[side], 8, 0); }
          else if (!game.upgradeBorder(side)) showPrice(wallPurchase[side], game.borderCoinCost(side), game.borderWoodCost(side)); changed(); } });
        wallPurchase[i].setTag("constructWall" + i); addBuildAction(wallPurchase[i]);
      }
    }
    if (buildTab == 4) addEquipment();
    // Cancel stale scroll/focus from the detached page, then settle the new page at its start.
    buildScroll.scrollTo(0, 0);
    buildScroll.post(() -> {
      if (revision == panelRevision) { buildScroll.scrollTo(0, 0); buildScroll.clearFocus(); }
    });
  }

  private void selectBuildPage(int page) {
    if (buildTab == page) return;
    buildTab = page;
    clearHint();
    rebuildBuildItems();
    refresh();
  }

  private void addConstruction(int kind) {
    construction[kind] = button("", () -> { if (canAct()) { if (game.buildings[kind]) game.repairBuilding(kind); else game.construct(kind); changed(); } });
    construction[kind].setTag("construct" + kind); addBuildAction(construction[kind]);
  }

  private void addTimber() {
    materialsButton = button("Buy 10 timber · 12 coins", () -> { if (canAct()) { game.buyTimber(); changed(); } });
    materialsButton.setTag("buyTimber"); addBuildAction(materialsButton);
  }

  private void addUpgrade(int kind) {
    upgradeButtons[kind] = button("", () -> { if (canAct()) { if (!game.upgrade(kind)) showPrice(upgradeButtons[kind], game.cost(kind), 0); else clearHint(); changed(); } });
    upgradeButtons[kind].setTag("upgrade" + kind); addBuildAction(upgradeButtons[kind]);
  }

  private void addAdvanced(int kind) {
    advanced[kind] = button("", () -> { if (canAct()) {
      if (!game.upgradeAdvanced(kind)) showPrice(advanced[kind], game.advancedCost(kind), game.advancedWood(kind)); else clearHint(); changed();
    } });
    advanced[kind].setTag("advanced" + kind); addBuildAction(advanced[kind]);
  }

  private void addSquad() {
    hireButton = button("", () -> { if (canAct()) { if (!game.hire()) showPrice(hireButton, game.hireCost(), 6); changed(); } });
    hireButton.setTag("hire"); addBuildAction(hireButton);
    roster = text("", 13, INK); roster.setPadding(dp(6), dp(8), dp(6), dp(8)); buildItems.addView(roster);
    LinearLayout rally = row();
    for (int i = 0; i < 4; i++) {
      final int side = i;
      GameButton action = button(Game.SIDES[i].substring(0, 1), () -> { if (canAct()) { game.rally(side); changed(); } });
      action.setContentDescription("Move mercenaries to defend the " + Game.SIDES[i] + " fence");
      rallyButtons[i] = action; action.setTag("rally" + i);
      rally.addView(action, new LinearLayout.LayoutParams(0, -2, 1));
    }
    buildItems.addView(text("Rally", 12, MUTED)); buildItems.addView(rally);
    patrolButton = button("Resume perimeter patrol", () -> { if (canAct()) { game.resumePatrol(); changed(); } });
    patrolButton.setTag("patrol"); addBuildAction(patrolButton);
  }

  private void showBuilding(int kind) {
    buildTab = kind + 1; showBuild();
  }

  private void showBuild(int page) { buildTab = page; showBuild(); }

  private void showBuild() {
    if (ioBusy) return;
    paused = true;
    seedTray.setVisibility(View.GONE);
    buildOpen = true;
    rebuildBuildItems();
    save();
    gameShell.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
    buildOverlay.setVisibility(View.VISIBLE);
    buildOverlay.findViewWithTag("closeBuild").requestFocus();
    refresh();
  }

  private void closeBuild() {
    buildOpen = false;
    buildOverlay.setVisibility(View.GONE);
    gameShell.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
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
    if (costButton == null) gameShell.announceForAccessibility(game.notice);
    else costButton.announceForAccessibility(costText);
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
    label(survived, "Survived " + (game.worldComplete ? 100 : game.day - 1) + (game.day == 2 ? " day" : " days") + (game.world > 1 ? " · World " + game.world : ""));
    label(stats, game.coins + " coins · " + game.wood + " timber");
    label(status, "Day " + game.day + " · " + Settlement.NAMES[game.settlementStyle()] + " · " + Game.WEATHER[game.weather] + " · Farm "
        + Math.round(game.health) + "/" + game.maxHealth() + (game.boss() != null ? " · BOSS" : game.bossDay() ? " · Boss tonight" : ""));
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
    label(harvest, "Harvest\n" + game.readyPlots() + " ready");
    harvest.setEnabled(available && game.readyPlots() > 0);
    harvest.colors(game.readyPlots() > 0 ? GOLD : 0xffe9e5d3, 0xffd2cba9, INK);
    label(phase, game.defeated ? "Rebuild\nFree" : game.night ? "Raid " + game.day + "\n" + game.enemies.size() + " foes" : "Raid in\n" + game.raidInSeconds() + "s");
    phase.setContentDescription(game.defeated ? "Rebuild fortress free" : game.night ? "Raid in progress" : "Start the next zombie raid now, automatic raid in " + game.raidInSeconds() + " seconds");
    phase.setEnabled(!storageBlocked && !ioBusy && !game.arenaActive && game.arenaOfferDay == 0 && (!game.night || game.defeated));
    if (game.worldComplete) label(phase, "Rift\nOpen");
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
            + "\n" + new String[] {"Produces " + game.millYield() + " timber /" + (int)game.millPeriod() + " active seconds", "+25% crop sale value", "Unlocks mercenary recruitment"}[i]);
        construction[i].setEnabled(available && (damaged ? game.coins >= 10 && game.wood >= 5
            : !game.buildings[i] && game.coins >= Game.BUILD_COINS[i] && game.wood >= Game.BUILD_WOOD[i]));
      }
    }
    for (int i = 0; i < 4; i++) {
      int tier = game.walls[i];
      boolean damaged = game.wallDamaged(i), breached = tier > 0 && game.wallHealth[i] <= 0;
      label(wallButtons[i], Game.SIDES[i].substring(0, 1));
      wallButtons[i].setEnabled(available);
      wallButtons[i].setSelected(selectedWall == i);
      wallButtons[i].setContentDescription("Select " + Game.SIDES[i] + " fence for center upgrade, " + (tier == 0 ? "gap, build in Build then Fence"
          : Game.FENCE_MATERIALS[tier] + ", " + Math.round(game.wallHealth[i]) + " of " + Math.round(game.maxWallHealth(i))
          + " health, " + (damaged ? "tap to repair up to50 health for8 coins" : "intact, no repair needed")));
      wallButtons[i].colors(Color.TRANSPARENT, selectedWall == i ? GOLD : Color.TRANSPARENT,
          breached ? 0xfff1a088 : damaged ? GOLD : CREAM);
      if (wallPurchase[i] != null) {
        label(wallPurchase[i], Game.SIDES[i] + " · " + Game.FENCE_MATERIALS[tier] + " · " + Math.round(game.wallHealth[i]) + "/" + Math.round(game.maxWallHealth(i)) + "HP\n" + (damaged ? "REPAIR · " + Math.round(game.wallHealth[i]) + "/"
            + Math.round(game.maxWallHealth(i)) + "HP\n8 coins · +50 health"
            : tier == 0 ? "BUILD WOOD FENCE\n" + game.wallCoinCost(i) + " coins · " + game.wallWoodCost(i) + " timber"
            : tier >= 3 ? "Steel · reinforcement " + game.wallRanks[i] + "/" + game.wallRankCap() + "\n" + (game.borderMaxed(i) ? "MAX" : game.borderCoinCost(i) + " coins · " + game.borderWoodCost(i) + " timber")
            : "UPGRADE " + Game.FENCE_MATERIALS[tier] + " → " + Game.FENCE_MATERIALS[tier + 1] + "\n" + game.wallCoinCost(i) + " coins · " + game.wallWoodCost(i) + " timber")
            + "\n" + (damaged ? "Closes the breach for new zombies; fight intruders inside" : "Material kits strengthen fences; coins buy supplies and timber frames them"));
        wallPurchase[i].setEnabled(available && (damaged || !game.borderMaxed(i)));
        wallPurchase[i].colors(selectedWall == i ? GOLD : 0xffe9e5d3, 0xffb6a274, INK);
      }
      buildTabs[i].setSelected(buildTab == i);
      buildTabs[i].colors(buildTab == i ? GOLD : 0xffe9e5d3, 0xffb6a274, INK);
    }
    buildTabs[4].setSelected(buildTab == 4);
    buildTabs[4].colors(buildTab == 4 ? GOLD : 0xffe9e5d3, 0xffb6a274, INK);
    for (int i = 0; i < 9; i++) {
      boolean unlocked = i < game.plots(); int crop = game.crop[i]; boolean ripe = crop >= 0 && game.growth[i] >= 1;
      label(plotButtons[i], !unlocked ? "Land +" : crop < 0 ? game.scorched[i] ? "Replant" : "+" : ripe ? "+" + game.sale(crop) + "c" : game.harvestSeconds(i) + "s");
      plotButtons[i].setEnabled(available);
      plotButtons[i].setContentDescription("Farm plot " + (i + 1) + ", " + (!unlocked ? "locked, tap for expansion" : crop < 0
          ? (game.scorched[i] ? "crop destroyed, replant " : "empty, plant ") + Game.CROPS[selectedCrop] + " for " + Game.SEED[selectedCrop] + " coins"
          : Game.CROPS[crop] + ", health " + Math.round(game.cropHealth[i]) + " of40" + (ripe ? ", tap to harvest " + game.sale(crop) + " coins and " + (2 + crop * 2) + " timber" : ", growing, " + game.harvestSeconds(i) + " seconds")));
      plotButtons[i].colors(0x00583c28, ripe ? GOLD : unlocked ? 0xffb9965c : 0xff8cb061, ripe ? GOLD : CREAM);
    }
    label(quickUniform, "Merc ↑");
    quickUniform.setContentDescription("Upgrade mercenary uniform, " + Game.UNIFORMS[Math.min(5, game.weapon)]
        + (game.maxed(0) ? ", maximum" : ", " + game.cost(0) + " coins"));
    quickUniform.setEnabled(available && !game.maxed(0));
    quickUniform.colors(Color.TRANSPARENT, Color.TRANSPARENT, CREAM);
    boolean canUpgradeFence = selectedWall >= 0 && !game.borderMaxed(selectedWall);
    fenceUpgrade.setEnabled(available && canUpgradeFence);
    label(fenceUpgrade, "↑"); fenceUpgrade.setTextSize(16);
    fenceUpgrade.colors(Color.TRANSPARENT, Color.TRANSPARENT, GOLD);
    fenceUpgrade.setContentDescription(selectedWall < 0 ? "Select a fence direction first"
        : Game.SIDES[selectedWall] + (canUpgradeFence ? " fence upgrade, " + game.borderCoinCost(selectedWall)
            + " coins and " + game.borderWoodCost(selectedWall) + " timber" : " fence, maximum reinforcement"));
    for (int slot = 0; slot < itemSlots.length; slot++) {
      int item = game.equipped[slot], stock = game.itemStock(item);
      label(itemSlots[slot], "×" + stock + (item == 1 && game.enhancerTime > 0 ? "\n" + (int)Math.ceil(game.enhancerTime) + "s" : ""));
      itemSlots[slot].setEnabled(available);
      itemSlots[slot].setContentDescription(Game.ITEMS[item] + ", " + stock + " owned. Tap to use; hold or tap an empty slot to replace.");
      int[] colors = {0xffb97570, 0xffaa87c7, 0xff77c4df, 0xffdfb475};
      itemSlots[slot].colors(Color.TRANSPARENT, colors[item], CREAM);
      if (slotIcons[slot] != item) {
        itemSlots[slot].setCompoundDrawablesRelativeWithIntrinsicBounds(new ItemIcon(item, dp(30)), null, null, null);
        slotIcons[slot] = item;
      }
    }
    for (int item = 0; item < Game.ITEMS.length; item++) if (itemBuy[item] != null) {
      String[] detail = {"heals surviving crew/farm", "20s combat boost", "single target · nearest threat", "area blast · nearby group"};
      label(itemBuy[item], Game.ITEMS[item] + " · " + detail[item] + "\n" + game.itemPrice(item) + " coins · " + game.itemStock(item) + "/9 owned");
      itemBuy[item].setEnabled(available && game.buildings[1] && game.itemStock(item) < Game.MAX_ITEMS);
    }
    for (int kind = 0; kind < 3; kind++) if (advanced[kind] != null) {
      String title = kind == 0 ? "Mill output " + game.millLevel + "/" + game.advancedCap(0) + " · " + game.millYield() + " timber /" + (int)game.millPeriod() + "s"
          : kind == 1 ? "Mercenary drills " + game.drill + "/" + game.advancedCap(1) + " · aim, armor and health" : "Hero training " + game.heroLevel + "/" + game.advancedCap(2);
      label(advanced[kind], title + (game.advancedMaxed(kind) ? " · MAX" : "\n" + game.advancedCost(kind) + " coins · " + game.advancedWood(kind) + " timber"));
      advanced[kind].setEnabled(available && !game.advancedMaxed(kind) && (kind == 0 ? game.buildings[0] : game.buildings[2]) && (kind != 2 || game.hero != null));
    }
    if (heroBuy != null) {
      label(heroBuy, game.hero == null ? "Recruit hero · 360 coins · 20 timber\nSeparate from your3 mercenaries" : "Hero deployed · " + Math.round(game.hero.health) + "/" + (int)game.heroMaxHealth() + "HP");
      heroBuy.setEnabled(available && game.buildings[2] && game.hero == null);
    }
    if (shopHarvest != null) {
      label(shopHarvest, "Sell harvest · " + game.readyPlots() + " ready");
      shopHarvest.setEnabled(available && game.readyPlots() > 0);
      int empty = game.plantablePlots(selectedCrop);
      label(shopPlant, "Plant " + Game.CROPS[selectedCrop] + " · " + empty + " plots · " + empty * Game.SEED[selectedCrop] + " coins");
      shopPlant.setEnabled(available && empty > 0);
    }
    if (materialsButton != null) materialsButton.setEnabled(available && game.coins >= 12 && game.wood <= 9_999_990);
    if (repairOverlay != null) {
      label(repairOverlay, "Repair homestead · 8 coins\n+40 health · " + Math.round(game.health) + "/" + game.maxHealth());
      repairOverlay.setEnabled(available && game.health < game.maxHealth() && game.coins >= 8);
    }
    if (hireButton != null) {
      label(hireButton, !game.buildings[2] ? "Build Barracks first" : game.mercenaries.size() >= Game.MAX_MERCS ? "Squad full · 3 survivors" : "Hire mercenary · " + game.hireCost() + " coins · 6 timber");
      hireButton.setEnabled(available && game.buildings[2] && game.mercenaries.size() < Game.MAX_MERCS);
      label(roster, "Mercenaries " + game.mercenaries.size() + "/3 · " + Game.UNIFORMS[Math.min(5, game.weapon)]
          + "\n" + game.guardDamage() + " damage per shot · " + (game.lane < 0 ? "Patrolling" : "Rally: " + Game.SIDES[game.lane])
          + "\nShort range: rally to threatened borders. Breached zombies can kill guards. Crew heals during preparation.");
      for (int side = 0; side < 4; side++) if (rallyButtons[side] != null)
        rallyButtons[side].colors(game.lane == side ? GOLD : 0xffe9e5d3, 0xffb6a274, INK);
      if (patrolButton != null) patrolButton.setEnabled(available && game.lane >= 0);
    }
    String[] upgrades = {"Uniform " + game.weapon + "/" + game.upgradeCap(0) + " · " + Game.UNIFORMS[Math.min(5, game.weapon)] + "\n+3 damage · +20 crew health · armor · faster fire", "Fortify · rank " + game.fence + "/" + game.upgradeCap(1) + "\n+50 farm health · +15 health per built fence", "Irrigation · tier " + game.irrigation + "\n+20% growth", "Farm expansion · 6 → 9 plots"};
    for (int i = 0; i < 4; i++) if (upgradeButtons[i] != null) {
      label(upgradeButtons[i], upgrades[i] + (game.maxed(i) ? " · MAX" : "\n" + game.cost(i) + " coins"));
      upgradeButtons[i].setEnabled(available && !game.maxed(i));
    }
    label(buildResources, game.coins + " coins · " + game.wood + " timber · game paused");
    if (costButton != null) {
      label(costButton, costText);
      if (costButton == fenceUpgrade) costButton.setTextSize(getResources().getConfiguration().fontScale >= 1.5f ? 7 : 10);
    }
    terrain.phase(game.night);
    board.refreshSettlement();
    updateAudio(0);
    scene.invalidate();
    refreshArena();
    scheduleAdventure();
  }

  private void welcome() {
    paused = true; adventureDialog = true;
    new AlertDialog.Builder(this)
        .setTitle("TAG: Zombie Survival Fantasy")
        .setMessage("One life ended. Your survival story begins.\n\nYou awaken in a world claimed by the dead. Build a haven. Gather allies. Unearth forgotten magic.\n\nSurvive one hundred days—and the rift will carry you to another world.\n\nA fence. A seed. A second chance.")
        .setPositiveButton("Awaken", (d, w) -> {
          game.tutorialDone = true; paused = game.arenaActive || game.arenaOfferDay > 0 || game.worldComplete;
          lastFrame = SystemClock.elapsedRealtime(); changed();
        })
        .setNegativeButton("Later", null)
        .setOnDismissListener(d -> { adventureDialog = false; refresh(); }).show();
  }

  private void howToPlay() {
    new AlertDialog.Builder(this).setTitle("Build. Fight. Survive.")
        .setMessage("Plant and harvest for coins and timber. Close fence gaps; upgrade defenses. Hire and rally crew in Barracks.\n\nHold any quick slot, or open Assign buttons, to choose a potion or magic. Buy supplies in Shop.\n\nRare relics go to Equipment. Equip Best fits your strongest collection. Bosses come every fifth night; optional champion arenas follow every tenth victory.\n\nThe local director varies the spacing of future reinforcements slightly. Enemy damage, health, quotas and loot odds stay fixed. Disable it in Options for fixed pacing. Export a backup before updating.")
        .setPositiveButton("Ready", null).show();
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
      musicEnabled ? "Background music: ON" : "Background music: OFF",
      effectsEnabled ? "Sound effects: ON" : "Sound effects: OFF",
      game.directorEnabled ? "Adaptive director: ON" : "Adaptive director: OFF",
      "Equipment & button assignments"
    };
    new AlertDialog.Builder(this)
        .setTitle("Take a breather")
        .setItems(
            items,
            (dialog, which) -> {
              if (which == 0) howToPlay();
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
                          selectedCrop = 0; selectedWall = -1; selectedBuilding = -1; clearHint();
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
              } else if (which == 8) {
                effectsEnabled = !effectsEnabled;
                getPreferences(MODE_PRIVATE).edit().putBoolean("effects", effectsEnabled).apply();
                refresh(); options();
              } else if (which == 9) {
                game.directorEnabled = !game.directorEnabled;
                game.directorPressure = 0; game.calmNights = 0; changed(); options();
              } else if (which == 10) showBuild(4);
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
            .putExtra(Intent.EXTRA_TITLE, "TAG-Zombie-Survival-Fantasy-Day-" + game.day + ".btfs");
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
                    selectedCrop = 0; selectedWall = -1; selectedBuilding = -1; clearHint();
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
    l.setBaselineAligned(false);
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
