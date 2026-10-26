package com.denberg.beyondthefence;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;
import java.util.function.Supplier;

/**
 * Original board artwork. Native action tiles are laid over matching shared pixel bounds.
 */
@android.annotation.SuppressLint("ViewConstructor") // Programmatic view requires explicit model and pause suppliers.
final class FarmView extends View {
  private final Paint p = new Paint(3);
  private final LinearGradient daySky = new LinearGradient(0, 0, 0, 680, 0xffc8dfcf, 0xff97bb80, Shader.TileMode.CLAMP);
  private final LinearGradient nightSky = new LinearGradient(0, 0, 0, 680, 0xff162d3b, 0xff3c6553, Shader.TileMode.CLAMP);
  private static final int[] COATS = {0xff687b69, 0xffa4775b, 0xff60717b, 0xff9c8d5d};
  private static final Typeface LETTERING = Typeface.create("sans-serif-medium", Typeface.NORMAL);
  private final RectF rect = new RectF();
  private final Supplier<Game> model;
  private final Supplier<Boolean> paused;
  private final Path path = new Path();
  private final BoardGeometry geometry;

  FarmView(Context context, Supplier<Game> model, Supplier<Boolean> paused, BoardGeometry geometry) {
    super(context);
    this.model = model;
    this.paused = paused;
    this.geometry = geometry;
    setLayerType(View.LAYER_TYPE_HARDWARE, null);
  }

  private void color(int c) {
    p.setColor(c);
    p.setStyle(Paint.Style.FILL);
    p.setShader(null);
  }

  private void box(Canvas c, float x, float y, float w, float h, float r, int color) {
    color(color);
    rect.set(x, y, x + w, y + h);
    c.drawRoundRect(rect, r, r, p);
  }

  private void circle(Canvas c, float x, float y, float r, int color) {
    color(color);
    c.drawCircle(x, y, r, p);
  }

  private void line(Canvas c, float x, float y, float xx, float yy, int color, float width) {
    color(color);
    p.setStrokeWidth(width);
    p.setStrokeCap(Paint.Cap.ROUND);
    c.drawLine(x, y, xx, yy, p);
  }

  private void label(Canvas c, String s, float x, float y, int size, int color) {
    color(color);
    p.setTextSize(size * getResources().getDisplayMetrics().density);
    p.setTypeface(LETTERING);
    c.drawText(s, x, y, p);
  }

