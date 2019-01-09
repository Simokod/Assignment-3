package bgu.spl.net.api.Messages;

public class LogoutMessage implements BGSMessage {

    private short opCode = 3;

    public short getOpCode() { return opCode; }
}
