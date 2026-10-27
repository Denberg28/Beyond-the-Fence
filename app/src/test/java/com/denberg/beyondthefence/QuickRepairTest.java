package com.denberg.beyondthefence;

import static org.junit.Assert.*;
import org.junit.Test;

public class QuickRepairTest {
  @Test public void eachBorderRepairsWithoutTimberAndNeverOverchargesIntactFences() throws Exception {
    Game g = new Game(); g.coins = 100; g.wood = 0;
    for (int i = 0; i < 4; i++) { g.walls[i] = 1; g.wallHealth[i] = 0; }
    for (int side = 0; side < 4; side++) {
      int before = g.coins; assertTrue(g.repairWall(side));
      assertEquals(before - 8, g.coins); assertEquals(50, g.wallHealth[side], 0); assertEquals(0, g.wood);
      for (int other = side + 1; other < 4; other++) assertEquals(0, g.wallHealth[other], 0);
      assertTrue(g.repairWall(side)); assertEquals(80, g.wallHealth[side], 0);
      before = g.coins; assertFalse(g.repairWall(side)); assertEquals(before, g.coins);
    }
    assertEquals(36, g.coins); assertEquals(0, g.wood);
    Game copy = SaveCodec.decode(SaveCodec.encode(g)); assertArrayEquals(g.wallHealth, copy.wallHealth, 0);
  }
  @Test public void insufficientCoinsGapDefeatAndInvalidBordersHaveNoDebit() {
    Game g = new Game(); g.wallHealth[0] = 0; g.coins = 7; g.wood = 100;
    assertFalse(g.repairWall(0)); assertEquals(0, g.wallHealth[0], 0);
    assertFalse(g.repairWall(1)); assertFalse(g.repairWall(-1)); assertFalse(g.repairWall(4));
    assertEquals(7, g.coins); assertEquals(100, g.wood);
    g.coins = 8; assertTrue(g.repairWall(0)); assertEquals(0, g.coins);
    g.defeated = true; assertFalse(g.repairWall(0));
  }
}
