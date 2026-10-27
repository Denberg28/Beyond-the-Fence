package com.denberg.beyondthefence;

import java.util.ArrayList;
import java.util.Iterator;

/** Deterministic simulation. No Android, disk, network or wall-clock dependencies. */
public final class Game {
  public static final String[] CROPS = {"Turnip", "Corn", "Pumpkin"};
  public static final int[] SEED = {4, 10, 18}, SALE = {12, 26, 44};
  public static final float[] GROW = {16, 29, 43};
  public static final String[] WEATHER = {"Clear", "Rain", "Fog", "Heat"};
  public static final String[] FOES = {"Walker", "Runner", "Brute", "Spitter"};
  public static final String[] WEAPONS = {
    "Ranch rifle", "Tuned rifle", "Repeater", "Marksman", "Longshot", "Sentinel"
  };
  public static final float DAY_SECONDS = 75;
  public static final String[] SIDES = {"North", "East", "South", "West"};
  public static final String[] BUILDINGS = {"Timber mill", "Market", "Barracks"};
  public static final int[] BUILD_COINS = {50, 80, 70}, BUILD_WOOD = {10, 18, 12};
  public static final int MAX_MERCS = 6;
  public static final int MAX_ROAMERS = 10;
  public final ArrayList<Roamer> roamers = new ArrayList<>();
  public static final float CROP_HEALTH = 40, BUILDING_HEALTH = 100;
  public final float[] wallHealth = {80, 0, 80, 0}, cropHealth = new float[9], buildingHealth = new float[3];
  public final boolean[] scorched = new boolean[9], buildingRuins = new boolean[3];
  public int lostCrops, lostBuildings;
  public int wood = 24;
  public final int[] walls = {1, 0, 1, 0};
  public final boolean[] buildings = new boolean[3];
  public final ArrayList<Mercenary> mercenaries = new ArrayList<>();
  public float productionClock;
  public final int[] crop = new int[9];
  public final float[] growth = new float[9];
  public final ArrayList<Enemy> enemies = new ArrayList<>();
  public int day = 1, coins = 80, xp, weather, weapon, fence, irrigation, land;
  public int kills, harvested, spawned, lane = 1;
  public float health = 100, elapsed, shotClock, spawnClock, focusClock;
  public boolean night, defeated, relaxed, reducedMotion, tutorialDone;
  public int randomState = 72419;
  public String notice = "Your farm fortress. Harvest crops, close fence gaps and recruit patrols.";
  public long revision;

  public Game() {
    java.util.Arrays.fill(crop, -1);
    populateRoamers();
  }

  public float preparationSeconds() { return Math.max(30, DAY_SECONDS - 5 * Math.min(9, level() - 1)); }
  public float reinforcementSeconds() { return Math.max(1.2f, 4.8f - .4f * Math.min(9, level() - 1)); }
  public int raidInSeconds() { return (int) Math.ceil(Math.max(0, preparationSeconds() - elapsed)); }

  /** Bounded, deterministic scouts; these are promoted into real enemies when a raid begins. */
  void populateRoamers() {
    if (night || defeated) return;
    int desired = Math.min(MAX_ROAMERS, 3 + level());
    while (roamers.size() < desired) {
      int i = roamers.size();
      roamers.add(new Roamer(i % Math.min(4, day), (i * 2.472f + .23f) % 4, .65f + i % 3 * .1f));
    }
  }

  public int plots() {
    return land == 0 ? 6 : 9;
  }

  public int level() {
    return 1 + xp / 50;
  }

  public int unlockedCrops() {
    return Math.min(3, level());
  }

  public int maxHealth() {
    return 100 + fence * 50;
  }

  public int quota() {
    return Math.min(45, 5 + day * 2);
  }

  public int cost(int kind) {
    switch (kind) {
      case 0:
        return 80 + weapon * 100;
      case 1:
        return 70 + fence * 90;
      case 2:
        return 100 + irrigation * 100;
      default:
        return 200;
    }
  }