  @Override
  protected void onDraw(Canvas c) {
    super.onDraw(c);
    Game g = model.get();
    if (g == null || getHeight() == 0) return;
    float d = geometry.density;
    if (d == 0) return;
    boolean night = g.night;
    p.setAlpha(255); // Weather overlays must not carry translucency into the next sky frame.
    p.setShader(night ? nightSky : daySky);
    c.drawRect(0, 0, getWidth(), getHeight(), p);
    p.setShader(null);
    String cropLabel = "HOMESTEAD · TAP SOIL TO PLANT";
    String laneLabel = night ? "NIGHT WATCH · TAP A LANE" : "BEYOND THE FENCE";
    label(c, geometry.wide ? cropLabel : laneLabel, 10 * d,
        geometry.wide ? geometry.cropLabelY : geometry.laneLabelY, 10, night ? CREAM_COLOR : 0xff203e36);
    if (geometry.wide)
      label(c, laneLabel, geometry.lanes[0].left, geometry.laneLabelY, 10, night ? CREAM_COLOR : 0xff203e36);
    else label(c, cropLabel, 10 * d, geometry.cropLabelY, 10, night ? CREAM_COLOR : 0xff203e36);
    for (int i = 0; i < 3; i++) {
      BoardGeometry.Box lane = geometry.lanes[i];
      box(c, lane.left, lane.top, lane.width(), lane.height(), 12 * d,
          night ? 0xff3a5741 : 0xffafc888);
      for (int tile = 0; tile < 5; tile++)
        box(c, lane.left + tile * lane.width() / 5f, lane.top, lane.width() / 5f,
            lane.height(), 8 * d, tile % 2 == 0 ? 0x12526c3e : 0x127f995b);
      // A watch post marks the protected end of each horizontal approach lane.
      float x = lane.left + 16 * d, y = lane.top + lane.height() * .43f;
      circle(c, x, y, 10 * d, night ? 0xffd6b67f : 0xff54734e);
      line(c, x + 4 * d, y, x + 15 * d, y - 3 * d, 0xff2b4135, 4 * d);
      line(c, lane.left + 3 * d, lane.top + 6 * d, lane.left + 3 * d,
          lane.bottom - 6 * d, g.health < g.maxHealth() * .3f ? 0xffce8a5d : 0xffe4c797, 5 * d);
    }
    for (int i = 0; i < 9; i++) {
      BoardGeometry.Box tile = geometry.plots[i];
      boolean unlocked = i < g.plots();
      box(c, tile.left, tile.top, tile.width(), tile.height(), 12 * d,
          unlocked ? 0xff654a35 : 0xff486247);
      if (!unlocked) continue;
      for (int row = 0; row < 3; row++)
        line(c, tile.left + 10 * d, tile.top + tile.height() * (.2f + row * .2f),
            tile.right - 10 * d, tile.top + tile.height() * (.2f + row * .2f), 0xff836344, 2 * d);
      if (g.crop[i] >= 0) {
        c.save();
        c.translate(tile.left + tile.width() * .5f, tile.top + tile.height() * .4f);
        float scale = Math.min(tile.width(), tile.height()) / 66f;
        c.scale(scale, scale);
        crop(c, 0, 0, g.crop[i], g.growth[i]);
        c.restore();
        box(c, tile.left + 8 * d, tile.bottom - 5 * d, tile.width() - 16 * d, 2 * d, d, 0xff3b3928);
        box(c, tile.left + 8 * d, tile.bottom - 5 * d,
            (tile.width() - 16 * d) * g.growth[i], 2 * d, d, 0xffedcc75);
      }
    }
    float time = g.reducedMotion || paused.get() ? 0 : SystemClock.uptimeMillis() / 200f;
    for (Game.Enemy enemy : g.enemies) {
      BoardGeometry.Box lane = geometry.lanes[enemy.lane];
      float x = lane.left + 28 * d + enemy.x * (lane.width() - 55 * d);
      float y = lane.top + lane.height() * .53f;
      float size = Math.min(1.5f * d, lane.height() / 90f);
      c.save();
      c.translate(x, y);
      c.scale(size, size);
      zombie(c, 0, 0, enemy.type, (float) Math.sin(time + enemy.lane) * 2);
      box(c, -22, -59, 44, 4, 2, 0xff203e36);
      box(c, -22, -59, 44 * enemy.hp / enemy.maxHp, 4, 2, 0xffedcc75);
      c.restore();
    }
    if (night && g.shotClock > .55f && !g.enemies.isEmpty() && !g.reducedMotion && !paused.get()) {
      Game.Enemy target = null;
      for (Game.Enemy enemy : g.enemies)
        if (target == null || (enemy.lane == g.lane && target.lane != g.lane)
            || (enemy.lane == target.lane && enemy.x < target.x)) target = enemy;
      BoardGeometry.Box lane = geometry.lanes[target.lane];
      line(c, lane.left + 25 * d, lane.top + lane.height() * .43f,
          lane.left + 28 * d + target.x * (lane.width() - 55 * d),
          lane.top + lane.height() * .53f, 0x99ffe6a6, d);
    }
    if (g.weather == 1)
      for (int i = 0; i < 35; i++) {
        float x = (i * 79 * d) % getWidth();
        float y = ((i * 53 * d) + (g.reducedMotion || paused.get() ? 0 : SystemClock.uptimeMillis() / 12)) % getHeight();
        line(c, x, y, x - 3 * d, y + 10 * d, 0x339fced8, d);
      }
    if (g.weather == 2) box(c, 0, 32 * d, getWidth(), getHeight() * .22f, 16 * d, 0x22e7eadb);
  }

  private static final int CREAM_COLOR = 0xfff5f2e9;

  private void cloud(Canvas c, float x, float y, float scale) {
    c.save();
    c.translate(x, y);
    c.scale(scale, scale);
    circle(c, 0, 0, 23, 0xffe5eddb);
    circle(c, 33, -15, 35, 0xffe5eddb);
    circle(c, 70, 1, 24, 0xffe5eddb);
    box(c, -5, 0, 80, 23, 11, 0xffe5eddb);
    c.restore();
  }

