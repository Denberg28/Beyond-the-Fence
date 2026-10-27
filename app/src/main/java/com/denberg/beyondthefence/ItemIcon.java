package com.denberg.beyondthefence;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Original bottle slots, drawn in code to stay crisp at any density. */
final class ItemIcon extends Drawable {
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final int type, size;
  ItemIcon(int type, int size) { this.type = type; this.size = size; }
  @Override public int getIntrinsicWidth() { return size; }
  @Override public int getIntrinsicHeight() { return size; }
  @Override public void draw(Canvas canvas) {
    Rect b = getBounds(); canvas.save(); canvas.translate(b.left, b.top); canvas.scale(b.width() / 32f, b.height() / 32f);
    paint.setColor(0xff2d3435); canvas.drawCircle(16, 17, 14, paint);
    paint.setColor(new int[] {0xffd26868, 0xffbb8bd6, 0xff68c1de, 0xffe0a45d}[type]); canvas.drawCircle(16, 19, 9, paint);
    paint.setColor(new int[] {0xfff1a899, 0xffe2bcf0, 0xffb7edf3, 0xfff4db96}[type]); canvas.drawCircle(13, 16, 3, paint);
    if (type >= 2) { paint.setColor(0xfff2eee1); paint.setStrokeWidth(2); canvas.drawLine(17,12,13,20,paint); canvas.drawLine(13,20,19,18,paint); if(type==3) canvas.drawCircle(16,19,6,paint); }
    paint.setColor(0xffa8b3a4); canvas.drawRect(12, 4, 20, 13, paint);
    paint.setColor(0xffdabf77); canvas.drawRect(11, 3, 21, 7, paint); canvas.restore();
  }
  @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); invalidateSelf(); }
  @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
  @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
