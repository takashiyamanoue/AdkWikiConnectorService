package org.yamalab.android.AdkWikiConnector;

import android.graphics.drawable.Drawable;

public class Utilities {
	static void centerAround(int x, int y, Drawable d) {
		int w = d.getIntrinsicWidth();
		int h = d.getIntrinsicHeight();
		int left = x - w / 2;
		int top = y - h / 2;
		int right = left + w;
		int bottom = top + h;
		d.setBounds(left, top, right, bottom);
	}
	static char[] i2c={'0','1','2','3','4','5','6','7',
			    '8','9','a','b','c','d','e','f'};
	static String b2h(byte x){
		int h1=(x >> 4)& 0x0f;
		int h2=x & 0x0f;
		return "0x0"+i2c[h1]+i2c[h2];
	}
	static String i2h(int x){
		String hex="";
		char c=i2c[x&0x0f];
		hex=""+c+hex;
		x=x>>4;
		for(int i=0;i<7;i++) {   // 4bit * 8 = 32 bit
			c=i2c[x&0x0f];
			hex=""+c+hex;
			x=x>>4;
		}
		return "0x0"+hex;
	}
	static boolean isEOL(String x){
		if(x==null) return true;
		if(x.equals("")) return true;
		if(x.charAt(0)=='\0'||
		   x.charAt(0)=='\n'||
		   x.charAt(0)=='\r') return true;
		return false;
	}

}
