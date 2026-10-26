package com.denberg.beyondthefence;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AtomicFile;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Coalesced, sequential writes to alternating atomic slots. Keeps one older valid checkpoint. */
final class SaveStore {
    interface Listener { void result(long revision, boolean success); }
    static final class Loaded { Game game; boolean damaged, recovered; }
    private final AtomicFile[] slots;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    private byte[] pending;
    private long pendingRevision;
    private boolean draining;
    private int nextSlot;
    private final Listener listener;
    SaveStore(Context context, Listener listener) {
        this.listener=listener;
        slots=new AtomicFile[]{new AtomicFile(new File(context.getFilesDir(),"farm-a.btfs")),new AtomicFile(new File(context.getFilesDir(),"farm-b.btfs"))};
    }
    Loaded load() {
        Loaded result=new Loaded(); int invalid=0;
        for(int index=0;index<slots.length;index++) {
            AtomicFile slot=slots[index];
            boolean exists=slot.getBaseFile().exists() || new File(slot.getBaseFile()+".bak").exists();
            if(!exists) continue;
            try(InputStream in=slot.openRead()) {
                Game g=SaveCodec.decode(SaveCodec.readBounded(in));
                if(result.game==null || g.revision>result.game.revision) {result.game=g;nextSlot=1-index;}
            } catch(IOException e) { invalid++; }
        }
        result.damaged=result.game==null && invalid>0; result.recovered=result.game!=null && invalid>0;
        return result;
    }
    void save(Game game) {
        final byte[] bytes;
        game.revision++;
        try { bytes=SaveCodec.encode(game); } catch(IOException e) { listener.result(game.revision,false); return; }
        synchronized(this) {
            pending=bytes; pendingRevision=game.revision;
            if(draining) return;
            draining=true; worker.execute(this::drain);
        }
    }
    private void drain() {
        while(true) {
            byte[] bytes; long rev;
            synchronized(this) {
                if(pending==null) { draining=false; return; }
                bytes=pending; rev=pendingRevision; pending=null;
            }
            AtomicFile slot=slots[nextSlot]; FileOutputStream out=null; boolean ok=false;
            try { out=slot.startWrite(); out.write(bytes); slot.finishWrite(out); nextSlot=1-nextSlot; ok=true; }
            catch(IOException e) { if(out!=null) slot.failWrite(out); }
            final boolean status=ok; main.post(()->listener.result(rev,status));
        }
    }
    void close() { worker.shutdown(); }
}