  private void tree(Canvas c, float x, float y, float size, boolean night) {
    c.save();
    c.translate(x, y);
    c.scale(size, size);
    box(c, -7, -2, 14, 64, 4, 0xff715f43);
    circle(c, -18, -13, 30, night ? 0xff274d40 : 0xff527d5c);
    circle(c, 15, -25, 40, night ? 0xff2c5444 : 0xff648e60);
    circle(c, -6, -48, 31, night ? 0xff345e49 : 0xff749e66);
    c.restore();
  }

  private void house(Canvas c, float x, float y, boolean night) {
    box(c, x + 10, y + 21, 253, 113, 7, 0x25343125);
    box(c, x, y + 14, 249, 112, 6, 0xffd5ba86);
    path.reset();
    path.moveTo(x - 17, y + 27);
    path.lineTo(x + 120, y - 65);
    path.lineTo(x + 267, y + 27);
    path.close();
    color(0xffa65f4d);
    c.drawPath(path, p);
    for (int i = 0; i < 6; i++) line(c, x + 32 + i * 31, y + 3, x + 120, y - 52, 0xffbf7960, 2);
    box(c, x + 100, y + 52, 48, 74, 5, 0xff3c5a4c);
    circle(c, x + 136, y + 91, 3, 0xffe9c986);
    for (int k = 0; k < 2; k++) {
      float xx = x + 24 + k * 157;
      box(c, xx, y + 51, 43, 43, 4, night ? 0xffffd47f : 0xffadc9b5);
      line(c, xx + 22, y + 51, xx + 22, y + 94, 0xfff3ddb0, 4);
      line(c, xx, y + 72, xx + 43, y + 72, 0xfff3ddb0, 4);
    }
    box(c, x + 176, y - 44, 25, 45, 3, 0xff8c6951);
    box(c, x + 171, y - 48, 35, 10, 3, 0xff735641);
  }

  private void crop(Canvas c, float x, float y, int type, float growth) {
    float size = .35f + growth * .65f;
    c.save();
    c.translate(x, y);
    c.scale(size, size);
    line(c, 0, 10, 0, -12, 0xff86ae69, 4);
    circle(c, -7, -6, 6, 0xff7d9c49);
    circle(c, 6, -12, 7, 0xff9dbf6c);
    if (growth > .45f) {
      if (type == 0) {
        circle(c, 0, 8, 10, 0xffe3d7af);
        circle(c, 0, 13, 6, 0xffb698b4);
      } else if (type == 1) {
        box(c, -5, -5, 12, 23, 5, 0xfff3ce6d);
        line(c, -11, 17, -6, 0, 0xff92af5e, 4);
      } else {
        circle(c, 0, 7, 15, 0xffd98b42);
        line(c, -3, -4, -3, 20, 0xffbd7134, 2);
        line(c, 5, -4, 5, 20, 0xffedab53, 2);
      }
    }
    c.restore();
  }

  private void zombie(Canvas c, float x, float y, int type, float bob) {
    c.save();
    c.translate(x, y + bob);
    float scale = type == 2 ? 1.3f : 1;
    c.scale(scale, scale);
    box(c, -21, 15, 43, 10, 6, 0x30202d26);
    int coat = COATS[type];
    line(c, -8, 5, -10, 20, 0xff394d46, 8);
    line(c, 7, 5, 11, 20, 0xff394d46, 8);
    box(c, -15, -23, 30, 33, 6, coat);
    line(c, -14, -18, -24, 0, 0xff94ac78, 7);
    line(c, 15, -18, 24, -2, 0xff94ac78, 7);
    box(c, -15, -48, 31, 29, 9, 0xffa5b97f);
    box(c, -13, -48, 26, 7, 4, 0xff485c48);
    circle(c, -6, -34, 3, 0xff233e34);
    circle(c, 7, -34, 3, 0xff233e34);
    line(c, -3, -25, 8, -26, 0xff506547, 2);
    if (type == 2) box(c, -20, -27, 40, 12, 4, 0xff829290);
    if (type == 3) circle(c, 24, -3, 7, 0xffc8cd77);
    c.restore();
  }
}