  public boolean maxed(int kind) {
    return kind == 0
        ? weapon >= 5
        : kind == 1 ? fence >= 4 : kind == 2 ? irrigation >= 3 : land >= 1;
  }

  private void money(int amount) {
    coins = Math.min(10_000_000, Math.max(0, coins + amount));
  }

  private void experience(int amount) {
    xp = Math.min(10_000_000, xp + amount);
  }

  private int random(int bound) {
    randomState = randomState * 1664525 + 1013904223;
    return (randomState >>> 1) % bound;
  }

  public boolean plant(int plot, int type) {
    if (defeated
        || plot < 0
        || plot >= plots()
        || type < 0
        || type >= unlockedCrops()
        || crop[plot] >= 0) return false;
    if (coins < SEED[type]) {
      notice = "Need " + SEED[type] + " coins for seeds. Night rewards can help.";
      return false;
    }
    money(-SEED[type]);
    crop[plot] = type;
    growth[plot] = 0;
    cropHealth[plot] = CROP_HEALTH; scorched[plot] = false;
    notice = CROPS[type] + " planted. Tap when ready to harvest.";
    return true;
  }

  public float growthRate() {
    return (1 + irrigation * .2f) * (weather == 1 ? 1.35f : weather == 3 ? .8f : 1);
  }

  public int harvestSeconds(int plot) {
    if (plot < 0 || plot >= plots() || crop[plot] < 0) return 0;
    return (int) Math.ceil(Math.max(0, 1 - growth[plot]) * GROW[crop[plot]] / growthRate());
  }

  public int readyPlots() {
    int count = 0;
    for (int p = 0; p < plots(); p++) if (crop[p] >= 0 && growth[p] >= 1) count++;
    return count;
  }

  /** Number of empty plots affordable now; never removes or replaces an existing crop. */
  public int plantablePlots(int type) {
    if (defeated || type < 0 || type >= unlockedCrops()) return 0;
    int empty = 0;
    for (int p = 0; p < plots(); p++) if (crop[p] < 0) empty++;
    return Math.min(empty, coins / SEED[type]);
  }

  public int plantAll(int type) {
    int available = plantablePlots(type), planted = 0;
    if (available == 0) {
      notice = "No empty plots or not enough coins for these seeds.";
      return 0;
    }
    for (int p = 0; p < plots() && planted < available; p++)
      if (crop[p] < 0 && plant(p, type)) planted++;
    notice = "Planted " + planted + " plots · " + planted * SEED[type] + " coins spent.";
    return planted;
  }

  public boolean harvest(int plot) {
    if (defeated || plot < 0 || plot >= plots() || crop[plot] < 0 || growth[plot] < 1) return false;
    int type = crop[plot];
    money(sale(type));
    timber(2 + type * 2);
    experience(4);
    harvested++;
    crop[plot] = -1;
    growth[plot] = 0; cropHealth[plot] = 0;
    notice = "+" + sale(type) + " coins · +" + (2 + type * 2) + " timber · " + CROPS[type];
    return true;
  }

  public int harvestAll() {
    int count = 0;
    for (int i = 0; i < plots(); i++) if (harvest(i)) count++;
    if (count == 0) notice = "No ripe crops yet. They grow while you play.";
    else notice = "Harvested " + count + " plots. Crops sold automatically.";
    return count;
  }

  public boolean upgrade(int kind) {
    if (kind < 0 || kind > 3 || defeated || maxed(kind)) return false;
    int price = cost(kind);
    if (coins < price) {
      notice = "Need " + price + " coins. Harvest or survive a night.";
      return false;
    }
    money(-price);
    if (kind == 0) weapon++;
    else if (kind == 1) {
      fence++;
      health += 50;
      for (int side = 0; side < 4; side++) if (walls[side] > 0)
        wallHealth[side] = Math.min(maxWallHealth(side), wallHealth[side] + 15);
    } else if (kind == 2) irrigation++;
    else land = 1;
    notice =
        new String[] {
              "Weapon upgraded. More damage, faster shots.",
              "Fence reinforced. +50 strength.",
              "Irrigation upgraded. Crops grow 20% faster.",
              "Three more farm plots unlocked."
            }
            [kind];
    return true;
  }

