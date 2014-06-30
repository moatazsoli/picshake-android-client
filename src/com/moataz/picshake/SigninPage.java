package com.moataz.picshake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class SigninPage extends Activity {

	EditText username;
    EditText password;
    CheckBox stay_signed;
    String SIGNIN_URL = "http://picshare.biz/customauth/loginme";
    HttpResponse response=null;
    private ProgressDialog progressDialog;
    private ProgressDialog progressDialogmsg;
    String checkBoxValue = "";
    SecurePreferences preferences;
    private final String _USERNAME_ = "userId";
    private final String _PASSWORD_ = "password";
    private final String _SAVEDUSER_ = "saveduser";
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin_page);
		// Show the Up button in the action bar.
		boolean resume = true;
		if (!isGPSEnabled()) {
			showGpsSettingsAlert();
		}
		if(!isNetworkAvailable())
		{
			showNoInternetSettingsAlert();
		}
		
			username = (EditText) findViewById(R.id.usernamein);
			password = (EditText) findViewById(R.id.password1in);
			stay_signed = (CheckBox) findViewById(R.id.keepmesignedin);

			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Signing In...");
			progressDialog.setIndeterminate(false);

			progressDialogmsg = new ProgressDialog(this);
			progressDialogmsg.setMessage("Please wait...");
			progressDialogmsg.setIndeterminate(false);
			preferences = new SecurePreferences(this, "my-preferences", "TopSecretKey123kdd", true);
			checkBoxValue = preferences.getString("CheckBox_Value");
			if(checkBoxValue != null && checkBoxValue.equals("1"))
			{
				String user = preferences.getString(_USERNAME_);
				String pass = preferences.getString(_PASSWORD_);
				username.setText(user);
				password.setText(pass);
				stay_signed.setChecked(true);
				new SignInRequest().execute();
			}
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
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.signin_page, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void signin(View view) {
    	new SignInRequest().execute();
	    	//Intent intent = new Intent(this, DisplayPage.class);
	}
	
	public void signup(View view) {
	    Intent intent = new Intent(this, SignupPage.class);
	    startActivity(intent);
	    finish();
	}
	
	public void resetCheckBoxValue()
	{
		preferences.put("CheckBox_Value", "");
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                                                        INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }
	
	private class SignInRequest extends AsyncTask<Void, Void, String> {
		String result = null;
		String _username;
		String _password;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }
 
        @Override
        protected String doInBackground(Void... URL) {
        	
        	if(!isNetworkAvailable())
        	{
        		return "3005";
        	}else{
        		HttpClient httpClient = new DefaultHttpClient();
        		HttpPost request = new HttpPost(SIGNIN_URL);

        		_username = username.getText().toString();
        		_password = password.getText().toString();
        		if(_username.equals("") || _password.equals(""))
        		{
        			return "3004";
        		}
        		try {
        			List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        			postParameters.add(new BasicNameValuePair("username", _username));
        			postParameters.add(new BasicNameValuePair("password", _password));

        			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(
        					postParameters);

        			request.setEntity(formEntity);
        			HttpResponse postresponse = httpClient.execute(request);

        			//set global response to pass to next view
        			response = postresponse;
        			BufferedReader reader1 = new BufferedReader(new InputStreamReader(
        					postresponse.getEntity().getContent(), "UTF-8"));

        			String sResponse1;
        			StringBuilder s1 = new StringBuilder();

        			while ((sResponse1 = reader1.readLine()) != null) {
        				s1 = s1.append(sResponse1);
        			}
        			result = s1.toString();

        			return result;

        		} catch(Exception e) {
        			// Do something about exceptions
        			result = e.getMessage();
        		}
        		return  result;
        	}
        }
 
        @Override
        protected void onPostExecute(String result) {
//        	Toast.makeText(getBaseContext(), result, 
//	        		Toast.LENGTH_LONG).show();
        	
        	switch(Integer.parseInt(result)){
        	case 3000:
        		preferences.put(_SAVEDUSER_, _username);
        		if(stay_signed.isChecked())
        		{
        			preferences.put(_USERNAME_, username.getText().toString());
        			preferences.put(_PASSWORD_, password.getText().toString());
        			preferences.put("CheckBox_Value", "1");
        		}else{
        			preferences.put("CheckBox_Value", "0");
        		}
        		Intent intent = new Intent(SigninPage.this, MainActivity.class);
            	startActivity(intent);
            	progressDialog.dismiss();
            	Toast.makeText(getBaseContext(), 
        				getResources().getString(R.string.login_success), 
    	        		Toast.LENGTH_LONG).show();
            	finish();
        		break;
        	case 3001:
        		progressDialog.dismiss();
        		Toast.makeText(getBaseContext(), 
        				getResources().getString(R.string.user_inactive),
    	        		Toast.LENGTH_LONG).show();
        		break;
        	case 3003:
        		progressDialog.dismiss();
        		Toast.makeText(getBaseContext(), 
        				getResources().getString(R.string.invalid_login), 
        				Toast.LENGTH_LONG).show();
        		break;
        	case 3004:
        		progressDialog.dismiss();
        		Toast.makeText(getBaseContext(), 
        				getResources().getString(R.string.empty_user_or_pass), 
        				Toast.LENGTH_LONG).show();
        		break;
        	case 3005:
        		progressDialog.dismiss();
        		Toast.makeText(getBaseContext(), 
        				getResources().getString(R.string.no_internet), 
        				Toast.LENGTH_LONG).show();
        		break;
        	default:
        		progressDialog.dismiss();
        	}
        }
	}

}
