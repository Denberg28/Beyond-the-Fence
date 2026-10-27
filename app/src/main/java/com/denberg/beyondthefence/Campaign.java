package com.denberg.beyondthefence;

/** Bounded century progression; early-day prices and upgrade allowances stay familiar. */
final class Campaign {
  static final int BOSS = 7, MAX_ENEMIES = 80, MAX_RANK = 20;
  static int day(int day) { return Math.max(1, Math.min(100, day)); }
  static int chapter(int day) { return (day(day) - 1) / 5; }
  static int upgradeCap(int kind, int day) {
    return kind == 0 ? Math.min(20, 5 + chapter(day)) : kind == 1 ? Math.min(20, 4 + chapter(day))
        : kind == 2 ? Math.min(10, 3 + (day(day) - 1) / 10) : 1;
  }
  static int wallCap(int day) { return Math.min(20, 4 + chapter(day)); }
  static int advancedCap(int kind, int day) { return Math.min(kind == 0 ? 10 : 20, 3 + (kind == 0 ? (day(day) - 1) / 10 : chapter(day))); }
  static int enemyKinds(int day) { return Math.min(4, day(day)) + Math.min(3, (day(day) - 1) / 15); }
  static boolean bossDay(int day) { return day > 0 && day % 5 == 0; }
}
