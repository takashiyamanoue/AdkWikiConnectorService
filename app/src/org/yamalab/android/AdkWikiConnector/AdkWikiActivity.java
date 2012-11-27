package org.yamalab.android.AdkWikiConnector;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.yamalab.android.AdkWikiConnector.R;
import org.yamalab.android.AdkWikiConnector.AndroidWikiConnector;

import org.yamalab.android.AdkWikiConnector.InputController;
import org.yamalab.android.AdkWikiConnector.AdkThread.DigitalMsg;
import org.yamalab.android.AdkWikiConnector.AdkThread.AnalogMsg;
import org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.connector.StringMsg;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AdkWikiActivity extends Activity implements OnClickListener {
	static final String TAG = "AdkWikiActivity";
		
	/** Called when the activity is first created. */
	TextView mInputLabel;
	TextView mOutputLabel;
	TextView mWikiLabel;
	TextView mDebugLabel;
	TextView mStopLabel;
	LinearLayout mInputContainer;
	LinearLayout mOutputContainer;
	LinearLayout mWikiContainer;
	LinearLayout mDebugContainer;
	LinearLayout mLoginContainer;
	LinearLayout mBasicContainer;
	Drawable mFocusedTabImage;
	Drawable mNormalTabImage;
	InputController mInputController;
	OutputController mOutputController;
	AndroidWikiConnector connector;
	public Properties setting;
//	public Hashtable properties;
	AdkService adkService;
	boolean mBound=false;

	/* */                 // comment the following onCreate and onResume for eclipse debugging
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG,"onCreate");
		mFocusedTabImage = getResources().getDrawable(
				R.drawable.tab_focused_holo_dark);
		mNormalTabImage = getResources().getDrawable(
				R.drawable.tab_normal_holo_dark);
		Log.d(TAG,"onCreate-after prepareUsbConnection");
		setContentView(R.layout.main);
		
		if (mAccessory != null) {        // debug 2012/7/26  do not comment out, eclipse
		
			this.enableControls(true);
		
		} else {                           // debug 2012/7/26 do not comment out, eclipse
			this.enableControls(false);    // debug 2012/7/26 do not comment out, eclipse
		}                                  // debug 2012/7/26 do not comment out, eclipse
		
	}
	
	public void onResume(){
		super.onResume();
		Log.d(TAG,"onResume");
		// comment out when running at eclipse. from here.
		prepareUsbConnection();
		if (mInputStream != null && mOutputStream != null) {
			Log.d(TAG,"onResume - mInputStream!=null");
			this.doBindService();
			return;
		}

		Log.d(TAG,"onResume - mInputStream==null");
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (mUsbManager.hasPermission(accessory)) {
				openAccessory(accessory);
			   // comment out when running at eclipse. until here.
		        startService();         // do not comment out when running at eclipse.
				this.doBindService();   // do not comment out when running at eclipse. 
			  // comment out when running at eclipse from here.
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory,
								mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d(TAG, "mAccessory is null");
		}
           //comment out when running at eclipse until here. 
	}
/*	 */
	/*   // un comment the following onCreate and onResume for eclipse debugging.
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG,"onCreate");
		mFocusedTabImage = getResources().getDrawable(
				R.drawable.tab_focused_holo_dark);
		mNormalTabImage = getResources().getDrawable(
				R.drawable.tab_normal_holo_dark);
		Log.d(TAG,"onCreate-after prepareUsbConnection");
		setContentView(R.layout.main);
		this.enableControls(true);
		
//		if (mAccessory != null) {        // debug 2012/7/26  do not comment out, eclipse
//		} else {                           // debug 2012/7/26 do not comment out, eclipse
//			this.enableControls(false);    // debug 2012/7/26 do not comment out, eclipse
//		}                                  // debug 2012/7/26 do not comment out, eclipse
		
	}
	public void onResume(){
		super.onResume();
		Log.d(TAG,"onResume");
		// comment out when running at eclipse. from here.

		// comment out when running at eclipse. until here.
		        startService();         // do not comment out when running at eclipse.
				this.doBindService();   // do not comment out when running at eclipse. 
        //comment out when running at eclipse until here. 
	}
	 */

	protected AdkThread mHostActivity;

	public void showControls() {
		Log.d(TAG,"showControls");
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);        
        setContentView(R.layout.main);
		mInputController = new InputController(this);
		mOutputController = new OutputController(this, false);
		/* */
		Log.d(TAG,"showControls-1");
		mInputLabel = (TextView) findViewById(R.id.main_input_label);
		mOutputLabel = (TextView) findViewById(R.id.main_output_label);
		mStopLabel = (TextView) findViewById(R.id.main_stop_label);
		mWikiLabel =   (TextView) findViewById(R.id.main_wiki_label);
		mDebugLabel = (TextView) findViewById(R.id.main_debug_label);
