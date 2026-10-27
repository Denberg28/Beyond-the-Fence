package com.denberg.beyondthefence;

/** Bounded simulation-time sound cues, kept separate from combat and from the saved random stream. */
final class AudioCues {
  static final int SHOT = 1, ZOMBIE = 2, OWL = 4, CRICKET = 8;
  private Game source;
  private long lastShots;
  private boolean wasNight;
  private float growl = 2, animal = 2;
  private int animalIndex;

  int poll(Game game, boolean active, float dt) {
    if (source != game) {
      source = game; lastShots = game.shots; wasNight = game.night;
      growl = game.night ? .5f : 5; animal = 2; animalIndex = 0;
    }
    if (!active) { lastShots = game.shots; return 0; }
    if (wasNight != game.night) {
      wasNight = game.night; growl = .5f; animal = 2; animalIndex = 0;
    }
    int events = game.shots != lastShots ? SHOT : 0; lastShots = game.shots;
    dt = Float.isFinite(dt) ? Math.max(0, Math.min(.1f, dt)) : 0;
    growl -= dt;
    if (growl <= 0) {
      growl = game.night ? 4 : 9;
      if (!(game.night ? game.enemies.isEmpty() : game.roamers.isEmpty())) events |= ZOMBIE;
    }
    if (game.night) {
      animal -= dt;
      if (animal <= 0) { events |= animalIndex++ % 2 == 0 ? OWL : CRICKET; animal = 5; }
    }
    return events;
  }
}
