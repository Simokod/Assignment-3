package bgu.spl.net.api.Messages;


public class UserlistMessage implements BGSMessage {

    private final short OPCODE = 7;

    public short getOpCode() { return OPCODE; }
}
