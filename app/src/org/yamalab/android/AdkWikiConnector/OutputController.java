package org.yamalab.android.AdkWikiConnector;

import java.util.StringTokenizer;

import org.yamalab.android.AdkWikiConnector.R;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class OutputController extends AccessoryController {
	static final String TAG = "OutputController";

	private OutputPinController[] outputs=new OutputPinController[14];
	
	OutputController(AdkWikiActivity hostActivity, boolean vertical) {
		super(hostActivity);
		this.initialize();
	}

	protected void initialize() {
		/* */
		setupOutputPin(8, R.id.radio80, R.id.radio81, R.id.pin8dv_on, R.id.pin8dv_off, R.id.pin8bar, R.id.pin8av);
		setupOutputPin(9, R.id.radio90, R.id.radio91, R.id.pin9dv_on, R.id.pin9dv_off, R.id.pin9bar, R.id.pin9av);
		setupOutputPin(10, R.id.radio100, R.id.radio101, R.id.pin10dv_on,R.id.pin10dv_off, R.id.pin10bar, R.id.pin10av);
		setupOutputPin(11, R.id.radio110, R.id.radio111, R.id.pin11dv_on,R.id.pin11dv_off, R.id.pin11bar, R.id.pin11av);
		setupOutputPin(12, R.id.radio120, R.id.radio121, R.id.pin12dv_on,R.id.pin12dv_off, R.id.pin12bar, R.id.pin12av);
		setupOutputPin(13, R.id.radio130, R.id.radio131, R.id.pin13dv_on,R.id.pin13dv_off, R.id.pin13bar, R.id.pin13av);
		/* */
	}

	private void setupOutputPin(int index, int adradio0, int adradio1, int dv_on, int dv_off, int abar, int av) {
		OutputPinController outputPC = new OutputPinController(mHostActivity, index, 
				adradio0, adradio1, dv_on, dv_off, abar,av);
		this.outputs[index]=outputPC;
	}
	public void setValue(String x){
		StringTokenizer st=new StringTokenizer(x);
//		String dmy=st.nextToken(); //dmy==device
		String ad=st.nextToken();
		String sport=st.nextToken();
		String sv=st.nextToken();
		Log.d(TAG,"setValue("+x+")...ad="+ad+" port="+sport+" sv="+sv);
		int port=(new Integer(sport)).intValue();
		int v=(new Integer(sv)).intValue();
		if(ad.equals("a")){
			outputs[port].setAnalog(v);
		}
		else
		if(ad.equals("d")){
			outputs[port].setDigital(v);
		}
	}

	@Override
	protected void onAccesssoryAttached() {
		// TODO Auto-generated method stub
		
	}

}
