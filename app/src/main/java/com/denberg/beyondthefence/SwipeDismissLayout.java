package com.denberg.beyondthefence;

import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/** Downward drags dismiss transient UI; taps and horizontal/vertical content scrolling stay native. */
@android.annotation.SuppressLint("ViewConstructor")
final class SwipeDismissLayout extends FrameLayout {
  private final Runnable dismiss;
  private final float threshold, slop;
  private float downX, downY;
  private boolean eligible, dragging;

  SwipeDismissLayout(Context context, Runnable dismiss) {
    super(context);
    this.dismiss = dismiss;
    setClickable(true); setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    threshold = 48 * getResources().getDisplayMetrics().density;
    slop = ViewConfiguration.get(context).getScaledTouchSlop();
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent event) {
    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        downX = event.getX(); downY = event.getY(); dragging = false;
        eligible = !scrollsUpAt(this, downX, downY);
        break;
      case MotionEvent.ACTION_POINTER_DOWN:
      case MotionEvent.ACTION_CANCEL:
        eligible = dragging = false; break;
      case MotionEvent.ACTION_MOVE:
        float dx = Math.abs(event.getX() - downX), dy = event.getY() - downY;
        if (dx > slop && dx > Math.abs(dy) || dy < -slop) eligible = false;
        if (eligible && dy >= threshold && dy > dx * 1.5f) {
          dragging = true; return true; // Android cancels the pressed child before we consume the release.
        }
        break;
      default: break;
    }
    return false;
  }

  @Override public void requestDisallowInterceptTouchEvent(boolean disallow) {
    // A ScrollView at its top must still allow the downward-dismiss gesture.
    super.requestDisallowInterceptTouchEvent(disallow && !eligible);
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    if (!dragging && event.getActionMasked() == MotionEvent.ACTION_MOVE) onInterceptTouchEvent(event);
    if (!dragging) return super.onTouchEvent(event);
    if (event.getActionMasked() == MotionEvent.ACTION_UP) {
      float dx = Math.abs(event.getX() - downX), dy = event.getY() - downY;
      dragging = eligible = false;
      if (dy >= threshold && dy > dx * 1.5f) { performClick(); dismiss.run(); }
    } else if (event.getActionMasked() == MotionEvent.ACTION_CANCEL
        || event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) dragging = eligible = false;
    return true;
  }

  @Override public boolean performClick() { super.performClick(); return true; }

  private static boolean scrollsUpAt(View view, float x, float y) {
    if (view.canScrollVertically(-1)) return true;
    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup)view;
      for (int i = group.getChildCount() - 1; i >= 0; i--) {
        View child = group.getChildAt(i);
        float cx = x + group.getScrollX() - child.getLeft(), cy = y + group.getScrollY() - child.getTop();
        if (child.getVisibility() == VISIBLE && cx >= 0 && cy >= 0 && cx < child.getWidth() && cy < child.getHeight()
            && scrollsUpAt(child, cx, cy)) return true;
      }
    }
    return false;
  }

  static void install(Dialog dialog) {
    ViewGroup content = dialog.findViewById(android.R.id.content);
    if (content == null || content.getChildCount() == 0 || content.getChildAt(0) instanceof SwipeDismissLayout) return;
    SwipeDismissLayout wrapper = new SwipeDismissLayout(dialog.getContext(), dialog::cancel);
    while (content.getChildCount() > 0) {
      View child = content.getChildAt(0); ViewGroup.LayoutParams params = child.getLayoutParams();
      content.removeViewAt(0); wrapper.addView(child, params);
    }
    content.addView(wrapper, new ViewGroup.LayoutParams(-1, -2));
  }
}
