package com.denberg.beyondthefence;

/** A finite collection: nine slots, five rarities; duplicates never inflate power or coins. */
public final class Equipment {
  public static final String[] SLOTS = {"Helmet", "Boots", "Gloves", "Weapon", "Ring", "Necklace", "Talisman", "Belt", "Shoulder pad"};
  public static final String[] RARITIES = {"Common", "Uncommon", "Rare", "Epic", "Legendary"};
  private static final String[] PREFIX = {"Wayfarer's", "Moonlit", "Runebound", "Riftforged", "Dawnkeeper's"};
  public static final int COUNT = 45;
  private Equipment() {}
  public static int slot(int code) { return code / 5; }
  public static int rarity(int code) { return code % 5; }
  public static String name(int code) { return PREFIX[rarity(code)] + " " + SLOTS[slot(code)]; }
  public static int stat(int slot) {
    return slot == 0 || slot == 7 || slot == 8 ? 1 : slot == 1 ? 3 : slot == 5 || slot == 6 ? 2 : 0;
  }
  public static float value(int code) { return (rarity(code) + 1) * (stat(slot(code)) == 0 || stat(slot(code)) == 1 ? .008f : stat(slot(code)) == 2 ? .01f : .012f); }
  public static String effect(int code) {
    return "+" + Math.round(value(code) * 1000) / 10f + "% " + new String[] {"crew attack", "farm / crew protection", "magic power", "attack speed"}[stat(slot(code))];
  }
}
