package com.denberg.beyondthefence;

import android.app.Application;

/** One in-process farm and writer, including across Activity recreation/rotation. */
public final class FarmApp extends Application {
  Game game;
  SaveStore store;
  boolean damaged;
  String saveMessage = "Local save ready";
  MainActivity observer;

  @Override
  public void onCreate() {
    super.onCreate();
    store =
        new SaveStore(
            this,
            (revision, ok) -> {
              saveMessage =
                  ok
                      ? (revision < game.revision ? "Saving locally…" : "Saved on this phone")
                      : "Save failed · export a backup in Options";
              if (observer != null) observer.showSaveStatus(saveMessage);
            });
    SaveStore.Loaded loaded = store.load();
    game = loaded.game == null ? new Game() : loaded.game;
    damaged = loaded.damaged;
    if (loaded.recovered) saveMessage = "Recovered from previous checkpoint";
  }
}
