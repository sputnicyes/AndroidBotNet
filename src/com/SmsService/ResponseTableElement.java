package com.SmsService;

import java.util.LinkedList;

public class ResponseTableElement {
	public String number;
	public LinkedList<CommandElement> commandQueue = new LinkedList<CommandElement>();

	/*
	 * public LinkedList<String> command = new LinkedList<String>(); public
	 * LinkedList<String> time = new LinkedList<String>(); public
	 * LinkedList<String> oldTime = new LinkedList<String>(); public
	 * LinkedList<String> sign = new LinkedList<String>();
	 */
	public ResponseTableElement(String number, String command, String sign, String oldTime) {
		this.number = number;
		CommandElement element = new CommandElement(command,sign,String.valueOf(System.currentTimeMillis()),oldTime);
		commandQueue.add(element);
	}
	public ResponseTableElement( String number ){
		this.number = number;
	}
	
	public String addCommand(String command, String sign, String oldTime) {
		String time = String.valueOf(System.currentTimeMillis()) ;
		CommandElement element = new CommandElement(command,sign,time,oldTime);
		commandQueue.add(element);
		return time;
	}

	public class CommandElement {
		public String command;
		public String time;
		public String oldTime;
		public String sign;
		public CommandElement( String command, String sign , String time, String oldTime) {
			this.command = command ;
			this.sign = sign;
			this.oldTime = oldTime;
			this.time = time;
		}
	}
}
