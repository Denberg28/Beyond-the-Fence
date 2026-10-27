package com.denberg.beyondthefence;

import static org.junit.Assert.*;
import org.junit.Test;

public class BoardGeometryTest {
  @Test
  public void visibleTilesAreSeparateAndAtLeast48DpAcrossSizesAndFonts() {
    for (boolean wide : new boolean[] {false, true})
      for (float density : new float[] {1, 2.625f, 3})
        for (float font : new float[] {1, 1.3f, 2}) {
          int width = Math.round((wide ? 640 : 320) * density);
          int height = BoardGeometry.minimumHeight(density, font, wide);
          for (int extra : new int[] {0, Math.round(120 * density)}) {
            BoardGeometry g = new BoardGeometry();
            g.layout(width, height + extra, density, wide, font);
            assertTrue("Roaming terrain on both horizontal sides", g.fortLeft >= 56 * density);
            assertTrue("Roaming terrain above and below", g.fortTop >= 48 * density);
            assertTrue("Farm stays compact in tall windows", g.fortBottom - g.fortTop < (height + extra) * .82f);
            BoardGeometry.Box[] boxes = new BoardGeometry.Box[16];
            System.arraycopy(g.plots, 0, boxes, 0, 9);
            System.arraycopy(g.buildings, 0, boxes, 9, 3);
            System.arraycopy(g.walls, 0, boxes, 12, 4);
            for (int i = 0; i < boxes.length; i++) {
              BoardGeometry.Box b = boxes[i];
              assertTrue(b.left >= 0 && b.top >= 0 && b.right <= width && b.bottom <= height + extra);
              assertTrue("Small tile width", b.width() >= Math.floor(48 * density));
              assertTrue("Small tile height", b.height() >= Math.floor(48 * density));
              if (i < 12) assertTrue("Farm stays inside the fence", b.left >= g.fortLeft && b.right <= g.fortRight
                  && b.top >= g.fortTop && b.bottom <= g.fortBottom);
              for (int j = 0; j < i; j++) {
                BoardGeometry.Box a = boxes[j];
                assertFalse("Overlapping actions", a.left < b.right && b.left < a.right
                    && a.top < b.bottom && b.top < a.bottom);
              }
            }
          }
        }
  }
}
