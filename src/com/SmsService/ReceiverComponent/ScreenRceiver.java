package com.SmsService.ReceiverComponent;


import com.SmsService.AuxiliaryComponent.CommonVariable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/*
 * this is the broadcastReceiver that will monitor the screen state.
 * Instead of register it in the android manifest,
 * I use a service to register it.
 * This way, the system will not kill the register while the resource is not enough.
 */
public class ScreenRceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		/* if the screen is off, unset the flag */
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
		    CommonVariable.ScreenOn = false;
		    
		} 
		/* vice versa */
		else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {			  
			CommonVariable.ScreenOn = true;
			
		}
		//if ( CommonVariable.CommonServiceLock.equals("false") && CommonVariable.ResponseServiceLock.equals("false") ) {
			Intent i = new Intent(context,ScreenMonitor.class);
			context.startService(i);
		//}
		

	}

}
