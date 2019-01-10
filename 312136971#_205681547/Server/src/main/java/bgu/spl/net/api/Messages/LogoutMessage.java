package bgu.spl.net.api.Messages;

public class LogoutMessage implements BGSMessage {

    private final short OPCODE = 3;

    public short getOpCode() { return OPCODE; }
}
