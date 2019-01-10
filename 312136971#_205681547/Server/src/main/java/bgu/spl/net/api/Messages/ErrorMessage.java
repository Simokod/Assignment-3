package bgu.spl.net.api.Messages;

public class ErrorMessage implements BGSMessage {

    private final short OPCODE = 11;
    private short msgOpCode;

    public ErrorMessage(short msgOpCode){
        this.msgOpCode = msgOpCode;
    }
    public short getOpCode() { return OPCODE; }
    public short getMsgOpCode() { return msgOpCode; }
}
