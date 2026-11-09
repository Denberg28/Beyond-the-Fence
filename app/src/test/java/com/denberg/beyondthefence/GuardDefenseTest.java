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
    assertEquals("Palisade", Game.FENCE_MATERIALS[1]); assertEquals("Stone wall", Game.FENCE_MATERIALS[2]);
    assertEquals("Ironbound", Game.FENCE_MATERIALS[3]); assertEquals(160, g.maxWallHealth(1), 0);
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

  private float gap(Game.Mercenary a, Game.Mercenary b) {
    float delta = Math.abs(a.progress - b.progress);
    return Math.min(delta, 4 - delta);
  }

  @Test public void hiringSpreadsCrewAndKeepsEconomyAndEnemyRandomness() throws Exception {
    Game g = new Game(); g.coins = 1000; g.wood = 100; g.buildings[2] = true; g.buildingHealth[2] = Game.BUILDING_HEALTH;
    int seed = g.randomState, loot = g.lootState;
    assertTrue(g.hire()); assertTrue(g.hire());
    assertEquals(3, g.mercenaries.size());
    assertEquals(815, g.coins); assertEquals(88, g.wood);
    assertEquals(seed, g.randomState); assertEquals(loot, g.lootState);
    for (int i = 0; i < 3; i++) for (int j = i + 1; j < 3; j++)
      assertTrue("Spread across perimeter", gap(g.mercenaries.get(i), g.mercenaries.get(j)) >= .85f);
    assertFalse(g.hire()); assertEquals(815, g.coins);
    Game copy = SaveCodec.decode(SaveCodec.encode(g));
    seconds(g, 18); seconds(copy, 18);
    assertArrayEquals("Save resumes identical patrols", SaveCodec.encode(g), SaveCodec.encode(copy));
  }

  @Test public void rallyThenPatrolRedistributesWithoutTeleporting() {
    Game g = new Game(); g.coins = 1000; g.wood = 100; g.buildings[2] = true; g.buildingHealth[2] = Game.BUILDING_HEALTH;
    assertTrue(g.hire()); assertTrue(g.hire());
    assertTrue(g.rally(1)); seconds(g, 14);
    for (Game.Mercenary m : g.mercenaries) assertTrue(m.progress >= 1 && m.progress < 2);
    g.resumePatrol();
    float before = g.mercenaries.get(1).progress;
    g.tick(.1f);
    float travel = Math.abs(g.mercenaries.get(1).progress - before);
    assertTrue("No teleport on resume", Math.min(travel, 4 - travel) <= .036f);
    seconds(g, 18);
    for (int i = 0; i < 3; i++) for (int j = i + 1; j < 3; j++)
      assertTrue("Return to sectors", gap(g.mercenaries.get(i), g.mercenaries.get(j)) >= .65f);
  }

  @Test public void heroDoesNotOverlapRecruitedSquad() {
    Game g = new Game(); g.coins = 1000; g.wood = 100; g.buildings[2] = true; g.buildingHealth[2] = Game.BUILDING_HEALTH;
    assertTrue(g.buyHero()); assertTrue(g.hire()); assertTrue(g.hire());
    for (Game.Mercenary m : g.mercenaries)
      assertTrue("Hero coverage", gap(g.hero, m) >= .45f);
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
