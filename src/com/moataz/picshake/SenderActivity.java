package com.moataz.picshake;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.http.HttpResponse;
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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
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
GooglePlayServicesClient.OnConnectionFailedListener {

	// A request to connect to Location Services
	private LocationRequest mLocationRequest;
	
	private GridView grid;
	private GalleryAdapter adapter;
	public final static int RESULT_LOAD_MULTI_IMAGES = 200;
	
	
	public final static int RESULT_LOAD_IMAGE = 5000;
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";
    private Uri fileUri; // file url to store image/video
	private Button uploadButton;
    private int serverResponseCode = 0;
    private ProgressDialog dialog = null;
    private Bitmap bm;
//    private String picture_path = null;
    private ArrayList<String> photoPathsList;
    private ProgressDialog mProgressDialog;
	
	// Stores the current instantiation of the location client in this object
	private LocationClient mLocationClient;

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

	/*
	 * Initialize the Activity
	 */
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sender);
		// Show the Up button in the action bar.
		setupActionBar();
		
		photoPathsList = new ArrayList<String>();
		Intent mIntent = getIntent();
		int selectOrCamera = mIntent.getIntExtra("selectOrCamera", 0);
		// Upload stuff
		uploadButton = (Button)findViewById(R.id.send_button);
		Button buttonLoadImage = (Button) findViewById(R.id.select_pic);
        passcode = (EditText)findViewById(R.id.passcode);
        
        uploadButton.setOnClickListener(new OnClickListener() {            
            @Override
            public void onClick(View v) {
//            	 try {
//                     bm = BitmapFactory.decodeFile(picture_path);
//                     
//                 } catch (Exception e) {
//                     Log.e(e.getClass().getName(), e.getMessage());
//                 }
            	
               // dialog = ProgressDialog.show(SenderActivity.this, "", "Uploading file...", true);
//                stopUpdates();
                 new UploadImage().execute(photoPathsList);
                                                     
                }
            });
        
		//***
		
		// Get handles to the UI view objects

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
		connectAndStartLocationUpdates();
        if(selectOrCamera == 1)
		{
			if (!isDeviceSupportCamera()) {
	            Toast.makeText(getApplicationContext(),
	                    "Sorry! Your device doesn't support camera",
	                    Toast.LENGTH_LONG).show();
			}else{
				buttonLoadImage.setVisibility(View.GONE);
				captureImage();
			}
		}else{
			
	        buttonLoadImage.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
//					startUpdates();
//					Intent i = new Intent(
//							Intent.ACTION_PICK,
//							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//					
//					startActivityForResult(i, RESULT_LOAD_IMAGE);
					Intent intent = new Intent();
					intent.setAction(com.luminous.pick.Action.ACTION_MULTIPLE_PICK);
					startActivityForResult(intent, RESULT_LOAD_MULTI_IMAGES);
				}
			});
		}
	}
	
	private void connectAndStartLocationUpdates()
	{
		mUpdatesRequested = true;
		mLocationClient.connect();
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

	//	getActionBar().setDisplayHomeAsUpEnabled(true);

	}
	
	private void captureImage() {
	    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
	 
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
	 
	    // start the image capture Intent
	    startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
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
	                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
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
				
                // successfully captured the image
                // display it in image view
//                previewCapturedImage();
				String picturePath = fileUri.getPath();
				ImageView mImageView = (ImageView) findViewById(R.id.imgView);
				BitmapFactory.Options options = new BitmapFactory.Options();
				 
	            // downsizing image as it throws OutOfMemory Exception for larger
	            // images
	            options.inSampleSize = 8;
	 
	            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
	                    options);
	 
	            mImageView.setImageBitmap(bitmap);
	          
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
//			    picture_path = picturePath;
			    
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
			
