package com.denberg.beyondthefence;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

/** Quiet, deterministic scenery beneath transparent HUD and command padding. */
@android.annotation.SuppressLint("ViewConstructor")
final class TerrainBackdrop extends View {
  private final MedievalPixelArt art = new MedievalPixelArt();
  private float dark;
  private boolean ashen;
  TerrainBackdrop(Context context) {
    super(context);setTag("terrain");
    setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
  }
  void phase(float value, boolean biome) {
    if(dark!=value || ashen!=biome) {dark=value;ashen=biome;invalidate();}
  }
  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    float d=getResources().getDisplayMetrics().density;
    art.ground(canvas,getWidth(),getHeight(),d,dark,ashen);
    art.atmosphere(canvas,getWidth(),getHeight(),d,dark,ashen);
  }
}
