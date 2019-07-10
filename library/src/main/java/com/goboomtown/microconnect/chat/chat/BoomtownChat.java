package com.goboomtown.microconnect.chat.chat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.goboomtown.microconnect.R;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.sasl.provided.SASLDigestMD5Mechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.MucEnterConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by larry on 2015-12-11.
 */
public class BoomtownChat {
    public static final String TAG = BoomtownChat.class.getSimpleName();
    public static final String ALGO_AES = "AES/ZeroBytePadding";
    public static final String UTF_8 = "UTF-8";

    private static final String PREFS                   = "com.goboomtown.Settings.Prefs";
    private static final String SETTINGS_CHATHISTORY    = "com.goboomtown.ChatHistory.";

    private static BoomtownChat shared_instance = null;

    private BoomtownChatListener listener;
    public Activity context;

    private String jid;
    private String password;
    private String host;
    private String port;
    public String roomJid;
    private String senderId;
    private String resource;

    private XMPPTCPConnectionConfiguration.Builder config;
    private XMPPTCPConnection connection;
    private ChatManager chatManager;
    private MultiUserChatManager roomManager;
    private MessageListener messageListener;
    private MultiUserChat room;

    public Object userPermissions;
    public Map<String, Object> participants_eligible;
    public List<BoomtownChatMessage> roomHistory;
    BoomtownChatMessage chatMessage;

    public String avatarRetrievalAPIBaseURL;

    /**
     * supposed to flag when to send history vs when to process msgs from cloud
     * doesn't seem to work anymore if it ever did
     */
    @Deprecated
    private boolean firstUse;

    public HashMap<String, Object> avatars;

    public interface BoomtownChatListener {
        public void onConnect();

        public void onConnectError();

        public void onNotAuthenticate();

        public void onDisconnect(Exception e);

        public void onReceiveMessage(BoomtownChatMessage message);

        public void onJoinRoom();

        public void onJoinRoomNoResponse();

        public void onJoinRoomFailed(String reason);
    }

    public static BoomtownChat sharedInstance() {
        if (shared_instance == null) {
            shared_instance = new BoomtownChat();
        }
        return shared_instance;
    }

    public void setListener(BoomtownChatListener listener) {
        this.listener = listener;
    }

    public static String sanitizeJabberId(final String jabberId) {
        if (jabberId == null) {
            return null;
        }
        List<String> parts = Arrays.asList(jabberId.split("@"));
        return parts.get(0);
    }

