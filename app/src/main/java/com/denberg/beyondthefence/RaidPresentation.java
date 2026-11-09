package com.denberg.beyondthefence;

/** Pure phase presentation derived from the existing checkpoint; never advances combat/RNG. */
final class RaidPresentation {
  static final float DUSK_SECONDS=12;
  private RaidPresentation() {}
  static float darkness(Game g) {
    if(g.night) return 1;
    if(g.defeated || g.worldComplete || g.arenaOfferDay>0 || g.arenaActive) return 0;
    float t=Math.max(0,Math.min(1,(g.elapsed-g.preparationSeconds()+DUSK_SECONDS)/DUSK_SECONDS));
    return t*t*(3-2*t);
  }
  static boolean dusk(Game g) {return !g.night && darkness(g)>0;}
  static String label(Game g) {
    if(g.defeated) return "Overrun";
    if(g.arenaActive) return "Arena";
    if(g.arenaOfferDay>0) return "Champion awaits";
    if(g.worldComplete) return "Rift open";
    return g.night?(g.elapsed<3?"Nightfall":"Night raid"):dusk(g)?"Dusk · "+g.raidInSeconds()+"s":"Daylight";
  }
  static String banner(Game g) {
    if(g.defeated) return "OVERRUN · RECLAIM YOUR HAVEN";
    if(g.arenaActive || g.arenaOfferDay>0 || g.worldComplete) return "";
    if(dusk(g)) return "DUSK · THE DEAD ARE GATHERING";
    return g.night && g.elapsed<3?"NIGHTFALL · HOLD THE LINE":"";
  }
  static int blend(int day,int night,float mix) {
    float t=Float.isFinite(mix)?Math.max(0,Math.min(1,mix)):0;
    int a=Math.round((day>>>24)*(1-t)+(night>>>24)*t);
    int r=Math.round(((day>>16)&255)*(1-t)+((night>>16)&255)*t);
    int g=Math.round(((day>>8)&255)*(1-t)+((night>>8)&255)*t);
    int b=Math.round((day&255)*(1-t)+(night&255)*t);
    return (a<<24)|(r<<16)|(g<<8)|b;
  }
}
