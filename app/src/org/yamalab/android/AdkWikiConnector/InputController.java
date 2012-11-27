package org.yamalab.android.AdkWikiConnector;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.yamalab.android.AdkWikiConnector.R;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

public class InputController  extends AccessoryController
implements OnClickListener, WikiConnectorListener
{
	private EditText mAdk2WikiView;
	private EditText mWikiCommandView;
	private TextView mMessageView;
	AdkWikiActivity mHostActivity;
	private View mToArduinoButton;
	private View mToWikiButton;
	
	private TextView mA0v;
	private TextView mA1v;
	private TextView mA2v;
	private TextView mA3v;
	private TextView mA4v;
	private TextView mA5v;
	private TextView mA6v;
	private TextView mA7v;
	
	TextView[] analogs=new TextView[8];
	
	private Button md0b;
	private Button md1b;
	private Button md2b;
	private Button md3b;
	private Button md4b;
	private Button md5b;
	private Button md6b;
	private Button md7b;
	
	Button[] dButtons=new Button[8];
	

	InputController(AdkWikiActivity hostActivity) {
		super(hostActivity);
		mHostActivity=hostActivity;
		/* */
		mWikiCommandView = (EditText) findViewById(R.id.input_wiki_command_text);
		mAdk2WikiView = (EditText) findViewById(R.id.input_adk_to_wiki_area);
		mMessageView = (EditText)findViewById(R.id.input_message_area);
		mToArduinoButton = (View)findViewById(R.id.label_to_arduino);
		mToWikiButton = (View)findViewById(R.id.label_to_wiki);
		
		mA0v=(TextView) findViewById(R.id.a0v);
		mA1v=(TextView) findViewById(R.id.a1v);
		mA2v=(TextView) findViewById(R.id.a2v);
		mA3v=(TextView) findViewById(R.id.a3v);
		mA4v=(TextView) findViewById(R.id.a4v);
		mA5v=(TextView) findViewById(R.id.a5v);
		mA6v=(TextView) findViewById(R.id.a6v);
		mA7v=(TextView) findViewById(R.id.a7v);
		analogs[0]=mA0v; analogs[1]=mA1v; analogs[2]=mA2v; analogs[3]=mA3v;
		analogs[4]=mA4v; analogs[5]=mA5v; analogs[6]=mA6v; analogs[7]=mA7v;
		
		md0b=(Button) findViewById(R.id.d0b);
		md1b=(Button) findViewById(R.id.d1b);
		md2b=(Button) findViewById(R.id.d2b);
		md3b=(Button) findViewById(R.id.d3b);
		md4b=(Button) findViewById(R.id.d4b);
		md5b=(Button) findViewById(R.id.d5b);
		md6b=(Button) findViewById(R.id.d6b);
		md7b=(Button) findViewById(R.id.d7b);
		dButtons[0]=md0b; dButtons[1]=md1b; dButtons[2]=md2b; dButtons[3]=md3b; 
		dButtons[4]=md4b; dButtons[5]=md5b; dButtons[6]=md6b; dButtons[7]=md7b;

		/* */
		mToArduinoButton.setOnClickListener(this);
		mToWikiButton.setOnClickListener(this);
	}
	protected void onAccesssoryAttached() {
	}

	public void onPir(int pir) {
//		setPir(pir);
	}

	public void println(String x){
		this.mMessageView.append(x+"\n");
	}
	
	public void setAnalogValue(int port, int val){
		if(port<0) return;
		if(port>7) return;
		analogs[port].setText(""+val);
	}

	public void setDigitalValues(byte val){
		int mask=0x0080;
		for(int i=0;i<8;i++){
			if((val & mask)==0){
	    	    dButtons[i].setBackgroundColor(0xfff0f0f0);				
				dButtons[i].setSelected(false);
			}
			else{
	    	    dButtons[i].setBackgroundColor(0xff10c0c0);
				dButtons[i].setSelected(true);
			}
			mask=mask >> 1;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(this.mHostActivity==null) return;
		if(v==mToArduinoButton){
			String commands=this.mWikiCommandView.getText().toString();
			StringTokenizer st=new StringTokenizer(commands,"\n");
			while(st.hasMoreElements()){
				String line=st.nextToken();
				mHostActivity.sendCommandToService("adk "+line, "");
			}
		}
//		System.out.println("onClick...");
	}
	@Override
	public void setWikiData(String x) {
		// TODO Auto-generated method stub
		
	}
	String outputText;
	public void setAdk2WikiText(String x){
		outputText=x;
		if(mAdk2WikiView==null) return;
        ((Activity)mHostActivity).runOnUiThread(new Runnable() {
        	@Override
        	public void run(){
		         mAdk2WikiView.setText(""+outputText);
        	}
        });
	}
	public void appendAdk2WikiText(String x){
		outputText=x;
		if(mAdk2WikiView==null) return;
        ((Activity)mHostActivity).runOnUiThread(new Runnable() {
        	@Override
        	public void run(){
		        mAdk2WikiView.append(""+outputText);
        	}
        });
	}
	String wikiCommandText;
	public void setWikiCommand(String x){
		wikiCommandText=x;
		if(mWikiCommandView==null) return;
        ((Activity)mHostActivity).runOnUiThread(new Runnable() {
        	@Override
        	public void run(){
		        mWikiCommandView.setText(""+wikiCommandText);
        	}
        });
	}
	public void appendWikiCommand(String x){
		wikiCommandText=x;
		if(mWikiCommandView==null) return;
        ((Activity)mHostActivity).runOnUiThread(new Runnable() {
        	@Override
        	public void run(){
		       mWikiCommandView.append(""+wikiCommandText);
        	}
        });
	}
	String messageText;
	public void setMessage(String x){
		messageText=x;
		if(mMessageView==null) return;
        ((Activity)mHostActivity).runOnUiThread(new Runnable() {
        	@Override
        	public void run(){
		         mMessageView.setText(""+messageText);
        	}
        });
	}
	Vector<String> messageLines=new Vector();
	int maxMessageLines=60;
	public void appendMessage(String x){
		if(mMessageView==null) return;
		messageLines.add(x);
		while(messageLines.size()>maxMessageLines){
			messageLines.remove(0);
		}
        ((Activity)mHostActivity).runOnUiThread(new Runnable() {
        	@Override
        	public void run(){
		        mMessageView.setText("");
		        for(int i=0;i<messageLines.size();i++){
			       mMessageView.append(messageLines.elementAt(i)+"\n");
		        }
        	}
        });
	}

}
