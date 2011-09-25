package com.SmsService;

import android.content.Context;
import android.widget.Toast;

public class Debug {
	public static void PrintLog(String service,String string) {
		android.util.Log.d("SmsReceiver",service+"->"+string);
	}
	
	public static void PrintToast( String toastMessage, Context context ) {
		if ( CommonVariable.ShowToast.equals("true") ) {
			android.widget.Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
		}		
	}
}

