package com.denberg.beyondthefence;

import android.graphics.Canvas;
import android.graphics.Paint;

/** Original sprites assembled on an integer pixel grid. No bitmaps, RNG or animation timers. */
final class PixelCharacters {
  private final Paint p=new Paint();
  private static final int OUT=0xff111821, BONE=0xffc6c0a3, IRON=0xff7e8b91, LIGHT=0xffbcc4bb;
  private static final int[] CLOTH={0xff4b3a39,0xff4b4738,0xff3c4349,0xff4f4a32,0xff443748,0xff343a43,0xff283d46};
  private static final int[] SKIN={0xff879278,0xff95977a,0xff747f6b,0xff8c9c60,0xff89908a,0xff8b927d,0xff99bdba};
  private static final int[] GUARD={0xff655d50,0xff3e5147,0xff756b50,0xff586773,0xff344b43,0xff2d3440};
  private void px(Canvas c,float x,float y,float w,float h,int color) {
    p.setColor(color);c.drawRect(x,y,x+w,y+h,p);
  }
  private void begin(Canvas c,float x,float y,float scale,float heading) {
    c.save();c.translate(Math.round(x),Math.round(y));c.scale(scale,scale);
    if(Math.cos(Math.toRadians(heading))<-.001) c.scale(-1,1);
  }
  private void feet(Canvas c,int step,int color,int bulk) {
    px(c,-bulk+1,5,bulk-1,8+step,OUT);px(c,2,5,bulk-1,8-step,OUT);
    px(c,-bulk+2,6,bulk-3,5+step,color);px(c,3,6,bulk-3,5-step,color);
    px(c,-bulk,12+step,bulk,3,0xff242a2d);px(c,2,12-step,bulk+1,3,0xff242a2d);
  }
  void undead(Canvas c,int type,float x,float y,float scale,float phase,boolean attack,float heading) {
    begin(c,x,y,scale,heading);
    int step=Math.round((float)Math.sin(phase)*2), width=type==2?11:type==1?6:8;
    int cloth=CLOTH[Math.min(6,type)],skin=SKIN[Math.min(6,type)];
    px(c,-width-2,12,width*2+4,3,0x66101820);
    feet(c,step,0xff43463e,width);
    px(c,-width-1,-7,width*2+2,16,OUT);px(c,-width,-6,width*2,14,cloth);
    px(c,-width+2,-4,3,10,0xff626352);px(c,-2,3,3,8,OUT);
    px(c,-width,8,4,3,cloth);px(c,width-3,7,3,5,cloth);
    int arm=attack?-10:-2;
    px(c,-width-5,-5,5,10,OUT);px(c,-width-4,-4,3,7,cloth);
    px(c,-width-7,arm+step,5,4,skin);px(c,-width-8,arm+step,2,6,BONE);
    px(c,width,-5,5,10,OUT);px(c,width+1,-4,3,7,cloth);
    px(c,width+3,arm-step,5,4,skin);px(c,width+7,arm-step,2,6,BONE);
    int lean=type==1?3:0;
    px(c,-7+lean,-18,14,13,OUT);px(c,-6+lean,-17,12,11,skin);
    px(c,-6+lean,-17,5,3,0xff404739);px(c,2+lean,-18,4,3,0xff404739);
    px(c,-5+lean,-13,4,4,OUT);px(c,2+lean,-13,4,3,OUT);
    px(c,-4+lean,-12,2,1,0xffe4bd73);px(c,3+lean,-12,2,1,0xffe4bd73);
    px(c,1+lean,-9,2,2,0xff59664f);px(c,-3+lean,-7,8,3,OUT);
    px(c,-2+lean,-7,2,1,BONE);px(c,3+lean,-7,1,2,BONE);
    if(type==1) {px(c,-7,-18,4,8,0xff5b4a3d);px(c,7,-4,2,10,0xff998260);}
    if(type==2 || type==5) {
      px(c,-width-2,-7,7,7,0xff4b5358);px(c,width-5,-7,7,7,0xff4b5358);
      px(c,-6,-5,13,10,type==5?0xff606b70:0xff515d60);px(c,-5,-5,2,8,IRON);
      px(c,-width,7,width*2,3,OUT);px(c,-2,7,4,3,0xff97764b);
      if(type==5) {px(c,-8,-19,16,8,0xff4c595e);px(c,-6,-19,12,2,IRON);px(c,-1,-19,2,10,IRON);}
    }
    if(type==3) {px(c,-5,-4,11,10,0xff677345);px(c,-4,-3,3,3,0xffb0b86a);px(c,2,1,3,4,0xffa3ad64);}
    if(type==4) {px(c,-6,-17,12,4,0xff3b3444);px(c,0,-2,2,10,BONE);px(c,-4,0,10,1,0xff8e8990);}
    if(type==6) {
      px(c,-8,-19,16,4,0xff24303d);px(c,-9,-16,4,13,0xff304651);px(c,6,-16,4,13,0xff304651);
      px(c,-5,-14,11,9,OUT);px(c,-4,-12,3,1,0xff9ddbd2);px(c,3,-12,3,1,0xff9ddbd2);
      px(c,-7,8,3,8,0xff41606a);px(c,0,7,3,10,0xff557980);px(c,6,6,3,7,0xff41606a);
    }
    c.restore();
  }
  void guard(Canvas c,float x,float y,float scale,int tier,float heading,float phase) {
    begin(c,x,y,scale,heading);tier=Math.max(0,Math.min(5,tier));
    int coat=GUARD[tier],step=Math.round((float)Math.sin(phase));
    px(c,-10,12,21,3,0x66101820);feet(c,step,0xff46515b,7);
    px(c,-9,-7,18,17,OUT);px(c,-7,-5,14,13,coat);
    px(c,-8,-6,4,8,tier>=2?IRON:0xff8a8270);px(c,5,-6,4,8,tier>=2?IRON:0xff8a8270);
    px(c,-4,-4,8,8,tier>=3?0xff7c8b92:coat);px(c,-7,6,14,3,0xff332c28);px(c,-1,6,3,3,0xffc4a05b);
    px(c,-7,-19,14,13,OUT);px(c,-5,-17,10,9,0xffb3a184);
    px(c,-7,-19,14,5,tier>=2?IRON:0xff56616a);px(c,-5,-20,10,2,tier>=2?LIGHT:0xff87918d);
    px(c,-5,-12,10,2,OUT);px(c,-3,-12,2,1,0xffd5d4bc);px(c,3,-12,2,1,0xffd5d4bc);
    // Crossbow silhouette replaces modern rifles; existing ranged combat stats are retained.
    px(c,4,-2,17,3,OUT);px(c,5,-1,17,1,0xffb7ab85);px(c,9,-8,3,16,0xff8c6b43);
    px(c,8,-7,1,14,0xffb7a17a);px(c,13,-6,1,12,0xffded5ae);px(c,14,-3,8,1,LIGHT);
    px(c,-9,-2,6,4,0xff9a8d73);px(c,4,1,5,4,0xff9a8d73);
    c.restore();
  }
  void hero(Canvas c,float x,float y,float scale,float heading,boolean empowered,boolean equippedHelm) {
    begin(c,x,y,scale,heading);
    px(c,-11,11,23,4,0x77101820);
    px(c,-11,-4,21,17,0xff351f2b);px(c,-10,-3,18,17,0xff6b2e3c);
    px(c,-11,6,4,10,0xff873b48);px(c,-5,8,3,6,0xff9c4852);px(c,6,7,4,9,0xff512735);
    feet(c,0,0xff4c5a68,7);px(c,-8,-7,17,16,OUT);px(c,-6,-5,13,12,IRON);
    px(c,-4,-4,8,7,LIGHT);px(c,-9,-6,5,7,0xff5c6b7a);px(c,6,-6,5,7,0xff5c6b7a);
    px(c,-6,6,13,3,0xff44322d);px(c,-1,6,3,3,0xffceaa61);
    px(c,-8,-20,16,14,OUT);px(c,-6,-18,12,11,IRON);px(c,-4,-20,9,3,LIGHT);
    px(c,-5,-13,11,3,OUT);px(c,-3,-12,3,1,0xffa5d9df);px(c,3,-12,2,1,0xffa5d9df);
    if(equippedHelm) {px(c,-1,-24,3,7,0xffc7a060);px(c,0,-24,5,2,0xffe3c48c);}
    px(c,12,-17,3,27,OUT);px(c,13,-16,1,24,0xffa59771);px(c,10,-5,7,2,LIGHT);
    px(c,10,-19,7,5,empowered?0xffbda0e5:0xff75bacb);px(c,12,-21,3,8,empowered?0xffe1caff:0xffc5edf0);
    c.restore();
  }
  void fantasy(Canvas c,int type,float x,float y,float scale,float phase,boolean attack,float heading) {
    begin(c,x,y,scale,heading);
    int step=Math.round((float)Math.sin(phase)*2);
    if(type==Bestiary.WOLF) {wolf(c,step,attack);c.restore();return;}
    int width=type==Bestiary.ORC?11:7,skin=type==Bestiary.ORC?0xff7f8964:0xff9a946d;
    feet(c,step,0xff44463d,width);
    px(c,-width-1,-7,width*2+2,17,OUT);px(c,-width,-5,width*2,13,type==Bestiary.HEXER?0xff554259:0xff584b3d);
    px(c,-width+2,-5,4,12,type==Bestiary.HEXER?0xff7a5a79:0xff7a684a);
    px(c,-width,7,width*2,3,0xff302928);px(c,-1,7,3,3,0xffc0a064);
    px(c,-8,-20,16,14,OUT);px(c,-6,-18,12,10,skin);
    px(c,-10,-18,4,7,skin);px(c,7,-18,5,5,skin);
    px(c,-5,-14,4,3,OUT);px(c,2,-14,4,3,OUT);px(c,-4,-13,2,1,0xffecc881);px(c,3,-13,2,1,0xffecc881);
    px(c,-3,-8,9,3,OUT);
    if(type==Bestiary.KOBOLD) {
      px(c,-8,-25,4,11,0xff91683f);px(c,4,-26,4,11,0xff91683f);
      px(c,-7,-23,2,6,0xffbc9962);px(c,5,-23,2,6,0xffbc9962);
      px(c,3,-11,8,4,0xffc0a178);px(c,9,-11,3,2,OUT);
      px(c,14,-26,2,42,0xff917957);px(c,12,-28,6,7,LIGHT);px(c,14,-31,2,5,LIGHT);
      px(c,-14,6,8,3,0xff8c6c44);px(c,-16,1,3,7,0xff8c6c44);
    } else if(type==Bestiary.ORC) {
      px(c,-13,-7,8,7,0xff5c6969);px(c,6,-7,9,7,0xff5c6969);px(c,-12,-10,2,5,BONE);px(c,11,-11,2,5,BONE);
      px(c,-7,-21,14,5,0xff4e5551);px(c,-1,-22,3,9,IRON);
      px(c,-4,-9,2,6,BONE);px(c,4,-9,2,6,BONE);
      int yA=attack?-12:0;px(c,17,yA-14,3,29,0xff92724a);px(c,20,yA-16,9,12,IRON);px(c,27,yA-18,3,16,LIGHT);
    } else {
      px(c,-9,-23,18,5,0xff322c3d);px(c,-6,-27,13,7,0xff51405d);px(c,-2,-31,5,6,0xff51405d);
      px(c,-8,-19,3,12,0xff51405d);px(c,7,-19,3,12,0xff51405d);
      px(c,-7,8,4,8,0xff655073);px(c,4,8,5,8,0xff49384f);
      px(c,14,-26,2,43,0xff927953);px(c,11,-28,8,7,0xff624f87);px(c,13,-27,4,4,0xffc9adea);
      if(attack) {px(c,21,-16,5,5,0xffb795d7);px(c,28,-12,2,2,0xffe4d6f3);}
    }
    px(c,-width-4,-3,5,5,skin);px(c,width,attack?-9:-3,5,5,skin);
    c.restore();
  }
  private void wolf(Canvas c,int step,boolean attack) {
    px(c,-23,9,49,4,0x66101820);
    for(int leg=0;leg<4;leg++) {
      int x=leg<2?-12:10,offset=(leg==0||leg==3)?step:-step;
      px(c,x,1,4,11,OUT);px(c,x+offset,7,3,7,leg%2==0?0xff48545b:0xff84918f);px(c,x+offset,13,6,2,BONE);
    }
    px(c,-20,-7,35,15,OUT);px(c,-18,-6,32,12,0xff687577);px(c,-11,-7,21,8,0xff919b94);
    px(c,-25,-12,6,9,0xff687577);px(c,-29,-14,6,5,0xff9ba49a);
    px(c,7,-14,12,16,0xff87968f);px(c,10,-21,4,11,0xff536065);px(c,18,-20,4,12,0xff536065);
    px(c,12,-11,15,10,0xff8a9892);px(c,20,-3,10,5,BONE);px(c,28,-3,4,4,OUT);
    px(c,16,-10,5,3,OUT);px(c,18,-9,2,1,0xffe3b974);
    if(attack) {px(c,21,2,11,5,OUT);px(c,23,2,2,3,BONE);px(c,28,2,2,3,BONE);}
  }
}
