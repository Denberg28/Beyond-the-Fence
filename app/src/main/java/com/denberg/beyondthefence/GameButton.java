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
    shape.setCornerRadius(12 * d);
    setBackground(new RippleDrawable(ColorStateList.valueOf(0x44ffffff), shape, null));
    colors(0xffe9e5d3, 0xffd2cba9, 0xff203e36);
  }

  void colors(int fill, int stroke, int text) {
    if (styled && fill == oldFill && stroke == oldStroke && text == oldText) return;
    styled = true;
    oldFill = fill;
    oldStroke = stroke;
    oldText = text;
    shape.setColor(fill);
    shape.setStroke(Math.round(2 * getResources().getDisplayMetrics().density), stroke);
    int disabled = text == 0xff203e36 ? 0xff63725b : 0xffc5cfb8;
    setTextColor(new ColorStateList(new int[][] {new int[] {-android.R.attr.state_enabled}, new int[0]},
        new int[] {disabled, text}));
  }
}
