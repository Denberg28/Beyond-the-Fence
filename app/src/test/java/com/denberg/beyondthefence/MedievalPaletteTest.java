package com.denberg.beyondthefence;

import org.junit.Test;
import static org.junit.Assert.*;

/** Visual-only palette regression: world/gameplay rules and persistence remain independent. */
public class MedievalPaletteTest {
  @Test public void bothBiomesUseOpaqueDarkPaletteInEveryPhase() {
    for (int world : new int[]{1,2,99}) {
      int day = World.ground(world,false), night = World.ground(world,true);
      assertEquals(0xff000000,day & 0xff000000);
      assertEquals(0xff000000,night & 0xff000000);
      for (int color : new int[]{day,night}) {
        int r=(color>>16)&255,g=(color>>8)&255,b=color&255;
        assertTrue("dark readable backdrop",Math.max(r,Math.max(g,b))<90);
      }
      assertNotEquals(day,night);
    }
  }
  @Test public void visualPaletteDoesNotModifySaveFormatOrWorldEvents() throws Exception {
    Game game = new Game(); game.world = 2; game.day = 25;
    byte[] before=SaveCodec.encode(game);
    World.ground(game.world,false); World.ground(game.world,true);
    assertArrayEquals(before,SaveCodec.encode(game));
  }
}
