package com.moataz.picshake;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	/** Called when the user clicks the Send button */
	public void sendActivity(View view) {
	    Intent intent = new Intent(this, SenderActivity.class);
	    startActivity(intent);
	}
	
	public void receiveActivity(View view) {
	    Intent intent = new Intent(this, ReceiverActivity.class);
	    startActivity(intent);
	}
	
	
	
//	/** Called when the user clicks the Send button */
//	public void receiveAvtivity(View view) {
//	    Intent intent = new Intent(this, DisplayMessageActivity.class);
//	    EditText editText = (EditText) findViewById(R.id.edit_message);
//	    String message = editText.getText().toString();
//	    intent.putExtra(EXTRA_MESSAGE, message);
//	    startActivity(intent);
//	}

}
