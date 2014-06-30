package com.moataz.picshake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;

public class Splashscreen extends Activity {

	 private String checkBoxValue = "";
	 private SecurePreferences preferences;
	    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splashscreen);
		preferences = new SecurePreferences(this, "my-preferences", "TopSecretKey123kdd", true);
		checkBoxValue = preferences.getString("CheckBox_Value");
		if(checkBoxValue != null && checkBoxValue.equals("1"))
		{
			boolean passFlag = true;
			if(!isNetworkAvailable())
			{
				passFlag = false;
				Utils.exitAlert("No internet connection! Please verify your connection and try again.", Splashscreen.this);
			}
			
			if(!isGPSEnabled())
			{
				passFlag = false;
				Utils.exitAlert("No GPS! Please verify your GPS is enabled and try again.", Splashscreen.this);
			}
			if(passFlag)
			{
				Intent intent = new Intent(Splashscreen.this, MainActivity.class);
            	startActivity(intent);
            	finish();
			}
		}else{
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
		    @Override
		    public void run() {
		        // Do something after 5s = 5000ms
		    	Intent intent = new Intent(getApplicationContext(),SigninPage.class);
		    	startActivity(intent);
		    	finish();
		    }
		}, 2000);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splashscreen, menu);
		return true;
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

}
