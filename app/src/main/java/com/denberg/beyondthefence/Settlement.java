package com.denberg.beyondthefence;

/** Shared boundary used by fences, scouts, guards and combat. Changes only on a new five-day chapter. */
final class Settlement {
  static final String[] NAMES = {"Farm", "Village", "Castle"};
  static int style(int day) { return ((day - 1) / 5) % 3; }
  static float x(int style, float progress, float distance) { return point(style, progress, distance, true); }
  static float y(int style, float progress, float distance) { return point(style, progress, distance, false); }
  private static float point(int style, float progress, float distance, boolean horizontal) {
    progress = (progress % 4 + 4) % 4;
    float l = .18f - distance * .18f, r = .82f + distance * .18f;
    float x, y;
    if (style == 2) {
      float cut = (r - l) * .14f, f = progress - (int)progress;
      float along = Math.min(1, f / .8f), corner = Math.max(0, (f - .8f) / .2f);
      switch ((int)progress) {
        case 0: x = l + cut + along * (r - l - 2 * cut) + corner * cut; y = l + corner * cut; break;
        case 1: x = r - corner * cut; y = l + cut + along * (r - l - 2 * cut) + corner * cut; break;
        case 2: x = r - cut - along * (r - l - 2 * cut) - corner * cut; y = r - corner * cut; break;
        default: x = l + corner * cut; y = r - cut - along * (r - l - 2 * cut) - corner * cut;
      }
    } else { x = Game.ringX(progress, distance); y = Game.ringY(progress, distance); }
    if (style == 1) x = .5f + (x - .5f) * (.86f + .14f * y);
    return horizontal ? x : y;
  }
}
