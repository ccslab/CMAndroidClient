package com.example.mlim.cmclient;

//import android.support.v4.app.DialogFragment;
import android.app.DialogFragment;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;
import kr.ac.konkuk.ccslab.cm.util.CMUtil;

public class MainActivity extends AppCompatActivity implements ServerInfoDialogFragment.ServerInfoDialogListener,
        LoginDSDialogFragment.LoginDSDialogListener,
        JoinSessionDialogFragment.JoinSessionDialogListener,
        ChangeGroupDialogFragment.ChangeGroupDialogListener,
        ChatDialogFragment.ChatDialogListener,
        AddChannelDialogFragment.AddChannelDialogListener,
        AddSocketChannelDialogFragment.AddSocketChannelDialogListener,
        AddDatagramChannelDialogFragment.AddDatagramChannelDialogListener,
        AddMulticastChannelDialogFragment.AddMulticastChannelDialogListener,
        RemoveChannelDialogFragment.RemoveChannelDialogListener,
        RemoveSocketChannelDialogFragment.RemoveSocketChannelDialogListener,
        RemoveDatagramChannelDialogFragment.RemoveDatagramChannelDialogListener,
        RemoveMulticastChannelDialogFragment.RemoveMulticastChannelDialogListener
{

    private CMClientStub m_cmClientStub;
    private CMClientEventHandler m_cmEventHandler;

    public static final String EXTRA_MESSAGE = "com.example.mlim.CMClient.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                printMessageln("Not supported yet!");
                //syncLoginDS();
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
                printMessageln("Not supported yet!");
                //syncSessionInfoDS();
                break;
            case "22": // join a session
                joinSession();
                break;
            case "23": // synchronously join a session
                printMessageln("Not supported yet!");
                //syncJoinSession();
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
            case "50": // print group info
                printMessageln("Not supported yet!");
                //printGroupInfo();
                break;
            case "51": // print current information about the client
                printMessageln("Not supported yet!");
                //currentUserStatus();
                break;
            case "52": 	// print current channels information
                printMessageln("Not supported yet!");
                //printCurrentChannelInfo();
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
                printMessageln("Not supported yet!");
                //measureInputThroughput();
                break;
            case "56": // test output network throughput
                printMessageln("Not supported yet!");
                //measureOutputThroughput();
                break;
            case "57": // print all configurations
                printMessageln("Not supported yet!");
                //printConfigurations();
                break;
            case "58": // change configuration
                printMessageln("Not supported yet!");
                //changeConfiguration();
                break;
            case "60": // add additional channel
                addChannel();
                break;
            case "61": // remove additional channel
                printMessageln("Not supported yet!");
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
                printMessageln("Not supported yet!");
                //requestFile();
                break;
            case "72": // push a file
                printMessageln("Not supported yet!");
                //pushFile();
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
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new ServerInfoDialogFragment();
        Bundle args = new Bundle();
        args.putString("serverAddress", strAddress);
        args.putInt("serverPort", nPort);
        dialog.setArguments(args);

        dialog.show(getFragmentManager(), "ServerInfoDialogFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onServerInfoDialogConfirmClick(DialogFragment dialog) {
        // User touched the dialog's confirm button
        EditText addressEditText = dialog.getView().findViewById(R.id.serverAddressEditText);
        EditText portEditText = dialog.getView().findViewById(R.id.serverPortEditText);
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

    }

    @Override
    public void onServerInfoDialogCancelClick(DialogFragment dialog) {
        // User touched the dialog's cancel button
        // nothing to do yet!
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
    }
    //////////

    ////////// login
    private void loginDS()
    {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new LoginDSDialogFragment();
        dialog.show(getFragmentManager(), "LoginDSDialogFragment");

    }

    public void onLoginDSDialogConfirmClick(DialogFragment dialog)
    {
        EditText idEditText = dialog.getView().findViewById(R.id.loginDSIDEditText);
        EditText passwdEditText = dialog.getView().findViewById(R.id.loginDSPasswdEditText);
        String strID = idEditText.getText().toString().trim();
        String strPasswd = passwdEditText.getText().toString();
        String strEncPasswd = CMUtil.getSHA1Hash(strPasswd);

        boolean bRequestResult = m_cmClientStub.loginCM(strID, strEncPasswd);
        if(bRequestResult)
        {
            printMessage("successfully sent the login request.\n");
        }
        else
        {
            printMessage("failed the login request!\n");
        }

    }

    public void onLoginDSDialogCancelClick(DialogFragment dialog)
    {
        // nothing to do
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

    }
    //////////

    ////////// request session info
    private void requestSessionInfoDS()
    {
        boolean bRequestResult = false;
        printMessage("====== request session info from default server\n");
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
    //////////

    ////////// join session
    private void joinSession()
    {
        DialogFragment dialog = new JoinSessionDialogFragment();
        dialog.show(getFragmentManager(), "JoinSessionDialogFragment");
    }

    public void onJoinSessionDialogConfirmClick(DialogFragment dialog)
    {
        printMessage("====== join a session\n");

        EditText sessionNameEditText = dialog.getView().findViewById(R.id.joinSessionNameEditText);
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

    }

    public void onJoinSessionDialogCancelClick(DialogFragment dialog)
    {
        // nothing to do
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
        DialogFragment dialog = new ChangeGroupDialogFragment();
        dialog.show(getFragmentManager(), "ChangeGroupDialogFragment");
    }

    public void onChangeGroupDialogConfirmClick(DialogFragment dialog)
    {
        printMessage("====== change group\n");
        EditText changeGroupNameEditText = dialog.getView().findViewById(R.id.changeGroupNameEditText);
        String strGroupName = changeGroupNameEditText.getText().toString().trim();

        if(strGroupName != null)
            m_cmClientStub.changeGroup(strGroupName);

    }

    public void onChangeGroupDialogCancelClick(DialogFragment dialog)
    {
        // nothing to do
    }
    //////////

    ////////// chat
    private void chat()
    {
        DialogFragment dialog = new ChatDialogFragment();
        dialog.show(getFragmentManager(), "ChatDialogFragment");
    }

    public void onChatDialogConfirmClick(DialogFragment dialog)
    {
        printMessage("====== chat\n");
        EditText targetEditText = dialog.getView().findViewById(R.id.chatTargetEditText);
        String strTarget = targetEditText.getText().toString().trim();
        EditText messageEditText = dialog.getView().findViewById(R.id.chatMessageEditText);
        String strMessage = messageEditText.getText().toString().trim();

        m_cmClientStub.chat(strTarget, strMessage);

    }

    public void onChatDialogCancelClick(DialogFragment dialog)
    {
        // nothing to do
    }
    //////////

    ////////// add channel
    private void addChannel()
    {
        printMessage("====== add channel\n");
        DialogFragment dialog = new AddChannelDialogFragment();
        dialog.show(getFragmentManager(), "AddChannelDialogFragment");
    }

    public void onAddChannelDialogConfirmClick(DialogFragment dialog)
    {
        RadioButton sockChRadioButton = dialog.getView().findViewById(R.id.sockChRadioButton);
        RadioButton datagramChRadioButton = dialog.getView().findViewById(R.id.datagramChRadioButton);
        RadioButton multicastChRadioButton = dialog.getView().findViewById(R.id.multicastChRadioButton);

        if(sockChRadioButton.isChecked())
        {
            printMessage("socket channel checked\n");
            DialogFragment sockChDialog = new AddSocketChannelDialogFragment();
            sockChDialog.show(getFragmentManager(), "AddSocketChannelDialogFragment");
        }
        else if(datagramChRadioButton.isChecked())
        {
            printMessage("datagram channel checked\n");
            DialogFragment datagramChDialog = new AddDatagramChannelDialogFragment();
            datagramChDialog.show(getFragmentManager(), "AddDatagramChannelDialogFragment");
        }
        else if(multicastChRadioButton.isChecked())
        {
            printMessage("multicast channel checked\n");
            printMessage("Android does not support NIO MULTICAST CHANNEL yet!!");
            DialogFragment multicastChDialog = new AddMulticastChannelDialogFragment();
            multicastChDialog.show(getFragmentManager(), "AddMulticastChannelDialogFragment");
        }
    }

    public void onAddChannelDialogCancelClick(DialogFragment dialog)
    {
        // nothing to do
    }

    public void onAddSocketChannelDialogConfirmClick(DialogFragment dialog)
    {
        int nChKey = -1;
        String strServerName = null;
        boolean isBlock = true;
        boolean isSyncCall = true;
        SocketChannel sc = null;
        boolean result = false;

        EditText chKeyEditText = dialog.getView().findViewById(R.id.chKeyEditText);
        try {
            nChKey = Integer.parseInt(chKeyEditText.getText().toString().trim());
        }catch(NumberFormatException e){
            printMessage("The channel key must be an integer number!");
            return;
        }

        EditText serverNameEditText = dialog.getView().findViewById(R.id.serverNameEditText);
        strServerName = serverNameEditText.getText().toString().trim();
        if(strServerName == null || strServerName.equals(""))
            strServerName = "SERVER"; // default server name

        RadioButton blockRadioButton = dialog.getView().findViewById(R.id.blockChRadioButton);
        RadioButton nonBlockRadioButton = dialog.getView().findViewById(R.id.nonBlockChRadioButton);
        if(blockRadioButton.isChecked()) isBlock = true;
        else if(nonBlockRadioButton.isChecked()) isBlock = false;

        RadioButton syncCallRadioButton = dialog.getView().findViewById(R.id.syncCallRadioButton);
        RadioButton asyncCallRadioButton = dialog.getView().findViewById(R.id.asyncCallRadioButton);
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

    }

    public void onAddSocketChannelDialogCancelClick(DialogFragment dialog)
    {
        // nothing to do
    }

    public void onAddDatagramChannelDialogConfirmClick(DialogFragment dialog)
    {
        int nChPort = -1;
        boolean isBlock = true;
        DatagramChannel dc = null;

        RadioButton blockChRadioButton = dialog.getView().findViewById(R.id.blockChRadioButton);
        RadioButton nonBlockChRadioButton = dialog.getView().findViewById(R.id.nonBlockChRadioButton);
        if(blockChRadioButton.isChecked()) isBlock = true;
        else if(nonBlockChRadioButton.isChecked()) isBlock = false;

        EditText portNumberEditText = dialog.getView().findViewById(R.id.portNumberEditText);
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

    }

    public void onAddDatagramChannelDialogCancelClick(DialogFragment dialog)
    {
        // nothing to do
    }

    public void onAddMulticastChannelDialogConfirmClick(DialogFragment dialog)
    {
        String strSessionName = null;
        String strGroupName = null;
        String strChAddress = null;
        int nChPort = -1;
        boolean result = false;

        EditText snameEditText = dialog.getView().findViewById(R.id.sessionNameEditText);
        EditText gnameEditText = dialog.getView().findViewById(R.id.groupNameEditText);
        EditText addrEditText = dialog.getView().findViewById(R.id.multicastAddrEditText);
        EditText portEditText = dialog.getView().findViewById(R.id.multicastPortEditText);

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

    }

    public void onAddMulticastChannelDialogCancelClick(DialogFragment dialog)
    {
        // nothing to do
    }

    //////////

    ////////// remove channel

    public void removeChannel()
    {
        printMessage("remove channel\n");
        DialogFragment dialog = new RemoveChannelDialogFragment();
        dialog.show(getFragmentManager(), "RemoveChannelDialogFragment");
    }

    public void onRemoveChannelDialogConfirmClick(DialogFragment dialog)
    {
        RadioButton sockChRadioButton = dialog.getView().findViewById(R.id.sockChRadioButton);
        RadioButton datagramChRadioButton = dialog.getView().findViewById(R.id.datagramChRadioButton);
        RadioButton multicastChRadioButton = dialog.getView().findViewById(R.id.multicastChRadioButton);

        if(sockChRadioButton.isChecked())
        {
            printMessage("socket channel checked\n");
            DialogFragment sockChDialog = new RemoveSocketChannelDialogFragment();
            sockChDialog.show(getFragmentManager(), "RemoveSocketChannelDialogFragment");
        }
        else if(datagramChRadioButton.isChecked())
        {
            printMessage("datagram channel checked\n");
            DialogFragment datagramChDialog = new RemoveDatagramChannelDialogFragment();
            datagramChDialog.show(getFragmentManager(), "RemoveDatagramChannelDialogFragment");
        }
        else if(multicastChRadioButton.isChecked())
        {
            printMessage("multicast channel checked\n");
            printMessage("Android does not support NIO MULTICAST CHANNEL yet!!");
            DialogFragment multicastChDialog = new RemoveMulticastChannelDialogFragment();
            multicastChDialog.show(getFragmentManager(), "RemoveMulticastChannelDialogFragment");
        }

    }

    public void onRemoveChannelDialogCancelClick(DialogFragment dialog)
    {
        // nothing to do
    }

    public void onRemoveSocketChannelDialogConfirmClick(DialogFragment dialog)
    {
        int nChKey = -1;
        String strServerName = null;
        boolean isBlock = true;
        boolean isSyncCall = true;
        SocketChannel sc = null;
        boolean result = false;

        EditText chKeyEditText = dialog.getView().findViewById(R.id.chKeyEditText);
        try {
            nChKey = Integer.parseInt(chKeyEditText.getText().toString().trim());
        }catch(NumberFormatException e){
            printMessage("The channel key must be an integer number!");
            return;
        }

        EditText serverNameEditText = dialog.getView().findViewById(R.id.serverNameEditText);
        strServerName = serverNameEditText.getText().toString().trim();
        if(strServerName == null || strServerName.equals(""))
            strServerName = "SERVER"; // default server name

        RadioButton blockRadioButton = dialog.getView().findViewById(R.id.blockChRadioButton);
        RadioButton nonBlockRadioButton = dialog.getView().findViewById(R.id.nonBlockChRadioButton);
        if(blockRadioButton.isChecked()) isBlock = true;
        else if(nonBlockRadioButton.isChecked()) isBlock = false;

        RadioButton syncCallRadioButton = dialog.getView().findViewById(R.id.syncCallRadioButton);
        RadioButton asyncCallRadioButton = dialog.getView().findViewById(R.id.asyncCallRadioButton);
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

    }

    public void onRemoveSocketChannelDialogCancelClick(DialogFragment dialog)
    {
        // nothing to do
    }

    public void onRemoveDatagramChannelDialogConfirmClick(DialogFragment dialog)
    {
        int nChPort = -1;
        boolean isBlock = true;
        DatagramChannel dc = null;
        boolean result = false;

        RadioButton blockChRadioButton = dialog.getView().findViewById(R.id.blockChRadioButton);
        RadioButton nonBlockChRadioButton = dialog.getView().findViewById(R.id.nonBlockChRadioButton);
        if(blockChRadioButton.isChecked()) isBlock = true;
        else if(nonBlockChRadioButton.isChecked()) isBlock = false;

        EditText portNumberEditText = dialog.getView().findViewById(R.id.portNumberEditText);
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
    }

    public void onRemoveDatagramChannelDialogCancelClick(DialogFragment dialog)
    {
        // nothing to do
    }

    public void onRemoveMulticastChannelDialogConfirmClick(DialogFragment dialog)
    {
        String strSessionName = null;
        String strGroupName = null;
        String strChAddress = null;
        int nChPort = -1;
        boolean result = false;

        EditText snameEditText = dialog.getView().findViewById(R.id.sessionNameEditText);
        EditText gnameEditText = dialog.getView().findViewById(R.id.groupNameEditText);
        EditText addrEditText = dialog.getView().findViewById(R.id.multicastAddrEditText);
        EditText portEditText = dialog.getView().findViewById(R.id.multicastPortEditText);

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

    }

    public void onRemoveMulticastChannelDialogCancelClick(DialogFragment dialog)
    {
        // nothing to do
    }

    //////////
}
