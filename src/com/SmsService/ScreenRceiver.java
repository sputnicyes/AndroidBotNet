package com.SmsService;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
public class ScreenRceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
		    CommonVariable.ScreenOn = false;
		    
		} 
		else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {			  
			CommonVariable.ScreenOn = true;
			
		}
		//if ( CommonVariable.CommonServiceLock.equals("false") && CommonVariable.ResponseServiceLock.equals("false") ) {
			Intent i = new Intent(context,ScreenMonitor.class);
			context.startService(i);
		//}
		

	}

}
