package com.denberg.beyondthefence;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/** Original deterministic pixel-style details, independent of save state and touch geometry. */
final class MedievalPixelArt {
  private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Path outline = new Path();
  private final RectF r = new RectF();
  private void tile(Canvas c,float x,float y,float w,float h,int color) {
    p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawRect(x,y,x+w,y+h,p);
  }
  private void line(Canvas c,float x,float y,float xx,float yy,float width,int color) {
    p.setColor(color);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.SQUARE);c.drawLine(x,y,xx,yy,p);
  }
  private void glow(Canvas c,float x,float y,float radius,int color) {
    p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawCircle(x,y,radius,p);
  }
  /** Draw only scenery; never draws touch surfaces or changes geometry. */
  void ground(Canvas c,int width,int height,float d,boolean night,boolean ashen) {
    c.drawColor(ashen ? (night?0xff12131b:0xff29242a) : (night?0xff101c24:0xff25313a));
    float step=18*d;
    for(int row=0;row*step<height;row++) for(int col=0;col*step<width;col++) {
      float x=col*step+(row%2)*step*.5f, y=row*step;
      int hash=(col*17+row*29+col*row*3)&15;
      int stone=hash<4?0xff29333d:hash<10?0xff222b35:0xff1d2630;
      if(ashen) stone=hash<4?0xff3a3034:hash<10?0xff302a32:0xff25232b;
      tile(c,x+1*d,y+1*d,step-2*d,step-2*d,stone);
      if(hash==2 || hash==11) {
        line(c,x+4*d,y+6*d,x+9*d,y+8*d,d,0xff48505a);
        line(c,x+9*d,y+8*d,x+9*d,y+12*d,d,0xff48505a);
      }
      if(hash==7) tile(c,x+3*d,y+3*d,3*d,d,0xff45515d);
    }
    // Small architectural fragments suggest a ruined fortress without hiding the farm.
    for(int i=0;i<9;i++) {
      float x=((i*97+19)*d)%Math.max(1,width-12*d);
      float y=((i*113+47)*d)%Math.max(1,height-12*d);
      tile(c,x,y,9*d,4*d,0xff111a23);
      tile(c,x+d,y+d,6*d,2*d,i%2==0?0xff62605b:0xff4a5257);
    }
  }
  void courtyard(Canvas c,Path boundary,int width,int height,float d,boolean night,boolean ashen) {
    c.save();c.clipPath(boundary);
    tile(c,0,0,width,height,ashen?0xff40353a:night?0xff3b3c40:0xff55514a);
    float size=14*d;
    for(int row=0;row*size<height;row++) for(int col=0;col*size<width;col++) {
      float x=col*size+(row%2)*size/2,y=row*size;
      int h=(row*13+col*7)%5;
      tile(c,x+d,y+d,size-2*d,size-2*d,h==0?0xff605951:h==1?0xff48454a:0xff514c4b);
      if(h==2) line(c,x+3*d,y+9*d,x+9*d,y+9*d,d,0xff343640);
    }
    c.restore();
  }
  void torch(Canvas c,float x,float y,float d,boolean night) {
    // Dark iron mount, pixel-stepped amber halo, and an original blue-core option.
    glow(c,x,y,night?12*d:8*d,night?0x225df4ab:0x166bdab3);
    tile(c,x-3*d,y-2*d,6*d,6*d,0xff11151d);
    tile(c,x-2*d,y-5*d,4*d,5*d,0xffcb6a2b);
    tile(c,x-d,y-8*d,2*d,6*d,0xffffb14b);
    tile(c,x,y-7*d,d,3*d,0xffffe19a);
    tile(c,x-4*d,y+4*d,8*d,2*d,0xff7f6950);
  }
  void banner(Canvas c,float x,float y,float d) {
    tile(c,x-5*d,y-2*d,10*d,2*d,0xffa68c60);
    tile(c,x-4*d,y,8*d,13*d,0xff622b39);
    tile(c,x-3*d,y+2*d,6*d,9*d,0xff8f3945);
    outline.reset();outline.moveTo(x-4*d,y+13*d);outline.lineTo(x,y+18*d);
    outline.lineTo(x+4*d,y+13*d);outline.close();
    p.setColor(0xff622b39);p.setStyle(Paint.Style.FILL);c.drawPath(outline,p);
    tile(c,x-d,y+4*d,2*d,6*d,0xffc59c5e);
    tile(c,x-3*d,y+6*d,6*d,2*d,0xffc59c5e);
  }
  void architecture(Canvas c,float left,float top,float right,float bottom,float d,boolean night) {
    // Decorative corner bastions are inside the existing fort perimeter.
    for(int corner=0;corner<4;corner++) {
      float x=corner%2==0?left:right, y=corner<2?top:bottom;
      tile(c,x-7*d,y-7*d,14*d,14*d,0xff171e27);
      tile(c,x-5*d,y-5*d,10*d,10*d,0xff758087);
      tile(c,x-3*d,y-3*d,6*d,6*d,0xff404850);
      for(int b=0;b<3;b++) tile(c,x+(b-1)*5*d,y-8*d,3*d,2*d,0xff9a9990);
    }
    if(right-left>75*d && bottom-top>75*d) {
      banner(c,(left+right)*.5f,top+10*d,d);
      torch(c,left+14*d,top+17*d,d,night);
      torch(c,right-14*d,top+17*d,d,night);
      torch(c,left+14*d,bottom-15*d,d,night);
      torch(c,right-14*d,bottom-15*d,d,night);
    }
  }
  /** Distinct red-cloaked plated hero, oriented in the top-down arena world. */
  void hero(Canvas c,float x,float y,float d,float heading,boolean empowered) {
    c.save();c.translate(x,y);c.rotate((float)Math.toDegrees(heading)+(float)90);
    c.scale(d,d);
    tile(c,-9,8,18,3,0x7710181e);
    outline.reset();outline.moveTo(-7,0);outline.lineTo(-12,13);
    outline.lineTo(0,9);outline.lineTo(11,13);outline.lineTo(7,0);outline.close();
    p.setStyle(Paint.Style.FILL);p.setColor(0xff782e3e);c.drawPath(outline,p);
    tile(c,-5,3,4,8,0xff293540);tile(c,2,3,4,8,0xff293540);
    tile(c,-7,-5,14,12,0xff19242f);
    tile(c,-5,-5,10,9,0xff7a8791);
    tile(c,-3,-4,6,5,0xffb0b8b8);
    tile(c,-9,-5,4,7,0xff596a75);tile(c,5,-5,4,7,0xff596a75);
    tile(c,-7,-13,14,9,0xff87919c);
    tile(c,-5,-15,10,4,0xffc3c5bd);
    tile(c,-4,-10,8,3,0xff111d2b);
    tile(c,-2,-10,4,2,0xff9cd4e6);
    tile(c,-2,1,4,2,0xffcda85d);
    line(c,10,-2,15,-14,2,0xffa3adb4);
    tile(c,13,-16,4,4,empowered?0xffd3bbff:0xff7acdeb);
    glow(c,15,-14,4,empowered?0x559a76ea:0x4457c9e9);
    c.restore();
  }
}
