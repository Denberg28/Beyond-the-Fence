package com.denberg.beyondthefence;

import static org.junit.Assert.*;
import org.junit.Test;

public class AudioCuesTest {
  @Test public void actualShotsTriggerOneBurstWithoutRepeatingOrChangingCombat() {
    Game g = new Game(); g.night = true; g.roamers.clear(); g.spawned = g.quota();
    g.enemies.add(new Game.Enemy(2, 0, 0, 400, 400));
    AudioCues cues = new AudioCues(); assertEquals(0, cues.poll(g, true, 0));
    g.tick(.1f); assertTrue(g.shots > 0); long shots = g.shots;
    assertEquals(AudioCues.SHOT, cues.poll(g, true, 0)); assertEquals(0, cues.poll(g, true, 0));
    assertEquals(shots, g.shots);
  }
  @Test public void dayHasNoNocturnalAnimalsAndNightAlternatesOwlAndCrickets() {
    Game g = new Game(); AudioCues cues = new AudioCues(); int day = 0;
    for (int i = 0; i < 300; i++) day |= cues.poll(g, true, .1f);
    assertTrue((day & AudioCues.ZOMBIE) != 0); assertEquals(0, day & (AudioCues.OWL | AudioCues.CRICKET));
    g.startNight(); int night = 0;
    for (int i = 0; i < 80; i++) night |= cues.poll(g, true, .1f);
    assertEquals(AudioCues.ZOMBIE | AudioCues.OWL | AudioCues.CRICKET, night);
  }
  @Test public void pausedTimeAndOldShotsNeverReplayAndNewFarmsResetCueState() {
    Game g = new Game(); AudioCues cues = new AudioCues(); cues.poll(g, true, 0);
    g.shots = 500; assertEquals(0, cues.poll(g, false, 900)); assertEquals(0, cues.poll(g, true, 0));
    g.shots++; assertEquals(AudioCues.SHOT, cues.poll(g, true, Float.NaN));
    Game replacement = new Game(); replacement.shots = 600;
    assertEquals(0, cues.poll(replacement, true, 0));
    assertEquals(0, cues.poll(replacement, true, 900)); // Delta capped; no catch-up chorus.
  }
}
