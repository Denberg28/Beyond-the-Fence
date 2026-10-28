package com.denberg.beyondthefence;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import java.util.function.Supplier;

/** Ripple and gold target share the exact model window; reduced motion uses a static progress bar. */
@android.annotation.SuppressLint("ViewConstructor")
final class FishingView extends View {
  private final Fishing fishing;
  private final Supplier<Boolean> reduced;
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  FishingView(Context context, Fishing fishing, Supplier<Boolean> reduced) {
    super(context); this.fishing = fishing; this.reduced = reduced;
    setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
  }
  @Override protected void onDraw(Canvas c) {
    super.onDraw(c);
    float x = getWidth() / 2f, y = getHeight() / 2f, radius = Math.min(x, y) * .88f;
    paint.setStyle(Paint.Style.FILL); paint.setColor(0xff315f68); c.drawRoundRect(0, 0, getWidth(), getHeight(), 18, 18, paint);
    paint.setColor(0xff7bb6b8); c.drawOval(x - 8, y - 5, x + 8, y + 5, paint);
    paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(4);
    paint.setColor(0xfff0cd7b); c.drawCircle(x, y, radius * .35f, paint); c.drawCircle(x, y, radius * .85f, paint);
    if (!reduced.get() && fishing.state == Fishing.RIPPLE) {
      paint.setColor(fishing.catchWindow() ? 0xfff4e7b6 : 0xff8dcdd5);
      c.drawCircle(x, y, radius * fishing.progress(), paint);
    }
    if (reduced.get()) {
      paint.setStyle(Paint.Style.FILL); paint.setColor(0xff253f49); c.drawRect(12, getHeight() - 18, getWidth() - 12, getHeight() - 8, paint);
      paint.setColor(fishing.catchWindow() ? 0xfff0cd7b : 0xff8dcdd5);
      c.drawRect(12, getHeight() - 18, 12 + (getWidth() - 24) * fishing.progress(), getHeight() - 8, paint);
    }
  }
}
