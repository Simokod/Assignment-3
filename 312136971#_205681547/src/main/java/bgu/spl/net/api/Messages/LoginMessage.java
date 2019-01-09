package bgu.spl.net.api.Messages;

public class LoginMessage implements BGSMessage {

    private short opCode;
    private String userName;
    private String password;
    private int paramCounter;

    public LoginMessage(){
        this.paramCounter = 0;
        this.opCode = 2;
    }

    public short getOpCode() { return opCode; }
    public int getParamCounter() { return paramCounter; }
    public void increaseCounter() { this.paramCounter++; }
    public String getUserName() { return userName; }
    public String getPassword() { return password; }

    public void setUserName(String userName) { this.userName = userName; }
    public void setPassword(String password) { this.password = password; }

}
