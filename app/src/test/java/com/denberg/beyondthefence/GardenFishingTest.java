package com.denberg.beyondthefence;

import static org.junit.Assert.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;
import org.junit.Test;

public class GardenFishingTest {
  private static void catchOne(Game g) {
    assertTrue(g.castLine());
    for (int i = 0; i < 65 && !g.fishing.catchWindow(); i++) g.fishing.tick(.1f);
    assertTrue(g.fishing.catchWindow()); assertTrue(g.reelFish());
  }
  @Test public void exactSelectionIsAtomicAndNeverReplacesCrops() {
    Game g = new Game(); g.coins = 8;
    assertEquals(0, g.plantSelected(0, new int[] {0, 1, 2})); assertEquals(8, g.coins);
    assertEquals(0, g.plantSelected(0, new int[] {0, 0}));
    assertEquals(0, g.plantSelected(0, new int[] {0, 7}));
    assertEquals(2, g.plantSelected(0, new int[] {1, 4})); assertEquals(0, g.coins);
    g.coins = 20; g.growth[1] = .5f;
    assertEquals(0, g.plantSelected(0, new int[] {0, 1})); assertEquals(20, g.coins);
    assertEquals(-1, g.crop[0]); assertEquals(.5f, g.growth[1], 0);
  }
  @Test public void seasonWeatherAndDayArePlantingGatesOnly() {
    Game g = new Game(); g.coins = 10000; assertEquals(1, g.unlockedCrops());
    for (int type = 0; type < Game.CROPS.length; type++) {
      g.day = Game.CROP_DAY[type]; assertEquals(type + 1, g.unlockedCrops());
      if (type > 0) { g.day--; assertFalse(g.canPlantCrop(type)); }
    }
    g.day = 41; g.weather = 2; assertTrue(g.plant(0, 6));
    g.day = 51; g.weather = 3; assertTrue(g.plant(1, 7));
    g.day = 61; g.weather = 1; assertTrue(g.plant(2, 8));
    g.weather = 0; assertFalse(g.plant(3, 8));
    g.day = 71; g.weather = 1; assertFalse(g.plant(3, 8));
    g.growth[0] = g.growth[1] = g.growth[2] = 1;
    assertEquals(3, g.harvestAll()); // Season changes never destroy or lock planted rare crops.
    for (int world : new int[] {1, 2, 99}) { g.world = world; g.day = 81; g.weather = 2; assertTrue(g.canPlantCrop(6)); }
  }
  @Test public void newCropsKeepBaselineIncomeAndTimberRatesBounded() {
    assertArrayEquals(new int[] {4, 10, 18}, Arrays.copyOf(Game.SEED, 3));
    assertArrayEquals(new int[] {12, 26, 44}, Arrays.copyOf(Game.SALE, 3));
    Game g = new Game();
    for (int type = 3; type < Game.CROPS.length; type++) {
      assertTrue((Game.SALE[type] - Game.SEED[type]) / Game.GROW[type] <= 26f / 43f + .00001f);
      assertTrue(g.cropTimber(type) / Game.GROW[type] <= 6f / 43f);
    }
  }
  @Test public void pondQuotaNeverUnlocksOrClosesFarmland() throws Exception {
    Game g = new Game(); g.coins = 1000;
    for (int i = 6; i < 9; i++) assertFalse(g.plant(i, 0));
    catchOne(g); catchOne(g); catchOne(g);
    assertEquals(6, g.plots()); assertEquals(6, SaveCodec.decode(SaveCodec.encode(g)).plots());
    assertTrue(g.upgrade(3)); assertEquals(9, g.farmCapacity());
    assertEquals(9, g.plantAll(0));
    Game saved = SaveCodec.decode(SaveCodec.encode(g));
    saved.night = true; saved.spawned = saved.quota(); saved.enemies.clear(); saved.tick(.1f);
    assertEquals(0, saved.fishingCatches); assertEquals(9, saved.plots());
    saved.growth[6] = 1; assertTrue(saved.harvest(6)); assertTrue(saved.plant(6, 0));
  }
  @Test public void everyAvailableCropCanUseEveryExpandedSoilBed() {
    for (int type = 0; type < Game.CROPS.length; type++) {
      Game g = new Game(); g.day = type == 6 ? 81 : type == 7 ? 91 : 61;
      g.weather = type == 6 ? 2 : type == 7 ? 3 : 1; g.land = 1; g.coins = 1000;
      assertEquals(9, g.plantAll(type));
      for (int crop : g.crop) assertEquals(type, crop);
    }
  }
  @Test public void releasedFloatingGardenMigratesToPermanentSoilWithoutLosingCrops() throws Exception {
    for (boolean planted : new boolean[] {false, true}) {
      Game g = new Game(); g.coins = 1000;
      if (planted) { g.crop[6] = 0; g.growth[6] = .5f; g.cropHealth[6] = Game.CROP_HEALTH; }
      else g.fishingCatches = 3;
      byte[] old = SaveCodec.encode(g); ByteBuffer.wrap(old).putInt(4, 11);
      CRC32 crc = new CRC32(); crc.update(old, 0, old.length - 8); ByteBuffer.wrap(old).putLong(old.length - 8, crc.getValue());
      Game restored = SaveCodec.decode(old);
      assertEquals(1, restored.land); assertEquals(9, restored.farmCapacity());
      assertEquals(g.coins, restored.coins); assertArrayEquals(g.crop, restored.crop);
      assertArrayEquals(g.growth, restored.growth, 0);
    }
  }
  @Test public void missedCancelledEarlyAndDuplicateReelsHaveNoReward() {
    Game g = new Game(); int coins = g.coins;
    assertFalse(g.reelFish()); assertTrue(g.castLine()); assertFalse(g.reelFish());
    assertTrue(g.castLine()); for (int i = 0; i < 100; i++) g.fishing.tick(.1f);
    assertEquals(Fishing.MISSED, g.fishing.state); assertFalse(g.reelFish());
    g.castLine(); g.fishing.cancel(); assertFalse(g.reelFish());
    assertEquals(0, g.fishingCatches); assertEquals(coins, g.coins);
    catchOne(g); coins = g.coins; assertFalse(g.reelFish()); assertEquals(coins, g.coins);
    g.night = true; assertFalse(g.castLine());
  }
  @Test public void rewardCapSurvivesReloadAndPracticeDoesNotAdvanceAnyRng() throws Exception {
    Game g = new Game(); int rng = g.randomState, loot = g.lootState, initial = g.coins;
    catchOne(g); catchOne(g); catchOne(g);
    assertTrue(g.coins >= initial + 18 && g.coins <= initial + 30);
    g = SaveCodec.decode(SaveCodec.encode(g)); int coins = g.coins, fishing = g.fishingState;
    for (int i = 0; i < 20; i++) catchOne(g);
    assertEquals(3, g.fishingCatches); assertEquals(coins, g.coins); assertEquals(fishing, g.fishingState);
    assertEquals(rng, g.randomState); assertEquals(loot, g.lootState);
    g.defeated = true; g.health = 0; g.recover(); assertEquals(3, g.fishingCatches);
  }
  @Test public void pondEquipmentIsScarceAndNeverHighTierOrAutomaticPower() {
    Game g = new Game(); g.day = 20; int finds = 0;
    for (int i = 0; i < 10000; i++) {
      g.fishingCatches = 0; g.lastDrop = -1; catchOne(g);
      if (g.lastDrop >= 0) { finds++; assertTrue(Equipment.rarity(g.lastDrop) <= 1); }
    }
    assertTrue(finds > 100 && finds < 300);
    for (int gear : g.gear) assertEquals(-1, gear);
  }
  @Test public void legacyTenKeepsAllNinePlantsWithoutResellingAndNewFieldsAreStrict() throws Exception {
    Game g = new Game(); g.land = 1; g.day = 3; g.coins = 1000; assertEquals(9, g.plantAll(2));
    byte[] old = Arrays.copyOf(SaveCodec.encode(g), SaveCodec.encode(g).length - 8);
    ByteBuffer.wrap(old).putInt(4, 10); CRC32 crc = new CRC32(); crc.update(old, 0, old.length - 8);
    ByteBuffer.wrap(old).putLong(old.length - 8, crc.getValue());
    Game restored = SaveCodec.decode(old); assertEquals(g.coins, restored.coins);
    assertArrayEquals(g.crop, restored.crop); assertEquals(0, restored.fishingCatches);
    restored.fishingCatches = 4; reject(SaveCodec.encode(restored));
    restored.fishingCatches = 0; restored.crop[0] = Game.CROPS.length; reject(SaveCodec.encode(restored));
  }
  @Test public void bottomSoilRowStaysLowerCentralInAllLayouts() {
    for (int style = 0; style < 3; style++) for (boolean wide : new boolean[] {false, true}) {
      BoardGeometry b = new BoardGeometry(); b.layout(wide ? 700 : 360, 600, 1, wide, 1, style);
      assertTrue(b.plots[6].left < b.plots[7].left && b.plots[7].left < b.plots[8].left);
      for (int i = 6; i < 9; i++) { assertEquals(b.plots[6].top, b.plots[i].top); assertTrue(b.plots[i].top > b.plots[3].top); }
    }
  }
  private static void reject(byte[] bytes) throws Exception {
    try { SaveCodec.decode(bytes); fail("Invalid save accepted"); } catch (IOException expected) { }
  }
}
