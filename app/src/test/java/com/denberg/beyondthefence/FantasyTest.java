package com.denberg.beyondthefence;

import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class FantasyTest {
  @Test public void releasedSchemaEightMigratesWithoutLosingMagicOrProgress() throws Exception {
    byte[] bytes = java.util.Base64.getMimeDecoder().decode(getClass().getResourceAsStream("/schema8.b64").readAllBytes());
    Game g = SaveCodec.decode(bytes);
    assertEquals(19, g.day); assertEquals(2345, g.coins); assertEquals(345, g.wood);
    assertEquals(4, g.bolts); assertEquals(2, g.blasts); assertEquals(3, g.equipped[0]);
    for (int code : g.gear) assertEquals(-1, code);
    assertEquals(1, g.world); assertEquals(0, g.arenaOfferDay); assertFalse(g.arenaActive);
    assertEquals(2345, SaveCodec.decode(SaveCodec.encode(g)).coins);
  }

  @Test public void relicsRequireOwnershipMatchSlotsAndCapCombinedPower() {
    Game g = new Game();
    assertFalse(g.equipGear(0, 4)); assertFalse(g.equipGear(-1, -1));
    Arrays.fill(g.collected, true); assertFalse(g.equipGear(0, 9));
    g.equipBest();
    for (int slot = 0; slot < 9; slot++) assertEquals(slot * 5 + 4, g.gear[slot]);
    assertEquals(.12f, g.equipmentBonus(0), .00001f); assertEquals(.12f, g.equipmentBonus(1), .00001f);
    assertEquals(.10f, g.equipmentBonus(2), .00001f); assertEquals(.06f, g.equipmentBonus(3), .00001f);
    assertTrue(g.guardDamage() > new Game().guardDamage());
    assertTrue(g.guardInterval() < new Game().guardInterval());
    assertTrue(g.equipGear(0, -1)); assertEquals(-1, g.gear[0]);
  }

  @Test public void lootIsScarceAndIndependentOfEnemyAndWeatherRandomness() throws Exception {
    int[] counts = new int[3]; int[] high = new int[3];
    for (int source = 0; source < 3; source++) {
      Game g = new Game(); int enemySeed = g.randomState;
      for (int i = 0; i < 100000; i++) {
        int code = g.rollEquipment(source);
        if (code >= 0) { counts[source]++; if (Equipment.rarity(code) >= 3) high[source]++; }
      }
      assertEquals(enemySeed, g.randomState);
      assertTrue(g.collected.length == 45); assertTrue(SaveCodec.encode(g).length < SaveCodec.MAX_BYTES);
    }
    assertTrue(counts[0] > 250 && counts[0] < 550);
    assertTrue(counts[1] > 11000 && counts[1] < 13000);
    assertTrue(counts[2] > 53000 && counts[2] < 57000);
    assertEquals(0, high[0]); assertTrue(high[2] > high[1]);
  }

  @Test public void commonGearHasRealFractionalCombatEffectAndLegacyEndlessFarmsReachTheRift() {
    Game plain = new Game(), geared = new Game();
    geared.collected[15] = true; geared.equipGear(3, 15);
    for (Game g : new Game[] {plain, geared}) {
      g.night = true; g.roamers.clear(); g.spawnClock = 5;
      g.enemies.add(new Game.Enemy(0, 0, .1f, 100, 100)); g.tick(.1f);
    }
    float baseDamage = 100 - plain.enemies.get(0).hp, relicDamage = 100 - geared.enemies.get(0).hp;
    assertEquals(baseDamage * 1.008f, relicDamage, .00001f);
    Game legacy = finishNight(123); assertTrue(legacy.worldComplete); assertEquals(100, legacy.day);
  }

  private Game finishNight(int day) {
    Game g = new Game(); g.day = day; g.startNight(); g.spawned = g.quota();
    g.enemies.clear(); g.tick(.1f); return g;
  }

  @Test public void arenasAppearOnlyAfterTenthNightAndDeclineCannotReplay() throws Exception {
    for (int day = 1; day <= 100; day++) {
      Game g = finishNight(day); assertEquals(day % 10 == 0 ? day : 0, g.arenaOfferDay);
      if (g.arenaOfferDay > 0) {
        float time = g.elapsed; int coins = g.coins; g.tick(.1f); assertEquals(time, g.elapsed, 0);
        assertTrue(g.declineArena()); assertFalse(g.declineArena()); assertFalse(g.acceptArena());
        Game copy = SaveCodec.decode(SaveCodec.encode(g)); assertEquals(day, copy.arenaDay);
        assertFalse(copy.acceptArena()); assertEquals(coins, copy.coins);
      }
    }
  }

  @Test public void arenaHasRandomChampionsActualCombatAndNoFarmDamageOrRewardsOnLoss() throws Exception {
    boolean[] champions = new boolean[4];
    for (int seed = 0; seed < 100; seed++) {
      Game g = finishNight(10); g.lootState = seed;
      assertTrue(g.acceptArena()); champions[g.arenaChampion] = true;
      assertFalse(g.acceptArena()); assertFalse(g.equipGear(0, -1));
      int day = g.day, coins = g.coins, kills = g.kills;
      float health = g.health, wall = g.wallHealth[0], growth = g.growth[0];
      for (int i = 0; i < 600 && g.arenaActive; i++) g.tick(.1f);
      assertFalse(g.arenaActive); assertEquals(2, g.arenaResult);
      assertEquals(day, g.day); assertEquals(coins, g.coins); assertEquals(kills, g.kills);
      assertEquals(health, g.health, 0); assertEquals(wall, g.wallHealth[0], 0); assertEquals(growth, g.growth[0], 0);
      for (boolean owned : g.collected) assertFalse(owned);
      assertFalse(SaveCodec.decode(SaveCodec.encode(g)).acceptArena());
    }
    for (boolean seen : champions) assertTrue(seen);
  }

  @Test public void arenaVictoryUsesAssignedMagicAndRewardIsOnceOnly() throws Exception {
    Game g = finishNight(10); g.weapon = 8; g.bolts = 3; g.elixirs = 1; g.acceptArena();
    assertTrue(g.equipItem(0, 2)); assertTrue(g.useItem(g.equipped[0])); assertEquals(2, g.bolts);
    assertFalse(g.useItem(0)); assertEquals(1, g.elixirs);
    assertTrue(g.arenaStrike()); assertFalse(g.arenaStrike());
    for (int i = 0; i < 600 && g.arenaActive; i++) { g.arenaStrike(); g.tick(.1f); }
    assertEquals(1, g.arenaResult); int rng = g.lootState;
    assertFalse(g.arenaStrike()); g.retreatArena(); assertEquals(rng, g.lootState);
    assertEquals(1, SaveCodec.decode(SaveCodec.encode(g)).arenaResult);
  }

  @Test public void arenaResumesIdenticallyAndRetreatDoesNotReroll() throws Exception {
    Game g = finishNight(20); g.acceptArena();
    for (int i = 0; i < 25; i++) g.tick(.1f);
    Game copy = SaveCodec.decode(SaveCodec.encode(g));
    for (int i = 0; i < 30; i++) { g.tick(.1f); copy.tick(.1f); }
    assertArrayEquals(SaveCodec.encode(g), SaveCodec.encode(copy));
    int rng = g.lootState; g.retreatArena(); assertEquals(3, g.arenaResult);
    assertFalse(g.acceptArena()); assertEquals(rng, g.lootState);
  }

  @Test public void directorChangesOnlyBoundedFuturePacingAndCanBeDisabled() {
    Game g = new Game(); g.day = 30;
    float base = g.reinforcementSeconds(), damage = g.enemyDamage(2); int quota = g.quota();
    g.raidStrain = .8f; g.evaluateDirector(); assertEquals(-1, g.directorPressure);
    assertEquals(base * 1.08f, g.reinforcementSeconds(), .0001f);
    for (int i = 0; i < 4; i++) g.evaluateDirector();
    assertEquals(1, g.directorPressure); assertEquals(base * .94f, g.reinforcementSeconds(), .0001f);
    assertEquals(damage, g.enemyDamage(2), 0); assertEquals(quota, g.quota());
    g.directorEnabled = false; assertEquals(base, g.reinforcementSeconds(), 0);
    g.day = 100; assertTrue(g.reinforcementSeconds() >= .6f);
  }

  @Test public void legendaryEquipmentDoesNotMakeUnattendedDefensesInvincible() {
    Game g = new Game(); g.day = 30; g.weapon = 5;
    Arrays.fill(g.collected, true); g.equipBest(); g.mercenaries.clear();
    for (int i = 0; i < 3; i++) {
      Game.Mercenary m = new Game.Mercenary(i * 1.3f, 0, .5f, .5f);
      m.health = g.guardMaxHealth(); g.mercenaries.add(m);
    }
    for (int side = 0; side < 4; side++) { g.walls[side] = 3; g.wallHealth[side] = g.maxWallHealth(side); }
    g.roamers.clear(); g.populateRoamers(); g.startNight();
    for (int i = 0; i < 5400; i++) g.tick(1f / 30);
    assertTrue(g.defeated); assertEquals(30, g.day);
  }

  @Test public void centuryPortalWaitsForArenaAndCarriesOnlyRelicsSettingsAndAssignments() throws Exception {
    Game g = finishNight(100); assertTrue(g.worldComplete); assertEquals(100, g.day);
    assertNull(g.crossRift()); g.declineArena(); g.collected[4] = true; g.equipGear(0, 4);
    g.equipItem(2, 3); g.revision = 120; g.coins = 8888; g.weapon = 10; g.bolts = 9;
    g = SaveCodec.decode(SaveCodec.encode(g)); g.startNight(); assertFalse(g.night);
    Game next = g.crossRift(); assertNotNull(next); assertEquals(2, next.world); assertEquals(1, next.day);
    assertEquals(80, next.coins); assertEquals(0, next.weapon); assertEquals(0, next.bolts);
    assertEquals(120, next.revision); assertEquals(4, next.gear[0]); assertEquals(3, next.equipped[2]);
    assertTrue(next.tutorialDone); assertFalse(next.worldComplete);
  }

  @Test public void schemaNineRejectsUnownedWrongSlotAndInvalidArenaState() throws Exception {
    Game g = new Game(); g.gear[0] = 4; rejects(g);
    g.collected[4] = true; g.gear[1] = 4; rejects(g);
    g.gear[1] = -1; g.arenaActive = true; rejects(g);
    g.arenaActive = false; g.raidStrain = Float.NaN; rejects(g);
    g.raidStrain = 0; g.arenaOfferDay = 15; rejects(g);
  }

  private void rejects(Game g) throws Exception {
    try { SaveCodec.decode(SaveCodec.encode(g)); fail("invalid save accepted"); } catch (IOException expected) { }
  }
}
