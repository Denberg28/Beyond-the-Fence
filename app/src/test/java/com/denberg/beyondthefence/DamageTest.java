package com.denberg.beyondthefence;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.Base64;
import org.junit.Test;

public class DamageTest {
  private void seconds(Game g, float n) { for (int i = 0; i < Math.ceil(n * 30); i++) g.tick(1f / 30); }
  private Game fight() {
    Game g = new Game(); g.mercenaries.clear(); g.night = true; g.roamers.clear(); g.spawned = g.quota(); g.shotClock = 1;
    return g;
  }
  private Game.Enemy intruder(int target) {
    Game.Enemy e = new Game.Enemy(0, 0, 0, 200, 200);
    e.breached = true; e.advance = 1; e.target = target; return e;
  }

  @Test public void attacksDamageOnlyTheContactedFenceBeforeEntering() {
    Game g = fight(); g.plant(0, 0);
    Game.Enemy e = new Game.Enemy(2, 0, 0, 200, 200); g.enemies.add(e);
    g.tick(.1f);
    assertEquals(79, g.wallHealth[0], .001f); assertEquals(80, g.wallHealth[2], 0);
    assertEquals(100, g.health, 0); assertEquals(40, g.cropHealth[0], 0); assertFalse(e.breached);
    g.wallHealth[0] = .1f; g.tick(.1f);
    assertEquals(0, g.wallHealth[0], 0); assertTrue(e.breached);
    assertEquals(0, e.target); assertTrue(Game.enemyY(e) > .18f);
    assertEquals(40, g.cropHealth[0], 0);
  }

  @Test public void breachDestroysCropsWithoutHarvestOrDuplicateRewards() {
    Game g = fight(); g.plant(0, 0); g.cropHealth[0] = 1;
    int coins = g.coins, timber = g.wood, xp = g.xp;
    g.enemies.add(intruder(0)); g.enemies.add(intruder(0));
    g.tick(.1f);
    assertEquals(-1, g.crop[0]); assertTrue(g.scorched[0]); assertEquals(0, g.cropHealth[0], 0);
    assertEquals(1, g.lostCrops); assertEquals(0, g.harvested);
    assertEquals(coins, g.coins); assertEquals(timber, g.wood); assertEquals(xp, g.xp);
    assertFalse(g.harvest(0)); assertEquals(0, g.harvestAll());
    assertTrue(g.plant(0, 0)); assertFalse(g.scorched[0]); assertEquals(40, g.cropHealth[0], 0);
    assertEquals(coins - 4, g.coins);
  }

  @Test public void repairingBreachBlocksNewZombiesButKeepsExistingIntruders() {
    Game g = fight(); g.wallHealth[0] = 0;
    Game.Enemy inside = intruder(-1); inside.attackClock = 1;
    Game.Enemy outside = new Game.Enemy(0, 0, 0, 200, 200);
    g.enemies.add(inside); g.enemies.add(outside);
    int coins = g.coins, wood = g.wood;
    assertTrue(g.repairWall(0)); assertEquals(50, g.wallHealth[0], 0);
    assertEquals(coins - 8, g.coins); assertEquals(wood - 4, g.wood);
    g.tick(.1f); assertTrue(inside.breached); assertFalse(outside.breached);
    assertTrue(g.wallHealth[0] < 50);
    assertTrue(g.repairWall(0)); assertEquals(80, g.wallHealth[0], 0);
    coins = g.coins; assertFalse(g.repairWall(0)); assertEquals(coins, g.coins);
  }

  @Test public void fenceRepairAndMaterialPurchaseAreAtomicAndCapped() {
    Game g = new Game(); g.wallHealth[0] = 0; g.wood = 3;
    int coins = g.coins;
    assertFalse(g.repairWall(0)); assertEquals(coins, g.coins); assertEquals(3, g.wood);
    assertTrue(g.buyTimber()); assertEquals(coins - 12, g.coins); assertEquals(13, g.wood);
    assertTrue(g.repairWall(0));
    g.wood = 9_999_995; coins = g.coins;
    assertFalse(g.buyTimber()); assertEquals(coins, g.coins); assertEquals(9_999_995, g.wood);
    assertFalse(g.repairWall(-1)); assertFalse(g.repairWall(4)); assertFalse(g.repairWall(1));
    g.coins = 11; assertFalse(g.buyTimber()); assertEquals(11, g.coins);
  }

  @Test public void harvestFundsMaterialsFencesAndActualRecruitment() {
    Game g = new Game();
    for (int batch = 0; batch < 3; batch++) {
      assertEquals(6, g.plantAll(0));
      for (int i = 0; i < 6; i++) g.growth[i] = 1;
      assertEquals(6, g.harvestAll());
    }
    assertTrue(g.buyTimber()); assertTrue(g.buildWall(1)); assertTrue(g.construct(2)); assertTrue(g.hire());
    assertEquals(2, g.mercenaries.size()); assertEquals(42, g.coins); assertEquals(44, g.wood);
  }

