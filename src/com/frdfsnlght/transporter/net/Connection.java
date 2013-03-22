/*
 * Copyright 2011 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.transporter.net;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.frdfsnlght.transporter.Global;
import com.frdfsnlght.transporter.Server;
import com.frdfsnlght.transporter.Servers;
import com.frdfsnlght.transporter.TypeMap;
import com.frdfsnlght.transporter.Utils;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Connection {

    private static final int HANDSHAKE_TIMEOUT = 5000;
    private static final int PROTOCOL_VERSION = 8;
    public static final int PROTOCOL_TIMEOUT = 8000;    // 8 seconds

    private static final byte ENCRYPTED_FLAG = 0x01;
    private static final int CIPHER_PAD_SIZE = 256;

    private SocketChannel channel;
    private String name = null;
    private Server server = null;
    private boolean incoming = false;
    private String connectAddress;
    private State state = State.NONE;
    private long lastMessageSentTime = 0;
    private long lastMessageReceivedTime = 0;

    private byte[] readBuffer = null;
    private final List<byte[]> writeBuffers = new ArrayList<byte[]>();

    private int nextRequestId = 1;
    private final Map<Integer,Result> requests = new HashMap<Integer,Result>();

    // For incoming connections
    public Connection(SocketChannel channel) {
        this.channel = channel;
        incoming = true;
    }

    // For outgoing connections
    public Connection(Server server, String address) {
        this.server = server;
        this.connectAddress = address;
        try {
            InetSocketAddress addr = Network.makeInetSocketAddress(address, "localhost", Global.DEFAULT_PLUGIN_PORT, false);
            name = addr.getAddress().getHostAddress() + ":" + addr.getPort();
        } catch (Exception e) {}
    }

    public boolean isIncoming() {
        return incoming;
    }

    public String getName() {
        if ((name == null) && (channel != null)) {
            Socket socket = channel.socket();
            if ((socket != null) && socket.isConnected())
                name = channel.socket().getInetAddress().getHostAddress() + "/" + channel.socket().getPort();
        }
        if (name == null)
            return "(not connected)";
        return name;
    }

    public long getLastMessageSentTime() {
        return lastMessageSentTime;
    }

    public long getLastMessageReceivedTime() {
        return lastMessageReceivedTime;
    }


    @Override
    public String toString() {
        return getName();
    }

    // Called from Network

    public SocketChannel getChannel() {
        return channel;
    }

    public String getConnectAddress() {
        return connectAddress;
    }

    public void onOpening(SocketChannel channel) {
        this.channel = channel;
    }

    public void onException(Exception e) {
        if (e instanceof ConnectException) {
            if ((server != null) && (! server.connectionMessagesSuppressed()))
                Utils.warning("connection exception with %s: %s", getName(), e.getMessage());
        } else
            Utils.severe(e, "connection exception with %s: %s", getName(), e.getMessage());
        close();
    }

    // outgoing connection, we're the client
    public void onOpened() {
        state = State.HANDSHAKE;
        // send the handshake message
        TypeMap message = new TypeMap();
        message.put("protocolVersion", PROTOCOL_VERSION);
        message.put("pluginVersion", Global.pluginVersion);

        try {
            MessageDigest dig = MessageDigest.getInstance("SHA1");
            Formatter f = new Formatter();
            byte[] out = dig.digest((Network.getCachedKey() + ":" + server.getKey()).getBytes("UTF-8"));
            for (Byte b : out) f.format("%02x", b);
            message.put("key", f.toString());
            sendMessage(message, false);
        } catch (NoSuchAlgorithmException e) {
            Utils.severe(e, "unable to create handshake message");
        } catch (UnsupportedEncodingException e) {}
    }

    // incoming connection, we're the server
    public void onAccepted() {
        Utils.info("accepted a connection from %s", getName());
        state = State.HANDSHAKE;
        Utils.fireDelayed(new Runnable() {
            @Override
            public void run() {
                if (state == State.HANDSHAKE) {
                    Utils.warning("closing connection from %s because no handshake was received", getName());
                    close();
                }
            }
        }, HANDSHAKE_TIMEOUT);
    }

    public void onReadData(byte[] data) {
        if (state == State.CLOSED) return;

        if (readBuffer == null)
            readBuffer = data;
        else {
            int oldSize = readBuffer.length;
            readBuffer = Arrays.copyOf(readBuffer, oldSize + data.length);
            System.arraycopy(data, 0, readBuffer, oldSize, data.length);
        }

        // extract and process all records received
        while (readBuffer.length >= 4) {
            byte flags = readBuffer[0];
            int recLenNew =
                    (0x00ff0000 & (Utils.unsignedByteToInt(readBuffer[1]) << 16)) +
                    (0x0000ff00 & (Utils.unsignedByteToInt(readBuffer[2]) << 8)) +
                    (0x000000ff & Utils.unsignedByteToInt(readBuffer[3]));
            int recLen =
                    (0x00ff0000 & ((int)readBuffer[1] << 16)) +
                    (0x0000ff00 & ((int)readBuffer[2] << 8)) +
                    (0x000000ff & (int)readBuffer[3]);
            if (recLen < 0) {
                Utils.warning("received invalid message length from %s", name);
                close();
                return;
            }
            if (readBuffer.length >= (recLen + 4)) {
                try {
                    byte[] messageData = Arrays.copyOfRange(readBuffer, 4, recLen + 4);
                    byte[] clearData = messageData;
                    if ((flags & ENCRYPTED_FLAG) == ENCRYPTED_FLAG) {
                        Cipher cipher = new Cipher(CIPHER_PAD_SIZE);
                        cipher.initDecrypt(Network.getCachedKey().getBytes("UTF-8"));
                        messageData = cipher.doFinal(messageData);
                    }
                    String encoded = new String(messageData, "UTF-8");
                    try {
                        TypeMap message = TypeMap.decode(encoded);
                        if (message != null)
                            onMessage(message);
                    } catch (StringIndexOutOfBoundsException e) {
                        Utils.severe("Got a StringIndexOutOfBounds, dumping debug state!!!");
                        Utils.severe("flags=%s", flags);
                        Utils.severe("recLen bytes: %s %s %s",
                            (0x00ff0000 & ((long)readBuffer[1] << 16)),
                            (0x0000ff00 & ((long)readBuffer[2] << 8)),
                            (0x000000ff & (long)readBuffer[3])
                        );
                        Utils.severe("recLen=%s", recLen);
                        Utils.severe("recLenNew=%s", recLenNew);
                        Utils.severe("readBuffer.length=%s", readBuffer.length);
                        Utils.severe("clearData.length=%s", clearData.length);
                        Utils.severe("messageData.length=%s", messageData.length);
                        Utils.severe("encoded.length=%s", encoded.length());
                        Utils.severe("encoded=%s", encoded);
                        Utils.severe("first 16 bytes of clearData: %s", Utils.byteArrayToString(clearData, 0, 16));
                        Utils.severe("last 16 bytes of clearData: %s", Utils.byteArrayToString(clearData, clearData.length - 16, 16));
                        Utils.severe("first 16 bytes of messageData: %s", Utils.byteArrayToString(messageData, 0, 16));
                        Utils.severe("last 16 bytes of messageData: %s", Utils.byteArrayToString(messageData, messageData.length - 16, 16));
                        Utils.severe("first 16 bytes of readBuffer: %s", Utils.byteArrayToString(readBuffer, 0, 16));
                        Utils.severe("next 16 bytes of readBuffer: %s", Utils.byteArrayToString(readBuffer, recLen + 4, 16));
                        throw e;
                    }
                } catch (Throwable t) {
                    Utils.severe(t, "exception while processing message from %s: %s", name, t.getMessage());
                    close();
                    return;
                }
                if ((recLen + 4) == readBuffer.length) {
                    readBuffer = null;
                    break;
                } else
                    readBuffer = Arrays.copyOfRange(readBuffer, recLen + 4, readBuffer.length);
            } else
                break;
        }

    }

    public boolean onHasWriteData() {
        synchronized (writeBuffers) {
            return ! writeBuffers.isEmpty();
        }

    }

    public byte[] onGetWriteData() {
        synchronized (writeBuffers) {
            if (writeBuffers.isEmpty()) return null;
            return writeBuffers.remove(0);
        }
    }

    public void onPutWriteData(byte[] data) {
        synchronized (writeBuffers) {
            writeBuffers.add(0, data);
        }
    }

    public void onWriteCompleted() {
    }

    public void onKilled() {
        channel = null;
    }

    public void onClosed() {
        if (server != null) {
    Utils.debug("state is %s", state);
            if (state == State.HANDSHAKE)
                Utils.warning("connection with %s was unexpectedly closed", getName());
            server.onDisconnected();
        } else
            Utils.info("closed connection with %s", getName());
    }

    // Called from Server

    // outbound connection
    public void open() {
        Network.open(this);
    }

    public boolean isOpen() {
        return (state == State.ESTABLISHED) &&
               (channel != null) &&
               (channel.socket() != null) &&
               channel.socket().isConnected() &&
               (! channel.socket().isClosed());
    }

    public void close() {
        if (state == State.CLOSED) return;
        state = State.CLOSED;
        Network.close(this);
    }

    public void sendMessage(TypeMap message, boolean encrypt) {
        if (state == State.CLOSED) return;
        try {
            String encoded = message.encode();
            byte[] messageData = encoded.getBytes("UTF-8");
            byte[] clearData = messageData;
            if (encrypt) {
                Cipher cipher = new Cipher(CIPHER_PAD_SIZE);
                cipher.initEncrypt(server.getKey().getBytes("UTF-8"));
                messageData = cipher.doFinal(messageData);
            }
            byte[] data = new byte[messageData.length + 4];
            System.arraycopy(messageData, 0, data, 4, messageData.length);
            data[0] = encrypt ? ENCRYPTED_FLAG : 0;
            data[1] = (byte)(0x00ff & (messageData.length >> 16));
            data[2] = (byte)(0x00ff & (messageData.length >> 8));
            data[3] = (byte)(0x00ff & messageData.length);

            int recLen =
                    (0x00ff0000 & ((int)data[1] << 16)) +
                    (0x0000ff00 & ((int)data[2] << 8)) +
                    (0x000000ff & (int)data[3]);
            if (recLen != messageData.length) {
                Utils.severe("Encoded message link mismatched, dumping debug state!!!");
                Utils.severe("encoded=%s", encoded);
                Utils.severe("encoded.length=%s", encoded.length());
                Utils.severe("encrypt=%s", encrypt);
                Utils.severe("clearData.length=%s", clearData.length);
                Utils.severe("messageData.length=%s", messageData.length);
                Utils.severe("data.length=%s", data.length);
                Utils.severe("recLen=%s", recLen);
                Utils.severe("recLen bytes: %s %s %s",
                    (0x00ff0000 & ((int)data[1] << 16)),
                    (0x0000ff00 & ((int)data[2] << 8)),
                    (0x000000ff & (int)data[3])
                );
                Utils.severe("first 16 bytes of clearData: %s", Utils.byteArrayToString(clearData, 0, 16));
                Utils.severe("last 16 bytes of clearData: %s", Utils.byteArrayToString(clearData, clearData.length - 16, 16));
                Utils.severe("first 16 bytes of messageData: %s", Utils.byteArrayToString(messageData, 0, 16));
                Utils.severe("last 16 bytes of messageData: %s", Utils.byteArrayToString(messageData, messageData.length - 16, 16));
                Utils.severe("first 16 bytes of data: %s", Utils.byteArrayToString(data, 0, 16));
                Utils.severe("last 16 bytes of data: %s", Utils.byteArrayToString(data, data.length - 16, 16));
                (new Exception("Invalid message encoding!!!")).printStackTrace();
            }
            synchronized (writeBuffers) {
                writeBuffers.add(data);
            }
            lastMessageSentTime = System.currentTimeMillis();
        } catch (UnsupportedEncodingException e) {
        }
        Network.wantWrite(this);
    }

    public Result sendRequest(TypeMap message, boolean encrypt) {
        int requestId = nextRequestId++;
        message.put("requestId", requestId);
        Result result = new Result();
        synchronized (requests) {
            requests.put(requestId, result);
        }
        sendMessage(message, encrypt);
        return result;
    }


    private void onMessage(TypeMap message) {
        lastMessageReceivedTime = System.currentTimeMillis();
        if (state == State.HANDSHAKE) {
            state = State.HANDSHAKING;

            String error = message.getString("error");
            if (error != null) {
                if ((server == null) || (! server.connectionMessagesSuppressed()))
                    Utils.warning("received handshake error with '%s': %s", getName(), error);
                close();
                return;
            }

            // handle handshake message
            if (! message.containsKey("protocolVersion")) {
                if ((server == null) || (! server.connectionMessagesSuppressed()))
                    Utils.warning("expected protocolVersion on connection with '%s'", getName());
                close();
                return;
            }
            int protocol = message.getInt("protocolVersion", 0);
            if (protocol != PROTOCOL_VERSION) {
                if ((server == null) || (! server.connectionMessagesSuppressed()))
                    Utils.warning("protocol version mismatch on connection with '%s', wanted '%d', got '%d'", getName(), PROTOCOL_VERSION, protocol);
                close();
                return;
            }
            String version = message.getString("pluginVersion");
            if (version == null) {
                if ((server == null) || (! server.connectionMessagesSuppressed()))
                    Utils.warning("expected pluginVersion on connection with '%s'", getName());
                close();
                return;
            }

            if (incoming) {
                // compare hashed keys with all the available servers to determine which server is connecting
                String key = message.getString("key");
                if (key == null) {
                    if ((server == null) || (! server.connectionMessagesSuppressed()))
                        Utils.warning("no server key detected on connection with %s", getName());
                    close();
                    return;
                }
                for (Server serv : Servers.getAll()) {
                    try {
                        MessageDigest dig = MessageDigest.getInstance("SHA1");
                        Formatter f = new Formatter();
                        byte[] out = dig.digest((serv.getKey() + ":" + Network.getCachedKey()).getBytes("UTF-8"));
                        for (Byte b : out) f.format("%02x", b);
                        if (f.toString().equals(key)) {
                            Utils.info("server key match detected for '%s' on connection with %s", serv.getName(), getName());
                            if (serv.isEnabled()) {
                                if (serv.isConnectionConnected()) {
                                    Utils.warning("server '%s' is already connected", serv.getName());
                                    close();
                                    return;
                                } else if (serv.isConnecting())
                                    serv.disconnect(false);
                                server = serv;
                                server.setConnection(this);
                                state = State.ESTABLISHED;

                                // send handshake
                                message = new TypeMap();
                                message.put("protocolVersion", PROTOCOL_VERSION);
                                message.put("pluginVersion", Global.pluginVersion);
                                sendMessage(message, false);

                                server.onConnected(version);
                                return;
                            } else {
                                Utils.info("server '%s' is disabled", serv.getName());
                                TypeMap errMsg = new TypeMap();
                                errMsg.put("error", "server is disabled");
                                sendMessage(errMsg, false);
                                close();
                                return;
                            }
                        }
                    } catch (NoSuchAlgorithmException e) {
                    } catch (UnsupportedEncodingException e) {}
                }
                if ((server == null) || (! server.connectionMessagesSuppressed()))
                    Utils.warning("unknown key detected on connection with %s", this);
                TypeMap errMsg = new TypeMap();
                errMsg.put("error", "unknown key");
                sendMessage(errMsg, false);
                close();
            } else {
                state = State.ESTABLISHED;
                server.onConnected(version);
            }

        } else if (state == State.ESTABLISHED) {
            // sanity check
            if (server.getConnection() != this) {
                Utils.warning("connection '%s' has been disowned by server '%s'!?!", getName(), server.getName());
                server = null;
                close();
                return;
            }
            if (message.containsKey("responseId")) {
                int responseId = message.getInt("responseId");
                Result result;
                synchronized (requests) {
                    result = requests.remove(responseId);
                }
                if (result == null)
                    Utils.warning("received response to unknown request %d from %s", responseId, getName());
                else
                    result.setResult(message);
            } else
                server.onMessage(message);
        }
    }

    private enum State {
        NONE,
        HANDSHAKE,
        HANDSHAKING,
        ESTABLISHED,
        CLOSED;
    }

}
