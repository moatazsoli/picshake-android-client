package com.moataz.picshake;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//TODO:remove these checks and move them only to send
		// activity when the offline token is implemented
		if (!isGPSEnabled()) {
			showGpsSettingsAlert();
		}
		if(!isNetworkAvailable())
		{
			showNoInternetSettingsAlert();
		}
		

	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	public boolean isGPSEnabled(){
		// flag for GPS status
		boolean isGPSEnabled = false;

		// flag for network status
		boolean isNetworkEnabled = false;

		// Declaring a Location Manager
		LocationManager locationManager = null;
		// flag for GPS status
		boolean canGetLocation = false;
		try {
			locationManager = (LocationManager) this
					.getSystemService(LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				return false;
			}
			return true;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
		
	}
	
	/**
	 * Function to show settings alert dialog
	 * On pressing Settings button will lauch Settings Options
	 * */
	public void showGpsSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
   	 
        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");
 
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
 
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            	startActivity(intent);
            }
        });
 
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            android.os.Process.killProcess(android.os.Process.myPid());
            dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();
	}
	
	public void showNoInternetSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
   	 
        // Setting Dialog Title
        alertDialog.setTitle("Internet settings");
 
        // Setting Dialog Message
        alertDialog.setMessage("Internet is not enabled. Do you want to go to settings menu?");
 
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	Intent intent = new Intent(Settings.ACTION_SETTINGS);
            	startActivity(intent);
            }
        });
 
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	android.os.Process.killProcess(android.os.Process.myPid());
            dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	/** Called when the user clicks the Send button */
	public void sendActivity(View view) {
		Animation vanish =AnimationUtils.loadAnimation(this,R.anim.vanish);
	    vanish.setAnimationListener(new Animation.AnimationListener() {
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
	    view.startAnimation(vanish);
	}
	
	public void takePic(View view) {
		Animation vanish =AnimationUtils.loadAnimation(this,R.anim.vanish);
	    vanish.setAnimationListener(new Animation.AnimationListener() {
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
	    view.startAnimation(vanish);
	}
	
	public void receiveActivity(View view) {
		Animation vanish =AnimationUtils.loadAnimation(this,R.anim.vanish);
	    vanish.setAnimationListener(new Animation.AnimationListener() {
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
	    view.startAnimation(vanish);
	}
	
	
		
//	/**
//	 * Function to show alert dialog
//	 * */
//	public void showAlert(String aInTitle, String aInMessage, Context aInContext){
//		AlertDialog.Builder alertDialog = new AlertDialog.Builder(aInContext);
//   	 
//        // Setting Dialog Title
//        alertDialog.setTitle(aInTitle);
// 
//        // Setting Dialog Message
//        alertDialog.setMessage(aInMessage);
// 
//        // on pressing cancel button
//        alertDialog.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//            dialog.cancel();
//            }
//        });
// 
//        // Showing Alert Message
//        alertDialog.show();
//	}

}