  @Test public void destroyedBarracksStopRecruitmentButDoNotDeleteHiredGuards() throws Exception {
    Game g = fight(); g.coins = 500; g.wood = 100; assertTrue(g.construct(2)); assertTrue(g.hire());
    g.buildingHealth[2] = 1; g.enemies.add(intruder(11)); g.tick(.1f);
    assertFalse(g.buildings[2]); assertTrue(g.buildingRuins[2]); assertEquals(1, g.lostBuildings);
    assertEquals(1, g.mercenaries.size()); assertFalse(g.hire());
    Game saved = SaveCodec.decode(SaveCodec.encode(g)); assertEquals(1, saved.mercenaries.size());
    assertFalse(saved.buildings[2]); assertTrue(saved.buildingRuins[2]);
    assertTrue(g.construct(2)); assertFalse(g.buildingRuins[2]); assertEquals(100, g.buildingHealth[2], 0);
  }

  @Test public void destroyedProductionAndMarketStopBenefitsAndCanBeRebuilt() {
    Game g = fight(); g.coins = 1000; g.wood = 100; g.construct(0); g.construct(1);
    g.productionClock = 11; g.buildingHealth[0] = 1; g.buildingHealth[1] = 1;
    Game.Enemy e = intruder(9); g.enemies.add(e); g.tick(.1f);
    assertFalse(g.buildings[0]); assertEquals(0, g.productionClock, 0);
    e.target = 10; e.attackClock = 0; g.tick(.1f);
    assertFalse(g.buildings[1]); assertEquals(Game.SALE[0], g.sale(0));
    int wood = g.wood; seconds(g, 1); assertEquals(wood, g.wood);
    assertTrue(g.construct(0)); assertTrue(g.construct(1)); assertEquals(15, g.sale(0));
    g.buildingHealth[0] = 20; int coins = g.coins;
    assertTrue(g.repairBuilding(0)); assertEquals(80, g.buildingHealth[0], 0); assertEquals(coins - 10, g.coins);
  }

  @Test public void recoveryKeepsLossesAndProvidesEnoughSeedMoneyToRestart() {
    Game g = new Game(); g.health = 0; g.defeated = true; g.coins = 0; g.wood = 0;
    g.scorched[0] = true; g.buildingRuins[0] = true; g.wallHealth[0] = 0; g.lostCrops = g.lostBuildings = 1;
    g.recover(); assertFalse(g.defeated); assertEquals(100, g.health, 0); assertEquals(80, g.wallHealth[0], 0);
    assertEquals(12, g.coins); assertTrue(g.scorched[0]); assertTrue(g.buildingRuins[0]);
    assertFalse(g.buildings[0]); assertEquals(-1, g.crop[0]); assertTrue(g.plant(0, 0));
  }

  @Test public void schema4ResumesActiveBreachDamageAndRejectsInvalidAssetState() throws Exception {
    Game g = fight(); g.plant(0, 0); g.wallHealth[0] = .1f;
    g.enemies.add(new Game.Enemy(2, 0, 0, 200, 200)); g.tick(.1f);
    Game saved = SaveCodec.decode(SaveCodec.encode(g)); seconds(g, 5); seconds(saved, 5);
    assertArrayEquals(SaveCodec.encode(g), SaveCodec.encode(saved));
    Game bad = new Game(); bad.wallHealth[0] = Float.NaN; rejects(bad);
    bad = new Game(); bad.cropHealth[0] = 1; rejects(bad);
    bad = new Game(); bad.buildingHealth[0] = 1; rejects(bad);
    bad = fight(); Game.Enemy e = intruder(12); bad.enemies.add(e); rejects(bad);
    e.target = -1; e.x = .1f; rejects(bad);
  }
  private void rejects(Game g) throws Exception {
    try { SaveCodec.decode(SaveCodec.encode(g)); fail(); } catch (IOException expected) { }
  }

  @Test public void releasedSchema3MigratesWithIntactAssetsAndSameResources() throws Exception {
    Game g = SaveCodec.decode(Base64.getDecoder().decode("QlRGUwAAAAMAAAAAAAAAKgAAAAEAAAGfAAAAlgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQABGuNCyAAAQD//+QAAAAAAAAAAAAAAAAAAAAAAAAAAAT80FSL/////AAAAAP////8AAAAA/////wAAAAD/////AAAAAP////8AAAAA/////wAAAAD/////AAAAAP////8AAAAAAAAAAAAAAD4AAAABAAAAAAAAAAEAAAAAAQABQD//+QAAAAE+1wo9AAAAAD8AAAA/AAAAAAAABwAAAAA+uuEwPyZmZgAAAABANZFdP0AAAAAAAAA/p404P1mZmQAAAABAcfvaPyZmZgAAAABAEDEYP0AAAAAAAAA/OZmcP1mZmQAAAABATJuUPyZmZgAAAADY4bd5"));
    assertEquals(42, g.revision); assertEquals(415, g.coins); assertEquals(62, g.wood);
    assertEquals(7, g.roamers.size()); assertEquals(1, g.mercenaries.size());
    assertEquals(40, g.cropHealth[0], 0); assertEquals(100, g.buildingHealth[0], 0);
    assertEquals(100, g.buildingHealth[2], 0); assertEquals(80, g.wallHealth[0], 0);
    assertEquals(0, g.lostCrops); assertEquals(0, g.lostBuildings);
    assertArrayEquals(SaveCodec.encode(g), SaveCodec.encode(SaveCodec.decode(SaveCodec.encode(g))));
  }
}
