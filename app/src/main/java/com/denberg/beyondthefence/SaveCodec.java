package com.denberg.beyondthefence;

import java.io.*;
import java.util.zip.CRC32;

/** Versioned, bounded save format with corruption detection. CRC is not authentication. */
public final class SaveCodec {
  private static final int MAGIC = 0x42544653, VERSION = 5;
  public static final int MAX_BYTES = 16384;

  private SaveCodec() {}

  public static byte[] encode(Game g) throws IOException {
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    DataOutputStream d = new DataOutputStream(body);
    d.writeInt(MAGIC);
    d.writeInt(VERSION);
    d.writeLong(g.revision);
    int[] values = {
      g.day,
      g.coins,
      g.xp,
      g.weather,
      g.weapon,
      g.fence,
      g.irrigation,
      g.land,
      g.kills,
      g.harvested,
      g.spawned,
      g.lane,
      g.randomState
    };
    for (int v : values) d.writeInt(v);
    for (float v : new float[] {g.health, g.elapsed, g.shotClock, g.spawnClock, g.focusClock})
      d.writeFloat(v);
    for (boolean b :
        new boolean[] {g.night, g.defeated, g.relaxed, g.reducedMotion, g.tutorialDone})
      d.writeBoolean(b);
    for (int i = 0; i < 9; i++) {
      d.writeInt(g.crop[i]);
      d.writeFloat(g.growth[i]);
    }
    d.writeInt(g.enemies.size());
    for (Game.Enemy e : g.enemies) {
      d.writeInt(e.type);
      d.writeInt(e.lane);
      d.writeFloat(e.x);
      d.writeFloat(e.hp);
      d.writeFloat(e.maxHp);
      d.writeFloat(e.offset);
    }
    d.writeInt(g.wood);
    for (int wall : g.walls) d.writeInt(wall);
    for (boolean built : g.buildings) d.writeBoolean(built);
    d.writeFloat(g.productionClock);
    d.writeInt(g.mercenaries.size());
    for (Game.Mercenary merc : g.mercenaries) {
      d.writeFloat(merc.progress); d.writeFloat(merc.shotClock);
      d.writeFloat(merc.targetX); d.writeFloat(merc.targetY);
    }
    d.writeInt(g.roamers.size());
    for (Game.Roamer roamer : g.roamers) {
      d.writeInt(roamer.type); d.writeFloat(roamer.progress); d.writeFloat(roamer.distance);
    }
    for (float hp : g.wallHealth) d.writeFloat(hp);
    for (int i = 0; i < 9; i++) { d.writeFloat(g.cropHealth[i]); d.writeBoolean(g.scorched[i]); }
    for (int i = 0; i < 3; i++) { d.writeFloat(g.buildingHealth[i]); d.writeBoolean(g.buildingRuins[i]); }
    d.writeInt(g.lostCrops); d.writeInt(g.lostBuildings);
    for (Game.Enemy enemy : g.enemies) {
      d.writeBoolean(enemy.breached); d.writeFloat(enemy.advance);
      d.writeFloat(enemy.attackClock); d.writeInt(enemy.target);
    }
    d.flush();
    CRC32 crc = new CRC32();
    crc.update(body.toByteArray());
    d.writeLong(crc.getValue());
    d.flush();
    return body.toByteArray();
  }

