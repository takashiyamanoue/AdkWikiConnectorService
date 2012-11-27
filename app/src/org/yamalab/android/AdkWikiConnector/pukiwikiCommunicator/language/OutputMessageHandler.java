package org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.language;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;
public class OutputMessageHandler extends Handler
{
	EditText text;
	public OutputMessageHandler(EditText txt){
		this.text=txt;
	}
	@Override
	public void handleMessage(Message msg){
		String x=(String)msg.obj;
		this.text.append(x);
		String all=this.text.getText().toString();
		int last=all.length();
		this.text.setSelection(last);
	}
}
