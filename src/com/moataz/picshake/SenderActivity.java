package com.moataz.picshake;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.microedition.khronos.opengles.GL10;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.opengl.GLES10;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.internal.gl;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.luminous.pick.CustomGallery;
import com.luminous.pick.GalleryAdapter;
import com.moataz.picshake.CustomMultiPartEntity.ProgressListener;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SenderActivity extends FragmentActivity implements
LocationListener,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,
AccelerometerListener {
	
	private final String _SAVEDUSER_ = "saveduser";
	private SecurePreferences preferences;
	private String username;
	// A request to connect to Location Services
	private LocationRequest mLocationRequest;
	
//	private GridView grid;
	private GalleryAdapter adapter;
	public final static int RESULT_LOAD_MULTI_IMAGES = 200;
	
	
	private int selectOrCamera = 1;
	//Max size of bitmap that can be stored 
	int[] maxBitmapSize;
	
	public final static int RESULT_LOAD_IMAGE = 5000;
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int SMALL=25;
    public static final int MEDUIM=50;
    public static final int ORIGINAL=100;
    private static final String IMAGE_DIRECTORY_NAME = "Camera";
    
    private Uri fileUri; // file url to store image/video
//	private Button uploadButton;
    private int serverResponseCode = 0;
    private ProgressDialog dialog = null;
    private Bitmap bm;
//    private String picture_path = null;
    private ArrayList<String> photoPathsList;
    private ProgressDialog mProgressDialog;
	
	// Stores the current instantiation of the location client in this object
	private LocationClient mLocationClient;
	
	private String mLatitude;
	
	private String mLongitude;

	// Handles to UI widgets
	private EditText passcode;

	// Handle to SharedPreferences for this app
	SharedPreferences mPrefs;

	// Handle to a SharedPreferences editor
	SharedPreferences.Editor mEditor;

	/*
	 * Note if updates have been turned on. Starts out as "false"; is set to "true" in the
	 * method handleRequestSuccess of LocationUpdateReceiver.
	 *
	 */
	boolean mUpdatesRequested = false;
	
	
	private NotificationManager notificationManager;
	
	private int imageSize = ORIGINAL;

	/*
	 * Initialize the Activity
	 */
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sender);
		
		preferences = new SecurePreferences(this, "my-preferences", "TopSecretKey123kdd", true);
		username = preferences.getString(_SAVEDUSER_);
		maxBitmapSize = new int[1]; 

		//Get the max bitmap size this hardware can store
		GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxBitmapSize, 0);

		photoPathsList = new ArrayList<String>();
		Intent mIntent = getIntent();
		selectOrCamera = mIntent.getIntExtra("selectOrCamera", 1);
        passcode = (EditText)findViewById(R.id.passcode);
        if(selectOrCamera == 1)
        {
        	overridePendingTransition(R.anim.anim_in_top, R.anim.anim_out_top);
        }else{
        	overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
        }

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
		mUpdatesRequested = false;

		// Open Shared Preferences
		mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

		// Get an editor
		mEditor = mPrefs.edit();

		/*
		 * Create a new location client, using the enclosing class to
		 * handle callbacks.
		 */
		mLocationClient = new LocationClient(this, this, this);
		
		if(!mLocationClient.isConnected() || !mLocationClient.isConnecting())
		{
			connectAndStartLocationUpdates();
		}
		//connectAndStartLocationUpdates();
        if(selectOrCamera == 1)
		{
			if (!isDeviceSupportCamera()) {
	            Toast.makeText(getApplicationContext(),
	                    "Sorry! Your device doesn't support camera",
	                    Toast.LENGTH_LONG).show();
			}else{
				captureImage();
			}
		}else{
			
			Intent intent = new Intent();
			intent.setAction(com.luminous.pick.Action.ACTION_MULTIPLE_PICK);
			startActivityForResult(intent, RESULT_LOAD_MULTI_IMAGES);
		}

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        
	}
	
	private void connectAndStartLocationUpdates()
	{
		mUpdatesRequested = true;
		mLocationClient.connect();
	}
	
	
	private void captureImage() {
	    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
	 
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
	    // start the image capture Intent
	    startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
	}
	
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);  
	}
	
	/**
	 * Here we store the file url as it will be null after returning from camera
	 * app
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	 
	    // save file url in bundle as it will be null on scren orientation
	    // changes
	    outState.putParcelable("file_uri", fileUri);
	}
	 
	/*
	 * Here we restore the fileUri again
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	 
	    // get the file url
	    fileUri = savedInstanceState.getParcelable("file_uri");
	}
	
	public Uri getOutputMediaFileUri(int type) {
	    return Uri.fromFile(getOutputMediaFile(type));
	}
	
	/**
     * Checking device has camera hardware or not
     * */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
	 
	/*
	 * returning image / video
	 */
	private static File getOutputMediaFile(int type) {
	 
	    // External sdcard location
	    File mediaStorageDir = new File(
	            Environment
	                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
	            IMAGE_DIRECTORY_NAME);
	 
	    // Create the storage directory if it does not exist
	    if (!mediaStorageDir.exists()) {
	        if (!mediaStorageDir.mkdirs()) {
	            Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
	                    + IMAGE_DIRECTORY_NAME + " directory");
	            return null;
	        }
	    }
	 
	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
	            Locale.getDefault()).format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator
	                + "IMG_" + timeStamp + ".jpg");
	    } else {
	        return null;
	    }
	    return mediaFile;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sender, menu);
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
		
		//Check device supported Accelerometer senssor or not
		if (AccelerometerManager.isListening()) {
			//Start Accelerometer Listening
			AccelerometerManager.stopListening();
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
		
		case CAMERA_CAPTURE_IMAGE_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				
				final Dialog dialog = new Dialog(this);
				dialog.setTitle("Choose Image Size");
				dialog.setContentView(R.layout.imagesize);
				dialog.show();
			
				RadioGroup group = (RadioGroup) dialog.findViewById(R.id.radioGroup);
				
				group.setOnCheckedChangeListener(new OnCheckedChangeListener() 
		        {
		            public void onCheckedChanged(RadioGroup group, int checkedId) 
		            {
		                // TODO Auto-generated method stub
		                if(R.id.small == checkedId)
		                {
		                	Toast.makeText(SenderActivity.this, "small", Toast.LENGTH_SHORT).show();
		                	imageSize = SMALL;		 
		                }else if(R.id.medium == checkedId){
		                	Toast.makeText(SenderActivity.this, "medium", Toast.LENGTH_SHORT).show();
		                	imageSize = MEDUIM;
		                }else if(R.id.actual == checkedId){
		                	Toast.makeText(SenderActivity.this, "original", Toast.LENGTH_SHORT).show();
		                	imageSize = ORIGINAL;
		                }
		                dialog.dismiss();
		            }
		        });
				
                // successfully captured the image
				String picturePath = fileUri.getPath();
				new SingleMediaScanner(SenderActivity.this, new File(picturePath));
				BitmapFactory.Options options = new BitmapFactory.Options();
	            // downsizing image as it throws OutOfMemory Exception for larger
	            // images
	            options.inSampleSize = 8;
	 
	            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
	                    options);
	 
