package com.denberg.beyondthefence;

import java.util.ArrayList;
import java.util.Iterator;

/** Deterministic simulation. No Android, disk, network or wall-clock dependencies. */
public final class Game {
  public static final String[] CROPS = {"Turnip", "Corn", "Pumpkin"};
  public static final int[] SEED = {4, 10, 18}, SALE = {12, 26, 44};
  public static final float[] GROW = {16, 29, 43};
  public static final String[] WEATHER = {"Clear", "Rain", "Fog", "Heat"};
  public static final String[] FOES = {"Walker", "Runner", "Brute", "Spitter", "Ghoul", "Ironhide", "Wraith", "Grave Warden", "Kobold skirmisher", "Orc raider", "Dire wolf", "Goblin hexer"};
  public static final String[] UNIFORMS = {"Workwear", "Scout green", "Desert kit", "Urban armor", "Ranger kit", "Black ops"};
  public static final String[] FENCE_MATERIALS = {"Gap", "Palisade", "Stone wall", "Ironbound"};
  private static final float[] FOE_HP = {42, 30, 125, 65, 90, 190, 80, 400, 38, 145, 36, 70}, FOE_SPEED = {.105f, .21f, .065f, .085f, .15f, .055f, .185f, .048f, .18f, .063f, .245f, .08f}, FOE_DAMAGE = {10, 7, 25, 13, 18, 32, 16, 38, 8, 24, 8, 12};
  public static final float GUARD_RANGE = .28f;
  public static final float DAY_SECONDS = 75;
  public static final String[] SIDES = {"North", "East", "South", "West"};
  public static final String[] BUILDINGS = {"Timber mill", "Market", "Barracks"};
  public static final int[] BUILD_COINS = {50, 80, 70}, BUILD_WOOD = {10, 18, 12};
  public static final int MAX_MERCS = 3;
  public static final int MAX_ROAMERS = 10;
  public final ArrayList<Roamer> roamers = new ArrayList<>();
  public static final float CROP_HEALTH = 40, BUILDING_HEALTH = 100;
  public final float[] wallHealth = {80, 0, 80, 0}, cropHealth = new float[9], buildingHealth = new float[3];
  public final boolean[] scorched = new boolean[9], buildingRuins = new boolean[3];
  public int lostCrops, lostBuildings;
  public final int[] wallRanks = new int[4];
  public int drill, millLevel, heroLevel, elixirs, enhancers;
  public float enhancerTime;
  public boolean bossSpawned;
  public int bolts, blasts;
  public final int[] equipped = {0, 1, 2};
  public final boolean[] collected = new boolean[Equipment.COUNT];
  public final int[] gear = {-1, -1, -1, -1, -1, -1, -1, -1, -1};
  public int lootState = 57391, lastDrop = -1, world = 1;
  public boolean worldComplete, directorEnabled = true;
  public int directorPressure, calmNights;
  public float raidStrain;
  public int arenaOfferDay, arenaDay, arenaChampion, arenaResult;
  public boolean arenaActive;
  public float arenaHealth, arenaEnemyHealth, arenaElapsed, arenaAttackClock, arenaEnemyClock, strikeClock;
  public static final String[] CHAMPIONS = {"Morrow, the Hollow Knight", "Vex, the Grave Huntress", "Gorath, the Bone Colossus", "Nyra, the Ash Witch"};
  public static final String[] ITEMS = {"Elixir", "Enhancer", "Arc bolt", "Starfall"};
  public float spellTime, spellX, spellY;
  public boolean spellArea; // Transient cosmetic cue; never changes combat or saved randomness.
  public Mercenary hero;
  public static final int MAX_WALL_RANK = 4, MAX_ITEMS = 9;
  public boolean ashenWorld() { return World.ashen(world); }
  public String worldName() { return World.name(world); }
  public String worldChapter() { return World.chapter(world, day); }
  public String weatherName() { return World.weatherName(world, weather); }
  public String settlementName() { return World.settlementName(world, settlementStyle()); }
  public boolean ashfall() { return ashenWorld() && weather == 2; }
  public String bossName() { return ashenWorld() ? "Cinder Regent" : "Grave Warden"; }
  public int settlementStyle() { return World.settlementStyle(world, day); }
  public float ringXAt(float progress, float distance) { return Settlement.x(settlementStyle(), progress, distance); }
  public float ringYAt(float progress, float distance) { return Settlement.y(settlementStyle(), progress, distance); }
  public float guardX(float progress) { return ringXAt(progress, 0); }
  public float guardY(float progress) { return ringYAt(progress, 0); }
  public float foeX(Enemy e) { return ringXAt(e.lane + e.offset, e.breached ? -.8f * e.advance : e.x); }
  public float foeY(Enemy e) { return ringYAt(e.lane + e.offset, e.breached ? -.8f * e.advance : e.x); }

