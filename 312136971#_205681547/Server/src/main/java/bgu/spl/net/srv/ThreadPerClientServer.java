package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;

import java.util.function.Supplier;

public class ThreadPerClientServer<T> extends BaseServer<T> {
    public ThreadPerClientServer(int port, Supplier<BidiMessagingProtocol<T>> protocolFactory,
                                 Supplier<MessageEncoderDecoder<T>> encdecFactory) {
        super(port, protocolFactory, encdecFactory);
    }

    @Override
    protected void execute(BlockingConnectionHandler<T> handler) {
        new Thread(handler).start();
    }
}
