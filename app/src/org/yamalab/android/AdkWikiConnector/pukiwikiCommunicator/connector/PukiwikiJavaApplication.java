package org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.connector;

public interface PukiwikiJavaApplication {
	public String getOutput();
	public void setInput(String x);
	public void setSaveButtonDebugFrame(SaveButtonDebugFrame f);
	public void sendCommandToActivity(String c, String v);
}
