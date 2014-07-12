package com.moataz.picshake;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.moataz.picshake.ui.ImageGridActivity;
import com.nhaarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;

public class ReceiverActivity extends FragmentActivity implements
LocationListener,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,
AccelerometerListener {

	// A request to connect to Location Services
	private LocationRequest mLocationRequest;
	public final static int RESULT_LOAD_IMAGE = 5000;
	public final static String NO_IMAGE_FOUND = "5002";
	
	public List<String> imgUrlsList;
	
	//selection checkmark
	Bitmap checkmarkBitmap;
	Drawable checkmarkDrawable;
	// Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0; 

    private final String _SAVEDUSER_ = "saveduser";
	private SecurePreferences preferences;
	private String username;
	private ArrayList<String> thumbs= new ArrayList<String>();
	private ArrayList<String> pics= new ArrayList<String>();
    private int serverResponseCode = 0;
    private ProgressDialog mProgressDialog;
    HashMap<String, Object> y;
    ArrayList<HashMap<String, Object>> imagesTemp;
    HashMap<Integer, String> selectedPositions;
    Bitmap imgBitmapTemp;
	// Stores the current instantiation of the location client in this object
	private LocationClient mLocationClient;
	
	ArrayList<HashMap<String, Object>> thumbnailsMap;
	private String mLatitude;
	
	private String mLongitude;
	// Handles to UI widgets
//	private ProgressBar mActivityIndicator;
	private TextView mConnectionState;
	private TextView mConnectionStatus;
	private EditText passcode;

	// Handle to SharedPreferences for this app
	SharedPreferences mPrefs;
	public boolean mActionModeStarted = false;

	// Handle to a SharedPreferences editor
	SharedPreferences.Editor mEditor;

	/*
	 * Note if updates have been turned on. Starts out as "false"; is set to "true" in the
	 * method handleRequestSuccess of LocationUpdateReceiver.
	 *
	 */
	boolean mUpdatesRequested = true;
	
	private NotificationManager notificationManager;
	
	//Public tags
	private ProgressBar spinner;
	private ArrayAdapter<String> simpleAdpt;
	private ArrayList<String> publicTags = new ArrayList<String>();
	private ListView lv;

	/*
	 * Initialize the Activity
	 */
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receiver);
		// Show the Up button in the action bar.
		setupActionBar();
		overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
		preferences = new SecurePreferences(this, "my-preferences", "TopSecretKey123kdd", true);
		username = preferences.getString(_SAVEDUSER_);
		
		imgUrlsList = new ArrayList<String>();
		// Upload stuff
        passcode = (EditText)findViewById(R.id.passcode_recv);
		//***
		
		// Get handles to the UI view objects
//		mAddress = (TextView) findViewById(R.id.address);
//		mActivityIndicator = (ProgressBar) findViewById(R.id.address_progress);

		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();

		/*
		 * Set the update interval
		 */
		mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		// Set the interval ceiling to one minute
		mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

		// Note that location updates are off until the user turns them on
		mUpdatesRequested = true;

		// Open Shared Preferences
		mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

		// Get an editor
		mEditor = mPrefs.edit();

		mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, true);
		mEditor.commit();
		/*
		 * Create a new location client, using the enclosing class to
		 * handle callbacks.
		 */
		mLocationClient = new LocationClient(this, this, this);
		
		notificationManager = (NotificationManager)  getSystemService(NOTIFICATION_SERVICE);
		
		/*
		 * public tags list setup
		 */
		//connectAndStartLocationUpdates();
		spinner = (ProgressBar)findViewById(R.id.progressBar1);
	    spinner.setVisibility(View.GONE);
		
		simpleAdpt = new ArrayAdapter<String>(ReceiverActivity.this,
                R.layout.mytextview, publicTags);
		lv = (ListView) findViewById(R.id.listView);
		
		AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(simpleAdpt);
		alphaInAnimationAdapter.setAbsListView(lv);
		lv.setAdapter(alphaInAnimationAdapter);
		
	    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	 
	        public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
	                                long id) {
	                 
	         
	            // We know the View is a TextView so we can cast it
	            TextView clickedView = (TextView) view;
	            passcode.setText(clickedView.getText());
	    
	        }
	   });
	    
	    
	}
	
	// #######################################################################
	// Thumbnails Grid
	// #######################################################################

	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);  
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(mActionModeStarted) {
	        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
	           // handle your back button code here
	           return true; // consumes the back key event - ActionMode is not finished
	        }
	    }
	    return super.dispatchKeyEvent(event);
	}
	