  public boolean repair() {
    if (defeated || health >= maxHealth()) {
      notice = "The homestead is already in good shape.";
      return false;
    }
    if (coins < 8) {
      notice = "Homestead repairs cost 8 coins. Recovery is free after defeat.";
      return false;
    }
    money(-8);
    health = Math.min(maxHealth(), health + 40);
    notice = "Homestead repaired · +40 health";
    return true;
  }

  public void startNight() {
    if (night || defeated) return;
    night = true;
    elapsed = 0;
    spawned = 0;
    for (Roamer roamer : roamers) {
      if (spawned >= quota()) break;
      float hp = enemyHealth(roamer.type);
      int side = (int) roamer.progress;
      enemies.add(new Enemy(roamer.type, side, roamer.distance, hp, hp, roamer.progress - side));
      spawned++;
    }
    roamers.clear();
    spawnClock = reinforcementSeconds();
    shotClock = 0;
    focusClock = 0;
    notice = "Raid " + day + " · Level " + level() + " · roaming zombies are attacking the fence!";
  }

  public boolean focus(int targetLane) {
    if (targetLane < 0 || targetLane > 3 || defeated) return false;
    lane = targetLane;
    if (!night || focusClock > 0) return false;
    Enemy e = target(true);
    if (e == null) return false;
    e.hp -= 18 + weapon * 5;
    focusClock = 3;
    notice = "Focus shot · " + SIDES[lane] + " border";
    removeDead();
    return true;
  }

  private Enemy target(boolean onlyLane) {
    Enemy chosen = null;
    for (Enemy e : enemies)
      if ((!onlyLane || e.lane == lane) && (chosen == null || e.x < chosen.x)) chosen = e;
    return chosen;
  }

  private void removeDead() {
    Iterator<Enemy> it = enemies.iterator();
    while (it.hasNext())
      if (it.next().hp <= 0) {
        it.remove();
        kills++;
        money(2);
        experience(3);
      }
  }

  public void tick(float dt) {
    if (!Float.isFinite(dt) || dt <= 0 || defeated) return;
    dt = Math.min(dt, 0.1f); // No catch-up damage after frame stalls or backgrounding.
    float rate = growthRate();
    for (int i = 0; i < plots(); i++)
      if (crop[i] >= 0) growth[i] = Math.min(1, growth[i] + dt * rate / GROW[crop[i]]);
    if (buildings[0]) {
      productionClock += dt;
      if (productionClock >= 12) { productionClock -= 12; timber(4); }
    }
    patrol(dt);
    elapsed += dt;
    if (!night) {
      populateRoamers();
      for (Roamer roamer : roamers)
        roamer.progress = (roamer.progress + dt * (.045f + roamer.type * .008f)) % 4;
      if (elapsed >= preparationSeconds()) startNight();
      return;
    }
    focusClock = Math.max(0, focusClock - dt);
    spawnClock = Math.max(-.1f, spawnClock - dt);
    if (spawned < quota() && spawnClock <= 0) {
      int kind = random(Math.min(4, day));
      float hp = enemyHealth(kind);
      enemies.add(new Enemy(kind, random(4), 1, hp, hp, .12f + random(760) / 1000f));
      spawned++;
      spawnClock = reinforcementSeconds();
    }
    shotClock = Math.max(-.1f, shotClock - dt);
    if (shotClock <= 0 && !enemies.isEmpty()) {
      Enemy e = target(true);
      if (e == null) e = target(false);
      e.hp -= 12 + weapon * 7;
      shotClock = Math.max(.22f, .7f - weapon * .075f);
      removeDead();
    }
    for (Enemy e : enemies) {
      if (!e.breached) {
        float speed = new float[] {.040f, .068f, .024f, .033f}[e.type];
        float stop = e.type == 3 && wallHealth[e.lane] > 0 ? .26f : 0;
        e.x = Math.max(stop, e.x - speed * dt * (weather == 1 ? .85f : 1));
        if (e.x <= stop && wallHealth[e.lane] > 0) {
          float damage = (e.type == 2 ? 10 : 6) * dt * (relaxed ? .5f : 1)
              * (walls[e.lane] == 2 ? .72f : walls[e.lane] >= 3 ? .5f : 1);
          wallHealth[e.lane] = Math.max(0, wallHealth[e.lane] - damage);
          if (wallHealth[e.lane] == 0) notice = SIDES[e.lane] + " fence breached! Repair it before more zombies enter.";
        }
        if (wallHealth[e.lane] <= 0 && e.x <= 0) {
          e.breached = true; e.target = assaultTarget(e.lane); e.attackClock = 1.2f;
          notice = "Zombie inside the farm! Rally guards and protect your harvest.";
        }
      }
      if (e.breached) {
        e.advance = Math.min(1, e.advance + dt * .55f);
        e.attackClock = Math.max(0, e.attackClock - dt);
        if (e.advance >= 1 && e.attackClock <= 0) {
          if (!targetAlive(e.target)) e.target = assaultTarget(e.lane);
          damageTarget(e.target, (e.type == 2 ? 18 : e.type == 1 ? 8 : 10) * (relaxed ? .5f : 1));
          e.attackClock = 1.2f;
        }
      }
    }
    if (health <= 0) {
      health = 0;
      defeated = true;
      notice = "Homestead overrun. Recover free; ruined crops and buildings need replanting or rebuilding.";
      return;
    }
    if ((spawned >= quota() && enemies.isEmpty()) || elapsed >= 75) dawn();
  }

