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
  private final Path woods = new Path();
  private static final int[] UNIFORM_COLORS = {0xff897b60, 0xff4d774b, 0xffb6a36f, 0xff687f8c, 0xff315b42, 0xff303d48};
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
    // Distant woodland, worn approach tracks, rock outcrops and abandoned camp debris.
    for (int band = 0; band < 3; band++) {
      color(g.night ? 0xff182f2c + band * 0x00050502 : 0xff385a38 + band * 0x00070702);
      woods.reset(); woods.moveTo(0, 0);
      for (int tree = 0; tree <= 20; tree++) {
        float tx = tree * getWidth() / 20f;
        woods.lineTo(tx - 10 * d, (18 + band * 8 + tree % 3 * 3) * d);
        woods.lineTo(tx, (3 + band * 6 + tree % 2 * 4) * d);
        woods.lineTo(tx + 10 * d, (18 + band * 8 + tree % 3 * 3) * d);
      }
      woods.lineTo(getWidth(), 0); woods.close(); c.drawPath(woods, p);
    }
    float road = geometry.worldX(.50f);
    line(c, road - 5 * d, getHeight(), road + 16 * d, geometry.fortBottom, g.night ? 0xff4c5140 : 0xffa39b70, 27 * d);
    for (int stone = 0; stone < 12; stone++) {
      float sx = stone % 2 == 0 ? 10 * d : getWidth() - 14 * d;
      float sy = (50 + stone * 47) * d % getHeight();
      box(c, sx, sy, (8 + stone % 3 * 3) * d, 6 * d, 2 * d, g.night ? 0xff43534d : 0xff87907a);
      line(c, sx, sy, sx + 6 * d, sy - d, 0x55747c6c, d);
    }
    box(c, 3 * d, getHeight() - 39 * d, 22 * d, 12 * d, 2 * d, 0xff58554a);
    line(c, 5 * d, getHeight() - 40 * d, 20 * d, getHeight() - 30 * d, 0xff99866b, 3 * d);
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
          if (tier == 1) box(c, px - 3 * d, py - 3 * d, 6 * d, 6 * d, 0, 0xffd1a265);
          else if (tier == 2) {
            boolean horizontal = side == 0 || side == 2;
            box(c, px - (horizontal ? 6 : 4) * d, py - (horizontal ? 4 : 6) * d,
                (horizontal ? 12 : 8) * d, (horizontal ? 8 : 12) * d, d, 0xffacb1a7);
            line(c, px - 2 * d, py - 2 * d, px + 2 * d, py + 2 * d, 0xff727c75, d);
          } else {
            box(c, px - 2 * d, py - 5 * d, 4 * d, 10 * d, 0, 0xffd3e0e0);
            line(c, px - 4 * d, py - 4 * d, px + 4 * d, py + 4 * d, 0xff526b78, d);
          }
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
    post(c, l, t, d); post(c, r, t, d); post(c, l, b, d); post(c, r, b, d);
    for (Game.Roamer roamer : g.roamers) {
      float x = geometry.worldX(Game.ringX(roamer.progress, roamer.distance));
      float y = geometry.worldY(Game.ringY(roamer.progress, roamer.distance));
      zombie(c, x, y, d, roamer.type, g.reducedMotion ? 0 : roamer.progress * 12, false);
    }
    for (Game.Enemy enemy : g.enemies) {
      float x = geometry.worldX(Game.enemyX(enemy)), y = geometry.worldY(Game.enemyY(enemy));
      float size = d;
      if (enemy.breached && enemy.advance >= 1 && enemy.attackClock > 1 && !paused.get() && !g.reducedMotion) {
        float tx = (l + r) / 2, ty = (t + b) / 2;
        if (enemy.target >= 0) {
          BoardGeometry.Box target = enemy.target < 9 ? geometry.plots[enemy.target] : geometry.buildings[enemy.target - 9];
          tx = (target.left + target.right) / 2f; ty = (target.top + target.bottom) / 2f;
        }
        line(c, x, y, tx, ty, 0xcced8257, 2 * d);
      }
      boolean attacking = !paused.get() && !g.reducedMotion && (enemy.breached ? enemy.advance >= 1 && enemy.attackClock > 1
          : enemy.x <= (enemy.type == 3 ? .26f : 0));
      zombie(c, x, y, size, enemy.type, g.reducedMotion ? 0 : g.elapsed * (enemy.type == 1 ? 10 : 6) + enemy.offset * 7, attacking);
      box(c, x - 8 * d, y - 22 * d, 16 * d, 2 * d, 0, 0xff172e26);
      box(c, x - 8 * d, y - 22 * d, 16 * d * enemy.hp / enemy.maxHp, 2 * d, 0, 0xffefad5a);
    }
    for (Game.Mercenary merc : g.mercenaries) {
      float x = geometry.worldX(Game.patrolX(merc.progress)), y = geometry.worldY(Game.patrolY(merc.progress));
      float angle = (float) Math.atan2(geometry.worldY(merc.targetY) - y, geometry.worldX(merc.targetX) - x);
      if (merc.shotClock > g.guardInterval() - .16f && !paused.get() && !g.reducedMotion)
        line(c, x, y, geometry.worldX(merc.targetX), geometry.worldY(merc.targetY), 0xb3ffe48c, d);
      fighter(c, x, y, d, true, g.weapon, angle);
      box(c, x - 7 * d, y - 15 * d, 14 * d, 2 * d, 0, 0xff302f2b);
      box(c, x - 7 * d, y - 15 * d, 14 * d * merc.health / g.guardMaxHealth(), 2 * d, 0, 0xff7ecddd);
    }
    if (g.weather == 1)
      for (int i = 0; i < 30; i++) {
        float x = (i * 79 * d) % getWidth();
        float y = (i * 53 * d + (g.reducedMotion || paused.get() ? 0 : SystemClock.uptimeMillis() / 14)) % getHeight();
        line(c, x, y, x - 3 * d, y + 8 * d, 0x449fced8, d);
      }
    if (g.weather == 2) box(c, 0, 0, getWidth(), getHeight() * .3f, 0, 0x22e5eadb);
  }
  private void post(Canvas c, float x, float y, float d) {
    box(c, x - 3 * d, y - 3 * d, 6 * d, 6 * d, d, 0xffb7a073);
    box(c, x - d, y - d, 2 * d, 2 * d, 0, 0xff6f593c);
  }

  private void zombie(Canvas c, float x, float y, float d, int type, float phase, boolean attacking) {
    c.save(); c.translate(x, y); c.scale(d, d);
    float gait = (float) Math.sin(phase), bulk = type == 2 ? 10 : type == 1 ? 5 : type == 3 ? 8 : 7;
    int skin = type == 2 ? 0xff91a286 : type == 3 ? 0xffbdc768 : 0xffa8bd82;
    color(0x55302b23); rect.set(-11, 5, 11, 13); c.drawOval(rect, p);
    // Uneven trouser legs and a shuffling gait keep the silhouette recognizably undead.
    line(c, -3, 5, -5 - gait * 2, 12, 0xff44483a, 4);
    line(c, 3, 5, 5 + gait * 2, 12, 0xff353b33, 4);
    box(c, -bulk, -6, bulk * 2, type == 2 ? 18 : 15, 3, COATS[type]);
    box(c, -bulk + 2, -5, 3, 13, 1, 0x447b862f);
    float armY = attacking ? -10 : -1;
    line(c, -bulk + 1, -3, -bulk - 5, armY + gait * 2, COATS[type], type == 2 ? 5 : 3);
    line(c, bulk - 1, -3, bulk + 5, armY - gait * 2, COATS[type], type == 2 ? 5 : 3);
    circle(c, -bulk - 5, armY + gait * 2, 2.4f, skin);
    circle(c, bulk + 5, armY - gait * 2, 2.4f, skin);
    if (type == 1) line(c, -3, -4, 4, 7, 0xffe1b88b, 2); // Torn runner jersey.
    if (type == 2) {
      box(c, -10, -7, 5, 7, 1, 0xff67717a); box(c, 5, -7, 5, 7, 1, 0xff67717a);
      box(c, -8, 4, 16, 3, 0, 0xff4d4540); box(c, -2, 4, 4, 3, 0, 0xffbda578);
    }
    if (type == 3) {
      circle(c, 0, 2, 7, 0xff9fac4e);
      circle(c, -3, 1, 2, 0xffccd879); circle(c, 3, 4, 2, 0xff7e933c);
    }
    circle(c, type == 1 ? 2 : 0, -10, type == 2 ? 7 : 6, skin);
    line(c, -4, -14, -1, -16, 0xff414c35, 2);
    line(c, 0, -14, 4, -15, 0xff414c35, 2);
    circle(c, -2, -10, 1.4f, 0xffefe6c5); circle(c, 3, -10, 1.4f, 0xffefe6c5);
    circle(c, -2, -10, .6f, 0xff302e27); circle(c, 3, -10, .6f, 0xff302e27);
    box(c, -2, -6, 5, 2.5f, .4f, 0xff4b4735);
    box(c, -1, -6, 1, 1, 0, 0xffeee0b2); box(c, 1, -6, 1, 1, 0, 0xffeee0b2);
    line(c, 4, -14, 5, -12, 0xff6a7751, 1);
    if (type == 3) { line(c, 2, -4, 5, 1, 0xffbfe875, 1.5f); circle(c, 6, 2, 1.5f, 0xffd3ed8d); }
    c.restore();
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
    box(c, x - 6 * d, y - 2 * d, 12 * d, 11 * d, 2 * d, UNIFORM_COLORS[type]);
    circle(c, x, y - 5 * d, 5 * d, friendly ? 0xffe2b878 : 0xffa5c06e);
    box(c, x - 5 * d, y - 10 * d, 10 * d, 4 * d, 1, type >= 3 ? 0xff354952 : 0xff596345);
    if (friendly) {
      if (type >= 2) box(c, x - 4 * d, y - d, 8 * d, 7 * d, d, type >= 4 ? 0xff263d34 : 0xff6a7269);
      for (int stripe = 0; stripe < Math.min(type, 3); stripe++)
        line(c, x - 5 * d, y + stripe * 2 * d, x - 2 * d, y + stripe * 2 * d, 0xffe3cc83, d);
      line(c, x + 3 * d, y, x + (float) Math.cos(angle) * 12 * d,
          y + (float) Math.sin(angle) * 12 * d, 0xff2c3029, 3 * d);
      box(c, x - d, y - 8 * d, 2 * d, 2 * d, 0, 0xfff4d26e);
    }
  }
}
