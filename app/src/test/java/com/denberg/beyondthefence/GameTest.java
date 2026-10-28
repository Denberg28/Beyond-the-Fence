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
  public void bulkPlantingRespectsBudgetUnlocksAndExistingCrops() {
    Game g = new Game();
    assertEquals(0, g.plantAll(1));
    assertEquals(0, g.plantAll(-1));
    assertEquals(80, g.coins);
    assertTrue(g.plant(0, 0));
    g.growth[0] = .5f;
    g.coins = 9;
    assertEquals(2, g.plantablePlots(0));
    assertEquals(2, g.plantAll(0));
    assertEquals(1, g.coins);
    assertEquals(.5f, g.growth[0], 0);
    assertEquals(-1, g.crop[3]);
    assertEquals(-1, g.crop[6]);
    assertEquals(0, g.plantAll(0));
    g.coins = 1000;
    g.land = 1;
    assertEquals(6, g.plantAll(0));
    assertEquals(976, g.coins);
    assertEquals(0, g.plantAll(0));
    g.defeated = true;
    g.crop[8] = -1;
    assertEquals(0, g.plantAll(0));
  }

  @Test
  public void harvestCountdownMatchesSimulationAndSurvivesSaveRoundTrip() throws Exception {
    Game g = new Game();
    g.plantAll(0);
    assertEquals(16, g.harvestSeconds(0));
    assertEquals(0, g.harvestSeconds(8));
    g.weather = 1;
    g.irrigation = 2;
    int remaining = g.harvestSeconds(0);
    assertTrue(remaining < 16);
    seconds(g, remaining);
    assertEquals(6, g.readyPlots());
    assertEquals(0, g.harvestSeconds(0));
    Game restored = SaveCodec.decode(SaveCodec.encode(g));
    assertEquals(6, restored.readyPlots());
    assertEquals(6, restored.harvestAll());
    assertEquals(0, restored.harvestAll());
    assertEquals(0, restored.readyPlots());
    assertEquals(128, restored.coins);
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
    g.day = 2;
    assertTrue(g.plant(0, 1));
    g.day = 3;
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
  public void firstNightIsWinnableThroughTheRealHarvestEconomy() {
    Game g = new Game(); assertTrue(g.buildWall(1)); assertTrue(g.buildWall(3));
    for (int t = 0; t < 750; t++) {
      g.harvestAll(); g.plantAll(0);
      if (!g.buildings[2] && g.coins >= 94) g.construct(2);
      if (g.coins >= g.hireCost() + 24) g.hire();
      g.tick(.1f);
    }
    assertEquals(2, g.mercenaries.size());
    for (int t = 0; t < 2000 && g.day == 1 && !g.defeated; t++) g.tick(.1f);
    assertFalse(g.defeated); assertEquals(2, g.day); assertEquals(10, g.kills);
    assertTrue(g.coins >= 4); assertTrue(g.lostCrops <= 6); // Slower daytime scouts can now be intercepted before destroying crops.
  }

  @Test
  public void defeatAndFreeRecoveryKeepInvestments() {
    Game g = new Game();
    g.plant(0, 0);
    g.weapon = 2; g.mercenaries.clear();
    g.health = .01f;
    g.night = true;
    Game.Enemy intruder = new Game.Enemy(2, 0, 0, 200, 200);
    intruder.breached = true; intruder.advance = 1; g.wallHealth[0] = 0;
    g.enemies.add(intruder);
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
    assertEquals(12, g.coins); // Recovery seed aid prevents an empty-wallet replanting lock.
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
    assertTrue(a.wallHealth[0] < 80);
    assertEquals((80 - a.wallHealth[0]) / 2, 80 - b.wallHealth[0], .001f);
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
  public void rallyMovesGuardsWithoutAnInstantShot() {
    Game g = new Game(); g.night = true;
    Game.Enemy foe = new Game.Enemy(0, 2, .5f, 25, 25); g.enemies.add(foe);
    assertFalse(g.rally(-1)); assertFalse(g.rally(4));
    assertTrue(g.rally(2)); assertEquals(25, foe.hp, 0);
    float before = g.mercenaries.get(0).progress;
    seconds(g, 10); assertTrue(g.mercenaries.get(0).progress > before);
    assertTrue(g.kills > 0); g.resumePatrol(); assertEquals(-1, g.lane);
  }

  @Test
  public void zeroCoinsCanRecoverWithoutWinningAFreeRaid() {
    Game g = new Game(); g.coins = 0; g.startNight(); seconds(g, 180);
    assertTrue(g.defeated); int day = g.day; g.recover();
    assertEquals(day, g.day); assertTrue(g.coins >= 4); assertTrue(g.plant(0, 0));
    assertEquals(1, g.mercenaries.size());
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
  public void repeatedSurvivalLossesKeepEconomyAndSavesRecoverable() throws Exception {
    Game g = new Game(); g.day = 7; g.mercenaries.clear();
    for (int attempt = 0; attempt < 20; attempt++) {
      g.coins = 0; g.startNight(); seconds(g, 180);
      assertTrue(g.defeated); assertEquals(7, g.day);
      g = SaveCodec.decode(SaveCodec.encode(g)); g.recover();
      assertFalse(g.defeated); assertTrue(g.coins >= 12);
      assertTrue(g.plant(0, 0)); assertTrue(g.enemies.isEmpty());
      assertTrue(g.mercenaries.size() <= 3);
      g = SaveCodec.decode(SaveCodec.encode(g));
    }
  }

  @Test
  public void everyZombieVariantAppears() {
    Game g = new Game();
    g.day = 8;
    g.fence = 4;
    g.health = 300;
    boolean[] seen = new boolean[Game.FOES.length];
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
