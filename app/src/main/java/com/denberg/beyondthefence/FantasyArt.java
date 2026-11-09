package com.denberg.beyondthefence;

import android.graphics.Canvas;

/** Shared original pixel silhouettes for the world and illustrated bestiary. */
final class FantasyArt {
  private final PixelCharacters pixels=new PixelCharacters();
  void enemy(Canvas c,int type,float x,float y,float scale,float phase,boolean attack,float heading) {
    pixels.fantasy(c,type,x,y,scale,phase,attack,heading);
  }
}
