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
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ServerInfoDialogFragment.ServerInfoDialogListener,
        LoginDSDialogFragment.LoginDSDialogListener,
        JoinSessionDialogFragment.JoinSessionDialogListener
{

    private CMRunnable m_cmRunnable;

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
        m_cmRunnable = new CMRunnable(this);
        m_cmRunnable.initCM();

        // check and update current server information
        m_cmRunnable.checkUpdateServerInfo();

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
                m_cmRunnable.checkUpdateServerInfo();
                break;
            case "999": // terminate CM
                terminateCM();
                break;
            case "1":   // connect to default server
                connectionDS();
                break;
            case "2":   // disconnect from default server
                disconnectionDS();
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
                printMessageln("Not supported yet!");
                joinSession();
                break;
            case "23": // synchronously join a session
                printMessageln("Not supported yet!");
                //syncJoinSession();
                break;
            case "24": // leave the current session
                printMessageln("Not supported yet!");
                //leaveSession();
                break;
            case "25": // change current group
                printMessageln("Not supported yet!");
                //changeGroup();
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
                printMessageln("Not supported yet!");
                //chat();
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
                printMessageln("Not supported yet!");
                //addChannel();
                break;
            case "61": // remove additional channel
                printMessageln("Not supported yet!");
                //removeChannel();
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

        m_cmRunnable.getClientStub().setServerAddress(strAddress);
        m_cmRunnable.getClientStub().setServerPort(nPort);

        // CM must start in a separate thread because of the Android policy !!
        startCM();
    }

    @Override
    public void onServerInfoDialogCancelClick(DialogFragment dialog) {
        // User touched the dialog's cancel button
        // nothing to do yet!
    }

    private void startCM()
    {
        TextView cmTextView = (TextView) findViewById(R.id.cmTextView);
        cmTextView.setText("");

        Thread t = new Thread(m_cmRunnable);
        t.start();
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

    private void wakeUpCMRunnable()
    {
        synchronized(m_cmRunnable.getSyncObject())
        {
            m_cmRunnable.getSyncObject().notify();
        }
    }

    private void terminateCM()
    {
        m_cmRunnable.setMenu("terminateCM");
        wakeUpCMRunnable();
    }

    private void connectionDS()
    {
        m_cmRunnable.setMenu("connectionDS");
        wakeUpCMRunnable();
    }

    private void disconnectionDS()
    {
        m_cmRunnable.setMenu("disconnectionDS");
        wakeUpCMRunnable();
    }

    private void loginDS()
    {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new LoginDSDialogFragment();
        dialog.show(getFragmentManager(), "LoginDSDialogFragment");

    }

    public void onLoginDSDialogConfirmClick(DialogFragment dialog)
    {
        // wakeup the CM thread (called after dialog)
        m_cmRunnable.setMenu("loginDS");
        m_cmRunnable.setDialog(dialog);
        wakeUpCMRunnable();
    }

    public void onLoginDSDialogCancelClick(DialogFragment dialog)
    {
        // nothing to do
    }

    private void logoutDS()
    {
        m_cmRunnable.setMenu("logoutDS");
        wakeUpCMRunnable();
    }

    private void requestSessionInfoDS()
    {
        m_cmRunnable.setMenu("requestSessionInfoDS");
        wakeUpCMRunnable();
    }

    private void joinSession()
    {
        DialogFragment dialog = new JoinSessionDialogFragment();
        dialog.show(getFragmentManager(), "JoinSessionDialogFragment");
    }

    public void onJoinSessionDialogConfirmClick(DialogFragment dialog)
    {
        m_cmRunnable.setMenu("joinSession");
        m_cmRunnable.setDialog(dialog);
        wakeUpCMRunnable();
    }

    public void onJoinSessionDialogCancelClick(DialogFragment dialog)
    {
        // nothing to do
    }

}
