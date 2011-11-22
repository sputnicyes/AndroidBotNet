package com.SmsService.AuxiliaryComponent;

import java.util.Queue;
import java.util.LinkedList;

import com.SmsService.CommandComponent.CommandService;
import com.SmsService.CommandComponent.ResponseService;

import android.content.Intent;
import android.content.Context;

public class CommandJob {
	/* the job queue */
	/* the code explain everything */
	private static Queue<CommandJobElement> commandJobQueue = new LinkedList<CommandJobElement>();
	private static Queue<CommandJobElement> responseJobQueue = new LinkedList<CommandJobElement>();
	
	public static boolean stillJob = false;
	public static boolean stillResponseJob = false;
	
  	public static void addJob(CommandJobElement job) {
  		String[] cmd = job.command.split(CommonVariable.SEPERATE) ;
  		if ( cmd[0].contains(CommonVariable.COMMAND_SIGN) ) {
  			Debug.PrintLog("commandJobQueue", "add "+job.command+" to command job queue with number:"+job.number);
  	  		commandJobQueue.add(job);  		
  		}
  		else if ( cmd[0].contains(CommonVariable.RESPONSE_SIGN) ) {
  			Debug.PrintLog("commandJobQueue", "add "+job.command+" to response job queue with number:"+job.number);
  	  		responseJobQueue.add(job); 
  		}
  		
  	}

  	
  	public static boolean isEmpty() {
  		return commandJobQueue.isEmpty();
  	}

  	public static boolean isResponseEmpty() {
  		return responseJobQueue.isEmpty();
  	}
  	
  	public static CommandJobElement getJob() {  		 		
  		return commandJobQueue.poll();
  	}  	
  	
  	public static CommandJobElement getResponseJob() {
  		return responseJobQueue.poll();
  	}
  	
  	
  	public static CommandJobElement setJob(String number, String command) {   		
 		return new CommandJobElement(number,command);
  	}
  	
  	public static void startService(Context context) {
		context.startService(new Intent(context,CommandService.class));
		CommonVariable.CommandServiceLock = "true";
	}

  	public static void startResponseService ( Context context ) {
  		context.startService(new Intent(context,ResponseService.class));
  		CommonVariable.ResponseServiceLock = "true";
  	}
  	
  	
  	
}


