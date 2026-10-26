package com.denberg.beyondthefence;

import static org.junit.Assert.*;

import java.io.*;
import java.util.Arrays;
import java.util.Random;
import org.junit.Test;

public class GameTest {
  private static void seconds(Game g, float seconds) {
    for (int i = 0; i < Math.ceil(seconds * 30); i++) g.tick(1f / 30);
  }

  @Test
  public void completeFarmEconomy() {
    Game g = new Game();
    int initial = g.coins;
    assertTrue(g.plant(0, 0));
    assertEquals(initial - 4, g.coins);
    assertFalse(g.plant(0, 0));
    seconds(g, 17);
    assertTrue(g.harvest(0));
    assertEquals(initial + 8, g.coins);
    assertEquals(-1, g.crop[0]);
    assertFalse(g.harvest(0));
  }

  @Test
  public void cropUnlocksAndBounds() {
    Game g = new Game();
    assertFalse(g.plant(0, 1));
    assertFalse(g.plant(-1, 0));
    assertFalse(g.plant(6, 0));
    assertFalse(g.plant(0, 7));
    g.xp = 50;
    assertTrue(g.plant(0, 1));
    g.xp = 100;
    assertTrue(g.plant(1, 2));
  }

  @Test
  public void spendingIsAtomicAndCapped() {
    Game g = new Game();
    g.coins = 79;
    assertFalse(g.upgrade(0));
    assertEquals(0, g.weapon);
    assertEquals(79, g.coins);
    g.coins = 10000;
    for (int kind = 0; kind < 4; kind++) for (int n = 0; n < 10; n++) g.upgrade(kind);
    assertEquals(5, g.weapon);
    assertEquals(4, g.fence);
    assertEquals(3, g.irrigation);
    assertEquals(9, g.plots());
    int coins = g.coins;
    assertFalse(g.upgrade(0));
    assertEquals(coins, g.coins);
    assertFalse(g.upgrade(-1));
  }

  @Test
  public void weatherAndIrrigationMatter() {
    Game clear = new Game(), rain = new Game(), heat = new Game(), irrigated = new Game();
    for (Game g : new Game[] {clear, rain, heat, irrigated}) g.plant(0, 0);
    rain.weather = 1;
    heat.weather = 3;
    irrigated.irrigation = 1;
    seconds(clear, 5);
    seconds(rain, 5);
    seconds(heat, 5);
    seconds(irrigated, 5);
    assertTrue(rain.growth[0] > clear.growth[0]);
    assertTrue(heat.growth[0] < clear.growth[0]);
    assertTrue(irrigated.growth[0] > clear.growth[0]);
  }

  @Test
  public void firstNightIsWinnableWithoutSpending() {
    Game g = new Game();
    g.startNight();
    seconds(g, 75);
    assertFalse(g.defeated);
    assertFalse(g.night);
    assertEquals(2, g.day);
    assertTrue(g.kills >= 7);
    assertTrue(g.coins > 80);
  }

  @Test
  public void defeatAndFreeRecoveryKeepInvestments() {
    Game g = new Game();
    g.plant(0, 0);
    g.weapon = 2;
    g.health = .01f;
    g.night = true;
    g.enemies.add(new Game.Enemy(2, 0, 0, 200, 200));
    g.shotClock = 1;
    g.tick(.1f);
    assertTrue(g.defeated);
    float elapsed = g.elapsed;
    g.tick(.1f);
    assertEquals(elapsed, g.elapsed, 0);
    g.coins = 0;
    g.recover();
    assertFalse(g.defeated);
    assertEquals(g.maxHealth(), g.health, 0);
    assertEquals(0, g.crop[0]);
    assertEquals(2, g.weapon);
    assertEquals(0, g.coins);
  }

  @Test
  public void relaxedModeReducesDamage() {
    Game a = new Game(), b = new Game();
    for (Game g : new Game[] {a, b}) {
      g.night = true;
      g.shotClock = 1;
      g.enemies.add(new Game.Enemy(0, 0, 0, 100, 100));
    }
    b.relaxed = true;
    a.tick(.1f);
    b.tick(.1f);
    assertEquals((100 - a.health) / 2, 100 - b.health, .001f);
  }

  @Test
  public void pauseGapsCannotInflictCatchupDamage() {
    Game g = new Game();
    g.tick(Float.NaN);
    g.tick(-1);
    assertEquals(0, g.elapsed, 0);
    g.tick(900);
    assertEquals(.1f, g.elapsed, .0001f);
  }

