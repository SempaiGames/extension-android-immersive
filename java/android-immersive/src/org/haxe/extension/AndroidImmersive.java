package org.haxe.extension;

import java.io.IOException;
import java.util.Map;
import java.lang.Runnable;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.KeyEvent;
import android.view.KeyCharacterMap;
import android.widget.FrameLayout;
import android.widget.VideoView;
import android.util.Log;
import android.content.res.AssetFileDescriptor;



public class AndroidImmersive extends Extension {
	
	protected static String currentMode = null;
	private static final int DELAY_TIME = 500;

	/**
	 * Called when an activity you launched exits, giving you the requestCode 
	 * you started it with, the resultCode it returned, and any additional data 
	 * from it.
	 */
	@Override public boolean onActivityResult (int requestCode, int resultCode, Intent data) {
		return true;
	}

	/**
	 * Called when the activity is starting.
	 */
	@Override public void onCreate (Bundle savedInstanceState) {
		View decorView = mainActivity.getWindow().getDecorView();
		decorView.setOnSystemUiVisibilityChangeListener (new View.OnSystemUiVisibilityChangeListener() {
			@Override
			public void onSystemUiVisibilityChange(int visibility) {
				if(Build.VERSION.SDK_INT < 16 || Build.VERSION.SDK_INT >= 19){
					return;
				}
				resetSystemUiVisibility();
			}
		});
	}
	
	/**
	 * Perform any final cleanup before an activity is destroyed.
	 */
	@Override public void onDestroy () { }
	
	
	/**
	 * Called as part of the activity lifecycle when an activity is going into
	 * the background, but has not (yet) been killed.
	 */
	@Override public void onPause () { }
	
	/**
	 * Called after {@link #onStop} when the current activity is being 
	 * re-displayed to the user (the user has navigated back to it).
	 */
	@Override public void onRestart () { }
	
	
	/**
	 * Called after {@link #onRestart}, or {@link #onPause}, for your activity 
	 * to start interacting with the user.
	 */
	@Override public void onResume () {
		resetSystemUiVisibility();
	}
	
	
	/**
	 * Called after {@link #onCreate} &mdash; or after {@link #onRestart} when  
	 * the activity had been stopped, but is now again being displayed to the 
	 * user.
	 */
	@Override public void onStart () {
		resetSystemUiVisibility();
	}
		
	/**
	 * Called when the activity is no longer visible to the user, because 
	 * another activity has been resumed and is covering this one. 
	 */
	@Override public void onStop () { }

	public static void resetSystemUiVisibility(){
		Handler handler = new Handler();
		Runnable runnable = new Runnable(){
			public void run() {
				if(currentMode == null) return;
				if(currentMode.equals("lowProfile")) {
					_setLowProfile();
				} else if(currentMode.equals("immersive")) {
					_setImmersive();
				} else if(currentMode.equals("statusBarColor")) {
					_setStatusBarColor(-1);
				}
			}
		};
		handler.postDelayed(runnable, DELAY_TIME);
	}

	///////////////////////////////////////////////////////////////////////////
	// Immersive Modes Available
	///////////////////////////////////////////////////////////////////////////

	private static void _setLowProfile(){
		::if (ANDROID_TARGET_SDK_VERSION >= 19)::
		if(Build.VERSION.SDK_INT >= 15) {
			// enable low profile in api 15 devices
			View decorView = mainActivity.getWindow().getDecorView();
			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);	
		}
		::end::
	}

	///////////////////////////////////////////////////////////////////////////

	private static void _setImmersive(){
		::if (ANDROID_TARGET_SDK_VERSION >= 19)::
		boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
		boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
		if(Build.VERSION.SDK_INT >= 19) {
			// devices with immersive mode
			View decorView = mainActivity.getWindow().getDecorView();
			decorView.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);	
		}
		else if (hasBackKey && hasHomeKey && Build.VERSION.SDK_INT >= 16) {
			// no navigation soft keys, unless it is enabled in the settings
			View decorView = mainActivity.getWindow().getDecorView();
			decorView.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
				| View.SYSTEM_UI_FLAG_LOW_PROFILE);	
		}
		else if(Build.VERSION.SDK_INT >= 15) {
			// enable low profile in api 15 devices
			View decorView = mainActivity.getWindow().getDecorView();
			decorView.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
				| View.SYSTEM_UI_FLAG_LOW_PROFILE);	
		}
		::end::
	}

	///////////////////////////////////////////////////////////////////////////

	private static void _setStatusBarColor(final int color){
		::if (ANDROID_TARGET_SDK_VERSION >= 21)::
		if(Build.VERSION.SDK_INT >= 21) {
			try{
				Window window = Extension.mainActivity.getWindow();
				window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); 
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				if(color!=-1) window.setStatusBarColor(color);
			}catch(Exception e) {
				Log.i("EXTENSION-ANDROID-IMMERSIVE", "Exception: "+e.toString());
			}
		}	
		::end::
	}

	///////////////////////////////////////////////////////////////////////////
	// PUBLIC INTERFACE
	///////////////////////////////////////////////////////////////////////////

	public static void setLowProfile(){
		currentMode = "lowProfile";
		Extension.mainActivity.runOnUiThread(new Runnable() {
			public void run() {
				_setLowProfile();
			}
		});
	}

	///////////////////////////////////////////////////////////////////////////

	public static void setImmersive(){
		currentMode = "immersive";
		Extension.mainActivity.runOnUiThread(new Runnable() {
			public void run() {
				_setImmersive();
			}
		});
	}

	///////////////////////////////////////////////////////////////////////////

	public static void setStatusBarColor(final int color){
		currentMode = "statusBarColor";
		Extension.mainActivity.runOnUiThread(new Runnable() {
			public void run() {
				_setStatusBarColor(color);
			}
		});
	}

	///////////////////////////////////////////////////////////////////////////

}
