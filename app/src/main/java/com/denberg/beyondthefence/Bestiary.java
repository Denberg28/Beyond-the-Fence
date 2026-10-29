package com.denberg.beyondthefence;

/** Original fantasy species. Persisted IDs0–7 keep their released meanings; the boss stays7. */
final class Bestiary {
  static final int KOBOLD = 8, ORC = 9, WOLF = 10, HEXER = 11, LAST = HEXER;
  private static final int[] NEW_TYPES = {KOBOLD, WOLF, ORC, HEXER};
  private static final int[] UNLOCK_DAYS = {2, 3, 6, 12};
  private Bestiary() {}
  static int unlocked(int day) {
    int count = 0; for (int unlock : UNLOCK_DAYS) if (day >= unlock) count++; return count;
  }
  static int weight(int day) { return Campaign.enemyKinds(day) * 3 + unlocked(day) * 2; }
  static int pick(int day, int roll) {
    int legacy = Campaign.enemyKinds(day) * 3;
    return roll < legacy ? roll / 3 : NEW_TYPES[(roll - legacy) / 2];
  }
  static int scout(int day, int index) {
    int legacy = Campaign.enemyKinds(day), count = unlocked(day), added = index - legacy;
    // Show every unlocked undead first; rotate new species when the ten-scout cap is full.
    return added >= 0 && added < count ? NEW_TYPES[(added + day / 5) % count] : index % legacy;
  }
  static boolean ranged(int type) { return type == 3 || type == HEXER; }
  static boolean heavy(int type) { return type == 2 || type == 5 || type == ORC; }
  static boolean fast(int type) { return type == 1 || type == 6 || type == KOBOLD || type == WOLF; }
  static String description() {
    return "Undead · Walkers, runners, brutes and spitters remain; ghouls, Ironhides and wraiths join later.\n\n"
        + "Day 2 · Kobold skirmisher\nLean canine raider with a short spear. Quick approach, light armor.\n\n"
        + "Day 3 · Dire wolf\nLow, four-legged silhouette. Fastest approach; fragile compared with heavy raiders.\n\n"
        + "Day 6 · Orc raider\nBroad tusked warrior with an axe and scrap armor. Slow, durable wall breaker.\n\n"
        + "Day 12 · Goblin hexer\nHooded caster with a violet staff. Attacks an intact wall from outside like a spitter.\n\n"
        + "Every fifth night · Boss procession\nEmber dragon, two-headed ogre, great frost wolf, mountain yeti, ancient serpent, storm lich, giant zombie and Grave Warden rotate in order. Later worlds shift the procession; Cinder variants keep the Ashen bonus.\n\nFire · Dragon / serpent. Rock · Ogre / yeti / giant zombie. Lightning · Wolf / lich / Warden. Short contact effects illustrate existing attacks; they add no range or damage. Reduced motion removes the effects and moving poses.\n\nOne boss replaces a reinforcement. Health, damage, quotas, salvage and relic odds are unchanged.\n\n"
        + "Ashfall slows approaches 15%, while Brutes, Ironhides and Orcs deal 20% more damage. Rally defenders to threatened borders.";
  }
}