//		case RESULT_LOAD_IMAGE:
//			super.onActivityResult(requestCode, resultCode, intent);
//			if (resultCode == RESULT_OK && null != intent) {
//				Uri selectedImage = intent.getData();
//				String[] filePathColumn = { MediaStore.Images.Media.DATA };
//
//				Cursor cursor = getContentResolver().query(selectedImage,
//						filePathColumn, null, null, null);
//				cursor.moveToFirst();
//
//				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//				String picturePath = cursor.getString(columnIndex);
//				cursor.close();
////				System.out.println("ASDFASDF");
//				ImageView mImageView = (ImageView) findViewById(R.id.imgView);
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
//			    photoPathsList.add(picturePath);
////			    picture_path = picturePath;
//				break;
//				//imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
//			}
			
		case RESULT_LOAD_MULTI_IMAGES:
			super.onActivityResult(requestCode, resultCode, intent);
			
			if (resultCode == RESULT_OK && null != intent) {
				ImageView mImageView = (ImageView) findViewById(R.id.imgView);
				mImageView.setVisibility(View.INVISIBLE);
				String[] all_path = intent.getStringArrayExtra("all_path");
				ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();
				
				//SHould be in Create method
				adapter=new GalleryAdapter(this, ImageLoader.getInstance(), new String[]{});
				grid = (GridView) findViewById(R.id.gridView);
				grid.setAdapter(adapter);
				
				for (String string : all_path) {
					CustomGallery item = new CustomGallery();
					item.sdcardPath = string;
					dataT.add(item);
					photoPathsList.add(item.sdcardPath);
				    
				}

				adapter.addAll(dataT);
				break;	
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
	
	public int uploadFile(Bitmap bmToUpload) {

		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet();
			try {
				getRequest.setURI(new URI("http://hezzapp.appspot.com/getuploadurl"));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			HttpResponse getresponse = httpClient.execute(getRequest);
			serverResponseCode = getresponse.getStatusLine().getStatusCode();
			BufferedReader reader1 = new BufferedReader(new InputStreamReader(
					getresponse.getEntity().getContent(), "UTF-8"));
			String sResponse1;
			StringBuilder s1 = new StringBuilder();

			while ((sResponse1 = reader1.readLine()) != null) {
				s1 = s1.append(sResponse1);
			}
			if(serverResponseCode == 200){

				runOnUiThread(new Runnable() {
					public void run() {
						String msg = "Got URL";
						Toast.makeText(SenderActivity.this, "GOT URL!!", 
								Toast.LENGTH_SHORT).show();
					}
				});                
			}   

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bmToUpload.compress(CompressFormat.JPEG, 100, bos);
			byte[] data = bos.toByteArray();
			HttpPost postRequest = new HttpPost(s1.toString());
			//    "http://hezzapp.appspot.com/_ah/upload/AMmfu6ZVSlpuF4VCQyW6D-SytsfCEC79yyS66YRi5aZApJmmVtFn1sL8xHgCiv5SDeuUB4h0VHW28ehwedWGnKAf2QfbQBlt9wMqhBvt9yR4Q12ovqrwgC0/ALBNUaYAAAAAUvrywvX7Sb3dDVb_oI77Wqyt5qUUoIoY/");
			ByteArrayBody bab = new ByteArrayBody(data, "image/jpeg","testimage.jpg");
			// File file= new File("/mnt/sdcard/forest.png");
			// FileBody bin = new FileBody(file);
			
//			MultipartEntity reqEntity = new MultipartEntity(
//					HttpMultipartMode.BROWSER_COMPATIBLE);
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
			reqEntity.addPart("longitude", new StringBody(getLng()));
			reqEntity.addPart("latitude", new StringBody(getLat()));
			
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

//			if(serverResponseCode == 200){
//				passcode.setText("");
//				runOnUiThread(new Runnable() {
//					public void run() {
//
//						String msg = "File Upload Completed";
//
//						messageText.setText(msg);
//						Toast.makeText(SenderActivity.this, "File Upload Complete.", 
//								Toast.LENGTH_SHORT).show();
//					}
//				});                
//			}    

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
		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(SenderActivity.this);
			mProgressDialog.setMessage("Uploading file. Please wait...");
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setMax(100);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			//later on allow user to cancel and set the button callback to cancel and exit the download process
			//mProgressDialog.setCancelable(true);
			mProgressDialog.show();
		}

		@Override
		protected String doInBackground(ArrayList<String>... params) {
			pathsList = params[0];
//			String picPath = params[0];
			for (Iterator iterator = pathsList.iterator(); iterator.hasNext();) {
				pathTempHolder = (String) iterator.next();
				try {
					bm = BitmapFactory.decodeFile(pathTempHolder);
					uploadFile(bm);

				} catch (Exception e) {
					Log.e(e.getClass().getName(), e.getMessage());
				}
			}

			bm = null;
			return null;
		}
        
		@Override
	    protected void onPostExecute(String result) {
			mProgressDialog.dismiss();
			photoPathsList.clear();
//			picture_path="";
			if(serverResponseCode == 200){
				passcode.setText("");
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(SenderActivity.this, "File Upload Complete.", 
								Toast.LENGTH_SHORT).show();
					}
				});                
			}  
	    }
		
		
	}
}
