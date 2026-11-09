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
  private final PixelCharacters pixels=new PixelCharacters();
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
  void ground(Canvas c,int width,int height,float d,boolean night,boolean ashen) {ground(c,width,height,d,night?1:0,ashen);}
  void ground(Canvas c,int width,int height,float d,float dark,boolean ashen) {
    c.drawColor(RaidPresentation.blend(World.ground(ashen?2:1,false),World.ground(ashen?2:1,true),dark));
    float step=24*d;
    // Broken paving interspersed with moss/ash. No simulation RNG or animation clock.
    for(int row=0;row*step<height;row++) for(int col=0;col*step<width;col++) {
      int hash=(col*17+row*29+col*row*3)&31;
      float x=col*step+(row%2)*step*.5f, y=row*step;
      int stone=ashen ? RaidPresentation.blend(0xff39313a,0xff24212c,dark) : RaidPresentation.blend(0xff35434a,0xff202d36,dark);
      if(hash%3!=0) {
        float inset=(3+hash%5)*d;
        tile(c,x+inset,y+5*d,(10+hash%8)*d,(6+hash%5)*d,stone);
        tile(c,x+inset+2*d,y+4*d,(6+hash%7)*d,d,ashen?RaidPresentation.blend(0xff51404a,0xff3d333e,dark):RaidPresentation.blend(0xff45515a,0xff35424b,dark));
        if(hash%4==1) line(c,x+inset+5*d,y+5*d,x+inset+7*d,y+9*d,d,0xff17222b);
      }
      if(hash%4==0) {
        int moss=ashen?RaidPresentation.blend(0xff4b383b,0xff352d34,dark):RaidPresentation.blend(0xff3c5148,0xff263c3b,dark);
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
      int shade=ashen?0xff171820:RaidPresentation.blend(0xff202f33,0xff101c24,dark);
      tile(c,x-d,y-14*d,2*d,24*d,shade);
      for(int tier=0;tier<3;tier++) {
        float half=(4+tier*3)*d, yy=y+(-16+tier*7)*d;
        outline.reset();outline.moveTo(x,yy-7*d);outline.lineTo(x-half,yy+6*d);
        outline.lineTo(x+half,yy+6*d);outline.close();
        p.setColor(shade);c.drawPath(outline,p);
      }
    }
  }

  void courtyard(Canvas c,Path boundary,int width,int height,float d,float dark,boolean ashen) {
    c.save();c.clipPath(boundary);
    tile(c,0,0,width,height,ashen?RaidPresentation.blend(0xff443a3e,0xff312d35,dark):RaidPresentation.blend(0xff36454a,0xff293741,dark));
    float size=18*d;
    for(int row=0;row*size<height;row++) for(int col=0;col*size<width;col++) {
      float x=col*size+(row%2)*size/2,y=row*size;
      int h=(row*13+col*7)%7;
      int stone=ashen ? RaidPresentation.blend(h<3?0xff4d4146:0xff413b42,h<3?0xff3b333d:0xff342f38,dark) : RaidPresentation.blend(h<3?0xff46565c:0xff3e4e53,h<3?0xff344650:0xff2e3d46,dark);
      tile(c,x+d,y+d,size-2*d,size-2*d,stone);
      line(c,x+3*d,y+2*d,x+size-4*d,y+2*d,d,RaidPresentation.blend(0xff5c696b,0xff465a63,dark));
      if(h==2) {
        line(c,x+9*d,y+4*d,x+7*d,y+10*d,d,0xff303639);
        line(c,x+7*d,y+10*d,x+11*d,y+14*d,d,0xff303639);
      }
      if(h==5) tile(c,x+2*d,y+size-4*d,6*d,2*d,ashen?0xff584047:0xff42574b);
    }
    c.restore();
  }
  void torch(Canvas c,float x,float y,float d,float dark) {
    // One constant-radius shader; dusk changes only paint alpha, never allocates per frame.
    float radius=42*d;
    if(torchLight==null || radius!=lightRadius) {
      lightRadius=radius;
      torchLight=new RadialGradient(0,0,radius,new int[]{0x66f6b95e,0x19d98a3d,0x00d98a3d},
          new float[]{0,.4f,1},Shader.TileMode.CLAMP);
    }
    c.save();c.translate(x,y);p.setColor(0xffffffff);p.setShader(torchLight);
    p.setAlpha(Math.round(115+140*dark));c.drawCircle(0,0,radius,p);p.setShader(null);p.setAlpha(255);c.restore();
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
  void architecture(Canvas c,Game game,BoardGeometry geometry,float d,float dark) {
    float left=geometry.fortLeft,top=geometry.fortTop,right=geometry.fortRight,bottom=geometry.fortBottom;
    // Decorative corner bastions are inside the existing fort perimeter.
    for(int corner=0;corner<4;corner++) {
      float x=geometry.worldX(game.ringXAt(corner,0)), y=geometry.worldY(game.ringYAt(corner,0));
      if(game.wallHealth[corner]<=0) continue;
      tile(c,x-7*d,y-7*d,14*d,14*d,0xff171e27);
      tile(c,x-5*d,y-5*d,10*d,10*d,RaidPresentation.blend(0xff63757b,0xff475b68,dark));
      tile(c,x-3*d,y-3*d,6*d,6*d,0xff404850);
      for(int b=0;b<3;b++) tile(c,x+(b-1)*5*d,y-8*d,3*d,2*d,RaidPresentation.blend(0xff8b9690,0xff63777b,dark));
    }
    if(right-left>75*d && bottom-top>75*d) {
      banner(c,(left+right)*.5f,top+10*d,d);
      torch(c,left+14*d,top+17*d,d,dark);
      torch(c,right-14*d,top+17*d,d,dark);
      torch(c,left+14*d,bottom-15*d,d,dark);
      torch(c,right-14*d,bottom-15*d,d,dark);
    }
  }
  void atmosphere(Canvas c,int width,int height,float d,boolean night,boolean ashen) {atmosphere(c,width,height,d,night?1:0,ashen);}
  void atmosphere(Canvas c,int width,int height,float d,float dark,boolean ashen) {
    // Stepped darkness supplies depth without obscuring threats near the fortress.
    for(int band=0;band<5;band++) {
      float edge=(5-band)*4*d;
      int shade=RaidPresentation.blend(0x09060911,0x14060911,dark);
      tile(c,0,0,width,edge,shade);tile(c,0,height-edge,width,edge,shade);
      tile(c,0,edge,edge,height-2*edge,shade);tile(c,width-edge,edge,edge,height-2*edge,shade);
    }
    if(ashen) for(int i=0;i<8;i++) {
      float x=((i*97+23)*d)%Math.max(1,width), y=((i*113+59)*d)%Math.max(1,height);
      tile(c,x,y,2*d,d,RaidPresentation.blend(0xff9a654b,0xffc68755,dark));
    }
  }
  /** One shared hero design in the world and arena. */
  void hero(Canvas c,float x,float y,float d,float heading,boolean empowered) {
    pixels.hero(c,x,y,d,(float)Math.toDegrees(heading),empowered,false);
  }
}
