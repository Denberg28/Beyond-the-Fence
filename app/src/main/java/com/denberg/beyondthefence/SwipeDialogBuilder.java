package com.denberg.beyondthefence;

import android.app.AlertDialog;
import android.content.Context;

/** Standard platform dialogs with a swipe-to-close surface, retaining buttons and accessibility. */
final class SwipeDialogBuilder extends AlertDialog.Builder {
  private boolean swipeEnabled = true;
  SwipeDialogBuilder(Context context) { super(context); }
  @Override public AlertDialog.Builder setCancelable(boolean cancelable) {
    swipeEnabled = cancelable; return super.setCancelable(cancelable);
  }
  @Override public AlertDialog show() {
    AlertDialog dialog = super.show();
    if (swipeEnabled) SwipeDismissLayout.install(dialog);
    return dialog;
  }
}
