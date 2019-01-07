package bgu.spl.net.api.Messages;


public class PMMessage implements BGSMessage {

    private short opCode = 6;
    private int paramCounter = 0;
    private String userName;
    private String content;

    public short getOpCode() { return opCode; }
    public void increaseCounter() { this.paramCounter++; }
    public int getParamCounter() { return paramCounter; }

    public String getUserName() { return userName; }
    public String getContent() { return content; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setContent(String content) { this.content = content; }
}
