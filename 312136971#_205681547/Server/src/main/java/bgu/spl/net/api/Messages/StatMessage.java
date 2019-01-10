package bgu.spl.net.api.Messages;


public class StatMessage implements BGSMessage {

    private final short OPCODE = 8;
    private String user;


    public short getOpCode() { return OPCODE; }
    public String getUser() { return user; }

    public void setUser(String user) { this.user = user; }
}