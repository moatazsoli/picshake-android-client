package com.moataz.picshake;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class FacebookFragment extends Fragment {
	
	private static final String TAG = "MainFragment";
	
	private UiLifecycleHelper uiHelper;
	
	public String firstname, lastname, email;
	
	Session mSession;
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, 
	        Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.facebook_login, container, false);
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
	    LoginButton authButton = (LoginButton) view.findViewById(R.id.login_button);
	    authButton.setFragment(this);
	    authButton.setReadPermissions(Arrays.asList("user_location", "email"));
	    return view;
	}
	
	private void buildUserInfoDisplay(GraphUser user) {
	    StringBuilder userInfo = new StringBuilder("");
	    firstname =  user.getFirstName();
	    lastname =  user.getLastName();
	    email =  user.asMap().get("email").toString();
	}

	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (state.isOpened()) {

			if (mSession == null || isSessionChanged(session)) {
			    // Request user data and show the results
			    mSession = session;
			    Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
	
					@Override
					public void onCompleted(GraphUser user, Response response) {
						// TODO Auto-generated method stub
						 if (user != null) {
				                // Display the parsed user info
							SigninPage sp = (SigninPage) getActivity();
							sp.fbSignup(user.getFirstName(), user.getLastName(), user.asMap().get("email").toString());
				            }
						
					}
			    });
			}
		}
	}
	
	private boolean isSessionChanged(Session session) {

	    // Check if session state changed
	    if (mSession.getState() != session.getState())
	        return true;

	    // Check if accessToken changed
	    if (mSession.getAccessToken() != null) {
	        if (!mSession.getAccessToken().equals(session.getAccessToken()))
	            return true;
	    }
	    else if (session.getAccessToken() != null) {
	        return true;
	    }

	    // Nothing changed
	    return false;
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    // For scenarios where the main activity is launched and user
	    // session is not null, the session state change notification
	    // may not be triggered. Trigger it if it's open/closed.
	    Session session = Session.getActiveSession();
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }

	    uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
	

}