  public int wood = 24;
  public final int[] walls = {1, 0, 1, 0};
  public final boolean[] buildings = new boolean[3];
  public final ArrayList<Mercenary> mercenaries = new ArrayList<>();
  public float productionClock;
  public final int[] crop = new int[9];
  public final float[] growth = new float[9];
  public final ArrayList<Enemy> enemies = new ArrayList<>();
  public int day = 1, coins = 80, xp, weather, weapon, fence, irrigation, land;
  public int kills, harvested, spawned, lane = -1;
  // weapon is the retained save-field name for uniform tier; xp is legacy-only. The two legacy clocks never fire.
  public float health = 100, elapsed, shotClock, spawnClock, focusClock;
  public boolean night, defeated, relaxed, reducedMotion, tutorialDone;
  public int randomState = 72419;
  public String notice = "Your farm fortress. Harvest crops, close fence gaps and recruit patrols.";
  public long revision;
  public long shots; // Transient audio event sequence; never drives combat or save state.

  public Game() {
    java.util.Arrays.fill(crop, -1);
    populateRoamers();
    ensureVolunteer();
  }

  public float preparationSeconds() { return Math.max(35, DAY_SECONDS - 4 * Math.min(10, day - 1)); }
  public float reinforcementSeconds() { return Math.max(.6f, (3.2f - .18f * Math.min(14, day - 1) - Math.max(0, Campaign.day(day) - 30) * .004f) * (directorEnabled ? directorPressure < 0 ? 1.08f : directorPressure > 0 ? .94f : 1 : 1)); }
  public int raidInSeconds() { return (int) Math.ceil(Math.max(0, preparationSeconds() - elapsed)); }

  /** Bounded, deterministic scouts; these are promoted into real enemies when a raid begins. */
  void populateRoamers() {
    if (night || defeated) return;
    int desired = Math.min(MAX_ROAMERS, 3 + day);
    while (roamers.size() < desired) {
      int i = roamers.size();
      roamers.add(new Roamer(Bestiary.scout(day, i), (i * 2.472f + .23f) % 4, .65f + i % 3 * .1f));
    }
  }

  public int plots() {
    return land == 0 ? 6 : 9;
  }

  public int unlockedCrops() {
    return Math.min(3, day);
  }

  public int maxHealth() {
    return 100 + fence * 50;
  }

  public int quota() {
    return Math.min(Campaign.MAX_ENEMIES, 7 + Campaign.day(day) * 3);
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
    return (kind == 0 ? weapon : kind == 1 ? fence : kind == 2 ? irrigation : land) >= upgradeCap(kind);
  }

  public int upgradeCap(int kind) { return Campaign.upgradeCap(kind, day); }
  public int wallRankCap() { return Campaign.wallCap(day); }
  public int advancedCap(int kind) { return Campaign.advancedCap(kind, day); }
  public boolean bossDay() { return Campaign.bossDay(day); }
  public Enemy boss() { for (Enemy e : enemies) if (e.type == Campaign.BOSS) return e; return null; }

