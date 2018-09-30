package com.example.mlim.cmclient;

import android.app.DialogFragment;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;
import kr.ac.konkuk.ccslab.cm.util.CMUtil;

public class CMRunnable implements Runnable {

    private CMClientStub m_cmClientStub;
    private CMClientEventHandler m_cmEventHandler;
    private MainActivity m_mainActivity;
    private boolean m_bInterrupted;
    private Object m_syncObject;
    private DialogFragment m_dialog;

    private String m_strMenu;

    CMRunnable(MainActivity activity)
    {
        m_cmClientStub = null;
        m_cmEventHandler = null;
        m_mainActivity = activity;
        m_bInterrupted = false;
        m_syncObject = new Object();
        m_dialog = null;
        m_strMenu = "";
    }

    // create and init CM
    public boolean initCM()
    {
        // create CM client stub object
        m_cmClientStub = new CMClientStub();
        // create and set an event handler to CM
        m_cmEventHandler = new CMClientEventHandler(m_cmClientStub, m_mainActivity);
        m_cmClientStub.setEventHandler(m_cmEventHandler);

        // get the path of internal storage
        String strInterPath = m_mainActivity.getFilesDir().getAbsolutePath();
        Path interPath = Paths.get(strInterPath);
        Path confPath = interPath.resolve("cm-client.conf");

        // if the cm-client.conf file does not exists in the internal storage,
        if(!Files.exists(confPath))
        {
            // get cm-client.conf in the asset folder
            InputStream is = null;
            try {
                is = m_mainActivity.getAssets().open("cm-client.conf");
            } catch(IOException e) {
                Log.e("MainActivity:initCM()", e.getMessage());
                m_mainActivity.printMessage("Open configuration file error!");
                return false;
            }

            try{
                // copy cm-client.conf from the asset folder to the internal storage
                Files.copy(is, confPath, StandardCopyOption.REPLACE_EXISTING);
            } catch(IOException e) {
                Log.e("MainActivity:initCM()", e.getMessage());
                m_mainActivity.printMessage("Copy configuration file error!");
                return false;
            }
        }

        // set the home directory of the configuration file to CM
        m_cmClientStub.setConfigurationHome(interPath);
        Log.d("MainActivity:initCM()", "conf file path: "+confPath.toString());

        // set the directory for transferred files
        if(m_mainActivity.isExternalStorageWritable())
        {
            File externFile = m_mainActivity.getExternalFilesDir(null);
            Path externPath = Paths.get(externFile.getPath());
            m_cmClientStub.setTransferedFileHome(externPath);
            Log.d("MainActivity:initCM()", "transfered file path: "+ CMConfigurator.getConfiguration(confPath.toString(), "FILE_PATH"));
        }
        else
        {
            Log.e("MainActivity:initCM()", "External storage is not available!");
            m_mainActivity.printMessage("External storage is not available!");
            return false;
        }

        return true;
    }

    public void checkUpdateServerInfo()
    {
        // get current server information to string/server_address and string/server_port
        String strCurServerAddress = null;
        int nCurServerPort = -1;
        strCurServerAddress = m_cmClientStub.getServerAddress();
        nCurServerPort = m_cmClientStub.getServerPort();
        Log.d("MainActivity : checkUpdateServerInfo()", "server info("+strCurServerAddress+", "+nCurServerPort+").");

        m_mainActivity.showServerInfoDialog(strCurServerAddress, nCurServerPort);
    }

    public CMClientStub getClientStub()
    {
        return m_cmClientStub;
    }

    public Object getSyncObject()
    {
        return m_syncObject;
    }

    public void setMenu(String strMenu)
    {
        m_strMenu = strMenu;
    }

    public void setDialog(DialogFragment dialog)
    {
        m_dialog = dialog;
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
                case "loginDS":
                    loginDS();
                    break;
                case "logoutDS":
                    logoutDS();
                    break;
                case "requestSessionInfoDS":
                    requestSessionInfoDS();
                    break;
                case "joinSession":
                    joinSession();
                    break;
                case "leaveSession":
                    leaveSession();
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

    private void loginDS()
    {
        if(m_dialog == null)
        {
            m_mainActivity.printMessageln("Login dialog unavailable!");
            return;
        }

        EditText idEditText = m_dialog.getView().findViewById(R.id.loginDSIDEditText);
        EditText passwdEditText = m_dialog.getView().findViewById(R.id.loginDSPasswdEditText);
        String strID = idEditText.getText().toString().trim();
        String strPasswd = passwdEditText.getText().toString();
        String strEncPasswd = CMUtil.getSHA1Hash(strPasswd);

        boolean bRequestResult = m_cmClientStub.loginCM(strID, strEncPasswd);
        if(bRequestResult)
        {
            m_mainActivity.printMessage("successfully sent the login request.\n");
        }
        else
        {
            m_mainActivity.printMessage("failed the login request!\n");
        }

    }

    private void logoutDS()
    {
        boolean bRequestResult = false;
        m_mainActivity.printMessage("====== logout from default server\n");
        bRequestResult = m_cmClientStub.logoutCM();
        if(bRequestResult) {
            m_mainActivity.printMessage("successfully sent the logout request.\n");
            m_mainActivity.toastMessage("로그아웃 요청!", Toast.LENGTH_LONG);
        }
        else {
            m_mainActivity.printMessage("failed the logout request!\n");
        }
        m_mainActivity.printMessage("======\n");

    }

    private void requestSessionInfoDS()
    {
        boolean bRequestResult = false;
        m_mainActivity.printMessage("====== request session info from default server\n");
        bRequestResult = m_cmClientStub.requestSessionInfo();
        if(bRequestResult)
        {
            m_mainActivity.printMessage("successfully sent the session-info request.\n");
        }
        else
        {
            m_mainActivity.printMessage("failed the session-info request!\n");
        }
        m_mainActivity.printMessage("======\n");

    }

    private void joinSession()
    {
        m_mainActivity.printMessage("====== join a session\n");
        if(m_dialog == null)
        {
            m_mainActivity.printMessageln("Join-session dialog unavailable!");
            return;
        }

        EditText sessionNameEditText = m_dialog.getView().findViewById(R.id.joinSessionNameEditText);
        String strSessionName = sessionNameEditText.getText().toString().trim();
        boolean bRequestResult = m_cmClientStub.joinSession(strSessionName);

        if(bRequestResult)
        {
            m_mainActivity.printMessage("successfully sent the session-join request.\n");
        }
        else
        {
            m_mainActivity.printMessage("failed the session-join request!\n");
        }

    }

    private void leaveSession()
    {
        boolean bRequestResult = false;
        m_mainActivity.printMessage("====== leave the current session\n");
        bRequestResult = m_cmClientStub.leaveSession();
        if(bRequestResult)
            m_mainActivity.printMessage("successfully sent the leave-session request.\n");
        else
            m_mainActivity.printMessage("failed the leave-session request!\n");

    }

}
