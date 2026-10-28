package com.denberg.beyondthefence;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/** Original small, readable fantasy silhouettes; all motion uses simulation time. */
final class FantasyArt {
  private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Path path = new Path();
  private final RectF rect = new RectF();
  private void fill(int color) { p.setColor(color); p.setStyle(Paint.Style.FILL); }
  private void box(Canvas c, float x, float y, float w, float h, int color) {
    fill(color); rect.set(x,y,x+w,y+h); c.drawRoundRect(rect,2,2,p);
  }
  private void oval(Canvas c, float x, float y, float rx, float ry, int color) {
    fill(color); rect.set(x-rx,y-ry,x+rx,y+ry); c.drawOval(rect,p);
  }
  private void line(Canvas c, float x, float y, float xx, float yy, float width, int color) {
    fill(color);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,p);
  }
  private void triangle(Canvas c, float x, float y, float xx, float yy, float xxx, float yyy, int color) {
    fill(color);path.reset();path.moveTo(x,y);path.lineTo(xx,yy);path.lineTo(xxx,yyy);path.close();c.drawPath(path,p);
  }
  void enemy(Canvas c, int type, float x, float y, float scale, float phase, boolean attack, float heading) {
    c.save();c.translate(x,y);c.scale(scale,scale);
    float gait=(float)Math.sin(phase);
    if(type==Bestiary.WOLF) {
      // A side-view animal must stay upright; rotating it through 360 degrees inverted its legs.
      // Mirror toward travel/target, with diagonal trot pairs and grounded paws.
      if (Math.cos(Math.toRadians(heading)) < 0) c.scale(-1,1);
      wolf(c,gait,attack);
    } else {
      oval(c,0,10,type==Bestiary.ORC?13:10,4,0x55302221);
      if(type==Bestiary.KOBOLD) kobold(c,gait,attack);
      else if(type==Bestiary.ORC) orc(c,gait,attack);
      else hexer(c,gait,attack);
    }
    c.restore();
  }
  private void kobold(Canvas c,float gait,boolean attack) {
    // Canine snout, tall ears, hooked tail and spear distinguish the kobold from undead.
    line(c,-5,5,-15,9,3,0xff6a422d);line(c,-15,9,-17,3,2,0xffbe8a4e);
    line(c,-3,4,-5-gait*2,12,4,0xff483a30);line(c,3,4,5+gait*2,12,4,0xff483a30);
    box(c,-7,-6,14,14,0xff714d35);box(c,-5,-5,10,10,0xff9a6a3d);
    line(c,-5,-4,5,6,2,0xffdec596);box(c,-7,3,14,3,0xff433d32);
    line(c,7,-4,12,attack?-10:1,3,0xffbc8952);
    line(c,13,12,13,attack?-22:-17,2,0xffa78754);
    triangle(c,10,attack?-21:-16,13,attack?-28:-23,16,attack?-21:-16,0xffc3c5b8);
    oval(c,0,-11,7,6,0xffb47e46);
    triangle(c,-7,-14,-7,-23,-1,-15,0xffa16b3a);triangle(c,2,-15,7,-23,7,-12,0xffa16b3a);
    triangle(c,-5,-15,-5,-20,-2,-15,0xffe8b879);triangle(c,3,-15,6,-20,6,-14,0xffe8b879);
    oval(c,3,-8,6,3.5f,0xffdfb478);oval(c,8,-8,1.6f,1.4f,0xff342d2a);
    oval(c,-2,-12,1.3f,1.5f,0xffffdf82);oval(c,3,-12,1.3f,1.5f,0xffffdf82);
    line(c,-2,-11,-2,-13,1,0xff292a22);line(c,3,-11,3,-13,1,0xff292a22);
  }
  private void orc(Canvas c,float gait,boolean attack) {
    line(c,-5,6,-7-gait,14,6,0xff3e3b32);line(c,5,6,7+gait,14,6,0xff3e3b32);
    box(c,-12,-7,24,18,0xff414a3b);box(c,-9,-5,18,13,0xff647449);
    box(c,-14,-8,9,8,0xff626d6b);box(c,5,-8,10,8,0xff626d6b);
    triangle(c,-12,-7,-10,-13,-8,-7,0xffc6c3a5);triangle(c,8,-7,10,-13,12,-7,0xffc6c3a5);
    line(c,-11,-1,-15,7,5,0xff7f9554);line(c,11,-1,17,attack?-10:6,5,0xff7f9554);
    float axe=attack?-8:6;line(c,18,axe+9,18,axe-15,3,0xff9c784a);
    triangle(c,17,axe-13,27,axe-18,27,axe-5,0xffb4b8a7);line(c,27,axe-17,27,axe-6,2,0xffe4dfbf);
    box(c,-10,5,20,4,0xff45342b);box(c,-2,5,5,4,0xffc49b50);
    oval(c,0,-12,9,7,0xff8ca563);triangle(c,-7,-13,-14,-16,-9,-8,0xff788f53);triangle(c,7,-13,14,-16,9,-8,0xff788f53);
    box(c,-7,-19,14,5,0xff4f5547);box(c,-2,-20,4,7,0xff929784);
    line(c,-5,-12,-2,-12,2,0xffedcc7d);line(c,3,-12,6,-12,2,0xffedcc7d);
    oval(c,0,-7,6,3,0xff4b5436);triangle(c,-5,-6,-5,-13,-2,-7,0xfff0e0b2);triangle(c,3,-7,5,-13,5,-6,0xfff0e0b2);
  }
  private void wolf(Canvas c,float gait,boolean attack) {
    oval(c,-1,4,20,6,0x55302221);
    line(c,-12,-1,-24,-8,5,0xff525a62);triangle(c,-24,-8,-29,-11,-25,-2,0xffabb0ad);
    for(int i=0;i<4;i++) {
      boolean far = i % 2 == 0;
      float x=i<2?-10:9, y=far?-2:4;
      float step=((i==0 || i==3)?1:-1)*gait*4;
      float lift=Math.max(0, step)*.5f;
      int fur=far?0xff48545f:0xff78848c;
      line(c,x,y,x+step*.5f,y+5-lift,3.5f,fur);
      line(c,x+step*.5f,y+5-lift,x+step,y+10-lift,3,fur);
      line(c,x+step,y+10-lift,x+step+3,y+10-lift,2,0xffc5c9bf);
    }
    oval(c,-1,0,17,8,0xff717b83);oval(c,2,-2,12,5,0xff9ca5a6);
    triangle(c,4,-5,8,-13,12,-5,0xffd5d7ca);triangle(c,8,-5,12,-12,16,-4,0xff8c969a);
    oval(c,15,attack?-1:-3,8,6,0xff8f9b9e);
    triangle(c,12,-7,12,-17,17,-8,0xff59646c);triangle(c,18,-7,22,-15,23,-4,0xff59646c);
    box(c,18,0,10,4,0xffc3c7bb);oval(c,28,0,2,2,0xff30363a);
    oval(c,18,-5,1.4f,1.3f,0xffffd36f);line(c,19,3,26,3,1.4f,0xff343a3e);
    if(attack) { triangle(c,22,3,24,8,25,3,0xfff5e6c0);triangle(c,26,3,27,7,28,3,0xfff5e6c0); }
  }
  private void hexer(Canvas c,float gait,boolean attack) {
    triangle(c,-10,11,0,-12,10,11,0xff4d3f65);triangle(c,-6,9,0,-9,6,9,0xff77608b);
    line(c,-6,-1,-12,4+gait,3,0xff6f8c53);line(c,6,-1,12,attack?-8:1,3,0xff6f8c53);
    line(c,14,13,14,-22,2.5f,0xff8d7150);oval(c,14,-22,5,5,0x446f9deb);oval(c,14,-22,3,3,0xffcfb5ff);
    triangle(c,-9,-10,0,-23,9,-10,0xff4c405e);oval(c,0,-10,6,5,0xff809961);
    triangle(c,-5,-11,-14,-15,-8,-7,0xff809961);triangle(c,5,-11,12,-15,8,-7,0xff809961);
    line(c,-4,-10,-2,-10,1.7f,0xffffd78b);line(c,2,-10,4,-10,1.7f,0xffffd78b);
    triangle(c,-1,-9,2,-6,4,-9,0xffa8b879);box(c,-6,4,12,2,0xffb595d2);
    if(attack) { oval(c,20,-15,3,3,0xffc5a4ef);oval(c,25,-12,1.5f,1.5f,0xffefe4ff); }
  }
}
