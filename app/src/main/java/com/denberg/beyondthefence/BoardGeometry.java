package com.denberg.beyondthefence;

/** Shared pixel bounds for the painted world and its native, accessible action tiles. */
final class BoardGeometry {
  static final class Box {
    int left, top, right, bottom;

    int width() { return right - left; }
    int height() { return bottom - top; }

    void set(float x, float y, float w, float h) {
      left = Math.round(x);
      top = Math.round(y);
      right = Math.round(x + w);
      bottom = Math.round(y + h);
    }
  }

  final Box[] plots = new Box[9], lanes = new Box[3];
  boolean wide;
  float density, cropLabelY, laneLabelY;

  BoardGeometry() {
    for (int i = 0; i < 9; i++) plots[i] = new Box();
    for (int i = 0; i < 3; i++) lanes[i] = new Box();
  }

  static int minimumHeight(float density, float fontScale, boolean wide) {
    float cell = Math.max(48, 28 * fontScale + 12);
    return (int) Math.ceil(density * (wide ? 52 + 3 * cell : 96 + 6 * cell));
  }

  void layout(int width, int height, float density, boolean wide) {
    this.wide = wide;
    this.density = density;
    float pad = 8 * density, gap = 6 * density, header = 32 * density;
    float cropX = pad, cropY, cropW, cropH, laneX, laneY, laneW, laneH;
    if (wide) {
      cropW = width * .45f - pad;
      cropY = laneY = header;
      cropH = laneH = height - header - pad;
      laneX = width * .53f;
      laneW = width - laneX - pad;
      cropLabelY = laneLabelY = 22 * density;
    } else {
      cropW = laneW = width - pad * 2;
      laneX = pad;
      laneY = header;
      laneH = (height - header - 32 * density - pad) / 2;
      cropY = laneY + laneH + 32 * density;
      cropH = height - cropY - pad;
      laneLabelY = 22 * density;
      cropLabelY = cropY - 9 * density;
    }
    float cw = (cropW - gap * 2) / 3, ch = (cropH - gap * 2) / 3;
    for (int i = 0; i < 9; i++)
      plots[i].set(cropX + (i % 3) * (cw + gap), cropY + (i / 3) * (ch + gap), cw, ch);
    float lh = (laneH - gap * 2) / 3;
    for (int i = 0; i < 3; i++) lanes[i].set(laneX, laneY + i * (lh + gap), laneW, lh);
  }
}