  @Test
  public void targetAndFocusCooldown() {
    Game g = new Game();
    g.night = true;
    g.enemies.add(new Game.Enemy(0, 2, .5f, 25, 25));
    assertFalse(g.focus(-1));
    assertFalse(g.focus(1));
    assertTrue(g.focus(2));
    assertFalse(g.focus(2));
    seconds(g, 4);
    assertTrue(g.kills > 0);
  }

  @Test
  public void zeroCoinsStillRecoverEconomy() {
    Game g = new Game();
    g.coins = 0;
    g.startNight();
    seconds(g, 75);
    assertFalse(g.defeated);
    assertTrue(g.coins >= 4);
    assertTrue(g.plant(0, 0));
  }

  @Test
  public void saveRoundTripAndDeterministicContinuation() throws Exception {
    Game g = new Game();
    g.plant(0, 0);
    g.day = 4;
    g.startNight();
    seconds(g, 12);
    g.revision = 48;
    Game h = SaveCodec.decode(SaveCodec.encode(g));
    assertArrayEquals(SaveCodec.encode(g), SaveCodec.encode(h));
    for (int i = 0; i < 500; i++) {
      g.tick(1f / 30);
      h.tick(1f / 30);
    }
    assertArrayEquals(SaveCodec.encode(g), SaveCodec.encode(h));
  }

  @Test
  public void damagedTruncatedAndOversizeSavesRejected() throws Exception {
    byte[] good = SaveCodec.encode(new Game());
    for (int i = 0; i < good.length; i++) {
      byte[] bad = good.clone();
      bad[i] ^= 1;
      try {
        SaveCodec.decode(bad);
        fail("Mutation accepted at " + i);
      } catch (IOException expected) {
      }
    }
    for (int i = 0; i < good.length; i++) {
      try {
        SaveCodec.decode(Arrays.copyOf(good, i));
        fail("Truncated save accepted");
      } catch (IOException expected) {
      }
    }
    try {
      SaveCodec.readBounded(new ByteArrayInputStream(new byte[SaveCodec.MAX_BYTES + 1]));
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void invalidValuesRejectedEvenWithValidChecksum() throws Exception {
    Game g = new Game();
    g.coins = -1;
    try {
      SaveCodec.decode(SaveCodec.encode(g));
      fail();
    } catch (IOException expected) {
    }
    g = new Game();
    g.health = Float.NaN;
    try {
      SaveCodec.decode(SaveCodec.encode(g));
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void randomInputFuzzHasBoundedSaveRejection() {
    Random r = new Random(2026);
    for (int i = 0; i < 3000; i++) {
      byte[] bytes = new byte[r.nextInt(1000)];
      r.nextBytes(bytes);
      try {
        SaveCodec.decode(bytes);
        fail();
      } catch (IOException expected) {
      }
    }
  }

  @Test
  public void twentyDayProgressionRemainsSolvent() throws Exception {
    Game g = new Game();
    int steps = 0;
    while (g.day <= 20 && steps++ < 120000) {
      if (!g.night) {
        for (int p = 0; p < g.plots(); p++) {
          if (g.crop[p] < 0) g.plant(p, Math.min(2, g.unlockedCrops() - 1));
          else g.harvest(p);
        }
        if (g.health < g.maxHealth() * .7f) g.repair();
        for (int kind = 0; kind < 4; kind++)
          if (!g.maxed(kind) && g.coins >= g.cost(kind) + 36) g.upgrade(kind);
      }
      g.tick(.1f);
      if (steps % 100 == 0) SaveCodec.decode(SaveCodec.encode(g));
      assertFalse("Defeat on day " + g.day, g.defeated);
      assertTrue(g.coins >= 0);
      assertTrue(g.enemies.size() <= 45);
    }
    assertEquals(21, g.day);
    assertTrue(g.harvested > 100);
    assertTrue(g.level() > 5);
    assertTrue(g.weapon >= 3);
  }

  @Test
  public void everyZombieVariantAppears() {
    Game g = new Game();
    g.day = 8;
    g.fence = 4;
    g.health = 300;
    boolean[] seen = new boolean[4];
    for (int wave = 0; wave < 4; wave++) {
      g.startNight();
      for (int i = 0; i < 750; i++) {
        g.health = g.maxHealth();
        g.tick(.1f);
        for (Game.Enemy e : g.enemies) seen[e.type] = true;
      }
    }
    for (int i = 0; i < 4; i++) assertTrue("Missing " + Game.FOES[i], seen[i]);
  }
}
