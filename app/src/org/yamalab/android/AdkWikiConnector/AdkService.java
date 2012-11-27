package org.yamalab.android.AdkWikiConnector;
 
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.connector.StringMsg;
import org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.connector.PukiWikiConnectorService;
import org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.connector.PukiwikiJavaApplication;
import org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.connector.SaveButtonDebugFrame;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class AdkService extends Service implements PukiwikiJavaApplication {

	AdkThread demoKitActivity;
	private static final String TAG = "AdkService";

    public static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";

	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;

	UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	public FileInputStream mInputStream;
	public FileOutputStream mOutputStream;
	Intent callerIntent;
	AdkThread thread;
	boolean mBound;
	PukiWikiConnectorService connectorService;
	public long readCommandInterval=0; // default 10 min.
	public long sendResultInterval=0; // default 10 min.
	String pageName="";
	
	@Override
	public void onStart(Intent intent, int startId) {
		callerIntent=intent;
		Log.d(TAG,"onStart start");
	}

    /** For showing and hiding our notification. */ 
  	NotificationManager mNM;
  	/** Keeps track of all current registered clients. */
  	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
  	/** Holds last value set by a client. */
  	int mValue = 0;
  	/*
  	 * Command to the service to register a client, receiving callbacks
  	 * from the service.  The Message's replyTo field must be a Messenger of
  	 * the client where callbacks should be sent.      
  	 */
  	static final int MSG_REGISTER_CLIENT = 1;
  	/*
  	 * Command to the service to unregister a client, ot stop receiving callbacks
  	 * from the service.  The Message's replyTo field must be a Messenger of
  	 * the client as previously given with MSG_REGISTER_CLIENT.
     */
  	static final int MSG_UNREGISTER_CLIENT = 2;
  	/*
  	 * Command to service to set a new value.  This can be sent to the
  	 * service to supply a new value, and will be sent by the service to
  	 * any registered clients with the new value.
  	 */
  	static final int MSG_SET_VALUE = 3;
  	/* stop */
  	static final int MSG_STOP = 4;
  	/* file descriptor */
  	static final int MSG_SET_USBFILEDESCRIPTOR = 5;
  	/* commands */
  	static final int MSG_EXEC_COMMAND = 6;
  	
  	/**     * Handler of incoming messages from clients.     */
  	class IncomingHandler extends Handler {
  		@Override
  		public void handleMessage(Message msg) {
  			switch (msg.what) {
  			case MSG_REGISTER_CLIENT:
  				Log.d(TAG, "handleMessage-MSG_REGISTER_CLIENT");
  				mClients.add(msg.replyTo);
  				break;
  			case MSG_UNREGISTER_CLIENT:
  				Log.d(TAG, "handleMessage-MSG_UNREGISTER_CLIENT");
  				mClients.remove(msg.replyTo);
  				break;
  			case MSG_SET_VALUE:
  				Log.d(TAG, "handleMessage-MSG_SET_VALUE");
  				mValue = msg.arg1;
  				for (int i=mClients.size()-1; i>=0; i--) {
  					try {
  						mClients.get(i).send(Message.obtain(null,
  								MSG_SET_VALUE, mValue, 0));
  					}
  					catch (RemoteException e) {
  							// The client is dead.  Remove it from the list;
  							// we are going through the list from back to front
  							// so this is safe to do inside the loop.
  							mClients.remove(i);
  					}
  				}
  				break;
  			case MSG_SET_USBFILEDESCRIPTOR:
  				Log.d(TAG, "handleMessage-MSG_SET_USBFILEDESCRIPTOR");
  				mFileDescriptor=(ParcelFileDescriptor)(msg.obj);
  				if(mFileDescriptor!=null){
  	      		    FileDescriptor fd = mFileDescriptor.getFileDescriptor();
  	      		    mInputStream = new FileInputStream(fd);
  	      		    mOutputStream = new FileOutputStream(fd);
  	      		    startThread();
  				}
  				break;
  			case MSG_STOP:
  				Log.d(TAG, "handleMessage-MSG_REGISTER_CLIENTonCreate");
  				onDestroy();
  				break;
  			case MSG_EXEC_COMMAND:
  				Log.d(TAG, "handleMessage-MSG_EXEC_COMMAND");
  				StringMsg cmd=(StringMsg)(msg.obj);
  				parseCommand(cmd);
  			default:
  				super.handleMessage(msg);
  			}
  		}
  	}
  	public void sendMessage(Message m){
  		if(mClients==null) return;
		if(!isBound()) return;
		for (int i=mClients.size()-1; i>=0; i--) {
			try {
				mClients.get(i).send(m);
			}
			catch (RemoteException e) {
					// The client is dead.  Remove it from the list;
					// we are going through the list from back to front
					// so this is safe to do inside the loop.
					mClients.remove(i);
			}
		}  		
  	}

	private static final String mActivityName = AdkWikiActivity.class.getCanonicalName(); 
	public boolean isActivityRunning() {
		Log.d(TAG, "isActivityRunning");
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> runningApp = activityManager.getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo info : runningApp) {
			if (mActivityName.equals(info.service.getClassName())) {
				Log.d(TAG, "isActivityRunning ... true");
				return true;
			}
		}
		Log.d(TAG, "isActivityRunning ... false");
		return false;
	}
  	/*
  	 * Target we publish for clients to send messages to IncomingHandler.
  	 */
  	final Messenger mMessenger = new Messenger(new IncomingHandler());
  	@Override
  	public void onCreate() {
		Log.d(TAG, "onCreate");
  		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
  		// Display a notification about us starting.
  		showNotification();
//  		connectUsb();
        connectorService=new PukiWikiConnectorService(this);
  		this.thread=null;
  		for(int i=0;i<8;i++){
  		   processors.put("a-"+i, new ProcessAnalogData("a-"+i,this));
  		}
  	}
  	@Override
  	public void onDestroy() {
  		// Cancel the persistent notification.
		Log.d(TAG, "onDestroy");
  		this.mBound=false;
  		if(this.thread!=null){
  			this.thread.stop();
  		}
  		if(this.mInputStream!=null){
  			try{
  			mInputStream.close();
  			}
  			catch(Exception e){
  				Log.d(TAG,"onDestroy-mInputStream.close error:"+e.toString());
  			}
  		}
  		if(this.mOutputStream!=null){
  			try{
  			mOutputStream.close();
  			}
  			catch(Exception e){
  				Log.d(TAG,"onDestroy-mOutputStream.close error:"+e.toString());
  			}
  		}
  		if(this.mFileDescriptor!=null){
  			try{
  				mFileDescriptor.close();
  			}
  			catch(Exception e){
  				Log.d(TAG,"onDestroy-mFileDescriptor.close error:"+e.toString());  				
  			}
  		}
//  		this.closeAccessory();
  		mNM.cancel(0);
  		// Tell the user we stopped.
  		Toast.makeText(this, "remote_service_stopped",	Toast.LENGTH_SHORT).show();
		Toast.makeText(this, "Service has been terminated.", Toast.LENGTH_SHORT).show();
//		unregisterReceiver(mUsbReceiver);
		super.onDestroy();
  	}

  	/*
  	 * When binding to the service, we return an interface to our messenger
  	 * for sending messages to the service.    
  	 */
  	@Override
  	public IBinder onBind(Intent intent) {
  		this.mBound=true;
		callerIntent=intent;
		Log.d(TAG,"onBind");
		/*
		if(this.mFileDescriptor==null){
			this.connectUsb();
		}
		*/
//        this.startThread();
  		return mMessenger.getBinder();
  	}
  	/*
  	 * Show a notification while this service is running.
  	 */
  	private void showNotification() {
  		// In this sample, we'll use the same text for the ticker and the expanded notification
  		//CharSequence text = getText(R.string.remote_service_started);
  		CharSequence text = "remote_service_started";
  		// Set the icon, scrolling text and timestamp
//  		Notification notification = new Notification(R.drawable.stat_sample, text,
//  		System.currentTimeMillis());
  		Notification notification = new Notification(R.drawable.indicator_button1_off_noglow, text,
  		System.currentTimeMillis());
  		// The PendingIntent to launch our activity if the user selects this notification
  		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
  				new Intent(this, AdkWikiActivity.class), 0);
  		// Set the info for the views that show in the notification panel.
  		notification.setLatestEventInfo(this, "AdkService",
  				text, contentIntent);
  		// Send the notification.
  		// We use a string id because it is a unique number.  We use it later to cancel.
  		mNM.notify(0, notification);
  	}
  	@Override
	public boolean onUnbind(Intent intent) {
		super.onUnbind(intent);
		Log.d(TAG, "onUnbind");
		this.mBound=false;
		return true;
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		Log.d(TAG, "onRebind");
		/*
		if(this.mFileDescriptor==null){
			this.connectUsb();
		}
		*/
//		this.startThread();
		this.mBound=true;
	}
	
	public boolean isBound(){
		return this.mBound;
	}

	public void startThread(){
		Log.d(TAG, "startThread");
		this.sendCommandToActivity("activity request setting", "");
  		if(thread==null){
   		   Log.d(TAG,"onBind-new thread");

       	   if(mFileDescriptor !=null){
       	  
       		   Log.d(TAG,"onBind-start AdkThread");
 		        thread = new AdkThread(this); //(this)
 		        thread.start();
 		        
       	   }
       	   else{
       		   Log.d(TAG,"onBind-not start AdkThread");
       	   }
       	  
  		}
	}

	protected boolean parseCommand(StringMsg m){
		if(m==null) return false;
		String x=m.getCommand();
		Log.d(TAG, "parseCommand("+x+")");
		String cmd=skipSpace(x);
		String [] rest=new String[1];
		String [] match=new String[1];
		int [] intv = new int[1];
		if(parseKeyWord(x,"connector ",rest)){
		    String subcmd=skipSpace(rest[0]);
			if (connectorService != null) {
				return connectorService.parseCommand(subcmd, m.getValue());
			}		    	
		}
		else
		if(parseKeyWord(x,"adk ",rest)){
			String subcmd=skipSpace(rest[0]);
			if(subcmd.startsWith("#")) return true;
			if(subcmd.startsWith("command:")){
				String command=subcmd.substring("command:".length());
				command=skipSpace(command);
				boolean rtn=parseInputCommand(command);
				return rtn;
			}
		}
		return false;
	}
	String skipSpace(String x){
		while(x.startsWith(" ")) x=x.substring(1);
		return x;
	}
	boolean parseKeyWord(String x, String key, String [] rest){
		if(x==null) return false;
		if(x.startsWith(key)){
			rest[0]=x.substring(key.length());
			return true;
		}
		return false;
	}
	boolean parseChar(String x, char[] chars, char[] charrtn, String [] rest){
		if(x==null) return false;
		if(x.length()<=0) return false;
		char c=x.charAt(0);
		for(int i=0;i<chars.length;i++){
			char d=chars[i];
			if(c==d) break;
			if(i==chars.length-1){
				return false;
			}
		}
		charrtn[0]=c;
		x=x.substring(1);
		rest[0]=x;
		return true;
	}
	boolean parseInt(String x, int[] intrtn, String [] rest){
		if(x==null) return false;
		if(x.length()<=0) return false;
		char c=x.charAt(0);
		String ix="";
		while('0'<=c && c<='9'){
			ix=ix+c;
			x=x.substring(1);
			if(x.length()<=0) break;
			c=x.charAt(0);
		}
		if(ix.length()<=0) return false;
		rest[0]="";
		if(!ix.equals("")){
		   int ixx=(new Integer(ix)).intValue();
		   intrtn[0]=ixx;
		   rest[0]=x;
		   return true;
		}
		return true;
	}
	boolean parseStrConst(String x,String [] sconst, String [] rest){
		String xconst="";
		if(x.startsWith("\"")){
			x=x.substring(1);
			while(!x.startsWith("\"")){
				if(x.length()<1) return false;
				if(x.startsWith("\\")){
						xconst=xconst+x.charAt(0)+x.charAt(1);
						x=x.substring(2);
				}
				else{
					xconst=xconst+x.charAt(0);
					x=x.substring(1);
				}
			}
			x=x.substring(1);
			sconst[0]=xconst;
			rest[0]=x;
			return true;
		}
		return false;
	}
	@Override
	public String getOutput() {
		// TODO Auto-generated method stub
		return this.saveText;
	}
	public void readCommands(){
		Log.d(TAG,"readCommands()");
//		this.sendCommand("connector request read", "");
		this.connectorService.parseCommand("readPage-",pageName);
	}
	@Override
	public void setInput(String x) {
		// TODO Auto-generated method stub
		String xdb="";
		if(x.length()>100) xdb=x.substring(100);
		else xdb=x;
		Log.d(TAG,"setInput("+xdb+"...)");
		readCommandInterval=1000*60*10; // default 10 min.
		sendResultInterval=1000*60*10; // default 10 min.
		StringTokenizer st=new StringTokenizer(x,"\n");
		sendCommandToActivity("activity set input","");
		this.filter.clear();
		final Calendar calendar = Calendar.getInstance();
		final int year = calendar.get(Calendar.YEAR);
		final int month = calendar.get(Calendar.MONTH);
		final int day = calendar.get(Calendar.DAY_OF_MONTH);
		final int hour = calendar.get(Calendar.HOUR_OF_DAY);
		final int minute = calendar.get(Calendar.MINUTE);
		final int second = calendar.get(Calendar.SECOND);
		String date="Date="+    
				   year + "/" + (month + 1) + "/" + day + "/" + " " +
		           hour + ":" + minute + ":" + second ;
       
		sendCommandToActivity("activity set input",date+"\n");
		while(st.hasMoreElements()){
			String line=st.nextToken();
			if(line.startsWith("#")) continue;
			if(line.startsWith("command:")){
				sendCommandToActivity("activity append input",line+"\n");
				String command=line.substring("command:".length());
				command=skipSpace(command);
				boolean rtn=parseInputCommand(command);
			}
			if(line.startsWith("result:")) break;
		}
		if(reports.size()>1) return;
		while(st.hasMoreElements()){
		    String line=st.nextToken();
		    this.reports.add(line+"\n");
		}		
	}
	@Override
	public void setSaveButtonDebugFrame(SaveButtonDebugFrame f) {
		// TODO Auto-generated method stub
	}
	@Override
	public void sendCommandToActivity(String c, String v) {
		// TODO Auto-generated method stub
		Log.d(TAG,"sendCommandToActivity("+c+","+v+")");
		StringMsg mx=new StringMsg(c,v); 		
		Message msg = Message.obtain(null,AdkThread.MESSAGE_STRING,mx);
		sendMessage(msg);					
	}	
	public void outputDevice(byte command, byte target, int value) {
		Log.d(TAG,"outputDevice("+command+"-"+target+","+value+")");
		byte[] buffer = new byte[3];
		if (value > 255)
			value = 255;

		buffer[0] = command;
		buffer[1] = target;
		buffer[2] = (byte) value;
		if (mOutputStream != null && buffer[1] != -1) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}

	String saveText="";
	int uploadInterval=10;
    int uploadNumber;	
    Vector<String> reports=new Vector();
    int reportQueueLength=120;
	public void putSendBuffer(String x){
		Log.d(TAG,"putSendBuffer("+x+")");
		this.sendCommandToActivity("activity append output", x);
//		mPirOutputView.append(line);
		reports.add(x);
		this.sendCommandToActivity("connector setMessage", "");
//		mMessageView.setText("");
		uploadNumber++;
	}
	public void sendResults(){
		Log.d(TAG,"sendResults()");
		this.sendCommandToActivity("activity set output", "");
		String px="";
		while(reports.size()>reportQueueLength){
			reports.remove(0);
		}
		for(int i=0;i<reports.size();i++){
			px=px+reports.elementAt(i);
		}
//		this.connectorService.saveText(px);
		boolean rtn=this.connectorService.parseCommand("saveText-", px);
		this.uploadNumber=0;	
	}
	String currentHour;
	String currentDayOfWeek;
	private void sendLastValue(String dname, String v){
		final Calendar calendar = Calendar.getInstance();
		final int year = calendar.get(Calendar.YEAR);
		final int month = calendar.get(Calendar.MONTH);
		final int day = calendar.get(Calendar.DAY_OF_MONTH);
		final int hour = calendar.get(Calendar.HOUR_OF_DAY);
		final int minute = calendar.get(Calendar.MINUTE);
		final int second = calendar.get(Calendar.SECOND);
		currentHour=""+hour;
		currentDayOfWeek=""+calendar.get(Calendar.DAY_OF_WEEK);
		String linex="device="+dname+", Date="+    
				   year + "/" + (month + 1) + "/" + day + "/" + " " +
		           hour + ":" + minute + ":" + second +
		           ",  "+v+
		           ".\n";	
			this.putSendBuffer(linex);
		
	}
	boolean parseInputCommand(String line){
		String [] rest=new String[1];
		String cmd=skipSpace(line);
		Log.d(TAG,"parseInputCommand-"+cmd);
		if(parseKeyWord(cmd,"get ",rest)){
			return this.parseGetCommand(rest[0]);
		}
		else
		if(parseKeyWord(cmd,"set ",rest)){
			return this.parseSetCommand(rest[0]);
		}
		return false;
	}
	public class FilterElement{
		public String cmd;
		public String device;
		public String options;
		public ProcessAnalogData processor;
		public FilterElement(String c, String dev, String opt, ProcessAnalogData p){
			cmd=c; device=dev; options=opt;
			processor=p;
		}
		public void process(int x){
			if(processor!=null)
				processor.process(x);
		}
	}
	Hashtable<String,ProcessAnalogData> processors =new Hashtable();
	public Hashtable<String,FilterElement> filter=new Hashtable();
	boolean parseGetCommand(String x){
		String [] rest=new String[1];
		int [] intVal=new int[1];
		String cmd=skipSpace(x);
		Log.d(TAG,"parseGetCommand-"+cmd);
		if(parseKeyWord(cmd,"in-a-",rest)){
			String v2=rest[0];
			if(!this.parseInt(v2, intVal, rest)) return false;
			int dn=intVal[0];
			v2=rest[0];
			if(!parseKeyWord(v2," ",rest)) return false;
			v2=skipSpace(rest[0]);			
			if(parseKeyWord(v2,"stat",rest)){
				int st=0;
				int at=0;
				v2=skipSpace(rest[0]);
				while(!Utilities.isEOL(v2)){
					if(this.parseKeyWord(v2, "sampleTerm=", rest)){
						v2=skipSpace(rest[0]);
						if(!this.parseInt(v2, intVal, rest)) return false;
						st=intVal[0];
					}
					else
					if(this.parseKeyWord(v2, "analysisTerm=", rest)){
						v2=skipSpace(rest[0]);
						if(!this.parseInt(v2, intVal, rest)) return false;
						at=intVal[0];
					}
					v2=skipSpace(rest[0]);
				}
				try{
  				  ProcessAnalogData prs=processors.get("a-"+dn);
				  if(st==0 && at==0){
				      this.setFilter("get","a-"+dn,"stat",prs);
				  }
				  else{
					  if(at==0) at=600000;
					  if(st==0) st=60000;
					  prs.setParams(st, at);
					  this.setFilter("get", "a-"+dn, "stat", prs);
				  }
			      return true;	
				}
				catch(Exception e){
					Log.e(TAG,"parseGetCommand("+x+")-error:"+e.toString());
					return false;
				}
			}
			else
			if(parseKeyWord(v2,"last",rest)){
				this.sendLastValue("a-"+dn, "v="+this.thread.analogVals[dn]);
				return true;
			}
			this.setFilter("get","a-"+dn,"stat", processors.get("a-"+dn));
			return true;
		}
		else
		if(parseKeyWord(cmd,"in-d",rest)){
			String v2=rest[0];
			if(!parseKeyWord(v2," ",rest)) return false;
			v2=skipSpace(rest[0]);			
			if(parseKeyWord(v2,"last",rest)){
				this.sendLastValue("d", "v="+Utilities.b2h(this.thread.digitalVals));
				return true;
			}
			return false;
		}
		else
		if(parseKeyWord(cmd,"pir",rest)){
			String v2=skipSpace(rest[0]);
			if(parseKeyWord(v2,"stat",rest)){
				this.setFilter("get","a-0","stat",processors.get("a-0"));
			    return true;	
			}
			else
			if(parseKeyWord(v2,"last",rest)){
				this.sendLastValue("pir", "v="+this.thread.analogVals[0]);
				return true;
			}
			this.setFilter("get","a-0","stat", processors.get("a-0"));
			return true;
		}
		else
		if(parseKeyWord(cmd,"light",rest)){
			String v2=skipSpace(rest[0]);
			if(parseKeyWord(v2,"stat",rest)){
				this.setFilter("get","a-1","stat", processors.get("a-1"));
			    return true;	
			}
			else
			if(parseKeyWord(v2,"last",rest)){
				this.sendLastValue("light", "v="+this.thread.analogVals[1]);
				return true;
			}
			this.setFilter("get","light","stat", processors.get("light"));
			return true;
		}
		else
		if(parseKeyWord(cmd,"switch",rest)){
			this.sendLastValue("switch", " v="+this.thread.currentSwitch1);
			return true;

		}

		return false;
	}
	private void setFilter(String cmd, String dev, String opt,ProcessAnalogData p){
		FilterElement fe=new FilterElement(cmd,dev,opt,p);
		filter.put(dev, fe);
	}
    char[] devKind=new char[]{'a','d'};
	boolean parseSetCommand(String x){
		String [] rest=new String[1];
		int[] intv1=new int[1];
		int[] intv2=new int[1];
		char[] chrtn=new char[1];
		String cmd=skipSpace(x);
		Log.d(TAG,"parseSetCommand-"+cmd);
		if(parseKeyWord(cmd,"out-",rest)){
			String v1=rest[0];
			if(!this.parseChar(v1, devKind, chrtn, rest)) return false;
			char dc=chrtn[0]; // a (analog) or d (digital)
			String s2=rest[0];
			if(!this.parseKeyWord(s2, "-", rest)) return false;
			s2=rest[0];
			if(!this.parseInt(s2, intv1, rest)) return false;
			int port=intv1[0];
			v1=skipSpace(rest[0]);
			if(!this.parseKeyWord(v1, "=", rest)) return false;
			v1=skipSpace(rest[0]);
			if(!this.parseInt(v1, intv2, rest)) return false;
			this.outputDevice((byte)dc,(byte)port,(byte)(intv2[0]));
			this.sendCommandToActivity("activity set device "+dc+" "+port+" "+intv2[0], "");
			return true;
		}
		else
		if(parseKeyWord(cmd,"xout-",rest)){
			String v1=rest[0];
			if(!this.parseChar(v1, devKind, chrtn, rest)) return false;
			char dc=chrtn[0]; // a (analog) or d (digital)
			String s2=rest[0];
			if(!this.parseKeyWord(s2, "-", rest)) return false;
			s2=rest[0];
			if(!this.parseInt(s2, intv1, rest)) return false;
			int port=intv1[0];
			v1=skipSpace(rest[0]);
			if(!this.parseKeyWord(v1, "=", rest)) return false;
			v1=skipSpace(rest[0]);
			if(!this.parseInt(v1, intv2, rest)) return false;
			this.outputDevice((byte)dc,(byte)port,(byte)(intv2[0]));
			return true;
		}
		else
		if(parseKeyWord(cmd,"readInterval",rest)){
			String v1=skipSpace(rest[0]);
			if(!this.parseKeyWord(v1, "=", rest)) return false;
			v1=skipSpace(rest[0]);
			if(!this.parseInt(v1, intv2, rest)) return false;
			int it=intv2[0];
			Log.d(TAG,"readCommandInterval-"+it);
			if(it>=1000*15)
			readCommandInterval=(long)it;
			return true;
		}
		else
		if(parseKeyWord(cmd,"sendInterval",rest)){
			String v1=skipSpace(rest[0]);
			if(!this.parseKeyWord(v1, "=", rest)) return false;
			v1=skipSpace(rest[0]);
			if(!this.parseInt(v1, intv2, rest)) return false;
			int it=intv2[0];
			Log.d(TAG,"sendCommandInterval-"+it);
			if(it>=1000*15)
			this.sendResultInterval=(long)it;
			return true;
		}
		else
		if(parseKeyWord(cmd, "pageName",rest)){
			String v1=skipSpace(rest[0]);
			String[] name=new String[1];
			final Calendar calendar = Calendar.getInstance();
			final int year = calendar.get(Calendar.YEAR);
			final int month = calendar.get(Calendar.MONTH);
			final int day = calendar.get(Calendar.DAY_OF_MONTH);
			final int hour = calendar.get(Calendar.HOUR_OF_DAY);
			final int minute = calendar.get(Calendar.MINUTE);
			final int second = calendar.get(Calendar.SECOND);
			currentHour=""+hour;
			currentDayOfWeek=""+calendar.get(Calendar.DAY_OF_WEEK);
			if(!this.parseKeyWord(v1, "=", rest)) return false;
			v1=skipSpace(rest[0]);
			if(!this.parseStrConst(v1, name, rest)) return false;
			String pNameX=name[0];
			Log.d(TAG,"pageNameX="+pNameX);
			pageName=pNameX.replace("<hour>",this.currentHour);
//			pName=pName.replace("<day_of_week>", this.currentDayOfWeek);
			Log.d(TAG,"pageName="+pageName);
			this.sendCommandToActivity("connector setPageName", pageName);
			this.connectorService.parseCommand("setPageName", pageName);
			return true;
			
		}
		return false;
	}
	public void setLastData(String dev, int v1){
		
	}
	public void setLastData(String dev, int v1, int v2){
		
	}	
  }
