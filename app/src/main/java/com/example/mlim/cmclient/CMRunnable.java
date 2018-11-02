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
        boolean bRet = m_cmClientStub.startCM();
        if(!bRet)
        {
            m_mainActivity.printMessageln("CM initialization error!");
        }
        else
        {
            m_mainActivity.printMessageln("Client CM starts.");
            m_mainActivity.printMessageln("Type \"0\" for menu.");
        }

        while(!Thread.interrupted() && !m_bInterrupted)
        {
            // check menu selected
            switch(m_strMenu)
            {
                case "terminateCM":
                    terminateCM();
                    break;
                case "connectionDS":
                    connectionDS();
                    break;
                case "disconnectionDS":
                    disconnectionDS();
                    break;
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

    private void terminateCM()
    {
        m_cmClientStub.disconnectFromServer();
        m_cmClientStub.terminateCM();
        m_bInterrupted = true;
        m_mainActivity.printMessage("Client CM terminates.\n");
    }

    private void connectionDS()
    {
        m_mainActivity.printMessage("====== connect to default server\n");
        boolean ret = m_cmClientStub.connectToServer();
        if(ret)
        {
            m_mainActivity.printMessage("Successfully connected to the default server.\n");
        }
        else
        {
            m_mainActivity.printMessage("Cannot connect to the default server!\n");
        }
        m_mainActivity.printMessage("======\n");
    }

    private void disconnectionDS()
    {
        m_mainActivity.printMessage("====== disconnect from default server\n");
        boolean ret = m_cmClientStub.disconnectFromServer();
        if(ret)
        {
            m_mainActivity.printMessage("Successfully disconnected from the default server.\n");
        }
        else
        {
            m_mainActivity.printMessage("Error while disconnecting from the default server!");
        }
        m_mainActivity.printMessage("======\n");
    }

}
