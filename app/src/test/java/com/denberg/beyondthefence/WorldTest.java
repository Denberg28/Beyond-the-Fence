package com.denberg.beyondthefence;

import org.junit.Test;
import static org.junit.Assert.*;

public class WorldTest {
  private Game ashen(int day) {
    Game g = new Game(); g.world = 2; g.day = day;
    g.weather = World.weather(g.world, day, 0); g.roamers.clear(); g.populateRoamers(); return g;
  }

  @Test public void firstWorldKeepsItsExistingRulesAndAshenBiomesAreBounded() {
    Game g = new Game(); assertFalse(g.ashenWorld()); assertEquals("Greenhaven", g.worldName());
    for (int day = 1; day <= 100; day++) {
      g.day = day; assertEquals(Settlement.style(day), g.settlementStyle());
      assertEquals(Game.WEATHER[g.weather], g.weatherName());
      Game ash = ashen(day);
      assertEquals((Settlement.style(day) + 1) % 3, ash.settlementStyle());
      assertEquals(g.quota(), ash.quota()); assertEquals(g.preparationSeconds(), ash.preparationSeconds(), 0);
      assertEquals(g.reinforcementSeconds(), ash.reinforcementSeconds(), 0);
      if (day % 5 == 0) { assertTrue(ash.ashfall()); assertEquals("Ashfall", ash.weatherName()); }
      for (int rolled = 0; rolled < 4; rolled++) assertEquals(rolled, World.weather(1, day, rolled));
    }
    assertEquals("Ashen March", World.name(999)); // Later worlds reuse this authored biome, not unbounded scaling.
  }

  @Test public void ashfallActuallySlowsApproachAndStrengthensOnlyHeavyFoes() {
    Game plain = new Game(), ash = ashen(3);
    for (Game g : new Game[] {plain, ash}) {
      g.day = 3; g.weather = 2; g.startNight(); g.enemies.clear(); g.mercenaries.clear();
      g.spawnClock = 5; g.enemies.add(new Game.Enemy(2, 0, 1, 125, 125)); g.tick(.1f);
    }
    assertEquals((1 - plain.enemies.get(0).x) * .85f, 1 - ash.enemies.get(0).x, .000001f);
    for (int type = 0; type < 7; type++)
      assertEquals(plain.enemyDamage(type) * (type == 2 || type == 5 ? 1.2f : 1), ash.enemyDamage(type), .00001f);
    ash.weather = 0; assertEquals(plain.enemyDamage(2), ash.enemyDamage(2), 0);
    assertEquals(plain.enemyDamage(Campaign.BOSS) * 1.1f, ash.enemyDamage(Campaign.BOSS), .00001f);
  }

  @Test public void realAshfallWallDamageHonorsArmorAndRelaxedMode() {
    for (boolean relaxed : new boolean[] {false, true}) {
      Game g = ashen(5); g.relaxed = relaxed; g.startNight(); g.enemies.clear(); g.mercenaries.clear();
      g.spawnClock = 5; g.enemies.add(new Game.Enemy(2, 0, 0, 125, 125));
      float before = g.wallHealth[0], expected = g.enemyDamage(2) * .1f * (relaxed ? .5f : 1);
      g.tick(.1f); assertEquals(expected, before - g.wallHealth[0], .00001f);
    }
  }

  @Test public void everyAshenBossSpawnsOnceAndRemainsWithinSchemaNineBounds() throws Exception {
    for (int day = 5; day <= 100; day += 5) {
      Game g = ashen(day); g.startNight(); g.enemies.clear(); g.spawned = g.quota() / 2; g.spawnClock = 0;
      g.tick(.1f); assertNotNull(g.boss()); assertEquals("Cinder Regent", g.bossName());
      assertTrue(g.boss().maxHp <= 2200); assertTrue(g.bossSpawned);
      Game copy = SaveCodec.decode(SaveCodec.encode(g));
      assertArrayEquals(SaveCodec.encode(g), SaveCodec.encode(copy));
      copy.spawnClock = 0; copy.tick(.1f);
      assertEquals(1, copy.enemies.stream().filter(e -> e.type == Campaign.BOSS).count());
    }
  }

  @Test public void riftCarriesRelicsPreferencesAndAssignmentsButResetsFarmOnce() throws Exception {
    Game g = new Game(); g.day = 100; g.worldComplete = true;
    g.collected[4] = true; g.equipGear(0, 4); g.equipItem(1, 3); g.relaxed = g.reducedMotion = true;
    g.directorEnabled = false; g.coins = 2000; g.wood = 500; g.revision = 81; g.elixirs = 3;
    Game next = g.crossRift(); assertNotNull(next); assertNull(next.crossRift());
    assertEquals("Ashen March", next.worldName()); assertEquals(1, next.day); assertEquals(2, next.world);
    assertEquals(4, next.gear[0]); assertEquals(3, next.equipped[1]); assertTrue(next.relaxed && next.reducedMotion);
    assertFalse(next.directorEnabled); assertEquals(81, next.revision);
    assertEquals(80, next.coins); assertEquals(24, next.wood); assertEquals(0, next.elixirs);
    assertFalse(next.buildings[0]); assertEquals(1, next.mercenaries.size());
    assertEquals(2000, g.coins); assertEquals(1, g.world); assertTrue(g.worldComplete);
    assertArrayEquals(SaveCodec.encode(next), SaveCodec.encode(SaveCodec.decode(SaveCodec.encode(next))));
  }

  @Test public void dawnAndRecoveryRetainBiomeAndFifthNightWeather() throws Exception {
    Game g = ashen(4); g.startNight(); g.spawned = g.quota(); g.enemies.clear(); g.tick(.1f);
    assertEquals(5, g.day); assertTrue(g.ashfall());
    g.startNight(); g.health = 0; g.defeated = true; g.recover();
    assertEquals(2, g.world); assertEquals(5, g.day); assertTrue(g.ashfall());
    Game copy = SaveCodec.decode(SaveCodec.encode(g));
    for (int frame = 0; frame < 300; frame++) { g.tick(.1f); copy.tick(.1f); }
    assertArrayEquals(SaveCodec.encode(g), SaveCodec.encode(copy));
  }

  @Test public void chapterNamesChangeAtAuthoredBoundaries() {
    Game g = ashen(1); assertEquals("I · The Black Pines", g.worldChapter());
    g.day = 26; assertEquals("II · Cinder Road", g.worldChapter());
    g.day = 51; assertEquals("III · The Silent Kilns", g.worldChapter());
    g.day = 76; assertEquals("IV · The Regent's Reach", g.worldChapter());
    assertNotEquals(World.ground(1, false), World.ground(2, false));
    assertNotEquals(World.ground(1, true), World.ground(2, true));
  }
}
