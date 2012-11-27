package org.yamalab.android.AdkWikiConnector;

import org.yamalab.android.AdkWikiConnector.R;
import org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.connector.StringMsg;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SubscriptSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class OutputPinController extends AccessoryController 
implements OnCheckedChangeListener, OnClickListener, OnTouchListener, OnSeekBarChangeListener
{
	static final String TAG = "OutputPinController";
	private int mLEDNumber;

	private AdkWikiActivity mActivity;
	private RadioButton mRadioAD;
	private RadioButton mRadioAA;
	private Button mD1;
	private Button mD0;
	private SeekBar mBar;
	private TextView mAnalogVal;
	private int myNumber;

	class LabelClickListener implements OnClickListener {

		public void onClick(View v) {
			
		}

	}

	public OutputPinController(AdkWikiActivity activity, int number,
			int avradio0, int avradio1, int dv_on, int dv_off, int abar, int av){
		super(activity);
		mActivity=activity;
		mRadioAD=(RadioButton)findViewById(avradio0);
		mRadioAA=(RadioButton)findViewById(avradio1);
		mD1=(Button)findViewById(dv_on);
		mD0=(Button)findViewById(dv_off);
		mBar=(SeekBar)findViewById(abar);
        mAnalogVal=(TextView)findViewById(av);		
        mBar.setMax(255);
        mBar.setClickable(true);
        myNumber=number;
		
        mRadioAD.setOnCheckedChangeListener(this);
//        mRadioG.setOnCheckedChangeListener(this);
        mD1.setOnClickListener(this);
        mD0.setOnClickListener(this);
        mBar.setOnSeekBarChangeListener(this);
        this.setDigital(0);
        this.setAnalog(0);
        this.mRadioAD.setSelected(true);
	}

	public void attachToView(ViewGroup targetView) {

	}

	@Override
	protected void onAccesssoryAttached() {
		// TODO Auto-generated method stub
	}
	int analogValue;
	public void setAnalog(int v){
		Log.d(TAG,"setAnalog("+v+"), id="+this.myNumber);
		analogValue=v;
        (mActivity).runOnUiThread(new Runnable() {
        	@Override
        	public void run(){
		      mRadioAD.setSelected(false);
		      mRadioAA.setChecked(true);
		      mBar.setProgress(analogValue);
		      mAnalogVal.setText(""+analogValue);
        	}
        });
	}
	int digitalValue;
	public void setDigital(int v){
		Log.d(TAG,"setDigital("+v+"), id="+this.myNumber);
		digitalValue=v;
		(mActivity).runOnUiThread(new Runnable(){
			@Override
            public void run(){
		      mRadioAD.setSelected(true);
		      mRadioAD.setChecked(true);
		      if(digitalValue==1){
	    	     mD1.setSelected(true);
	    	     mD1.setBackgroundColor(0xff10f0f0);
	    	     mD0.setSelected(false);
	    	     mD0.setBackgroundColor(0xfff0f0f0);
		      }
		      else {
			     mD1.setSelected(false);
			     mD1.setBackgroundColor(0xfff0f0f0);
			     mD0.setSelected(true);
			     mD0.setBackgroundColor(0xfff01010);
		      }
			}
		});
	}
/* */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		int id=buttonView.getId();
		Log.d(TAG,"onCheckChanged-"+this.myNumber+" id="+id);
		if(mRadioAD.isChecked()){
			mRadioAD.setSelected(true);
			if(mD1.isSelected()){
				this.setDigital(1);
				this.sendCommand("d", 1);
			}
			else
			if(mD0.isSelected()){
				this.setDigital(0);
				this.sendCommand("d", 0);					
			}
		}
		else
		if(mRadioAA.isChecked()){
			mRadioAD.setSelected(false);
			int v=mBar.getProgress();
			this.mAnalogVal.setText(""+v);
			this.sendCommand("a", v);			
		}
	}
/* */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		int id=v.getId();
		Log.d(TAG,"onTouch-"+this.myNumber+" id="+id);
		if(id==mBar.getId()){
			Log.d(TAG,"onTouch-2");
			int a=mBar.getProgress();
			this.setAnalog(a);
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id=v.getId();
		Log.d(TAG,"onClick-"+this.myNumber+" id="+id);
		if(id==mD1.getId()){
//			Log.d(TAG,"onClick-mD1");
			if(mRadioAD.isSelected()){
//				Log.d(TAG,"onClick-mD1-mRadioAD");
				if(this.digitalValue==0){
					this.setDigital(1);
					this.sendCommand("d", 1);
				}
				else {
					this.setDigital(0);
					this.sendCommand("d", 0);					
				}
			}
			
		}
		else
		if(id==mD0.getId()){
			if(mRadioAD.isSelected()){
//				Log.d(TAG,"onClick-mD0-mRadioAD");
				if(this.digitalValue==1){
					this.setDigital(0);
					this.sendCommand("d", 0);
				}
				else {
					this.setDigital(1);
					this.sendCommand("d", 1);					
				}
			}
			
		}
		else
		if(id==mBar.getId()){
//			Log.d(TAG,"onClick-mBar");
			if(!mRadioAD.isSelected()){
//				Log.d(TAG,"onClick-mBar-!mRadioAD");
					this.setAnalog(mBar.getProgress());
					this.sendCommand("a", mBar.getProgress());
			}
		}
		else
		if(id==mRadioAA.getId()){
//			Log.d(TAG,"onClick-mRadioAA");
			mRadioAD.setSelected(false);
		}
		else
		if(id==mRadioAD.getId()){
//			Log.d(TAG,"onClick-mRadioAD");
			mRadioAD.setSelected(true);
		}
	}
	private void sendCommand(String ad, int v){
	   mActivity.sendCommandToService("adk command: set xout-"+ad+"-"+myNumber+"="+v, "");
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		int id=seekBar.getId();
		Log.d(TAG,"onProgressChanged-"+this.myNumber+" id="+id);
		// TODO Auto-generated method stub
		/* */
		if(id==mBar.getId()){
//			Log.d(TAG,"onProgressChanged-2");
			if(!mRadioAD.isSelected()){
//				Log.d(TAG,"onProgressChanged-3");
				int v=seekBar.getProgress();
				this.setAnalog(v);
				this.sendCommand("a", v);
			}
		}		
		/* */
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		int id=seekBar.getId();
		Log.d(TAG,"onStartTrackingTouch-"+this.myNumber+" id="+id);
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		int id=seekBar.getId();
		Log.d(TAG,"onStopTrackingTouch-"+this.myNumber+" id="+id);
		if(id==mBar.getId()){
//			Log.d(TAG,"onStopTrackingTouch-mBar");
			if(!mRadioAD.isSelected()){
//				Log.d(TAG,"onStopTrackingTouch-mRadioAD");
				int v=seekBar.getProgress();
				this.setAnalog(v);
				this.sendCommand("a", v);
			}
		}
	}

}
