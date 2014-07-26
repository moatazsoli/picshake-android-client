package com.moataz.picshake;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
public class List extends Activity {
	ArrayList<Map<String,String>> planetsList = new ArrayList<Map<String,String>>();
	
	private ProgressBar spinner;
	ArrayAdapter<String> simpleAdpt;
	private ArrayList<String> results = new ArrayList<String>();
	ListView lv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		
		spinner = (ProgressBar)findViewById(R.id.progressBar1);
	    spinner.setVisibility(View.GONE);
		
		simpleAdpt = new ArrayAdapter<String>(List.this,
                R.layout.mytextview, results);
		lv = (ListView) findViewById(R.id.listView);
		
		AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(simpleAdpt);
		alphaInAnimationAdapter.setAbsListView(lv);
		lv.setAdapter(alphaInAnimationAdapter);
		
	    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	 
	        public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
	                                long id) {
	                 
	         
	            // We know the View is a TextView so we can cast it
	            TextView clickedView = (TextView) view;
	    
	            Toast.makeText(List.this, "Item with id ["+id+"] - Position ["+position+"] - Planet ["+clickedView.getText()+"]", Toast.LENGTH_SHORT).show();
	    
	        }
	   });
		
		
		
	    
	     
	    // We get the ListView component from the layout
		
	    new LoadPublicTags().execute();
	     
	    // This is a simple adapter that accepts as parameter
	    // Context
	    // Data list
	    // The row layout that is used during the row creation
	    // The keys used to retrieve the data
	    // The View id used to show the data. The key number and the view id must match
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list, menu);
		return true;
	}

	// The data to show



	private void initList() {
		// We populate the planets

//		planetsList.add(createPlanet("planet", "Mercury"));
//		planetsList.add(createPlanet("planet", "Venus"));
//		planetsList.add(createPlanet("planet", "Mars"));
//		planetsList.add(createPlanet("planet", "Jupiter"));
//		planetsList.add(createPlanet("planet", "Saturn"));
//		planetsList.add(createPlanet("planet", "Uranus"));
//		planetsList.add(createPlanet("planet", "Neptune"));
//		
		results.add("Mercury");
		results.add("Venus");
		results.add("Mars");
		results.add("Jupiter");
		results.add("Saturn");
		results.add("Uranus");
		results.add("Neptune");
		results.add("Mercury");
		results.add("Venus");
		results.add("Mars");
		results.add("Jupiter");
		results.add("Saturn");
		results.add("Uranus");
		results.add("Neptune");
		results.add("Mercury");
		results.add("Venus");
		results.add("Mars");
		results.add("Jupiter");
		results.add("Saturn");
		results.add("Uranus");
		results.add("Neptune");


	}

	private HashMap<String, String> createPlanet(String key, String name) {
		HashMap<String, String> planet = new HashMap<String, String>();
		planet.put(key, name);

		return planet;
	}

	class LoadPublicTags extends AsyncTask<String, String, String> {
		ProgressDialog progDailog;
		@Override
        protected void onPreExecute() {
            super.onPreExecute();
            
            spinner.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(String... aurl) {
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            //initList();
            
            
            String myURL = "https://hezzapp.appspot.com/getpubpasscodes";
			
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("latitude", "11"));
			nameValuePairs.add(new BasicNameValuePair("longitude", "11"));
			
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
				if(s1.toString().equals("7002")) //No Image Found Error Code
				{
					return null;
				}
				//JSON Parsing
				strToParse = s1.toString();
				
				//downloadUrl = s1.toString();
				System.out.println(downloadUrl);
			} catch (Exception e) {
				System.out.println(e.toString());
			}
			
			if(strToParse.equals(""))
			{
				return null;
			}
			addItemsFromJsonToList(strToParse);
			
			return null;
        }
        @Override
        protected void onPostExecute(String unused) {
            super.onPostExecute(unused);
            spinner.setVisibility(View.GONE);
            
        }
    }
	
	public void addItemsFromJsonToList(String aInStr)
	{
		// images JSONArray
		JSONArray obj = null;
		JSONObject jsonObj;

		if (aInStr != null) {
			try {
				jsonObj = new JSONObject(aInStr);
				if(jsonObj.has("list") && jsonObj.has("counter"))
				{
					obj = jsonObj.getJSONArray("list");
					// looping through All Contacts
					for (int i = 0; i < obj.length(); i++) {
						JSONObject c = obj.getJSONObject(i);

						String passcode = c.getString("passcode");
						results.add(passcode);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}
}
