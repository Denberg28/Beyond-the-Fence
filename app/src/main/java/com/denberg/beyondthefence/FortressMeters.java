package com.denberg.beyondthefence;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/** Read-only native HUD: actual farm health and average perimeter integrity. */
@android.annotation.SuppressLint("ViewConstructor")
final class FortressMeters extends View {
  private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
  private float farm=1, fence=1;
  private int health=-1, maximum=-1, integrity=-1;
  FortressMeters(Context context) {
    super(context);setTag("fortressMeters");
    setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
  }
  void update(Game game) {
    int next=Math.round(game.health), max=game.maxHealth();
    float walls=0;for(int side=0;side<4;side++) walls+=game.wallRatio(side);
    int percent=Math.round(walls*25);
    if(next==health && max==maximum && percent==integrity) return;
    health=next;maximum=max;integrity=percent;
    farm=Math.max(0,Math.min(1,game.health/max));fence=Math.max(0,Math.min(1,walls/4));
    setContentDescription("Farm health "+health+" of "+maximum+", fence integrity "+integrity+" percent");
    invalidate();
  }
  @Override protected void onMeasure(int widthSpec,int heightSpec) {
    float d=getResources().getDisplayMetrics().density;
    float font=getResources().getConfiguration().fontScale;
    setMeasuredDimension(View.MeasureSpec.getSize(widthSpec),resolveSize(Math.round((20*font+12)*d),heightSpec));
  }
  @Override protected void onDraw(Canvas c) {
    super.onDraw(c);
    float d=getResources().getDisplayMetrics().density;
    float text=10*d*getResources().getConfiguration().fontScale;
    float gap=12*d, width=(getWidth()-gap)/2;
    meter(c,0,width,text,d,"Farm "+health+"/"+maximum,farm,0xffdc827d);
    meter(c,width+gap,width,text,d,"Fence "+integrity+"%",fence,0xff8bc2ca);
  }
  private void meter(Canvas c,float x,float width,float text,float d,String label,float ratio,int color) {
    paint.setStyle(Paint.Style.FILL);paint.setTextSize(text);paint.setColor(0xffe9e2d1);
    c.drawText(label,x,text,paint);
    float y=text+5*d;
    paint.setColor(0xff121c25);c.drawRect(x,y,x+width,y+6*d,paint);
    paint.setColor(0xff56616b);c.drawRect(x,y,x+width,y+d,paint);
    paint.setColor(color);c.drawRect(x+d,y+d,x+d+(width-2*d)*ratio,y+5*d,paint);
  }
}
