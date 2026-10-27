package com.denberg.beyondthefence;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

/** Local day/night scores plus a six-voice effects pool. All playback stops on pause/focus loss. */
final class SurvivalMusic {
  private final AudioManager manager;
  private final AudioFocusRequest request;
  private final SoundPool effects;
  private final AudioCues cues = new AudioCues();
  private final int[] sounds = new int[4], streams = new int[6];
  private final MediaPlayer[] players = new MediaPlayer[2];
  private boolean focused, blocked, closed;
  private int activeTrack = -1, loaded, nextStream;
  int shotSounds, zombieSounds, owlSounds, cricketSounds;

  SurvivalMusic(Context context) {
    manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    AudioAttributes attributes = new AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
    request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(attributes).setOnAudioFocusChangeListener(change -> {
          if (change != AudioManager.AUDIOFOCUS_GAIN) { blocked = true; stop(); }
        }).build();
    for (int i = 0; i < 2; i++) {
      try {
        players[i] = MediaPlayer.create(context, i == 0 ? R.raw.sunny : R.raw.creepy, attributes, 0);
        if (players[i] != null) { players[i].setLooping(true); players[i].setVolume(i == 0 ? .38f : .45f, i == 0 ? .38f : .45f); }
      } catch (RuntimeException ignored) { players[i] = null; }
    }
    effects = new SoundPool.Builder().setMaxStreams(6).setAudioAttributes(new AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
    effects.setOnLoadCompleteListener((pool, id, status) -> { if (status == 0) loaded++; });
    sounds[0] = effects.load(context, R.raw.shot, 1); sounds[1] = effects.load(context, R.raw.zombie, 1);
    sounds[2] = effects.load(context, R.raw.owl, 1); sounds[3] = effects.load(context, R.raw.cricket, 1);
  }

  void update(boolean active, Game game, boolean musicOn, boolean effectsOn, float dt) {
    if (closed) return;
    int events = cues.poll(game, active, dt);
    if (!active || (!musicOn && !effectsOn)) { blocked = false; stop(); return; }
    if (blocked) return;
    if (!focused) {
      focused = manager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
      if (!focused) { blocked = true; return; }
    }
    int desired = game.night || game.arenaActive ? 1 : 0;
    if (!musicOn) stopMusic();
    else if (activeTrack != desired) {
      stopMusic(); activeTrack = desired;
      if (players[desired] != null) players[desired].start();
    }
    if (!effectsOn) stopEffects();
    else if (loaded == 4) {
      for (int i = 0; i < 4; i++) if ((events & (1 << i)) != 0) {
        if (streams[nextStream] != 0) effects.stop(streams[nextStream]);
        float volume = i == 0 ? .40f : i == 1 ? .28f : .22f;
        int stream = effects.play(sounds[i], volume, volume, i == 0 ? 2 : 1, 0, 1);
        streams[nextStream] = stream; nextStream = (nextStream + 1) % streams.length;
        if (stream != 0) {
          if (i == 0) shotSounds++; else if (i == 1) zombieSounds++;
          else if (i == 2) owlSounds++; else cricketSounds++;
        }
      }
    }
  }
  boolean isPlaying() { return activeTrack >= 0 && players[activeTrack] != null && players[activeTrack].isPlaying(); }
  boolean isNightTrack() { return activeTrack == 1 && isPlaying(); }
  boolean effectsReady() { return loaded == 4; }
  int effectStreams() { int count = 0; for (int stream : streams) if (stream != 0) count++; return count; }
  private void stopMusic() {
    for (MediaPlayer player : players) if (player != null && player.isPlaying()) player.pause();
    activeTrack = -1;
  }
  private void stopEffects() {
    for (int i = 0; i < streams.length; i++) { if (streams[i] != 0) effects.stop(streams[i]); streams[i] = 0; }
  }
  private void stop() {
    stopMusic(); stopEffects();
    if (focused) { focused = false; manager.abandonAudioFocusRequest(request); }
  }
  void pause() { if (!closed) { blocked = false; stop(); } }
  void close() {
    if (closed) return;
    stop(); effects.release();
    for (int i = 0; i < players.length; i++) if (players[i] != null) { players[i].release(); players[i] = null; }
    closed = true;
  }
}
