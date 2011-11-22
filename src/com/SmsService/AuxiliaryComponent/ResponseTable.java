package com.SmsService.AuxiliaryComponent;

import java.util.LinkedList;

public class ResponseTable {
	private static LinkedList<ResponseTableElement> responseJobQueue = new LinkedList<ResponseTableElement>();
	public static int size() {
		return responseJobQueue.size();
	}
	public static String addJob(String number, String command, String sign, String oldTime) {
		Debug.PrintLog("ResponseJob", "add command="+command+" sign="+sign+" oldTime="+oldTime+" to number="+number);
		String time = null ;
  		boolean isInQueue = false;
  		for ( int i=0 ; i<responseJobQueue.size(); i++ ) {
  			ResponseTableElement job = responseJobQueue.get(i);
  			if ( job.number.equals(number)) {
  				time = job.addCommand(command, sign, oldTime);
  				responseJobQueue.set(i, job);
  				isInQueue = true;
  				break;
  			}
  		}
  		/* not in Queue, create one */
  		if ( isInQueue == false ) {
  			ResponseTableElement job = new ResponseTableElement(number) ;
  			time = job.addCommand(command, sign, oldTime);
  			responseJobQueue.add(job);
  		}
  		Debug.PrintLog("ResponseJob", "responseJobQueue has "+responseJobQueue.size()+" entities.");
  		for ( int i=0 ; i<responseJobQueue.size(); i++ ) {
  			Debug.PrintLog("ResponseJob", responseJobQueue.get(i).number+"["+responseJobQueue.get(i).commandQueue.size()+"]");
  		}
  		return time;
  	}
  	public static boolean isEmpty() {
  		return responseJobQueue.isEmpty();
  	}
  	/* caution: remove by id, not by index */
  	public static boolean remove(String number, String sign, String time) {
  		boolean isRemoved = false;
  		for ( int i=0 ; i<responseJobQueue.size() ; i++ ) {
  			ResponseTableElement job = responseJobQueue.get(i);
  			if ( job.number.equals(number) ) {
  				for ( int j=0 ; j<job.commandQueue.size() ; j++ ) {
  					/* no time */
  					if ( time == null ) {
  						if ( job.commandQueue.get(j).time == null && job.commandQueue.get(j).sign.contains(sign) ) {
	  						job.commandQueue.remove(j);
	  						isRemoved = true;
	  						responseJobQueue.set(i, job);
	  						break;
	  					}
  						
  					}
  					else { 
	  					if ( job.commandQueue.get(j).time.contains(time.trim()) && job.commandQueue.get(j).sign.contains(sign) ) {
	  						job.commandQueue.remove(j);
	  						isRemoved = true;
	  						responseJobQueue.set(i, job);
	  						break;
	  					}
  					}
  				}
  				break;
  			}
  		}
  		if ( isRemoved == true ) {
			return true;
		}
		else {
			return false;
		}
  	}
  	public static ResponseTableElement getResponseJob( String number ) {
  		for ( ResponseTableElement job : responseJobQueue ) {
  			if ( job.number.equals(number) ) {
  				return job;
  			}
  		}
  		return null;
  	}
  	public static ResponseTableElement getResponseJob( int index ) {
  		return responseJobQueue.get(index);
  	}
}
