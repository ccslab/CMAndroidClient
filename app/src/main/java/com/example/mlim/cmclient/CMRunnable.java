package com.example.mlim.cmclient;

import android.util.Log;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMRunnable implements Runnable {

    private CMClientStub m_cmClientStub;
    private MainActivity m_mainActivity;
    private boolean m_bInterrupted;
    private Object m_syncObject;

    private String m_strMenu;

    CMRunnable(CMClientStub stub, MainActivity activity)
    {
        m_cmClientStub = stub;
        m_mainActivity = activity;
        m_bInterrupted = false;
        m_syncObject = new Object();
        m_strMenu = "";
    }

    public Object getSyncObject()
    {
        return m_syncObject;
    }

    public void setMenu(String strMenu)
    {
        m_strMenu = strMenu;
    }

    public void run()
    {

        while(!Thread.interrupted() && !m_bInterrupted)
        {
            // check menu selected
            switch(m_strMenu)
            {
                default:
                    Log.e("CMRunnable : run()", "menu ("+m_strMenu+") not defined!");
                    break;
            }

            synchronized(m_syncObject) {
                try {
                    Log.d("CMRunnable : run()", "waiting to be notified..");
                    m_syncObject.wait();
                }catch(InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }



}
