package com.SmsService.AuxiliaryComponent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class CommandRestartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {			
		if(CommandJob.stillJob) {
        	Debug.PrintLog("CommandRestartReceiver", "there are still job inside job queue");        	
        	CommandJob.stillJob = false;
        	CommandJob.startService(context);
        }
		if (CommandJob.stillResponseJob) {
			Debug.PrintLog("ResponseRestartReceiver", "there are still job inside response job queue");        	
        	CommandJob.stillResponseJob = false;
        	CommandJob.startResponseService(context);
		}

	}

}
