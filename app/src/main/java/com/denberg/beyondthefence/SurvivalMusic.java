package com.denberg.beyondthefence;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;

/** One bounded, local loop. Focus loss mutes it until the next explicit Play. */
final class SurvivalMusic {
  private final AudioManager manager;
  private final AudioFocusRequest request;
  private MediaPlayer player;
  private boolean focused, blocked;

  SurvivalMusic(Context context) {
    manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    AudioAttributes attributes = new AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
    request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(attributes).setOnAudioFocusChangeListener(change -> {
          if (change != AudioManager.AUDIOFOCUS_GAIN) { blocked = true; stop(); }
        }).build();
    try {
      player = MediaPlayer.create(context, R.raw.survival, attributes, 0);
      if (player != null) { player.setLooping(true); player.setVolume(.40f, .40f); }
    } catch (RuntimeException ignored) { player = null; }
  }
  void update(boolean playing) {
    if (!playing) { blocked = false; stop(); return; }
    if (player == null || blocked) return;
    if (!focused) {
      focused = manager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
      if (!focused) blocked = true; // Retry only after pause/play, never repeatedly steal focus.
    }
    if (focused && !player.isPlaying()) player.start();
  }
  boolean isPlaying() { return player != null && player.isPlaying(); }
  private void stop() {
    if (player != null && player.isPlaying()) player.pause();
    if (focused) { focused = false; manager.abandonAudioFocusRequest(request); }
  }
  void close() { stop(); if (player != null) { player.release(); player = null; } }
}