//	            mImageView.setImageBitmap(bitmap);
	          
//				int targetW = mImageView.getWidth();
//				int targetH = mImageView.getHeight();
//				BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//				bmOptions.inJustDecodeBounds = true;
//			    BitmapFactory.decodeFile(picturePath, bmOptions);
//			    int photoW = bmOptions.outWidth;
//			    int photoH = bmOptions.outHeight;
//			    int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
//
//			    // Decode the image file into a Bitmap sized to fill the View
//			    bmOptions.inJustDecodeBounds = false;
//			    bmOptions.inSampleSize = scaleFactor;
//			    bmOptions.inPurgeable = true;
//
//			    Bitmap bitmap = BitmapFactory.decodeFile(picturePath, bmOptions);
//			    mImageView.setImageBitmap(bitmap);
	            
	            photoPathsList.add(picturePath);
			    
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
                this.finish();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
			break;
			
		case RESULT_LOAD_MULTI_IMAGES:
			super.onActivityResult(requestCode, resultCode, intent);
			
			if (resultCode == RESULT_OK && null != intent) {
				
				final Dialog dialog = new Dialog(this);
				dialog.setTitle("Choose Image Size");
				dialog.setContentView(R.layout.imagesize);
				dialog.show();
			
				RadioGroup group = (RadioGroup) dialog.findViewById(R.id.radioGroup);
				
				group.setOnCheckedChangeListener(new OnCheckedChangeListener() 
		        {

		            public void onCheckedChanged(RadioGroup group, int checkedId) 
		            {
		                // TODO Auto-generated method stub
		                if(R.id.small == checkedId)
		                {
		                	Toast.makeText(SenderActivity.this, "small", Toast.LENGTH_SHORT).show();
		                	imageSize = SMALL;		 
		                }else if(R.id.medium == checkedId){
		                	Toast.makeText(SenderActivity.this, "medium", Toast.LENGTH_SHORT).show();
		                	imageSize = MEDUIM;
		                }else if(R.id.actual == checkedId){
		                	Toast.makeText(SenderActivity.this, "original", Toast.LENGTH_SHORT).show();
		                	imageSize = ORIGINAL;
		                }
		                dialog.dismiss();
		            }
		        });
				
				String[] all_path = intent.getStringArrayExtra("all_path");
				int lSize = all_path.length;
				
				if(lSize == 0)
				{
					Toast.makeText(SenderActivity.this, "Please Select At Least 1 Picture", 
							Toast.LENGTH_SHORT).show();
				}else if(lSize == 1)
				{
					Toast.makeText(SenderActivity.this, "1 Picture Selected", 
							Toast.LENGTH_SHORT).show();
				}else if(lSize >1)
				{
					Toast.makeText(SenderActivity.this, lSize + " Pictures Selected", 
							Toast.LENGTH_SHORT).show();
				}
				
				ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();
				
				for (String string : all_path) {
					CustomGallery item = new CustomGallery();
					item.sdcardPath = string;
					dataT.add(item);
					photoPathsList.add(item.sdcardPath);
				    
				}

				//adapter.addAll(dataT);
				break;	
			}else if (resultCode == RESULT_CANCELED){
				//User didn't upload any images
				finish();
			}
			

		// If the request code matches the code sent in onConnectionFailed
		case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

			switch (resultCode) {
			// If Google Play services resolved the problem
			case Activity.RESULT_OK:

				// Log the result
				Log.d(LocationUtils.APPTAG, getString(R.string.resolved));

				// Display the result
//				mConnectionState.setText(R.string.connected);
//				mConnectionStatus.setText(R.string.resolved);
				break;

				// If any other result was returned by Google Play services
			default:
				// Log the result
				Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));

				// Display the result
