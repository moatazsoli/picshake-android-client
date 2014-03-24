package com.moataz.picshake;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;


public final class Utils {

	/**
	 * Function to show alert dialog
	 * */
	public static void showAlert(String aInTitle, String aInMessage, Context aInContext){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(aInContext);
   	 
        // Setting Dialog Title
        alertDialog.setTitle(aInTitle);
 
        // Setting Dialog Message
        alertDialog.setMessage(aInMessage);
 
        // on pressing cancel button
        alertDialog.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();
	}
}


