package org.yamalab.android.AdkWikiConnector;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.StringTokenizer;

import org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.connector.PukiwikiJavaApplication;
import org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.connector.SaveButtonDebugFrame;
import org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.connector.StringMsg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Handler;
public class AndroidWikiConnector extends AccessoryController
implements OnClickListener, PukiwikiJavaApplication
{
	private static final String TAG = "AndroidWikiConnector";
	private EditText loadArea;
    private EditText saveArea;
    private EditText messageArea;
    private EditText urlArea;
    private View loadButton;
    private View saveButton;
    private SaveButtonDebugFrame debugger;
	public Properties setting;
	private WikiConnectorListener wikiConnectorListener;
	private AdkWikiActivity mActivity;
//	private AuthDialog mAuthDialog;
	private TextView mLoginRequiredLabel;
	private EditText mLoginIdField;
	private EditText mLoginPwField;
	private String responseLabel;
	private View mLoginCancelButton;
	private View mLoginButton;

    public AndroidWikiConnector(AdkWikiActivity activity) { //adk
		super(activity);
		mActivity=activity;
		Log.d(TAG,"AndroidWikiConnector(activity)");
    	debugger=new SaveButtonDebugFrame(activity);
    	debugger.setApplication(this);
    	loadArea=(EditText)findViewById(R.id.wiki_download_area);
    	saveArea=(EditText)findViewById(R.id.wiki_upload_area);
    	messageArea=(EditText)findViewById(R.id.wiki_message_area);
    	urlArea=(EditText)findViewById(R.id.wiki_url_text_field);
    	loadButton=(View)findViewById(R.id.wiki_load_button);
    	saveButton=(View)findViewById(R.id.wiki_save_button);
		mLoginIdField=(EditText)this.findViewById(R.id.wiki_login_id_field);
		mLoginPwField=(EditText)this.findViewById(R.id.wiki_login_pw_field);
		mLoginButton=(View)this.findViewById(R.id.wiki_login_button);
		mLoginCancelButton=(View)this.findViewById(R.id.wiki_cancel_button);
		mLoginRequiredLabel=(TextView)this.findViewById(R.id.wiki_login_required_label);
		this.mLoginRequiredLabel.setText("");
        /* */
   	    setListeners();
   	    
	}
	private void setListeners(){
       loadButton.setOnClickListener(this);
       saveButton.setOnClickListener(this);
		mLoginButton.setOnClickListener(this);
		mLoginCancelButton.setOnClickListener(this);
	}
	public void setSetting(Properties s){
		Log.d(TAG,"AndroidWikiConnector-setSetting");
		this.setting=s;
		if(setting!=null){
			Log.d(TAG,"AndroidWikiConnector-setSetting setting!=null");
			String url=setting.getProperty("managerUrl");
			if(url!=null){
				this.urlArea.setText(url);
				this.sendCommandToActivity("connector setUrl-", url);
			}
			else{
				this.setting.put("managerUrl", ""+this.urlArea.getText());
			}
			if(debugger!=null){
	    	    debugger.setSetting(setting);
			}
		}
	}

	public void setWikiConnectorListener(WikiConnectorListener x){
		this.wikiConnectorListener=x;
	}
	
    /** Called when the activity is first created. */

    @Override
    public void onClick(View v){
    	if(setting!=null){
    		String url=(this.urlArea.getText()).toString();
            setting.setProperty("managerUrl", url);
    	}
		String id=mLoginIdField.getText().toString();
		this.sendCommandToActivity("connector setLoginId-", id);
		String pas=mLoginPwField.getText().toString();
		this.sendCommandToActivity("connector setLoginPassword-", pas);
		if(v==this.loadButton){
			String url=""+this.urlArea.getText();
//			String url="http://www.yama-lab.org/adk-wiki-1/index.php?Test1";
//			System.out.println(url);
			this.setting.setProperty("managerUrl", url);
			this.mActivity.saveProperties();
			this.println(url);
			this.sendCommandToActivity("connector setUrl-", url);
			sendCommandToActivity("connector read-", "");
 		}
		else
		if(v==this.saveButton){
			String x=""+this.saveArea.getText();
			String url=""+this.urlArea.getText();
			this.sendCommandToActivity("connector saveTextAtUrl-"+url, x);
		}
		if(v==mLoginButton){
			this.loginButtonActionPerformed();
		}
		else
		if(v==mLoginCancelButton){
			this.cancelButtonActionPerformed();
		}		
    }
	@Override
	public String getOutput() {
		// TODO Auto-generated method stub
		return null;
	}
	String inputText;
	@Override
	public void setInput(String x) {
		// TODO Auto-generated method stub
		this.wikiConnectorListener.setWikiData(x);
	}
	@Override
	public void setSaveButtonDebugFrame(SaveButtonDebugFrame f) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onAccesssoryAttached() {
		// TODO Auto-generated method stub
		
	}

	public void println(String x){
		this.messageArea.append(x+"\n");
	}
	String outputText;
	public void setOutput(String x){
		outputText=x;
        ((Activity)mActivity).runOnUiThread(new Runnable() {
        	@Override
        	public void run(){
        		saveArea.setText(outputText);
        	}
        });
	}
	public void write(){
		this.onClick(saveButton);
	}
	String loginId;
	public void setLoginId(String id){
		loginId=id;
		if(mLoginIdField==null) return;
	  	  ((Activity)mActivity).runOnUiThread(new Runnable() {
	          @Override
	          public void run() {
		         mLoginIdField.setText(loginId);
	          }
	  	  });
	}
	public void setLoginPassword(String password){
		if(mLoginIdField==null) return;
        mLoginPwField.setText(password);
	}

	public void setLoginMessage(String x){
	}
	public String getLoginId(){
		if(mLoginIdField==null) return null;
		return mLoginPwField.getText().toString();
	}
	public String getLoginPass(){
		if(mLoginPwField==null) return null;
		return mLoginPwField.getText().toString();
	}
	public void setLoginRequired(){
  	  ((Activity)mActivity).runOnUiThread(new Runnable() {
          @Override
          public void run() {
		    if(mLoginRequiredLabel==null) {
			    Log.d(TAG,"setLoginRequired-authDialog==null");
			    return;
		    }
		    Log.d(TAG,"setLoginRequiread-authDialog!=null");
		    mActivity.showTabContents(R.id.main_wiki_label);
          }
      });
		
	}
	@Override
	public void sendCommandToActivity(String c, String v) {
		// TODO Auto-generated method stub
		if(mActivity==null) return;
		mActivity.sendCommandToService(c,v);
	}
	public void appendOutput(String x){
		Log.d(TAG,"appendOutput("+x+")");
		outputText=x;
        ((Activity)mActivity).runOnUiThread(new Runnable() {
        	@Override
        	public void run(){
        		saveArea.append(outputText);
        	}
        });
	}
	public void setMessage(String x){
		Log.d(TAG,"setMessage("+x+")");
		outputText=x;
        ((Activity)mActivity).runOnUiThread(new Runnable() {
        	@Override
        	public void run(){
        		messageArea.setText(outputText);
        	}
        });
	}
	public boolean parseCommand(String subcmd, StringMsg m){
		Log.d(TAG,"parseCommand("+subcmd+")");
		String[] rest=new String[1];
	    if(parseKeyWord(subcmd,"setOutput",rest)){
		    setOutput(m.getValue());
			return true;
	    }
	    else
	    if(parseKeyWord(subcmd,"setInput",rest)){
		    setInput(m.getValue());
			return true;
	    }
	    else
	    if(parseKeyWord(subcmd,"appendOutput",rest)){
		    appendOutput(m.getValue());
			return true;
	    }
	    else
	    if(parseKeyWord(subcmd,"setMessage",rest)){
		    setMessage(m.getValue());		    
			return true;
	    }
	    else
	    if(parseKeyWord(subcmd,"setAuthUrl-",rest)){
			this.setting.put("auth-url", m.getValue());
			return true;
	    }
	    else
	    if(parseKeyWord(subcmd,"setLoginId-",rest)){
			setLoginId(m.getValue());
			return true;
	    }
	    else
	    if(parseKeyWord(subcmd,"setLoginPassword-",rest)){
		    setLoginPassword(m.getValue());		    
			return true;
	    }
	    else
	    if(parseKeyWord(subcmd,"setLoginMessage",rest)){
			setLoginMessage(m.getValue());		    
			return true;
	    }
	    else
	    if(parseKeyWord(subcmd,"loginRequired",rest)){
			this.setLoginRequired();		    
			return true;
	    }
	    else
	    if(parseKeyWord(subcmd,"request ",rest)){
	    	String subsub=skipSpace(rest[0]);
			if(parseKeyWord(subsub,"read",rest)){
				this.onClick(this.loadButton);
			    return true;
			}
			if(parseKeyWord(subsub,"url",rest)){
				String url=""+this.urlArea.getText();
				this.sendCommandToActivity("connector setUrl-", url);				
			    return true;
			}
	    }
	    else
	    if(parseKeyWord(subcmd,"setPageName",rest)){
			String url=""+this.urlArea.getText();
			StringTokenizer st=new StringTokenizer(url,"?");
			if(!st.hasMoreElements()) return false;
			String baseUrl=st.nextToken();
			if(!st.hasMoreElements()) return false;
			String oldPageName=st.nextToken();
			String newPageUrl=baseUrl+"?"+m.getValue();
			Log.d(TAG,"parseCommand-setPageName-"+url+" to "+newPageUrl);
			this.urlArea.setText(""+newPageUrl);
			return true;
	    }
	    return false;
	}
	String skipSpace(String x){
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
	/* */
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
	private void cancelButtonActionPerformed() {
	//	System.out.println("cancelButton.actionPerformed, event="+evt);
		//TODO add your code for cancelButton.actionPerformed
		this.mLoginIdField.setText("");
		this.mLoginPwField.setText("");
		this.mLoginRequiredLabel.setText("");
		this.sendCommandToActivity("connector loginCancel-", "");
	}
	
	private void loginButtonActionPerformed() {
	//	System.out.println("loginButton.actionPerformed, event="+evt);
		//TODO add your code for loginButton.actionPerformed
		Log.d(TAG,"loginButtonActionPerformed");
		this.mLoginRequiredLabel.setText("");
		String url="";
		if(setting!=null){
		    url=(this.urlArea.getText()).toString();
		    setting.setProperty("managerUrl", url);
		}
		this.sendCommandToActivity("connector setUrl-", url);
		String id=mLoginIdField.getText().toString();
		this.sendCommandToActivity("connector setLoginId-", id);
		String pas=mLoginPwField.getText().toString();
		this.sendCommandToActivity("connector setLoginPassword-", pas);
		String urlWithoutParameters=this.setting.getProperty("auth-url");
	    String authUrl="basicAuth-"+urlWithoutParameters;
		this.setting.setProperty(authUrl,id+"::::"+pas);
		this.sendCommandToActivity("connector login-", "");
		this.mActivity.saveProperties();
	}	
	/* */

}
