package bgu.spl.net.api.Messages;


public class StatMessage implements BGSMessage {

    private short opCode = 8;
    private String user;


    public short getOpCode() { return opCode; }
    public String getUser() { return user; }

    public void setUser(String user) { this.user = user; }
}