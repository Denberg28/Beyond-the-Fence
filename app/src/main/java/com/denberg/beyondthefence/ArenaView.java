package com.denberg.beyondthefence;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** Original illustrated duel stage. Poses and effects derive only from saved simulation clocks. */
@android.annotation.SuppressLint("ViewConstructor")
final class ArenaView extends View {
  long renderedFrames;
  private final Game game;
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF rect=new RectF();
  private final Path path=new Path();
  ArenaView(Context context,Game game) {
    super(context);this.game=game;
    setContentDescription("Champion arena. Battle health and controls follow below.");
    setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
  }
  private void fill(int color) {p.setColor(color);p.setStyle(Paint.Style.FILL);}
  private void box(Canvas c,float x,float y,float w,float h,float radius,int color) {
    fill(color);rect.set(x,y,x+w,y+h);c.drawRoundRect(rect,radius,radius,p);
  }
  private void oval(Canvas c,float x,float y,float rx,float ry,int color) {
    fill(color);rect.set(x-rx,y-ry,x+rx,y+ry);c.drawOval(rect,p);
  }
  private void line(Canvas c,float x,float y,float xx,float yy,float width,int color) {
    fill(color);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,p);
  }
  private void triangle(Canvas c,float x,float y,float xx,float yy,float xxx,float yyy,int color) {
    fill(color);path.reset();path.moveTo(x,y);path.lineTo(xx,yy);path.lineTo(xxx,yyy);path.close();c.drawPath(path,p);
  }
  private void arc(Canvas c,float x,float y,float w,float h,float start,float sweep,float width,int color) {
    fill(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);rect.set(x,y,x+w,y+h);c.drawArc(rect,start,sweep,false,p);p.setStyle(Paint.Style.FILL);
  }
  @Override protected void onDraw(Canvas c) {
    super.onDraw(c);renderedFrames++;
    c.drawColor(0xff1d282c);
    float scale=Math.min(getWidth()/300f,getHeight()/164f);
    c.save();c.translate((getWidth()-300*scale)/2,(getHeight()-164*scale)/2);c.scale(scale,scale);
    backdrop(c);
    float pulse=game.reducedMotion?0:(float)Math.sin(game.arenaElapsed*3);
    float playerInterval=(game.enhancerTime>0?.8f:1)/(1+game.equipmentBonus(3));
    float playerSwing=game.reducedMotion?0:game.arenaActive&&game.arenaAttackClock>playerInterval-.2f?1:0;
    float interval=game.arenaChampion==1?1.05f:game.arenaChampion==2?2.4f:1.6f;
    float enemySwing=game.reducedMotion?0:game.arenaActive&&game.arenaEnemyClock>interval-.2f?1:0;
    oval(c,75,148,33,7,0x88201e27);oval(c,225,148,game.arenaChampion==2?42:32,7,0x88201e27);
    if(game.enhancerTime>0) {
      oval(c,75,149,32,6,0x887f60aa);arc(c,43,139,64,13,0,360,1.5f,0xffd6b5ef);
    }
    c.save();c.translate(74+playerSwing*5,143+(game.reducedMotion?0:pulse));vanguard(c,playerSwing);c.restore();
    c.save();c.translate(225-enemySwing*5,143+(game.reducedMotion?0:-pulse));c.scale(-1,1);
    if(game.arenaChampion==0) hollowKnight(c,enemySwing);
    else if(game.arenaChampion==1) huntress(c,enemySwing);
    else if(game.arenaChampion==2) colossus(c,enemySwing);
    else witch(c,enemySwing,pulse);
    c.restore();
    if(playerSwing>0) arc(c,97,61,49,64,-65,128,3,0xfff5e6b6);
    if(enemySwing>0) arc(c,156,64,46,61,113,127,2.5f,game.arenaChampion==3?0xffd0a4ef:0xffe2aa83);
    bar(c,14,game.arenaHealth/game.arenaMaxHealth(),0xff91be89,"VANGUARD");
    bar(c,174,game.arenaEnemyHealth/game.arenaMaxEnemyHealth(),0xffd98d7c,"CHAMPION");
    c.restore();
  }
  private void backdrop(Canvas c) {
    box(c,0,0,300,164,10,0xff29383d);
    for(int row=0;row<5;row++) for(int col=0;col<8;col++) {
      float x=col*42-(row%2)*21,y=28+row*19;
      box(c,x+1,y+1,39,16,2,(row+col)%2==0?0xff354448:0xff3b4b4e);
      line(c,x+3,y+2,x+37,y+2,.8f,0xff53615f);
    }
    // Recessed gate, stone piers and heraldic banners frame the combat silhouettes.
    box(c,119,34,62,90,24,0xff17272d);arc(c,113,28,74,83,180,180,6,0xff71817c);
    for(int bar=0;bar<5;bar++) line(c,126+bar*12,46,126+bar*12,123,2,0xff4a6061);
    for(int side=0;side<2;side++) {
      float x=side==0?13:272;
      box(c,x,29,15,106,2,0xff697774);box(c,x-3,29,21,6,1,0xffa1aaa0);
      for(int j=0;j<5;j++) line(c,x,47+j*18,x+15,47+j*18,1,0xff3b4b4c);
      float flag=side==0?39:246;
      box(c,flag,31,15,31,0,side==0?0xff806346:0xff684451);
      triangle(c,flag,62,flag+7.5f,68,flag+15,62,side==0?0xff806346:0xff684451);
      line(c,flag+7,37,flag+7,53,2,0xffd6b975);line(c,flag+3,44,flag+11,44,2,0xffd6b975);
    }
    oval(c,150,145,143,20,0xff665e57);oval(c,150,141,122,15,0xff847664);
    for(int i=0;i<7;i++) line(c,38+i*37,133,29+i*40,154,.8f,0xffb09b77);
    line(c,25,143,275,143,.7f,0xffb09b77);
    box(c,0,0,300,27,0,0xdd18282d);
  }
  private void bar(Canvas c,float x,float ratio,int color,String label) {
    fill(0xffd8d6bb);p.setTextSize(7);p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);c.drawText(label,x,10,p);
    box(c,x,14,112,7,2,0xff0f1d23);box(c,x+1,15,110*Math.max(0,Math.min(1,ratio)),5,1,color);
  }
  private void boots(Canvas c,int armor) {
    line(c,-10,-25,-11,-5,10,0xff283841);line(c,9,-25,11,-5,10,0xff283841);
    box(c,-16,-7,18,8,2,0xff263238);box(c,7,-7,18,8,2,0xff263238);
    box(c,-15,-24,10,17,2,armor);box(c,6,-24,10,17,2,armor);
    line(c,-10,-23,-10,-9,1,0xffd6d8c8);line(c,11,-23,11,-9,1,0xffd6d8c8);
  }
  private void sword(Canvas c,float x,float y,float angle,boolean dark) {
    c.save();c.translate(x,y);c.rotate(angle);
    box(c,-2,0,4,14,1,0xff5c4434);oval(c,0,14,3,2,0xffc5a160);
    line(c,-9,0,9,0,3,0xffd4b56f);
    triangle(c,-3,-31,0,-41,3,-31,dark?0xff94a6aa:0xffe3e7d8);
    box(c,-3,-31,6,30,0,dark?0xff819599:0xffbbcbd0);line(c,0,-36,0,-3,1.2f,0xfff5efca);
    c.restore();
  }
  private void shield(Canvas c,float x,float y,int face,int rim) {
    fill(rim);path.reset();path.moveTo(x-12,y-15);path.lineTo(x+12,y-15);path.lineTo(x+10,y+5);path.lineTo(x,y+16);path.lineTo(x-10,y+5);path.close();c.drawPath(path,p);
    fill(face);path.reset();path.moveTo(x-9,y-12);path.lineTo(x+9,y-12);path.lineTo(x+7,y+4);path.lineTo(x,y+12);path.lineTo(x-7,y+4);path.close();c.drawPath(path,p);
    line(c,x,y-8,x,y+7,2,rim);line(c,x-5,y-2,x+5,y-2,2,rim);
  }
  private void vanguard(Canvas c,float swing) {
    boolean relic=game.gear[0]>=0;int trim=relic?0xffe6c071:0xffb2bcae;
    triangle(c,-12,-68,-30,-13,5,-20,0xff873d40);triangle(c,-9,-64,-23,-18,0,-24,0xffb4544b);
    boots(c,0xff84969e);
    box(c,-18,-62,35,38,5,0xff364954);box(c,-13,-58,26,29,4,0xff829da9);
    triangle(c,-13,-56,0,-61,13,-56,0xffc2d0cb);line(c,0,-53,0,-31,2,trim);
    box(c,-25,-62,15,11,4,0xffa7b5b2);box(c,10,-62,15,11,4,0xffa7b5b2);
    line(c,18,-49,27,-36-swing*10,8,0xff5f7884);box(c,22,-42-swing*10,9,10,2,0xffd2b18d);
    sword(c,29,-40-swing*10,27+swing*48,false);
    box(c,-16,-30,31,5,1,0xff624a39);box(c,-3,-31,7,7,1,trim);
    oval(c,0,-73,12,13,0xffddb58e);box(c,-12,-84,24,10,4,0xff544532);
    if(relic) {
      box(c,-14,-87,28,17,6,0xff9aafb1);box(c,-10,-78,20,7,1,0xff2d414a);
      line(c,-12,-78,12,-78,2,trim);line(c,0,-87,0,-71,2,trim);
      triangle(c,-5,-87,0,-97,5,-87,0xffad5352);
    } else {line(c,-7,-74,-3,-74,1.6f,0xff28343a);line(c,4,-74,7,-74,1.6f,0xff28343a);}
    shield(c,-24,-43,0xff3a6077,trim);
  }
  private void hollowKnight(Canvas c,float swing) {
    triangle(c,-13,-69,-30,-8,11,-14,0xff392c40);boots(c,0xff687783);
    box(c,-20,-64,39,39,5,0xff414451);box(c,-15,-61,29,30,4,0xff63727c);
    line(c,-12,-58,10,-31,3,0xffb79b69);line(c,11,-58,-11,-31,2,0xff38434c);
    box(c,-27,-66,17,13,3,0xff7c8790);box(c,10,-66,18,13,3,0xff7c8790);
    triangle(c,-23,-65,-24,-77,-17,-66,0xffc3ba9c);triangle(c,17,-66,24,-77,23,-65,0xffc3ba9c);
    line(c,19,-50,27,-36-swing*12,8,0xff4a5261);sword(c,30,-42-swing*12,25+swing*55,true);
    box(c,-13,-88,26,23,5,0xff657581);box(c,-10,-78,21,5,0,0xff202c35);
    line(c,-7,-76,-2,-76,1.5f,0xfff09b85);line(c,3,-76,8,-76,1.5f,0xfff09b85);
    line(c,-10,-85,-19,-94,4,0xffb5ae91);line(c,10,-85,19,-94,4,0xffb5ae91);
    shield(c,-25,-43,0xff4f3545,0xffa8aa99);
  }
  private void huntress(Canvas c,float swing) {
    triangle(c,-10,-77,-29,-10,14,-16,0xff384a40);boots(c,0xff676758);
    box(c,-14,-62,28,35,6,0xff59674b);box(c,-10,-59,20,28,3,0xff7e7956);
    line(c,-11,-59,12,-29,3,0xffb2a272);box(c,-15,-30,30,5,1,0xff47392c);
    for(int arrow=0;arrow<3;arrow++) {line(c,-20+arrow*4,-52,-20+arrow*4,-89,1,0xffc6bb8e);triangle(c,-22+arrow*4,-88,-20+arrow*4,-93,-18+arrow*4,-88,0xffb7c2bb);}
    oval(c,0,-75,11,12,0xffa5ae7b);triangle(c,-15,-75,0,-96,15,-75,0xff3a504b);
    box(c,-11,-72,22,8,1,0xff536652);line(c,-7,-77,-3,-77,1.8f,0xffe4cf7b);line(c,3,-77,7,-77,1.8f,0xffe4cf7b);
    line(c,11,-52,30,-47,6,0xff7f8c64);line(c,-10,-50,23-swing*10,-43,5,0xff7f8c64);
    arc(c,18,-75,27,66,-72,145,3,0xffb3925e);line(c,36,-73,36,-12,1,0xffd0cba9);
    line(c,20-swing*7,-43,47,-43,1.5f,0xffd2c09b);triangle(c,47,-46,54,-43,47,-40,0xffbdc9c3);
  }
  private void colossus(Canvas c,float swing) {
    c.save();c.scale(1.17f,1.05f);
    line(c,-12,-28,-15,-5,13,0xffb2ae94);line(c,13,-28,16,-5,13,0xffb2ae94);
    box(c,-24,-8,25,9,3,0xff7b8075);box(c,5,-8,25,9,3,0xff7b8075);
    box(c,-24,-67,48,42,7,0xff5b6760);oval(c,0,-49,21,23,0xff969e87);
    for(int rib=0;rib<4;rib++) {arc(c,-18,-64+rib*7,36,17,5,170,3,0xffd3c9a8);}
    line(c,0,-65,0,-29,4,0xffded2ae);
    oval(c,-26,-62,13,11,0xffc2b99a);oval(c,26,-62,13,11,0xffc2b99a);
    line(c,-28,-53,-33,-25,11,0xffa7ab93);line(c,28,-53,36,-32-swing*14,11,0xffa7ab93);
    line(c,37,-18-swing*14,37,-63-swing*14,6,0xff655448);box(c,28,-69-swing*14,19,25,3,0xff93988a);
    triangle(c,29,-62-swing*14,22,-68-swing*14,29,-69-swing*14,0xffdbcfac);
    triangle(c,46,-60-swing*14,54,-67-swing*14,46,-68-swing*14,0xffdbcfac);
    oval(c,0,-82,16,17,0xffd3c6a1);oval(c,-6,-84,4,5,0xff343f3c);oval(c,6,-84,4,5,0xff343f3c);
    oval(c,-6,-83,1.5f,2,0xffe1b373);oval(c,6,-83,1.5f,2,0xffe1b373);
    triangle(c,-2,-79,0,-84,3,-79,0xff51584b);
    for(int tooth=0;tooth<4;tooth++) box(c,-8+tooth*5,-74,3,7,0,0xffede1bd);
    line(c,-11,-92,-22,-100,5,0xffb5ad8e);line(c,11,-92,22,-100,5,0xffb5ad8e);
    c.restore();
  }
  private void witch(Canvas c,float swing,float pulse) {
    oval(c,0,5,25,5,0x88916da9);arc(c,-30,-2,60,12,0,360,1,0xffb794cf);
    triangle(c,-20,-7,0,-71,21,-7,0xff473454);triangle(c,-11,-9,0,-60,12,-9,0xff80618d);
    for(int fold=0;fold<3;fold++) line(c,-7+fold*7,-39,-13+fold*13,-9,1.5f,0xffa486ac);
    oval(c,0,-75,11,12,0xffc6b696);triangle(c,-11,-78,-18,-49,-5,-66,0xffb7b4ba);triangle(c,10,-78,18,-49,6,-66,0xffb7b4ba);
    line(c,-6,-76,-2,-76,1.8f,0xffe3bbea);line(c,3,-76,7,-76,1.8f,0xffe3bbea);
    triangle(c,-13,-84,-1,-111,15,-84,0xff504361);oval(c,1,-83,24,4,0xff3d304d);
    line(c,-10,-87,10,-87,3,0xffb89ac7);oval(c,4,-88,2,2,0xffe7c372);
    line(c,9,-56,28,-43-swing*9,6,0xff8b6e96);line(c,-9,-56,-21,-45,6,0xff8b6e96);
    line(c,30,-9,30,-94,3,0xffaa8e63);arc(c,22,-106,17,20,12,306,3,0xffbba479);
    oval(c,30,-99,10+pulse,10+pulse,0x338f76d9);oval(c,30,-99,5,5,0xffd9b5f1);oval(c,29,-101,2,2,0xfff1e2ff);
    oval(c,-27,-45,5+pulse*.5f,5+pulse*.5f,0xffbaa0db);
    if(swing>0) {oval(c,44,-47,6,6,0xffcfb3ef);oval(c,53,-41,3,3,0xffe9cffe);}
  }
}
