package com.denberg.beyondthefence;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Original, compact seed-card illustration; the native text also identifies every crop. */
final class CropIcon extends Drawable {
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF box = new RectF();
  private final int crop, size;

  CropIcon(int crop, int size) { this.crop = crop; this.size = size; }

  @Override
  public void draw(Canvas c) {
    c.save();
    c.translate(getBounds().left, getBounds().top);
    c.scale(getBounds().width() / 32f, getBounds().height() / 32f);
    paint.setColor(0xff427955);
    c.drawOval(7, 1, 16, 11, paint);
    c.drawOval(16, 0, 25, 10, paint);
    paint.setColor(Game.CROP_COLOR[crop]);
    box.set(crop == 1 ? 10 : 5, 8, crop == 1 ? 23 : 28, 30);
    c.drawRoundRect(box, crop == 1 ? 6 : 12, 12, paint);
    paint.setColor(crop == 0 ? 0xffad8aaa : crop == 1 ? 0xffb7903a : 0xffac642e);
    if (crop == 0) c.drawOval(8, 21, 25, 31, paint);
    else {
      paint.setStrokeWidth(2);
      c.drawLine(16, 11, 16, 28, paint);
      if (crop == 2) c.drawLine(22, 12, 23, 27, paint);
    }
    if (crop == 3) { paint.setColor(0xffb97837); c.drawLine(16, 20, 16, 31, paint); }
    if (crop == 4) { paint.setColor(0xffd5e5b2); c.drawOval(10, 12, 23, 26, paint); }
    if (crop == 5) { paint.setColor(0xffffe1a5); for (int i = 0; i < 4; i++) c.drawCircle(11 + i % 2 * 8, 15 + i / 2 * 8, 1, paint); }
    if (crop >= 6) { paint.setColor(0xfff2dda6); c.drawCircle(16, 17, 4, paint); }
    c.restore();
  }

  @Override public int getIntrinsicWidth() { return size; }
  @Override public int getIntrinsicHeight() { return size; }
  @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); invalidateSelf(); }
  @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
  @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
