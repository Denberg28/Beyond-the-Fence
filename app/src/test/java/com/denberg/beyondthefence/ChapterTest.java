package com.denberg.beyondthefence;

import static org.junit.Assert.*;
import java.io.IOException;
import org.junit.Test;

public class ChapterTest {
  private void seconds(Game g, float n) { for (int i = 0; i < n * 30; i++) g.tick(1f/30); }
  private Game funded() { Game g = new Game(); g.coins = 10000; g.wood = 2000; return g; }
  @Test public void layoutsCycleAtFiveDayBoundariesWithoutResettingAssets() throws Exception {
    Game g = funded(); g.plant(0,0); g.day = 5; g.wallHealth[0] = 17;
    int coins = g.coins; g.night = true; g.roamers.clear(); g.spawned = g.quota();
    g.tick(.1f); assertEquals(6,g.day); assertEquals(1,g.settlementStyle());
    assertEquals(0,g.crop[0]); assertEquals(17,g.wallHealth[0],0); assertTrue(g.coins > coins);
    g = SaveCodec.decode(SaveCodec.encode(g)); assertEquals(1,g.settlementStyle());
    g.day = 10; assertEquals(1,g.settlementStyle()); g.day = 11; assertEquals(2,g.settlementStyle());
    g.day = 16; assertEquals(0,g.settlementStyle());
  }
  @Test public void sharedBoundariesAreContinuousBoundedAndActuallyDifferent() {
    for (int style = 0; style < 3; style++) for (float distance : new float[] {0,.85f,-.8f}) {
      for (int corner = 0; corner < 4; corner++) {
        float before = corner == 0 ? 3.99999f : corner - .00001f;
        assertEquals(Settlement.x(style,before,distance),Settlement.x(style,corner,distance),.0001f);
        assertEquals(Settlement.y(style,before,distance),Settlement.y(style,corner,distance),.0001f);
      }
      for (int i=0;i<400;i++) {
        float x=Settlement.x(style,i/100f,distance),y=Settlement.y(style,i/100f,distance);
        assertTrue(x>=0&&x<=1&&y>=0&&y<=1);
      }
    }
    assertNotEquals(Settlement.x(0,0,0),Settlement.x(1,0,0),.001f);
    assertNotEquals(Settlement.x(0,0,0),Settlement.x(2,0,0),.001f);
    Game g=new Game();g.day=11; Game.Enemy foe=new Game.Enemy(0,0,0,100,100,.5f);
    assertEquals(g.guardX(.5f),g.foeX(foe),0); assertEquals(g.guardY(.5f),g.foeY(foe),0);
  }
  @Test public void allTemplatesKeepDistinctAccessibleTilesInsideTheirActualBoundary() {
    for(int style=0;style<3;style++) for(boolean wide:new boolean[]{false,true}) for(float font:new float[]{1,2}) {
      BoardGeometry geometry=new BoardGeometry();int width=wide?640:320;
      geometry.layout(width,BoardGeometry.minimumHeight(1,font,wide),1,wide,font,style);
      BoardGeometry.Box[] boxes=new BoardGeometry.Box[12];System.arraycopy(geometry.plots,0,boxes,0,9);System.arraycopy(geometry.buildings,0,boxes,9,3);
      for(int i=0;i<boxes.length;i++) {
        BoardGeometry.Box b=boxes[i];assertTrue(b.width()>=48&&b.height()>=48);
        for(int px:new int[]{b.left,b.right}) for(int py:new int[]{b.top,b.bottom}) {
          float x=.18f+(px-geometry.fortLeft)*.64f/(geometry.fortRight-geometry.fortLeft);
          float y=.18f+(py-geometry.fortTop)*.64f/(geometry.fortBottom-geometry.fortTop);
          if(style==1)x=.5f+(x-.5f)/(.86f+.14f*y);
          assertTrue(x>=.18f&&x<=.82f&&y>=.18f&&y<=.82f);
          if(style==2) {assertTrue(x+y>=.4495f&&x+y<=1.5505f);assertTrue(Math.abs(x-y)<=.5505f);}
        }
        for(int j=0;j<i;j++){BoardGeometry.Box a=boxes[j];assertFalse(a.left<b.right&&b.left<a.right&&a.top<b.bottom&&b.top<a.bottom);}
      }
    }
  }
  @Test public void steelReinforcementsHaveAtomicCostsCapsAndDamageMitigation() throws Exception {
    Game g=funded();for(int i=0;i<3;i++)assertTrue(g.upgradeBorder(1));
    assertEquals(3,g.walls[1]);assertEquals(160,g.maxWallHealth(1),0);
    for(int rank=1;rank<=4;rank++){int c=g.coins,w=g.wood,price=g.borderCoinCost(1),timber=g.borderWoodCost(1);assertTrue(g.upgradeBorder(1));assertEquals(c-price,g.coins);assertEquals(w-timber,g.wood);assertEquals(rank,g.wallRanks[1]);}
    assertTrue(g.borderMaxed(1));assertFalse(g.upgradeBorder(1));assertEquals(280,g.maxWallHealth(1),0);
    Game h=SaveCodec.decode(SaveCodec.encode(g));assertEquals(4,h.wallRanks[1]);assertEquals(280,h.wallHealth[1],0);
    Game plain=funded();plain.walls[1]=3;plain.wallHealth[1]=160;
    for(Game game:new Game[]{g,plain}){game.mercenaries.clear();game.night=true;game.roamers.clear();game.spawned=game.quota();game.enemies.add(new Game.Enemy(2,1,0,400,400));game.tick(.1f);}
    assertTrue(280-g.wallHealth[1] < 160-plain.wallHealth[1]);
  }
  @Test public void advancedPurchasesImproveRealProductionAndCrewAndRemainBounded() {
    Game g=funded(); assertFalse(g.upgradeAdvanced(0));g.construct(0);g.construct(2);
    int c=g.coins,w=g.wood;assertTrue(g.upgradeAdvanced(0));assertEquals(c-90,g.coins);assertEquals(w-12,g.wood);
    w=g.wood;seconds(g,11.1f);assertEquals(w+6,g.wood);
    int damage=g.guardDamage();float hp=g.guardMaxHealth();assertTrue(g.upgradeAdvanced(1));assertEquals(damage+3,g.guardDamage());assertEquals(hp+10,g.guardMaxHealth(),0);
    for(int i=0;i<6;i++){g.upgradeAdvanced(0);g.upgradeAdvanced(1);}assertEquals(3,g.millLevel);assertEquals(3,g.drill);
    assertFalse(g.upgradeAdvanced(0));assertFalse(g.upgradeAdvanced(1));assertFalse(g.upgradeAdvanced(2));
  }
  @Test public void heroIsSeparateFromMercenaryCapFightsAndCanBeLost() throws Exception {
    Game g=funded();g.construct(2);g.hire();g.hire();assertEquals(3,g.mercenaries.size());
    int c=g.coins,w=g.wood;assertTrue(g.buyHero());assertEquals(c-360,g.coins);assertEquals(w-20,g.wood);assertFalse(g.buyHero());
    assertTrue(g.upgradeAdvanced(2));assertEquals(260,g.hero.health,0);g.mercenaries.clear();g.hero.progress=.5f;
    g.night=true;g.roamers.clear();g.spawned=g.quota();Game.Enemy e=new Game.Enemy(2,0,0,400,400);g.enemies.add(e);g.tick(.1f);
    assertEquals(360,e.hp,0);assertTrue(g.shots>0);g.hero.health=1;e.breached=true;e.advance=1;e.attackClock=0;g.tick(.1f);assertNull(g.hero);
    Game h=SaveCodec.decode(SaveCodec.encode(g));assertNull(h.hero);assertEquals(1,h.heroLevel);
  }
  @Test public void elixirsAndEnhancersHaveAtomicStocksUsefulEffectsAndNoWaste() throws Exception {
    Game g=funded();assertFalse(g.buyItem(0));g.construct(1);int c=g.coins;
    assertTrue(g.buyItem(0));assertEquals(c-35,g.coins);assertEquals(1,g.elixirs);assertFalse(g.useItem(0));assertEquals(1,g.elixirs);
    g.health=30;g.mercenaries.get(0).health=10;assertTrue(g.useItem(0));assertEquals(65,g.health,0);assertEquals(70,g.mercenaries.get(0).health,0);assertEquals(0,g.elixirs);
    assertTrue(g.buyItem(1));int damage=g.guardDamage();float interval=g.guardInterval();assertTrue(g.useItem(1));assertTrue(g.guardDamage()>damage);assertTrue(g.guardInterval()<interval);assertEquals(20,g.enhancerTime,0);assertFalse(g.useItem(1));
    Game copy=SaveCodec.decode(SaveCodec.encode(g));seconds(g,3);seconds(copy,3);assertArrayEquals(SaveCodec.encode(g),SaveCodec.encode(copy));
    seconds(g,18);assertEquals(0,g.enhancerTime,0);assertEquals(damage,g.guardDamage());
    for(int i=0;i<12;i++)g.buyItem(0);assertEquals(9,g.elixirs);c=g.coins;assertFalse(g.buyItem(0));assertEquals(c,g.coins);
  }
  @Test public void newSaveStateRejectsInvalidRanksStockHeroAndTimers() throws Exception {
    Game g=funded();g.wallRanks[0]=1;rejects(g);g.wallRanks[0]=0;
    g.elixirs=10;rejects(g);g.elixirs=0;g.enhancerTime=Float.NaN;rejects(g);g.enhancerTime=0;
    g.hero=new Game.Mercenary(0,0,.5f,.5f);g.hero.health=400;rejects(g);g.hero.health=1;
    assertNotNull(SaveCodec.decode(SaveCodec.encode(g)).hero);
  }
  @Test public void releasedSchema6KeepsDamageResourcesAndAddsEmptyNewState() throws Exception {
    Game g=SaveCodec.decode(java.util.Base64.getDecoder().decode("QlRGUwAAAAYAAAAAAAAAAAAAAAYAAAPjAAAAAAAAAAAAAAACAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA/////wABGuNCyAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD7MzM3/////AAAAAP////8AAAAA/////wAAAAD/////AAAAAP////8AAAAA/////wAAAAD/////AAAAAP////8AAAAAAAAAAAAAAH0AAAABAAAAAAAAAAEAAAAAAQAAQIAAAAAAAAE/AAAAAAAAAD8AAAA/AAAAAAAABAAAAAA+a4UfPyZmZgAAAABALO2RP0AAAAAAAAA/lkWgP1mZmQAAAABAaVgOPyZmZkG8AAAAAAAAQqAAAAAAAABCIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABCggAAAAAAAAAAAAAAAAAAAAAAAAAAAEIWAAAAAAAASkEoaw=="));
    assertEquals(6,g.day);assertEquals(995,g.coins);assertEquals(125,g.wood);assertEquals(23.5f,g.wallHealth[0],0);
    assertEquals(37.5f,g.mercenaries.get(0).health,0);assertEquals(65,g.buildingHealth[0],0);
    assertEquals(0,g.elixirs);assertEquals(0,g.drill);assertNull(g.hero);assertEquals(1,g.settlementStyle());
    assertArrayEquals(SaveCodec.encode(g),SaveCodec.encode(SaveCodec.decode(SaveCodec.encode(g))));
  }
  private void rejects(Game g)throws Exception{try{SaveCodec.decode(SaveCodec.encode(g));fail();}catch(IOException expected){}}
}
