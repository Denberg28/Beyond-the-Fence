package com.denberg.beyondthefence;

import android.app.*;
import android.content.*;
import android.graphics.Bitmap;
import android.os.*;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import java.io.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Runs against the installed APK using framework instrumentation; no test runtime dependency. */
public final class SmokeInstrumentation extends Instrumentation {
    private int checks;
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    private void check(boolean condition,String message){if(!condition)throw new AssertionError(message);checks++;}
    private void click(MainActivity a,String tag){runOnMainSync(()->{View v=a.findViewById(android.R.id.content).findViewWithTag(tag);if(v==null)throw new AssertionError("Missing control "+tag);v.performClick();});waitForIdleSync();}
    private MainActivity launch(){return (MainActivity)startActivitySync(new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}
    @Override public void onStart(){
        Bundle result=new Bundle();
        try {
            MainActivity a=launch();waitForIdleSync();SystemClock.sleep(300);
            List<AccessibilityNodeInfo> nodes=getUiAutomation().getRootInActiveWindow().findAccessibilityNodeInfosByText("Plant the first seed");
            if(!nodes.isEmpty())nodes.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            waitForIdleSync();
            runOnMainSync(()->{a.paused=true;});
            check(a.game.coins==80,"Starting balance");
            click(a,"plot0");check(a.game.crop[0]==0 && a.game.coins==76,"Native planting");
            runOnMainSync(()->a.game.growth[0]=1);click(a,"harvest");check(a.game.crop[0]==-1 && a.game.coins==88,"Native harvest sale");
            click(a,"tab1");click(a,"upgrade0");check(a.game.weapon==1 && a.game.coins==8,"Native weapon upgrade");
            click(a,"tab2");click(a,"night");check(a.game.night,"Night transition");click(a,"lane2");check(a.game.lane==2,"Lane focus selection");
            click(a,"play");SystemClock.sleep(300);
            getUiAutomation().executeShellCommand("input keyevent 3").close();SystemClock.sleep(500);
            check(a.paused,"Background pauses game");float elapsed=a.game.elapsed;SystemClock.sleep(400);check(a.game.elapsed==elapsed,"No background simulation");
            getTargetContext().startActivity(new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));SystemClock.sleep(500);check(a.paused,"Explicit resume required");
            runOnMainSync(()->{a.game.defeated=true;a.game.health=0;a.refresh();});click(a,"night");check(!a.game.defeated && a.game.health==a.game.maxHealth(),"Free recovery through native control");
            // Persist a real action, close, and relaunch the actual activity.
            click(a,"tab0");click(a,"plot0");int expected=a.game.coins;
            SystemClock.sleep(600);runOnMainSync(a::finish);SystemClock.sleep(500);
            MainActivity b=launch();waitForIdleSync();check(b.game.coins==expected && b.game.crop[0]==0,"Persist across activity restart");check(b.paused,"Relaunch is paused");
            Bitmap screenshot=getUiAutomation().takeScreenshot();
            try(FileOutputStream out=new FileOutputStream(new File(getTargetContext().getFilesDir(),"smoke-farm.png"))){check(screenshot!=null,"Screenshot available");screenshot.compress(Bitmap.CompressFormat.PNG,100,out);}finally{if(screenshot!=null)screenshot.recycle();}
            testStoreRecovery();
            result.putString("stream","\nOK ("+checks+" checks)\n");finish(Activity.RESULT_OK,result);
        }catch(Throwable e){result.putString("stream","\nFAILURES!!!\n"+android.util.Log.getStackTraceString(e));finish(Activity.RESULT_CANCELED,result);}
    }
    private void testStoreRecovery() throws Exception {
        File dir=new File(getTargetContext().getCacheDir(),"checkpoint-test");if(!dir.exists()&&!dir.mkdirs())throw new IOException("Cannot create test directory");
        for(File f:dir.listFiles())if(!f.delete())throw new IOException("Cannot clear test file");
        Context isolated=new ContextWrapper(getTargetContext()){@Override public File getFilesDir(){return dir;}};
        CountDownLatch first=new CountDownLatch(1),second=new CountDownLatch(1);
        SaveStore store=new SaveStore(isolated,(rev,ok)->{if(!ok)throw new AssertionError("Checkpoint write");if(rev==1)first.countDown();else second.countDown();});
        Game g=new Game();store.save(g);check(first.await(5,TimeUnit.SECONDS),"First checkpoint durable");g.coins=123;store.save(g);check(second.await(5,TimeUnit.SECONDS),"Second checkpoint durable");check(store.load().game.coins==123,"Newest checkpoint selected");
        try(FileOutputStream out=new FileOutputStream(new File(dir,"farm-b.btfs"))){out.write(0);}
        SaveStore.Loaded loaded=store.load();check(loaded.recovered && loaded.game.coins==80,"Corruption recovers previous checkpoint");
        try(FileOutputStream out=new FileOutputStream(new File(dir,"farm-a.btfs"))){out.write(0);}
        check(store.load().damaged,"Both corrupt checkpoints block reset");store.close();
    }
}
