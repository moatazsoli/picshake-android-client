package com.moataz.picshake;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class SigninPage extends Activity {

	EditText username;
    EditText password;
    CheckBox stay_signed;
    String SIGNUP_URL = "http://picshare.biz/customauth/loginme";
    HttpResponse response=null;
    private ProgressDialog progressDialog;
    String checkBoxValue = "";
    SecurePreferences preferences;
    private final String _USERNAME_ = "userId";
    private final String _PASSWORD_ = "password";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin_page);
		// Show the Up button in the action bar.
		setupActionBar();
		
		username = (EditText) findViewById(R.id.usernamein);
		password = (EditText) findViewById(R.id.password1in);
		stay_signed = (CheckBox) findViewById(R.id.keepmesignedin);
		progressDialog = new ProgressDialog(this);
		// Set progressdialog title
//		progressDialog.setTitle("Download Image");
		// Set progressdialog message
		progressDialog.setMessage("Signing In...");
		progressDialog.setIndeterminate(false);
		// Show progressdialog
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

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

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
		if(stay_signed.isChecked())
		{
			preferences.put(_USERNAME_, username.getText().toString());
			preferences.put(_PASSWORD_, password.getText().toString());
			preferences.put("CheckBox_Value", "1");
		}else{
			preferences.put("CheckBox_Value", "0");
		}
		
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
	
	private class SignInRequest extends AsyncTask<Void, Void, String> {
		String result = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }
 
        @Override
        protected String doInBackground(Void... URL) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost request = new HttpPost(SIGNUP_URL);
            
            try {
	            List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
	            postParameters.add(new BasicNameValuePair("username", username.getText().toString()));
	            postParameters.add(new BasicNameValuePair("password", password.getText().toString()));
	
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
 
        @Override
        protected void onPostExecute(String result) {
//        	Toast.makeText(getBaseContext(), result, 
//	        		Toast.LENGTH_LONG).show();
        	progressDialog.dismiss();
        	switch(Integer.parseInt(result)){
        	
        	case 3000:
        		Toast.makeText(getBaseContext(), 
        				getResources().getString(R.string.login_success), 
    	        		Toast.LENGTH_LONG).show();
        		Intent intent = new Intent(SigninPage.this, MainActivity.class);
            	startActivity(intent);
            	finish();
        		break;
        	case 3001:
        		Toast.makeText(getBaseContext(), 
        				getResources().getString(R.string.user_inactive),
    	        		Toast.LENGTH_LONG).show();
        		break;
        	case 3003:
        		Toast.makeText(getBaseContext(), 
        				getResources().getString(R.string.invalid_login), 
    	        		Toast.LENGTH_LONG).show();
        		break;
        	}
        }
    }

}