  private void dawn() {
    int reward = 20 + Math.min(day, 30) * 3;
    money(reward);
    timber(6);
    experience(20);
    day = Math.min(9999, day + 1);
    night = false;
    elapsed = 0;
    enemies.clear();
    spawned = 0;
    spawnClock = 0;
    shotClock = 0;
    focusClock = 0;
    health = Math.min(maxHealth(), health + 15);
    weather = random(4);
    populateRoamers();
    notice =
        "Dawn · +" + reward + " coins. " + WEATHER[weather] + " ahead. Plant, repair, prepare.";
  }

  public void recover() {
    if (!defeated) return;
    int lost = Math.min(30, coins / 5);
    money(-lost);
    defeated = false;
    day = Math.min(9999, day + 1);
    night = false;
    elapsed = 0;
    health = maxHealth();
    enemies.clear();
    spawned = 0;
    spawnClock = 0;
    shotClock = 0;
    focusClock = 0;
    weather = random(4);
    roamers.clear();
    populateRoamers();
    for (int side = 0; side < 4; side++) wallHealth[side] = maxWallHealth(side);
    int aid = Math.max(0, 12 - coins); money(aid);
    notice = "Homestead recovered. Replant ruined crops and rebuild lost buildings. "
        + (aid > 0 ? "+" + aid + " seed coins." : "Lost " + lost + " coins.");
  }

  public int sale(int type) { return SALE[type] + (buildings[1] ? SALE[type] / 4 : 0); }

  private void timber(int amount) { wood = Math.min(10_000_000, Math.max(0, wood + amount)); }

  public float maxWallHealth(int side) { return walls[side] == 0 ? 0 : 80 + (walls[side] - 1) * 40 + fence * 15; }
  public boolean wallDamaged(int side) { return walls[side] > 0 && wallHealth[side] < maxWallHealth(side); }

  public boolean repairWall(int side) {
    if (defeated || side < 0 || side > 3 || !wallDamaged(side)) return false;
    if (coins < 8 || wood < 4) { notice = "Fence repair needs 8 coins and 4 timber. Harvest or buy timber in Fence."; return false; }
    money(-8); timber(-4); wallHealth[side] = Math.min(maxWallHealth(side), wallHealth[side] + 50);
    notice = SIDES[side] + " fence repaired. Zombies already inside still need to be defeated.";
    return true;
  }

  public boolean buyTimber() {
    if (defeated || coins < 12 || wood > 9_999_990) { notice = "A 10-timber bundle costs 12 coins."; return false; }
    money(-12); timber(10); notice = "+10 timber · 12 coins. Build fences, repair damage or hire guards.";
    return true;
  }

