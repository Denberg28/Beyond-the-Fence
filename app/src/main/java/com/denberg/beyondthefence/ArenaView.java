package com.denberg.beyondthefence;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/** Small original champion stage; all timings derive from the saved battle simulation. */
@android.annotation.SuppressLint("ViewConstructor")
final class ArenaView extends View {
  long renderedFrames;
  private static final int[] ROBES = {0xff657783, 0xff6b705d, 0xff9c8778, 0xff856494};
  private final Game game;
  private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
  ArenaView(Context context, Game game) {
    super(context); this.game = game;
    setContentDescription("Champion arena. Battle health and controls follow below.");
    setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
  }
  @Override protected void onDraw(Canvas c) {
    super.onDraw(c); renderedFrames++; c.save(); c.scale(getWidth() / 300f, getHeight() / 148f);
    p.setColor(0xff1c2930); c.drawRoundRect(0, 0, 300, 148, 12, 12, p);
    p.setColor(0xff635963); c.drawOval(22, 104, 278, 140, p);
    fighter(c, 75, 62, false); fighter(c, 221, 55, true);
    bar(c, 22, game.arenaHealth / game.arenaMaxHealth(), 0xff86b978);
    bar(c, 174, game.arenaEnemyHealth / game.arenaMaxEnemyHealth(), 0xffd98375);
    c.restore();
  }
  private void bar(Canvas c, float x, float ratio, int color) {
    p.setColor(0xff111b22); c.drawRect(x, 15, x + 104, 23, p);
    p.setColor(color); c.drawRect(x, 15, x + 104 * Math.max(0, Math.min(1, ratio)), 23, p);
  }
  private void fighter(Canvas c, float x, float y, boolean zombie) {
    float bob = game.reducedMotion ? 0 : (float)Math.sin(game.arenaElapsed * 4) * 2;
    c.save(); c.translate(x, y + bob);
    float scale = zombie && game.arenaChampion == 2 ? 1.25f : 1; c.scale(scale, scale);
    p.setColor(zombie ? ROBES[game.arenaChampion] : 0xff57829a); c.drawRoundRect(-14, 16, 14, 48, 4, 4, p);
    p.setColor(zombie ? 0xffa3ae79 : 0xffd9b58b); c.drawCircle(0, 5, 12, p);
    p.setColor(zombie ? 0xffee7d74 : 0xff192b35); c.drawCircle(-4, 4, 2, p); c.drawCircle(5, 4, 2, p);
    p.setColor(0xff343b44); c.drawRect(-12, 45, -3, 63, p); c.drawRect(4, 45, 13, 63, p);
    p.setStrokeWidth(4); p.setColor(zombie && game.arenaChampion == 3 ? 0xffcb91de : 0xffddd4b0);
    c.drawLine(-18, 29, -28, zombie && game.arenaChampion == 1 ? 2 : 49, p);
    if (zombie && game.arenaChampion == 3) c.drawCircle(-28, 49, 6, p);
    if (zombie && game.arenaChampion == 0) { p.setColor(0xff9babb4); c.drawRect(-13, -6, 13, 0, p); }
    c.restore();
  }
}
