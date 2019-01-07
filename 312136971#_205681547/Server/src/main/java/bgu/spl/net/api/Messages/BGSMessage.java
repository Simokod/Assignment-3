package bgu.spl.net.api.Messages;

public interface BGSMessage {

    /**
     * @return a short according to the type of message
     */
    short getOpCode();
}