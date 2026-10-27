package com.denberg.beyondthefence;

import static org.junit.Assert.*;
import org.junit.Test;

public class SurvivalTest {
  private void seconds(Game g, float n) { for (int i = 0; i < n * 30; i++) g.tick(1f / 30); }
  private Game defense(int day, int material, int uniform) {
    Game g = new Game(); g.day = day; g.weapon = uniform; g.coins = 0;
    g.roamers.clear(); g.populateRoamers(); g.mercenaries.clear();
    for (int i = 0; i < 3; i++) {
      Game.Mercenary m = new Game.Mercenary(i * 1.3f, 0, .5f, .5f); m.health = g.guardMaxHealth(); g.mercenaries.add(m);
    }
    for (int i = 0; i < 4; i++) { g.walls[i] = material; g.wallHealth[i] = g.maxWallHealth(i); }
    g.startNight(); return g;
  }
  @Test public void releasedSixGuardSaveMigratesOnceWithRefundsAndUniformHealth() throws Exception {
    Game g = SaveCodec.decode(java.util.Base64.getDecoder().decode("QlRGUwAAAAUAAAAAAAAAAAAAAAMAAAH0AAAAAAAAAAAAAAADAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA/////wABGuNCyAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA/////wAAAAD/////AAAAAP////8AAAAA/////wAAAAD/////AAAAAP////8AAAAA/////wAAAAD/////AAAAAP////8AAAAAAAAAAAAAAFoAAAABAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAY/AAAAAAAAAD8AAAA/AAAAPwAAAAAAAAA/AAAAPwAAAD+AAAAAAAAAPwAAAD8AAAA/wAAAAAAAAD8AAAA/AAAAQAAAAAAAAAA/AAAAPwAAAEAgAAAAAAAAPwAAAD8AAAAAAAAEAAAAAD5rhR8/JmZmAAAAAEAs7ZE/QAAAAAAAAD+WRaA/WZmZAAAAAEBpWA4/JmZmQfgAAAAAAABCoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAJl3DTI="));
    assertEquals(3, g.mercenaries.size()); assertEquals(965, g.coins); assertEquals(108, g.wood);
    assertEquals(31, g.wallHealth[0], 0); assertEquals(3, g.weapon); assertEquals(3, g.day);
    for (Game.Mercenary merc : g.mercenaries) assertEquals(140, merc.health, 0);
    for (int i = 0; i < 3; i++) g = SaveCodec.decode(SaveCodec.encode(g));
    assertEquals(965, g.coins); assertEquals(108, g.wood); assertEquals(3, g.mercenaries.size());
  }
  @Test public void dayThreeDamagesWoodEvenWithMaximumCrewAndCanWipeOutTheFarm() {
    Game g = defense(3, 1, 0); seconds(g, 180);
    assertTrue(g.wallHealth[0] + g.wallHealth[1] + g.wallHealth[2] + g.wallHealth[3] < 320);
    assertTrue(g.defeated); assertTrue(g.kills < g.quota());
    assertEquals(0, g.health, 0); assertEquals(3, g.day);
  }
  @Test public void stoneAndUniformInvestmentCanSurviveTheSameDayThreeSeed() {
    Game g = defense(3, 2, 2); seconds(g, 80);
    assertFalse(g.defeated); assertEquals(4, g.day); assertEquals(16, g.kills);
    assertTrue(g.wallHealth[0] + g.wallHealth[1] + g.wallHealth[2] + g.wallHealth[3] < 480);
  }
  @Test public void laterHordesThreatenEvenMaxUniformsAndSteel() {
    Game g = defense(12, 3, 5); seconds(g, 180);
    assertTrue(g.defeated); assertEquals(12, g.day);
  }
  @Test public void raidsNeverDeleteSurvivorsAtTheOldSeventyFiveSecondTimeout() throws Exception {
    Game g = new Game(); g.mercenaries.clear(); g.night = true; g.roamers.clear();
    g.spawned = g.quota(); g.elapsed = 74.9f;
    g.enemies.add(new Game.Enemy(0, 0, 1, 400, 400));
    seconds(g, 1); assertTrue(g.night); assertEquals(1, g.day); assertEquals(1, g.enemies.size());
    g = SaveCodec.decode(SaveCodec.encode(g)); assertTrue(g.elapsed > 75); assertTrue(g.night);
  }
  @Test public void variantsHaveDifferentMovementAndDestructivePower() {
    Game g = new Game(); assertTrue(g.enemySpeed(1) > g.enemySpeed(0));
    assertTrue(g.enemySpeed(0) > g.enemySpeed(2)); assertTrue(g.enemySpeed(3) > g.enemySpeed(2));
    assertTrue(g.enemyDamage(2) > g.enemyDamage(3)); assertTrue(g.enemyDamage(3) > g.enemyDamage(0));
    assertTrue(g.enemyDamage(0) > g.enemyDamage(1));
    float speed = g.enemySpeed(1), damage = g.enemyDamage(2); g.day = 7;
    assertTrue(g.enemySpeed(1) > speed); assertTrue(g.enemyDamage(2) > damage);
  }
  @Test public void breachedZombiesKillMercenariesAndUniformArmorReducesInjury() throws Exception {
    Game[] games = {new Game(), new Game()}; games[1].weapon = 5;
    for (Game g : games) {
      g.night = true; g.roamers.clear(); g.spawned = g.quota();
      Game.Mercenary m = g.mercenaries.get(0); m.progress = .5f; m.shotClock = 1;
      Game.Enemy e = new Game.Enemy(2, 0, 0, 400, 400); e.breached = true; e.advance = 1;
      g.enemies.add(e); g.tick(.1f);
    }
    assertTrue(games[1].mercenaries.get(0).health > games[0].mercenaries.get(0).health);
    Game g = games[0]; g.mercenaries.get(0).health = 1;
    g.enemies.get(0).attackClock = 0; g.tick(.1f); assertTrue(g.mercenaries.isEmpty());
    assertTrue(SaveCodec.decode(SaveCodec.encode(g)).mercenaries.isEmpty());
    g.health = 0; g.defeated = true; g.recover(); assertEquals(1, g.mercenaries.size());
    assertEquals(g.guardMaxHealth(), g.mercenaries.get(0).health, 0);
  }
  @Test public void crewHealthRoundTripsAndHealsOnlyDuringPreparation() throws Exception {
    Game g = new Game(); g.mercenaries.get(0).health = 20;
    Game h = SaveCodec.decode(SaveCodec.encode(g)); seconds(g, 3); seconds(h, 3);
    assertArrayEquals(SaveCodec.encode(g), SaveCodec.encode(h)); assertTrue(h.mercenaries.get(0).health > 20);
    float hp = h.mercenaries.get(0).health; h.startNight(); h.tick(.1f);
    assertEquals(hp, h.mercenaries.get(0).health, 0);
  }
}
