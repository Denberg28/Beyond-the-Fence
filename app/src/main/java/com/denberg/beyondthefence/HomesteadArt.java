package com.denberg.beyondthefence;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/** Weathered pixel masonry and kitchen gardens on the exact interactive boundary. */
final class HomesteadArt {
  private final Paint p=new Paint();
  private final Path path=new Path();
  private void box(Canvas c,float x,float y,float w,float h,int color) {
    p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRect(x,y,x+w,y+h,p);
  }
  private void line(Canvas c,float x,float y,float xx,float yy,float width,int color) {
    p.setColor(color);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.SQUARE);c.drawLine(x,y,xx,yy,p);
  }
  private int shade(int day,int night,float dark,boolean ashen) {
    int value=RaidPresentation.blend(day,night,dark);
    return ashen?RaidPresentation.blend(value,0xff493b46,.23f):value;
  }
  void plot(Canvas c,BoardGeometry.Box tile,boolean open,boolean scorched,float d,float dark,boolean ashen) {
    float x=tile.left,y=tile.top,w=tile.width(),h=tile.height();
    if(!open) {
      box(c,x,y,w,h,shade(0xff303e39,0xff263332,dark,ashen));
      for(int i=0;i<4;i++) line(c,x+(8+i*9)*d,y+h-9*d,x+(10+i*9)*d,y+h-14*d,d,0xff566653);
      box(c,x+w/2-10*d,y+9*d,20*d,12*d,shade(0xff4d5758,0xff39464d,dark,ashen));
      line(c,x+w/2,y+21*d,x+w/2,y+28*d,2*d,0xff514738);return;
    }
    // Low stone edging, damp soil and irregular furrows instead of bright framed cards.
    box(c,x,y+2*d,w,h-2*d,0x7713191e);
    box(c,x+d,y+d,w-2*d,h-4*d,shade(0xff465354,0xff35434b,dark,ashen));
    box(c,x+4*d,y+4*d,w-8*d,h-10*d,scorched?0xff302b2b:shade(0xff4c4035,0xff39352f,dark,ashen));
    for(int row=0;row<4;row++) {
      float ry=y+h*(.19f+row*.18f);
      line(c,x+7*d,ry,x+w-7*d,ry,2*d,scorched?0xff242629:shade(0xff352f29,0xff2a2d2b,dark,ashen));
      line(c,x+9*d,ry-d,x+w-9*d,ry-d,d,shade(0xff66513c,0xff4d4437,dark,ashen));
    }
    for(int edge=0;edge<3;edge++) {
      float at=x+w*(.18f+edge*.31f);
      box(c,at,y+d,6*d,d,shade(0xff69716b,0xff4e5e61,dark,ashen));
      box(c,at+3*d,y+h-5*d,5*d,2*d,shade(0xff59645e,0xff465354,dark,ashen));
    }
    box(c,x+2*d,y+h*.55f,3*d,5*d,shade(0xff465b4a,0xff35483d,dark,ashen));
  }
  void wall(Canvas c,float x,float y,float xx,float yy,float d,int tier,int part,int style,float dark,boolean ashen) {
    float length=(float)Math.hypot(xx-x,yy-y)/d;
    c.save();c.translate(x,y);c.rotate((float)Math.toDegrees(Math.atan2(yy-y,xx-x)));c.scale(d,d);
    box(c,-1,-4,length+2,14,0x66101820);
    if(tier==0) {
      if(part%3==0) {box(c,1,-3,4,5,shade(0xff505859,0xff3b494f,dark,ashen));box(c,6,1,3,4,0xff3e4848);}
    } else if(tier==1) {
      box(c,-1,-5,length+2,11,shade(0xff493e32,0xff37352f,dark,ashen));
      for(int i=0;i<2;i++) {
        float at=(i+.4f)*length/2;
        p.setColor(shade(0xff796145,0xff594f3d,dark,ashen));path.reset();
        path.moveTo(at-3,7);path.lineTo(at-3,-5);path.lineTo(at,-9);path.lineTo(at+3,-5);path.lineTo(at+3,7);path.close();c.drawPath(path,p);
        line(c,at-1,-3,at-1,5,1,shade(0xff9a8059,0xff716348,dark,ashen));
      }
      line(c,0,4,length,4,2,0xff453a30);
    } else {
      box(c,-1,-7,length+2,15,shade(0xff293840,0xff202d35,dark,ashen));
      for(int course=0;course<2;course++) {
        float by=-6+course*6;
        box(c,0,by,length,5,shade(part%3==0?0xff606f74:0xff52636b,part%3==0?0xff495b65:0xff3d4f5b,dark,ashen));
        float joint=course==0?length/3:length*2/3;
        line(c,joint,by,joint,by+5,1,0xff293941);
        line(c,1,by+1,length-2,by+1,1,shade(0xff7b8785,0xff596d73,dark,ashen));
      }
      if(part%2==0) {
        box(c,1,-10,length*.65f,4,shade(0xff57686e,0xff40535f,dark,ashen));
        box(c,2,-10,Math.max(1,length*.65f-2),1,shade(0xff83908b,0xff60737a,dark,ashen));
      }
      if(part%7==3) box(c,length*.3f,4,4,2,shade(0xff435c4c,0xff30483e,dark,ashen));
      if(tier==3) {
        box(c,length/2-1,-7,2,14,0xff24333f);
        box(c,length/2-1,-5,2,2,0xff92938a);box(c,length/2-1,4,2,2,0xff777e78);
      }
    }
    if(tier>0 && part==0) {
      box(c,-4,-8,8,16,shade(tier==1?0xff67533c:0xff596b71,tier==1?0xff4f4536:0xff40545f,dark,ashen));
      if(style==2 && tier>=2) {box(c,-6,-11,12,4,0xff607079);box(c,-6,8,12,3,0xff344750);}
    }
    c.restore();
  }
}
