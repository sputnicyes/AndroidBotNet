package com.SmsService;

public class CommandJobElement 
{
	public String number, command; 
	public CommandJobElement(String number, String command) {
		this.number = number ;
		this.command = command ;
	}
	public CommandJobElement() {
		number = "";
		command = "";
	}
}
