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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SignupPage extends Activity {
	
	private ProgressDialog progressDialog;
	EditText username;
	EditText email;
	SecurePreferences preferences;
    EditText password1;
    EditText password2;
    EditText firstname;
    EditText lastname;
	String SIGNUP_URL = "http://picshare.biz/customauth/simpleregister";
	HttpResponse response=null;
	
	String username_field;
	String password_field;
	String firstname_field;
	String lastname_field;
	private final String _USERNAME_ = "userId";
    private final String _PASSWORD_ = "password";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup_page);
		// Show the Up button in the action bar.
		setupActionBar();
		preferences = new SecurePreferences(this, "my-preferences", "TopSecretKey123kdd", true);
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Creating New Account!");
		progressDialog.setIndeterminate(false);
		username = (EditText) findViewById(R.id.username);
		email = (EditText) findViewById(R.id.email);
	    password1 = (EditText) findViewById(R.id.password1);
	    password2 = (EditText) findViewById(R.id.password2);
	    firstname = (EditText) findViewById(R.id.firstname);
	    lastname = (EditText) findViewById(R.id.lastname);
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
		getMenuInflater().inflate(R.menu.signup_page, menu);
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
	
	public void submit(View view) {
		
	    
	    if(!(password1.getText().toString().equals(password2.getText().toString())))
	    {
	    	Toast.makeText(getBaseContext(), "Passwords doesn't match", 
	        		Toast.LENGTH_SHORT).show();
	    }else{
	    	new SignUpRequest().execute();
	    	
	    	//Intent intent = new Intent(this, DisplayPage.class);
	    }
	    
	}
	
	private class SignUpRequest extends AsyncTask<Void, Void, String> {
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
            	username_field = username.getText().toString();
            	password_field = password1.getText().toString();
            	firstname_field = firstname.getText().toString();
            	lastname_field = lastname.getText().toString();
	            List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
	            postParameters.add(new BasicNameValuePair("username", username_field));
	            postParameters.add(new BasicNameValuePair("email", email.getText().toString()));
	            postParameters.add(new BasicNameValuePair("password1", password_field));
	            postParameters.add(new BasicNameValuePair("firstname", firstname_field));
	            postParameters.add(new BasicNameValuePair("lastname", lastname_field));
	
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
			switch (Integer.parseInt(result)) {

			case 2000:
				Toast.makeText(
						getBaseContext(),
						getResources().getString(R.string.registration_success),
						Toast.LENGTH_LONG).show();

				preferences.put(_USERNAME_, username_field);
				preferences.put(_PASSWORD_, password_field);
				preferences.put("CheckBox_Value", "1");
				Intent intent = new Intent(SignupPage.this, SigninPage.class);
				// Bundle b = new Bundle();
				// b.putParcelable("RESPONSE",(Parcelable) response);
				// intent.putExtras(b);
				startActivity(intent);
				finish();
				break;
				
			case 2001:
        		Toast.makeText(getBaseContext(), 
        				getResources().getString(R.string.username_in_use),
    	        		Toast.LENGTH_LONG).show();
        		break;
        	case 2002:
        		Toast.makeText(getBaseContext(), 
        				getResources().getString(R.string.invalid_email), 
    	        		Toast.LENGTH_LONG).show();
        		break;
        	case 2003:
        		Toast.makeText(getBaseContext(), 
        				getResources().getString(R.string.email_in_use), 
    	        		Toast.LENGTH_LONG).show();
        		break;
        	}
        }
    }

}
