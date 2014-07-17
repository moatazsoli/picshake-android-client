package com.moataz.picshake;


import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

public class MainActivity extends FragmentActivity {
	
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
		
//		//check for tutorial
		if(!preferences.getBoolean("firstTime", false)) {
	
			showTutorialFragment();
			showTutorialAlert();
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
		case R.id.action_share:
			String message = "@PicShake! SNAP, SHAKE, SHARE! With a new #ShakeToShare twist.\n#Selfies #Pics #GroupPics.\n https://play.google.com/store/apps/details?id=com.moataz.picshake&hl=en";
			Intent share = new Intent(Intent.ACTION_SEND);
			share.setType("text/plain");
			share.putExtra(Intent.EXTRA_TEXT, message);

			startActivity(Intent.createChooser(share, "Choose an app to share through"));
			return true;


		case R.id.action_logout:
			preferences.removeValue(_USERNAME_);
			preferences.removeValue(_PASSWORD_);
			preferences.removeValue(_SAVEDUSER_);
			preferences.put("CheckBox_Value", "0");
			Intent intent = new Intent(MainActivity.this, SigninPage.class);
			startActivity(intent);
			finish();
			return true;

		case R.id.action_tutorial:	        
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
			alertDialog.setTitle("Tutorial Completed")
			.setMessage("Would you like to view the tutorial?")	       
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					// User clicked OK, so save the mSelectedItems results somewhere
					// or return them to the component that opened the dialog	
					dialog.dismiss();		               
				}
			})
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					// User clicked OK, so save the mSelectedItems results somewhere
					// or return them to the component that opened the dialog		           					
					dialog.dismiss();
					showTutorialFragment();	               
				}
			});
			alertDialog.create();
			alertDialog.show();
			return true;

		case R.id.action_info:
			Utils.showAlert("About PicShake", "<p>Version 1.2</p>" +
					"<p>PicShake</p>" +
					"<p>PicShake is an awesome app used for sharing beautiful moments captured in pictues in a very easy and a straight way with no need for contact info</p>"+
					"<p>Checkout our website <a href='http://www.picshake.ca'>www.picshake.ca</p>"+
					"<p><a href='http://hezzapp.appspot.com/terms'>Terms of Use</a></p>" +
					"<p><a href='http://hezzapp.appspot.com/privacy'>Privacy Policy</a></p>" +
					"</a> Copyright 2014 Valyria Inc. All rights reserved.</p>", MainActivity.this);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	public void setupActionAnimationListeners()
	{
		//Send Activity Vanish Animation Listeners
		sendActivityVanish =AnimationUtils.loadAnimation(this,R.anim.vanish);
		sendActivityVanish.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				Intent intent = new Intent(MainActivity.this, SenderActivity.class);
			    intent.putExtra("selectOrCamera", 0); //camera or select
			    // 0 is select , 1 is camera
			    startActivity(intent);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// Do nothing
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				
			}
		});
		
		//Take a pic Activity Vanish Animation Listeners
		takePicVanish =AnimationUtils.loadAnimation(this,R.anim.vanish);
		takePicVanish.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				Intent intent = new Intent(MainActivity.this, SenderActivity.class);
			    intent.putExtra("selectOrCamera", 1); //camera or select
			     //0 is select , 1 is camera
			    startActivity(intent);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// Do nothing
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
					
			}
		});
		
		//Receive Activity Vanish Listeners
		receiveActivityVanish =AnimationUtils.loadAnimation(this,R.anim.vanish);
		receiveActivityVanish.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				Intent intent = new Intent(MainActivity.this, ReceiverActivity.class);
			    startActivity(intent);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// Do nothing
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
			    
			}
		});
		
	}
	
	
	/** Called when the user clicks the Send button */
	public void sendActivity(View view) {
		overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
	    view.startAnimation(sendActivityVanish);
	}
	
	public void takePic(View view) {
		overridePendingTransition(R.anim.anim_in_bottom, R.anim.anim_out_bottom);
	    view.startAnimation(takePicVanish);
	}
	
	public void receiveActivity(View view) {
		overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
	    view.startAnimation(receiveActivityVanish);
	}
	
	public void showTutorialAlert()
	{
		String[] pref = {"Don't show tutorial again"};
		//final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("Tutorial Completed")		
		.setMultiChoiceItems(pref, null, new DialogInterface.OnMultiChoiceClickListener() {				
	
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				// TODO Auto-generated method stub
				 switch (which) {
                 case 0:                                                       	
                 	preferences.putBoolean("firstTime", true);          		    
                     break;                   
                 default:
                     break;
                 }
//				
			}          
           })
           .setPositiveButton("OK", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int id) {
                   // User clicked OK, so save the mSelectedItems results somewhere
                   // or return them to the component that opened the dialog	
            	   dialog.dismiss();		               
               }
           });
		alertDialog.create();
		alertDialog.show();
	}
	
	public void showTutorialFragment()
	{
        android.app.FragmentManager fragmentManager = getFragmentManager();
        android.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        TutorialFragment fragment = new TutorialFragment();
        fragmentTransaction.replace(R.id.FragmentContainer, fragment);
        fragmentTransaction.commit();
	}

	
}