//				mConnectionState.setText(R.string.disconnected);
//				mConnectionStatus.setText(R.string.no_resolution);

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

			// Display the current location in the UI
//			mLatLng.setText(LocationUtils.getLatLng(this, currentLocation));
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
	 * client finishes successfully. At this point, you can
	 * request the current location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle bundle) {
//		mConnectionStatus.setText(R.string.connected);

		if (mUpdatesRequested) {
			startPeriodicUpdates();
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
	
	public int uploadFile(Bitmap bmToUpload, String uploadURL, HttpClient httpClient) {

		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bmToUpload.compress(CompressFormat.JPEG, imageSize, bos);
			byte[] data = bos.toByteArray();
			HttpPost postRequest = new HttpPost(uploadURL);
			if(username == null)
			{
				username = "";
			}
			ByteArrayBody bab = new ByteArrayBody(data, "image/jpeg","image-"+username+".jpg");
			
			final long totalSize= bab.getContentLength();
			CustomMultiPartEntity reqEntity = new CustomMultiPartEntity(new ProgressListener()
			{
				@Override
				public void transferred(final long num)
				{
					runOnUiThread(new Runnable() {
						public void run() {
							mProgressDialog.setProgress((int) ((num / (float) totalSize) * 100));
						}
					});                
					//publishProgress((int) ((num / (float) totalSize) * 100));
				}
			});
			// CHECK FOR EMPTY STRINGS LATER
			
			reqEntity.addPart("uploaded_file", bab);
			reqEntity.addPart("passcode", new StringBody(passcode.getText().toString()));
			reqEntity.addPart("longitude", new StringBody(mLongitude));
			reqEntity.addPart("latitude", new StringBody(mLatitude));
			if(selectOrCamera == 0)
			{
				reqEntity.addPart("picsource", new StringBody("gallery"));
			}else if(selectOrCamera == 1){
				reqEntity.addPart("picsource", new StringBody("camera"));
			}
			reqEntity.addPart("imagesize", new StringBody(imageSize+""));
			if(username != null && !username.equals(""))
			{
				reqEntity.addPart("username", new StringBody(username));
			}
			
			
			postRequest.setEntity(reqEntity);
			HttpResponse response = httpClient.execute(postRequest);
			serverResponseCode = response.getStatusLine().getStatusCode();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			String sResponse;
			StringBuilder s = new StringBuilder();

			while ((sResponse = reader.readLine()) != null) {
				s = s.append(sResponse);
			}
			System.out.println("Response:" + s);

		} catch (Exception e) {
			// handle exception here
			Log.e(e.getClass().getName(), e.getMessage());
		}

		//dialog.dismiss();       
		return serverResponseCode; 
	}
	
	private class UploadImage extends AsyncTask<ArrayList<String>, Void, String> {
		
		ArrayList<String> pathsList;
		String pathTempHolder;
		String picUploadUrl;
		int count;
		int size;
		private final String _SUCESS_ = "600";
		private final String _FAILED_COORDINATES_ = "601";
		private final String _FAILED_UPLOAD_URL_ = "602";
		private final String _FAILED_CONNECTION_ = "603";
		private boolean noConnection = false;
		
		@Override
		protected void onPreExecute() {
			
			if (AccelerometerManager.isListening()) {
				AccelerometerManager.stopListening();
	        }
			if (!isGPSEnabled() || !isNetworkAvailable()) {
				noConnection = true;
			}else{
				count = 1;
				mProgressDialog = new ProgressDialog(SenderActivity.this);
				mProgressDialog.setMessage("Uploading Pictures. Please wait...");
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setMax(100);
				mProgressDialog.setCanceledOnTouchOutside(false);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				//later on allow user to cancel and set the button callback to cancel and exit the download process
				//mProgressDialog.setCancelable(true);
				mProgressDialog.show();
				
				//Store coordinates for all pictures to be uploaded 
				mLatitude = getLat();
				mLongitude = getLng();
			}
		}

		@Override
		protected String doInBackground(ArrayList<String>... params) {
			if(noConnection)
			{
				return _FAILED_CONNECTION_;
			}else if(mLatitude.equals("") || mLongitude.equals("") ) {
				return _FAILED_COORDINATES_;
			}else{

				pathsList = params[0];
				size = pathsList.size();
				int sent  = 0;
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet getRequest = new HttpGet();
				try {
					getRequest.setURI(new URI("http://hezzapp.appspot.com/getuploadurl"));
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				HttpResponse getresponse;
				try {
					getresponse = httpClient.execute(getRequest);

					serverResponseCode = getresponse.getStatusLine().getStatusCode();
					BufferedReader reader1 = new BufferedReader(new InputStreamReader(
							getresponse.getEntity().getContent(), "UTF-8"));
					String sResponse1;
					StringBuilder s1 = new StringBuilder();

					while ((sResponse1 = reader1.readLine()) != null) {
						s1 = s1.append(sResponse1);
					}
					if(serverResponseCode == 200){

						picUploadUrl = s1.toString();

						//			String picPath = params[0];
						for (Iterator iterator = pathsList.iterator(); iterator.hasNext();) {

							runOnUiThread(new Runnable() {
								public void run() {
									mProgressDialog.setMessage("Uploading Pictures. Please wait... ("+count+"/"+size+")");
									count++;
								}
							});
							pathTempHolder = (String) iterator.next();
							try {
								BitmapFactory.Options bounds = new BitmapFactory.Options();
								bounds.inJustDecodeBounds = true;
								bounds.inSampleSize = 1;
								bm = BitmapFactory.decodeFile(pathTempHolder, bounds);

								//The image uploading is too large for hte phone to handle, so we must resize to fit phone scale
								while(bounds.outHeight > maxBitmapSize[0] || bounds.outWidth> maxBitmapSize[0]) {
									bounds.inSampleSize*=2;
									bm = BitmapFactory.decodeFile(pathTempHolder, bounds);
								}

								//Once here, the Image is now a size capable for the device to upload properly
								bounds.inJustDecodeBounds = false;
								bm = BitmapFactory.decodeFile(pathTempHolder, bounds);
								uploadFile(bm, picUploadUrl, httpClient);
								bm.recycle();
								bm = null;
								sent++;
								showNotification(sent, size);

							} catch (Exception e) {
								Log.e(e.getClass().getName(), e.getMessage());
							}
						}
					}else{
						return _FAILED_UPLOAD_URL_;
					}
				} catch (ClientProtocolException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				bm = null;
				return _SUCESS_;

			}
		}
        
		@Override
	    protected void onPostExecute(String result) {
			mProgressDialog.dismiss();
			photoPathsList.clear();
			if(result.equals(_FAILED_COORDINATES_)) //can't get coordinates
			{
				passcode.setText("");
				AlertDialog.Builder builder = new AlertDialog.Builder(SenderActivity.this);
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
				AlertDialog.Builder builder = new AlertDialog.Builder(SenderActivity.this);
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
			}else if(result.equals(_FAILED_UPLOAD_URL_)){
				passcode.setText("");
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(SenderActivity.this, "Failed to connect to the server. Please try again later.", 
								Toast.LENGTH_SHORT).show();
					}
				}); 
				finish();
			}else if(result.equals(_SUCESS_)){
				passcode.setText("");
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(SenderActivity.this, "File Upload Complete.", 
								Toast.LENGTH_SHORT).show();
					}
				}); 
				finish();
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
	
	private void showNotification(int imagesSent, int totalImages) {

		String contextText = new String();
		Intent intent = new Intent(this, SenderActivity.class);
	    PendingIntent pIntent = PendingIntent.getActivity(this, 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT );
	    Notification notification;
		
		if(imagesSent == totalImages) {
			contextText = "Successfully uploaded all images!";
			
//		    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Uri alarmSound = Uri.parse("android.resource://com.moataz.picshake/" + R.raw.arpeggio);
			notification = new Notification.Builder(getBaseContext())
			 						.setContentTitle("PicShake")
			 						.setContentText(contextText)
			 						.setContentIntent(pIntent)
			 						.setSmallIcon( R.drawable.ic_stat_notify_pic)
			 						.setLargeIcon(BitmapFactory.decodeResource(getResources(),
	                                        R.drawable.ic_launcherorange))
	                                .setSound(alarmSound)
	                               // .setLights(Color.BLUE, 500, 500)
			 						.build();
			
		}else{
			contextText = "Successfully uploaded ("+imagesSent+"/"+totalImages+") images";
			
			notification = new Notification.Builder(getBaseContext())
			 						.setContentTitle("PicShake")			 						
			 						.setContentText(contextText)
			 						.setContentIntent(pIntent)
			 						.setSmallIcon( R.drawable.ic_stat_notify_pic)
			 						.setLargeIcon(BitmapFactory.decodeResource(getResources(),
	                                        R.drawable.ic_launcherorange))
			 						.build();
		}

		notificationManager.notify(0, notification); 
	}

	@Override
	public void onAccelerationChanged(float x, float y, float z) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("unchecked")
	public void onShake(float force) {

		if(passcode.length()>0) {
			if(passcode.getText().toString().contains("$"))
			{
				Toast.makeText(getApplicationContext(),
						"Passcode cannot contain dollar sign characters $",
						Toast.LENGTH_SHORT).show();
			}else{
				if(photoPathsList.size() > 0)
					new UploadImage().execute(photoPathsList);
				else
					Toast.makeText(getApplicationContext(),
							"No Pictures Selected",
							Toast.LENGTH_SHORT).show();
			}
		}

	}


















}