  public boolean repairBuilding(int kind) {
    if (defeated || kind < 0 || kind > 2 || !buildings[kind] || buildingHealth[kind] >= BUILDING_HEALTH) return false;
    if (coins < 10 || wood < 5) { notice = "Building repairs need 10 coins and 5 timber."; return false; }
    money(-10); timber(-5); buildingHealth[kind] = Math.min(BUILDING_HEALTH, buildingHealth[kind] + 60);
    notice = BUILDINGS[kind] + " repaired."; return true;
  }

  private boolean targetAlive(int target) {
    return target < 0 || (target < 9 ? crop[target] >= 0 : buildings[target - 9]);
  }

  private int assaultTarget(int side) {
    int best = -1, score = Integer.MAX_VALUE;
    for (int p = 0; p < plots(); p++) if (crop[p] >= 0) {
      int rank = side == 0 ? p : side == 1 ? (2 - p % 3) * 3 + p / 3
          : side == 2 ? 8 - p : p % 3 * 3 + p / 3;
      if (rank < score) { best = p; score = rank; }
    }
    if (best >= 0) return best;
    for (int b = 0; b < 3; b++) if (buildings[b]) return 9 + b;
    return -1;
  }

  private void damageTarget(int target, float damage) {
    if (target < 0) { health = Math.max(0, health - damage); return; }
    if (target < 9) {
      cropHealth[target] = Math.max(0, cropHealth[target] - damage);
      if (cropHealth[target] == 0 && crop[target] >= 0) {
        crop[target] = -1; growth[target] = 0; scorched[target] = true;
        lostCrops = Math.min(10_000_000, lostCrops + 1);
        notice = "Farm plot " + (target + 1) + " destroyed! No harvest earned. Repair the breach and replant.";
      }
    } else {
      int kind = target - 9;
      buildingHealth[kind] = Math.max(0, buildingHealth[kind] - damage);
      if (buildingHealth[kind] == 0 && buildings[kind]) {
        buildings[kind] = false; buildingRuins[kind] = true;
        if (kind == 0) productionClock = 0;
        lostBuildings = Math.min(10_000_000, lostBuildings + 1);
        notice = BUILDINGS[kind] + " destroyed! Rebuild in Farm. Existing mercenaries stay with you.";
      }
    }
  }

  public int wallCoinCost(int side) { return 20 + walls[side] * 12; }
  public int wallWoodCost(int side) { return 8 + walls[side] * 6; }

  public boolean buildWall(int side) {
    if (defeated || side < 0 || side > 3 || walls[side] >= 3) return false;
    int coinsNeeded = wallCoinCost(side), woodNeeded = wallWoodCost(side);
    if (coins < coinsNeeded || wood < woodNeeded) {
      notice = "Need " + coinsNeeded + " coins and " + woodNeeded + " timber for this border.";
      return false;
    }
    money(-coinsNeeded); timber(-woodNeeded); walls[side]++;
    wallHealth[side] = maxWallHealth(side);
    health = Math.min(maxHealth(), health + 20);
    notice = SIDES[side] + " fence tier " + walls[side] + " · " + Math.round(wallHealth[side]) + " health. Protect your crops.";
    return true;
  }

  public boolean construct(int kind) {
    if (defeated || kind < 0 || kind > 2 || buildings[kind]) return false;
    if (coins < BUILD_COINS[kind] || wood < BUILD_WOOD[kind]) {
      notice = "Gather more coins and timber to build " + BUILDINGS[kind] + ".";
      return false;
    }
    money(-BUILD_COINS[kind]); timber(-BUILD_WOOD[kind]); buildings[kind] = true;
    buildingHealth[kind] = BUILDING_HEALTH; buildingRuins[kind] = false;
    notice = BUILDINGS[kind] + " built · " + new String[] {
        "+4 timber every 12 active seconds", "+25% crop sale value", "mercenary recruitment unlocked"}[kind];
    return true;
  }

  public int hireCost() { return 55 + mercenaries.size() * 25; }

