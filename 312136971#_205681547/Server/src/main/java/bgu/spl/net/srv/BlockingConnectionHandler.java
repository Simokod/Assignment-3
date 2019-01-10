package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.ConnectionsImpl;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private int connectionId;
    private ConnectionsImpl<T> connections;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader,
                                     BidiMessagingProtocol<T> protocol,
                                     int connectionId, ConnectionsImpl<T> connections) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connectionId = connectionId;
        this.connections = connections;
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;
            in = new BufferedInputStream(sock.getInputStream());
            protocol.start(connectionId, connections);
            connections.addClient(connectionId, this);

            while (!protocol.shouldTerminate() && connected && ((read = in.read()) >= 0)) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage);
                }

            }
        } catch (IOException e) {
            System.out.println("IO Exception");
        }
    }

    @Override
    public void close() {
        connected = false;
    }

    @Override
    public void send(T msg) {
        try {
            out = new BufferedOutputStream(sock.getOutputStream());
            out.write(encdec.encode(msg));
            out.flush();
        } catch (IOException e) {
            System.out.println("error sending");
        }
    }
}
