package com.example.mlim.cmclient;

import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.event.CMDataEvent;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEventHandler;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEventField;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMClientEventHandler implements CMEventHandler {

    private MainActivity m_mainActivity;
    private CMClientStub m_clientStub;
    private long m_lStartTime;
    private boolean m_bDistFileProc;
    private boolean m_bReqAttachedFile;
    private long m_lDelaySum;

    CMClientEventHandler(CMClientStub clientStub, MainActivity activity)
    {
        m_mainActivity = activity;
        m_clientStub = clientStub;
        m_lStartTime = 0;
        m_bDistFileProc = false;
        m_bReqAttachedFile = false;
        m_lDelaySum = 0;
    }

    // event handling method
    // event comes from the CM.
    public void processEvent(CMEvent cme)
    {
        switch(cme.getType())
        {
            case CMInfo.CM_SESSION_EVENT:
                processSessionEvent(cme);
                break;
            case CMInfo.CM_INTEREST_EVENT:
                processInterestEvent(cme);
                break;
            case CMInfo.CM_DATA_EVENT:
                processDataEvent(cme);
                break;
            case CMInfo.CM_DUMMY_EVENT:
                processDummyEvent(cme);
                break;
            case CMInfo.CM_USER_EVENT:
                processUserEvent(cme);
                break;
            case CMInfo.CM_FILE_EVENT:
                processFileEvent(cme);
                break;
            case CMInfo.CM_SNS_EVENT:
                processSNSEvent(cme);
                break;
            case CMInfo.CM_MULTI_SERVER_EVENT:
                processMultiServerEvent(cme);
                break;
            default:
                return;
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    // get/set methods

    public void setStartTime(long time)
    {
        m_lStartTime = time;
    }

    public long getStartTime()
    {
        return m_lStartTime;
    }

    public void setDistFileProc(boolean b)
    {
        m_bDistFileProc = b;
    }

    public boolean isDistFileProc()
    {
        return m_bDistFileProc;
    }

    public void setReqAttachedFile(boolean bReq)
    {
        m_bReqAttachedFile = bReq;
    }

    public boolean isReqAttachedFile()
    {
        return m_bReqAttachedFile;
    }

    //////////////////////////////////////////////////////////////////////////////

    private void processSessionEvent(CMEvent cme)
    {
        long lDelay = 0;
        CMSessionEvent se = (CMSessionEvent)cme;
        switch(se.getID())
        {
            case CMSessionEvent.LOGIN_ACK:
                //lDelay = System.currentTimeMillis() - m_lStartTime;
                //m_mainActivity.printMessage("LOGIN_ACK delay: "+lDelay+" ms.\n");
                if(se.isValidUser() == 0)
                {
                    m_mainActivity.printMessage("This client fails authentication by the default server!\n");
                    m_mainActivity.toastMessage("사용자 인증 실패!", Toast.LENGTH_SHORT);
                }
                else if(se.isValidUser() == -1)
                {
                    m_mainActivity.printMessage("This client is already in the login-user list!\n");
                    m_mainActivity.toastMessage("이미 로그인중!", Toast.LENGTH_SHORT);
                }
                else
                {
                    m_mainActivity.printMessage("This client successfully logs in to the default server.\n");
                    m_mainActivity.toastMessage("로그인 성공!", Toast.LENGTH_SHORT);
                    CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();

                    // Change the title of the client window
                    m_mainActivity.setActivityTitle(m_mainActivity.getString(R.string.app_name)+
                            " ["+interInfo.getMyself().getName()+"]");
                }
                break;
            case CMSessionEvent.RESPONSE_SESSION_INFO:
                lDelay = System.currentTimeMillis() - m_lStartTime;
                m_mainActivity.printMessage("RESPONSE_SESSION_INFO delay: "+lDelay+" ms.\n");
                processRESPONSE_SESSION_INFO(se);
                break;
            case CMSessionEvent.SESSION_TALK:
                m_mainActivity.printMessage("("+se.getHandlerSession()+")\n");
                m_mainActivity.printMessage("<"+se.getUserName()+">: "+se.getTalk()+"\n");
                break;
            case CMSessionEvent.JOIN_SESSION_ACK:
                //lDelay = System.currentTimeMillis() - m_lStartTime;
                //printMessage("JOIN_SESSION_ACK delay: "+lDelay+" ms.\n");
                //m_client.setButtonsAccordingToClientState();
                break;
            case CMSessionEvent.ADD_NONBLOCK_SOCKET_CHANNEL_ACK:
                if(se.getReturnCode() == 0)
                {
                    m_mainActivity.printMessage("Adding a nonblocking SocketChannel("+se.getChannelName()+","+se.getChannelNum()
                            +") failed at the server!\n");
                }
                else
                {
                    m_mainActivity.printMessage("Adding a nonblocking SocketChannel("+se.getChannelName()+","+se.getChannelNum()
                            +") succeeded at the server!\n");
                }
                break;
            case CMSessionEvent.ADD_BLOCK_SOCKET_CHANNEL_ACK:
                //lDelay = System.currentTimeMillis() - m_lStartTime;
                //printMessage("ADD_BLOCK_SOCKET_CHANNEL_ACK delay: "+lDelay+" ms.\n");
                if(se.getReturnCode() == 0)
                {
                    m_mainActivity.printMessage("Adding a blocking socket channel ("+se.getChannelName()+","+se.getChannelNum()
                            +") failed at the server!\n");
                }
                else
                {
                    m_mainActivity.printMessage("Adding a blocking socket channel("+se.getChannelName()+","+se.getChannelNum()
                            +") succeeded at the server!\n");
                }
                break;
            case CMSessionEvent.REMOVE_BLOCK_SOCKET_CHANNEL_ACK:
                //lDelay = System.currentTimeMillis() - m_lStartTime;
                //printMessage("REMOVE_BLOCK_SOCKET_CHANNEL_ACK delay: "+lDelay+" ms.\n");
                if(se.getReturnCode() == 0)
                {
                    m_mainActivity.printMessage("Removing a blocking socket channel ("+se.getChannelName()+","+se.getChannelNum()
                            +") failed at the server!\n");
                }
                else
                {
                    m_mainActivity.printMessage("Removing a blocking socket channel("+se.getChannelName()+","+se.getChannelNum()
                            +") succeeded at the server!\n");
                }
                break;
            case CMSessionEvent.REGISTER_USER_ACK:
                if( se.getReturnCode() == 1 )
                {
                    // user registration succeeded
                    m_mainActivity.printMessage("User["+se.getUserName()+"] successfully registered at time["
                            +se.getCreationTime()+"].\n");
                }
                else
                {
                    // user registration failed
                    m_mainActivity.printMessage("User["+se.getUserName()+"] failed to register!\n");
                }
                break;
            case CMSessionEvent.DEREGISTER_USER_ACK:
                if( se.getReturnCode() == 1 )
                {
                    // user deregistration succeeded
                    m_mainActivity.printMessage("User["+se.getUserName()+"] successfully deregistered.\n");
                }
                else
                {
                    // user registration failed
                    m_mainActivity.printMessage("User["+se.getUserName()+"] failed to deregister!\n");
                }
                break;
            case CMSessionEvent.FIND_REGISTERED_USER_ACK:
                if( se.getReturnCode() == 1 )
                {
                    m_mainActivity.printMessage("User profile search succeeded: user["+se.getUserName()
                            +"], registration time["+se.getCreationTime()+"].\n");
                }
                else
                {
                    m_mainActivity.printMessage("User profile search failed: user["+se.getUserName()+"]!\n");
                }
                break;
            case CMSessionEvent.UNEXPECTED_SERVER_DISCONNECTION:
                /*
                m_client.printStyledMessage("Unexpected disconnection from the default server!\n", "bold");
                m_client.setButtonsAccordingToClientState();
                m_client.setTitle("CM Client");
                */
                m_mainActivity.printMessageln("Unexpected disconnection from the default server!");
                m_mainActivity.setActivityTitle("CM Client");
                break;
            default:
                return;
        }
    }

    private void processRESPONSE_SESSION_INFO(CMSessionEvent se)
    {
        Iterator<CMSessionInfo> iter = se.getSessionInfoList().iterator();

        m_mainActivity.printMessage(String.format("%-60s%n", "------------------------------------------------------------"));
        m_mainActivity.printMessage(String.format("%-20s%-20s%-10s%-10s%n", "name", "address", "port", "user num"));
        m_mainActivity.printMessage(String.format("%-60s%n", "------------------------------------------------------------"));

        while(iter.hasNext())
        {
            CMSessionInfo tInfo = iter.next();
            m_mainActivity.printMessage(String.format("%-20s%-20s%-10d%-10d%n", tInfo.getSessionName(), tInfo.getAddress(),
                    tInfo.getPort(), tInfo.getUserNum()));
        }
    }

    private void processInterestEvent(CMEvent cme)
    {
        CMInterestEvent ie = (CMInterestEvent) cme;
        switch(ie.getID())
        {
            case CMInterestEvent.USER_TALK:
                m_mainActivity.printMessage("("+ie.getHandlerSession()+", "+ie.getHandlerGroup()+")\n");
                m_mainActivity.printMessage("<"+ie.getUserName()+">: "+ie.getTalk()+"\n");
                break;
            default:
                return;
        }
    }

    private void processDataEvent(CMEvent cme)
    {
        CMDataEvent de = (CMDataEvent) cme;
        switch(de.getID())
        {
            case CMDataEvent.NEW_USER:
                m_mainActivity.printMessage("["+de.getUserName()+"] enters group("+de.getHandlerGroup()+") in session("
                        +de.getHandlerSession()+").\n");
                break;
            case CMDataEvent.REMOVE_USER:
                m_mainActivity.printMessage("["+de.getUserName()+"] leaves group("+de.getHandlerGroup()+") in session("
                        +de.getHandlerSession()+").\n");
                break;
            default:
                return;
        }
    }

    private void processDummyEvent(CMEvent cme)
    {
        m_mainActivity.printMessageln("received a CM dummy event.");
    }

    private void processUserEvent(CMEvent cme)
    {
        int id = -1;
        long lSendTime = 0;
        int nSendNum = 0;

        CMUserEvent ue = (CMUserEvent) cme;

        if(ue.getStringID().equals("testForward"))
        {
            id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
            m_mainActivity.printMessage("Received user event \'testForward\', id: "+id+"\n");
        }
        else if(ue.getStringID().equals("testNotForward"))
        {
            id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
            m_mainActivity.printMessage("Received user event 'testNotForward', id("+id+")\n");
        }
        else if(ue.getStringID().equals("testForwardDelay"))
        {
            id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
            lSendTime = Long.parseLong(ue.getEventField(CMInfo.CM_LONG, "stime"));
            long lDelay = System.currentTimeMillis() - lSendTime;
            m_lDelaySum += lDelay;
            m_mainActivity.printMessage("Received user event 'testNotForward', id("+id+"), delay("+lDelay+"), delay_sum("+m_lDelaySum+")\n");
        }
        else if(ue.getStringID().equals("EndForwardDelay"))
        {
            nSendNum = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "sendnum"));
            m_mainActivity.printMessage("Received user envet 'EndForwardDelay', avg delay("+m_lDelaySum/nSendNum+" ms)\n");
            m_lDelaySum = 0;
        }
        else if(ue.getStringID().equals("repRecv"))
        {
            String strReceiver = ue.getEventField(CMInfo.CM_STR, "receiver");
            int nBlockingChannelType = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "chType"));
            int nBlockingChannelKey = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "chKey"));
            int nRecvPort = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "recvPort"));
            int opt = -1;
            if(nBlockingChannelType == CMInfo.CM_SOCKET_CHANNEL)
                opt = CMInfo.CM_STREAM;
            else if(nBlockingChannelType == CMInfo.CM_DATAGRAM_CHANNEL)
                opt = CMInfo.CM_DATAGRAM;

            CMDummyEvent due = new CMDummyEvent();
            due.setDummyInfo("This is a test message to test a blocking channel");
            System.out.println("Sending a dummy event to ("+strReceiver+")..");

            if(opt == CMInfo.CM_STREAM)
                m_clientStub.send(due, strReceiver, opt, nBlockingChannelKey, true);
            else if(opt == CMInfo.CM_DATAGRAM)
                m_clientStub.send(due, strReceiver, opt, nBlockingChannelKey, nRecvPort, true);
            else
                System.err.println("invalid sending option!: "+opt);
        }
        else if(ue.getStringID().equals("testSendRecv"))
        {
            m_mainActivity.printMessage("Received user event from ["+ue.getSender()+"] to ["+ue.getReceiver()+
                    "], (id, "+ue.getID()+"), (string id, "+ue.getStringID()+")\n");

            if(!m_clientStub.getMyself().getName().equals(ue.getReceiver()))
                return;

            CMUserEvent rue = new CMUserEvent();
            rue.setID(222);
            rue.setStringID("testReplySendRecv");
            boolean ret = m_clientStub.send(rue, ue.getSender());
            if(ret)
                m_mainActivity.printMessage("Sent reply event: (id, "+rue.getID()+"), (string id, "+rue.getStringID()+")\n");
            else
                m_mainActivity.printMessage("Failed to send the reply event!\n");
        }
        else if(ue.getStringID().equals("testCastRecv"))
        {
            m_mainActivity.printMessage("Received user event from ["+ue.getSender()+"], to session["+
                    ue.getEventField(CMInfo.CM_STR, "Target Session")+"] and group["+
                    ue.getEventField(CMInfo.CM_STR,  "Target Group")+"], (id, "+ue.getID()+
                    "), (string id, "+ue.getStringID()+")\n");
            CMUserEvent rue = new CMUserEvent();
            rue.setID(223);
            rue.setStringID("testReplyCastRecv");
            boolean ret = m_clientStub.send(rue, ue.getSender());
            if(ret)
                m_mainActivity.printMessage("Sent reply event: (id, "+rue.getID()+"), (sting id, "+rue.getStringID()+")\n");
            else
                m_mainActivity.printMessage("Failed to send the reply event!\n");
        }
        else
        {
            m_mainActivity.printMessage("CMUserEvent received from ["+ue.getSender()+"], strID("+ue.getStringID()+")\n");
            m_mainActivity.printMessage(String.format("%-5s%-20s%-10s%-20s%n", "Type", "Field", "Length", "Value"));
            m_mainActivity.printMessage("-----------------------------------------------------\n");
            Iterator<CMUserEventField> iter = ue.getAllEventFields().iterator();
            while(iter.hasNext())
            {
                CMUserEventField uef = iter.next();
                if(uef.nDataType == CMInfo.CM_BYTES)
                {
                    m_mainActivity.printMessage(String.format("%-5s%-20s%-10d", uef.nDataType, uef.strFieldName,
                            uef.nValueByteNum));
                    for(int i = 0; i < uef.nValueByteNum; i++)
                    {
                        //not yet
                    }
                    m_mainActivity.printMessage("\n");
                }
                else
                {
                    m_mainActivity.printMessage(String.format("%-5d%-20s%-10d%-20s%n", uef.nDataType, uef.strFieldName,
                            uef.strFieldValue.length(), uef.strFieldValue));
                }
            }
        }
        return;
    }

    private void processFileEvent(CMEvent cme)
    {
        CMFileEvent fe = (CMFileEvent) cme;
        long lDelay = 0;

        switch(fe.getID())
        {
            case CMFileEvent.REQUEST_FILE_TRANSFER:
            case CMFileEvent.REQUEST_FILE_TRANSFER_CHAN:
                m_mainActivity.printMessage("["+fe.getReceiverName()+"] requests file("+fe.getFileName()+").\n");
                break;
            case CMFileEvent.REPLY_FILE_TRANSFER:
            case CMFileEvent.REPLY_FILE_TRANSFER_CHAN:
                if(fe.getReturnCode() == 0)
                {
                    m_mainActivity.printMessage("["+fe.getFileName()+"] does not exist in the owner!\n");
                }
                break;
            case CMFileEvent.START_FILE_TRANSFER:
            case CMFileEvent.START_FILE_TRANSFER_CHAN:
                m_mainActivity.printMessage("["+fe.getSenderName()+"] is about to send file("+fe.getFileName()+").\n");
                break;
            case CMFileEvent.END_FILE_TRANSFER:
            case CMFileEvent.END_FILE_TRANSFER_CHAN:
                m_mainActivity.printMessage("["+fe.getSenderName()+"] completes to send file("+fe.getFileName()+", "
                        +fe.getFileSize()+" Bytes).\n");
                lDelay = System.currentTimeMillis() - m_lStartTime;
                m_mainActivity.printMessage("file-transfer delay: "+lDelay+" ms.\n");

                if(m_bDistFileProc)
                    processFile(fe.getFileName());
                if(m_bReqAttachedFile)
                {
                    // need to be modified for Android platform
                    /*
                    CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
                    String strPath = confInfo.getTransferedFileHome().toString() + File.separator + fe.getFileName();
                    File file = new File(strPath);
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    */
                    m_bReqAttachedFile = false;
                }
                break;
            case CMFileEvent.CANCEL_FILE_SEND:
            case CMFileEvent.CANCEL_FILE_SEND_CHAN:
                m_mainActivity.printMessage("["+fe.getSenderName()+"] cancelled the file transfer.\n");
                break;
            case CMFileEvent.CANCEL_FILE_RECV_CHAN:
                m_mainActivity.printMessage("["+fe.getReceiverName()+"] cancelled the file request.\n");
                break;
        }
        return;
    }

    private void processFile(String strFile)
    {
        m_mainActivity.printMessage("Not yet supported in Android client!");
        return;
    }

    private void processSNSEvent(CMEvent cme)
    {
        m_mainActivity.printMessageln("received a CM SNS event.");
    }

    private void processMultiServerEvent(CMEvent cme)
    {
        m_mainActivity.printMessageln("received a CM multi-server event.");
    }
}