  public boolean hire() {
    if (defeated || !buildings[2] || mercenaries.size() >= MAX_MERCS) return false;
    if (coins < hireCost() || wood < 6) { notice = "Recruitment needs " + hireCost() + " coins and 6 timber."; return false; }
    money(-hireCost()); timber(-6);
    mercenaries.add(new Mercenary((mercenaries.size() * .65f) % 4, 0, .5f, .5f));
    notice = "Mercenary hired. Patrols roam the fence and fire at nearby zombies.";
    return true;
  }

  /** Clockwise perimeter in normalized world coordinates, shared with the renderer. */
  public static float patrolX(float progress) {
    if (progress < 1) return .18f + progress * .64f;
    if (progress < 2) return .82f;
    if (progress < 3) return .82f - (progress - 2) * .64f;
    return .18f;
  }

  public static float patrolY(float progress) {
    if (progress < 1) return .18f;
    if (progress < 2) return .18f + (progress - 1) * .64f;
    if (progress < 3) return .82f;
    return .82f - (progress - 3) * .64f;
  }

  public static float enemyX(Enemy e) {
    return ringX(e.lane + e.offset, e.breached ? -.8f * e.advance : e.x);
  }

  public static float enemyY(Enemy e) {
    return ringY(e.lane + e.offset, e.breached ? -.8f * e.advance : e.x);
  }

  public static float ringX(float progress, float distance) {
    float left = .18f - distance * .18f, right = .82f + distance * .18f;
    if (progress < 1) return left + progress * (right - left);
    if (progress < 2) return right;
    if (progress < 3) return right - (progress - 2) * (right - left);
    return left;
  }

  public static float ringY(float progress, float distance) {
    float top = .18f - distance * .18f, bottom = .82f + distance * .18f;
    if (progress < 1) return top;
    if (progress < 2) return top + (progress - 1) * (bottom - top);
    if (progress < 3) return bottom;
    return bottom - (progress - 3) * (bottom - top);
  }

  private float enemyHealth(int type) {
    return new float[] {25, 18, 68, 32}[type] + Math.min(day - 1, 20) * 2;
  }

  public static final class Roamer {
    public int type;
    public float progress, distance;
    public Roamer(int type, float progress, float distance) {
      this.type = type; this.progress = progress; this.distance = distance;
    }
  }

  private void patrol(float dt) {
    for (Mercenary merc : mercenaries) {
      merc.progress = (merc.progress + dt * .14f) % 4;
      merc.shotClock = Math.max(0, merc.shotClock - dt);
      if (!night || merc.shotClock > 0) continue;
      float mx = patrolX(merc.progress), my = patrolY(merc.progress), nearest = .28f * .28f;
      Enemy chosen = null;
      for (Enemy e : enemies) {
        if (e.hp <= 0) continue;
        float dx = enemyX(e) - mx, dy = enemyY(e) - my, distance = dx * dx + dy * dy;
        if (distance < nearest) { chosen = e; nearest = distance; }
      }
      if (chosen != null) {
        merc.targetX = enemyX(chosen); merc.targetY = enemyY(chosen);
        chosen.hp -= 9 + weapon * 2; merc.shotClock = .9f;
      }
    }
    removeDead();
  }

  public static final class Mercenary {
    public float progress, shotClock, targetX, targetY;
    public Mercenary(float progress, float shotClock, float targetX, float targetY) {
      this.progress = progress; this.shotClock = shotClock; this.targetX = targetX; this.targetY = targetY;
    }
  }

  public static final class Enemy {
    // lane is the persisted side index: north, east, south, west. x is distance outside the wall.
    public int type, lane, target = -1;
    public boolean breached;
    public float advance, attackClock;
    public float x, hp, maxHp, offset;

    public Enemy(int type, int lane, float x, float hp, float maxHp) {
      this(type, lane, x, hp, maxHp, .5f);
    }
    public Enemy(int type, int lane, float x, float hp, float maxHp, float offset) {
      this.type = type; this.lane = lane; this.x = x; this.hp = hp; this.maxHp = maxHp; this.offset = offset;
    }
  }
}
