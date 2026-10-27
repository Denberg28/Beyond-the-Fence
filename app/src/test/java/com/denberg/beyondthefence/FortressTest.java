package com.denberg.beyondthefence;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.Base64;
import org.junit.Test;

public class FortressTest {
  private void seconds(Game g, float n) { for (int i = 0; i < n * 30; i++) g.tick(1f / 30); }
  @Test public void wallConstructionDebitsBothResourcesAndCaps() {
    Game g = new Game();
    assertEquals(0, g.walls[1]);
    g.wood = 7;
    assertFalse(g.buildWall(1)); assertEquals(80, g.coins); assertEquals(7, g.wood);
    g.wood = 100; g.coins = 1000;
    assertTrue(g.buildWall(1)); assertEquals(980, g.coins); assertEquals(92, g.wood);
    assertTrue(g.buildWall(1)); assertTrue(g.buildWall(1));
    int coins = g.coins, wood = g.wood;
    assertFalse(g.buildWall(1)); assertEquals(coins, g.coins); assertEquals(wood, g.wood);
    assertFalse(g.buildWall(-1)); assertFalse(g.buildWall(4));
  }
  @Test public void cropTimberMillAndMarketCreateResourcesOnlyDuringTicks() {
    Game g = new Game(); g.coins = 1000; g.wood = 100;
    assertTrue(g.construct(0)); assertFalse(g.construct(0));
    int timber = g.wood;
    seconds(g, 11); assertEquals(timber, g.wood);
    seconds(g, 2); assertEquals(timber + 4, g.wood);
    assertTrue(g.construct(1));
    int coins = g.coins; timber = g.wood;
    assertTrue(g.plant(0, 0)); g.growth[0] = 1;
    assertTrue(g.harvest(0)); assertEquals(coins - 4 + 15, g.coins); assertEquals(timber + 2, g.wood);
    assertFalse(g.harvest(0));
  }
  @Test public void recruitingRequiresBarracksBudgetAndHasBoundedSquad() {
    Game g = new Game(); assertFalse(g.hire());
    g.coins = 10000; g.wood = 1000; assertTrue(g.construct(2));
    int coins = g.coins, price = g.hireCost(); assertTrue(g.hire()); assertEquals(coins - price, g.coins);
    for (int i = g.mercenaries.size(); i < Game.MAX_MERCS; i++) assertTrue(g.hire());
    coins = g.coins; int wood = g.wood;
    assertFalse(g.hire()); assertEquals(6, g.mercenaries.size());
    assertEquals(coins, g.coins); assertEquals(wood, g.wood);
  }
  @Test public void guardsPatrolAndOnlyShootTargetsInSpatialRange() {
    Game g = new Game(); g.mercenaries.clear(); g.buildings[2] = true; g.night = true; g.spawned = g.quota(); g.shotClock = 1;
    Game.Mercenary merc = new Game.Mercenary(.5f, 0, .5f, .5f); g.mercenaries.add(merc);
    Game.Enemy distant = new Game.Enemy(0, 2, 1, 100, 100, 0);
    Game.Enemy near = new Game.Enemy(0, 0, .05f, 9, 25);
    g.enemies.add(distant); g.enemies.add(near);
    g.tick(.1f);
    assertTrue(merc.progress > .5f); assertEquals(1, g.kills); assertFalse(g.enemies.contains(near));
    assertEquals(100, distant.hp, 0); assertTrue(merc.shotClock > 0);
    merc.progress = 3.999f; g.night = false; g.enemies.clear();
    g.tick(.1f); assertTrue(merc.progress >= 0 && merc.progress < 4);
  }
  @Test public void strongerWallsReduceDamageAndDefeatKeepsInvestments() {
    Game open = new Game(), stone = new Game();
    for (Game g : new Game[] {open, stone}) {
      g.mercenaries.clear(); g.night = true; g.spawned = g.quota(); g.shotClock = 1;
      g.enemies.add(new Game.Enemy(0, 1, 0, 100, 100));
    }
    open.walls[1] = 1; open.wallHealth[1] = open.maxWallHealth(1);
    stone.walls[1] = 3; stone.wallHealth[1] = stone.maxWallHealth(1);
    open.tick(.1f); stone.tick(.1f);
    assertTrue(80 - open.wallHealth[1] > (160 - stone.wallHealth[1]) * 1.9f);
    assertEquals(100, open.health, 0); assertEquals(100, stone.health, 0);
    stone.buildings[0] = true; stone.buildings[2] = true;
    stone.buildingHealth[0] = 100; stone.buildingHealth[2] = 100;
    stone.mercenaries.add(new Game.Mercenary(1, 0, .5f, .5f));
    stone.wood = 25; stone.defeated = true; stone.health = 0; stone.recover();
    assertEquals(3, stone.walls[1]); assertTrue(stone.buildings[0]);
    assertEquals(1, stone.mercenaries.size()); assertEquals(25, stone.wood);
  }
  @Test public void schema1FixtureMigratesWithoutLosingProgress() throws Exception {
    byte[] legacy = Base64.getDecoder().decode("QlRGUwAAAAEAAAAAAAAAKgAAAAQAAAD1AAAAaQAAAAEAAAACAAAAAQAAAAEAAAABAAAABwAAAAwAAAACAAAAAgABGuNDFgAAQUAAAD7MzM1AAAAAP4AAAAEAAAABAAAAAT8AAAD/////AAAAAP////8AAAAA/////wAAAAD/////AAAAAP////8AAAAA/////wAAAAD/////AAAAAP////8AAAAAAAAAAQAAAAAAAAACPszMzUFAAABByAAAAAAAAJmOWN8=");
    Game g = SaveCodec.decode(legacy);
    assertEquals(42, g.revision); assertEquals(245, g.coins); assertEquals(105, g.xp);
    assertEquals(2, g.weapon); assertEquals(1, g.crop[0]); assertEquals(.5f, g.growth[0], 0);
    assertEquals(1, g.enemies.size()); assertEquals(2, g.enemies.get(0).lane);
    assertEquals(.5f, g.enemies.get(0).offset, 0);
    for (int wall : g.walls) assertEquals(2, wall);
    assertEquals(24, g.wood); assertEquals(1, g.mercenaries.size());
    assertArrayEquals(SaveCodec.encode(g), SaveCodec.encode(SaveCodec.decode(SaveCodec.encode(g))));
  }
  @Test public void schema2PatrolProductionAndBattleResumeDeterministically() throws Exception {
    Game g = new Game(); g.coins = 2000; g.wood = 1000;
    g.construct(0); g.construct(1); g.construct(2); g.hire(); g.hire(); g.startNight();
    seconds(g, 5);
    Game restored = SaveCodec.decode(SaveCodec.encode(g));
    seconds(g, 15); seconds(restored, 15);
    assertArrayEquals(SaveCodec.encode(g), SaveCodec.encode(restored));
    restored.wood = -1;
    try { SaveCodec.decode(SaveCodec.encode(restored)); fail(); } catch (IOException expected) { }
    restored.wood = 1; restored.mercenaries.get(0).progress = Float.NaN;
    try { SaveCodec.decode(SaveCodec.encode(restored)); fail(); } catch (IOException expected) { }
  }
  @Test public void allFourBordersAreReachableAndFirstWaveHasNoEconomicSoftlock() {
    Game g = new Game(); boolean[] seen = new boolean[4]; g.day = 4; g.fence = 4; g.health = 300;
    for (int wave = 0; wave < 3; wave++) {
      g.startNight();
      for (int i = 0; i < 750; i++) {
        g.health = g.maxHealth(); g.tick(.1f);
        for (Game.Enemy e : g.enemies) { seen[e.lane] = true; assertTrue(Game.enemyX(e) >= 0 && Game.enemyX(e) <= 1); assertTrue(Game.enemyY(e) >= 0 && Game.enemyY(e) <= 1); }
      }
    }
    for (boolean side : seen) assertTrue(side);
    Game poor = new Game(); poor.coins = 0; poor.wood = 0; poor.startNight(); seconds(poor, 75);
    assertFalse(poor.defeated); assertTrue(poor.coins >= 4); assertTrue(poor.wood >= 6);
  }
}
