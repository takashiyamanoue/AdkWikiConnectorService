package org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.language;


public interface GuiWithControlFlag
{
    void resetStopFlag();

    boolean stopFlagIsOn();
    boolean traceFlagIsOn();

}
