package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoderBGS;
import bgu.spl.net.api.bidi.BidiMessageProtocolBGS;
import bgu.spl.net.api.bidi.ProtocolDataBase;

public class ReactorMain {
    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("wrong amount of arguments");
            return;
        }
        ProtocolDataBase db = new ProtocolDataBase();

        Server.reactor(Integer.parseInt(args[1]),
                Integer.parseInt(args[0]),
                () -> new BidiMessageProtocolBGS(db),
                MessageEncoderDecoderBGS::new).serve();
    }
}