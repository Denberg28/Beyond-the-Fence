package com.denberg.beyondthefence;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;
import java.util.function.Supplier;

/** Original resolution-independent artwork. Decorative; all gameplay has native labeled controls. */
final class FarmView extends View {
    private final Paint p=new Paint(3);
    private final RectF rect=new RectF();
    private final Supplier<Game> model;
    private final Supplier<Boolean> paused;
    private final Path path=new Path();
    FarmView(Context context,Supplier<Game> model,Supplier<Boolean> paused){super(context);this.model=model;this.paused=paused;setLayerType(View.LAYER_TYPE_HARDWARE,null);}
    private void color(int c){p.setColor(c);p.setStyle(Paint.Style.FILL);p.setShader(null);}
    private void box(Canvas c,float x,float y,float w,float h,float r,int color){color(color);rect.set(x,y,x+w,y+h);c.drawRoundRect(rect,r,r,p);}
    private void circle(Canvas c,float x,float y,float r,int color){color(color);c.drawCircle(x,y,r,p);}
    private void line(Canvas c,float x,float y,float xx,float yy,int color,float width){color(color);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,p);}
    private void label(Canvas c,String s,float x,float y,int size,int color){color(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));c.drawText(s,x,y,p);}
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);Game g=model.get();if(g==null || getHeight()==0)return;
        c.save();float sx=getWidth()/1000f, sy=getHeight()/680f; c.scale(sx,sy);
        rect.set(0,0,1000,680);path.reset();path.addRoundRect(rect,30,30,Path.Direction.CW);c.clipPath(path);
        boolean night=g.night;
        p.setShader(new LinearGradient(0,0,0,680,night?0xff162d3b:0xffc8dfcf,night?0xff3c6553:0xff97bb80,Shader.TileMode.CLAMP));c.drawRect(0,0,1000,680,p);p.setShader(null);
        circle(c,825,86,48,night?0xffeee8bd:0xffffe2a1);
        if(night){circle(c,844,74,43,0xff162d3b);for(int i=0;i<24;i++)circle(c,35+(i*139)%920,20+(i*53)%135,i%3==0?2.5f:1.6f,0xffb9d4c8);}
        else {cloud(c,140,67,1);cloud(c,480,104,.7f);}
        path.reset();path.moveTo(-20,240);path.cubicTo(180,30,290,210,430,180);path.cubicTo(620,70,760,230,1020,138);path.lineTo(1020,680);path.lineTo(-20,680);path.close();color(night?0xff284a43:0xff78a47c);c.drawPath(path,p);
        path.reset();path.moveTo(0,300);path.cubicTo(300,115,500,335,1000,202);path.lineTo(1000,680);path.lineTo(0,680);path.close();color(night?0xff3c6249:0xffa1bf81);c.drawPath(path,p);
        for(int i=0;i<8;i++)tree(c,18+i*139,181+(i%3)*24,.65f,night);
        box(c,495,223,450,457,70,night?0xff4a6046:0xffb0b684);
        for(int lane=0;lane<3;lane++){box(c,520+lane*138,240,113,410,24,night?0xff4f654b:0xffbdc091);for(int k=0;k<5;k++)box(c,565+lane*138,255+k*82,8,27,4,night?0xff698068:0xffded9a4);}
        // Tiny trail leads to a warm farmhouse.
        box(c,115,332,170,348,48,night?0xff7b7860:0xffded0a3);
        house(c,70,223,night);
        box(c,39,483,400,156,18,night?0xff476448:0xff97b777);
        for(int i=0;i<9;i++){
            if(i>=g.plots())continue;int col=i%3,row=i/3;float x=55+col*132,y=395+row*80;
            box(c,x+3,y+5,112,63,12,0x2831271c);box(c,x,y,112,63,12,0xff70533b);
            for(int j=0;j<3;j++)line(c,x+13,y+15+j*16,x+98,y+15+j*16,0xff8c6947,3);
            if(g.crop[i]>=0)for(int j=0;j<4;j++)crop(c,x+22+(j%2)*54,y+17+(j/2)*27,g.crop[i],g.growth[i]);
        }
        // Fence with an opening for the watch post.
        int wood=g.health/g.maxHealth()<.3f?0xffa66d50:0xffe4c797;
        box(c,479,390,491,13,3,wood);box(c,479,425,491,13,3,wood);
        for(int i=0;i<12;i++){float x=481+i*43;if(g.health<g.maxHealth()*.3f && i%4==1)continue;box(c,x,374,13,87,3,wood);}
        box(c,644,474,120,112,12,night?0xff314a3d:0xff66845b);box(c,658,492,92,84,10,0xffb39263);
        circle(c,705,525,24,0x33202c20);box(c,689,506,31,31,9,0xffd1a975);circle(c,704,493,17,0xffefc591);box(c,681,477,46,13,5,0xff324f43);box(c,690,467,28,18,4,0xff324f43);
        line(c,705,518,719,492,0xff4a3b30,9);line(c,719,492,726,473,0xff1c3434,7);
        label(c,"HOMESTEAD",66,365,18,night?0xffe8ddbe:0xff334e3c);
        label(c,night?"THE NIGHT WATCH":"BEYOND THE FENCE",530,207,18,night?0xffdedbb8:0xff344e3d);
        float t=g.reducedMotion||paused.get()?0:SystemClock.uptimeMillis()/200f;
        for(Game.Enemy e:g.enemies){float x=575+e.lane*138,y=250+(1-e.x)*139;zombie(c,x,y,e.type,(float)Math.sin(t+e.lane)*2);box(c,x-22,y-57,44,5,2,0xff293e36);box(c,x-22,y-57,44*e.hp/e.maxHp,5,2,0xffe4b568);}
        if(night && g.shotClock>.55f && !g.enemies.isEmpty() && !g.reducedMotion && !paused.get()){
            Game.Enemy target=null;for(Game.Enemy e:g.enemies)if(e.lane==g.lane && (target==null||e.x<target.x))target=e;
            if(target==null)target=g.enemies.get(0);line(c,726,473,575+target.lane*138,250+(1-target.x)*139,0xaaffd477,3);circle(c,726,473,9,0xffffde8e);
        }
        if(g.weather==1){for(int i=0;i<45;i++){float x=(i*79)%1000,y=((i*53)+(g.reducedMotion||paused.get()?0:SystemClock.uptimeMillis()/12))%680;line(c,x,y,x-6,y+19,0x559fced8,2);}}
        if(g.weather==2){box(c,0,90,1000,115,50,0x38e7eadb);box(c,0,290,1000,80,40,0x25e7eadb);}
        // A short readable scene badge; the native status also announces this state.
        box(c,24,22,night?154:154,43,18,0xc9224037);label(c,night?"NIGHT "+g.day:"DAY "+g.day,47,51,21,0xfffaf3d9);
        if(paused.get()){box(c,404,595,194,48,20,0xdf203e36);label(c,"PAUSED",453,627,22,0xfff5e6c3);}
        c.restore();
    }
    private void cloud(Canvas c,float x,float y,float scale){c.save();c.translate(x,y);c.scale(scale,scale);circle(c,0,0,23,0xffe5eddb);circle(c,33,-15,35,0xffe5eddb);circle(c,70,1,24,0xffe5eddb);box(c,-5,0,80,23,11,0xffe5eddb);c.restore();}
    private void tree(Canvas c,float x,float y,float size,boolean night){c.save();c.translate(x,y);c.scale(size,size);box(c,-7,-2,14,64,4,0xff715f43);circle(c,-18,-13,30,night?0xff274d40:0xff527d5c);circle(c,15,-25,40,night?0xff2c5444:0xff648e60);circle(c,-6,-48,31,night?0xff345e49:0xff749e66);c.restore();}
    private void house(Canvas c,float x,float y,boolean night){
        box(c,x+10,y+21,253,113,7,0x25343125);box(c,x,y+14,249,112,6,0xffd5ba86);
        path.reset();path.moveTo(x-17,y+27);path.lineTo(x+120,y-65);path.lineTo(x+267,y+27);path.close();color(0xffa65f4d);c.drawPath(path,p);
        for(int i=0;i<6;i++)line(c,x+32+i*31,y+3,x+120,y-52,0xffbf7960,2);
        box(c,x+100,y+52,48,74,5,0xff3c5a4c);circle(c,x+136,y+91,3,0xffe9c986);
        for(int k=0;k<2;k++){float xx=x+24+k*157;box(c,xx,y+51,43,43,4,night?0xffffd47f:0xffadc9b5);line(c,xx+22,y+51,xx+22,y+94,0xfff3ddb0,4);line(c,xx,y+72,xx+43,y+72,0xfff3ddb0,4);}
        box(c,x+176,y-44,25,45,3,0xff8c6951);box(c,x+171,y-48,35,10,3,0xff735641);
    }
    private void crop(Canvas c,float x,float y,int type,float growth){
        float size=.35f+growth*.65f;
        c.save();c.translate(x,y);c.scale(size,size);
        line(c,0,10,0,-12,0xff86ae69,4);circle(c,-7,-6,6,0xff7d9c49);circle(c,6,-12,7,0xff9dbf6c);
        if(growth>.45f){if(type==0){circle(c,0,8,10,0xffe3d7af);circle(c,0,13,6,0xffb698b4);}else if(type==1){box(c,-5,-5,12,23,5,0xfff3ce6d);line(c,-11,17,-6,0,0xff92af5e,4);}else{circle(c,0,7,15,0xffd98b42);line(c,-3,-4,-3,20,0xffbd7134,2);line(c,5,-4,5,20,0xffedab53,2);}}
        c.restore();
    }
    private void zombie(Canvas c,float x,float y,int type,float bob){
        c.save();c.translate(x,y+bob);float scale=type==2?1.3f:1;c.scale(scale,scale);
        box(c,-21,15,43,10,6,0x30202d26);int coat=new int[]{0xff687b69,0xffa4775b,0xff60717b,0xff9c8d5d}[type];
        line(c,-8,5,-10,20,0xff394d46,8);line(c,7,5,11,20,0xff394d46,8);box(c,-15,-23,30,33,6,coat);
        line(c,-14,-18,-24,0,0xff94ac78,7);line(c,15,-18,24,-2,0xff94ac78,7);
        box(c,-15,-48,31,29,9,0xffa5b97f);box(c,-13,-48,26,7,4,0xff485c48);
        circle(c,-6,-34,3,0xff233e34);circle(c,7,-34,3,0xff233e34);line(c,-3,-25,8,-26,0xff506547,2);
        if(type==2)box(c,-20,-27,40,12,4,0xff829290);
        if(type==3)circle(c,24,-3,7,0xffc8cd77);
        c.restore();
    }
}
