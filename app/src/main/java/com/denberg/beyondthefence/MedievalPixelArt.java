package com.denberg.beyondthefence;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/** Original deterministic pixel-style details, independent of save state and touch geometry. */
final class MedievalPixelArt {
  private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Path outline = new Path();
  private RadialGradient torchLight;
  private float lightRadius;
  private boolean lightNight;
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
    c.drawColor(World.ground(ashen ? 2 : 1, night));
    float step=24*d;
    // Broken paving interspersed with moss/ash. No simulation RNG or animation clock.
    for(int row=0;row*step<height;row++) for(int col=0;col*step<width;col++) {
      int hash=(col*17+row*29+col*row*3)&31;
      float x=col*step+(row%2)*step*.5f, y=row*step;
      int stone=ashen ? (night?0xff24212c:0xff39313a) : (night?0xff202d36:0xff35434a);
      if(hash%3!=0) {
        float inset=(3+hash%5)*d;
        tile(c,x+inset,y+5*d,(10+hash%8)*d,(6+hash%5)*d,stone);
        tile(c,x+inset+2*d,y+4*d,(6+hash%7)*d,d,ashen?0xff51404a:0xff45515a);
        if(hash%4==1) line(c,x+inset+5*d,y+5*d,x+inset+7*d,y+9*d,d,0xff17222b);
      }
      if(hash%4==0) {
        int moss=ashen?0xff4b383b:night?0xff263c3b:0xff3c5148;
        tile(c,x+3*d,y+16*d,9*d,3*d,moss);
        tile(c,x+7*d,y+13*d,3*d,8*d,moss);
      }
      if(hash==9 || hash==19) {
        line(c,x+5*d,y+18*d,x+7*d,y+12*d,d,ashen?0xff75604c:0xff65745b);
        line(c,x+7*d,y+16*d,x+11*d,y+14*d,d,ashen?0xff75604c:0xff65745b);
      }
    }
    // Low-contrast pine silhouettes frame only the outer margins, behind roaming enemies.
    for(int side=0;side<2;side++) for(int i=0;i<5;i++) {
      float x=side==0?4*d:width-4*d, y=(35+i*83)*d;
      if(y>height-18*d) continue;
      int shade=ashen?0xff171820:night?0xff101c24:0xff202f33;
      tile(c,x-d,y-14*d,2*d,24*d,shade);
      for(int tier=0;tier<3;tier++) {
        float half=(4+tier*3)*d, yy=y+(-16+tier*7)*d;
        outline.reset();outline.moveTo(x,yy-7*d);outline.lineTo(x-half,yy+6*d);
        outline.lineTo(x+half,yy+6*d);outline.close();
        p.setColor(shade);c.drawPath(outline,p);
      }
    }
  }

  void courtyard(Canvas c,Path boundary,int width,int height,float d,boolean night,boolean ashen) {
    c.save();c.clipPath(boundary);
    tile(c,0,0,width,height,ashen?(night?0xff312d35:0xff443a3e):(night?0xff343b40:0xff4c504c));
    float size=20*d;
    for(int row=0;row*size<height;row++) for(int col=0;col*size<width;col++) {
      float x=col*size+(row%2)*size/2,y=row*size;
      int h=(row*13+col*7)%7;
      int stone=ashen ? (h<3?0xff4d4146:0xff413b42) : night ? (h<3?0xff41464a:0xff394044) : (h<3?0xff5b5c53:0xff51554f);
      tile(c,x+d,y+d,size-2*d,size-2*d,stone);
      line(c,x+3*d,y+2*d,x+size-4*d,y+2*d,d,night?0xff515456:0xff6e6c5e);
      if(h==2) {
        line(c,x+9*d,y+4*d,x+7*d,y+10*d,d,0xff303639);
        line(c,x+7*d,y+10*d,x+11*d,y+14*d,d,0xff303639);
      }
      if(h==5) tile(c,x+2*d,y+size-4*d,6*d,2*d,ashen?0xff584047:0xff42574b);
    }
    c.restore();
  }
  void torch(Canvas c,float x,float y,float d,boolean night) {
    // Cached warm falloff avoids shader allocations in the active frame loop.
    float radius=(night?42:25)*d;
    if(torchLight==null || radius!=lightRadius || night!=lightNight) {
      lightRadius=radius;lightNight=night;
      torchLight=new RadialGradient(0,0,radius,
          new int[]{night?0x66f6b95e:0x35f6b95e,0x19d98a3d,0x00d98a3d},
          new float[]{0,.4f,1},Shader.TileMode.CLAMP);
    }
    c.save();c.translate(x,y);p.setColor(0xffffffff);p.setShader(torchLight);
    c.drawCircle(0,0,radius,p);p.setShader(null);c.restore();
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
  void atmosphere(Canvas c,int width,int height,float d,boolean night,boolean ashen) {
    // Stepped darkness supplies depth without obscuring threats near the fortress.
    for(int band=0;band<5;band++) {
      float edge=(5-band)*4*d;
      int shade=night?0x14060911:0x09060911;
      tile(c,0,0,width,edge,shade);tile(c,0,height-edge,width,edge,shade);
      tile(c,0,edge,edge,height-2*edge,shade);tile(c,width-edge,edge,edge,height-2*edge,shade);
    }
    if(ashen) for(int i=0;i<8;i++) {
      float x=((i*97+23)*d)%Math.max(1,width), y=((i*113+59)*d)%Math.max(1,height);
      tile(c,x,y,2*d,d,night?0xffc68755:0xff9a654b);
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