  public static Game decode(byte[] bytes) throws IOException {
    if (bytes.length < 16 || bytes.length > MAX_BYTES) throw new IOException("Invalid save size");
    CRC32 crc = new CRC32();
    crc.update(bytes, 0, bytes.length - 8);
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
    if (in.readInt() != MAGIC) throw new IOException("Unsupported save format");
    int version = in.readInt();
    if (version < 1 || version > VERSION) throw new IOException("Unsupported save format");
    Game g = new Game();
    g.mercenaries.clear();
    g.revision = in.readLong();
    g.day = in.readInt();
    g.coins = in.readInt();
    g.xp = in.readInt();
    g.weather = in.readInt();
    g.weapon = in.readInt();
    g.fence = in.readInt();
    g.irrigation = in.readInt();
    g.land = in.readInt();
    g.kills = in.readInt();
    g.harvested = in.readInt();
    g.spawned = in.readInt();
    g.lane = in.readInt();
    g.randomState = in.readInt();
    g.health = in.readFloat();
    g.elapsed = in.readFloat();
    g.shotClock = in.readFloat();
    g.spawnClock = in.readFloat();
    g.focusClock = in.readFloat();
    g.night = in.readBoolean();
    g.defeated = in.readBoolean();
    g.relaxed = in.readBoolean();
    g.reducedMotion = in.readBoolean();
    g.tutorialDone = in.readBoolean();
    range(g.revision >= 0);
    integer(g.day, 1, 9999);
    integer(g.coins, 0, 10_000_000);
    integer(g.xp, 0, 10_000_000);
    integer(g.weather, 0, 3);
    integer(g.weapon, 0, 5);
    integer(g.fence, 0, 4);
    integer(g.irrigation, 0, 3);
    integer(g.land, 0, 1);
    integer(g.kills, 0, Integer.MAX_VALUE);
    integer(g.harvested, 0, Integer.MAX_VALUE);
    integer(g.spawned, 0, g.quota());
    integer(g.lane, version >= 5 ? -1 : 0, version == 1 ? 2 : 3);
    number(g.health, 0, g.maxHealth());
    number(g.elapsed, 0, 75.2f);
    number(g.shotClock, -.2f, 1);
    number(g.spawnClock, -.2f, 7);
    number(g.focusClock, 0, 3);
    for (int i = 0; i < 9; i++) {
      g.crop[i] = in.readInt();
      g.growth[i] = in.readFloat();
      integer(g.crop[i], -1, 2);
      number(g.growth[i], 0, 1);
      if (i >= g.plots()) range(g.crop[i] == -1);
    }
    int count = in.readInt();
    integer(count, 0, 45);
    for (int i = 0; i < count; i++) {
      int t = in.readInt(), lane = in.readInt();
      float x = in.readFloat(), hp = in.readFloat(), max = in.readFloat();
      integer(t, 0, 3);
      integer(lane, 0, version == 1 ? 2 : 3);
      number(x, 0, 1);
      number(max, 1, 200);
      number(hp, .001f, max);
      float offset = version == 1 ? .5f : in.readFloat();
      number(offset, 0, 1);
      g.enemies.add(new Game.Enemy(t, lane, x, hp, max, offset));
    }
    if (version >= 2) {
      g.wood = in.readInt(); integer(g.wood, 0, 10_000_000);
      for (int i = 0; i < 4; i++) { g.walls[i] = in.readInt(); integer(g.walls[i], 0, 3); }
      for (int i = 0; i < 3; i++) g.buildings[i] = in.readBoolean();
      g.productionClock = in.readFloat(); number(g.productionClock, 0, 12);
      int mercs = in.readInt(); integer(mercs, 0, Game.MAX_MERCS);
      range(version >= 4 || mercs == 0 || g.buildings[2]);
      for (int i = 0; i < mercs; i++) {
        float progress = in.readFloat(), clock = in.readFloat(), tx = in.readFloat(), ty = in.readFloat();
        number(progress, 0, 3.9999998f); number(clock, 0, .9f);
        number(tx, 0, 1); number(ty, 0, 1);
        g.mercenaries.add(new Game.Mercenary(progress, clock, tx, ty));
      }
    } else {
      // Preserve existing investments; old farms arrive with a complete matching perimeter.
      java.util.Arrays.fill(g.walls, Math.min(3, g.fence + 1));
    }
    g.roamers.clear();
    if (version >= 3) {
      int roamers = in.readInt(); integer(roamers, 0, Game.MAX_ROAMERS);
      range(!g.night || roamers == 0);
      for (int i = 0; i < roamers; i++) {
        int type = in.readInt();
        float progress = in.readFloat(), distance = in.readFloat();
        integer(type, 0, 3); number(progress, 0, 3.9999998f); number(distance, .55f, .95f);
        g.roamers.add(new Game.Roamer(type, progress, distance));
      }
    } else {
      // Old raids keep their existing enemies and spawn timing; daytime gains roaming scouts.
      g.populateRoamers();
    }
    if (version >= 4) {
      for (int i = 0; i < 4; i++) { g.wallHealth[i] = in.readFloat(); number(g.wallHealth[i], 0, g.maxWallHealth(i)); }
      for (int i = 0; i < 9; i++) {
        g.cropHealth[i] = in.readFloat(); g.scorched[i] = in.readBoolean();
        number(g.cropHealth[i], 0, Game.CROP_HEALTH);
        range(g.crop[i] >= 0 ? g.cropHealth[i] > 0 && !g.scorched[i] : g.cropHealth[i] == 0);
        if (i >= g.plots()) range(!g.scorched[i]);
      }
      for (int i = 0; i < 3; i++) {
        g.buildingHealth[i] = in.readFloat(); g.buildingRuins[i] = in.readBoolean();
        number(g.buildingHealth[i], 0, Game.BUILDING_HEALTH);
        range(g.buildings[i] ? g.buildingHealth[i] > 0 && !g.buildingRuins[i] : g.buildingHealth[i] == 0);
      }
      g.lostCrops = in.readInt(); g.lostBuildings = in.readInt();
      integer(g.lostCrops, 0, 10_000_000); integer(g.lostBuildings, 0, 10_000_000);
      for (Game.Enemy enemy : g.enemies) {
        enemy.breached = in.readBoolean(); enemy.advance = in.readFloat();
        enemy.attackClock = in.readFloat(); enemy.target = in.readInt();
        number(enemy.advance, 0, 1); number(enemy.attackClock, 0, 1.2f); integer(enemy.target, -1, 11);
        range(enemy.breached ? enemy.x == 0 : enemy.advance == 0 && enemy.attackClock == 0 && enemy.target == -1);
      }
    } else {
      // Older releases had no independent damage. Preserve investments with intact assets.
      for (int i = 0; i < 4; i++) g.wallHealth[i] = g.maxWallHealth(i);
      for (int i = 0; i < 9; i++) g.cropHealth[i] = g.crop[i] < 0 ? 0 : Game.CROP_HEALTH;
      for (int i = 0; i < 3; i++) g.buildingHealth[i] = g.buildings[i] ? Game.BUILDING_HEALTH : 0;
    }
    range(g.night || count == 0);
    range(!g.defeated || g.health == 0);
    range(in.readLong() == crc.getValue());
    range(in.available() == 0);
    if (version < 5) {
      g.lane = -1; g.shotClock = 0; g.focusClock = 0;
      g.ensureVolunteer();
    }
    g.notice = version == 1 ? "Farm upgraded to fortress format. Existing crops and upgrades kept."
        : "Fortress restored. Resume when ready.";
    return g;
  }

  private static void integer(int n, int min, int max) throws IOException {
    range(n >= min && n <= max);
  }

  private static void number(float n, float min, float max) throws IOException {
    range(Float.isFinite(n) && n >= min && n <= max);
  }

  private static void range(boolean ok) throws IOException {
    if (!ok) throw new IOException("Damaged or invalid save");
  }

  public static byte[] readBounded(InputStream input) throws IOException {
    if (input == null) throw new IOException("No file selected");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] block = new byte[2048];
    int count;
    while ((count = input.read(block)) != -1) {
      if (out.size() + count > MAX_BYTES) throw new IOException("Save file too large");
      out.write(block, 0, count);
    }
    return out.toByteArray();
  }
}
