package com.denberg.beyondthefence;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.Test;
import static org.junit.Assert.*;

public class CampaignTest {
  @Test public void centurySchedulesExactlyTwentyBossesAndSavesThemWithoutReplay() throws Exception {
    int total = 0;
    for (int day = 1; day <= 100; day++) {
      Game g = new Game(); g.day = day; g.mercenaries.clear(); g.startNight();
      int seen = 0;
      for (int t=0;t<6000 && g.day==day;t++) {
        g.tick(.1f);
        if (g.boss() != null) {
          seen++; total++;
          g=SaveCodec.decode(SaveCodec.encode(g));
          assertTrue(g.bossSpawned); assertEquals(1,g.enemies.stream().filter(e->e.type==Campaign.BOSS).count());
        }
        for (Game.Enemy e:g.enemies) e.hp=0;
      }
      assertEquals(day+1,g.day); assertFalse(g.defeated);
      assertEquals(day%5==0 ? 1 : 0,seen);
    }
    assertEquals(20,total);
  }

  @Test public void centuryUnlocksRealUpgradesWithAtomicCostsAndBoundedStats() throws Exception {
    Game g=new Game(); assertEquals(5,g.upgradeCap(0)); assertEquals(4,g.wallRankCap());
    g.day=100; g.coins=g.wood=10_000_000;
    for(int kind=0;kind<3;kind++) g.construct(kind);
    for(int kind=0;kind<4;kind++) while(!g.maxed(kind)) assertTrue(g.upgrade(kind));
    for(int side=0;side<4;side++) while(!g.borderMaxed(side)) assertTrue(g.upgradeBorder(side));
    g.buyHero(); while(g.mercenaries.size()<3) assertTrue(g.hire());
    for(int kind=0;kind<3;kind++) while(!g.advancedMaxed(kind)) assertTrue(g.upgradeAdvanced(kind));
    assertEquals(20,g.weapon); assertEquals(20,g.fence); assertEquals(20,g.drill); assertEquals(20,g.heroLevel); assertEquals(10,g.millLevel);
    assertTrue(g.guardDamage()>100); assertEquals(3,g.millPeriod(),0); assertTrue(g.maxWallHealth(0)>1000);
    assertTrue(g.enemies.size()<=80); assertTrue(g.guardInterval()>0);
    int paid=g.coins; assertFalse(g.upgrade(0)); assertFalse(g.upgradeBorder(0)); assertEquals(paid,g.coins);
    Game copy=SaveCodec.decode(SaveCodec.encode(g)); assertEquals(g.guardDamage(),copy.guardDamage()); assertEquals(20,copy.wallRanks[0]);
    float damage=g.enemyDamage(2); g.day=9999; assertEquals(damage,g.enemyDamage(2),0); assertEquals(80,g.quota());
  }

  @Test public void dayContainsDormantScoutsAndNightHasDistinctGaitsAndNewEnemies() {
    Game g=new Game(); g.day=100;g.roamers.clear();g.populateRoamers();
    boolean moving=false,dormant=false;
    for(Game.Roamer r:g.roamers) { moving|=g.roamerActive(r);dormant|=!g.roamerActive(r); }
    assertTrue(moving&&dormant); assertEquals(7,Campaign.enemyKinds(100));
    Game.Enemy e=new Game.Enemy(0,0,1,100,100,.2f), other=new Game.Enemy(0,0,1,100,100,.8f);
    assertNotEquals(g.movementFactor(e),g.movementFactor(other),.001f);
    for(int type=0;type<8;type++) assertTrue(g.enemySpeed(type)>0&&g.enemyDamage(type)>0);
    assertTrue(g.enemySpeed(1)>g.enemySpeed(2));assertTrue(g.enemySpeed(6)>g.enemySpeed(5));
  }

  @Test public void attackPotionsTargetOneOrNearbyGroupAndNeverChargeWithoutTargets() {
    Game g=new Game();g.bolts=2;g.blasts=2;
    assertFalse(g.useItem(2));assertFalse(g.useItem(3));assertEquals(2,g.bolts);
    g.night=true;g.roamers.clear();g.spawnClock=6;
    Game.Enemy close=new Game.Enemy(0,0,.1f,200,200,.5f), near=new Game.Enemy(0,0,.2f,200,200,.55f), far=new Game.Enemy(0,2,.9f,200,200,.5f);
    g.enemies.add(close);g.enemies.add(near);g.enemies.add(far);
    assertTrue(g.useItem(2));assertEquals(102,close.hp,0);assertEquals(200,near.hp,0);assertEquals(1,g.bolts);
    assertTrue(g.useItem(3));assertEquals(40,close.hp,0);assertEquals(138,near.hp,0);assertEquals(200,far.hp,0);assertEquals(1,g.blasts);
    assertTrue(g.spellArea&&g.spellTime>0);
    g.enemies.clear(); assertFalse(g.useItem(3));assertEquals(1,g.blasts);
  }

  @Test public void stockEquipmentAndCastStateContinueDeterministically() throws Exception {
    Game g=new Game();g.coins=1000;g.wood=100;g.construct(1);
    for(int type=0;type<4;type++){assertTrue(g.buyItem(type));assertEquals(1,g.itemStock(type));}
    assertTrue(g.equipItem(0,3));assertTrue(g.equipItem(2,0));assertFalse(g.equipItem(-1,3));assertFalse(g.equipItem(0,4));
    Game copy=SaveCodec.decode(SaveCodec.encode(g));assertArrayEquals(g.equipped,copy.equipped);assertEquals(1,copy.blasts);
    for(int i=0;i<100;i++){g.tick(.1f);copy.tick(.1f);}assertArrayEquals(SaveCodec.encode(g),SaveCodec.encode(copy));
    g.bolts=10;try {SaveCodec.decode(SaveCodec.encode(g));fail();}catch(java.io.IOException expected){}
  }

  @Test public void releasedSchemaSevenRetainsActualFarmProgress() throws Exception {
    byte[] fixture=Base64.getMimeDecoder().decode(new String(getClass().getResourceAsStream("/schema7.b64").readAllBytes(),StandardCharsets.US_ASCII));
    Game g=SaveCodec.decode(fixture);assertEquals(45,g.day);assertEquals(4321,g.coins);assertEquals(876,g.wood);
    assertEquals(137,g.health,0);assertEquals(111,g.wallHealth[1],0);assertEquals(4,g.wallRanks[1]);assertEquals(75,g.mercenaries.get(0).health,0);
    assertEquals(3,g.elixirs);assertEquals(2,g.enhancers);assertEquals(200,g.hero.health,0);assertEquals(.75f,g.growth[0],0);
    assertEquals(0,g.bolts);assertEquals(0,g.blasts);assertArrayEquals(new int[]{0,1,2},g.equipped);
    assertEquals(4321,SaveCodec.decode(SaveCodec.encode(g)).coins);
  }
}
