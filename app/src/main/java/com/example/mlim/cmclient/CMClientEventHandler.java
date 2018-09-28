package com.example.mlim.cmclient;

import android.widget.Toast;

import java.util.Iterator;

import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.event.CMDataEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEventHandler;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMClientEventHandler implements CMEventHandler {

    private MainActivity m_mainActivity;
    private CMClientStub m_clientStub;

    CMClientEventHandler(CMClientStub clientStub, MainActivity activity)
    {
        m_mainActivity = activity;
        m_clientStub = clientStub;
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

    private void processSessionEvent(CMEvent cme)
    {
        //long lDelay = 0;
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
                    /*
                    CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();

                    // Change the title of the client window
                    m_client.setTitle("CM Client ["+interInfo.getMyself().getName()+"]");

                    // Set the appearance of buttons in the client frame window
                    m_client.setButtonsAccordingToClientState();
                    */
                }
                break;
            case CMSessionEvent.RESPONSE_SESSION_INFO:
                //lDelay = System.currentTimeMillis() - m_lStartTime;
                //printMessage("RESPONSE_SESSION_INFO delay: "+lDelay+" ms.\n");
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
        m_mainActivity.printMessageln("received a CM user event.");
    }

    private void processFileEvent(CMEvent cme)
    {
        m_mainActivity.printMessageln("received a CM file event.");
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