package com.denberg.beyondthefence;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/** Original Canvas bosses. Bounded geometry, no bitmap assets, particles or wall-clock timers. */
final class BossArt {
  private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Path path = new Path();
  private final RectF rect = new RectF();
  private final FantasyArt fantasy = new FantasyArt();
  private void color(int value) { p.setColor(value); p.setStyle(Paint.Style.FILL); }
  private void oval(Canvas c, float x, float y, float rx, float ry, int value) {
    color(value); rect.set(x-rx,y-ry,x+rx,y+ry); c.drawOval(rect,p);
  }
  private void box(Canvas c, float x, float y, float w, float h, int value) {
    color(value); rect.set(x,y,x+w,y+h); c.drawRoundRect(rect,2,2,p);
  }
  private void line(Canvas c, float x, float y, float xx, float yy, float width, int value) {
    color(value); p.setStrokeWidth(width); p.setStrokeCap(Paint.Cap.ROUND); c.drawLine(x,y,xx,yy,p);
  }
  private void tri(Canvas c, float x, float y, float xx, float yy, float xxx, float yyy, int value) {
    color(value); path.reset();path.moveTo(x,y);path.lineTo(xx,yy);path.lineTo(xxx,yyy);path.close();c.drawPath(path,p);
  }
  private void face(Canvas c, float x, float y, float radius, int skin, boolean tusks) {
    oval(c,x,y,radius,radius*.85f,skin);
    line(c,x-4,y-2,x-1,y-1,1.8f,0xffffd77b);line(c,x+2,y-1,x+5,y-2,1.8f,0xffffd77b);
    box(c,x-4,y+2,8,3,0xff322e36);
    if(tusks) {tri(c,x-5,y+5,x-5,y-1,x-2,y+4,0xfff3e5bf);tri(c,x+2,y+4,x+5,y-1,x+5,y+5,0xfff3e5bf);}
  }
  void draw(Canvas c, int kind, float x, float y, float scale, float phase, boolean attacking, float heading) {
    c.save();c.translate(x,y);c.scale(scale,scale);
    float gait=(float)Math.sin(phase), lift=attacking?-5:0;
    oval(c,0,14,22,5,0x6630272b);
    if(kind==BossRoster.DRAGON) {
      if(Math.cos(Math.toRadians(heading))<0)c.scale(-1,1);
      float flap=gait*3;
      tri(c,-7,-3,-26,-29-flap,-30,-5,0xff6f303e);
      tri(c,-7,-3,-26,-29-flap,-12,-14,0xffba6853);
      tri(c,3,-5,12,-33+flap,26,-9,0xff9b4546);
      line(c,3,-5,12,-33+flap,1.5f,0xffd8966e);
      line(c,-14,3,-29,7,5,0xff754947);tri(c,-29,7,-35,1,-33,12,0xffdbac67);
      for(int i=0;i<2;i++) {float xx=-8+i*17;line(c,xx,3,xx+gait*(i==0?2:-2),13,5,0xffa95842);line(c,xx-2,14,xx+5,14,2,0xffe6c18a);}
      oval(c,0,0,17,10,0xffb76649);oval(c,4,3,10,6,0xffddae6c);
      for(int i=0;i<4;i++)tri(c,-13+i*7,-7,-10+i*7,-14,-7+i*7,-7,0xffe3b46d);
      line(c,12,-1,17,-13+lift,10,0xffb76649);face(c,21,-14+lift,8,0xffc87a50,true);
      box(c,23,-12+lift,10,5,0xffdf9b60);oval(c,31,-12+lift,1,1,0xff493436);
      tri(c,15,-19+lift,11,-27+lift,20,-20+lift,0xffe9ca88);
    } else if(kind==BossRoster.WOLF) {
      c.save();c.scale(1.25f,1.25f);fantasy.enemy(c,Bestiary.WOLF,0,1,1,phase,attacking,heading);c.restore();
      for(int i=0;i<4;i++)tri(c,-11+i*6,-7,-9+i*6,-18-(i%2)*3,-5+i*6,-7,0xffa5e2ed);
      oval(c,0,-2,11,4,0x55cbf1ff);
    } else if(kind==BossRoster.SNAKE) {
      float sway=gait*3;
      oval(c,0,8,24,10,0xff314f43);oval(c,0,8,16,6,0xff6c9b65);oval(c,1,8,9,3,0xff263e38);
      line(c,11,6,5+sway,-5,10,0xff709a58);line(c,5+sway,-5,1+sway,-17+lift,10,0xff95b16a);
      oval(c,1+sway,-18+lift,15,12,0xff3b7152);oval(c,1+sway,-18+lift,9,10,0xffbfbd75);
      face(c,1+sway,-21+lift,8,0xff8bb16a,true);
      line(c,1+sway,-14+lift,1+sway,-9+lift,1,0xffe97069);
      for(int i=0;i<6;i++)oval(c,-16+i*6,6,1.5f,2,0xffd1b969);
    } else if(kind==BossRoster.LICH || kind==BossRoster.WARDEN) {
      boolean lich=kind==BossRoster.LICH;
      tri(c,-17,15,0,-22,17,15,lich?0xff44375f:0xff562f43);
      tri(c,-9,13,0,-12,9,13,lich?0xff71628d:0xff91485a);
      box(c,-12,-10,24,10,lich?0xff736386:0xff77838b);box(c,-4,-9,8,17,0xffb7a16b);
      line(c,-12,-6,-21,2+gait,4,0xffcbc6b1);line(c,12,-6,20,-11+lift,4,0xffcbc6b1);
      line(c,21,15,21,-28,2.5f,0xff8b7663);oval(c,21,-28,5,6,0xff96dce8);
      oval(c,0,-18,8,8,0xffdad4bc);oval(c,-3,-19,2,2.5f,0xff434451);oval(c,3,-19,2,2.5f,0xff434451);
      oval(c,-3,-19,1,1,0xff94e8ff);oval(c,3,-19,1,1,0xff94e8ff);tri(c,0,-17,-2,-14,2,-14,0xff55515b);
      for(int i=0;i<4;i++)line(c,-4+i*2,-12,-4+i*2,-10,1,0xff514856);
      box(c,-9,-26,18,3,0xffcba65f);
      for(int i=0;i<3;i++)tri(c,-9+i*6,-25,-7+i*6,-33-(i==1?3:0),-3+i*6,-25,0xffdfbe78);
      if(lich) {oval(c,-22,2+gait,4,4,0x667fcfe7);oval(c,-22,2+gait,2,2,0xffc4f5ff);}
      else {line(c,-20,-7,-20,14,4,0xffb9c5c6);line(c,-26,5,-14,5,2,0xffdfbc70);}
    } else {
      boolean ogre=kind==BossRoster.OGRE, yeti=kind==BossRoster.YETI;
      int skin=ogre?0xff9da36a:yeti?0xffdce8e9:0xff91a38c;
      for(int i=0;i<2;i++) {float xx=-7+i*14;line(c,xx,5,xx+gait*(i==0?2:-2),15,7,yeti?0xff9eb9c5:0xff4c4244);}
      oval(c,0,-2,ogre?19:18,17,skin);oval(c,0,1,12,10,yeti?0xffafcbd4:ogre?0xffb7b47c:0xff627b6e);
      line(c,-15,-6,-23,6+lift,8,skin);line(c,15,-6,24,4+lift,8,skin);
      oval(c,-23,6+lift,5,5,skin);oval(c,24,4+lift,5,5,skin);
      if(ogre) {
        face(c,-8,-23,9,skin,true);face(c,10,-22,8,0xffb5ad75,true);
        box(c,-18,7,36,6,0xff6c4b38);line(c,25,12+lift,28,-16+lift,4,0xff81624b);oval(c,28,-18+lift,7,10,0xff645e58);
        tri(c,-15,-27,-18,-34,-11,-29,0xffe3d8ab);tri(c,14,-27,19,-33,17,-24,0xffe3d8ab);
      } else if(yeti) {
        for(int i=0;i<6;i++)tri(c,-18+i*6,-9,-17+i*6,-19,-12+i*6,-7,0xffeef5ef);
        face(c,0,-23,10,0xff87acbf,true);oval(c,0,-30,10,5,0xfff2f5e9);
        for(int i=0;i<4;i++)tri(c,-12+i*7,6,-9+i*7,18,-5+i*7,6,0xffeaf2ed);
      } else {
        box(c,-18,-9,8,13,0xff534b62);box(c,9,-8,9,14,0xff65506a);box(c,-17,8,34,5,0xff504145);
        face(c,0,-25,11,skin,false);box(c,-10,-35,18,5,0xff41483e);
        line(c,-7,-27,5,-22,1,0xff4a5850);line(c,-5,0,7,3,2,0xffb59d84);
        for(int i=0;i<3;i++)line(c,-3+i*4,-1,-4+i*4,4,1,0xff4a4848);
      }
    }
    c.restore();
  }
  /** A short local effect to the actual contact point; never obscures the full screen. */
  void effect(Canvas c, int element, float x, float y, float tx, float ty, float d, float progress) {
    if(progress<0 || progress>1 || d<=0)return;
    float dx=tx-x,dy=ty-y, length=(float)Math.hypot(dx,dy);
    // Close-range contact effects reach at least 14dp, while long interior rays stay <=56dp.
    float angle=(float)Math.atan2(dy,dx), reach=Math.max(14*d,Math.min(56*d,length));
    float ex=x+(float)Math.cos(angle)*reach,ey=y+(float)Math.sin(angle)*reach;
    if(element==BossRoster.LIGHTNING) {
      float px=x,py=y;
      for(int i=1;i<=6;i++) {
        float t=i/6f,off=i==6?0:(i%2==0?-1:1)*4*d;
        float xx=x+(ex-x)*t-(float)Math.sin(angle)*off,yy=y+(ey-y)*t+(float)Math.cos(angle)*off;
        line(c,px,py,xx,yy,4*d,0x887ebce9);line(c,px,py,xx,yy,1.4f*d,0xffd6f6ff);px=xx;py=yy;
      }
      oval(c,ex,ey,(3+progress*4)*d,(3+progress*4)*d,0x7778cbee);
    } else if(element==BossRoster.FIRE) {
      for(int i=0;i<6;i++) {
        float t=(i+progress)/6f, xx=x+(ex-x)*t,yy=y+(ey-y)*t;
        oval(c,xx,yy,(3+4*t)*d,(3+4*t)*d,0xbbe7793e);
        oval(c,xx,yy,(1+2*t)*d,(1+2*t)*d,0xfff7d77d);
      }
    } else {
      for(int i=0;i<5;i++) {
        float a=i*1.2566f,spread=(4+progress*13)*d;
        float xx=ex+(float)Math.cos(a)*spread,yy=ey+(float)Math.sin(a)*spread-progress*(1-progress)*14*d;
        tri(c,xx-3*d,yy+2*d,xx,yy-4*d,xx+4*d,yy+2*d,0xffa5a18d);
        line(c,xx,yy-2*d,xx+2*d,yy,1*d,0xffd8cdb3);
      }
      color(0x887e7568);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d);
      c.drawCircle(ex,ey,(4+progress*14)*d,p);p.setStyle(Paint.Style.FILL);
    }
  }
}
