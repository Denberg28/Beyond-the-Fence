package com.denberg.beyondthefence;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/** Static illustration plate; the adjacent native text supplies complete accessible descriptions. */
@android.annotation.SuppressLint("ViewConstructor")
final class BestiaryView extends View {
  private final FantasyArt art=new FantasyArt();
  private final BossArt bosses=new BossArt();
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  BestiaryView(Context context) {super(context);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
  @Override protected void onDraw(Canvas c) {
    super.onDraw(c);float scale=Math.min(getWidth()/300f,getHeight()/290f);
    c.save();c.translate((getWidth()-300*scale)/2,(getHeight()-290*scale)/2);c.scale(scale,scale);
    int[] types={Bestiary.KOBOLD,Bestiary.ORC,Bestiary.WOLF,Bestiary.HEXER};
    String[] names={"Kobold","Orc","Dire wolf","Hexer"};
    for(int i=0;i<4;i++) {
      float x=37+i*75;art.enemy(c,types[i],x,53,i==2?1.15f:1.45f,0,false,0);
      p.setColor(0xff203e36);p.setTextSize(10);p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);c.drawText(names[i],x,89,p);
    }
    for(int i=0;i<BossRoster.COUNT;i++) {
      float x=37+(i%4)*75,y=145+(i/4)*93;
      bosses.draw(c,i,x,y,1.1f,0,false,0);
      String[] labels={"Dragon","Two-head ogre","Great wolf","Yeti","Serpent","Lich","Giant zombie","Warden"};
      p.setTextSize(9);c.drawText(labels[i],x,y+28,p);
    }
    c.restore();
  }
}
