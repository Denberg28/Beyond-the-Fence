package com.denberg.beyondthefence;

/** Authored biome rules derived from the persisted world/day; no extra save fields or RNG. */
final class World {
  private World() {}
  static boolean ashen(int world) { return world >= 2; }
  static String name(int world) { return ashen(world) ? "Ashen March" : "Greenhaven"; }
  static String introduction(int world) {
    return ashen(world)
        ? "Beyond Greenhaven, a burned kingdom waits. Raise a haven among black pines and ember ruins.\n\nAshfall slows the approaching dead by 15%, but Cinder brutes, Ironhides and Orcs strike 20% harder. Every fifth night brings ashfall and the Cinder Regent. Rally guards before the walls break."
        : "A fence. A seed. A second chance. Survive one hundred nights in Greenhaven to open the rift.";
  }
  static String chapter(int world, int day) {
    if (!ashen(world)) return "The first haven";
    return day <= 25 ? "I · The Black Pines" : day <= 50 ? "II · Cinder Road"
        : day <= 75 ? "III · The Silent Kilns" : "IV · The Regent's Reach";
  }
  static int settlementStyle(int world, int day) {
    return (Settlement.style(day) + (ashen(world) ? 1 : 0)) % 3;
  }
  static String settlementName(int world, int style) {
    return ashen(world) ? new String[] {"Ash camp", "Cinder outpost", "Ember keep"}[style] : Settlement.NAMES[style];
  }
  static String weatherName(int world, int weather) {
    return ashen(world) && weather == 2 ? "Ashfall" : Game.WEATHER[weather];
  }
  static int weather(int world, int day, int rolled) {
    return ashen(world) && Campaign.bossDay(day) ? 2 : rolled;
  }
  static int ground(int world, boolean night) {
    return ashen(world) ? night ? 0xff302c35 : 0xff706351 : night ? 0xff213e32 : 0xff4b793a;
  }
}
