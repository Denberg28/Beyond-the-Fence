package com.denberg.beyondthefence;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/** Original, code-native silhouettes; rarity has a text label as well as a color. */
final class EquipmentIcon extends Drawable {
  private static final int[] COLORS = {0xff737b71, 0xff427952, 0xff386b9c, 0xff875c9e, 0xffad7d29};
  private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final int slot, quality, size;
  EquipmentIcon(int slot, int quality, int size) { this.slot = slot; this.quality = quality; this.size = size; setBounds(0, 0, size, size); }
  @Override public void draw(Canvas canvas) {
    canvas.save(); canvas.translate(getBounds().left, getBounds().top); canvas.scale(getBounds().width() / 32f, getBounds().height() / 32f);
    p.setColor(quality < 0 ? 0xff79857b : COLORS[quality]);
    p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2.5f);
    switch (slot) {
      case 0: canvas.drawArc(5, 5, 27, 28, 180, 180, false, p); canvas.drawLine(5, 17, 5, 26, p); canvas.drawLine(27, 17, 27, 26, p); canvas.drawLine(8, 21, 24, 21, p); break;
      case 1: canvas.drawRect(8, 5, 19, 24, p); canvas.drawRoundRect(8, 21, 28, 28, 3, 3, p); break;
      case 2: canvas.drawRoundRect(8, 12, 24, 27, 4, 4, p); for (int i = 0; i < 4; i++) canvas.drawLine(9 + i * 5, 15, 9 + i * 5, 5, p); break;
      case 3: canvas.drawLine(7, 27, 25, 5, p); canvas.drawLine(8, 16, 18, 25, p); canvas.drawLine(22, 4, 28, 4, p); break;
      case 4: canvas.drawCircle(16, 19, 9, p); canvas.drawRect(12, 3, 20, 11, p); break;
      case 5: canvas.drawArc(5, 3, 27, 23, 0, 180, false, p); canvas.drawCircle(16, 25, 4, p); break;
      case 6: Path diamond = new Path(); diamond.moveTo(16, 3); diamond.lineTo(27, 16); diamond.lineTo(16, 29); diamond.lineTo(5, 16); diamond.close(); canvas.drawPath(diamond, p); canvas.drawCircle(16, 16, 3, p); break;
      case 7: canvas.drawRoundRect(3, 11, 29, 23, 2, 2, p); canvas.drawRect(12, 8, 21, 26, p); break;
      default: canvas.drawArc(3, 6, 29, 31, 190, 160, false, p); canvas.drawLine(4, 22, 28, 22, p); canvas.drawLine(9, 26, 23, 26, p); break;
    }
    canvas.restore();
  }
  @Override public int getIntrinsicWidth() { return size; }
  @Override public int getIntrinsicHeight() { return size; }
  @Override public void setAlpha(int alpha) { p.setAlpha(alpha); }
  @Override public void setColorFilter(ColorFilter filter) { p.setColorFilter(filter); }
  @SuppressWarnings("deprecation") @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
