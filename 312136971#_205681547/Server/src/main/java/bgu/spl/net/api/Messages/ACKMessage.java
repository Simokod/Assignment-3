package bgu.spl.net.api.Messages;

public class ACKMessage implements BGSMessage {

    private final short OPCODE = 10;
    private short messageOpCode;
    private String optionalData=null;

    public ACKMessage(short messageOpCode){
        this.messageOpCode = messageOpCode;

    }

    public short getOpCode() { return OPCODE; }
    public short getMessageOpCode() { return messageOpCode; }
    public String getOptionalData() { return optionalData; }

    public void setOptionalData(String optionalData) { this.optionalData = optionalData; }
}