//	public void viewThumbnails() {
//
//		lv.setVisibility(View.GONE);
//		passcode.setVisibility(View.GONE);
//		textView1.setVisibility(View.GONE);
//		mGrid.setVisibility(View.VISIBLE);
//		mGrid.setAdapter(new ThumbnailsAdapter());
//		mGrid.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
//		mGrid.setMultiChoiceModeListener(new MultiChoiceModeListener());
//		Toast.makeText(ReceiverActivity.this, "Press and Hold to Select the Pictures", 
//				Toast.LENGTH_LONG).show();
//
//	}








	public class MultiChoiceModeListener implements
			GridView.MultiChoiceModeListener {
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.setTitle("Select Items");
			mode.setSubtitle("One item selected");
			return true;
		}

		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			mActionModeStarted = true;
			return true;
		}

		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return true;
		}

		@SuppressWarnings("unchecked")
		public void onDestroyActionMode(ActionMode mode) {
			mActionModeStarted = false;
		}



		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {

		}

	}

	public class CheckableLayout extends FrameLayout implements Checkable {
		private boolean mChecked;

		public CheckableLayout(Context context) {
			super(context);
		}

		@SuppressLint("NewApi")
		public void setChecked(boolean checked) {
			mChecked = checked;
//			setBackground(checked ? getResources().getDrawable(R.color.blue)
//					: null);
			setForeground(checked ?
					 checkmarkDrawable
					 : null);
		}

		public boolean isChecked() {
			return mChecked;
		}

		public void toggle() {
			setChecked(!mChecked);
		}

	}

	public class ThumbnailsAdapter extends BaseAdapter {
		public ThumbnailsAdapter() {
			selectedPositions = new HashMap<Integer, String>();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			CheckableLayout l;
			ImageView i;

			if (convertView == null) {
				i = new ImageView(ReceiverActivity.this);
				i.setScaleType(ImageView.ScaleType.CENTER_CROP);
				i.setPadding(1, 1, 1, 1);
//				i.setVerticalFadingEdgeEnabled(true);
				//i.setLayoutParams(new ViewGroup.LayoutParams(300,300));
				l = new CheckableLayout(ReceiverActivity.this);
				l.setLayoutParams(new GridView.LayoutParams(
						GridView.LayoutParams.MATCH_PARENT,
						dpToPx(120)));
				l.addView(i);
			} else {
				l = (CheckableLayout) convertView;
				i = (ImageView) l.getChildAt(0);
			}

			HashMap<String, Object> info = thumbnailsMap.get(position);
			imgBitmapTemp = (Bitmap) info.get("img");
			Drawable drawable = new BitmapDrawable(getResources(), imgBitmapTemp );
				i.setImageDrawable(drawable);
			return l;
		}

		public final int getCount() {
			return thumbnailsMap.size();
		}

		public final Object getItem(int position) {
			return thumbnailsMap.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}
	}
	

	public int dpToPx(int dp) {
	    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
	    return px;
	}
	
	
	//#######################################################################
	//  Thumbnails Grid End
	//#######################################################################
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(false);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.receiver, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			stopUpdates();
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
	
	private void connectAndStartLocationUpdates()
	{
		mUpdatesRequested = true;
		mLocationClient.connect();
	}
	
	
	/*
	 * Called when the Activity is no longer visible at all.
	 * Stop updates and disconnect.
	 */
	@Override
	public void onStop() {

		// If the client is connected
		if (mLocationClient.isConnected()) {
			stopPeriodicUpdates();
		}

		// After disconnect() is called, the client is considered "dead".
		mLocationClient.disconnect();
		
		//Check device supported Accelerometer senssor or not
        if (AccelerometerManager.isListening()) {
        	
        	//Start Accelerometer Listening
			AccelerometerManager.stopListening();
			
//			Toast.makeText(getBaseContext(), "onStop Accelerometer Stoped", 
//					Toast.LENGTH_LONG).show();
        }

		super.onStop();
	}
	/*
	 * Called when the Activity is going into the background.
	 * Parts of the UI may be visible, but the Activity is inactive.
	 */
	@Override
	public void onPause() {

		// Save the current setting for updates
		mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, mUpdatesRequested);
		mEditor.commit();

		if (AccelerometerManager.isListening()) {

			//Start Accelerometer Listening
			AccelerometerManager.stopListening();

			//			Toast.makeText(getBaseContext(), "onStop Accelerometer Stopped", 
			//					Toast.LENGTH_LONG).show();
		}
		super.onPause();
	}

	/*
	 * Called when the Activity is restarted, even before it becomes visible.
	 */
	@Override
	public void onStart() {

		super.onStart();

		/*
		 * Connect the client. Don't re-start any requests here;
		 * instead, wait for onResume()
		 */
		mLocationClient.connect();

	}
	/*
	 * Called when the system detects that this Activity is now visible.
	 */
	@Override
	public void onResume() {
		super.onResume();

		// If the app already has a setting for getting location updates, get it
		if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED)) {
			mUpdatesRequested = mPrefs.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);

			// Otherwise, turn off location updates until requested
		} else {
			mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
			mEditor.commit();
		}
		
		if (AccelerometerManager.isSupported(this)) {
//          Toast.makeText(getBaseContext(), "onResume Accelerometer Started", 
//    		Toast.LENGTH_LONG).show();
        	//Start Accelerometer Listening
			AccelerometerManager.startListening(this);
        }

		notificationManager.cancel(0);
	}

	/*
	 * Handle results returned to this Activity by other Activities started with
	 * startActivityForResult(). In particular, the method onConnectionFailed() in
	 * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
	 * start an Activity that handles Google Play services problems. The result of this
	 * call returns here, to onActivityResult.
	 * Also used for handling image selection
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		// Choose what to do based on the request code
		switch (requestCode) {
		
		// If the request code matches the code sent in onConnectionFailed
		case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

			switch (resultCode) {
			// If Google Play services resolved the problem
			case Activity.RESULT_OK:

				// Log the result
				Log.d(LocationUtils.APPTAG, getString(R.string.resolved));

				// Display the result
				mConnectionState.setText(R.string.connected);
				mConnectionStatus.setText(R.string.resolved);
				break;

				// If any other result was returned by Google Play services
			default:
				// Log the result
				Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));

				// Display the result
				mConnectionState.setText(R.string.disconnected);
				mConnectionStatus.setText(R.string.no_resolution);

				break;
			}

			// If any other request code was received
		default:
			// Report that this Activity received an unknown requestCode
			Log.d(LocationUtils.APPTAG,
					getString(R.string.unknown_activity_request_code, requestCode));

			break;
		}
	}

	/**
	 * Verify that Google Play services is available before making a request.
	 *
	 * @return true if Google Play services is available, otherwise false
	 */
	private boolean servicesConnected() {

		// Check that Google Play services is available
		int resultCode =
				GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));

			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Display an error dialog
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
			if (dialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(dialog);
				errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
			}
			return false;
		}
	}

	/**
	 * Invoked by the "Get Location" button.
	 *
	 * Calls getLastLocation() to get the current location
	 *
	 * @param v The view object associated with this method, in this case a Button.
	 */
	public void getLocation(View v) {

		// If Google Play Services is available
		if (servicesConnected()) {

			// Get the current location
			Location currentLocation = mLocationClient.getLastLocation();
		}
	}
	
	public String getLat() {
		if (servicesConnected()) {

			// Get the current location
			Location currentLocation = mLocationClient.getLastLocation();
			return LocationUtils.getLat(this, currentLocation);
		}
		return LocationUtils.EMPTY_STRING;
	}
	
	public String getLng() {
		if (servicesConnected()) {

			// Get the current location
			Location currentLocation = mLocationClient.getLastLocation();
			return LocationUtils.getLng(this, currentLocation);
		}
		return LocationUtils.EMPTY_STRING;
	}
	
	

