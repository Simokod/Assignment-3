package bgu.spl.net.api.Messages;

public class DisconnectMessage implements BGSMessage {

    private final short OPCODE = 99;

    public short getOpCode() {
        return OPCODE;
    }
}
