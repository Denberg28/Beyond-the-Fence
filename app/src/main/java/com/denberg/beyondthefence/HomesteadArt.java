package com.denberg.beyondthefence;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/** Medieval kitchen gardens and wall materials, using the exact interactive boundary geometry. */
final class HomesteadArt {
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF rect=new RectF();
  private final Path path=new Path();
  private void fill(int color) { p.setStyle(Paint.Style.FILL);p.setColor(color); }
  private void box(Canvas c,float x,float y,float w,float h,float radius,int color) {
    fill(color);rect.set(x,y,x+w,y+h);c.drawRoundRect(rect,radius,radius,p);
  }
  private void line(Canvas c,float x,float y,float xx,float yy,float width,int color) {
    fill(color);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.SQUARE);c.drawLine(x,y,xx,yy,p);
  }
  void plot(Canvas c,BoardGeometry.Box tile,boolean open,boolean scorched,float d) {
    float x=tile.left,y=tile.top,w=tile.width(),h=tile.height();
    if(!open) {
      box(c,x,y,w,h,3*d,0xff7b8f53);
      for(int i=0;i<4;i++) line(c,x+(8+i*9)*d,y+h-9*d,x+(10+i*9)*d,y+h-14*d,d,0xffa2b36b);
      box(c,x+w/2-12*d,y+7*d,24*d,15*d,2*d,0xffa18454);
      line(c,x+w/2,y+22*d,x+w/2,y+29*d,3*d,0xff715239);return;
    }
    box(c,x+d,y+3*d,w-2*d,h-3*d,4*d,0x66342a21);
    box(c,x+d,y+d,w-2*d,h-4*d,3*d,0xff705033);
    box(c,x+4*d,y+4*d,w-8*d,h-10*d,2*d,scorched?0xff44372c:0xff78573a);
    line(c,x+2*d,y+2*d,x+w-2*d,y+2*d,2*d,0xffc69b63);
    line(c,x+2*d,y+h-4*d,x+w-2*d,y+h-4*d,3*d,0xff987047);
    for(int row=0;row<4;row++) {
      float ry=y+h*(.17f+row*.18f);
      line(c,x+6*d,ry,x+w-6*d,ry,3*d,scorched?0xff352d28:0xff5b3c29);
      line(c,x+7*d,ry-d,x+w-7*d,ry-d,d,scorched?0xff66503a:0xffa97d4e);
    }
    for(int corner=0;corner<4;corner++) {
      float cx=corner%2==0?x+2*d:x+w-5*d,cy=corner<2?y+2*d:y+h-7*d;
      box(c,cx,cy,3*d,4*d,d,0xffbdab82);
    }
  }
  void wall(Canvas c,float x,float y,float xx,float yy,float d,int tier,int part,int style) {
    float length=(float)Math.hypot(xx-x,yy-y)/d;
    c.save();c.translate(x,y);c.rotate((float)Math.toDegrees(Math.atan2(yy-y,xx-x)));c.scale(d,d);
    box(c,-1,-5,length+2,14,2,0x55342923);
    if(tier==0) {
      if(part%3==0) {box(c,1,-3,4,5,1,0xff8c7960);box(c,6,1,3,4,1,0xffa18c69);}
    } else if(tier==1) {
      box(c,-1,-6,length+2,12,1,0xff63462f);
      for(int i=0;i<2;i++) {
        float at=(i+.4f)*length/2;
        fill(0xffa77c48);path.reset();path.moveTo(at-3,7);path.lineTo(at-3,-5);path.lineTo(at,-10);path.lineTo(at+3,-5);path.lineTo(at+3,7);path.close();c.drawPath(path,p);
        line(c,at-1,-4,at-1,6,1,0xffd2a168);line(c,at+2,-4,at+2,6,1,0xff775334);
      }
      line(c,0,4,length,4,2,0xff6e4b32);
    } else {
      box(c,-1,-7,length+2,14,1,tier==2?0xff717a78:0xff576b73);
      for(int course=0;course<2;course++) {
        float by=-6+course*6;
        box(c,0,by,length,5,1,tier==2?0xffa3aaa0:0xff9eafb3);
        line(c,course==0?length/3:length*2/3,by,course==0?length/3:length*2/3,by+5,1,0xff5b6666);
        line(c,1,by+1,length-1,by+1,1,tier==2?0xffc6c9b7:0xffd0d9cd);
      }
      if(part%2==0) box(c,1,-11,length*.65f,5,1,0xff9eaaa3);
      if(tier==3) {
        box(c,length/2-1,-8,2,16,0,0xff3e5159);
        box(c,length/2-1,-5,2,2,0,0xffd7b474);box(c,length/2-1,4,2,2,0,0xffd7b474);
      }
    }
    if(tier>0 && part==0) {
      box(c,-5,-8,10,16,2,tier==1?0xff86643c:0xff818f8c);
      if(style==2 && tier>=2) {box(c,-7,-12,14,5,1,0xffabb5a4);box(c,-7,9,14,4,1,0xff667877);}
    }
    c.restore();
  }
}
