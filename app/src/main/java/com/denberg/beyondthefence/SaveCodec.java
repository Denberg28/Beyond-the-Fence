package com.denberg.beyondthefence;

import java.io.*;
import java.util.zip.CRC32;

/** Versioned, bounded save format with corruption detection. CRC is not authentication. */
public final class SaveCodec {
  private static final int MAGIC = 0x42544653, VERSION = 1;
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
    if (in.readInt() != MAGIC || in.readInt() != VERSION)
      throw new IOException("Unsupported save format");
    Game g = new Game();
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
    integer(g.lane, 0, 2);
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
      integer(lane, 0, 2);
      number(x, 0, 1);
      number(max, 1, 200);
      number(hp, .001f, max);
      g.enemies.add(new Game.Enemy(t, lane, x, hp, max));
    }
    range(g.night || count == 0);
    range(!g.defeated || g.health == 0);
    range(in.readLong() == crc.getValue());
    range(in.available() == 0);
    g.notice = "Farm restored. Take your time; resume when ready.";
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