  private void money(int amount) {
    coins = Math.min(10_000_000, Math.max(0, coins + amount));
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
    if (kind == 0) {
      weapon++;
      for (Mercenary merc : mercenaries) merc.health = Math.min(guardMaxHealth(), merc.health + 20);
    } else if (kind == 1) {
      fence++;
      health += 50;
      for (int side = 0; side < 4; side++) if (walls[side] > 0)
        wallHealth[side] = Math.min(maxWallHealth(side), wallHealth[side] + 15);
    } else if (kind == 2) irrigation++;
    else land = 1;
    notice =
        new String[] {
              "Uniform upgraded. Better aim, armor and faster fire for all three guards.",
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
    if (night || defeated || arenaActive || arenaOfferDay > 0 || worldComplete) return;
    raidStrain = 0;
    night = true;
    elapsed = 0;
    spawned = 0; bossSpawned = false;
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
    notice = "Night " + day + " · Survive the horde. The horde is attacking the walls!";
  }

  public boolean rally(int side) {
    if (side < 0 || side > 3 || defeated) return false;
    lane = side;
    notice = "Mercenaries rallying to the " + SIDES[side] + " fence.";
    return true;
  }

  public void resumePatrol() {
    lane = -1; notice = "Mercenaries resume their perimeter patrol.";
  }

  void ensureVolunteer() {
    if (mercenaries.isEmpty()) {
      Mercenary volunteer = new Mercenary(.5f, 0, .5f, .5f);
      volunteer.health = guardMaxHealth(); mercenaries.add(volunteer);
    }
  }

  public int guardDamage() { return Math.round(guardAttackDamage()); }
  private float guardAttackDamage() { return Math.round((16 + weapon * 3 + drill * 3) * (enhancerTime > 0 ? 1.4f : 1)) * (1 + equipmentBonus(0)); }
  public float guardMaxHealth() { return 80 + weapon * 20 + drill * 10; }
  public float guardRange() { return GUARD_RANGE + weapon * .008f + drill * .005f; }
  public float guardInterval() { return Math.max(.65f, 1.15f - weapon * .07f - drill * .025f) * (enhancerTime > 0 ? .8f : 1) / (1 + equipmentBonus(3)); }
  public float wallRatio(int side) { return walls[side] == 0 ? 0 : Math.max(0, Math.min(1, wallHealth[side] / maxWallHealth(side))); }

  private void removeDead() {
    Iterator<Enemy> it = enemies.iterator();
    while (it.hasNext())
      { Enemy dead = it.next(); if (dead.hp <= 0) {
        it.remove();
        kills++;
        money(dead.type == Campaign.BOSS ? 10 + Math.min(20, day / 5) : 1); // Small salvage reward; harvest remains the primary income.
        rollEquipment(dead.type == Campaign.BOSS ? 1 : 0);
      } }
  }

  public void tick(float dt) {
    if (!Float.isFinite(dt) || dt <= 0 || defeated) return;
    dt = Math.min(dt, 0.1f); // No catch-up damage after frame stalls or backgrounding.
    if (arenaActive) { tickArena(dt); return; }
    if (worldComplete || arenaOfferDay > 0) return;
    enhancerTime = Math.max(0, enhancerTime - dt);
    spellTime = Math.max(0, spellTime - dt);
    float rate = growthRate();
    for (int i = 0; i < plots(); i++)
      if (crop[i] >= 0) growth[i] = Math.min(1, growth[i] + dt * rate / GROW[crop[i]]);
    if (buildings[0]) {
      productionClock += dt;
      if (productionClock >= millPeriod()) { productionClock -= millPeriod(); timber(millYield()); }
    }
    patrol(dt);
    elapsed = Math.min(86400, elapsed + dt);
    if (!night) {
      populateRoamers();
      for (Roamer roamer : roamers) if (roamerActive(roamer))
        roamer.progress = (roamer.progress + dt * (.008f + roamer.type * .0015f)) % 4;
      if (elapsed >= preparationSeconds()) startNight();
      return;
    }
    spawnClock = Math.max(-.1f, spawnClock - dt);
    if (spawned < quota() && spawnClock <= 0) {
      int kind = bossDay() && !bossSpawned && spawned >= quota() / 2 ? Campaign.BOSS : Bestiary.pick(day, random(Bestiary.weight(day)));
      if (kind == Campaign.BOSS) { bossSpawned = true; notice = bossName() + " arrives! Defeat the five-day boss."; }
      float hp = enemyHealth(kind);
      enemies.add(new Enemy(kind, random(4), 1, hp, hp, .12f + random(760) / 1000f));
      spawned++;
      spawnClock = reinforcementSeconds();
    }
    for (Enemy e : enemies) {
      if (!e.breached) {
        float speed = enemySpeed(e.type) * movementFactor(e);
        float stop = Bestiary.ranged(e.type) && wallHealth[e.lane] > 0 ? .26f : 0;
        e.x = Math.max(stop, e.x - speed * dt * (weather == 1 || ashfall() ? .85f : 1));
        if (e.x <= stop && wallHealth[e.lane] > 0) {
          float damage = enemyDamage(e.type) * dt * (relaxed ? .5f : 1) * (1 - equipmentBonus(1))
              * (walls[e.lane] == 2 ? .72f : walls[e.lane] >= 3 ? Math.max(.3f, .5f - wallRanks[e.lane] * .05f) : 1);
          wallHealth[e.lane] = Math.max(0, wallHealth[e.lane] - damage);
          raidStrain = Math.min(1, raidStrain + damage / (maxWallHealth(e.lane) + maxHealth()) / 4);
          if (wallHealth[e.lane] == 0) notice = SIDES[e.lane] + " fence breached! Repair it before more zombies enter.";
        }
        if (wallHealth[e.lane] <= 0 && e.x <= 0) {
          e.breached = true; e.target = assaultTarget(e.lane); e.attackClock = 1.2f;
          notice = "Enemy inside the haven! Rally guards and protect your harvest.";
        }
      }
      if (e.breached) {
        e.advance = Math.min(1, e.advance + dt * .55f);
        e.attackClock = Math.max(0, e.attackClock - dt);
        if (e.advance >= 1 && e.attackClock <= 0) {
          if (!targetAlive(e.target)) e.target = assaultTarget(e.lane);
          float damage = enemyDamage(e.type) * 1.2f * (relaxed ? .5f : 1) * (1 - equipmentBonus(1));
          raidStrain = Math.min(1, raidStrain + damage / maxHealth());
          Mercenary victim = null; float closest = .20f * .20f;
          for (int person = 0; person < mercenaries.size() + (hero == null ? 0 : 1); person++) {
            Mercenary merc = person < mercenaries.size() ? mercenaries.get(person) : hero;
            float dx = foeX(e) - guardX(merc.progress), dy = foeY(e) - guardY(merc.progress);
            float distance = dx * dx + dy * dy;
            if (distance < closest) { closest = distance; victim = merc; }
          }
          if (victim != null) {
            victim.health = Math.max(0, victim.health - damage * (victim == hero ? .65f : Math.max(.45f, 1 - weapon * .08f - drill * .04f)));
            if (victim.health == 0) {
              if (victim == hero) { hero = null; notice = "Hero lost. Recruit a replacement at the Barracks."; }
              else { mercenaries.remove(victim); notice = "Mercenary lost! The horde can wipe out your crew."; }
            }
          } else damageTarget(e.target, damage);
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
    if (spawned >= quota() && enemies.isEmpty()) dawn();
  }

  private void dawn() {
    int finished = day;
    evaluateDirector();
    if (finished <= 100 && finished % 10 == 0 && arenaDay < finished) arenaOfferDay = finished;
    worldComplete = finished >= 100; // Older endless-mode farms finish their current night before the rift.
    int reward = 20 + Math.min(day, 30) * 3;
    money(reward);
    timber(6);
    day = worldComplete ? 100 : Math.min(9999, day + 1); bossSpawned = false;
    night = false;
    elapsed = 0;
    enemies.clear();
    spawned = 0;
    spawnClock = 0;
    shotClock = 0;
    focusClock = 0;
    health = Math.min(maxHealth(), health + 15);
    weather = World.weather(world, day, random(4));
    populateRoamers();
    notice =
        "Dawn · +" + reward + " coins. " + weatherName() + " ahead. " + (ashenWorld() ? worldChapter() : "Plant, repair, prepare.");
  }

  public void recover() {
    if (!defeated) return;
    int lost = Math.min(30, coins / 5);
    money(-lost);
    defeated = false;
    if (directorEnabled) { directorPressure = -1; calmNights = 0; }
    raidStrain = 0;
    enhancerTime = 0; spellTime = 0; bossSpawned = false;
    night = false;
    elapsed = 0;
    health = maxHealth();
    enemies.clear();
    spawned = 0;
    spawnClock = 0;
    shotClock = 0;
    focusClock = 0;
    weather = World.weather(world, day, random(4));
    roamers.clear();
    populateRoamers();
    for (int side = 0; side < 4; side++) wallHealth[side] = maxWallHealth(side);
    int aid = Math.max(0, 12 - coins); money(aid);
    ensureVolunteer();
    notice = "Homestead recovered. Replant ruined crops and rebuild lost buildings. "
        + (aid > 0 ? "+" + aid + " seed coins." : "Lost " + lost + " coins.");
  }

  public int sale(int type) { return SALE[type] + (buildings[1] ? SALE[type] / 4 : 0); }

  private void timber(int amount) { wood = Math.min(10_000_000, Math.max(0, wood + amount)); }

  public float maxWallHealth(int side) { return walls[side] == 0 ? 0 : 80 + (walls[side] - 1) * 40 + fence * 15 + wallRanks[side] * 30; }
  public boolean wallDamaged(int side) { return walls[side] > 0 && wallHealth[side] < maxWallHealth(side); }

  public boolean repairWall(int side) {
    if (defeated || side < 0 || side > 3) return false;
    if (!wallDamaged(side)) {
      notice = walls[side] == 0 ? "Open gap. Build this border in Build → Fence." : SIDES[side] + " fence is intact. No coins spent.";
      return false;
    }
    if (coins < 8) { notice = "Fence repair needs8 coins. Harvest crops to fund repairs."; return false; }
    money(-8); wallHealth[side] = Math.min(maxWallHealth(side), wallHealth[side] + 50);
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
    notice = SIDES[side] + " " + FENCE_MATERIALS[walls[side]] + " fence tier " + walls[side] + " · " + Math.round(wallHealth[side]) + " health. Protect your crops.";
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
    Mercenary recruit = new Mercenary((mercenaries.size() * .65f) % 4, 0, .5f, .5f);
    recruit.health = guardMaxHealth(); mercenaries.add(recruit);
    notice = "Mercenary hired. Patrols roam the fence and fire at nearby zombies.";
    return true;
  }

  public int borderCoinCost(int side) { return walls[side] < 3 ? wallCoinCost(side) : 120 + wallRanks[side] * 60; }
  public int borderWoodCost(int side) { return walls[side] < 3 ? wallWoodCost(side) : 18 + wallRanks[side] * 8; }
  public boolean borderMaxed(int side) { return walls[side] >= 3 && wallRanks[side] >= wallRankCap(); }
  public boolean upgradeBorder(int side) {
    if (defeated || side < 0 || side > 3 || borderMaxed(side)) return false;
    if (walls[side] < 3) return buildWall(side);
    if (coins < borderCoinCost(side) || wood < borderWoodCost(side)) return false;
    money(-borderCoinCost(side)); timber(-borderWoodCost(side)); wallRanks[side]++;
    wallHealth[side] = maxWallHealth(side); notice = SIDES[side] + " reinforced Steel rank " + wallRanks[side]; return true;
  }
  public int advancedCost(int kind) { return kind == 0 ? 90 + millLevel * 80 : kind == 1 ? 140 + drill * 100 : 220 + heroLevel * 150; }
  public int advancedWood(int kind) { return kind == 0 ? 12 + millLevel * 6 : kind == 1 ? 8 : 12; }
  public boolean advancedMaxed(int kind) { return (kind == 0 ? millLevel : kind == 1 ? drill : heroLevel) >= advancedCap(kind); }
  public boolean upgradeAdvanced(int kind) {
    if (defeated || kind < 0 || kind > 2 || advancedMaxed(kind) || !(kind == 0 ? buildings[0] : buildings[2])
        || kind == 2 && hero == null || coins < advancedCost(kind) || wood < advancedWood(kind)) return false;
    money(-advancedCost(kind)); timber(-advancedWood(kind));
    if (kind == 0) millLevel++;
    else if (kind == 1) { drill++; for (Mercenary m : mercenaries) m.health = Math.min(guardMaxHealth(), m.health + 10); }
    else { heroLevel++; hero.health = Math.min(heroMaxHealth(), hero.health + 40); }
    notice = "Upgrade purchased."; return true;
  }
  public float millPeriod() { return Math.max(3, 12 - millLevel); }
  public int millYield() { return 4 + millLevel * 2; }
  public float heroMaxHealth() { return 220 + heroLevel * 40; }
  public boolean buyHero() {
    if (defeated || !buildings[2] || hero != null || coins < 360 || wood < 20) return false;
    money(-360); timber(-20); hero = new Mercenary(2.5f, 0, .5f, .5f); hero.health = heroMaxHealth();
    notice = "Hero recruited. One hero fights alongside your three mercenaries."; return true;
  }
  public int itemPrice(int type) { return new int[] {35, 45, 30, 65}[type]; }
  public int itemStock(int type) { return type == 0 ? elixirs : type == 1 ? enhancers : type == 2 ? bolts : blasts; }
  public boolean equipItem(int slot, int type) {
    if (slot < 0 || slot >= equipped.length || type < 0 || type >= ITEMS.length || defeated) return false;
    equipped[slot] = type; notice = ITEMS[type] + " assigned to slot " + (slot + 1) + "."; return true;
  }
  public boolean buyItem(int type) {
    if (defeated || type < 0 || type >= ITEMS.length || !buildings[1] || itemStock(type) >= MAX_ITEMS || coins < itemPrice(type)) return false;
    money(-itemPrice(type)); if (type == 0) elixirs++; else if (type == 1) enhancers++; else if (type == 2) bolts++; else blasts++;
    notice = ITEMS[type] + " purchased. Hold a quick slot to change its potion."; return true;
  }
  public boolean useItem(int type) {
    if (defeated || type < 0 || type >= ITEMS.length) return false;
    if (arenaActive) return useArenaItem(type);
    if (type == 0) {
      boolean injured = health < maxHealth() || hero != null && hero.health < heroMaxHealth();
      for (Mercenary m : mercenaries) injured |= m.health < guardMaxHealth();
      if (elixirs == 0 || !injured) return false;
      elixirs--; health = Math.min(maxHealth(), health + 35);
      for (Mercenary m : mercenaries) m.health = Math.min(guardMaxHealth(), m.health + 60);
      if (hero != null) hero.health = Math.min(heroMaxHealth(), hero.health + 90);
      notice = "Elixir: farm and surviving crew healed.";
    } else if (type == 1) {
      if (enhancers == 0 || enhancerTime > 0) return false;
      enhancers--; enhancerTime = 20; notice = "Enhancer:20 active seconds of stronger, faster fire.";
    } else {
      if (!night || itemStock(type) == 0 || enemies.isEmpty()) return false;
      Enemy target = null; float closest = Float.MAX_VALUE;
      for (Enemy e : enemies) if (e.hp > 0) {
        float score = e.breached ? -1 - e.advance : e.x;
        if (score < closest) { closest = score; target = e; }
      }
      if (target == null) return false;
      float x = foeX(target), y = foeY(target);
      int damage = type == 2 ? 95 + Campaign.day(day) * 3 + heroLevel * 4 : 60 + Campaign.day(day) * 2 + heroLevel * 3;
      for (Enemy e : enemies) {
        float dx = foeX(e) - x, dy = foeY(e) - y;
        if (e == target || type == 3 && dx * dx + dy * dy <= .32f * .32f)
          e.hp -= damage * (e.type == Campaign.BOSS ? .6f : 1) * (1 + equipmentBonus(2));
      }
      if (type == 2) bolts--; else blasts--;
      spellX = x; spellY = y; spellTime = .7f; spellArea = type == 3;
      removeDead(); notice = ITEMS[type] + " cast on the nearest threat.";
    }
    return true;
  }

  public float equipmentBonus(int stat) {
    float sum = 0;
    for (int code : gear) if (code >= 0 && Equipment.stat(Equipment.slot(code)) == stat) sum += Equipment.value(code);
    return Math.min(stat == 0 || stat == 1 ? .12f : stat == 2 ? .10f : .06f, sum);
  }

  public boolean equipGear(int slot, int code) {
    if (defeated || arenaActive || slot < 0 || slot >= gear.length || code < -1 || code >= collected.length
        || code >= 0 && (!collected[code] || Equipment.slot(code) != slot)) return false;
    gear[slot] = code;
    notice = Equipment.SLOTS[slot] + (code < 0 ? " removed." : ": " + Equipment.name(code) + " equipped.");
    return true;
  }

  public void equipBest() {
    if (defeated || arenaActive) return;
    for (int slot = 0; slot < gear.length; slot++) {
      for (int quality = 4; quality >= 0; quality--) if (collected[slot * 5 + quality]) {
        gear[slot] = slot * 5 + quality; break;
      }
    }
    notice = "Best collected equipment fitted. Relics help; your defenses still decide survival.";
  }

  private int lootRandom(int bound) {
    lootState = lootState * 1664525 + 1013904223;
    return (lootState >>> 1) % bound;
  }

  /** Per kill: 0.4% ordinary, 12% boss, 55% arena. No luck/economy multiplier. */
  int rollEquipment(int source) {
    if (lootRandom(1000) >= (source == 2 ? 550 : source == 1 ? 120 : 4)) return -1;
    int roll = lootRandom(1000);
    int quality = source == 2 ? (roll < 20 ? 4 : roll < 160 ? 3 : roll < 650 ? 2 : 1)
        : source == 1 ? (roll < 5 ? 4 : roll < 55 ? 3 : roll < 300 ? 2 : roll < 650 ? 1 : 0)
        : (roll < 10 ? 2 : roll < 200 ? 1 : 0);
    int code = lootRandom(9) * 5 + quality;
    boolean duplicate = collected[code]; collected[code] = true; lastDrop = code;
    notice = duplicate ? "A familiar relic: " + Equipment.name(code) + ". Collection already holds it."
        : "Relic found: " + Equipment.name(code) + " · " + Equipment.RARITIES[quality] + ". Open Equipment.";
    return code;
  }

  void evaluateDirector() {
    if (!directorEnabled || day < 3) { raidStrain = 0; return; }
    if (raidStrain > .55f || health < maxHealth() * .5f) { directorPressure = -1; calmNights = 0; }
    else if (raidStrain < .15f) {
      calmNights = Math.min(2, calmNights + 1);
      if (calmNights == 2) directorPressure = Math.min(1, directorPressure + 1);
    } else { calmNights = 0; directorPressure = 0; }
    raidStrain = 0;
  }

  public String directorDescription() {
    return !directorEnabled ? "Director off · fixed pacing" : "Director · " + (directorPressure < 0 ? "breathing room" : directorPressure > 0 ? "rising tension" : "steady tension");
  }

  public boolean acceptArena() {
    if (arenaOfferDay == 0 || arenaActive || defeated || night) return false;
    arenaDay = arenaOfferDay; arenaOfferDay = 0; arenaChampion = lootRandom(CHAMPIONS.length);
    arenaActive = true; arenaResult = 0;
    arenaHealth = arenaMaxHealth(); arenaEnemyHealth = arenaMaxEnemyHealth();
    arenaElapsed = arenaAttackClock = strikeClock = 0; arenaEnemyClock = 1.6f;
    notice = "The arena opens. " + CHAMPIONS[arenaChampion] + " awaits.";
    return true;
  }

  public boolean declineArena() {
    if (arenaOfferDay == 0 || arenaActive) return false;
    arenaDay = arenaOfferDay; arenaOfferDay = 0; arenaResult = 4;
    notice = "The arena fades. Your survival comes first."; return true;
  }

  public float arenaMaxHealth() { return 180 + weapon * 8 + drill * 4 + heroLevel * 5; }
  public float arenaMaxEnemyHealth() { return 240 + arenaDay * 9 + (arenaChampion == 2 ? 100 : 0); }
  public float arenaDamage() { return (14 + weapon * 2 + drill + heroLevel) * (1 + equipmentBonus(0)) * (enhancerTime > 0 ? 1.4f : 1); }

  public boolean arenaStrike() {
    if (!arenaActive || strikeClock > 0) return false;
    arenaEnemyHealth = Math.max(0, arenaEnemyHealth - arenaDamage() * 1.6f);
    strikeClock = 2.4f; shots++;
    if (arenaEnemyHealth == 0) finishArena(1);
    return true;
  }

  public void retreatArena() { if (arenaActive) finishArena(3); }

  private boolean useArenaItem(int type) {
    if (itemStock(type) == 0) return false;
    if (type == 0) {
      if (arenaHealth >= arenaMaxHealth()) return false;
      elixirs--; arenaHealth = Math.min(arenaMaxHealth(), arenaHealth + 60);
    } else if (type == 1) {
      if (enhancerTime > 0) return false;
      enhancers--; enhancerTime = 20;
    } else {
      if (type == 2) bolts--; else blasts--;
      arenaEnemyHealth = Math.max(0, arenaEnemyHealth - (type == 2 ? 70 : 45) * (1 + equipmentBonus(2)));
      if (arenaEnemyHealth == 0) finishArena(1);
    }
    return true;
  }

  private void tickArena(float dt) {
    arenaElapsed = Math.min(45, arenaElapsed + dt);
    enhancerTime = Math.max(0, enhancerTime - dt);
    strikeClock = Math.max(0, strikeClock - dt);
    arenaAttackClock = Math.max(0, arenaAttackClock - dt);
    arenaEnemyClock = Math.max(0, arenaEnemyClock - dt);
    if (arenaAttackClock == 0) {
      arenaEnemyHealth = Math.max(0, arenaEnemyHealth - arenaDamage()); shots++;
      arenaAttackClock = (enhancerTime > 0 ? .8f : 1) / (1 + equipmentBonus(3));
      if (arenaEnemyHealth == 0) { finishArena(1); return; }
    }
    if (arenaEnemyClock == 0) {
      float damage = (11 + arenaDay * .17f) * (arenaChampion == 2 ? 1.7f : arenaChampion == 3 ? 1.2f : 1);
      if (arenaChampion == 0 && (int)arenaElapsed % 4 < 2) damage *= 1.35f;
      if (arenaChampion == 3 && (int)arenaElapsed % 6 < 2) damage *= 1.4f;
      arenaHealth = Math.max(0, arenaHealth - damage * (1 - equipmentBonus(1)) * (relaxed ? .5f : 1));
      arenaEnemyClock = arenaChampion == 1 ? 1.05f : arenaChampion == 2 ? 2.4f : 1.6f;
    }
    if (arenaHealth == 0 || arenaElapsed >= 45) finishArena(2);
  }

  private void finishArena(int result) {
    if (!arenaActive) return;
    arenaActive = false; arenaResult = result;
    notice = result == 1 ? "Champion defeated. The rift offers no relic this time." : result == 3 ? "You withdrew. Your farm and crew are safe." : "The champion prevails. Your farm and crew are safe.";
    if (result == 1) rollEquipment(2);
  }

  public Game crossRift() {
    if (!worldComplete || arenaActive || arenaOfferDay > 0) return null;
    Game next = new Game(); next.world = Math.min(999, world + 1); next.revision = revision;
    System.arraycopy(collected, 0, next.collected, 0, collected.length);
    System.arraycopy(gear, 0, next.gear, 0, gear.length);
    System.arraycopy(equipped, 0, next.equipped, 0, equipped.length);
    next.lootState = lootState; next.randomState = randomState; next.weather = World.weather(next.world, 1, next.random(4));
    next.roamers.clear(); next.populateRoamers();
    next.tutorialDone = true; next.directorEnabled = directorEnabled;
    next.relaxed = relaxed; next.reducedMotion = reducedMotion;
    next.notice = "World " + next.world + " · " + next.worldName() + " · " + next.worldChapter();
    return next;
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
    return (FOE_HP[type] + (Campaign.day(day) - 1) * 6) * (type == Campaign.BOSS ? ashenWorld() ? 2.2f : 2 : 1);
  }

  public float enemySpeed(int type) { return FOE_SPEED[type] * (1 + Math.min(Campaign.day(day) - 1, 40) * .015f); }
  public float enemyDamage(int type) {
    float biome = ashfall() && Bestiary.heavy(type) ? 1.2f : ashenWorld() && type == Campaign.BOSS ? 1.1f : 1;
    return FOE_DAMAGE[type] * (1 + Math.min(day - 1, 30) * .06f + Math.max(0, Campaign.day(day) - 31) * .025f) * biome;
  }

  public boolean roamerActive(Roamer roamer) {
    return ((int)(elapsed / 6) + (int)(roamer.progress * 7) + roamer.type) % 4 != 0;
  }
  public float movementFactor(Enemy e) {
    // Each enemy shuffles on its own cadence; fast types surge while heavy types lumber.
    float wave = (float)Math.sin(elapsed * (Bestiary.fast(e.type) ? 5 : 2.2f) + e.offset * 9 + e.lane);
    return .8f + .2f * wave;
  }

  public static final class Roamer {
    public int type;
    public float progress, distance;
    public Roamer(int type, float progress, float distance) {
      this.type = type; this.progress = progress; this.distance = distance;
    }
  }

  private void patrol(float dt) {
    int count = mercenaries.size() + (hero == null ? 0 : 1);
    for (int index = 0; index < count; index++) {
      Mercenary merc = index < mercenaries.size() ? mercenaries.get(index) : hero;
      boolean champion = merc == hero;
      if (!night) merc.health = Math.min(champion ? heroMaxHealth() : guardMaxHealth(), merc.health + dt * (champion ? 2.5f : 1.5f));
      if (lane < 0) merc.progress = (merc.progress + dt * .14f) % 4;
      else {
        float destination = lane + (count == 1 ? .5f : .15f + .7f * index / (count - 1));
        float delta = destination - merc.progress;
        if (delta > 2) delta -= 4;
        if (delta < -2) delta += 4;
        merc.progress = (merc.progress + Math.max(-dt * .35f, Math.min(dt * .35f, delta)) + 4) % 4;
      }
      merc.shotClock = Math.max(0, merc.shotClock - dt);
      if (!night || merc.shotClock > 0) continue;
      float mx = guardX(merc.progress), my = guardY(merc.progress), best = Float.MAX_VALUE;
      Enemy chosen = null;
      for (Enemy e : enemies) {
        if (e.hp <= 0) continue;
        float dx = foeX(e) - mx, dy = foeY(e) - my, distance = dx * dx + dy * dy;
        if (distance > (champion ? .36f : guardRange()) * (champion ? .36f : guardRange())) continue;
        float score = distance + e.x * .05f - (e.breached ? 2 : 0) - (lane >= 0 && e.lane == lane ? .5f : 0);
        if (score < best) { chosen = e; best = score; }
      }
      if (chosen != null) {
        merc.targetX = foeX(chosen); merc.targetY = foeY(chosen);
        chosen.hp -= champion ? Math.round((32 + heroLevel * 8) * (enhancerTime > 0 ? 1.4f : 1)) * (1 + equipmentBonus(0)) : guardAttackDamage();
        shots++; merc.shotClock = champion ? (enhancerTime > 0 ? .72f : .9f) / (1 + equipmentBonus(3)) : guardInterval();
      }
    }
    removeDead();
  }

  public static final class Mercenary {
    public float progress, shotClock, targetX, targetY, health = 80;
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
