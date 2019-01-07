package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoderBGS;
import bgu.spl.net.api.bidi.BidiMessageProtocolBGS;
import bgu.spl.net.api.bidi.ProtocolDataBase;

public class TPCMain {
    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("wrong amount of arguments");
            return;
        }
        ProtocolDataBase db = new ProtocolDataBase();

        Server.threadPerClient(Integer.parseInt(args[0]),
                () -> new BidiMessageProtocolBGS(db),
                MessageEncoderDecoderBGS::new).serve();
    }
}
