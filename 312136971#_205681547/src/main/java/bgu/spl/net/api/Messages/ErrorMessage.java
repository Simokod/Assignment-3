package bgu.spl.net.api.Messages;

public class ErrorMessage implements BGSMessage {

    private short opCode = 11;
    private short msgOpCode;

    public ErrorMessage(short msgOpCode){
        this.msgOpCode = msgOpCode;
    }
    public short getOpCode() { return opCode; }
    public short getMsgOpCode() { return msgOpCode; }
}
