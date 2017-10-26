package com.goboomtown.microconnect.chat;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.sasl.provided.SASLDigestMD5Mechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author lborsato created on 2015-12-11
 * @author fbeachler
 */
public class BoomtownChat {
    private static BoomtownChat shared_instance = null;

    private BoomtownChatListener listener;
    public Activity context;

    private String  jid;
    private String  password;
    private String  host;
    private String  port;
    public  String  roomJid;
    private String  senderId;
    private String  resource;

    private XMPPTCPConnectionConfiguration  config;
    private XMPPTCPConnection               connection;
    private ChatManager                     chatManager;
    private MultiUserChatManager            roomManager;
    private MessageListener                 messageListener;
    private MultiUserChat room;

    public Map<String, Object>              participants_eligible;
//    public Map<String, Object>              chatHistory;
    public ArrayList<BoomtownChatMessage>   roomHistory;
    BoomtownChatMessage chatMessage;
    private boolean firstUse;
    public HashMap<String, Object> avatars;

    public interface BoomtownChatListener {
        public void onConnect();
        public void onTimeoutConnect();
        public void onNotAuthenticate();
        public void onDisconnect();
        public void onReceiveMessage(BoomtownChatMessage message);
        public void onJoinRoom();
        public void onJoinRoomNoResponse();
        public void onJoinRoomFailed(String reason);
    }

    public static BoomtownChat sharedInstance(){
        if( shared_instance == null){
            shared_instance = new BoomtownChat();
        }
        return shared_instance;
    }

    public void setListener(BoomtownChatListener listener) {
        this.listener = listener;
    }

