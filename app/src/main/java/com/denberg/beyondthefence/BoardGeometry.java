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
  final Box[] plots = new Box[9], buildings = new Box[3];
  boolean wide;
  float density, fortLeft, fortTop, fortRight, fortBottom;
  private int width, height;
  BoardGeometry() {
    for (int i = 0; i < 9; i++) plots[i] = new Box();
    for (int i = 0; i < 3; i++) buildings[i] = new Box();
  }
  static int minimumHeight(float density, float fontScale, boolean wide) {
    float cell = Math.max(48, 28 * fontScale + 12);
    return (int) Math.ceil(density * (wide ? 3 * cell + 160 : 4 * cell + 198));
  }
  void layout(int width, int height, float density, boolean wide, float fontScale) { layout(width, height, density, wide, fontScale, 0); }
  void layout(int width, int height, float density, boolean wide, float fontScale, int style) {
    this.width = width; this.height = height; this.density = density; this.wide = wide;
    // Keep the farm compact as the viewport grows. The surrounding terrain belongs to zombies.
    float gap = 6 * density;
    float cell = Math.max(48, 28 * fontScale + 12) * density;
    float farmWidth = Math.min(width - 112 * density, (wide ? 380 : 256) * density);
    float farmHeight = (wide ? 3 : 4) * cell + (wide ? 64 : 70) * density;
    fortLeft = (width - farmWidth) / 2; fortRight = width - fortLeft;
    fortTop = (height - farmHeight) / 2; fortBottom = height - fortTop;
    float left = fortLeft + 26 * density, right = fortRight - 26 * density;
    float top = fortTop + 26 * density, bottom = fortBottom - 26 * density;
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
    if (style > 0) {
      Box[] old = new Box[3];
      for (int i = 0; i < 3; i++) { old[i] = new Box(); old[i].set(buildings[i].left, buildings[i].top, buildings[i].width(), buildings[i].height()); }
      for (int i = 0; i < 3; i++) { Box b = old[(i + style) % 3]; buildings[i].set(b.left, b.top, b.width(), b.height()); }
      // Garden coordinates stay stable: the entire lower row is the pond / floating garden.
    }
  }
  float worldX(float x) {
    float inset = 24 * density;
    if (x < .18f) return inset + (fortLeft - inset) * x / .18f;
    if (x > .82f) return fortRight + (width - inset - fortRight) * (x - .82f) / .18f;
    return fortLeft + (fortRight - fortLeft) * (x - .18f) / .64f;
  }
  float worldY(float y) {
    float inset = 24 * density;
    if (y < .18f) return inset + (fortTop - inset) * y / .18f;
    if (y > .82f) return fortBottom + (height - inset - fortBottom) * (y - .82f) / .18f;
    return fortTop + (fortBottom - fortTop) * (y - .18f) / .64f;
  }
}