/* */
		Log.d(TAG,"showControls-2");
/* */
		mInputContainer = (LinearLayout) findViewById(R.id.inputContainer);
		mOutputContainer = (LinearLayout) findViewById(R.id.outputContainer);
  	    mWikiContainer = (LinearLayout) findViewById(R.id.connector_container);
		mDebugContainer = (LinearLayout) findViewById(R.id.pukiwiki_connector_debugger);
/* */
		Log.d(TAG,"showControls-3");
		this.loadProperties();
		connector=new AndroidWikiConnector(this);
		connector.setSetting(this.setting);
		this.setWikiConnector(connector);
		connector.setWikiConnectorListener(this.getInputController());
		Log.d(TAG,"showControls-4");
		
		/* */
		mInputLabel.setOnClickListener(this);
		mOutputLabel.setOnClickListener(this);
		mStopLabel.setOnClickListener(this);
		mWikiLabel.setOnClickListener(this);
		mDebugLabel.setOnClickListener(this);
		showTabContents(R.id.main_input_label);
	}

	protected void hideControls() {
		Log.d(TAG,"hideControls");
		setContentView(R.layout.no_device);
		mInputController = null;
		mOutputController = null;
	}

	public void showTabContents(int id) {
		Log.d(TAG,"showTabContents id="+id);
		mInputContainer.setVisibility(View.GONE);
		mInputLabel.setBackgroundDrawable(mNormalTabImage);
		mOutputContainer.setVisibility(View.GONE);
		mOutputLabel.setBackgroundDrawable(mNormalTabImage);
		mWikiContainer.setVisibility(View.GONE);
		mWikiLabel.setBackgroundDrawable(mNormalTabImage);
		mDebugContainer.setVisibility(View.GONE);
		mDebugLabel.setBackgroundDrawable(mNormalTabImage);
		if (id==R.id.main_input_label) {
			Log.d(TAG,"showTabContents -main_input_label id="+id);
			mInputContainer.setVisibility(View.VISIBLE);
			mInputLabel.setBackgroundDrawable(mFocusedTabImage);
		}
		else
		if(id==R.id.main_output_label)
		{
			Log.d(TAG,"showTabContents -main_output_label id="+id);
			mOutputContainer.setVisibility(View.VISIBLE);
			mOutputLabel.setBackgroundDrawable(mFocusedTabImage);
		}
		else
		if(id==R.id.main_wiki_label)
			{
			Log.d(TAG,"showTabContents -main_wiki_label id="+id);
				mWikiContainer.setVisibility(View.VISIBLE);
				mWikiLabel.setBackgroundDrawable(mFocusedTabImage);
			}
		else
		if(id==R.id.main_debug_label){
			Log.d(TAG,"showTabContents -main_debug_label id="+id);
			mDebugContainer.setVisibility(View.VISIBLE);
			mDebugLabel.setBackgroundDrawable(mFocusedTabImage);
			
		}

	}

	public void onClick(View v) {
		int vId = v.getId();
		Log.d(TAG,"onClick("+vId+")");
		if(vId==R.id.main_stop_label){
			stopService();
			finish();
		}
		else
		showTabContents(vId);

	}
	public void loadProperties(){
		Log.d(TAG,"loadProperties");
		this.setting=new Properties();
		SharedPreferences pref =
				getSharedPreferences("pref",MODE_WORLD_READABLE|MODE_WORLD_WRITEABLE);
		if(pref!=null){
			Log.d(TAG,"loadProperties- pref!=null,setting");
			Map m=pref.getAll();
			Set keys = m.keySet();  
			Iterator itr=keys.iterator();
			while(itr.hasNext()){
				String key=(String)(itr.next());
				String info=(String)(m.get(key));
				setting.put(key, info);
			}
		}
		else{
			Log.d(TAG,"loadProperties- pref==null");			
		}
	}
	public void saveProperties(){
		Log.d(TAG,"saveProperties");
        //add "returnKey" as a key and assign it the value
        //in the textbox...
		if(setting!=null){
			SharedPreferences pref =
				getSharedPreferences("pref", MODE_WORLD_READABLE|MODE_WORLD_WRITEABLE);
			Editor e = pref.edit();
			Enumeration keys=setting.keys();
			while(keys.hasMoreElements()){
				String key=(String)(keys.nextElement());
				String info=(String)(this.setting.getProperty(key));
				e.putString(key, info);
			}
			e.commit();
		}
		else{
			
		}
	}
	public void onPause(){
		Log.d(TAG,"onPause");
		this.saveProperties();
		this.doUnbindService();
		super.onPause();
	}
	@Override  
	protected void onSaveInstanceState(Bundle outState) {  
	    super.onSaveInstanceState(outState);  
		Log.d(TAG,"onSaveInstanceState");
	    saveProperties();
	}
	@Override  
	protected void onRestoreInstanceState(Bundle savedInstanceState) {  
	    super.onRestoreInstanceState(savedInstanceState);  
		Log.d(TAG,"onRestoreInstanceState");
	    this.loadProperties();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG,"onCreateOptionsMenu");
		menu.add("Simulate");
		menu.add("Quit");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG,"onOptionItemSelected");
		if (item.getTitle() == "Simulate") {
			showControls();
		} else if (item.getTitle() == "Quit") {
			finish();
			System.exit(0);
		}
		return true;
	}

	protected void enableControls(boolean enable) {
		Log.d(TAG,"enableControls("+enable+")");
		/* x */
		if (enable) {
			showControls();
		} else {
			hideControls();
		}
       /*		*/
		// debug
//		showControls();
	}
	
	protected void handleDigitalMessage(DigitalMsg m){
		if(mInputController==null) return;
		if(m==null) return;
		mInputController.setDigitalValues(m.getState());
	}
	protected void handleAnalogMessage(AnalogMsg m){
		if(mInputController==null) return;
		if(m==null) return;
		mInputController.setAnalogValue(m.getPort(), m.getVal());
	}

	protected void handleMessageString(StringMsg m) {
		if(m==null) return;
		String cmd=m.getCommand();
//		Log.d(TAG,"handleMessageString("+cmd+")");
		boolean rtn=parseStringMessage(m);
	}
	protected boolean parseStringMessage(StringMsg m){
		String x=m.getCommand();
		String cmd=skipSpace(x);
		String [] rest=new String[1];
		String [] match=new String[1];
		int [] intv = new int[1];
		Log.d(TAG,"parseStringMessage cmd="+cmd+")");
		if(parseKeyWord(x,"connector ",rest)){
		    String subcmd=skipSpace(rest[0]);
		    if(connector!=null){
		    	return connector.parseCommand(subcmd, m);
		    }
		}
		else
		if(parseKeyWord(x,"activity ",rest)){
			return parseActivityMessage(rest[0],m);
		}
		return false;
	}

	protected boolean parseActivityMessage(String x, StringMsg m){
		String subcmd=skipSpace(x);
		String [] rest=new String[1];
		String [] match=new String[1];
		Log.d(TAG,"parseActivityMessage("+x+","+m.getValue()+")");
		int [] intv = new int[1];
		if(mInputController==null) return true;
		if(parseKeyWord(subcmd,"set ",rest)){
		   	String subsub=skipSpace(rest[0]);
		   	if(parseKeyWord(subsub,"output",rest)){
		        mInputController.setAdk2WikiText(m.getValue());
			    return true;
		   	}
		   	else
		   	if(parseKeyWord(subsub,"input",rest)){
		   		mInputController.setWikiCommand(m.getValue());
		   		return true;
		   	}
		   	else
		   	if(parseKeyWord(subsub,"message",rest)){
		   		mInputController.setMessage(m.getValue());
		   		connector.setMessage(m.getValue());
		   		return true;
		   	}
		   	else
			if(parseKeyWord(subsub,"show",rest)){
			   	String subsubsub=skipSpace(rest[0]);
			 	if(subsubsub.startsWith("connector")){
			 		this.showTabContents(R.id.main_wiki_label);
			 	}
			   	return true;
			}
		   	else
			if(parseKeyWord(subsub,"device",rest)){
			   	String subsubsub=skipSpace(rest[0]);
			 	this.mOutputController.setValue(subsubsub);
			   	return true;
			}
		   		
		}
		else
		if(parseKeyWord(subcmd,"append ",rest)){
		   	String subsub=skipSpace(rest[0]);
		   	if(parseKeyWord(subsub,"output",rest)){
		        mInputController.appendAdk2WikiText(m.getValue());
			    return true;
		   	}
		   	else
		   	if(parseKeyWord(subsub,"input",rest)){
			    mInputController.appendWikiCommand(m.getValue());
		   	}
		   	else
		   	if(parseKeyWord(subsub,"message",rest)){
			    mInputController.appendMessage(m.getValue());
		   	}
		}
		else
		if(parseKeyWord(subcmd,"request ",rest)){
		   	String w=skipSpace(rest[0]);
		   	if(parseKeyWord(w,"setting",rest)){
		 	   this.sendSetting();
			}
		}	    
		return false;
	}
	protected String skipSpace(String x){
		while(x.startsWith(" ")) x=x.substring(1);
		return x;
	}
	boolean parseKeyWord(String x, String key, String [] rest){
		if(x.startsWith(key)){
			rest[0]=x.substring(key.length());
			return true;
		}
		return false;
	}
	boolean parseInt(String x, int[] intrtn, String [] rest){
		char c=x.charAt(0);
		String ix="";
		while('0'<=c && c<='9'){
			ix=ix+c;
			x=x.substring(1);
			c=x.charAt(0);
		}
		if(!ix.equals("")){
		   int ixx=(new Integer(ix)).intValue();
		   intrtn[0]=ixx;
		   rest[0]=x;
		   return true;
		}
		return false;
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
	protected void handleMessageConnect(String x) {
		Log.d(TAG,"handleMessageConnect("+x+")");
		if(x==null) return;
		StringTokenizer st=new StringTokenizer(x," ");
		String c1=st.nextToken();
		if(c1==null) return;
		if(c1.equals("display")){
			String c2=st.nextToken();
			if(c2==null) return;
			if(c2.equals("disconnect")){
				this.hideControls();
			}
		}
	}
	
	/* */
	protected void setWikiConnector(AndroidWikiConnector c){
		Log.d(TAG,"setWikiConnector");
		if(c==null) return;
		/*
		if(mInputController!=null)
		mInputController.setWikiConnector(c);
		*/
	}
	
	protected InputController getInputController(){
		return this.mInputController;
	}
	
    /** Messenger for communicating with service. */
	Messenger mService = null;
	/** Flag indicating whether we have called bind on the service. */
	boolean mIsBound;
	/** Some text view we are using to show state information. */
	TextView mCallbackText;
	/**         
	 * Handler of incoming messages from service.
	 */

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AdkThread.MESSAGE_DIGITAL:
				DigitalMsg o = (DigitalMsg) msg.obj;
				handleDigitalMessage(o);
				break;

			case AdkThread.MESSAGE_ANALOG:
				AnalogMsg t = (AnalogMsg) msg.obj;
				handleAnalogMessage(t);
				break;

			case AdkThread.MESSAGE_CONNECT:
				String x = (String) msg.obj;
				handleMessageConnect(x);
				break;
				
			case AdkThread.MESSAGE_STRING:
				Object m = msg.obj;
				handleMessageString((StringMsg)m);
				break;
				
			}
		}
	};
	/*
	 * Target we publish for clients to send messages to IncomingHandler.
	 *         */
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	/**         
	 * * Class for interacting with the main interface of the service.
	 *          */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			Log.d(TAG,"-ServiceConnection-onServiceConnected");
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service.  We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mService = new Messenger(service);
//			mCallbackText.setText("Attached.");
			// We want to monitor the service for as long as we are
			// connected to it.
			try {
				Message msg = Message.obtain(null,
						AdkService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
				// Give it some value as an example.
//				msg = Message.obtain(null,
//						AdkService.MSG_SET_VALUE, this.hashCode(), 0);
//				mService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even
				// do anything with it; we can count on soon being
				// disconnected (and then reconnected if it can be restarted)
				// so there is no need to do anything here.
			}
			sendUsbFileDescriptor();
			// As part of the sample, tell the user what happened.
			Toast.makeText(AdkWikiActivity.this, "AdkService Connected",
					Toast.LENGTH_SHORT).show();

		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG,"onServiceDisconnected("+className+")");
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;
//			mCallbackText.setText("Disconnected.");
			// As part of the sample, tell the user what happened.
			Toast.makeText(AdkWikiActivity.this, "AdkService disconnected",
					Toast.LENGTH_SHORT).show();
		}
	};
	void doBindService() {
		Log.d(TAG,"dobindService");
		// Establish a connection with the service.  We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
		bindService(new Intent(AdkWikiActivity.this,
				AdkService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;

	}
	public void sendSetting(){
		if(setting==null){
			Log.d(TAG,"sendSetting, setting==null");
			return;
		}
		String url=setting.getProperty("managerUrl");
		if(url!=null){
			Log.d(TAG,"sendSetting--url!=null");
			sendCommandToService("connector setUrl-",url);
		    String urlWithoutParameters=getUrlWithoutParameters(url);
	            String authUrl="basicAuth-"+urlWithoutParameters;
		    sendCommandToService("connector setAuth2Url-",authUrl);
	            String idPass=setting.getProperty(authUrl);
		    if(idPass!=null) sendCommandToService("connector setIdPass-",idPass);
		    String registeredUrl=setting.getProperty("auth-url");
		    if(registeredUrl!=null) sendCommandToService("connector setAuthUrl-",registeredUrl);
		}

	}
	
	private String getUrlWithoutParameters(String url){
		int i=url.indexOf("?");
		if(i<0) return url;
		String rtn=url.substring(0,i);
		return rtn;
	}
	void doUnbindService() {
		Log.d(TAG,"doUnbindService..mIsBound="+mIsBound);
		if (mIsBound) {
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							AdkService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service
					// has crashed.
				}
				mService=null;
			}
			Log.d(TAG,"doUnbindService..2");
			/* */  
			if(mUsbReceiver!=null)
	    	    unregisterReceiver(mUsbReceiver);
	    	/*  */
			if(mConnection!=null)
		    	unbindService(mConnection);
			Log.d(TAG,"doUnbindService..3");
			mIsBound = false;
		}
	}
	public void onDestroy(){
		Log.d(TAG,"onDestroy..1");
		this.doUnbindService();
		Log.d(TAG,"onDestroy..2");		
		super.onDestroy();
	}
	private static final String mServiceName = AdkService.class.getCanonicalName(); 
	public boolean isServiceRunning() {
		Log.d(TAG,"isServiceRunning");
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> runningApp = activityManager.getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo info : runningApp) {
			if (mServiceName.equals(info.service.getClassName())) {
				Log.d(TAG,"isServiceRunning==true");
				return true;
			}
		}
		Log.d(TAG,"isServiceRunning==false");
		return false;
	}
	private void stopService(){
		Log.d(TAG,"stopService");
		try{
		   Message msg = Message.obtain(null,AdkService.MSG_STOP);
		   mService.send(msg);
		}
		catch(Exception e){
			
		}
		this.doUnbindService();
		closeAccessory();
	}
	private void sendUsbFileDescriptor(){
		Log.d(TAG,"sendUsbFileDescriptor");
		try{
		   Message msg = Message.obtain(null,AdkService.MSG_SET_USBFILEDESCRIPTOR);
		   msg.obj=this.mFileDescriptor;
		   mService.send(msg);
		}
		catch(Exception e){
			
		}
	}
	public void sendCommandToService(String cmd, String val){
		Log.d(TAG,"sendCommandToService("+cmd+","+val+")");
		try{
		   Message msg = Message.obtain(null,AdkService.MSG_EXEC_COMMAND);
		   msg.obj=new StringMsg(cmd,val);
		   mService.send(msg);
		}
		catch(Exception e){
			
		}
	}	/* */
	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;

    public static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";

	UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	public FileInputStream mInputStream;
	public FileOutputStream mOutputStream;
	
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
     		Log.i(TAG, "BroadcastReceiver-onRecieve");
       	    String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
	     		Log.i(TAG, "BroadcastReceiver-onRecieve- ACTION_USB_PERMISSION");
				synchronized (this) {
					UsbAccessory accessory = UsbManager.getAccessory(intent);
					if (intent.getBooleanExtra(	UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openAccessory(accessory);
				        startService();
				        doBindService();
					} else {
						Log.d(TAG, "permission denied for accessory "
								+ accessory);
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
	     		Log.i(TAG, "BroadcastReceiver-onRecieve- ACTION_USB_ACCESSORY_DETACHED");
				UsbAccessory accessory = UsbManager.getAccessory(intent);
				if (accessory != null && accessory.equals(mAccessory)) {
					closeAccessory();
				}
			}
        }
     };
     
     private void openAccessory(UsbAccessory accessory){
 		Log.d(TAG, "openAccessory");
      	mFileDescriptor = mUsbManager.openAccessory(accessory);
      	if(mFileDescriptor !=null){
     		Log.d(TAG, "openAccessory- mFileDescriptor!=null");
      		mAccessory = accessory;
      		/* */
      		FileDescriptor fd = mFileDescriptor.getFileDescriptor();
      		mInputStream = new FileInputStream(fd);
      		mOutputStream = new FileOutputStream(fd);
      		/* */
			enableControls(true);
	 		Log.d(TAG, "openAccessory -2 ");
			if(mInputController!=null)
				   mInputController.accessoryAttached();
			if(mOutputController!=null)
				  mOutputController.accessoryAttached();
			this.enableControls(true);
		}
      	else{
      		Log.d(TAG, "accessory open fail");
      	}
      }
    public void startService(){
		if(!this.isServiceRunning()){
			Log.d(TAG,"startService .. startService");
			// Service‚ðIntent‚Å‹N“®‚·‚é
		    Intent intent = new Intent(AdkWikiActivity.this, AdkService.class);
		    startService(intent);
		}
		else{
			Log.d(TAG,"openAccessory .. service is already running?");				
		}    	
    }
  	private void closeAccessory() {
		Log.d(TAG, "closeAccessory");
 		try {
 			if (mFileDescriptor != null) {
 				mFileDescriptor.close();
 			}
 		} catch (IOException e) {
 		} finally {
 			mFileDescriptor = null;
 			mAccessory = null;
 		}
     }
/* */  	
	public void prepareUsbConnection(){
 		Log.d(TAG, "prepareUsbConnection");
        mUsbManager = UsbManager.getInstance(this);
        mPermissionIntent = 
     		PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver, filter); 

		if (getLastNonConfigurationInstance() != null) {
	 		Log.d(TAG, "getLastnonConfigurationInstance()!=null");
			mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
			openAccessory(mAccessory);
	        startService();
	        this.doBindService();
		}
	}
}