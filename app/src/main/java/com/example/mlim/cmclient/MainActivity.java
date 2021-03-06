package com.example.mlim.cmclient;

//import android.support.v4.app.DialogFragment;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

import kr.ac.konkuk.ccslab.cm.entity.CMGroup;
import kr.ac.konkuk.ccslab.cm.entity.CMGroupInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class MainActivity extends AppCompatActivity
{

    private CMClientStub m_cmClientStub;
    private CMClientEventHandler m_cmEventHandler;
    private String m_strReceiver;

    private Dialog m_serverInfoDialog;
    private Dialog m_loginDSDialog;
    private Dialog m_joinSessionDialog;
    private Dialog m_changeGroupDialog;
    private Dialog m_chatDialog;
    private Dialog m_addChannelDialog;
    private Dialog m_addSocketChannelDialog;
    private Dialog m_addDatagramChannelDialog;
    private Dialog m_addMulticastChannelDialog;
    private Dialog m_removeChannelDialog;
    private Dialog m_removeSocketChannelDialog;
    private Dialog m_removeDatagramChannelDialog;
    private Dialog m_removeMulticastChannelDialog;
    private Dialog m_requestFileDialog;
    private Dialog m_pushFileDialog;
    private Dialog m_measureInputThgoughputDialog;
    private Dialog m_measureOutputThroughputDialog;

    public static final String EXTRA_MESSAGE = "com.example.mlim.CMClient.MESSAGE";
    private static final int READ_REQUEST_CODE = 42;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize dialogs
        m_serverInfoDialog = null;
        m_loginDSDialog = null;
        m_joinSessionDialog = null;
        m_changeGroupDialog = null;
        m_chatDialog = null;
        m_addChannelDialog = null;
        m_addSocketChannelDialog = null;
        m_addDatagramChannelDialog = null;
        m_addMulticastChannelDialog = null;
        m_removeChannelDialog = null;
        m_removeSocketChannelDialog = null;
        m_removeDatagramChannelDialog = null;
        m_removeMulticastChannelDialog = null;
        m_requestFileDialog = null;
        m_pushFileDialog = null;
        m_measureInputThgoughputDialog = null;
        m_measureOutputThroughputDialog = null;

        // initialize the cmTextView
        TextView cmTextView = (TextView) findViewById(R.id.cmTextView);
        cmTextView.setMovementMethod(new ScrollingMovementMethod());
        cmTextView.setText("");

        // to handle typing the enter key at the editText view.
        EditText editText = (EditText) findViewById(R.id.editText);
        editText.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if((event.getAction()== KeyEvent.ACTION_DOWN) && keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    sendCommand(v);
                    return true;
                }
                return false;
            }
        });

        // create and init CM
        initCM();
        // check and update current server information
        checkUpdateServerInfo();

        // CM will start after the user presses the confirm button of the server-info dialog.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cm_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.menuShowAllMenus:
                printAllMenus(); return true;
            case R.id.menuStartCM:
                checkUpdateServerInfo(); return true;
            case R.id.menuTerminateCM:
                terminateCM(); return true;
            case R.id.menuConnDefServer:
                connectToDS(); return true;
            case R.id.menuDisconDefServer:
                disconnectFromDS(); return true;
            case R.id.menuLoginDefServer:
                loginDS(); return true;
            case R.id.menuSyncLoginDefServer:
                syncLoginDS(); return true;
            case R.id.menuLogoutDefServer:
                logoutDS(); return true;
            case R.id.menuReqSessionInfoDefServer:
                requestSessionInfoDS(); return true;
            case R.id.menuSyncReqSessionInfoDefServer:
                syncSessionInfoDS(); return true;
            case R.id.menuJoinSessionDefServer:
                joinSession(); return true;
            case R.id.menuSyncJoinSessionDefServer:
                syncJoinSession(); return true;
            case R.id.menuLeaveSessionDefServer:
                leaveSession(); return true;
            case R.id.menuChangeGroupDefServer:
                changeGroup(); return true;
            case R.id.menuChat:
                chat(); return true;
            case R.id.menuMulticastChat:
                printMessageln("Not supported yet in Android!");
                return true;
            case R.id.menuTestDummyEvent:
            case R.id.menuTestUserEvent:
            case R.id.menuTestDatagram:
            case R.id.menuTestUserPos:
            case R.id.menuSendRecv:
            case R.id.menuCastRecv:
            case R.id.menuAsyncSendRecv:
            case R.id.menuAsyncCastRecv:
            case R.id.menuShowGroupInfo:
            case R.id.menuShowCurUserStat:
            case R.id.menuShowCurChannels:
            case R.id.menuInputThroughput:
                measureInputThroughput(); return true;
            case R.id.menuOutputThroughput:
                measureOutputThroughput(); return true;
            case R.id.menuShowAllConfig:
            case R.id.menuChangeConfig:
                printMessageln("Not supported yet!");
                return true;
            case R.id.menuAddChannel:
                addChannel(); return true;
            case R.id.menuRemoveChannel:
                removeChannel(); return true;
            case R.id.menuTestBlockChannel:
            case R.id.menuSetFilePath:
                printMessageln("Not supported yet!");
                return true;
            case R.id.menuReqFile:
                requestFile(); return true;
            case R.id.menuPushFile:
                pushFile(); return true;
            case R.id.menuCancelRecvFile:
            case R.id.menuCancelSendFile:
            case R.id.menuReqContentList:
            case R.id.menuReqNextContentList:
            case R.id.menuReqPrevContentList:
            case R.id.menuReqAttachFile:
            case R.id.menuUploadContent:
            case R.id.menuRegNewUser:
            case R.id.menuDeregUser:
            case R.id.menuFindRegUser:
            case R.id.menuAddNewFriend:
            case R.id.menuRemoveFriend:
            case R.id.menuShowFriends:
            case R.id.menuShowFriendRequesters:
            case R.id.menuShowBiFriends:
            case R.id.menuTestForwardScheme:
            case R.id.menuTestDelayForwardScheme:
            case R.id.menuRepeatSNSRequest:
            case R.id.menuPullPushMultipleFiles:
            case R.id.menuSplitFile:
            case R.id.menuMergeFiles:
            case R.id.menuDistMergeFile:
                printMessageln("Not supported yet!");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Called when the user taps the Enter button or types the enter key */
    public void sendCommand(View view) {
        //Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String strCommand = editText.getText().toString();
        editText.setText("");

        switch(strCommand)
        {
            case "0":   // print all menus
                printAllMenus();
                break;
            case "100": // start CM
                checkUpdateServerInfo();
                break;
            case "999": // terminate CM
                terminateCM();
                break;
            case "1":   // connect to default server
                connectToDS();
                break;
            case "2":   // disconnect from default server
                disconnectFromDS();
                break;
            case "3":   // connect to designated server
                printMessageln("Not supported yet!");
                break;
            case "4":   // disconnect from designated server
                printMessageln("Not supported yet!");
                break;
            case "10":  // login to default server
                loginDS();
                break;
            case "11": // synchronously login to default server
                syncLoginDS();
                break;
            case "12": // logout from default server
                logoutDS();
                break;
            case "13": // log in to a designated server
                printMessageln("Not supported yet!");
                //loginServer();
                break;
            case "14": // log out from a designated server
                printMessageln("Not supported yet!");
                //logoutServer();
                break;
            case "20": // request session info from default server
                requestSessionInfoDS();
                break;
            case "21": // synchronously request session info from default server
                syncSessionInfoDS();
                break;
            case "22": // join a session
                joinSession();
                break;
            case "23": // synchronously join a session
                syncJoinSession();
                break;
            case "24": // leave the current session
                leaveSession();
                break;
            case "25": // change current group
                changeGroup();
                break;
            case "26": // request session information from a designated server
                printMessageln("Not supported yet!");
                //requestSessionInfoOfServer();
                break;
            case "27": // join a session of a designated server
                printMessageln("Not supported yet!");
                //joinSessionOfServer();
                break;
            case "28": // leave a session of a designated server
                printMessageln("Not supported yet!");
                //leaveSessionOfServer();
                break;
            case "40": // chat
                chat();
                break;
            case "41": // test multicast chat in current group
                printMessageln("Not supported yet!");
                //multicastChat();
                break;
            case "42": // test CMDummyEvent
                //from here
                printMessageln("Not supported yet!");
                //testDummyEvent();
                break;
            case "43": // test CMUserEvent
                printMessageln("Not supported yet!");
                //testUserEvent();
                break;
            case "44": // test datagram message
                printMessageln("Not supported yet!");
                //testDatagram();
                break;
            case "45": // user position
                printMessageln("Not supported yet!");
                //testUserPosition();
                break;
            case "46": // test sendrecv
                printMessageln("Not supported yet!");
                //testSendRecv();
                break;
            case "47": // test castrecv
                printMessageln("Not supported yet!");
                //testCastRecv();
                break;
            case "50": // print group info
                printGroupInfo();
                break;
            case "51": // print current information about the client
                printCurrentUserStatus();
                break;
            case "52": 	// print current channels information
                printCurrentChannelInfo();
                break;
            case "53": // request additional server info
                printMessageln("Not supported yet!");
                //requestServerInfo();
                break;
            case "54": // print current group info of a designated server
                printMessageln("Not supported yet!");
                //printGroupInfoOfServer();
                break;
            case "55": // test input network throughput
                measureInputThroughput();
                break;
            case "56": // test output network throughput
                measureOutputThroughput();
                break;
            case "57": // print all configurations
                printConfigurations();
                break;
            case "58": // change configuration
                printMessageln("Not supported yet!");
                //changeConfiguration();
                break;
            case "60": // add additional channel
                addChannel();
                break;
            case "61": // remove additional channel
                removeChannel();
                break;
            case "62": // test blocking channel
                printMessageln("Not supported yet!");
                //testBlockingChannel();
                break;
            case "70": // set file path
                printMessageln("Not supported yet!");
                //setFilePath();
                break;
            case "71": // request a file
                requestFile();
                break;
            case "72": // push a file
                pushFile();
                break;
            case "73":	// test cancel receiving a file
                printMessageln("Not supported yet!");
                //cancelRecvFile();
                break;
            case "74":	// test cancel sending a file
                printMessageln("Not supported yet!");
                //cancelSendFile();
                break;
            case "80": // test SNS content download
                printMessageln("Not supported yet!");
                //downloadNewSNSContent();
                break;
            case "81":
                printMessageln("Not supported yet!");
                //downloadNextSNSContent();
                break;
            case "82":
                printMessageln("Not supported yet!");
                //downloadPreviousSNSContent();
                break;
            case "83": // request an attached file of SNS content
                printMessageln("Not supported yet!");
                //requestAttachedFileOfSNSContent();
                break;
            case "84": // test SNS content upload
                printMessageln("Not supported yet!");
                //uploadSNSContent();
                break;
            case "90": // register user
                printMessageln("Not supported yet!");
                //registerUser();
                break;
            case "91": // deregister user
                printMessageln("Not supported yet!");
                //deregisterUser();
                break;
            case "92": // find user
                printMessageln("Not supported yet!");
                //findRegisteredUser();
                break;
            case "93": // add a new friend
                printMessageln("Not supported yet!");
                //addNewFriend();
                break;
            case "94": // remove a friend
                printMessageln("Not supported yet!");
                //removeFriend();
                break;
            case "95": // request current friends list
                printMessageln("Not supported yet!");
                //requestFriendsList();
                break;
            case "96": // request friend requesters list
                printMessageln("Not supported yet!");
                //requestFriendRequestersList();
                break;
            case "97": // request bi-directional friends
                printMessageln("Not supported yet!");
                //requestBiFriendsList();
                break;
            case "101": // test forwarding schemes (typical vs. internal)
                printMessageln("Not supported yet!");
                //testForwarding();
                break;
            case "102": // test delay of forwarding schemes
                printMessageln("Not supported yet!");
                //testForwardingDelay();
                break;
            case "103": // test repeated downloading of SNS content
                printMessageln("Not supported yet!");
                //testRepeatedSNSContentDownload();
                break;
            case "104": // pull or push multiple files
                printMessageln("Not supported yet!");
                //sendMultipleFiles();
                break;
            case "105": // split a file
                printMessageln("Not supported yet!");
                //splitFile();
                break;
            case "106": // merge files
                printMessageln("Not supported yet!");
                //mergeFiles();
                break;
            case "107": // distribute a file and merge
                printMessageln("Not supported yet!");
                //distFileProc();
                break;
            default:
                printMessageln("Unknown command.");
                break;
        }
    }

    // print a text message in the cmTextView
    public void printMessageln(String strMessage)
    {
        TextView cmTextView = (TextView) findViewById(R.id.cmTextView);
        cmTextView.append(strMessage+"\n");

        // find the amount we need to scroll.  This works by
        // asking the TextView's internal layout for the position
        // of the final line and then subtracting the TextView's height
        final int scrollAmount = cmTextView.getLayout().getLineTop(cmTextView.getLineCount()) - cmTextView.getHeight();
        // if there is no need to scroll, scrollAmount will be <=0
        if (scrollAmount > 0)
            cmTextView.scrollTo(0, scrollAmount);
        else
            cmTextView.scrollTo(0, 0);
    }

    public void printMessage(String strMessage)
    {
        TextView cmTextView = (TextView) findViewById(R.id.cmTextView);
        cmTextView.append(strMessage);

        // find the amount we need to scroll.  This works by
        // asking the TextView's internal layout for the position
        // of the final line and then subtracting the TextView's height
        final int scrollAmount = cmTextView.getLayout().getLineTop(cmTextView.getLineCount()) - cmTextView.getHeight();
        // if there is no need to scroll, scrollAmount will be <=0
        if (scrollAmount > 0)
            cmTextView.scrollTo(0, scrollAmount);
        else
            cmTextView.scrollTo(0, 0);

    }

    public void toastMessage(String strMessage, int nToastLength)
    {
        final String msg = strMessage;
        final int len = nToastLength;
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(getBaseContext(), msg, len).show();
            }
        });

    }

    public void setActivityTitle(String title)
    {
        final Activity activity = this;
        final String strTitle = title;
        runOnUiThread(new Runnable() {
           public void run()
           {
               activity.setTitle(strTitle);
           }
        });
    }

    // Checks if external storage is available for read and write
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    // create and init CM
    public boolean initCM()
    {
        // create CM client stub object
        m_cmClientStub = new CMClientStub();
        // create and set an event handler to CM
        m_cmEventHandler = new CMClientEventHandler(m_cmClientStub, this);
        m_cmClientStub.setEventHandler(m_cmEventHandler);

        // get the path of internal storage
        String strInterPath = getFilesDir().getAbsolutePath();
        Path interPath = Paths.get(strInterPath);
        Path confPath = interPath.resolve("cm-client.conf");

        // if the cm-client.conf file does not exists in the internal storage,
        if(!Files.exists(confPath))
        {
            // get cm-client.conf in the asset folder
            InputStream is = null;
            try {
                is = getAssets().open("cm-client.conf");
            } catch(IOException e) {
                Log.e("MainActivity:initCM()", e.getMessage());
                printMessage("Open configuration file error!");
                return false;
            }

            try{
                // copy cm-client.conf from the asset folder to the internal storage
                Files.copy(is, confPath, StandardCopyOption.REPLACE_EXISTING);
            } catch(IOException e) {
                Log.e("MainActivity:initCM()", e.getMessage());
                printMessage("Copy configuration file error!");
                return false;
            }
        }

        // set the home directory of the configuration file to CM
        m_cmClientStub.setConfigurationHome(interPath);
        Log.d("MainActivity:initCM()", "conf file path: "+confPath.toString());

        // set the directory for transferred files
        if(isExternalStorageWritable())
        {
            File externFile = getExternalFilesDir(null);
            Path externPath = Paths.get(externFile.getPath());
            m_cmClientStub.setTransferedFileHome(externPath);
            Log.d("MainActivity:initCM()", "transfered file path: "+ CMConfigurator.getConfiguration(confPath.toString(), "FILE_PATH"));
        }
        else
        {
            Log.e("MainActivity:initCM()", "External storage is not available!");
            printMessage("External storage is not available!");
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

        showServerInfoDialog(strCurServerAddress, nCurServerPort);
    }

    public void showServerInfoDialog(String strAddress, int nPort)
    {
        m_serverInfoDialog = new Dialog(this);
        m_serverInfoDialog.setContentView(R.layout.dialog_server_info);
        m_serverInfoDialog.setTitle(R.string.server_info_title);

        EditText addressEditText = m_serverInfoDialog.findViewById(R.id.serverAddressEditText);
        EditText portEditText = m_serverInfoDialog.findViewById(R.id.serverPortEditText);
        addressEditText.setText(strAddress);
        portEditText.setText(String.valueOf(nPort));

        m_serverInfoDialog.show();
    }

    public void onConfirmServerInfo(View v)
    {
        EditText addressEditText = m_serverInfoDialog.findViewById(R.id.serverAddressEditText);
        EditText portEditText = m_serverInfoDialog.findViewById(R.id.serverPortEditText);
        String strAddress = addressEditText.getText().toString().trim();
        int nPort = Integer.parseInt(portEditText.getText().toString().trim());
        Log.d("MainActivity : onServerInfoDialogConfirmClick()", "server input("+strAddress+", "+nPort+").");

        m_cmClientStub.setServerAddress(strAddress);
        m_cmClientStub.setServerPort(nPort);

        // CM must start in a separate thread because of the Android policy !!
        // CM is updated to support this requirement.
        boolean bRet = m_cmClientStub.startCM();
        if(!bRet)
        {
            printMessageln("CM initialization error!");
        }
        else
        {
            printMessageln("Client CM starts.");
            printMessageln("Type \"0\" for menu.");
        }

        m_serverInfoDialog.dismiss();
    }

    public void onCancelServerInfo(View v)
    {
        printMessageln("cancel to start CM");
        m_serverInfoDialog.dismiss();
    }

    private void printAllMenus()
    {
        printMessage("---------------------------------- Help\n");
        printMessage("0: show all menus\n");
        printMessage("---------------------------------- Start/Stop\n");
        printMessage("100: start CM, 999: terminate CM\n");
        printMessage("---------------------------------- Connection\n");
        printMessage("1: connect to default server, 2: disconnect from default server\n");
        printMessage("3: connect to designated server, 4: disconnect from designated server\n");
        printMessage("---------------------------------- Login\n");
        printMessage("10: login to default server, 11: synchronously login to default server\n");
        printMessage("12: logout from default server\n");
        printMessage("13: login to designated server, 14: logout from designated server\n");
        printMessage("---------------------------------- Session/Group\n");
        printMessage("20: request session information from default server\n");
        printMessage("21: synchronously request session information from default server\n");
        printMessage("22: join session of default server, 23: synchronously join session of default server\n");
        printMessage("24: leave session of default server, 25: change group of default server\n");
        printMessage("26: request session information from designated server\n");
        printMessage("27: join session of designated server, 28: leave session of designated server\n");
        printMessage("---------------------------------- Event Transmission\n");
        printMessage("40: chat, 41: multicast chat in current group\n");
        printMessage("42: test CMDummyEvent, 43: test CMUserEvent, 44: test datagram event, 45: test user position\n");
        printMessage("46: test sendrecv, 47: test castrecv\n");
        printMessage("---------------------------------- Information\n");
        printMessage("50: show group information of default server, 51: show current user status\n");
        printMessage("52: show current channels, 53: show current server information\n");
        printMessage("54: show group information of designated server\n");
        printMessage("55: measure input network throughput, 56: measure output network throughput\n");
        printMessage("57: show all configurations, 58: change configuration\n");
        printMessage("---------------------------------- Channel\n");
        printMessage("60: add channel, 61: remove channel, 62: test blocking channel\n");
        printMessage("---------------------------------- File Transfer\n");
        printMessage("70: set file path, 71: request file, 72: push file\n");
        printMessage("73: cancel receiving file, 74: cancel sending file\n");
        printMessage("---------------------------------- Social Network Service\n");
        printMessage("80: request content list, 81: request next content list, 82: request previous content list\n");
        printMessage("83: request attached file, 84: upload content\n");
        printMessage("---------------------------------- User\n");
        printMessage("90: register new user, 91: deregister user, 92: find registered user\n");
        printMessage("93: add new friend, 94: remove friend, 95: show friends, 96: show friend requesters\n");
        printMessage("97: show bi-directional friends\n");
        printMessage("---------------------------------- Other CM Tests\n");
        printMessage("101: test forwarding scheme, 102: test delay of forwarding scheme\n");
        printMessage("103: test repeated request of SNS content list\n");
        printMessage("104: pull/push multiple files, 105: split file, 106: merge files, 107: distribute and merge file\n");

    }

    ////////// terminate CM
    private void terminateCM()
    {
        m_cmClientStub.disconnectFromServer();
        m_cmClientStub.terminateCM();
        printMessage("Client CM terminates.\n");
    }
    //////////

    ////////// connect to default server
    private void connectToDS()
    {
        printMessage("====== connect to default server\n");
        boolean ret = m_cmClientStub.connectToServer();
        if(ret)
        {
            printMessage("Successfully connected to the default server.\n");
        }
        else
        {
            printMessage("Cannot connect to the default server!\n");
        }
        printMessage("======\n");
    }
    //////////

    ////////// disconnect from default server
    private void disconnectFromDS()
    {
        printMessage("====== disconnect from default server\n");
        boolean ret = m_cmClientStub.disconnectFromServer();
        if(ret)
        {
            printMessage("Successfully disconnected from the default server.\n");
        }
        else
        {
            printMessage("Error while disconnecting from the default server!");
        }
        printMessage("======\n");
        setActivityTitle(getString(R.string.app_name));
    }
    //////////

    ////////// login
    private void loginDS()
    {
        printMessage("====== login to default server\n");
        m_loginDSDialog = new Dialog(this);
        m_loginDSDialog.setContentView(R.layout.dialog_login_ds);
        m_loginDSDialog.setTitle(R.string.login_ds_title);

        m_loginDSDialog.show();
    }

    public void onConfirmLogin(View v)
    {
        EditText idEditText = m_loginDSDialog.findViewById(R.id.loginDSIDEditText);
        EditText passwdEditText = m_loginDSDialog.findViewById(R.id.loginDSPasswdEditText);
        String strID = idEditText.getText().toString().trim();
        String strPasswd = passwdEditText.getText().toString();

        boolean bRequestResult = m_cmClientStub.loginCM(strID, strPasswd);
        if(bRequestResult)
        {
            printMessage("successfully sent the login request.\n");
        }
        else
        {
            printMessage("failed the login request!\n");
        }

        m_loginDSDialog.dismiss();
    }

    public void onCancelLogin(View v)
    {
        printMessage("login canceled.\n");
        m_loginDSDialog.dismiss();
    }

    private void syncLoginDS()
    {
        printMessage("====== synchronously login to default server\n");
        m_loginDSDialog = new Dialog(this);
        m_loginDSDialog.setContentView(R.layout.dialog_login_ds);
        m_loginDSDialog.setTitle(R.string.login_ds_title);

        Button loginButton = (Button) m_loginDSDialog.findViewById(R.id.buttonLogin);
        Button cancelButton = (Button) m_loginDSDialog.findViewById(R.id.buttonCancelLogin);

        loginButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                confirmSyncLoginDS();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                cancelSyncLoginDS();
            }
        });

        m_loginDSDialog.show();
    }

    private void confirmSyncLoginDS()
    {
        EditText idEditText = m_loginDSDialog.findViewById(R.id.loginDSIDEditText);
        EditText passwdEditText = m_loginDSDialog.findViewById(R.id.loginDSPasswdEditText);
        String strUserName = idEditText.getText().toString().trim();
        String strPasswd = passwdEditText.getText().toString();
        CMSessionEvent loginAckEvent = null;

        m_cmEventHandler.setStartTime(System.currentTimeMillis());
        loginAckEvent = m_cmClientStub.syncLoginCM(strUserName, strPasswd);
        long lDelay = System.currentTimeMillis() - m_cmEventHandler.getStartTime();
        if(loginAckEvent != null)
        {
            // print login result
            if(loginAckEvent.isValidUser() == 0)
            {
                printMessage("This client fails authentication by the default server!\n");
                toastMessage("사용자 인증 실패!", Toast.LENGTH_SHORT);
            }
            else if(loginAckEvent.isValidUser() == -1)
            {
                printMessage("This client is already in the login-user list!\n");
                toastMessage("이미 로그인중!", Toast.LENGTH_SHORT);
            }
            else
            {
                printMessage("return delay: "+lDelay+" ms.\n");
                printMessage("This client successfully logs in to the default server.\n");
                toastMessage("로그인 성공!", Toast.LENGTH_SHORT);

                CMInteractionInfo interInfo = m_cmClientStub.getCMInfo().getInteractionInfo();

                // Change the title of the client window
                setActivityTitle(getString(R.string.app_name)+" ["+interInfo.getMyself().getName()+"]");
            }
        }
        else
        {
            printMessage("failed the login request!\n");
        }

        m_loginDSDialog.dismiss();
    }

    private void cancelSyncLoginDS()
    {
        printMessage("syncLogin canceled!\n");
        m_loginDSDialog.dismiss();
    }

    //////////

    ////////// logout
    private void logoutDS()
    {
        boolean bRequestResult = false;
        printMessage("====== logout from default server\n");
        bRequestResult = m_cmClientStub.logoutCM();
        if(bRequestResult) {
            printMessage("successfully sent the logout request.\n");
            toastMessage("로그아웃 요청!", Toast.LENGTH_LONG);
        }
        else {
            printMessage("failed the logout request!\n");
        }
        setActivityTitle(getString(R.string.app_name));

    }
    //////////

    ////////// request session info
    private void requestSessionInfoDS()
    {
        boolean bRequestResult = false;
        printMessage("====== request session info from default server\n");
        m_cmEventHandler.setStartTime(System.currentTimeMillis());
        bRequestResult = m_cmClientStub.requestSessionInfo();
        if(bRequestResult)
        {
            printMessage("successfully sent the session-info request.\n");
        }
        else
        {
            printMessage("failed the session-info request!\n");
        }

    }

    private void syncSessionInfoDS()
    {
        CMSessionEvent se = null;
        printMessage("====== synchronous request session info from default server\n");
        m_cmEventHandler.setStartTime(System.currentTimeMillis());
        se = m_cmClientStub.syncRequestSessionInfo();
        long lDelay = System.currentTimeMillis() - m_cmEventHandler.getStartTime();
        if(se == null)
        {
            printMessage("failed the session-info request!\n");
            return;
        }

        printMessage("return delay: "+ lDelay +" ms.\n");

        // print the request result
        Iterator<CMSessionInfo> iter = se.getSessionInfoList().iterator();

        printMessage(String.format("%-60s%n", "------------------------------------------------------------"));
        printMessage(String.format("%-20s%-20s%-10s%-10s%n", "name", "address", "port", "user num"));
        printMessage(String.format("%-60s%n", "------------------------------------------------------------"));

        while(iter.hasNext())
        {
            CMSessionInfo tInfo = iter.next();
            printMessage(String.format("%-20s%-20s%-10d%-10d%n", tInfo.getSessionName(), tInfo.getAddress(),
                    tInfo.getPort(), tInfo.getUserNum()));
        }

    }
    //////////

    ////////// join session
    private void joinSession()
    {
        printMessage("====== join a session\n");
        m_joinSessionDialog = new Dialog(this);
        m_joinSessionDialog.setContentView(R.layout.dialog_join_session);
        m_joinSessionDialog.setTitle(R.string.join_session_title);

        m_joinSessionDialog.show();
    }

    public void onConfirmJoinSession(View v)
    {
        EditText sessionNameEditText = m_joinSessionDialog.findViewById(R.id.joinSessionNameEditText);
        String strSessionName = sessionNameEditText.getText().toString().trim();
        boolean bRequestResult = m_cmClientStub.joinSession(strSessionName);

        if(bRequestResult)
        {
            printMessage("successfully sent the session-join request.\n");
        }
        else
        {
            printMessage("failed the session-join request!\n");
        }

        m_joinSessionDialog.dismiss();
    }

    public void onCancelJoinSession(View v)
    {
        printMessage("join-session canceled!\n");
        m_joinSessionDialog.dismiss();
    }

    private void syncJoinSession()
    {
        printMessage("====== synchronously join a session\n");
        m_joinSessionDialog = new Dialog(this);
        m_joinSessionDialog.setContentView(R.layout.dialog_join_session);
        m_joinSessionDialog.setTitle(R.string.join_session_title);

        Button buttonConfirm = m_joinSessionDialog.findViewById(R.id.buttonJoinSession);
        Button buttonCancel = m_joinSessionDialog.findViewById(R.id.buttonCancelJoinSession);

        buttonConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                confirmSyncJoinSession();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                cancelSyncJoinSession();
            }
        });

        m_joinSessionDialog.show();
    }

    private void confirmSyncJoinSession()
    {
        CMSessionEvent se = null;
        EditText sessionNameEditText = m_joinSessionDialog.findViewById(R.id.joinSessionNameEditText);
        String strSessionName = sessionNameEditText.getText().toString().trim();

        if(!strSessionName.isEmpty())
        {
            m_cmEventHandler.setStartTime(System.currentTimeMillis());
            se = m_cmClientStub.syncJoinSession(strSessionName);
            long lDelay = System.currentTimeMillis() - m_cmEventHandler.getStartTime();
            if(se != null)
            {
                // print result of the request
                printMessage("successfully joined a session that has ("+se.getGroupNum()+") groups.\n");
                printMessage("return delay: "+lDelay+" ms.\n");
            }
            else
            {
                printMessage("failed the session-join request!\n");
            }
        }
        printMessage("======\n");

        m_joinSessionDialog.dismiss();
    }

    private void cancelSyncJoinSession()
    {
        printMessage("sync-join-session canceled!\n");
        m_joinSessionDialog.dismiss();
    }
    //////////

    ////////// leave session
    private void leaveSession()
    {
        boolean bRequestResult = false;
        printMessage("====== leave the current session\n");
        bRequestResult = m_cmClientStub.leaveSession();
        if(bRequestResult)
            printMessage("successfully sent the leave-session request.\n");
        else
            printMessage("failed the leave-session request!\n");

    }
    //////////

    ////////// change group
    private void changeGroup()
    {
        printMessage("====== change group\n");
        m_changeGroupDialog = new Dialog(this);
        m_changeGroupDialog.setContentView(R.layout.dialog_change_group);
        m_changeGroupDialog.setTitle(R.string.change_group_title);

        m_changeGroupDialog.show();
    }

    public void onConfirmChangeGroup(View v)
    {
        EditText changeGroupNameEditText = m_changeGroupDialog.findViewById(R.id.changeGroupNameEditText);
        String strGroupName = changeGroupNameEditText.getText().toString().trim();

        if(!strGroupName.isEmpty())
            m_cmClientStub.changeGroup(strGroupName);

        m_changeGroupDialog.dismiss();
    }

    public void onCancelChangeGroup(View v)
    {
        printMessage("change-group canceled!");
        m_changeGroupDialog.dismiss();
    }
    //////////

    ////////// chat
    private void chat()
    {
        printMessage("====== chat\n");

        m_chatDialog = new Dialog(this);
        m_chatDialog.setContentView(R.layout.dialog_chat);
        m_chatDialog.setTitle(R.string.chat_title);

        m_chatDialog.show();

    }

    public void onConfirmChat(View v)
    {
        EditText targetEditText = m_chatDialog.findViewById(R.id.chatTargetEditText);
        String strTarget = targetEditText.getText().toString().trim();
        EditText messageEditText = m_chatDialog.findViewById(R.id.chatMessageEditText);
        String strMessage = messageEditText.getText().toString().trim();

        m_cmClientStub.chat(strTarget, strMessage);
        m_chatDialog.dismiss();
    }

    public void onCancelChat(View v)
    {
        printMessage("chat canceled!\n");
        m_chatDialog.dismiss();
    }
    //////////

    ////////// add channel
    private void addChannel()
    {
        printMessage("========== add channel\n");
        m_addChannelDialog = new Dialog(this);
        m_addChannelDialog.setContentView(R.layout.dialog_channel);
        m_addChannelDialog.setTitle(R.string.add_channel);

        m_addChannelDialog.show();
    }

    public void onConfirmAddChannel(View v)
    {
        RadioButton sockChRadioButton = m_addChannelDialog.findViewById(R.id.sockChRadioButton);
        RadioButton datagramChRadioButton = m_addChannelDialog.findViewById(R.id.datagramChRadioButton);
        RadioButton multicastChRadioButton = m_addChannelDialog.findViewById(R.id.multicastChRadioButton);

        if(sockChRadioButton.isChecked())
        {
            printMessage("socket channel checked\n");
            m_addSocketChannelDialog = new Dialog(this);
            m_addSocketChannelDialog.setContentView(R.layout.dialog_socket_channel);
            m_addSocketChannelDialog.setTitle(R.string.add_socket_channel);
            m_addSocketChannelDialog.show();
        }
        else if(datagramChRadioButton.isChecked())
        {
            printMessage("datagram channel checked\n");
            m_addDatagramChannelDialog = new Dialog(this);
            m_addDatagramChannelDialog.setContentView(R.layout.dialog_datagram_channel);
            m_addDatagramChannelDialog.setTitle(R.string.add_datagram_channel);
            m_addDatagramChannelDialog.show();
        }
        else if(multicastChRadioButton.isChecked())
        {
            printMessage("multicast channel checked\n");
            printMessage("Android does not support NIO MULTICAST CHANNEL yet!!");
            m_addMulticastChannelDialog = new Dialog(this);
            m_addMulticastChannelDialog.setContentView(R.layout.dialog_multicast_channel);
            m_addMulticastChannelDialog.setTitle(R.string.add_multicast_channel);
            m_addMulticastChannelDialog.show();
        }

        m_addChannelDialog.dismiss();
    }

    public void onCancelAddChannel(View v)
    {
        printMessage("add-channel canceled!\n");
        m_addChannelDialog.dismiss();
    }

    public void onConfirmAddSocketChannel(View v)
    {
        int nChKey = -1;
        String strServerName = null;
        boolean isBlock = true;
        boolean isSyncCall = true;
        SocketChannel sc = null;
        boolean result = false;

        EditText chKeyEditText = m_addSocketChannelDialog.findViewById(R.id.chKeyEditText);
        try {
            nChKey = Integer.parseInt(chKeyEditText.getText().toString().trim());
        }catch(NumberFormatException e){
            printMessage("The channel key must be an integer number!");
            return;
        }

        EditText serverNameEditText = m_addSocketChannelDialog.findViewById(R.id.serverNameEditText);
        strServerName = serverNameEditText.getText().toString().trim();
        if(strServerName == null || strServerName.equals(""))
            strServerName = "SERVER"; // default server name

        RadioButton blockRadioButton = m_addSocketChannelDialog.findViewById(R.id.blockChRadioButton);
        RadioButton nonBlockRadioButton = m_addSocketChannelDialog.findViewById(R.id.nonBlockChRadioButton);
        if(blockRadioButton.isChecked()) isBlock = true;
        else if(nonBlockRadioButton.isChecked()) isBlock = false;

        RadioButton syncCallRadioButton = m_addSocketChannelDialog.findViewById(R.id.syncCallRadioButton);
        RadioButton asyncCallRadioButton = m_addSocketChannelDialog.findViewById(R.id.asyncCallRadioButton);
        if(syncCallRadioButton.isChecked()) isSyncCall = true;
        else if(asyncCallRadioButton.isChecked()) isSyncCall = false;

        if(!isBlock && nChKey <= 0)
        {
            printMessage("invalid nonblocking socket channel key ("+nChKey+")!\n");
            return;
        }
        else if(isBlock && nChKey < 0)
        {
            printMessage("invalid blocking socket channel key ("+nChKey+")!\n");
            return;
        }

        if(isBlock)
        {
            if(isSyncCall)
            {
                //m_eventHandler.setStartTime(System.currentTimeMillis());
                sc = m_cmClientStub.syncAddBlockSocketChannel(nChKey, strServerName);
                //lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
                if(sc != null)
                {
                    printMessage("Successfully added a blocking socket channel both "
                            + "at the client and the server: key("+nChKey+"), server("+strServerName+")\n");
                    //printMessage("return delay: "+lDelay+" ms.\n");
                }
                else
                    printMessage("Failed to add a blocking socket channel both at "
                            + "the client and the server: key("+nChKey+"), server("+strServerName+")\n");
            }
            else
            {
                //m_eventHandler.setStartTime(System.currentTimeMillis());
                result = m_cmClientStub.addBlockSocketChannel(nChKey, strServerName);
                //lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
                if(result)
                {
                    printMessage("Successfully added a blocking socket channel at the client and "
                            +"requested to add the channel info to the server: key("+nChKey+"), server("
                            +strServerName+")\n");
                    //printMessage("return delay: "+lDelay+" ms.\n");
                }
                else
                    printMessage("Failed to add a blocking socket channel at the client or "
                            +"failed to request to add the channel info to the server: key("+nChKey
                            +"), server("+strServerName+")\n");
            }
        }
        else
        {
            if(isSyncCall)
            {
                sc = m_cmClientStub.syncAddNonBlockSocketChannel(nChKey, strServerName);
                if(sc != null)
                    printMessage("Successfully added a nonblocking socket channel both at the client "
                            + "and the server: key("+nChKey+"), server("+strServerName+")\n");
                else
                    printMessage("Failed to add a nonblocking socket channel both at the client "
                            + "and the server: key("+nChKey+") to server("+strServerName+")\n");
            }
            else
            {
                result = m_cmClientStub.addNonBlockSocketChannel(nChKey, strServerName);
                if(result)
                    printMessage("Successfully added a nonblocking socket channel at the client and "
                            + "requested to add the channel info to the server: key("+nChKey+"), server("
                            +strServerName+")\n");
                else
                    printMessage("Failed to add a nonblocking socket channe at the client or "
                            + "failed to request to add the channel info to the server: key("+nChKey
                            +") to server("+strServerName+")\n");
            }
        }

        m_addSocketChannelDialog.dismiss();
    }

    public void onCancelAddSocketChannel(View v)
    {
        printMessage("add-socket-channel canceled!\n");
        m_addSocketChannelDialog.dismiss();
    }

    public void onConfirmAddDatagramChannel(View v)
    {
        int nChPort = -1;
        boolean isBlock = true;
        DatagramChannel dc = null;

        RadioButton blockChRadioButton = m_addDatagramChannelDialog.findViewById(R.id.blockChRadioButton);
        RadioButton nonBlockChRadioButton = m_addDatagramChannelDialog.findViewById(R.id.nonBlockChRadioButton);
        if(blockChRadioButton.isChecked()) isBlock = true;
        else if(nonBlockChRadioButton.isChecked()) isBlock = false;

        EditText portNumberEditText = m_addDatagramChannelDialog.findViewById(R.id.portNumberEditText);
        try{
            nChPort = Integer.parseInt(portNumberEditText.getText().toString().trim());
        }catch(NumberFormatException e){
            printMessage("The channel UDP port must be an integer number!\n");
            return;
        }

        if(isBlock)
        {
            dc = m_cmClientStub.addBlockDatagramChannel(nChPort);
            if(dc != null)
                printMessage("Successfully added a blocking datagram socket channel: port("+nChPort+")\n");
            else
                printMessage("Failed to add a blocking datagram socket channel: port("+nChPort+")\n");
        }
        else
        {
            dc = m_cmClientStub.addNonBlockDatagramChannel(nChPort);
            if(dc != null)
                printMessage("Successfully added a non-blocking datagram socket channel: port("+nChPort+")\n");
            else
                printMessage("Failed to add a non-blocking datagram socket channel: port("+nChPort+")\n");
        }

        m_addDatagramChannelDialog.dismiss();
    }

    public void onCancelAddDatagramChannel(View v)
    {
        printMessage("add-datagram-channel canceled!\n");
        m_addDatagramChannelDialog.dismiss();
    }

    public void onConfirmAddMulticastChannel(View v)
    {
        String strSessionName = null;
        String strGroupName = null;
        String strChAddress = null;
        int nChPort = -1;
        boolean result = false;

        EditText snameEditText = m_addMulticastChannelDialog.findViewById(R.id.sessionNameEditText);
        EditText gnameEditText = m_addMulticastChannelDialog.findViewById(R.id.groupNameEditText);
        EditText addrEditText = m_addMulticastChannelDialog.findViewById(R.id.multicastAddrEditText);
        EditText portEditText = m_addMulticastChannelDialog.findViewById(R.id.multicastPortEditText);

        strSessionName = snameEditText.getText().toString().trim();
        strGroupName = gnameEditText.getText().toString().trim();
        strChAddress = addrEditText.getText().toString().trim();
        try{
            nChPort = Integer.parseInt(portEditText.getText().toString().trim());
        }catch(NumberFormatException e){
            printMessage("The port number must be an integer number!");
            return;
        }

        result = m_cmClientStub.addMulticastChannel(strSessionName, strGroupName, strChAddress, nChPort);
        if(result)
        {
            printMessage("Successfully added a multicast channel: session("+strSessionName+"), group("
                    +strGroupName+"), address("+strChAddress+"), port("+nChPort+")\n");
        }
        else
        {
            printMessage("Failed to add a multicast channel: session("+strSessionName+"), group("
                    +strGroupName+"), address("+strChAddress+"), port("+nChPort+")\n");
        }

        m_addMulticastChannelDialog.dismiss();
    }

    public void onCancelAddMulticastChannel(View v)
    {
        printMessage("add-multicast-channel canceled!\n");
        m_addMulticastChannelDialog.dismiss();
    }

    //////////

    ////////// remove channel

    public void removeChannel()
    {
        printMessage("========== remove channel\n");
        m_removeChannelDialog = new Dialog(this);
        m_removeChannelDialog.setContentView(R.layout.dialog_channel);
        m_removeChannelDialog.setTitle(R.string.remove_channel);

        Button confirmButton = m_removeChannelDialog.findViewById(R.id.buttonConfirmChannel);
        Button cancelButton = m_removeChannelDialog.findViewById(R.id.buttonCancelChannel);

        confirmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onConfirmRemoveChannel(v);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onCancelRemoveChannel(v);
            }
        });

        m_removeChannelDialog.show();
    }

    public void onConfirmRemoveChannel(View v)
    {
        RadioButton sockChRadioButton = m_removeChannelDialog.findViewById(R.id.sockChRadioButton);
        RadioButton datagramChRadioButton = m_removeChannelDialog.findViewById(R.id.datagramChRadioButton);
        RadioButton multicastChRadioButton = m_removeChannelDialog.findViewById(R.id.multicastChRadioButton);

        if(sockChRadioButton.isChecked())
        {
            printMessage("socket channel checked\n");
            m_removeSocketChannelDialog = new Dialog(this);
            m_removeSocketChannelDialog.setContentView(R.layout.dialog_socket_channel);
            m_removeSocketChannelDialog.setTitle(R.string.remove_socket_channel);

            Button confirmRemoveSocketChannelButton = m_removeSocketChannelDialog.findViewById(R.id.buttonConfirmSocketChannel);
            Button cancelRemoveSocketChannelButton = m_removeSocketChannelDialog.findViewById(R.id.buttonCancelSocketChannel);

            confirmRemoveSocketChannelButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v)
                {
                    onConfirmRemoveSocketChannel(v);
                }
            });

            cancelRemoveSocketChannelButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v)
                {
                    onCancelRemoveSocketChannel(v);
                }
            });

            m_removeSocketChannelDialog.show();
        }
        else if(datagramChRadioButton.isChecked())
        {
            printMessage("datagram channel checked\n");
            m_removeDatagramChannelDialog = new Dialog(this);
            m_removeDatagramChannelDialog.setContentView(R.layout.dialog_datagram_channel);
            m_removeDatagramChannelDialog.setTitle(R.string.remove_datagram_channel);

            Button confirmRemoveDatagramChannelButton = m_removeDatagramChannelDialog.findViewById(R.id.buttonConfirmDatagramChannel);
            Button cancelRemoveDatagramChannelButton = m_removeDatagramChannelDialog.findViewById(R.id.buttonCancelDatagramChannel);

            confirmRemoveDatagramChannelButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v)
                {
                    onConfirmRemoveDatagramChannel(v);
                }
            });

            cancelRemoveDatagramChannelButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v)
                {
                    onCancelRemoveDatagramChannel(v);
                }
            });

            m_removeDatagramChannelDialog.show();
        }
        else if(multicastChRadioButton.isChecked())
        {
            printMessage("multicast channel checked\n");
            printMessage("Android does not support NIO MULTICAST CHANNEL yet!!");

            m_removeMulticastChannelDialog = new Dialog(this);
            m_removeMulticastChannelDialog.setContentView(R.layout.dialog_multicast_channel);
            m_removeMulticastChannelDialog.setTitle(R.string.remove_multicast_channel);

            Button confirmRemoveMulticastChannelButton = m_removeMulticastChannelDialog.findViewById(R.id.buttonConfirmMulticastChannel);
            Button cancelRemoveMulticastChannelButton = m_removeMulticastChannelDialog.findViewById(R.id.buttonCancelMulticastChannel);

            confirmRemoveMulticastChannelButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v)
                {
                    onConfirmRemoveMulticastChannel(v);
                }
            });

            cancelRemoveMulticastChannelButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v)
                {
                    onCancelRemoveMulticastChannel(v);
                }
            });

            m_removeMulticastChannelDialog.show();
        }

        m_removeChannelDialog.dismiss();
    }

    public void onCancelRemoveChannel(View v)
    {
        printMessage("remove-channel canceled!\n");
        m_removeChannelDialog.dismiss();
    }

    public void onConfirmRemoveSocketChannel(View v)
    {
        int nChKey = -1;
        String strServerName = null;
        boolean isBlock = true;
        boolean isSyncCall = true;
        SocketChannel sc = null;
        boolean result = false;

        EditText chKeyEditText = m_removeSocketChannelDialog.findViewById(R.id.chKeyEditText);
        try {
            nChKey = Integer.parseInt(chKeyEditText.getText().toString().trim());
        }catch(NumberFormatException e){
            printMessage("The channel key must be an integer number!");
            return;
        }

        EditText serverNameEditText = m_removeSocketChannelDialog.findViewById(R.id.serverNameEditText);
        strServerName = serverNameEditText.getText().toString().trim();
        if(strServerName == null || strServerName.equals(""))
            strServerName = "SERVER"; // default server name

        RadioButton blockRadioButton = m_removeSocketChannelDialog.findViewById(R.id.blockChRadioButton);
        RadioButton nonBlockRadioButton = m_removeSocketChannelDialog.findViewById(R.id.nonBlockChRadioButton);
        if(blockRadioButton.isChecked()) isBlock = true;
        else if(nonBlockRadioButton.isChecked()) isBlock = false;

        RadioButton syncCallRadioButton = m_removeSocketChannelDialog.findViewById(R.id.syncCallRadioButton);
        RadioButton asyncCallRadioButton = m_removeSocketChannelDialog.findViewById(R.id.asyncCallRadioButton);
        if(syncCallRadioButton.isChecked()) isSyncCall = true;
        else if(asyncCallRadioButton.isChecked()) isSyncCall = false;

        if(isBlock)
        {
            if(isSyncCall)
            {
                //m_eventHandler.setStartTime(System.currentTimeMillis());
                result = m_cmClientStub.syncRemoveBlockSocketChannel(nChKey, strServerName);
                //lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
                if(result)
                {
                    printMessage("Successfully removed a blocking socket channel both "
                            + "at the client and the server: key("+nChKey+"), server ("+strServerName+")\n");
                    //printMessage("return delay: "+lDelay+" ms.\n");
                }
                else
                    printMessage("Failed to remove a blocking socket channel both at the client "
                            + "and the server: key("+nChKey+"), server ("+strServerName+")\n");
            }
            else
            {
                //m_eventHandler.setStartTime(System.currentTimeMillis());
                result = m_cmClientStub.removeBlockSocketChannel(nChKey, strServerName);
                //lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
                if(result)
                {
                    printMessage("Successfully removed a blocking socket channel at the client and "
                            + "requested to remove it at the server: key("+nChKey+"), server("+strServerName+")\n");
                    //printMessage("return delay: "+lDelay+" ms.\n");
                }
                else
                    printMessage("Failed to remove a blocking socket channel at the client or "
                            + "failed to request to remove it at the server: key("+nChKey+"), server("
                            +strServerName+")\n");
            }
        }
        else
        {
            result = m_cmClientStub.removeNonBlockSocketChannel(nChKey, strServerName);
            if(result)
                printMessage("Successfully removed a nonblocking socket channel: key("+nChKey
                        +"), server("+strServerName+")\n");
            else
                printMessage("Failed to remove a nonblocing socket channel: key("+nChKey
                        +"), server("+strServerName+")\n");
        }

        m_removeSocketChannelDialog.dismiss();
    }

    public void onCancelRemoveSocketChannel(View v)
    {
        printMessage("remove-socket canceled!\n");
        m_removeSocketChannelDialog.dismiss();
    }

    public void onConfirmRemoveDatagramChannel(View v)
    {
        int nChPort = -1;
        boolean isBlock = true;
        DatagramChannel dc = null;
        boolean result = false;

        RadioButton blockChRadioButton = m_removeDatagramChannelDialog.findViewById(R.id.blockChRadioButton);
        RadioButton nonBlockChRadioButton = m_removeDatagramChannelDialog.findViewById(R.id.nonBlockChRadioButton);
        if(blockChRadioButton.isChecked()) isBlock = true;
        else if(nonBlockChRadioButton.isChecked()) isBlock = false;

        EditText portNumberEditText = m_removeDatagramChannelDialog.findViewById(R.id.portNumberEditText);
        try{
            nChPort = Integer.parseInt(portNumberEditText.getText().toString().trim());
        }catch(NumberFormatException e){
            printMessage("The channel UDP port must be an integer number!\n");
            return;
        }

        if(isBlock)
        {
            result = m_cmClientStub.removeBlockDatagramChannel(nChPort);
            if(result)
                printMessage("Successfully removed a blocking datagram socket channel: port("+nChPort+")\n");
            else
                printMessage("Failed to remove a blocking datagram socket channel: port("+nChPort+")\n");
        }
        else
        {
            result = m_cmClientStub.removeNonBlockDatagramChannel(nChPort);
            if(result)
                printMessage("Successfully removed a non-blocking datagram socket channel: port("+nChPort+")\n");
            else
                printMessage("Failed to remove a non-blocking datagram socket channel: port("+nChPort+")\n");
        }

        m_removeDatagramChannelDialog.dismiss();
    }

    public void onCancelRemoveDatagramChannel(View v)
    {
        printMessage("remove-datagram-channel canceled!\n");
        m_removeDatagramChannelDialog.dismiss();
    }

    public void onConfirmRemoveMulticastChannel(View v)
    {
        String strSessionName = null;
        String strGroupName = null;
        String strChAddress = null;
        int nChPort = -1;
        boolean result = false;

        EditText snameEditText = m_removeMulticastChannelDialog.findViewById(R.id.sessionNameEditText);
        EditText gnameEditText = m_removeMulticastChannelDialog.findViewById(R.id.groupNameEditText);
        EditText addrEditText = m_removeMulticastChannelDialog.findViewById(R.id.multicastAddrEditText);
        EditText portEditText = m_removeMulticastChannelDialog.findViewById(R.id.multicastPortEditText);

        strSessionName = snameEditText.getText().toString().trim();
        strGroupName = gnameEditText.getText().toString().trim();
        strChAddress = addrEditText.getText().toString().trim();
        try{
            nChPort = Integer.parseInt(portEditText.getText().toString().trim());
        }catch(NumberFormatException e){
            printMessage("The port number must be an integer number!");
            return;
        }

        result = m_cmClientStub.removeAdditionalMulticastChannel(strSessionName, strGroupName, strChAddress, nChPort);
        if(result)
        {
            printMessage("Successfully removed a multicast channel: session("+strSessionName+"), group("
                    +strGroupName+"), address("+strChAddress+"), port("+nChPort+")\n");
        }
        else
        {
            printMessage("Failed to remove a multicast channel: session("+strSessionName+"), group("
                    +strGroupName+"), address("+strChAddress+"), port("+nChPort+")\n");
        }

        m_removeMulticastChannelDialog.dismiss();
    }

    public void onCancelRemoveMulticastChannel(View v)
    {
        printMessage("remove-multicast-channel canceled!\n");
        m_removeMulticastChannelDialog.dismiss();
    }

    //////////

    ////////// print info

    public void printGroupInfo()
    {
        // check local state
        CMInteractionInfo interInfo = m_cmClientStub.getCMInfo().getInteractionInfo();
        CMUser myself = interInfo.getMyself();

        if(myself.getState() != CMInfo.CM_SESSION_JOIN)
        {
            //System.out.println("You should join a session and a group.");
            printMessage("You should join a session and a group.\n");
            return;
        }

        printMessage("========== print group info of current session\n");

        CMSession session = interInfo.findSession(myself.getCurrentSession());
        Iterator<CMGroup> iter = session.getGroupList().iterator();
        printMessage("---------------------------------------------------------\n");
        printMessage(String.format("%-20s%-20s%-20s%n", "group name", "multicast addr", "multicast port"));
        printMessage("---------------------------------------------------------\n");

        while(iter.hasNext())
        {
            CMGroupInfo gInfo = iter.next();
            printMessage(String.format("%-20s%-20s%-20d%n", gInfo.getGroupName(), gInfo.getGroupAddress()
                    , gInfo.getGroupPort()));
        }

        return;
    }

    public void printCurrentUserStatus()
    {
        CMInteractionInfo interInfo = m_cmClientStub.getCMInfo().getInteractionInfo();
        CMUser myself = interInfo.getMyself();
        CMConfigurationInfo confInfo = m_cmClientStub.getCMInfo().getConfigurationInfo();
        printMessage("========== print current user status for the default server\n");
        printMessage("name("+myself.getName()+"), session("+myself.getCurrentSession()+"), group("
                +myself.getCurrentGroup()+"), udp port("+myself.getUDPPort()+"), state("
                +myself.getState()+"), attachment download scheme("+confInfo.getAttachDownloadScheme()+").\n");

        // for additional servers
        Iterator<CMServer> iter = interInfo.getAddServerList().iterator();
        while(iter.hasNext())
        {
            CMServer tserver = iter.next();
            if(tserver.getNonBlockSocketChannelInfo().findChannel(0) != null)
            {
                printMessage("------ for additional server["+tserver.getServerName()+"]\n");
                printMessage("current session("+tserver.getCurrentSessionName()+
                        "), current group("+tserver.getCurrentGroupName()+"), state("
                        +tserver.getClientState()+").");

            }
        }

        return;
    }

    public void printCurrentChannelInfo()
    {
        printMessage("========== print current channel info\n");
        String strChannels = m_cmClientStub.getCurrentChannelInfo();
        printMessage(strChannels);
    }

    public void printConfigurations()
    {
        String[] strConfigurations;
        printMessage("========== print all current configurations\n");
        Path confPath = m_cmClientStub.getConfigurationHome().resolve("cm-client.conf");
        strConfigurations = CMConfigurator.getConfigurations(confPath.toString());

        printMessage("configuration file path: "+confPath.toString()+"\n");
        for(String strConf : strConfigurations)
        {
            String[] strFieldValuePair;
            strFieldValuePair = strConf.split("\\s+");
            printMessage(strFieldValuePair[0]+" = "+strFieldValuePair[1]+"\n");
        }
    }

    //////////

    ////////// request file

    public void requestFile()
    {
        printMessage("========== request file\n");
        m_requestFileDialog = new Dialog(this);
        m_requestFileDialog.setContentView(R.layout.dialog_request_file);
        m_requestFileDialog.setTitle(R.string.req_file);

        m_requestFileDialog.show();
    }

    public void onConfirmRequestFile(View v)
    {
        boolean bReturn = false;
        String strFileName = null;
        String strFileOwner = null;
        String strFileAppendMode = null;

        EditText fileNameEditText = m_requestFileDialog.findViewById(R.id.fileNameEditText);
        strFileName = fileNameEditText.getText().toString().trim();
        EditText fileOwnerEditText = m_requestFileDialog.findViewById(R.id.fileOwnerEditText);
        strFileOwner = fileOwnerEditText.getText().toString().trim();
        RadioButton defaultRadioButton = m_requestFileDialog.findViewById(R.id.defaultRadioButton);
        RadioButton overwriteRadioButton = m_requestFileDialog.findViewById(R.id.overwriteRadioButton);
        RadioButton appendRadioButton = m_requestFileDialog.findViewById(R.id.appendRadioButton);
        if(defaultRadioButton.isChecked())
            strFileAppendMode = "Default";
        else if(overwriteRadioButton.isChecked())
            strFileAppendMode = "Overwrite";
        else
            strFileAppendMode = "Append";

        m_cmEventHandler.setStartTime(System.currentTimeMillis());	// set the start time of the request

        if(strFileAppendMode.equals("Default"))
            bReturn = m_cmClientStub.requestFile(strFileName, strFileOwner);
        else if(strFileAppendMode.equals("Overwrite"))
            bReturn = m_cmClientStub.requestFile(strFileName,  strFileOwner, CMInfo.FILE_OVERWRITE);
        else
            bReturn = m_cmClientStub.requestFile(strFileName, strFileOwner, CMInfo.FILE_APPEND);

        if(!bReturn)
            printMessage("Request file error! file("+strFileName+"), owner("+strFileOwner+").\n");

        m_requestFileDialog.dismiss();
    }

    public void onCancelRequestFile(View v)
    {
        printMessage("request-file canceled!\n");
        m_requestFileDialog.dismiss();
    }
    //////////

    ////////// push file

    public void pushFile()
    {
        printMessage("========== push file\n");
        m_pushFileDialog = new Dialog(this);
        m_pushFileDialog.setContentView(R.layout.dialog_push_file);
        m_pushFileDialog.setTitle(R.string.push_file);

        m_pushFileDialog.show();
    }

    public void onConfirmPushFile(View v)
    {
        EditText recvEditText = m_pushFileDialog.findViewById(R.id.fileReceiverEditText);
        m_strReceiver = recvEditText.getText().toString().trim();

        // file selection with Storage Access Framework(SAF) since Android 4.4(API level 19)
        performFileSearch();

        m_pushFileDialog.dismiss();
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch()
    {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());

                // find real file path
                String strFilePath = null;
                try {
                    strFilePath = PathUtil.getPath(getApplicationContext(), uri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "found file path: "+strFilePath);
                //showImage(uri);

                //call pushFile of CM
                boolean ret = m_cmClientStub.pushFile(strFilePath, m_strReceiver);
                if(!ret)
                    printMessage("file push error! receiver("+m_strReceiver
                            +"), file path("+uri.getPath()+")");
            }
        }
    }

    public void onCancelPushFile(View v)
    {
        printMessage("push-file canceled!\n");
        m_pushFileDialog.dismiss();
    }

    //////////

    ////////// measure network throughput

    public void measureInputThroughput()
    {
        printMessage("========== measure input network throughput\n");
        m_measureInputThgoughputDialog = new Dialog(this);
        m_measureInputThgoughputDialog.setContentView(R.layout.dialog_target_node);
        m_measureInputThgoughputDialog.setTitle(R.string.measure_input_throughput);

        m_measureInputThgoughputDialog.show();
    }

    public void onConfirmMeasureInputThroughput(View v)
    {
        String strTarget = null;
        float fSpeed = -1;

        EditText targetEditText = m_measureInputThgoughputDialog.findViewById(R.id.targetNodeEditText);
        strTarget = targetEditText.getText().toString().trim();

        if(strTarget == null)
            return;
        else if(strTarget.equals(""))
            strTarget = "SERVER";

        fSpeed = m_cmClientStub.measureInputThroughput(strTarget);
        if(fSpeed == -1)
            printMessage("Test failed!\n");
        else
            printMessage(String.format("Input network throughput from [%s] : %.2f MBps%n", strTarget, fSpeed));

        m_measureInputThgoughputDialog.dismiss();
    }

    public void onCancelMeasureInputThroughput(View v)
    {
        printMessage("measure-input-network-throughput canceled!\n");
        m_measureInputThgoughputDialog.dismiss();
    }

    public void measureOutputThroughput()
    {
        printMessage("========== measure output network throughput\n");
        m_measureOutputThroughputDialog = new Dialog(this);
        m_measureOutputThroughputDialog.setContentView(R.layout.dialog_target_node);
        m_measureOutputThroughputDialog.setTitle(R.string.measure_output_throughput);

        Button buttonConfirm = m_measureOutputThroughputDialog.findViewById(R.id.buttonMeasureThroughput);
        Button buttonCancel = m_measureOutputThroughputDialog.findViewById(R.id.buttonCancelMeasureThroughput);

        buttonConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onConfirmMeasureOutputThroughput(v);
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onCancelMeasureOutputThroughput(v);
            }
        });

        m_measureOutputThroughputDialog.show();
    }

    public void onConfirmMeasureOutputThroughput(View v)
    {
        String strTarget = null;
        float fSpeed = -1;

        EditText targetEditText = m_measureOutputThroughputDialog.findViewById(R.id.targetNodeEditText);
        strTarget = targetEditText.getText().toString().trim();

        if(strTarget == null)
            return;
        else if(strTarget.equals(""))
            strTarget = "SERVER";

        fSpeed = m_cmClientStub.measureOutputThroughput(strTarget);
        if(fSpeed == -1)
            printMessage("Test failed!\n");
        else
            printMessage(String.format("Output network throughput to [%s] : %.2f MBps%n", strTarget, fSpeed));

        m_measureOutputThroughputDialog.dismiss();
    }

    public void onCancelMeasureOutputThroughput(View v)
    {
        printMessage("measure-output-network-throughput canceled!\n");
        m_measureOutputThroughputDialog.dismiss();
    }
    //////////
}
