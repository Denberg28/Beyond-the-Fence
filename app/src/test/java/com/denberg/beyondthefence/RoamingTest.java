package com.denberg.beyondthefence;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.Base64;
import org.junit.Test;

public class RoamingTest {
  private void seconds(Game g, float time) { for (int i = 0; i < Math.ceil(time * 30); i++) g.tick(1f / 30); }

  @Test public void preparationHasMovingZombiesWithoutDamageOrRewards() {
    Game g = new Game();
    assertEquals(4, g.roamers.size());
    float before = g.roamers.get(0).progress;
    seconds(g, 5);
    assertTrue(g.roamers.get(0).progress > before);
    assertEquals(100, g.health, 0); assertEquals(80, g.coins); assertEquals(0, g.kills);
    assertTrue(g.enemies.isEmpty()); assertFalse(g.night);
    for (Game.Roamer r : g.roamers) {
      float x = Game.ringX(r.progress, r.distance), y = Game.ringY(r.progress, r.distance);
      assertTrue(x < .18f || x > .82f || y < .18f || y > .82f);
    }
  }

  @Test public void raidsConvertRoamersAtTheirActualPositionsWithoutDuplicatingThem() {
    Game g = new Game(); seconds(g, 2);
    Game.Roamer r = g.roamers.get(0);
    float x = Game.ringX(r.progress, r.distance), y = Game.ringY(r.progress, r.distance);
    g.startNight();
    assertEquals(4, g.spawned); assertEquals(4, g.enemies.size()); assertTrue(g.roamers.isEmpty());
    assertEquals(x, Game.enemyX(g.enemies.get(0)), .00001f);
    assertEquals(y, Game.enemyY(g.enemies.get(0)), .00001f);
    g.startNight(); assertEquals(4, g.enemies.size());
    int spawned = g.spawned;
    g.shotClock = 1; seconds(g, 3); assertEquals(spawned, g.spawned);
    seconds(g, .3f); assertEquals(spawned + 1, g.spawned);
  }

  @Test public void laterDaysAttackMoreFrequentlyAndXpHasNoEffect() {
    Game low = new Game(), high = new Game(); high.day = 11;
    assertEquals(75, low.preparationSeconds(), 0); assertEquals(35, high.preparationSeconds(), 0);
    assertTrue(high.reinforcementSeconds() < low.reinforcementSeconds());
    seconds(low, 35.2f); seconds(high, 35.2f);
    assertFalse(low.night); assertTrue(high.night);
    assertTrue(high.spawned <= high.quota()); assertTrue(high.roamers.isEmpty());
    Game copy = new Game(); copy.xp = 10_000_000;
    assertEquals(low.preparationSeconds(), copy.preparationSeconds(), 0);
    assertEquals(low.reinforcementSeconds(), copy.reinforcementSeconds(), 0);
    assertEquals(1, copy.unlockedCrops());
    Game crowded = new Game(); crowded.day = 9999; crowded.tick(.1f);
    assertEquals(Game.MAX_ROAMERS, crowded.roamers.size());
    assertTrue(crowded.reinforcementSeconds() >= .6f);
  }

  @Test public void roamingCornersAreContinuousAndInWorldBounds() {
    for (int corner = 1; corner <= 4; corner++) {
      float before = corner - .00001f, after = corner == 4 ? 0 : corner;
      assertEquals(Game.ringX(before, .8f), Game.ringX(after, .8f), .0001f);
      assertEquals(Game.ringY(before, .8f), Game.ringY(after, .8f), .0001f);
    }
    for (int i = 0; i < 400; i++) {
      float p = i / 100f;
      assertTrue(Game.ringX(p, .85f) >= 0 && Game.ringX(p, .85f) <= 1);
      assertTrue(Game.ringY(p, .85f) >= 0 && Game.ringY(p, .85f) <= 1);
    }
  }

  @Test public void schema3ResumesRoamingAndRaidTimingExactlyAndRejectsInvalidState() throws Exception {
    Game g = new Game(); g.day = 3; seconds(g, 8);
    Game copy = SaveCodec.decode(SaveCodec.encode(g));
    seconds(g, 65); seconds(copy, 65);
    assertArrayEquals(SaveCodec.encode(g), SaveCodec.encode(copy));
    Game bad = new Game(); bad.roamers.get(0).progress = Float.NaN;
    try { SaveCodec.decode(SaveCodec.encode(bad)); fail(); } catch (IOException expected) { }
    bad = new Game(); bad.roamers.get(0).distance = 1.5f;
    try { SaveCodec.decode(SaveCodec.encode(bad)); fail(); } catch (IOException expected) { }
    bad = new Game(); bad.night = true;
    try { SaveCodec.decode(SaveCodec.encode(bad)); fail(); } catch (IOException expected) { }
  }

  @Test public void releasedSchema2FixtureKeepsFarmAndGainsDaytimeRoamers() throws Exception {
    Game g = SaveCodec.decode(Base64.getDecoder().decode("QlRGUwAAAAIAAAAAAAAAFwAAAAEAAAHdAAAAbgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQABGuNCyAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAj7MzM3/////AAAAAP////8AAAAA/////wAAAAD/////AAAAAP////8AAAAA/////wAAAAD/////AAAAAP////8AAAAAAAAAAAAAAEoAAAABAAAAAAAAAAEAAAAAAQABAAAAAAAAAAEAAAAAAAAAAD8AAAA/AAAAAAAAAH1vPaE="));
    assertEquals(23, g.revision); assertEquals(477, g.coins); assertEquals(74, g.wood);
    assertEquals(110, g.xp); assertEquals(2, g.crop[0]); assertEquals(.4f, g.growth[0], 0);
    assertTrue(g.buildings[0] && g.buildings[2]); assertEquals(1, g.mercenaries.size());
    assertEquals(4, g.roamers.size()); assertFalse(g.night);
    assertArrayEquals(SaveCodec.encode(g), SaveCodec.encode(SaveCodec.decode(SaveCodec.encode(g))));
  }
}
