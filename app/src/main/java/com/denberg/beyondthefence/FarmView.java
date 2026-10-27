package com.denberg.beyondthefence;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;
import java.util.function.Supplier;

/** Original chunky arcade artwork, viewed from above. All actions have native overlays. */
@android.annotation.SuppressLint("ViewConstructor")
final class FarmView extends View {
  // Counts actual scene rendering, excluding window/ripple draws using the cached layer.
  long renderedFrames;
  private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF rect = new RectF();
  private final Supplier<Game> model;
  private final Supplier<Boolean> paused;
  private final BoardGeometry geometry;
  private static final int[] COATS = {0xff8a6246, 0xffa64e3c, 0xff687d8b, 0xff9a933f};

  FarmView(Context context, Supplier<Game> model, Supplier<Boolean> paused, BoardGeometry geometry) {
    super(context);
    this.model = model; this.paused = paused; this.geometry = geometry;
    setLayerType(View.LAYER_TYPE_HARDWARE, null);
  }
  private void color(int value) { p.setShader(null); p.setColor(value); p.setStyle(Paint.Style.FILL); }
  private void box(Canvas c, float x, float y, float w, float h, float radius, int value) {
    color(value); rect.set(x, y, x + w, y + h); c.drawRoundRect(rect, radius, radius, p);
  }
  private void circle(Canvas c, float x, float y, float r, int value) { color(value); c.drawCircle(x, y, r, p); }
  private void line(Canvas c, float x, float y, float xx, float yy, int value, float width) {
    color(value); p.setStrokeWidth(width); p.setStrokeCap(Paint.Cap.SQUARE); c.drawLine(x, y, xx, yy, p);
  }
  @Override protected void onDraw(Canvas c) {
    super.onDraw(c);
    renderedFrames++;
    Game g = model.get(); float d = geometry.density;
    if (g == null || d == 0 || getHeight() == 0) return;
    // Every frame starts opaque; weather cannot leak alpha into subsequent backgrounds.
    color(g.night ? 0xff213e32 : 0xff4b793a);
    c.drawRect(0, 0, getWidth(), getHeight(), p);
    float l = geometry.fortLeft, t = geometry.fortTop, r = geometry.fortRight, b = geometry.fortBottom;
    for (int i = 0; i < 50; i++) {
      float x = ((i * 131 + 19) * d) % getWidth(), y = ((i * 97 + 7) * d) % getHeight();
      if (x > l - 12 * d && x < r + 12 * d && y > t - 12 * d && y < b + 12 * d) continue;
      if (i % 3 == 0) {
        circle(c, x + d, y + 3 * d, 10 * d, 0x44312c20);
        circle(c, x, y, 9 * d, g.night ? 0xff294d35 : 0xff37632e);
        circle(c, x - 2 * d, y - 2 * d, 5 * d, g.night ? 0xff365b3c : 0xff668e43);
      } else box(c, x, y, 3 * d, 2 * d, d, i % 2 == 0 ? 0xff8a9a59 : 0xff335b32);
    }
    box(c, l, t, r - l, b - t, 3 * d, g.night ? 0xff486444 : 0xff8eb45a);
    // Courtyard grid and path stay behind every building and soil tile.
    for (float x = l + 8 * d; x < r; x += 18 * d)
      for (float y = t + 8 * d; y < b; y += 18 * d)
        box(c, x, y, 3 * d, 2 * d, 0, 0x2479a353);
    for (int i = 0; i < 9; i++) {
      BoardGeometry.Box tile = geometry.plots[i];
      boolean open = i < g.plots();
      box(c, tile.left, tile.top, tile.width(), tile.height(), 3 * d, g.scorched[i] ? 0xff44372c : open ? 0xff755132 : 0xff668e48);
      if (!open) continue;
      for (int row = 0; row < 4; row++)
        line(c, tile.left + 7 * d, tile.top + tile.height() * (.15f + row * .18f),
            tile.right - 7 * d, tile.top + tile.height() * (.15f + row * .18f), 0xff997044, 2 * d);
      if (g.scorched[i]) {
        line(c, tile.left + 10 * d, tile.top + 8 * d, tile.right - 10 * d, tile.top + 20 * d, 0xffb56a46, 3 * d);
        line(c, tile.left + 10 * d, tile.top + 20 * d, tile.right - 10 * d, tile.top + 8 * d, 0xff694733, 3 * d);
      }
      if (g.crop[i] >= 0) {
        box(c, tile.left + 5 * d, tile.top + 3 * d, tile.width() - 10 * d, 3 * d, 0, 0xff492e27);
        box(c, tile.left + 5 * d, tile.top + 3 * d, (tile.width() - 10 * d) * g.cropHealth[i] / Game.CROP_HEALTH,
            3 * d, 0, g.cropHealth[i] < 20 ? 0xffe99b55 : 0xffa8ce6b);
        for (int plant = 0; plant < 3; plant++)
          crop(c, tile.left + tile.width() * (.25f + plant * .25f), tile.top + tile.height() * .35f,
              g.crop[i], g.growth[i], Math.min(tile.width() / 105f, tile.height() / 75f));
        box(c, tile.left + 5 * d, tile.bottom - 4 * d, tile.width() - 10 * d, 2 * d, 0, 0xff3f3c27);
        box(c, tile.left + 5 * d, tile.bottom - 4 * d, (tile.width() - 10 * d) * g.growth[i], 2 * d, 0, 0xfff4d36c);
      }
    }
    for (int i = 0; i < 3; i++) {
      BoardGeometry.Box tile = geometry.buildings[i];
      building(c, tile, i, g.buildings[i], g.buildingRuins[i], d);
      if (g.buildings[i]) {
        box(c, tile.left + 5 * d, tile.top + 3 * d, tile.width() - 10 * d, 3 * d, 0, 0xff492e27);
        box(c, tile.left + 5 * d, tile.top + 3 * d, (tile.width() - 10 * d) * g.buildingHealth[i] / Game.BUILDING_HEALTH,
            3 * d, 0, g.buildingHealth[i] < 50 ? 0xffe99b55 : 0xffa8ce6b);
      }
    }
    for (int side = 0; side < 4; side++) {
      float x = side == 1 ? r : l, y = side == 2 ? b : t;
      float xx = side == 3 ? l : r, yy = side == 0 ? t : b;
      int tier = g.wallHealth[side] > 0 ? g.walls[side] : 0;
      int tone = tier == 0 ? 0x669b8a56 : tier == 1 ? 0xff9d683b : tier == 2 ? 0xff87939c : 0xffb5c4cb;
      line(c, x, y, xx, yy, 0x44302d25, 12 * d);
      if (tier == 0) {
        float length = (float) Math.hypot(xx - x, yy - y);
        for (float at = 0; at < length; at += 18 * d) {
          float end = Math.min(length, at + 6 * d);
          line(c, x + (xx - x) * at / length, y + (yy - y) * at / length,
              x + (xx - x) * end / length, y + (yy - y) * end / length, tone, 4 * d);
        }
      } else {
        line(c, x, y, xx, yy, tone, 8 * d);
        float length = (float) Math.hypot(xx - x, yy - y);
        for (float at = 0; at <= length; at += 14 * d) {
          float px = x + (xx - x) * at / length, py = y + (yy - y) * at / length;
          box(c, px - 3 * d, py - 3 * d, 6 * d, 6 * d, 0, tier == 1 ? 0xffd1a265 : 0xffd8dfce);
        }
      }
      if (g.walls[side] > 0) {
        float ratio = g.wallHealth[side] / g.maxWallHealth(side);
        line(c, x, y, xx, yy, 0xff6e3b2f, 3 * d);
        line(c, x, y, x + (xx - x) * ratio, y + (yy - y) * ratio,
            ratio < .5f ? 0xffe58d4e : 0xffb4d176, 3 * d);
        if (ratio < .6f) for (int crack = 1; crack <= 3; crack++) {
          float at = crack / 4f, cx = x + (xx - x) * at, cy = y + (yy - y) * at;
          line(c, cx - 4 * d, cy - 5 * d, cx + 3 * d, cy + 5 * d, 0xff48352a, 2 * d);
        }
      }
    }
    tower(c, l, t, d); tower(c, r, t, d); tower(c, l, b, d); tower(c, r, b, d);
    for (Game.Roamer roamer : g.roamers) {
      float x = geometry.worldX(Game.ringX(roamer.progress, roamer.distance));
      float y = geometry.worldY(Game.ringY(roamer.progress, roamer.distance));
      fighter(c, x, y, d, false, roamer.type, 0);
    }
    for (Game.Enemy enemy : g.enemies) {
      float x = geometry.worldX(Game.enemyX(enemy)), y = geometry.worldY(Game.enemyY(enemy));
      float size = d * (enemy.type == 2 ? 1.25f : 1);
      if (enemy.breached && enemy.advance >= 1 && enemy.attackClock > 1 && !paused.get() && !g.reducedMotion) {
        float tx = (l + r) / 2, ty = (t + b) / 2;
        if (enemy.target >= 0) {
          BoardGeometry.Box target = enemy.target < 9 ? geometry.plots[enemy.target] : geometry.buildings[enemy.target - 9];
          tx = (target.left + target.right) / 2f; ty = (target.top + target.bottom) / 2f;
        }
        line(c, x, y, tx, ty, 0xcced8257, 2 * d);
      }
      fighter(c, x, y, size, false, enemy.type, 0);
      box(c, x - 8 * d, y - 13 * d, 16 * d, 2 * d, 0, 0xff172e26);
      box(c, x - 8 * d, y - 13 * d, 16 * d * enemy.hp / enemy.maxHp, 2 * d, 0, 0xffefad5a);
    }
    for (Game.Mercenary merc : g.mercenaries) {
      float x = geometry.worldX(Game.patrolX(merc.progress)), y = geometry.worldY(Game.patrolY(merc.progress));
      float angle = (float) Math.atan2(geometry.worldY(merc.targetY) - y, geometry.worldX(merc.targetX) - x);
      if (merc.shotClock > .72f && !paused.get() && !g.reducedMotion)
        line(c, x, y, geometry.worldX(merc.targetX), geometry.worldY(merc.targetY), 0xb3ffe48c, d);
      fighter(c, x, y, d, true, 0, angle);
    }
    if (g.night && g.shotClock > .55f && !g.enemies.isEmpty() && !paused.get() && !g.reducedMotion) {
      Game.Enemy target = null;
      for (Game.Enemy enemy : g.enemies)
        if (target == null || (enemy.lane == g.lane && target.lane != g.lane)
            || (enemy.lane == target.lane && enemy.x < target.x)) target = enemy;
      line(c, r, t, geometry.worldX(Game.enemyX(target)), geometry.worldY(Game.enemyY(target)), 0x88ffe48c, d);
    }
    if (g.weather == 1)
      for (int i = 0; i < 30; i++) {
        float x = (i * 79 * d) % getWidth();
        float y = (i * 53 * d + (g.reducedMotion || paused.get() ? 0 : SystemClock.uptimeMillis() / 14)) % getHeight();
        line(c, x, y, x - 3 * d, y + 8 * d, 0x449fced8, d);
      }
    if (g.weather == 2) box(c, 0, 0, getWidth(), getHeight() * .3f, 0, 0x22e5eadb);
  }
  private void tower(Canvas c, float x, float y, float d) {
    box(c, x - 9 * d, y - 9 * d, 18 * d, 18 * d, 2 * d, 0xff39443b);
    box(c, x - 7 * d, y - 8 * d, 14 * d, 14 * d, d, 0xffa3ac9d);
    box(c, x - 5 * d, y - 6 * d, 10 * d, 10 * d, 0, 0xff526b80);
    box(c, x - 2 * d, y - 3 * d, 4 * d, 5 * d, 0, 0xffd7c894);
  }
  private void building(Canvas c, BoardGeometry.Box tile, int kind, boolean built, boolean ruined, float d) {
    box(c, tile.left, tile.top, tile.width(), tile.height(), 3 * d, built ? 0xffc0a66a : 0xff739a53);
    float x = tile.left + tile.width() * .5f, y = tile.top + tile.height() * .32f;
    float size = Math.min(tile.width() / 50f, tile.height() / 65f);
    c.save(); c.translate(x, y); c.scale(size, size);
    if (ruined) {
      box(c, -15, -8, 30, 22, 1, 0xff504438);
      line(c, -12, -10, 13, 12, 0xffb2764f, 5);
      line(c, 12, -9, -12, 11, 0xff3c332e, 5);
    } else if (!built) {
      box(c, -12, -9, 24, 20, 1, 0xffa0b67d);
      line(c, -7, 1, 7, 1, 0xff4b6e4d, 3); line(c, 0, -6, 0, 8, 0xff4b6e4d, 3);
    } else {
      box(c, -15, -12, 30, 27, 1, 0xff4b4933);
      box(c, -13, -13, 26, 24, 1, kind == 0 ? 0xffceaa6a : kind == 1 ? 0xffc4583e : 0xff4d79a2);
      line(c, -13, -1, 13, -1, kind == 0 ? 0xff987441 : 0xfff2d68e, 3);
      if (kind == 0) {
        circle(c, 0, -1, 4, 0xff554c36);
        line(c, -17, -13, 17, 11, 0xffeee1aa, 4); line(c, 17, -13, -17, 11, 0xffeee1aa, 4);
      } else if (kind == 1) {
        for (int i = 0; i < 4; i++) box(c, -12 + i * 7, -12, 3, 22, 0, 0xffeedca4);
      } else {
        box(c, -4, -7, 8, 9, 0, 0xffc7dfe4);
        line(c, 15, -18, 15, 5, 0xff4c4b35, 2); box(c, 15, -18, 11, 7, 0, 0xffedc75e);
      }
    }
    c.restore();
  }
  private void crop(Canvas c, float x, float y, int type, float growth, float size) {
    c.save(); c.translate(x, y); c.scale(size * (.4f + growth * .6f), size * (.4f + growth * .6f));
    circle(c, 0, 0, type == 2 ? 7 : 5, type == 0 ? 0xffdec9d7 : type == 1 ? 0xfff4d05d : 0xffe5923e);
    box(c, -6, -8, 5, 7, 1, 0xff9fca61); box(c, 0, -9, 6, 8, 1, 0xffbbd978);
    c.restore();
  }
  private void fighter(Canvas c, float x, float y, float d, boolean friendly, int type, float angle) {
    circle(c, x, y + 4 * d, 8 * d, 0x44302922);
    box(c, x - 6 * d, y - 2 * d, 12 * d, 11 * d, 2 * d, friendly ? 0xff2e6596 : COATS[type]);
    circle(c, x, y - 5 * d, 5 * d, friendly ? 0xffe2b878 : 0xffa5c06e);
    box(c, x - 5 * d, y - 10 * d, 10 * d, 4 * d, 1, friendly ? 0xff264c78 : 0xff557044);
    if (friendly) {
      line(c, x + 3 * d, y, x + (float) Math.cos(angle) * 12 * d,
          y + (float) Math.sin(angle) * 12 * d, 0xff2c3029, 3 * d);
      box(c, x - d, y - 8 * d, 2 * d, 2 * d, 0, 0xfff4d26e);
    }
  }
}
