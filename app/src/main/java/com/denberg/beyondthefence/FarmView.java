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
  private final HomesteadArt homestead = new HomesteadArt();
  private final FantasyArt fantasy = new FantasyArt();
  private final BossArt bosses = new BossArt();
  private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF rect = new RectF();
  private final Path woods = new Path(), courtyard = new Path();
  private static final int[] UNIFORM_COLORS = {0xff897b60, 0xff4d774b, 0xffb6a36f, 0xff687f8c, 0xff315b42, 0xff303d48};
  private final Supplier<Game> model;
  private final Supplier<Boolean> paused;
  private final BoardGeometry geometry;
  private static final int[] COATS = {0xff8a6246, 0xffa64e3c, 0xff687d8b, 0xff9a933f, 0xff66527e, 0xff505b63, 0xff668889, 0xff652e42};

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
    color(World.ground(g.world, g.night));
    c.drawRect(0, 0, getWidth(), getHeight(), p);
    // Distant woodland, worn approach tracks, rock outcrops and abandoned camp debris.
    for (int band = 0; band < 3; band++) {
      color(g.ashenWorld() ? (g.night ? 0xff211f29 : 0xff423d39) + band * 0x00040403 : g.night ? 0xff182f2c + band * 0x00050502 : 0xff385a38 + band * 0x00070702);
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
    line(c, road - 5 * d, getHeight(), road + 16 * d, geometry.fortBottom, g.ashenWorld() ? 0xff8c7660 : g.night ? 0xff4c5140 : 0xffa39b70, 27 * d);
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
        circle(c, x, y, 9 * d, g.ashenWorld() ? 0xff3e3939 : g.night ? 0xff294d35 : 0xff37632e);
        circle(c, x - 2 * d, y - 2 * d, 5 * d, g.ashenWorld() ? 0xff706051 : g.night ? 0xff365b3c : 0xff668e43);
      } else box(c, x, y, 3 * d, 2 * d, d, i % 2 == 0 ? 0xff8a9a59 : 0xff335b32);
    }
    if (g.ashenWorld()) {
      // Charred trunks and ruined kiln pillars frame the playable courtyard.
      for (int ruin = 0; ruin < 6; ruin++) {
        float rx = ruin % 2 == 0 ? 14 * d : getWidth() - 24 * d;
        float ry = (70 + ruin * 63) * d % getHeight();
        box(c, rx, ry, 10 * d, 18 * d, d, g.night ? 0xff49414a : 0xff534b47);
        line(c, rx + 2 * d, ry + 7 * d, rx + 8 * d, ry + 4 * d, 0xffb77343, 2 * d);
        line(c, rx + 5 * d, ry, rx - 3 * d, ry - 9 * d, 0xff332c2b, 3 * d);
      }
    }
    courtyard.reset();
    for (int step = 0; step <= 80; step++) {
      float px = geometry.worldX(g.ringXAt(step / 20f, 0)), py = geometry.worldY(g.ringYAt(step / 20f, 0));
      if (step == 0) courtyard.moveTo(px, py); else courtyard.lineTo(px, py);
    }
    courtyard.close(); color(g.ashenWorld() ? g.night ? 0xff5b514b : 0xffa38d68 : g.night ? 0xff486444 : g.settlementStyle() == 2 ? 0xff8e9c7e : 0xff8eb45a); c.drawPath(courtyard, p);
    if (g.settlementStyle() > 0) {
      for (int paving = 0; paving < 18; paving++) {
        float px = l + (r - l) * (.14f + paving % 3 * .35f), py = t + (b - t) * (.12f + paving / 3 * .14f);
        box(c, px, py, 6 * d, 3 * d, d, g.settlementStyle() == 2 ? 0xff727f72 : 0xffb4a15f);
      }
      for (int hut = 0; hut < 2; hut++) {
        float hx = hut == 0 ? 9 * d : getWidth() - 29 * d, hy = t + (hut == 0 ? .35f : .65f) * (b - t);
        box(c, hx, hy, 20 * d, 18 * d, 2 * d, g.settlementStyle() == 1 ? 0xff9c774f : 0xff7a8580);
        line(c, hx - d, hy, hx + 10 * d, hy - 7 * d, 0xffb58c5d, 4 * d);
        line(c, hx + 10 * d, hy - 7 * d, hx + 21 * d, hy, 0xffb58c5d, 4 * d);
      }
    }
    // Courtyard grid and path stay behind every building and soil tile.
    for (float x = l + 8 * d; x < r; x += 18 * d)
      for (float y = t + 8 * d; y < b; y += 18 * d)
        box(c, x, y, 3 * d, 2 * d, 0, 0x2479a353);
    for (int i = 0; i < 9; i++) {
      BoardGeometry.Box tile = geometry.plots[i];
      boolean open = g.farmPlot(i);
      homestead.plot(c, tile, open, g.scorched[i], d);
      if (!open) continue;
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
    pond(c, geometry.pond, d, g);
    for (int side = 0; side < 4; side++) fenceSide(c, g, side, d);
    for (Game.Roamer roamer : g.roamers) {
      float x = geometry.worldX(g.ringXAt(roamer.progress, roamer.distance));
      float y = geometry.worldY(g.ringYAt(roamer.progress, roamer.distance));
      float nextX = geometry.worldX(g.ringXAt((roamer.progress + .01f) % 4, roamer.distance));
      float nextY = geometry.worldY(g.ringYAt((roamer.progress + .01f) % 4, roamer.distance));
      float heading = (float)Math.toDegrees(Math.atan2(nextY - y, nextX - x));
      zombie(c, x, y, d, roamer.type, g.reducedMotion || !g.roamerActive(roamer) ? 0 : g.elapsed * 2 + roamer.progress * 8, false, heading);
    }
    for (Game.Enemy enemy : g.enemies) {
      float x = geometry.worldX(g.foeX(enemy)), y = geometry.worldY(g.foeY(enemy));
      float size = d * (enemy.type == Campaign.BOSS ? 1.65f : enemy.type == 5 ? 1.15f : 1);
      if (enemy.breached && enemy.advance >= 1 && enemy.attackClock > 1 && !paused.get() && !g.reducedMotion) {
        float tx = (l + r) / 2, ty = (t + b) / 2;
        if (enemy.target >= 0) {
          BoardGeometry.Box target = enemy.target < 9 ? geometry.plots[enemy.target] : geometry.buildings[enemy.target - 9];
          tx = (target.left + target.right) / 2f; ty = (target.top + target.bottom) / 2f;
        }
        line(c, x, y, tx, ty, 0xcced8257, 2 * d);
      }
      boolean attacking = !paused.get() && !g.reducedMotion && (enemy.breached ? enemy.advance >= 1 && enemy.attackClock > 1
          : enemy.x <= (Bestiary.ranged(enemy.type) ? .26f : 0));
      float targetX = (l + r) / 2, targetY = (t + b) / 2;
      if (enemy.breached && enemy.target >= 0) {
        BoardGeometry.Box target = enemy.target < 9 ? geometry.plots[enemy.target] : geometry.buildings[enemy.target - 9];
        targetX = (target.left + target.right) / 2f; targetY = (target.top + target.bottom) / 2f;
      }
      float heading = (float)Math.toDegrees(Math.atan2(targetY - y, targetX - x));
      if (enemy.type == Campaign.BOSS) {
        int kind = BossRoster.kind(g.day, g.world);
        float effect = BossRoster.effect(g, enemy);
        if (!enemy.breached) {
          targetX = geometry.worldX(g.ringXAt(enemy.lane + enemy.offset, 0));
          targetY = geometry.worldY(g.ringYAt(enemy.lane + enemy.offset, 0));
        }
        bosses.effect(c, BossRoster.element(kind), x, y, targetX, targetY, d, effect);
        bosses.draw(c, kind, x, y, size, g.reducedMotion ? 0 : g.elapsed * 4,
            !g.reducedMotion && effect >= 0, heading);
      } else {
        zombie(c, x, y, size, enemy.type, g.reducedMotion ? 0 : g.elapsed * (enemy.type == 1 ? 10 : 6) + enemy.offset * 7, attacking, heading);
      }
      if (g.ashenWorld() && (Bestiary.heavy(enemy.type) || enemy.type == Campaign.BOSS)) {
        line(c, x - 4 * size, y - 6 * size, x + 3 * size, y + 2 * size, 0xffe5a35a, 2 * size);
        circle(c, x + 4 * size, y - 7 * size, 2 * size, 0xffffcc7d);
      }
      box(c, x - 8 * d, y - 22 * d, 16 * d, 2 * d, 0, 0xff172e26);
      box(c, x - 8 * d, y - 22 * d, 16 * d * enemy.hp / enemy.maxHp, 2 * d, 0, 0xffefad5a);
    }
    Game.Enemy boss = g.boss();
    if (boss != null) {
      float width = Math.min(getWidth() - 24 * d, 240 * d), bx = (getWidth() - width) / 2;
      color(0xfff5e5c4); p.setTextSize(10 * d); p.setTypeface(Typeface.DEFAULT_BOLD); c.drawText(g.bossTitle(), bx, 12 * d, p);
      box(c, bx, 16 * d, width, 5 * d, 2 * d, 0xff291e2b);
      box(c, bx, 16 * d, width * boss.hp / boss.maxHp, 5 * d, 2 * d, 0xffce6b87);
    }
    if (g.spellTime > 0) {
      float sx = geometry.worldX(g.spellX), sy = geometry.worldY(g.spellY);
      color(g.spellArea ? 0xffe5ac68 : 0xff8bdcf0); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3*d);
      c.drawCircle(sx, sy, (g.spellArea ? 42 : 15) * d * (1.2f-g.spellTime), p); p.setStyle(Paint.Style.FILL);
    }
    for (int person = 0; person < g.mercenaries.size() + (g.hero == null ? 0 : 1); person++) {
      Game.Mercenary merc = person < g.mercenaries.size() ? g.mercenaries.get(person) : g.hero;
      boolean hero = merc == g.hero;
      float x = geometry.worldX(g.guardX(merc.progress)), y = geometry.worldY(g.guardY(merc.progress));
      float angle = (float) Math.atan2(geometry.worldY(merc.targetY) - y, geometry.worldX(merc.targetX) - x);
      if (merc.shotClock > g.guardInterval() - .16f && !paused.get() && !g.reducedMotion)
        line(c, x, y, geometry.worldX(merc.targetX), geometry.worldY(merc.targetY), 0xb3ffe48c, d);
      fighter(c, x, y, d * (hero ? 1.2f : 1), true, hero ? 5 : Math.min(5, g.weapon), angle);
      if (hero) { circle(c, x, y - 12 * d, 3 * d, 0xfff1cb68); line(c, x - 5 * d, y + 8 * d, x + 5 * d, y + 8 * d, 0xffc75b45, 3 * d); }
      if (g.enhancerTime > 0) circle(c, x + 8 * d, y, 2 * d, 0xffe5bcff);
      box(c, x - 7 * d, y - 15 * d, 14 * d, 2 * d, 0, 0xff302f2b);
      box(c, x - 7 * d, y - 15 * d, 14 * d * merc.health / (hero ? g.heroMaxHealth() : g.guardMaxHealth()), 2 * d, 0, 0xff7ecddd);
    }
    if (g.weather == 1)
      for (int i = 0; i < 30; i++) {
        float x = (i * 79 * d) % getWidth();
        float y = (i * 53 * d + (g.reducedMotion || paused.get() ? 0 : SystemClock.uptimeMillis() / 14)) % getHeight();
        line(c, x, y, x - 3 * d, y + 8 * d, 0x449fced8, d);
      }
    if (g.weather == 2) {
      box(c, 0, 0, getWidth(), getHeight() * .3f, 0, g.ashenWorld() ? 0x228e7f7c : 0x22e5eadb);
      if (g.ashenWorld()) for (int ash = 0; ash < 28; ash++) {
        float ax = (ash * 97 * d + (g.reducedMotion ? 0 : g.elapsed * 6 * d)) % getWidth();
        float ay = (ash * 59 * d + (g.reducedMotion ? 0 : g.elapsed * 12 * d)) % getHeight();
        circle(c, ax, ay, d, ash % 5 == 0 ? 0xffe4b16c : 0x779b918d);
      }
    }
  }
  private void fenceSide(Canvas c, Game g, int side, float d) {
    int tier = g.wallHealth[side] > 0 ? g.walls[side] : 0;
    float ratio = g.wallRatio(side);
    for (int part = 0; part < 20; part++) {
      float at = part / 20f, next = (part + 1) / 20f;
      float x = geometry.worldX(g.ringXAt(side + at, 0)), y = geometry.worldY(g.ringYAt(side + at, 0));
      float xx = geometry.worldX(g.ringXAt(side + next, 0)), yy = geometry.worldY(g.ringYAt(side + next, 0));
      homestead.wall(c, x, y, xx, yy, d, tier, part, g.settlementStyle());
      if (g.walls[side] > 0) {
        line(c, x, y, xx, yy, 0xff6e3b2f, 3 * d);
        float fill = Math.max(0, Math.min(1, (ratio - at) * 20));
        if (fill > 0) line(c, x, y, x + (xx - x) * fill, y + (yy - y) * fill,
            ratio < .5f ? 0xffe58d4e : 0xffb4d176, 3 * d);
        if (ratio < .6f && part % 5 == 2) line(c, x - 4 * d, y - 5 * d, x + 3 * d, y + 5 * d, 0xff48352a, 2 * d);
        if (g.wallRanks[side] > 0 && part % 4 == 0) circle(c, x, y, 2 * d, 0xfff0ce7c);
      }
    }
  }

  private void zombie(Canvas c, float x, float y, float d, int type, float phase, boolean attacking, float heading) {
    if (type >= Bestiary.KOBOLD) {
      fantasy.enemy(c, type, x, y, d, phase, attacking, heading); return;
    }
    c.save(); c.translate(x, y); c.scale(d, d);
    float gait = (float) Math.sin(phase), bulk = type == Campaign.BOSS ? 11 : type == 2 ? 10 : type == 1 ? 5 : type == 3 ? 8 : 7;
    int skin = type == Campaign.BOSS ? 0xffc4a9b2 : type == 6 ? 0xff9fd3ca : type == 2 ? 0xff91a286 : type == 3 ? 0xffbdc768 : 0xffa8bd82;
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
    if (type == 5) { box(c, -8, -6, 16, 12, 2, 0xff677580); line(c,-6,-5,6,7,0xffa4afb5,2); }
    if (type == 4) line(c,-5,-7,5,9,0xffb5a0d1,2);
    if (type == 6) { line(c,-6,8,0,13,0xff9dd5cb,2); line(c,0,13,6,8,0xff9dd5cb,2); }
    if (type == Campaign.BOSS) { box(c,-10,-6,20,12,2,0xff522734); line(c,-7,-15,0,-21,0xffcbb675,3); line(c,0,-21,7,-15,0xffcbb675,3); }
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
  private void pond(Canvas c, BoardGeometry.Box tile, float d, Game g) {
    box(c, tile.left, tile.top, tile.width(), tile.height(), 9 * d, 0xff7e8565);
    box(c, tile.left + 3 * d, tile.top + 3 * d, tile.width() - 6 * d, tile.height() - 6 * d, 8 * d, g.night ? 0xff294c59 : 0xff427e88);
    float x = tile.left + tile.width() * .5f, y = tile.top + tile.height() * .35f;
    color(0xffa0d3cd); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(d);
    for (int ring = 0; ring < 3; ring++) {
      float radius = (4 + ring * 4) * d;
      c.drawOval(x - radius, y - radius * .4f, x + radius, y + radius * .4f, p);
    }
    p.setStyle(Paint.Style.FILL);
    circle(c, tile.left + 9 * d, tile.top + 9 * d, 4 * d, 0xff9bbd75);
    line(c, tile.right - 7 * d, tile.top + 17 * d, tile.right - 9 * d, tile.top + 4 * d, 0xffc5c589, 2 * d);
    line(c, tile.right - 11 * d, tile.top + 16 * d, tile.right - 13 * d, tile.top + 6 * d, 0xff8fa66c, 2 * d);
  }
  private void crop(Canvas c, float x, float y, int type, float growth, float size) {
    c.save(); c.translate(x, y); c.scale(size * (.4f + growth * .6f), size * (.4f + growth * .6f));
    circle(c, 0, 0, type == 2 ? 7 : 5, Game.CROP_COLOR[type]);
    if (type == 3) { line(c, 0, 1, 0, 12, Game.CROP_COLOR[type], 4); }
    if (type == 4) { circle(c, -2, -1, 5, 0xffd2e2a5); circle(c, 2, 0, 3, 0xff6c9f60); }
    if (type == 5) { circle(c, -2, 1, 1, 0xfff0d7a1); circle(c, 2, -1, 1, 0xfff0d7a1); }
    if (type >= 6) { circle(c, -5, -2, 3, Game.CROP_COLOR[type]); circle(c, 5, -2, 3, Game.CROP_COLOR[type]); circle(c, 0, -4, 3, 0xfff1df9b); }
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
