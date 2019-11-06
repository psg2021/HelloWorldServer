package com.Sunggeun;


import java.io.DataOutputStream;
import java.net.Socket;

public class ChatModel {
        Socket socket;



        String uid;
        String destinationUid;
        DataOutputStream dos;

        public ChatModel(Socket socket, String uid, String destinationUid, DataOutputStream dos) {
                this.socket = socket;
                this.uid = uid;
                this.destinationUid = destinationUid;
                this.dos = dos;
        }

}
