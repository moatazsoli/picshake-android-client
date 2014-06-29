package com.moataz.picshake;


import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	static int countDemo = 1;

    SecurePreferences preferences;
    private final String _USERNAME_ = "userId";
    private final String _PASSWORD_ = "password";
    private final String _SAVEDUSER_ = "saveduser";
    private Animation sendActivityVanish;
    private Animation takePicVanish;
    private Animation receiveActivityVanish;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		
		preferences = new SecurePreferences(this, "my-preferences", "TopSecretKey123kdd", true);
		
		//check for tutorial
		if(!preferences.getBoolean("firstTime", false)) {
			showActivityOverlay();
			preferences.putBoolean("firstTime", true);
		}
		
		setupActionAnimationListeners();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		NotificationManager nm = (NotificationManager)  getSystemService(NOTIFICATION_SERVICE);
	    nm.cancel(0);
	}
	
	@Override
	public void onBackPressed() {
	    moveTaskToBack(true);
	}
	
	
	private void showActivityOverlay() {
		final Dialog dialog = new Dialog(this,
		android.R.style.Theme_Black_NoTitleBar_Fullscreen);

		dialog.setContentView(R.layout.overlay_activity);

		final LinearLayout layout = (LinearLayout) dialog
		.findViewById(R.id.llOverlay_activity);
//		ImageView img = (ImageView) findViewById(R.drawable.startup1);
//		layout.addView(img);
		ImageView im =  (ImageView) dialog.findViewById(R.id.ivOverlayEntertask);
		im.setImageResource(R.drawable.startup1);
		layout.removeAllViews();
		layout.addView(im);
		layout.setBackgroundColor(Color.TRANSPARENT);
		layout.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			ImageView im =  (ImageView) dialog.findViewById(R.id.ivOverlayEntertask);
			layout.removeAllViews();
			countDemo++;
			switch (countDemo){
			
				case 2:
					im.setImageResource(R.drawable.startup2);					
					layout.addView(im);
					break;
				case 3:
					im.setImageResource(R.drawable.startup3);					
					layout.addView(im);
					break;
				case 4:
					im.setImageResource(R.drawable.startup4);					
					layout.addView(im);
					break;
				case 5:
					im.setImageResource(R.drawable.startup5);					
					layout.addView(im);
					break;
				case 6:
					im.setImageResource(R.drawable.startup6);					
					layout.addView(im);
					break;
				case 7:
					im.setImageResource(R.drawable.startup7);					
					layout.addView(im);
					break;
				default:
					dialog.dismiss();
				
			}
			

		}

		});

		dialog.show();
		}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_logout:
	        	preferences.removeValue(_USERNAME_);
				preferences.removeValue(_PASSWORD_);
				preferences.removeValue(_SAVEDUSER_);
				preferences.put("CheckBox_Value", "0");
				Intent intent = new Intent(MainActivity.this, SigninPage.class);
            	startActivity(intent);
            	finish();
	            return true;
	        case R.id.action_info:
	        	Utils.showAlert("About PicShake", "<p>Version 1.0</p><p>PicShake</p><p>Copyright 2014 Valyria Inc. All rights reserved.</p><p>This is only a non official Beta Version of the app</p><p><a href='http://hezzapp.appspot.com/terms'>Terms of Use</a></p><p><a href='http://hezzapp.appspot.com/privacy'>Privacy Policy</a></p>", MainActivity.this);
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	public void setupActionAnimationListeners()
	{
		//Send Activity Vanish Animation Listeners
		sendActivityVanish =AnimationUtils.loadAnimation(this,R.anim.vanish);
		sendActivityVanish.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				// Do nothing
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// Do nothing
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				Intent intent = new Intent(MainActivity.this, SenderActivity.class);
			    intent.putExtra("selectOrCamera", 0); //camera or select
			    // 0 is select , 1 is camera
			    startActivity(intent);
			}
		});
		
		//Take a pic Activity Vanish Animation Listeners
		takePicVanish =AnimationUtils.loadAnimation(this,R.anim.vanish);
		takePicVanish.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				// Do nothing
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// Do nothing
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				Intent intent = new Intent(MainActivity.this, SenderActivity.class);
			    intent.putExtra("selectOrCamera", 1); //camera or select
			     //0 is select , 1 is camera
			    startActivity(intent);	
			}
		});
		
		//Receive Activity Vanish Listeners
		receiveActivityVanish =AnimationUtils.loadAnimation(this,R.anim.vanish);
		receiveActivityVanish.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				// Do nothing
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// Do nothing
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
			    Intent intent = new Intent(MainActivity.this, ReceiverActivity.class);
			    startActivity(intent);
			}
		});
		
	}
	
	
	/** Called when the user clicks the Send button */
	public void sendActivity(View view) {
	    view.startAnimation(sendActivityVanish);
	}
	
	public void takePic(View view) {
	    view.startAnimation(takePicVanish);
	}
	
	public void receiveActivity(View view) {
	    view.startAnimation(receiveActivityVanish);
	}
	
}