    public void connectToServerWithJid(String jabberID, String jabberPassword, String host, int port, int timeout)
    {
        firstUse = false;

        messageListener = new BoomtownMessageListener();

        SmackConfiguration.setDefaultPacketReplyTimeout(30000);
        config = XMPPTCPConnectionConfiguration.builder()
                .setServiceName(host)
                .setPort(port)
                .setUsernameAndPassword(jabberID, jabberPassword)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled).setDebuggerEnabled(true)
                .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.required )
                .setCompressionEnabled(false).build();
        SASLMechanism mechanism = new SASLDigestMD5Mechanism();
        SASLAuthentication.registerSASLMechanism(mechanism);
        SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
        SASLAuthentication.unBlacklistSASLMechanism("DIGEST-MD5");
        connection = new XMPPTCPConnection(config);
        connection.addConnectionListener(new AbstractConnectionListener() {
            @Override
            public void connected(XMPPConnection connection)
            {
                chatManager = ChatManager.getInstanceFor(connection);
                sendHistory();
                if ( listener != null )
                    listener.onConnect();
            }

            @Override
            public void connectionClosed() {

                System.out.println("uhoh connectionClosed");
            }

            @Override
            public void connectionClosedOnError(Exception e) {

                System.out.println("uhoh connectionClosedOnError");
            }

            @Override
            public void reconnectingIn(int i) {
                if(i < 4){
                    //TODO notify
                }
            }

            @Override
            public void reconnectionSuccessful() {

                System.out.println("uhoh reconnectionSuccessful");
            }

            @Override
            public void reconnectionFailed(Exception e) {

                System.out.println("uhoh reconnectionFailed");
            }
        });
        try {
            if ( connection!=null && connection.isConnected() )
                return;

            connection.connect();
            connection.login(jabberID, jabberPassword);
            Presence presence = new Presence(Presence.Type.available);
//            connection.sendPacket(presence);
            connection.sendStanza(presence);
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    public Boolean isConnected() {
        if ( connection!=null && connection.isConnected() )
            return true;
        else
            return false;
    }


    public void disconnect()
    {
        if ( connection!=null && connection.isConnected() ) {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    connection.disconnect();
               }
            }).start();
        }
    }


    public void joinRoom(String roomJid, String nickname)
    {
        String[] tokens = nickname.split(";");
        senderId = tokens[0];
        roomManager = MultiUserChatManager.getInstanceFor(connection);
        room = roomManager.getMultiUserChat(roomJid);
        room.addMessageListener(messageListener);
        try {
            room.join(senderId);
            if ( listener != null )
                listener.onJoinRoom();
        } catch (SmackException.NoResponseException e) {
            if ( listener != null )
                listener.onJoinRoomNoResponse();
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            if ( listener != null )
                listener.onJoinRoomFailed("XMPP error");
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            if ( listener != null )
                listener.onJoinRoomFailed("Not connected");
            e.printStackTrace();
        }
    }

    public void sendGroupchatMessage(String text, Boolean sendOnly)
    {
        firstUse = false;
        if ( room != null )
            try {
                Message message = new Message();
//                message.setFrom(BoomtownAPI.sharedInstance().currentUser().xmpp_resource);
                message.setFrom(resource);
                Log.d("sendGroupchatMessage", text);
                message.setBody(text);
                message.setStanzaId(java.util.UUID.randomUUID().toString());
                room.sendMessage(message);
                if ( !sendOnly ) {
                    BoomtownChatMessage chatMessage = new BoomtownChatMessage();
                    chatMessage.populateFromMessage(message);
                    chatMessage.from = senderId;
//                chatMessage.id = java.util.UUID.randomUUID().toString();
//                chatMessage.from = senderId;
                    chatMessage.fromName = getParticipantInfo(chatMessage.from, "name");
                    chatMessage.fromMention = getParticipantInfo(chatMessage.from, "alias");
//                chatMessage.date = new Date();
//                chatMessage.dateString = chatMessage.getDateString(chatMessage.date);
//                chatMessage.text = message;
                    addHistoryEntry(chatMessage);
                    if (listener != null)
                        listener.onReceiveMessage(chatMessage);
                }
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
    }


    public boolean addHistoryEntry(BoomtownChatMessage message)
    {
        if ( message.id==null || message.id.length()==0 )
            return false;

        for (BoomtownChatMessage chatMessage : roomHistory )
        {
            if ( message.id!=null && message.id.equalsIgnoreCase(chatMessage.id) )
                return false;
        }
        roomHistory.add(message);
        SettingsStore.getInstance(context).setRoomHistory(roomJid, roomHistory);
        return true;
    }

    public void sendHistory()
    {
        roomHistory = SettingsStore.getInstance(context).roomHistory(roomJid);
        if ( roomHistory.size() == 0 )
            firstUse = true;
        else
            firstUse = false;

        for ( BoomtownChatMessage chatMessage : roomHistory )
        {
            if ( listener != null )
                listener.onReceiveMessage(chatMessage);
        }
    }


    public String getParticipantInfo(String user, String type)
    {
        if ( user == null )
            return "";

        if ( participants_eligible == null )
            return "";

        Map<String, String> participantInfo = (Map<String, String>) participants_eligible.get(user);
        if ( participantInfo == null )
            return user;

        return (String) participantInfo.get(type);

    }


    public static JSONObject extractXmppInformation(String xmppData, String key)
    {
        // example payload, as returned in {{xmpp_data}} from an api/v2 issue response.
        // payload is a base64 encoded string of the IV concatenated with the AES 256 encrypted JSON encoded payload.

        try {
            byte[] data = decrypt(key, xmppData);
            String response = new String(data);
            return new JSONObject(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    /*
    The PHP encryption code:

        $key = substr(sprintf('%032s', $session->getPrivateKey()), 0, 32);
        $result = mcrypt_encrypt(MCRYPT_RIJNDAEL_128, $key, json_encode($xmppData), MCRYPT_MODE_ECB);
        if (!$result) {
            $this->log->error('{}() Error encrypting issue xmpp packet (valid modes={}, valid algos={})', __FUNCTION__, mcrypt_list_modes(), mcrypt_list_algorithms());
        }
        else {
            $item->xmpp_data = base64_encode($result);
        }


        $key = substr(sprintf('%032s', $session->getPrivateKey()), 0, 32);
        $result = mcrypt_encrypt(MCRYPT_RIJNDAEL_128, $key, json_encode($xmppData), MCRYPT_MODE_ECB);
        $item->xmpp_data = base64_encode($result);

     */

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(String key, String data) throws Exception {
        byte[] keyBytes = new byte[32];
        try {
            System.arraycopy(key.getBytes("UTF-8"), 0, keyBytes, 0, keyBytes.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] encrypted = Base64.decode(data, Base64.NO_WRAP);
        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    private static byte[] getKey(String keyString) {
        byte[] keyBytes = new byte[32];
        try {
            System.arraycopy(keyString.getBytes("UTF-8"), 0, keyBytes, 0, keyBytes.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return keyBytes;
    }




    public BoomtownChatMessage processMessageFromString(String message) {
        Log.d("BoomtownChatMessage", "processMessageFromString: "+message);
        chatMessage = new BoomtownChatMessage();
        chatMessage.populateFromString(message);

        chatMessage.fromName = getParticipantInfo(chatMessage.from, "name");
        chatMessage.fromMention = getParticipantInfo(chatMessage.from, "alias");
        if ( chatMessage.fromName == null )
            chatMessage.fromName = chatMessage.from;
        if ( chatMessage.fromMention == null )
            chatMessage.fromMention = chatMessage.from;

//        if ( chatMessage.text == null )
        chatMessage.text = message;

        if ( !chatMessage.postprocessMessage() )
            return null;

        chatMessage.trimText();

        chatMessage.calculateId();
        chatMessage.self = true;

        if ( chatMessage.self ) {
            addHistoryEntry(chatMessage);
            if (listener != null)
                listener.onReceiveMessage(chatMessage);
        }
        return chatMessage;
    }


    class BoomtownMessageListener implements MessageListener
    {
        public void processMessage(Message message) {
//            if ( message.getStanzaId()==null || message.getStanzaId().length()==0 )
//                return;

            if ( message.getBody() == null )
                return;

            chatMessage = new BoomtownChatMessage();
            chatMessage.populateFromMessage(message);

            chatMessage.fromName = getParticipantInfo(chatMessage.from, "name");
            chatMessage.fromMention = getParticipantInfo(chatMessage.from, "alias");
            if ( chatMessage.fromName == null )
                chatMessage.fromName = chatMessage.from;
            if ( chatMessage.fromMention == null )
                chatMessage.fromMention = chatMessage.from;

            Log.d("Listener processMessage", String.format("first_user %d from %s senderId %s text %s",
                    firstUse?1:0, chatMessage.from, senderId, chatMessage.text));

            if ( !chatMessage.postprocessMessage() )
                return;

            chatMessage.trimText();

            chatMessage.calculateId();

            if ( firstUse || (!firstUse && !chatMessage.from.equalsIgnoreCase(senderId)) ) {
                if ( addHistoryEntry(chatMessage) ) {
                    if (listener != null)
                        listener.onReceiveMessage(chatMessage);
                }
            }
        }
    }

}


