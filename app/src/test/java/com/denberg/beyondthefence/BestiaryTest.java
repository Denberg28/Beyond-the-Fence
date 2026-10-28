package com.denberg.beyondthefence;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.zip.CRC32;
import org.junit.Test;
import static org.junit.Assert.*;

public class BestiaryTest {
  @Test public void weightedRosterRespectsUnlocksAndNeverSelectsABoss() {
    for(int day=1;day<=100;day++) {
      boolean[] seen=new boolean[Game.FOES.length];
      for(int roll=0;roll<Bestiary.weight(day);roll++) {
        int type=Bestiary.pick(day,roll);assertTrue(type>=0&&type<=Bestiary.LAST);assertNotEquals(Campaign.BOSS,type);seen[type]=true;
      }
      for(int type=0;type<Campaign.enemyKinds(day);type++) {
        assertTrue(seen[type]); assertEquals(type, Bestiary.scout(day, type));
      }
      assertEquals(day>=2,seen[Bestiary.KOBOLD]);assertEquals(day>=3,seen[Bestiary.WOLF]);
      assertEquals(day>=6,seen[Bestiary.ORC]);assertEquals(day>=12,seen[Bestiary.HEXER]);
    }
  }
  @Test public void newScoutsBecomeLiveEnemiesAndSaveWithoutChangingTheirIds() throws Exception {
    Game g=new Game();g.day=20;g.roamers.clear();g.populateRoamers();
    boolean[] seen=new boolean[12];for(Game.Roamer r:g.roamers)seen[r.type]=true;
    for(int type=8;type<=11;type++)assertTrue(seen[type]);
    g=SaveCodec.decode(SaveCodec.encode(g));g.startNight();
    for(Game.Enemy e:g.enemies) {assertTrue(e.hp>0);assertEquals(e.hp,e.maxHp,0);}
    Game copy=SaveCodec.decode(SaveCodec.encode(g));
    for(int tick=0;tick<200;tick++){g.tick(.1f);copy.tick(.1f);}
    assertArrayEquals(SaveCodec.encode(g),SaveCodec.encode(copy));
  }
  @Test public void actualReinforcementsIncludeEveryFantasySpeciesAndKeepQuotaBounded() throws Exception {
    Game g=new Game();g.day=80;g.roamers.clear();g.mercenaries.clear();g.startNight();
    boolean[] seen=new boolean[12];
    for(int frame=0;frame<1000&&g.night;frame++) {
      g.tick(.1f);
      if(g.boss()!=null)g=SaveCodec.decode(SaveCodec.encode(g));
      for(Game.Enemy e:g.enemies){seen[e.type]=true;e.hp=0;}
      assertTrue(g.spawned<=g.quota());assertTrue(g.enemies.size()<=Campaign.MAX_ENEMIES);
    }
    for(int type=8;type<=11;type++)assertTrue("Missing "+Game.FOES[type],seen[type]);
    assertTrue(seen[Campaign.BOSS]);assertEquals(81,g.day);
  }
  @Test public void wolfIsFastOrcIsHeavyAndHexerActuallyStrikesAtRange() {
    Game g=new Game();g.day=12;
    assertTrue(g.enemySpeed(Bestiary.WOLF)>g.enemySpeed(1));assertTrue(g.enemySpeed(Bestiary.KOBOLD)>g.enemySpeed(0));
    assertTrue(g.enemySpeed(Bestiary.ORC)<g.enemySpeed(0));assertTrue(g.enemyDamage(Bestiary.ORC)>g.enemyDamage(Bestiary.WOLF));
    g.world=2;g.weather=2;float heavy=g.enemyDamage(Bestiary.ORC);g.weather=0;assertEquals(g.enemyDamage(Bestiary.ORC)*1.2f,heavy,.00001f);
    g.roamers.clear();g.mercenaries.clear();g.startNight();g.spawnClock=5;
    Game.Enemy hexer=new Game.Enemy(Bestiary.HEXER,0,.27f,70,70);g.enemies.add(hexer);
    float hp=g.wallHealth[0];for(int i=0;i<5;i++)g.tick(.1f);
    assertEquals(.26f,hexer.x,0);assertFalse(hexer.breached);assertTrue(g.wallHealth[0]<hp);
  }
  @Test public void releasedNineFixtureRetainsActiveRaidAssetsRelicsAndWorld() throws Exception {
    byte[] b=Base64.getMimeDecoder().decode(getClass().getResourceAsStream("/schema9.b64").readAllBytes());
    assertEquals(9,ByteBuffer.wrap(b).getInt(4));Game g=SaveCodec.decode(b);
    assertEquals(18,g.day);assertEquals(2,g.world);assertEquals(3456,g.coins);assertEquals(789,g.wood);
    assertEquals(137,g.health,0);assertEquals(62,g.wallHealth[0],0);assertEquals(111,g.wallHealth[1],0);
    assertEquals(.75f,g.growth[0],0);assertTrue(g.buildings[0]);assertEquals(2,g.gear[0]);assertEquals(3,g.equipped[2]);
    assertEquals(4,g.bolts);assertEquals(3,g.blasts);assertTrue(g.night);assertEquals(2,g.enemies.size());
    assertEquals(2,g.enemies.get(0).type);assertEquals(5,g.enemies.get(1).type);assertEquals(90,g.enemies.get(0).hp,0);
    byte[] migrated=SaveCodec.encode(g);assertEquals(10,ByteBuffer.wrap(migrated).getInt(4));
    Game copy=SaveCodec.decode(migrated);for(int i=0;i<100;i++){g.tick(.1f);copy.tick(.1f);}
    assertArrayEquals(SaveCodec.encode(g),SaveCodec.encode(copy));
  }
  @Test public void newIdsAreRejectedInLegacySchemasAndInvalidBossScoutsAreRejected() throws Exception {
    Game g=new Game();g.day=20;g.roamers.clear();g.roamers.add(new Game.Roamer(Bestiary.KOBOLD,.5f,.7f));
    byte[] bytes=SaveCodec.encode(g);ByteBuffer.wrap(bytes).putInt(4,9);CRC32 crc=new CRC32();crc.update(bytes,0,bytes.length-8);
    ByteBuffer.wrap(bytes).putLong(bytes.length-8,crc.getValue());reject(bytes);
    g.roamers.get(0).type=Campaign.BOSS;reject(SaveCodec.encode(g));
    g.roamers.get(0).type=Bestiary.LAST+1;reject(SaveCodec.encode(g));
    g.roamers.clear();g.startNight();g.enemies.add(new Game.Enemy(Bestiary.LAST+1,0,1,70,70));reject(SaveCodec.encode(g));
  }
  private void reject(byte[] b) throws Exception {try{SaveCodec.decode(b);fail("Invalid enemy state accepted");}catch(IOException expected){}}
}
