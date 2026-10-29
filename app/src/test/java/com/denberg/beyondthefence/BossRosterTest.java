package com.denberg.beyondthefence;

import org.junit.Test;
import static org.junit.Assert.*;

public class BossRosterTest {
  @Test public void processionCoversAllEightOnScheduledNightsAndShiftsWorlds() {
    for(int world=1;world<=8;world++) {
      boolean[] seen=new boolean[8];
      for(int day=5;day<=40;day+=5) {
        int kind=BossRoster.kind(day,world);assertFalse(seen[kind]);seen[kind]=true;
        assertFalse(BossRoster.name(kind).isEmpty());
        assertTrue(BossRoster.element(kind)>=0 && BossRoster.element(kind)<=2);
      }
    }
    assertEquals(BossRoster.DRAGON,BossRoster.kind(5,1));
    assertEquals(BossRoster.OGRE,BossRoster.kind(5,2));
    assertEquals(BossRoster.kind(5,1),BossRoster.kind(45,1));
  }
  @Test public void appearanceQueriesPreserveEverySavedByteAndCombatStats() throws Exception {
    for(int day=5;day<=100;day+=5) {
      Game g=new Game();g.day=day;g.world=2;g.startNight();
      g.spawned=g.quota()/2;g.spawnClock=0;g.tick(.1f);assertNotNull(g.boss());
      byte[] before=SaveCodec.encode(g);float hp=g.boss().maxHp,damage=g.enemyDamage(Campaign.BOSS);
      String title=g.bossTitle();for(int i=0;i<200;i++){BossRoster.effect(g,g.boss());g.bossTitle();}
      assertArrayEquals(before,SaveCodec.encode(g));assertEquals(hp,g.boss().maxHp,0);
      assertEquals(damage,g.enemyDamage(Campaign.BOSS),0);
      Game copy=SaveCodec.decode(before);assertEquals(title,copy.bossTitle());
      for(int i=0;i<100;i++){g.tick(.1f);copy.tick(.1f);}
      assertArrayEquals(SaveCodec.encode(g),SaveCodec.encode(copy));
    }
  }
  @Test public void effectsRequireLiveContactOrAnActualInteriorStrikeAndRespectReducedMotion() {
    Game g=new Game();g.day=5;g.night=true;g.elapsed=.1f;
    Game.Enemy e=new Game.Enemy(Campaign.BOSS,0,1,500,500);
    assertEquals(-1,BossRoster.effect(g,e),0);
    e.x=0;assertTrue(BossRoster.effect(g,e)>=0);
    g.elapsed=.6f;assertEquals(-1,BossRoster.effect(g,e),0);
    e.breached=true;e.advance=.8f;e.attackClock=1.1f;assertEquals(-1,BossRoster.effect(g,e),0);
    e.advance=1;assertTrue(BossRoster.effect(g,e)>=0);
    e.attackClock=.5f;assertEquals(-1,BossRoster.effect(g,e),0);
    e.attackClock=1.1f;g.reducedMotion=true;assertEquals(-1,BossRoster.effect(g,e),0);
    g.reducedMotion=false;e.hp=0;assertEquals(-1,BossRoster.effect(g,e),0);
    e.hp=500;g.night=false;assertEquals(-1,BossRoster.effect(g,e),0);
  }
}
