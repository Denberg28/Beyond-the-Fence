package com.denberg.beyondthefence;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/** Static scenery continues beneath the transparent HUD and command padding. */
@android.annotation.SuppressLint("ViewConstructor")
final class TerrainBackdrop extends View {
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private boolean night;

  TerrainBackdrop(Context context) {
    super(context);
    setTag("terrain");
    setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
  }

  void phase(boolean value) {
    if (night != value) { night = value; invalidate(); }
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    float d = getResources().getDisplayMetrics().density;
    canvas.drawColor(night ? 0xff213e32 : 0xff4b793a);
    // Quiet low-contrast ground detail keeps native labels readable over the world.
    paint.setColor(night ? 0xff2a4637 : 0xff507c3e);
    for (int i = 0; i < 90; i++) {
      float x = (i * 137 + 17) * d % getWidth();
      float y = (i * 83 + 41) * d % getHeight();
      canvas.drawCircle(x, y, (2 + i % 3) * d, paint);
    }
    paint.setColor(night ? 0x204c5140 : 0x20a39b70);
    paint.setStrokeWidth(27 * d);
    canvas.drawLine(getWidth() * .5f, 0, getWidth() * .46f, getHeight(), paint);
  }
}
