package com.denberg.beyondthefence;

import org.junit.Test;
import static org.junit.Assert.*;

public class RaidTransitionTest {
  @Test public void duskIsContinuousAndResumesFromTheSavedPreparationClock() throws Exception {
    for(int day:new int[]{1,5,30,100}) {
      Game g=new Game();g.day=day;
      g.elapsed=g.preparationSeconds()-12;assertEquals(0,RaidPresentation.darkness(g),0);
      float last=0;
      for(int step=0;step<=24;step++) {
        g.elapsed=g.preparationSeconds()-12+step*.5f;
        float next=RaidPresentation.darkness(g);assertTrue(next>=last && next<=1);last=next;
        Game restored=SaveCodec.decode(SaveCodec.encode(g));
        assertEquals(next,RaidPresentation.darkness(restored),0);
      }
      assertEquals(1,last,0);g.startNight();assertEquals(last,RaidPresentation.darkness(g),0);
    }
  }
  @Test public void presentationNeverMutatesEconomyCombatRngOrCheckpoint() throws Exception {
    Game g=new Game();g.elapsed=g.preparationSeconds()-5;byte[] before=SaveCodec.encode(g);
    for(int i=0;i<300;i++) {RaidPresentation.darkness(g);RaidPresentation.label(g);RaidPresentation.banner(g);RaidPresentation.blend(0xff123456,0xff010203,.5f);}
    assertArrayEquals(before,SaveCodec.encode(g));
    g.arenaOfferDay=10;assertEquals(0,RaidPresentation.darkness(g),0);assertEquals("",RaidPresentation.banner(g));
    g.arenaOfferDay=0;g.worldComplete=true;assertEquals("Rift open",RaidPresentation.label(g));assertEquals("",RaidPresentation.banner(g));
  }
  @Test public void automaticNightPromotesScoutsOnceAndDawnAwardsExactlyOnce() {
    Game g=new Game();g.day=4;g.roamers.clear();g.populateRoamers();
    int count=g.roamers.size();float[] x=new float[count],y=new float[count];
    for(int i=0;i<count;i++) {Game.Roamer r=g.roamers.get(i);x[i]=g.ringXAt(r.progress,r.distance);y[i]=g.ringYAt(r.progress,r.distance);}
    g.elapsed=g.preparationSeconds()-.025f;g.tick(.05f);
    assertTrue(g.night);assertEquals(0,g.elapsed,0);assertEquals(count,g.spawned);assertEquals(count,g.enemies.size());
    for(int i=0;i<count;i++) {assertEquals(x[i],g.foeX(g.enemies.get(i)),.01f);assertEquals(y[i],g.foeY(g.enemies.get(i)),.01f);}
    g.startNight();assertEquals(count,g.enemies.size());
    int coins=g.coins,wood=g.wood;g.enemies.clear();g.spawned=g.quota();g.tick(.05f);
    assertFalse(g.night);assertEquals(5,g.day);assertEquals(0,g.elapsed,0);assertEquals(coins+32,g.coins);assertEquals(wood+6,g.wood);
    g.tick(.05f);assertEquals(coins+32,g.coins);assertEquals(5,g.day);
  }
  @Test public void phaseAudioRampsWithoutCutsCatchupOrStaleGains() {
    ScoreBlend blend=new ScoreBlend();
    for(int i=0;i<8;i++)blend.advance(0,.1f);
    assertEquals(1,blend.day,.0001f);assertEquals(0,blend.night,0);
    blend.advance(1,.1f);assertEquals(.875f,blend.day,.0001f);assertEquals(.125f,blend.night,.0001f);
    blend.advance(1,Float.NaN);assertEquals(.875f,blend.day,.0001f);
    blend.advance(1,900);assertEquals(.75f,blend.day,.0001f);
    for(int i=0;i<8;i++)blend.advance(1,.1f);
    assertEquals(0,blend.day,0);assertEquals(1,blend.night,0);
    blend.reset();blend.advance(1,0);assertEquals(0,blend.night,0);
  }
  @Test public void duskAndNightAnnounceOnceAndPauseDoesNotReplayShots() {
    Game g=new Game();AudioCues cues=new AudioCues();cues.poll(g,true,0);
    g.elapsed=g.preparationSeconds()-6;
    assertEquals(AudioCues.ZOMBIE,cues.poll(g,true,0));assertEquals(0,cues.poll(g,true,0));
    g.startNight();assertEquals(AudioCues.ZOMBIE,cues.poll(g,true,0));assertEquals(0,cues.poll(g,true,0));
    g.shots=30;cues.poll(g,false,100);assertEquals(0,cues.poll(g,true,0));
  }
}
