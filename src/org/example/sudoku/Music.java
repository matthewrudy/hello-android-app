package org.example.sudoku;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class Music {
	
  private static final String TAG = "Sudoku";
	
  private static MediaPlayer mp = null;
  
  public static void play(Context context, int resource) {
	  stop(context);
	  
	  if (Prefs.getMusic(context)) {
		  mp = MediaPlayer.create(context, resource);
		  mp.setLooping(true);
		  mp.start();
		  
		  Log.d(TAG, "music started");
	  } else {
		  Log.d(TAG, "music is turned off, dont start it");
	  }
  }
  
  public static void stop(Context context) {
	  if (mp != null) {
		  mp.stop();
		  mp.release();
		  mp = null;
		  
		  Log.d(TAG, "music stopped");
	  }
  }
}
