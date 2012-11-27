package org.yamalab.android.AdkWikiConnector;

import java.util.Calendar;
import java.util.Date;

public class ProcessAnalogData{
int samplingTerm=100; // mili sec.
int analysisTerm=1000*60; // one min.
int[]  data=new int[analysisTerm/samplingTerm];
long reportTerm=1000*60*30;  // 30min.
int dataP=0;
String linex;
String deviceName;
AdkService service;
 // . default=0 ... max frequency depend on the AdkThread run loop.
public ProcessAnalogData(String name, AdkService s){
	this.deviceName=name;
	this.service=s;
//	this.frequency=0;
}
public void setParams(int st, int at){
	samplingTerm=st;
	analysisTerm=at;
	data=new int[analysisTerm/samplingTerm];
//	this.frequency=0;
}
public void setSamplingTerm(int x){
//	this.frequency=x;
}
long lastTime=0;
public void process(int inputAnalog){
	long currentTime=(new Date()).getTime();
	if(samplingTerm>100){
        if(lastTime+samplingTerm>currentTime){
        	return;
        }
	}
    lastTime=currentTime;
	if(dataP>=data.length){
		dataP=0;
		int size=data.length;
		int max=0;
		int min=2000;
		int ave=0;
		int sdv=0;
		int sum=0;
		for(int i=0;i<size;i++){
			if(data[i]>max) max=data[i];
			if(data[i]<min) min=data[i];
			sum=sum+data[i];
		}
	    int motionFrequency1=0;
	    int motionDetectValue1=40; // if pir exceeds this, count up the motion frequency
	    int motionFrequency2=0;
	    int motionDetectValue2=80; // if pir exceeds this, count up the motion frequency
	    int motionFrequency3=0;
	    int motionDetectValue3=160; // if pir exceeds this, count up the motion frequency
		ave=sum/size;
		double dsum=0;
		for(int i=0;i<size;i++){
			double d=0;
			d=data[i]-ave;
			dsum=dsum+d*d;
			if(d>motionDetectValue3){
				motionFrequency3++;
			}
			else
			if(d>motionDetectValue2){
				motionFrequency2++;
			}
			else
			if(d>motionDetectValue1){
				motionFrequency1++;
			}
		}
		double std=Math.sqrt(dsum/(size-1));
		final Calendar calendar = Calendar.getInstance();
		final int year = calendar.get(Calendar.YEAR);
		final int month = calendar.get(Calendar.MONTH);
		final int day = calendar.get(Calendar.DAY_OF_MONTH);
		final int hour = calendar.get(Calendar.HOUR_OF_DAY);
		final int minute = calendar.get(Calendar.MINUTE);
		final int second = calendar.get(Calendar.SECOND);
		linex="device="+deviceName+", Date="+    
				   year + "/" + (month + 1) + "/" + day + "/" + " " +
		           hour + ":" + minute + ":" + second +
		           ",  ave="+ave+", sdv="+std+", max="+max+", min="+min+
		           ", f1="+motionFrequency1+
		           ", f2="+motionFrequency2+
		           ", f3="+motionFrequency3+
		           ", n="+data.length+
		           ", dt="+samplingTerm+
		           ".\n";	
		if(service!=null){
			service.putSendBuffer(linex);
		}

	}
	data[dataP]=inputAnalog;
	dataP++;

}
}
