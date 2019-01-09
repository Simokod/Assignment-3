package bgu.spl.net.api.Messages;


public class PostMessage implements BGSMessage {

    private short opCode = 5;
    private String post;

    public short getOpCode() { return opCode; }
    public String getPost() { return post; }

    public void setPost(String post) { this.post = post; }
}
