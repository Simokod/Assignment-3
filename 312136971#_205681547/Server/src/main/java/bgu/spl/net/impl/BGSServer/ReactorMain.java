package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessageEncoderDecoderBGS;
import bgu.spl.net.api.bidi.BidiMessageProtocolBGS;
import bgu.spl.net.api.bidi.ProtocolDataBase;
import bgu.spl.net.srv.Server;

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