/*	*//**
	 * Invoked by the "Get Address" button.
	 * Get the address of the current location, using reverse geocoding. This only works if
	 * a geocoding service is available.
	 *
	 * @param v The view object associated with this method, in this case a Button.
	 *//*
	// For Eclipse with ADT, suppress warnings about Geocoder.isPresent()
	@SuppressLint("NewApi")
	public void getAddress(View v) {

		// In Gingerbread and later, use Geocoder.isPresent() to see if a geocoder is available.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && !Geocoder.isPresent()) {
			// No geocoder is present. Issue an error message
			Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
			return;
		}

		if (servicesConnected()) {

			// Get the current location
			Location currentLocation = mLocationClient.getLastLocation();

			// Turn the indefinite activity indicator on
			mActivityIndicator.setVisibility(View.VISIBLE);

			// Start the background task
			(new SenderActivity.GetAddressTask(this)).execute(currentLocation);
		}
	}*/

	/**
	 * Invoked by the "Start Updates" button
	 * Sends a request to start location updates
	 *
	 * @param v The view object associated with this method, in this case a Button.
	 */
	public void startUpdates(View v) {
		mUpdatesRequested = true;

		if (servicesConnected()) {
			startPeriodicUpdates();
		}
	}
	
	public void startUpdates() {
		mUpdatesRequested = true;

		if (servicesConnected()) {
			startPeriodicUpdates();
		}
	}

	/**
	 * Invoked by the "Stop Updates" button
	 * Sends a request to remove location updates
	 * request them.
	 *
	 * @param v The view object associated with this method, in this case a Button.
	 */
	public void stopUpdates(View v) {
		mUpdatesRequested = false;

		if (servicesConnected()) {
			stopPeriodicUpdates();
		}
	}
	
	public void stopUpdates() {
		mUpdatesRequested = false;

		if (servicesConnected()) {
			stopPeriodicUpdates();
		}
	}

	/*
	 * Called by Location Services when the request to connect the
	 * request the current location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle bundle) {

		if (mUpdatesRequested) {
			startPeriodicUpdates();
			new LoadPublicTags().execute();
		}
	}

	/*
	 * Called by Location Services if the connection to the
	 * location client drops because of an error.
	 */
	@Override
	public void onDisconnected() {
//		mConnectionStatus.setText(R.string.disconnected);
	}

	/*
	 * Called by Location Services if the attempt to
	 * Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

		/*
		 * Google Play services can resolve some errors it detects.
		 * If the error has a resolution, try sending an Intent to
		 * start a Google Play services activity that can resolve
		 * error.
		 */
		if (connectionResult.hasResolution()) {
			try {

				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(
						this,
						LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */

			} catch (IntentSender.SendIntentException e) {

				// Log the error
				e.printStackTrace();
			}
		} else {

			// If no resolution is available, display a dialog to the user with the error.
			showErrorDialog(connectionResult.getErrorCode());
		}
	}
	
	public static List<String> getItemsFromJsonReply(String aInStr)
	{
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(aInStr);
			if(jsonObj.has("list"))
			{
				String list = jsonObj.getString("list");
				list = list.substring(1,list.length()-1);
				list = list.replace("\"", "");
				list = list.replace("\\", "");
				List<String> items = Arrays.asList(list.split("\\s*,\\s*"));
				return items;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public HashMap<String, Object> getItemsMapFromJsonReply(String aInStr)
	{
		 // images JSONArray
	    JSONArray images = null;
	    int totalSize = 0;
	    ArrayList<HashMap<String, Object>> imageList = new ArrayList<HashMap<String, Object>>();
	    HashMap<String,Object> return_val = new HashMap<String, Object>();
	    
	    JSONObject jsonObj;
	    if (aInStr != null) {
	    	try {
	    		jsonObj = new JSONObject(aInStr);
	    		if(jsonObj.has("list") && jsonObj.has("total_size"))
	    		{
	    			totalSize = jsonObj.getInt("total_size");
	    			images = jsonObj.getJSONArray("list");
	    			
	    			// looping through All Contacts
                    for (int i = 0; i < images.length(); i++) {
                        JSONObject c = images.getJSONObject(i);
                         
                        int no = c.getInt("no");
                        int size = c.getInt("size");
                        String url = c.getString("url");
                        String thumb = c.getString("thumb");
                        thumbs.add(thumb);
                        pics.add(url);
                        // tmp hashmap for single contact
                        HashMap<String, Object> image = new HashMap<String, Object>();
 
                        // adding each child node to HashMap key => value
                        image.put("no", no);
                        image.put("size", size);
                        image.put("url", url);
                        image.put("thumb", thumb);
 
                        // adding contact to contact list
                        imageList.add(image);
                    }
                    
	    			
                    return_val.put("list", imageList);
                    return_val.put("total_size", totalSize);
                    return_val.put("counter", images.length());
                    return return_val;
	    		}
	    	} catch (JSONException e) {
	    		e.printStackTrace();
	    	}
	    }
		
		return null;
	}
	
	public static int getCounterFromJsonReply(String aInStr)
	{
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(aInStr);
			if(jsonObj.has("counter"))
			{
				return jsonObj.getInt("counter");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * Report location updates to the UI.
	 *
	 * @param location The updated location.
	 */
	@Override
	public void onLocationChanged(Location location) {

		// Report to the UI that the location was updated
//		mConnectionStatus.setText(R.string.location_updated);

		// In the UI, set the latitude and longitude to the value received
//		mLatLng.setText(LocationUtils.getLatLng(this, location));
	}

	/**
	 * In response to a request to start updates, send a request
	 * to Location Services
	 */
	private void startPeriodicUpdates() {

		mLocationClient.requestLocationUpdates(mLocationRequest, this);
//		mConnectionState.setText(R.string.location_requested);
	}

	/**
	 * In response to a request to stop updates, send a request to
	 * Location Services
	 */
	private void stopPeriodicUpdates() {
		mLocationClient.removeLocationUpdates(this);
//		mConnectionState.setText(R.string.location_updates_stopped);
	}

/*	*//**
	 * An AsyncTask that calls getFromLocation() in the background.
	 * The class uses the following generic types:
	 * Location - A {@link android.location.Location} object containing the current location,
	 *            passed as the input parameter to doInBackground()
	 * Void     - indicates that progress units are not used by this subclass
	 * String   - An address passed to onPostExecute()
	 *//*
	protected class GetAddressTask extends AsyncTask<Location, Void, String> {

		// Store the context passed to the AsyncTask when the system instantiates it.
		Context localContext;

		// Constructor called by the system to instantiate the task
		public GetAddressTask(Context context) {

			// Required by the semantics of AsyncTask
			super();

			// Set a Context for the background task
			localContext = context;
		}

		*//**
		 * Get a geocoding service instance, pass latitude and longitude to it, format the returned
		 * address, and return the address to the UI thread.
		 *//*
		@Override
		protected String doInBackground(Location... params) {
			
			 * Get a new geocoding service instance, set for localized addresses. This example uses
			 * android.location.Geocoder, but other geocoders that conform to address standards
			 * can also be used.
			 
			Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());

			// Get the current location from the input parameter list
			Location location = params[0];

			// Create a list to contain the result address
			List <Address> addresses = null;

			// Try to get an address for the current location. Catch IO or network problems.
			try {

				
				 * Call the synchronous getFromLocation() method with the latitude and
				 * longitude of the current location. Return at most 1 address.
				 
				addresses = geocoder.getFromLocation(location.getLatitude(),
						location.getLongitude(), 1
						);

				// Catch network or other I/O problems.
			} catch (IOException exception1) {

				// Log an error and return an error message
				Log.e(LocationUtils.APPTAG, getString(R.string.IO_Exception_getFromLocation));

				// print the stack trace
				exception1.printStackTrace();

				// Return an error message
				return (getString(R.string.IO_Exception_getFromLocation));

				// Catch incorrect latitude or longitude values
			} catch (IllegalArgumentException exception2) {

				// Construct a message containing the invalid arguments
				String errorString = getString(
						R.string.illegal_argument_exception,
						location.getLatitude(),
						location.getLongitude()
						);
				// Log the error and print the stack trace
				Log.e(LocationUtils.APPTAG, errorString);
				exception2.printStackTrace();

				//
				return errorString;
			}
			// If the reverse geocode returned an address
			if (addresses != null && addresses.size() > 0) {

				// Get the first address
				Address address = addresses.get(0);

				// Format the first line of address
				String addressText = getString(R.string.address_output_string,

						// If there's a street address, add it
						address.getMaxAddressLineIndex() > 0 ?
								address.getAddressLine(0) : "",

								// Locality is usually a city
								address.getLocality(),

								// The country of the address
								address.getCountryName()
						);

				// Return the text
				return addressText;

				// If there aren't any addresses, post a message
			} else {
				return getString(R.string.no_address_found);
			}
		}

		*//**
		 * A method that's called once doInBackground() completes. Set the text of the
		 * UI element that displays the address. This method runs on the UI thread.
		 *//*
		@Override
		protected void onPostExecute(String address) {

			// Turn off the progress bar
			mActivityIndicator.setVisibility(View.GONE);

			// Set the address in the UI
			mAddress.setText(address);
		}
	}*/

	/**
	 * Show a dialog returned by Google Play services for the
	 * connection error code
	 *
	 * @param errorCode An error code returned from onConnectionFailed
	 */
	private void showErrorDialog(int errorCode) {

		// Get the error dialog from Google Play services
		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
				errorCode,
				this,
				LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

		// If Google Play services can provide an error dialog
		if (errorDialog != null) {

			// Create a new DialogFragment in which to show the error dialog
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();

			// Set the dialog in the DialogFragment
			errorFragment.setDialog(errorDialog);

			// Show the error dialog in the DialogFragment
			errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
		}
	}

	/**
	 * Define a DialogFragment to display the error dialog generated in
	 * showErrorDialog.
	 */
	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		/**
		 * Default constructor. Sets the dialog field to null
		 */
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		/**
		 * Set the dialog to display
		 *
		 * @param dialog An error dialog
		 */
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		/*
		 * This method must return a Dialog to the DialogFragment.
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
	
	
	// ######## Shake API
	public void onAccelerationChanged(float x, float y, float z) {
		// TODO Auto-generated method stub

	}
	@SuppressWarnings("unchecked")
	public void onShake(float force) {

		// Called when Motion Detected
		//		Toast.makeText(getBaseContext(), "Motion detected", 
		//				Toast.LENGTH_SHORT).show();
		if (!passcode.getText().toString().equals("")) {
			if(passcode.getText().toString().contains("$"))
			{
				Toast.makeText(getApplicationContext(),
						"Passcode cannot contain dollar sign characters $",
						Toast.LENGTH_SHORT).show();
			}else{
				//					"Previewing Images... Please wait", Toast.LENGTH_LONG)
				//					.show();
				new GetImageUrls().execute();
				// STEP 1 : getting URLS and info
			}
		} else {
			Toast.makeText(getBaseContext(), "Please Enter a Passcode",
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("Sensor", "Service  distroy");

		//Check device supported Accelerometer senssor or not
		if (AccelerometerManager.isListening()) {

			//Start Accelerometer Listening
			AccelerometerManager.stopListening();

			//			Toast.makeText(getBaseContext(), "onDestroy Accelerometer Stoped", 
			//					Toast.LENGTH_LONG).show();
		}

	}

	private class GetImageUrls extends AsyncTask<Void, Void, String> {

		private boolean error;
		private final String _SUCCESS_ = "600";
		private final String _FAILED_COORDINATES_ = "601";
		private final String _FAILED_GET_URL_ = "602";
		private final String _FAILED_CONNECTION_ = "603";
		String _return_val;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLatitude = getLat();
			mLongitude = getLng();
			thumbs.clear();
			pics.clear();
			_return_val="";
			error = false;
			mProgressDialog = new ProgressDialog(ReceiverActivity.this);
			mProgressDialog.setTitle("Searching For Pictures");
			mProgressDialog.setMessage("Please Wait...");
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.show();
		}

		@Override
		protected String doInBackground(Void... params) {	
			
			if (!isGPSEnabled() || !isNetworkAvailable()) {
				_return_val =  _FAILED_CONNECTION_;
				return null;
			}else if(mLatitude.equals("") || mLongitude.equals("") ) {
				_return_val =  _FAILED_COORDINATES_;
				return null;
			}else{

				String myURL = "https://hezzapp.appspot.com/getpic";

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
				nameValuePairs.add(new BasicNameValuePair("passcode", passcode.getText().toString()));
				nameValuePairs.add(new BasicNameValuePair("latitude", mLatitude));
				nameValuePairs.add(new BasicNameValuePair("longitude", mLongitude));
				if(username != null && !username.equals(""))
				{
					nameValuePairs.add(new BasicNameValuePair("username", username));
				}

				String paramsString = URLEncodedUtils.format(nameValuePairs, "UTF-8");

				String downloadUrl="";
				String strToParse="";
				try {
					HttpClient client = new DefaultHttpClient();
					HttpGet request = new HttpGet(myURL + "?" + paramsString);
					HttpResponse response = client.execute(request);
					BufferedReader reader1 = new BufferedReader(new InputStreamReader(
							response.getEntity().getContent(), "UTF-8"));
					String sResponse1;
					StringBuilder s1 = new StringBuilder();

					while ((sResponse1 = reader1.readLine()) != null) {
						s1 = s1.append(sResponse1);
					}
					if(s1.toString().equals(NO_IMAGE_FOUND)) //No Image Found Error Code
					{
						return null;
					}
					//JSON Parsing
					strToParse = s1.toString();

					//downloadUrl = s1.toString();
					System.out.println(downloadUrl);
				} catch (Exception e) {
					System.out.println(e.toString());
					error = true;
					Log.v("dd", "error");
				}

				if(strToParse.equals(""))
				{
					return null;
				}
				getItemsMapFromJsonReply(strToParse);
				//			List<String> items = getItemsFromJsonReply(strToParse);
				//			if(items.size()==0)
				//			{
				//				return null;
				//			}
				return _SUCCESS_;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(String result) {
			// Set the bitmap into ImageView
			//			image.setImageBitmap(result);
			mProgressDialog.dismiss();
			if(result == null)
			{

				if(_return_val.equals(_FAILED_COORDINATES_)) //can't get coordinates
				{
					passcode.setText("");
					AlertDialog.Builder builder = new AlertDialog.Builder(ReceiverActivity.this);
					String message = "Please make sure that your GPS is enabled and your Google Location Settings is enabled as well and try again.";
					builder.setTitle("Can't Access Location")
					.setMessage(message)
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							finish();
						}
					});
					AlertDialog alert = builder.create();
					alert.show();

				}else if(_return_val.equals(_FAILED_CONNECTION_)){
					passcode.setText("");
					AlertDialog.Builder builder = new AlertDialog.Builder(ReceiverActivity.this);
					String message = "Please make sure that your GPS is enabled and your have internet connectivity.";
					builder.setTitle("Failed Connection")
					.setMessage(message)
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							finish();
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
				}else if(_return_val.equals(_FAILED_GET_URL_)){
					passcode.setText("");
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(ReceiverActivity.this, "Failed to connect to the server. Please try again later.", 
									Toast.LENGTH_SHORT).show();
						}
					}); 
					finish();
				}else{
					passcode.setText("");
					runOnUiThread(new Runnable() {
						public void run() {
							if(error)
							{
								Toast.makeText(ReceiverActivity.this, "Error! While Getting pics", 
										Toast.LENGTH_SHORT).show();
							}else{
								Toast.makeText(ReceiverActivity.this, "No Images Found", 
										Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
			}else{
				
				Intent i = new Intent (ReceiverActivity.this,ImageGridActivity.class);
				i.putExtra("thumbs", thumbs);
				i.putExtra("pics", pics);
				startActivity(i);
				finish();
				// List<String> z;
				//					listVar.clear();
				//					imagesTemp.clear();
//				imagesTemp = (ArrayList<HashMap<String, Object>>) result.get("list");
//				new DownloadThumbnails()
//				.execute(imagesTemp);
			}
		}
	}
	
	class LoadPublicTags extends AsyncTask<String, String, String> {
		ProgressDialog progDailog;
		boolean noConnection;
		String strToParse="";
		private final String _SUCESS_ = "600";
		private final String _FAILED_COORDINATES_ = "601";
		private final String _FAILED_GET_URL_ = "602";
		private final String _FAILED_CONNECTION_ = "603";
		private final String _NO_NEARBY_PUBLIC_TAGS = "7002";
		private final String _ERROR_ = "7005"; 
		@Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	        mLatitude = getLat();
			mLongitude = getLng();
	        spinner.setVisibility(View.VISIBLE);
	    }
	    @Override
	    protected String doInBackground(String... aurl) {

	    	if (!isGPSEnabled() || !isNetworkAvailable()) {
	    		return _FAILED_CONNECTION_;
	    	}else if(mLatitude.equals("") || mLongitude.equals("") ) {
	    		return _FAILED_COORDINATES_;
	    	}else{

	    		String myURL = "https://hezzapp.appspot.com/getpubpasscodes";

	    		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	    		nameValuePairs.add(new BasicNameValuePair("latitude", mLatitude));
	    		nameValuePairs.add(new BasicNameValuePair("longitude", mLongitude));

	    		String paramsString = URLEncodedUtils.format(nameValuePairs, "UTF-8");

	    		try {
	    			HttpClient client = new DefaultHttpClient();
	    			HttpGet request = new HttpGet(myURL + "?" + paramsString);
	    			HttpResponse response = client.execute(request);
	    			serverResponseCode = response.getStatusLine().getStatusCode();
	    			BufferedReader reader1 = new BufferedReader(new InputStreamReader(
	    					response.getEntity().getContent(), "UTF-8"));
	    			String sResponse1;
	    			StringBuilder s1 = new StringBuilder();

	    			if(!(serverResponseCode == 200)){
	    				return _FAILED_GET_URL_;
	    			}else{
	    				while ((sResponse1 = reader1.readLine()) != null) {
	    					s1 = s1.append(sResponse1);
	    				}
	    				if(s1.toString().equals(_NO_NEARBY_PUBLIC_TAGS)) //No Image Found Error Code
	    				{
	    					return _NO_NEARBY_PUBLIC_TAGS;
	    				}
	    				strToParse = s1.toString();
	    				if(!strToParse.equals(""))
	    				{
	    					return _SUCESS_;
	    				}else{
	    					return _ERROR_;
	    				}
	    			}
	    		} catch (Exception e) {
	    			System.out.println(e.toString());
	    			return null;
	    		}
	    	}
	    }
	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
	        spinner.setVisibility(View.GONE);
	        
			if(result.equals(_FAILED_COORDINATES_)) //can't get coordinates
			{
				passcode.setText("");
				AlertDialog.Builder builder = new AlertDialog.Builder(ReceiverActivity.this);
				String message = "Please make sure that your GPS is enabled and your Google Location Settings is enabled as well and try again.";
				builder.setTitle("Can't Access Location")
					   .setMessage(message)
				       .setCancelable(false)
				       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   finish();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
				
			}else if(result.equals(_FAILED_CONNECTION_)){
				passcode.setText("");
				AlertDialog.Builder builder = new AlertDialog.Builder(ReceiverActivity.this);
				String message = "Please make sure that your GPS is enabled and your have internet connectivity.";
				builder.setTitle("Failed Connection")
					   .setMessage(message)
				       .setCancelable(false)
				       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   finish();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
			}else if(result.equals(_FAILED_GET_URL_)){
				passcode.setText("");
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(ReceiverActivity.this, "Failed to connect to the server. Please try again later.", 
								Toast.LENGTH_SHORT).show();
					}
				}); 
				finish();
			}else if(result.equals(_SUCESS_)){
				addItemsFromJsonToList(strToParse);
			}
			
	        
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


	public void addItemsFromJsonToList(String aInStr)
	{
		// images JSONArray
		JSONArray obj = null;
		JSONObject jsonObj;

		if (aInStr != null) {
			try {
				jsonObj = new JSONObject(aInStr);
				publicTags.clear();
				lv.clearChoices();
				if(jsonObj.has("list") && jsonObj.has("counter"))
				{
					obj = jsonObj.getJSONArray("list");
					// looping through All Contacts
					for (int i = 0; i < obj.length(); i++) {
						JSONObject c = obj.getJSONObject(i);

						String passcode = c.getString("passcode");
						publicTags.add(passcode);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}
	
	
	
	
	
	

}
