package com.denberg.beyondthefence;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.widget.Button;

/** Native focus, TalkBack and pressed feedback with a reusable game skin. */
@android.annotation.SuppressLint("ViewConstructor") // Created in code, never inflated from XML.
final class GameButton extends Button {
  private final GradientDrawable shape = new GradientDrawable();
  private int oldFill, oldStroke, oldText;
  private boolean styled;

  GameButton(Context context) {
    super(context);
    float d = getResources().getDisplayMetrics().density;
    setAllCaps(false);
    setTextSize(12);
    setMinHeight(Math.round(48 * d));
    setMinimumWidth(0);
    setPadding(Math.round(6 * d), Math.round(4 * d), Math.round(6 * d), Math.round(4 * d));
    shape.setCornerRadius(2 * d);
    setBackground(new RippleDrawable(ColorStateList.valueOf(0x44ffffff), shape, null));
    colors(0xff2e3541, 0xffad8d62, 0xffeee4d2);
  }

  void colors(int fill, int stroke, int text) {
    if (styled && fill == oldFill && stroke == oldStroke && text == oldText) return;
    styled = true;
    oldFill = fill;
    oldStroke = stroke;
    oldText = text;
    // Restyle parchment-colored action surfaces; preserve transparent hit overlays.
    if (fill == 0xffe9e5d3) fill = 0xff303541;
    if (fill == 0xff536f48 || fill == 0xff314e40) fill = 0xff33434d;
    if ((text == 0xff203e36 || text == MainActivity.INK) && (fill == 0xff303541 || fill == 0xff33434d)) text = 0xfff0e5cf;
    shape.setColor(fill);
    shape.setStroke(Math.round(2 * getResources().getDisplayMetrics().density), stroke);
    int disabled = text == 0xff203e36 ? 0xff65717a : 0xffa8a7a0;
    setTextColor(new ColorStateList(new int[][] {new int[] {-android.R.attr.state_enabled}, new int[0]},
        new int[] {disabled, text}));
  }
}
