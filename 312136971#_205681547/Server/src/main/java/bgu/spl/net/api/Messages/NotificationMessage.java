package bgu.spl.net.api.Messages;


public class NotificationMessage implements BGSMessage {

    private final short OPCODE = 9;
    private char type;
    private String postingUser;
    private String content;

    public NotificationMessage(String postingUser, String content, char type){
        this.postingUser = postingUser;
        this.content = content;
        this.type = type;
    }
    public short getOpCode() { return OPCODE; }

    public char getType() { return type; }

    public String getPostingUser() { return postingUser; }

    public String getContent() { return content; }
}
