package com.denberg.beyondthefence;

/** Bounded gain ramp; a phase change cannot produce a hard audio cut or resume a stale fade. */
final class ScoreBlend {
  float day,night;
  void reset() {day=night=0;}
  void advance(float darkness,float dt) {
    dt=Float.isFinite(dt)?Math.max(0,Math.min(.1f,dt)):0;
    float mix=Float.isFinite(darkness)?Math.max(0,Math.min(1,darkness)):0;
    day=approach(day,1-mix,dt/0.8f);night=approach(night,mix,dt/0.8f);
  }
  private float approach(float value,float target,float step) {
    return value<target?Math.min(target,value+step):Math.max(target,value-step);
  }
}
