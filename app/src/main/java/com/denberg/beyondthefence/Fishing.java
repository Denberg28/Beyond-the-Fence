package com.denberg.beyondthefence;

/** A loss-free ripple timing activity. Session time is transient; rewards live in Game. */
public final class Fishing {
  public static final int IDLE = 0, WAITING = 1, RIPPLE = 2, MISSED = 3, CAUGHT = 4;
  public static final float RIPPLE_SECONDS = 2;
  public int state;
  public float clock, wait;
  public boolean cast(float delay) {
    if (state == WAITING || state == RIPPLE) return false;
    wait = Math.max(2, Math.min(4, delay)); clock = 0; state = WAITING; return true;
  }
  public void tick(float dt) {
    if (!Float.isFinite(dt) || dt <= 0 || state != WAITING && state != RIPPLE) return;
    clock += Math.min(.1f, dt);
    if (state == WAITING && clock >= wait) { clock -= wait; state = RIPPLE; }
    if (state == RIPPLE && clock >= RIPPLE_SECONDS) { clock = 0; state = MISSED; }
  }
  public float progress() { return state == RIPPLE ? Math.min(1, clock / RIPPLE_SECONDS) : 0; }
  public boolean catchWindow() { return state == RIPPLE && progress() >= .35f && progress() <= .85f; }
  public boolean reel() {
    if (state != WAITING && state != RIPPLE) return false;
    boolean caught = catchWindow(); state = caught ? CAUGHT : MISSED; clock = 0; return caught;
  }
  public void cancel() { state = IDLE; clock = 0; }
}
