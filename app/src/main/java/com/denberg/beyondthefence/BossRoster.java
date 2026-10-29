package com.denberg.beyondthefence;

/** Presentation only: stable cycle without new persisted IDs, random draws or combat rules. */
final class BossRoster {
  static final int DRAGON = 0, OGRE = 1, WOLF = 2, YETI = 3, SNAKE = 4,
      LICH = 5, ZOMBIE = 6, WARDEN = 7, COUNT = 8;
  static final int FIRE = 0, ROCK = 1, LIGHTNING = 2;
  private static final String[] NAMES = {"Ember dragon", "Two-headed ogre", "Great frost wolf",
      "Mountain yeti", "Ancient serpent", "Storm lich", "Giant zombie", "Grave Warden"};
  private BossRoster() {}
  static int kind(int day, int world) {
    return (Math.max(0, (Campaign.day(day) - 1) / 5) + Math.max(0, world - 1) % COUNT) % COUNT;
  }
  static String name(int kind) { return NAMES[kind]; }
  static int element(int kind) {
    return kind == DRAGON || kind == SNAKE ? FIRE
        : kind == OGRE || kind == YETI || kind == ZOMBIE ? ROCK : LIGHTNING;
  }
  /** Matches contact damage / the existing 1.2s interior strike clock; no extra damage. */
  static float effect(Game g, Game.Enemy e) {
    if (g.reducedMotion || !g.night || g.defeated || e.hp <= 0 || e.type != Campaign.BOSS) return -1;
    if (e.breached) {
      return e.advance >= 1 && e.attackClock > .90f ? (1.2f - e.attackClock) / .30f : -1;
    }
    if (e.x > 0 || g.wallHealth[e.lane] <= 0) return -1;
    float cycle = g.elapsed % 1.2f;
    return cycle < .30f ? cycle / .30f : -1;
  }
}
