package org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.connector;


import java.util.Properties;
import org.yamalab.android.AdkWikiConnector.AccessoryController;
import org.yamalab.android.AdkWikiConnector.AdkWikiActivity;
import org.yamalab.android.AdkWikiConnector.R;
import org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.connector.PukiwikiJavaApplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/

public class SaveButtonDebugFrame extends AccessoryController implements OnClickListener 
{
	private static final String TAG = "SaveButtonDebugFrame";
	private View saveButton;
	private String baseUrl;
	private String pageName;
	private String plugInName;
	private String pageCharSet;
	private String charset;

	private static final int AUTH_MODE_PREEMPTIVE = 0;
    private static final int AUTH_MODE_NOT_PREEMPTIVE = 1;
    private static final int AUTH_MODE_CONSOLE = 2;
    private Properties setting;
	private View clearButton;
	private EditText messageTextArea;
	private CheckBox showMessagesCheckBox;
	private View readFromPukiWikiButton;
	private View paramButton;
	private View updateButton;
//	private JScrollPane messagePane;
	private View editPageButton;
	private View connectButton;
	private EditText urlField;
	private View sendButton;
	private EditText inputField;
	private PukiwikiJavaApplication application;
//	LinearLayout mAuthContainer;
//	private LinearLayout debuggerLayout;
	private AdkWikiActivity mActivity;

	public SaveButtonDebugFrame(AdkWikiActivity activity) {
		super(activity);	
		mActivity=activity;
		messageTextArea=(EditText)this.findViewById(R.id.wiki_debug_message_text_area);
		urlField=(EditText)this.findViewById(R.id.wiki_debug_url_text_field);
		inputField=(EditText)this.findViewById(R.id.wiki_debug_input_text_field);
		readFromPukiWikiButton=(View)this.findViewById(R.id.wiki_debug_read_button);
		sendButton=(View)this.findViewById(R.id.wiki_debug_send_button);
		updateButton=(View)this.findViewById(R.id.wiki_debug_update_button);
		editPageButton=(View)this.findViewById(R.id.wiki_debug_edit_button);
		showMessagesCheckBox=(CheckBox)this.findViewById(R.id.wiki_debug_show_message_checkbox);
		connectButton=(View)this.findViewById(R.id.wiki_debug_connect_button);
		clearButton=(View)this.findViewById(R.id.wiki_debug_clear_message_button);
//		mAuthContainer=(LinearLayout)this.findViewById(R.id.authentication_dialog);
//		mAuthContainer.setVisibility(View.GONE);
		
		this.println("y2");
//		this.initGUI();
		this.println("saveButtonDebugFrame");
//		this.setName("saveButtonFrame");
		charset="UTF-8";
		this.setListeners();
//		this.handler=new OutputMessageHandler(outputText);
//		backSpaceButton=this.findViewById(R.id.symbol_bs_button);		
	}
	
	private void setListeners(){
		sendButton.setOnClickListener(this);
		updateButton.setOnClickListener(this);
		editPageButton.setOnClickListener(this);
		readFromPukiWikiButton.setOnClickListener(this);
		clearButton.setOnClickListener(this);
	}

	public void setApplication(PukiwikiJavaApplication a){
		application=a;
	}
	public void setSetting(Properties p){
		this.setting=p;
	}
		
	private void clearButtonActionPerformed() {
//		this.println("clearButton.actionPerformed, event="+evt);
		//TODO add your code for clearButton.actionPerformed
		this.messageTextArea.setText("");
	}
	
//	HttpClient client=null;
	
//	AuthDialog authDialog=null;
//	AuthDialog authDialogBack=null;

	private void sendButtonActionPerformed() {
//		this.println("sendButton.actionPerformed, event="+evt);
		//TODO add your code for sendButton.actionPerformed
	}
	String updateText;
	String actionUrl;
	String editCmd;
	String editPage;
	String editDigest;
	String editWriteValue;
	String editEncodeHint;

	private void updateButtonActionPerformed() {
		String output=application.getOutput();
		this.sendCommand("connector replaceTextWith-", output);
	}


	String messageText;
	public void println(String x){
		Log.d(TAG,x);
		if(this.showMessagesCheckBox==null) return;
		if(this.showMessagesCheckBox.isSelected()){
			/* */
			if(this.messageTextArea==null) return;
			messageText=x;
			/* */
			 ((Activity)mActivity).runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
			      messageTextArea.append(messageText+"\n");
                 }
			 });
			/* */
//			this.messageTextArea.setCaretPosition((this.messageTextArea.getText()).length());
			 this.sendCommand("connector message-", messageText);
			/* */
			System.out.println(x);
		}
//		this.application.println(x+"\n");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
        if(this.application==null) return;
		if(v==sendButton){
			this.application.sendCommandToActivity("connector send", "");
		}
		else
		if(v==updateButton){
			updateButtonActionPerformed();
		}
		else
		if(v==editPageButton){
			this.application.sendCommandToActivity("connector edit","");
		}
		else
		if(v==readFromPukiWikiButton){
			this.application.sendCommandToActivity("connector read-","");
		}
		else
		if(v==clearButton){
			this.clearButtonActionPerformed();
		}
		else
		if(v==connectButton){
			this.application.sendCommandToActivity("connector connect-","");
		}
	}
	
	private void sendCommand(String x, String v){
		if(this.application==null)return;
		this.application.sendCommandToActivity(x, v);
	}

	@Override
	protected void onAccesssoryAttached() {
		// TODO Auto-generated method stub
		
	}
}

