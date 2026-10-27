package com.denberg.beyondthefence;

import static org.junit.Assert.*;
import java.util.Base64;
import org.junit.Test;

public class GuardDefenseTest {
  private void seconds(Game g, float n) { for (int i = 0; i < Math.ceil(n * 30); i++) g.tick(1f / 30); }

  @Test public void noGuardMeansNoDamageFromStructuresUpgradesOrRally() throws Exception {
    Game g = new Game(); g.mercenaries.clear(); g.weapon = 5;
    for (int side = 0; side < 4; side++) { g.walls[side] = 3; g.wallHealth[side] = g.maxWallHealth(side); }
    for (int b = 0; b < 3; b++) { g.buildings[b] = true; g.buildingHealth[b] = 100; }
    g.night = true; g.roamers.clear(); g.spawned = g.quota();
    Game.Enemy foe = new Game.Enemy(0, 0, 0, 200, 200); g.enemies.add(foe);
    assertTrue(g.rally(0)); assertEquals(200, foe.hp, 0);
    seconds(g, 5); assertEquals(200, foe.hp, 0); assertEquals(0, g.kills);
    assertTrue(g.wallHealth[0] < g.maxWallHealth(0));
    g.resumePatrol(); seconds(g, 2); assertEquals(200, foe.hp, 0);
    Game saved = SaveCodec.decode(SaveCodec.encode(g)); assertTrue(saved.mercenaries.isEmpty());
  }

  @Test public void starterGuardCannotProvideMapWideProtection() {
    Game g = new Game(); assertEquals(1, g.mercenaries.size()); assertFalse(g.buildings[2]);
    g.coins = 0; g.wood = 0; g.startNight(); seconds(g, 180);
    assertTrue(g.defeated); assertTrue(g.day == 1);
    g.recover(); assertTrue(g.coins >= 12); assertTrue(g.plant(0, 0));
  }

  @Test public void trainingImprovesActualGuardShotsAndCadence() {
    Game recruit = new Game(), trained = new Game(); assertTrue(trained.upgrade(0));
    for (Game g : new Game[] {recruit, trained}) {
      g.night = true; g.roamers.clear(); g.spawned = g.quota();
      g.enemies.add(new Game.Enemy(0, 0, .1f, 200, 200)); g.tick(.1f);
    }
    assertEquals(184, recruit.enemies.get(0).hp, 0); assertEquals(181, trained.enemies.get(0).hp, 0);
    assertTrue(trained.guardInterval() < recruit.guardInterval());
  }

  @Test public void materialUpgradesAndRepairsDriveRealHealthFractions() {
    Game g = new Game(); g.coins = 1000; g.wood = 100;
    assertEquals(0, g.wallRatio(1), 0);
    for (int material = 1; material <= 3; material++) {
      assertTrue(g.buildWall(1)); assertEquals(material, g.walls[1]);
      assertEquals(1, g.wallRatio(1), 0);
    }
    assertEquals("Wood", Game.FENCE_MATERIALS[1]); assertEquals("Stone", Game.FENCE_MATERIALS[2]);
    assertEquals("Steel", Game.FENCE_MATERIALS[3]); assertEquals(160, g.maxWallHealth(1), 0);
    g.wallHealth[1] = 80; assertEquals(.5f, g.wallRatio(1), 0);
    assertTrue(g.repairWall(1)); assertEquals(130f / 160, g.wallRatio(1), .0001f);
    g.wallHealth[1] = 0; assertEquals(0, g.wallRatio(1), 0);
  }

  @Test public void rallyMovementAndPatrolResumeSaveDeterministically() throws Exception {
    Game g = new Game(); float before = g.mercenaries.get(0).progress;
    assertTrue(g.rally(3)); seconds(g, 1);
    assertTrue(Math.abs(g.mercenaries.get(0).progress - before) > .1f);
    Game copy = SaveCodec.decode(SaveCodec.encode(g)); seconds(g, 15); seconds(copy, 15);
    assertArrayEquals(SaveCodec.encode(g), SaveCodec.encode(copy));
    assertEquals(3.5f, g.mercenaries.get(0).progress, .001f);
    g.resumePatrol(); before = g.mercenaries.get(0).progress; seconds(g, .5f);
    assertTrue(g.mercenaries.get(0).progress > before); assertEquals(-1, g.lane);
  }

  @Test public void releasedSchema4PreservesDamageAndTrainingAndGrantsVolunteerOnlyOnce() throws Exception {
    Game g = SaveCodec.decode(Base64.getDecoder().decode("QlRGUwAAAAQAAAAAAAAAMwAAAAEAAAG+AAAAZAAAAAAAAAACAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQABGuNCyAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/////AAAAAP////8AAAAA/////wAAAAD/////AAAAAP////8AAAAA/////wAAAAD/////AAAAAP////8AAAAAAAAAAAAAAEsAAAABAAAAAAAAAAEAAAAAAQAAAAAAAAAAAAAAAAAEAAAAAD5rhR8/JmZmAAAAAEAs7ZE/QAAAAAAAAD+WRaA/WZmZAAAAAEBpWA4/JmZmQfAAAAAAAABCoAAAAAAAAEGQAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEIgAAAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAJ6NJTU="));
    assertEquals(51, g.revision); assertEquals(446, g.coins); assertEquals(75, g.wood);
    assertEquals(2, g.weapon); assertEquals(30, g.wallHealth[0], 0);
    assertEquals(18, g.cropHealth[0], 0); assertEquals(40, g.buildingHealth[0], 0);
    assertTrue(g.scorched[1]); assertEquals(1, g.lostCrops); assertFalse(g.buildings[2]);
    assertEquals(-1, g.lane); assertEquals(1, g.mercenaries.size());
    for (int i = 0; i < 3; i++) g = SaveCodec.decode(SaveCodec.encode(g));
    assertEquals(1, g.mercenaries.size()); assertEquals(446, g.coins);
  }
}