    public void connectToServerWithJid(String jabberID, String jabberPassword, String host, int port, int timeout) {
        firstUse = false;

        messageListener = new BoomtownMessageListener();

        password = jabberPassword;

        SmackConfiguration.setDefaultReplyTimeout(30000);
        try {
            InetAddress addy = Inet4Address.getByName(host);
            DomainBareJid serviceName = JidCreate.domainBareFrom(host);
            config = XMPPTCPConnectionConfiguration.builder()
                    .setHostAddress(addy)
                    .setPort(port)
                    .setXmppDomain(serviceName)
                    .setUsernameAndPassword(jabberID, jabberPassword)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .enableDefaultDebugger()
                    .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.required)
                    .setCompressionEnabled(false);
        } catch (Exception e) {
            Log.w(TAG, "exception building chat config, aborting\n" + Log.getStackTraceString(e));
            if (listener != null) {
                listener.onConnectError();
            }
        }
        SASLMechanism mechanism = new SASLDigestMD5Mechanism();
        SASLAuthentication.registerSASLMechanism(mechanism);
        SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
        SASLAuthentication.unBlacklistSASLMechanism("DIGEST-MD5");
        SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
        connection = new XMPPTCPConnection(config.build());
        connection.addConnectionListener(new AbstractConnectionListener() {
            @Override
            public void connected(XMPPConnection connection) {
                Log.i(TAG, "chat connected");
                if (listener != null) {
                    listener.onConnect();
                }
            }

            @Override
            public void connectionClosed() {
                Log.i(TAG, "chat connection closed");
                if (listener != null) {
                    listener.onDisconnect(null);
                }
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                Log.i(TAG, "- chat connection closed with error");
                if (listener != null) {
                    listener.onDisconnect(e);
                }
            }

            @Override
            public void authenticated(XMPPConnection connection, boolean resumed) {
                // noop
            }
        });
        try {
            connection.connect();
            chatManager = ChatManager.getInstanceFor(connection);
        } catch (SmackException.AlreadyConnectedException e) {
            // FIXME we should have already have chat manager instance, probably shouldn't be here
            Log.w(TAG, Log.getStackTraceString(e));
            return;
        }
        catch ( SmackException.NoResponseException e ) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        catch (IllegalArgumentException | InterruptedException | SmackException | IOException | XMPPException e) {
            Log.w(TAG, Log.getStackTraceString(e));
            if (listener != null) {
                listener.onConnectError();
            }
            return;
        }
        Presence presence = null;
        try {
            connection.login(jabberID, jabberPassword);
            presence = new Presence(Presence.Type.available);
            connection.sendStanza(presence);
        } catch (SmackException.NotConnectedException e) {
            Log.w(TAG, Log.getStackTraceString(e));
            if (listener != null) {
                String msg = "";
                if (presence != null) {
                    msg = context.getString(R.string.unable_to_send_presence);
                } else {
                    msg = context.getString(R.string.conn_failed_before_login);
                }
                listener.onJoinRoomFailed(msg);
                return;
            }
        } catch (SmackException.AlreadyLoggedInException e) {
            Log.w(TAG, Log.getStackTraceString(e));
            return;
        } catch (IllegalArgumentException | InterruptedException | SmackException | IOException | XMPPException e) {
            Log.w(TAG, Log.getStackTraceString(e));
            if (listener != null) {
                listener.onJoinRoomFailed(e.getMessage());
            }
            return;
        }
    }

    public List<BoomtownChatMessage> roomHistory(String roomJid) {
        BoomtownChatMessage message;
        List<BoomtownChatMessage> history = new ArrayList<>();
//        roomHistory = new ArrayList<>();
        SharedPreferences prefs = this.context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String historyString = prefs.getString(SETTINGS_CHATHISTORY + roomJid, null);
        if (historyString != null) {
            try {
                JSONArray jsonArray = new JSONArray(historyString);
                List<String> historyEntries = (ArrayList<String>) JSONHelper.toList(jsonArray);
                for (String historyEntry : historyEntries) {
                    message = new BoomtownChatMessage();
                    message.populateFromString(new JSONObject(historyEntry));
                    history.add(message);
                }
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        roomHistory = new ArrayList<BoomtownChatMessage>(history);
        return history;
    }

    public void setRoomHistory(String roomJid, List<BoomtownChatMessage> history) {
        SharedPreferences prefs = this.context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        Object jsonHistory = null;
        try {
            jsonHistory = JSONHelper.toJSON(history);
            ed.putString(SETTINGS_CHATHISTORY + roomJid, jsonHistory.toString());
            ed.commit();
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }


    public Boolean isConnected() {
        return connection != null && connection.isConnected();
    }


    public void disconnect() {
        if (room != null && room.isJoined()) {
            try {
                room.leave();
            } catch (Exception e) {
                Log.w(TAG, Log.getStackTraceString(e));
            }
            room = null;
        }
        if (connection != null && connection.isConnected()) {
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connection.disconnect();
                        } catch (Exception e) {
                            Log.w(TAG, Log.getStackTraceString(e));
                        }
                    }
                }).start();
            } catch (Exception e) {
                Log.w(TAG, Log.getStackTraceString(e));
            }
        }
    }


    public Boolean isInRoom() {
        return (room != null && room.isJoined());
    }


    public void joinRoom(String roomName, String nickname) {
        if (!connection.isConnected()) {
            Log.w(TAG, "attempt to join room while disconnected");
            if (listener != null) {
                listener.onJoinRoomFailed(context.getString(R.string.chat_not_connected));
            }
            return;
        }
        if (room != null) {
            if (room.isJoined() && roomJid != null && !roomJid.equals(roomName)) {
                try {
                    room.leave();
                } catch (InterruptedException | SmackException.NotConnectedException e) {
                    // noop
                }
            }
        }
        roomJid = roomName;
        EntityBareJid jid = null;
        try {
            jid = JidCreate.entityBareFrom(roomName);
        } catch (XmppStringprepException e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        String[] tokens = nickname.split(";");
        senderId = tokens[0];
        roomManager = MultiUserChatManager.getInstanceFor(connection);
        room = roomManager.getMultiUserChat(jid);
        room.addMessageListener(messageListener);
        try {
            Resourcepart rp = Resourcepart.from(senderId);
            //  A much more versatile (and poorly documented) way to enter a room with history
            MucEnterConfiguration.Builder builder = room.getEnterConfigurationBuilder(rp);
            builder.requestMaxStanzasHistory(50);
            MucEnterConfiguration mucEnterConfiguration = builder.build();
            room.join(mucEnterConfiguration);
            if (listener != null) {
                listener.onJoinRoom();
            }
            Log.i(TAG, "room join success (" + roomName + ") - (" + senderId + "),(" + roomJid + ")");
        } catch (SmackException.NoResponseException e) {
            if (listener != null && room != null && !room.isJoined()) {
                listener.onJoinRoomNoResponse();
            }
            Log.w(TAG, Log.getStackTraceString(e));
        } catch (XMPPException.XMPPErrorException e) {
            if (listener != null && room != null && !room.isJoined()) {
                listener.onJoinRoomFailed("XMPP error");
            }
            Log.w(TAG, Log.getStackTraceString(e));
        } catch (SmackException.NotConnectedException e) {
            if (listener != null && room != null && !room.isJoined()) {
                listener.onJoinRoomFailed("Not connected");
            }
            Log.w(TAG, Log.getStackTraceString(e));
        } catch (MultiUserChatException.NotAMucServiceException | XmppStringprepException | InterruptedException | NullPointerException e) {
            if (listener != null && room != null && !room.isJoined()) {
                listener.onJoinRoomFailed("Unexpected error");
            }
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    public void sendGroupchatMessage(String text, Boolean sendOnly) {
        Log.d(TAG, "#sendGroupchatMessage room (" + room + "): " + text);
        firstUse = false;
        if (room != null) {
            try {
                Message message = new Message();
                if (text.startsWith("{\"")) {
                    message.setType(Message.Type.groupchat);
                }
                // message.setFrom(resource);
                message.setBody(text);
                message.setStanzaId(java.util.UUID.randomUUID().toString());
                room.sendMessage(message);
            } catch (InterruptedException | SmackException.NotConnectedException e) {
                Log.w(TAG, "unable to send chat message, exception:\n" + Log.getStackTraceString(e));
            }
        }
    }

    public boolean isDuplicateMessage(BoomtownChatMessage message) {
        for (BoomtownChatMessage chatMessage : roomHistory) {
            if ( message.hash.equalsIgnoreCase(chatMessage.hash) ) {
                return true;
            }
        }
        return false;
    }


    public boolean addHistoryEntry(BoomtownChatMessage message) {
        boolean added = false;
        if (roomHistory == null) {
            roomHistory = new ArrayList<>();
        }
        int rhSizeOrig = roomHistory.size();
//        if (message.id != null && message.id.length() > 0 && !roomHistory.contains(message)) {
            added = true;
            roomHistory.add(message);
//        } else {
//            Log.v(TAG, "message already exists in history: " + message.toString());
//        }
        if (roomHistory.size() > rhSizeOrig) {
            setRoomHistory(roomJid, roomHistory);
        }
        return added;
    }

    /**
     * Get the room history and send each message to {@link this#listener} to be handled by
     * {@link BoomtownChatListener#onReceiveMessage(BoomtownChatMessage)}.
     */
    public void sendHistory() {
        roomHistory = roomHistory(roomJid);
        if (roomHistory.size() == 0)
            firstUse = true;
        else
            firstUse = false;

        for (BoomtownChatMessage chatMessage : roomHistory) {
            if (listener != null)
                listener.onReceiveMessage(chatMessage);
        }
    }


    public String getParticipantInfo(String user, String type) {
        if (user == null)
            return "";

        if (participants_eligible == null)
            return "";

        Map<String, String> participantInfo = (Map<String, String>) participants_eligible.get(user);
        if (participantInfo == null)
            return user;

        return (String) participantInfo.get(type);

    }


    public Boolean isParticipant(String user) {
        if (user != null && participants_eligible != null) {
            if (participants_eligible.containsKey(user)) {
                return true;
            }
        }
        return false;
    }


    public static JSONObject extractXmppInformation(String xmppData, String key) {
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

    /**
     * Encrypt message.  The PHP encryption code is in boomtown(core)/includes/resources/api_v2_service#exportIssue@1567-1587
     *
     * @param raw
     * @param clear
     * @return
     * @throws Exception
     */
    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, ALGO_AES);
        Cipher cipher = Cipher.getInstance(ALGO_AES);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(String key, String data) throws Exception {
        byte[] keyBytes = new byte[32];
        try {
            System.arraycopy(key.getBytes(UTF_8), 0, keyBytes, 0, keyBytes.length);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        byte[] encrypted = Base64.decode(data, Base64.NO_WRAP);
        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, ALGO_AES);
        Cipher cipher = Cipher.getInstance(ALGO_AES);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    private static byte[] getKey(String keyString) {
        byte[] keyBytes = new byte[32];
        try {
            System.arraycopy(keyString.getBytes(UTF_8), 0, keyBytes, 0, keyBytes.length);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return keyBytes;
    }


    public class BoomtownMessageListener implements MessageListener {
        public final String TAG = BoomtownMessageListener.class.getSimpleName();

        public static final String NS_JABBER_ORG_PROTOCOL_CHATSTATES = "http://jabber.org/protocol/chatstates";

        public void processMessage(Message message) {
            if (Message.Type.groupchat.equals(message.getType())) {
                ExtensionElement chatState = message.getExtension("gone", NS_JABBER_ORG_PROTOCOL_CHATSTATES);
                if (chatState != null) {
                    return;
                }
                chatState = message.getExtension("composing", NS_JABBER_ORG_PROTOCOL_CHATSTATES);
                if (chatState != null) {
                    return;
                }
            }
            if (message.getBody() == null) {
                return;
            }

            chatMessage = new BoomtownChatMessage();
            chatMessage.populateFromMessage(message);

            chatMessage.fromName = getParticipantInfo(chatMessage.from, "name");
            chatMessage.fromMention = getParticipantInfo(chatMessage.from, "alias");
            if (chatMessage.fromName == null) {
                chatMessage.fromName = chatMessage.from;
            }
            if (chatMessage.fromMention == null) {
                chatMessage.fromMention = chatMessage.from;
            }

            Log.v(TAG, String.format("listener #processMessage first_use %d from %s senderId %s text %s",
                    firstUse ? 1 : 0, chatMessage.from, senderId, chatMessage.dispMessage.toString()));

            Boolean isValidMessage = chatMessage.postprocessMessage();
            if (!isValidMessage) {
                return;
            }

            if ( isDuplicateMessage(chatMessage) ) {
                Log.d(TAG, "Found duplicate message: " + chatMessage.text);
                return;
            }

            chatMessage.determineMessageType();

            if (isParticipant(chatMessage.from)) {
                chatMessage.trimText();
//                chatMessage.calculateId();
            }

            addHistoryEntry(chatMessage);

            if (listener != null) {
                listener.onReceiveMessage(chatMessage);
            }
        }
    }
}
