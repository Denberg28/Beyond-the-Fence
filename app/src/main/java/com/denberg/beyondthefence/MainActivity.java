package com.denberg.beyondthefence;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
  private int tab, selectedCrop;
  private long lastFrame, lastUi, lastSave;
  private TextView heading, stats, status, notice, saving, forecast;
  private Button play, phase, harvest;
  private final Button[] tabs = new Button[3],
      cropButtons = new Button[3],
      plotButtons = new Button[9],
      upgradeButtons = new Button[4],
      laneButtons = new Button[3];
  private LinearLayout panel;
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
    buildUi();
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
  protected void onResume() {
    super.onResume();
    session.observer = this;
    saveMessage = session.saveMessage;
    foreground = true;
    paused = true;
    lastFrame = SystemClock.elapsedRealtime();
    handler.post(frame);
    refresh();
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
    options();
  }

  private void buildUi() {
    LinearLayout root = column();
    root.setPadding(dp(16), dp(8), dp(16), 0);
    root.setBackgroundColor(CREAM);
    root.setFitsSystemWindows(true);
    setContentView(root);
    root.setOnApplyWindowInsetsListener(
        (v, insets) -> {
          root.setPadding(
              dp(16) + insets.getSystemWindowInsetLeft(),
              dp(8) + insets.getSystemWindowInsetTop(),
              dp(16) + insets.getSystemWindowInsetRight(),
              insets.getSystemWindowInsetBottom());
          return insets;
        });
    LinearLayout title = row();
    LinearLayout branding = column();
    TextView eyebrow = text("THE HOMESTEAD", 10, MUTED);
    branding.addView(eyebrow);
    heading = text("Beyond the Fence", 23, INK);
    heading.setTypeface(null, Typeface.BOLD);
    branding.addView(heading);
    title.addView(branding, new LinearLayout.LayoutParams(0, -2, 1));
    play =
        button(
            "Play",
            () -> {
              if (storageBlocked || ioBusy) return;
              if (game.defeated) {
                game.recover();
                changed();
              }
              paused = !paused;
              lastFrame = SystemClock.elapsedRealtime();
              refresh();
            });
    play.setTag("play");
    title.addView(play, new LinearLayout.LayoutParams(dp(68), dp(48)));
    Button menu = button("•••", this::options);
    menu.setContentDescription("Pause and open options");
    title.addView(menu, new LinearLayout.LayoutParams(dp(48), dp(48)));
    root.addView(title);
    stats = text("", 14, INK);
    stats.setPadding(0, dp(8), 0, dp(4));
    root.addView(stats);
    status = text("", 13, INK);
    status.setTypeface(null, Typeface.BOLD);
    root.addView(status);
    forecast = text("", 11, MUTED);
    root.addView(forecast);
    scene = new FarmView(this, () -> game, () -> paused);
    scene.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
    LinearLayout.LayoutParams sceneParams = new LinearLayout.LayoutParams(-1, 0, 0.9f);
    sceneParams.topMargin = dp(10);
    sceneParams.bottomMargin = dp(8);
    root.addView(scene, sceneParams);
    notice = text("", 13, INK);
    notice.setMinHeight(dp(40));
    notice.setMaxLines(3);
    root.addView(notice);
    LinearLayout nav = row();
    String[] names = {"Farm", "Build", "Defend"};
    for (int i = 0; i < 3; i++) {
      final int index = i;
      tabs[i] =
          button(
              names[i],
              () -> {
                tab = index;
                buildPanel();
                refresh();
              });
      tabs[i].setTag("tab" + i);
      nav.addView(tabs[i], new LinearLayout.LayoutParams(0, dp(48), 1));
    }
    root.addView(nav);
    ScrollView scroll = new ScrollView(this);
    scroll.setFillViewport(false);
    panel = column();
    panel.setPadding(0, dp(6), 0, dp(6));
    scroll.addView(panel);
    root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1.15f));
    saving = text(saveMessage + " · v0.1.0 rc1", 10, MUTED);
    saving.setPadding(0, dp(4), 0, dp(6));
    root.addView(saving);
    buildPanel();
  }

  private void buildPanel() {
    panel.removeAllViews();
    if (tab == 0) {
      panel.addView(text("Choose seeds • tap a plot to plant or harvest", 12, MUTED));
      LinearLayout choices = row();
      for (int i = 0; i < 3; i++) {
        final int c = i;
        cropButtons[i] =
            button(
                "",
                () -> {
                  selectedCrop = c;
                  refresh();
                });
        choices.addView(cropButtons[i], new LinearLayout.LayoutParams(0, dp(56), 1));
      }
      panel.addView(choices);
      for (int r = 0; r < 3; r++) {
        LinearLayout plots = row();
        for (int c = 0; c < 3; c++) {
          final int p = r * 3 + c;
          plotButtons[p] =
              button(
                  "",
                  () -> {
                    if (!canAct()) return;
                    if (game.crop[p] < 0) game.plant(p, selectedCrop);
                    else if (!game.harvest(p))
                      game.notice = "Still growing. Rain and irrigation speed up your crops.";
                    changed();
                  });
          plotButtons[p].setTag("plot" + p);
          plots.addView(plotButtons[p], new LinearLayout.LayoutParams(0, dp(52), 1));
        }
        panel.addView(plots);
      }
      harvest =
          button(
              "Harvest all",
              () -> {
                if (canAct()) {
                  game.harvestAll();
                  changed();
                }
              });
      harvest.setTag("harvest");
      panel.addView(harvest);
    } else if (tab == 1) {
      panel.addView(text("Invest in tomorrow. All upgrades are permanent.", 12, MUTED));
      for (int i = 0; i < 4; i++) {
        final int kind = i;
        upgradeButtons[i] =
            button(
                "",
                () -> {
                  if (canAct()) {
                    game.upgrade(kind);
                    changed();
                  }
                });
        upgradeButtons[i].setTag("upgrade" + i);
        panel.addView(upgradeButtons[i], new LinearLayout.LayoutParams(-1, dp(56)));
      }
    } else {
      panel.addView(
          text(
              "Auto-fire protects every lane. Tap a lane for a focus shot (3s recharge).",
              13,
              MUTED));
      LinearLayout lanes = row();
      for (int i = 0; i < 3; i++) {
        final int l = i;
        laneButtons[i] =
            button(
                "",
                () -> {
                  if (canAct()) {
                    game.focus(l);
                    changed();
                  }
                });
        laneButtons[i].setTag("lane" + i);
        lanes.addView(laneButtons[i], new LinearLayout.LayoutParams(0, dp(56), 1));
      }
      panel.addView(lanes);
      Button repair =
          button(
              "Repair fence · 8 coins / +40 strength",
              () -> {
                if (canAct()) {
                  game.repair();
                  changed();
                }
              });
      panel.addView(repair);
      phase =
          button(
              "Start night",
              () -> {
                if (storageBlocked || ioBusy) return;
                if (game.defeated) game.recover();
                else game.startNight();
                changed();
              });
      phase.setTag("night");
      panel.addView(phase);
      panel.addView(
          text(
              "Walkers → runners (night 2) → brutes (3) → spitters (4). Rain slows enemies. Keep"
                  + " your fence healthy.",
              12,
              MUTED));
    }
    refresh();
  }

  private boolean canAct() {
    if (storageBlocked || ioBusy) return false;
    if (game.defeated) {
      game.notice = "Rebuild your fence first. Tap Rebuild or open Defend.";
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

  @android.annotation.SuppressLint("SetTextI18n") // This candidate deliberately supports English only; see SPEC.md.
  void refresh() {
    if (stats == null) return;
    stats.setText(
        game.coins
            + " coins     Lv "
            + game.level()
            + "     Fence "
            + Math.round(game.health)
            + "/"
            + game.maxHealth());
    String state =
        game.defeated
            ? "FENCE DOWN"
            : paused ? "PAUSED" : game.night ? "NIGHT WATCH" : "GROWING SEASON";
    status.setText("Day " + game.day + "  ·  " + Game.WEATHER[game.weather] + "  ·  " + state);
    forecast.setText(
        game.night
            ? "Night threats: "
                + game.enemies.size()
                + " active · "
                + game.spawned
                + "/"
                + game.quota()
                + " arrived"
            : "Night in "
                + Math.max(0, (int) Math.ceil(Game.DAY_SECONDS - game.elapsed))
                + "s · "
                + new String[] {
                      "Steady growing weather",
                      "Crops +35% growth · enemies slower",
                      "Low visibility · watch all lanes",
                      "Crops grow 20% slower"
                    }
                    [game.weather]);
    play.setText(game.defeated ? "Rebuild" : paused ? "Play" : "Pause");
    notice.setText(game.notice);
    saving.setText(saveMessage);
    for (int i = 0; i < 3; i++) {
      tabs[i].setBackground(bg(tab == i ? INK : 0xffe5e8de));
      tabs[i].setTextColor(tab == i ? Color.WHITE : INK);
    }
    if (tab == 0) {
      for (int i = 0; i < 3; i++) {
        boolean unlocked = i < game.unlockedCrops();
        cropButtons[i].setText(
            unlocked
                ? Game.CROPS[i] + " · " + Game.SEED[i] + "c\nSell " + Game.SALE[i] + "c"
                : Game.CROPS[i] + "\nLevel " + (i + 1));
        cropButtons[i].setEnabled(unlocked);
        cropButtons[i].setBackground(bg(selectedCrop == i ? GOLD : 0xffe5e8de));
      }
      for (int i = 0; i < 9; i++) {
        boolean unlocked = i < game.plots();
        int c = game.crop[i];
        String label =
            !unlocked
                ? "Expand land"
                : c < 0
                    ? "+ Plant"
                    : Game.CROPS[c]
                        + (game.growth[i] >= 1
                            ? " · Ready"
                            : " · " + (int) (game.growth[i] * 100) + "%");
        plotButtons[i].setText(label);
        plotButtons[i].setEnabled(unlocked && !storageBlocked && !ioBusy);
        plotButtons[i].setContentDescription("Plot " + (i + 1) + ", " + label);
        plotButtons[i].setBackground(bg(c >= 0 && game.growth[i] >= 1 ? GOLD : 0xffe5e8de));
      }
    } else if (tab == 1) {
      String[] title = {
        Game.WEAPONS[game.weapon] + " → upgrade",
        "Fence · tier " + (game.fence + 1),
        "Irrigation · tier " + game.irrigation,
        "Expand land · 6 → 9 plots"
      };
      for (int i = 0; i < 4; i++) {
        upgradeButtons[i].setText(
            title[i] + (game.maxed(i) ? "  ·  MAX" : "  ·  " + game.cost(i) + " coins"));
        upgradeButtons[i].setEnabled(!game.maxed(i) && !storageBlocked && !ioBusy);
      }
    } else {
      for (int i = 0; i < 3; i++) {
        laneButtons[i].setText(
            new String[] {"Left", "Middle", "Right"}[i] + (game.lane == i ? " ✓" : ""));
        laneButtons[i].setBackground(bg(game.lane == i ? GOLD : 0xffe5e8de));
      }
      phase.setText(
          game.defeated
              ? "Rebuild for free"
              : game.night
                  ? "Focus recharge: " + (int) Math.ceil(game.focusClock) + "s"
                  : "Ready early? Start night");
      phase.setEnabled(!game.night || game.defeated);
    }
  }

  private void welcome() {
    paused = true;
    new AlertDialog.Builder(this)
        .setTitle("Your farm. Your last safe place.")
        .setMessage(
            "1  Choose seeds, then tap empty plots. Tap ripe crops to sell them.\n\n"
                + "2  Spend coins on your fence, rifle, irrigation and land.\n\n"
                + "3  Nights bring zombies. Your rifle fires automatically; choose a lane for a"
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
                          tab = 0;
                          buildPanel();
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
                    buildPanel();
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

  private Button button(String label, Runnable click) {
    Button b = new Button(this);
    b.setText(label);
    b.setTextSize(12);
    b.setAllCaps(false);
    b.setTextColor(INK);
    b.setMinHeight(dp(48));
    b.setMinimumWidth(0);
    b.setPadding(dp(6), dp(2), dp(6), dp(2));
    b.setBackground(bg(0xffe5e8de));
    b.setOnClickListener(v -> click.run());
    return b;
  }

  private GradientDrawable bg(int color) {
    GradientDrawable d = new GradientDrawable();
    d.setColor(color);
    d.setCornerRadius(dp(10));
    d.setStroke(dp(2), CREAM);
    return d;
  }

  private int dp(int value) {
    return Math.round(value * getResources().getDisplayMetrics().density);
  }
}
