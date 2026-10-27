package com.denberg.beyondthefence;

import org.junit.Test;
import static org.junit.Assert.*;

public class RewardTest {
  @Test public void everyVariantPaysOneCoinOnce() {
    for (int type = 0; type < 4; type++) {
      Game g = new Game(); g.night = true; g.roamers.clear(); g.spawnClock = 6;
      g.enemies.add(new Game.Enemy(type, 0, 1, 0, 100));
      g.tick(.1f);
      assertEquals(81, g.coins); assertEquals(1, g.kills); assertTrue(g.enemies.isEmpty());
      g.tick(.1f); assertEquals(81, g.coins); assertEquals(1, g.kills);
    }
  }

  @Test public void actualGuardKillPaysWithoutRewardingSurvivingEnemies() {
    Game g = new Game(); g.night = true; g.roamers.clear(); g.spawnClock = 6;
    g.enemies.add(new Game.Enemy(0, 0, .1f, 1, 100));
    g.enemies.add(new Game.Enemy(2, 2, 1, 200, 200));
    g.tick(.1f);
    assertEquals(81, g.coins); assertEquals(1, g.kills); assertEquals(1, g.enemies.size());
    for (int i = 0; i < 5; i++) g.tick(.1f);
    assertEquals(81, g.coins); assertEquals(1, g.kills);
  }

  @Test public void rewardSurvivesReloadWithoutReplayAndRespectsCoinCap() throws Exception {
    Game g = new Game(); g.night = true; g.roamers.clear(); g.spawnClock = 6;
    g.enemies.add(new Game.Enemy(0, 0, .1f, 1, 100)); g.tick(.1f);
    Game copy = SaveCodec.decode(SaveCodec.encode(g)); copy.tick(.1f);
    assertEquals(81, copy.coins); assertEquals(1, copy.kills);
    copy.coins = 10_000_000;
    copy.enemies.add(new Game.Enemy(0, 0, 1, 0, 100)); copy.tick(.1f);
    assertEquals(10_000_000, copy.coins); assertEquals(2, copy.kills);
  }
}
