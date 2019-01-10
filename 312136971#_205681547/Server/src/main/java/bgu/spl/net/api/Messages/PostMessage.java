package bgu.spl.net.api.Messages;


public class PostMessage implements BGSMessage {

    private final short OPCODE = 5;
    private String post;

    public short getOpCode() { return OPCODE; }
    public String getPost() { return post; }

    public void setPost(String post) { this.post = post; }
}
