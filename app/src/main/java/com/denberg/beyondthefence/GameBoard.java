package com.denberg.beyondthefence;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.widget.FrameLayout;
import java.util.function.Supplier;

/** The artwork and clickable native tiles always use the same geometry, including after rotation. */
@android.annotation.SuppressLint("ViewConstructor") // Programmatic model injection.
final class GameBoard extends FrameLayout {
  final BoardGeometry geometry = new BoardGeometry();
  final FarmView artwork;
  private final View[] plots = new View[9], buildings = new View[3];

  GameBoard(Context context, Supplier<Game> game, Supplier<Boolean> paused) {
    super(context);
    setTag("field");
    artwork = new FarmView(context, game, paused, geometry);
    artwork.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
    addView(artwork);
  }

  int minimumBoardHeight() {
    return BoardGeometry.minimumHeight(getResources().getDisplayMetrics().density,
        getResources().getConfiguration().fontScale,
        getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
  }

  void plot(int index, View control) { plots[index] = control; addView(control); }
  void building(int index, View control) { buildings[index] = control; addView(control); }

  @Override
  protected void onMeasure(int widthSpec, int heightSpec) {
    int width = MeasureSpec.getSize(widthSpec), height = resolveSize(minimumBoardHeight(), heightSpec);
    setMeasuredDimension(width, height);
    geometry.layout(width, height, getResources().getDisplayMetrics().density,
        getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE,
        getResources().getConfiguration().fontScale);
    artwork.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    for (int i = 0; i < 9; i++) measureTile(plots[i], geometry.plots[i]);
    for (int i = 0; i < 3; i++) measureTile(buildings[i], geometry.buildings[i]);
  }

  private void measureTile(View v, BoardGeometry.Box b) {
    if (v != null) v.measure(MeasureSpec.makeMeasureSpec(b.width(), MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(b.height(), MeasureSpec.EXACTLY));
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    artwork.layout(0, 0, getWidth(), getHeight());
    for (int i = 0; i < 9; i++) place(plots[i], geometry.plots[i]);
    for (int i = 0; i < 3; i++) place(buildings[i], geometry.buildings[i]);
  }

  private void place(View v, BoardGeometry.Box b) {
    if (v != null) v.layout(b.left, b.top, b.right, b.bottom);
  }
}
