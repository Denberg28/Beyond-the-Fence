package com.denberg.beyondthefence;

/** One top-down fortress, with all farm/building actions inside its perimeter. */
final class BoardGeometry {
  static final class Box {
    int left, top, right, bottom;
    int width() { return right - left; }
    int height() { return bottom - top; }
    void set(float x, float y, float w, float h) {
      left = Math.round(x); top = Math.round(y); right = Math.round(x + w); bottom = Math.round(y + h);
    }
  }
  final Box[] plots = new Box[9], walls = new Box[4], buildings = new Box[3];
  boolean wide;
  float density, fortLeft, fortTop, fortRight, fortBottom;
  private int width, height;
  BoardGeometry() {
    for (int i = 0; i < 9; i++) plots[i] = new Box();
    for (int i = 0; i < 4; i++) walls[i] = new Box();
    for (int i = 0; i < 3; i++) buildings[i] = new Box();
  }
  static int minimumHeight(float density, float fontScale, boolean wide) {
    float cell = Math.max(48, 28 * fontScale + 12);
    return (int) Math.ceil(density * (wide ? 5 * cell + 24 : 6 * cell + 30));
  }
  void layout(int width, int height, float density, boolean wide) {
    this.width = width; this.height = height; this.density = density; this.wide = wide;
    // Grow the edge targets vertically when additional height is available; no hit areas overlap.
    float gap = 6 * density;
    float cell = Math.max(48 * density, (height - (wide ? 24 : 30) * density) / (wide ? 5 : 6));
    float wallHeight = Math.min(cell, 80 * density);
    fortLeft = 42 * density; fortRight = width - 42 * density;
    fortTop = wallHeight / 2 + 4 * density; fortBottom = height - fortTop;
    float left = fortLeft + 26 * density, right = fortRight - 26 * density;
    float top = fortTop + wallHeight / 2 + 2 * density, bottom = fortBottom - wallHeight / 2 - 2 * density;
    walls[0].set(width / 2f - 24 * density, fortTop - wallHeight / 2, 48 * density, wallHeight);
    walls[1].set(fortRight - 24 * density, height / 2f - wallHeight / 2, 48 * density, wallHeight);
    walls[2].set(width / 2f - 24 * density, fortBottom - wallHeight / 2, 48 * density, wallHeight);
    walls[3].set(fortLeft - 24 * density, height / 2f - wallHeight / 2, 48 * density, wallHeight);
    float cropRight = right, cropTop = top, rowHeight;
    if (wide) {
      float bw = Math.max(64 * density, (right - left) * .22f);
      rowHeight = (bottom - top - gap * 2) / 3;
      for (int i = 0; i < 3; i++) buildings[i].set(right - bw, top + i * (rowHeight + gap), bw, rowHeight);
      cropRight = right - bw - gap;
    } else {
      rowHeight = (bottom - top - gap * 3) / 4;
      float bw = (right - left - gap * 2) / 3;
      for (int i = 0; i < 3; i++) buildings[i].set(left + i * (bw + gap), top, bw, rowHeight);
      cropTop = top + rowHeight + gap;
    }
    float cw = (cropRight - left - gap * 2) / 3, ch = (bottom - cropTop - gap * 2) / 3;
    for (int i = 0; i < 9; i++) plots[i].set(left + (i % 3) * (cw + gap), cropTop + (i / 3) * (ch + gap), cw, ch);
  }
  float worldX(float x) {
    if (x < .18f) return fortLeft * x / .18f;
    if (x > .82f) return fortRight + (width - fortRight) * (x - .82f) / .18f;
    return fortLeft + (fortRight - fortLeft) * (x - .18f) / .64f;
  }
  float worldY(float y) {
    if (y < .18f) return fortTop * y / .18f;
    if (y > .82f) return fortBottom + (height - fortBottom) * (y - .82f) / .18f;
    return fortTop + (fortBottom - fortTop) * (y - .18f) / .64f;
  }
}
