package com.Sunggeun;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

public class Main {
    static final int PORT = 5568;
    private static AccountDAO accountInfoDAO = new AccountDAO();
    private static AccountDTO accountInfo = new AccountDTO();
    private static String clientId = "################";//애플리케이션 클라이언트 아이디값";
    private static String clientSecret = "@@@@@@@@@@@@@@@@@";//애플리케이션 클라이언트 시크릿값";

    private static ArrayList<ChatModel> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket; // 서버소켓
        Socket clientSocket; // 클라이언트 소켓
        serverSocket = new ServerSocket(PORT);
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                AcceptClient myThread = new AcceptClient(clientSocket);
                myThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class AcceptClient extends Thread {
        private Socket clientSock;
        private DataInputStream dis;
        private DataOutputStream dos;

        AcceptClient(Socket clientSock) {
            this.clientSock = clientSock;
            System.out.println("클라이언트 연결");

            try {
                dis = new DataInputStream(clientSock.getInputStream());
                dos = new DataOutputStream(clientSock.getOutputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void run() {
            System.out.println("run 시작");
            while (true) {
                try {
                    System.out.println("나여기있어요");
                    String inputLine = null;
                    inputLine = dis.readUTF();
                    System.out.println("클라이언트에게 받은 문자열 : " + inputLine);

                    if(inputLine.equals("init")){
                        String uid = dis.readUTF();
                        String desUid = dis.readUTF();
                        ChatModel data = new ChatModel(clientSock, uid, desUid, dos);
                        clients.add(data);
                        System.out.println(uid + "추가됨.");
                    }
                    else if (inputLine.equals("signup")) {
                        addAccount(dis);
                    }
                    else if (inputLine.equals("login")) {

                        loginAccount(dis, dos);

                    }
                    else if(inputLine.equals("send_profileimg")){
                        String uid = dis.readUTF();
                        String imgSize = dis.readUTF();
                        int size = Integer.parseInt(imgSize.substring(5));
                        FileOutputStream fos = new FileOutputStream(uid + ".png");

                        byte[] buf = new byte[3000000];
                        int recvSize = 0;
                        int read = 0;
                        while (recvSize < size) {
                            read = dis.read(buf);
                            recvSize += read;
                            System.out.println(recvSize + " < " + size);
                            fos.write(buf, 0, read);
                        }

                        fos.flush();
                        fos.close();

                        dos.writeUTF("image_end");
                    }
                    else if (inputLine.equals("friendList")) {
                        System.out.println("친구목록");

                        String uid = dis.readUTF();

                        getFriends(uid, dos);

                    }
                    else if(inputLine.equals("send_msg")){
                        String roomId;
                        String sendUser = dis.readUTF();
                        String recvUser = dis.readUTF();
                        String message = dis.readUTF();
                        String isRoom = dis.readUTF();
                        String result = null;
                        if(isRoom.equals("noroom")){
                            roomId = "chatlog";
                            accountInfoDAO.makeChatroom(sendUser, recvUser);

                            ArrayList<String> countryList = accountInfoDAO.checkCountry(sendUser, recvUser);
                            if(countryList.get(0).equals(countryList.get(1))){
                                result = message;
                            }else if(countryList.get(0).equals("ja") && countryList.get(1).equals("zh-CN")){
                                result = message;
                            }
                            else if(countryList.get(0).equals("zh-CN") && countryList.get(1).equals("ja")){
                                result = message;
                            }
                            else{
                                result = translate(message, countryList.get(0), countryList.get(1));

                            }
                            accountInfoDAO.sendMsg(roomId, sendUser, recvUser, message, false);
                            if(!result.equals(message)){
                                dos.writeUTF(sendUser + "&" + recvUser + "&" + result);
                                System.out.println(sendUser + "가 " + recvUser +"에게 " + result + "보냄");

                                accountInfoDAO.sendMsg(roomId, sendUser, recvUser, result, false);
                            }

                            for(ChatModel i : clients){
                                if(i.uid.equals(recvUser) && i.destinationUid.equals(sendUser)){
                                    i.dos.writeUTF(sendUser + "&" + recvUser + "&" + message);
                                    System.out.println(sendUser + "가 " + recvUser +"에게 " + message + "보냄");
                                    if(!result.equals(message)){
                                        i.dos.writeUTF(sendUser + "&" + recvUser + "&" + result);
                                        System.out.println(sendUser + "가 " + recvUser +"에게 " + result + "보냄");
                                    }
                                }
                            }

                        }else{
                            roomId = "roomlog";
                            ArrayList<String> countryList = accountInfoDAO.checkCountry(sendUser, sendUser);
                            if(countryList.get(0).equals("ko")){
                                result = message;

                            }else{
                                System.out.println(countryList.get(0) + "는 어디?");
                                String korea = "ko";
                                result = translate(message, countryList.get(0), korea);

                            }

                            if(!result.equals(message)){
                                dos.writeUTF(sendUser + "&" + recvUser + "&" + result);
                                System.out.println(sendUser + "가 " + recvUser +"에게 " + result + "보냄");

                                accountInfoDAO.sendMsg(roomId, recvUser, sendUser, result, false);
                            }



                            accountInfoDAO.sendMsg(roomId, recvUser, sendUser, message, false);

                            for(ChatModel i : clients){
                                if(i.destinationUid.equals(recvUser) && !(i.uid.equals(sendUser))){
                                    i.dos.writeUTF(sendUser + "&" + recvUser + "&" + message);
                                    System.out.println(sendUser + "가 " + recvUser +"에게 " + message + "보냄");
                                    if(!result.equals(message)){
                                        i.dos.writeUTF(sendUser + "&" + recvUser + "&" + result);
                                        System.out.println(sendUser + "가 " + recvUser +"에게 " + result + "보냄");
                                    }

                                }
                            }

                        }





//                        if(accountInfoDAO.checkIsRoom(sendUser, recvUser)){
//                            roomId = "roomlog";
//                            if(accountInfoDAO.sendMsg(roomId, recvUser, sendUser, message, false)){
//                                System.out.println("success");
//                            }else{
//                                System.out.println("false");
//                            }


//                            for(ChatModel i : clients){
//                                System.out.println("user: " + i.uid);
//                                if(i.destinationUid.equals(recvUser) && (!i.uid.equals(sendUser))){
//                                    i.dos.writeUTF(sendUser + "&" + recvUser + "&" + message);
//                                    System.out.println(sendUser + "가 " + recvUser +"에게 " + message + "보냄");
//                                }
//                            }
//                        }else{
//                            System.out.println("1:1 채팅입니다.");
//                            if(accountInfoDAO.sendMsg(roomId, sendUser, recvUser, message, false)){
//                                System.out.println("success");
//                            }else{
//                                System.out.println("false");
//                            }
//                            if(accountInfoDAO.sendMsg(roomId, sendUser, recvUser, result, false)){
//                                System.out.println("success");
//                            }else{
//                                System.out.println("false");
//                            }
//
//
//                            accountInfoDAO.makeChatroom(sendUser, recvUser);
//                            accountInfoDAO.makeChatroom(recvUser, sendUser);
//
//                            //여기서 push해줘야된다.
//                            //해당 방에 있는 모든 사람에게
//
//                            //방에있는 사람. 지금 메시지 보낸 방과. 사용자의 접속한 닉네임이 일치하면
//                            //push해준다.
//                            for(ChatModel i : clients){
//                                System.out.println("user: " + i.uid);
//                                if(i.uid.equals(recvUser) && i.destinationUid.equals(sendUser)){
//                                    i.dos.writeUTF(sendUser + "&" + recvUser + "&" + message);
//                                    System.out.println(sendUser + "가 " + recvUser +"에게 " + message + "보냄");
//                                    i.dos.writeUTF(sendUser + "&" + recvUser + "&" + result);
//                                    System.out.println(sendUser + "가 " + recvUser +"에게 " + result + "보냄");
//
//
//                                }
////                            }
//                        }

                    }else if(inputLine.equals("commentsList")){
                        String uid = dis.readUTF();
                        String destinationUid = dis.readUTF();
                        String isRoom = dis.readUTF();
                        ArrayList<String> messageList = new ArrayList<>();

                        if(isRoom.equals("room")){
                            System.out.println("단체방읽기");
                            messageList = accountInfoDAO.getMessageList(uid, destinationUid, true);

                        }else{
                            System.out.println("1:1방읽기");
                            messageList = accountInfoDAO.getMessageList(uid, destinationUid, false);
                        }

                        int index = 0;
                        if(messageList.size() == 0){
                            dos.writeUTF("messageList_null");
                        }

                        for(String i : messageList){
                            if (++index == messageList.size()) {
                                dos.writeUTF("messageList_end");
                            }else {
                                dos.writeUTF("not_end");
                            }

                            dos.writeUTF(i);
                        }
                    }
                    else if(inputLine.equals("quit")){
                        System.out.println("quit로 끝남");
                        inputLine = dis.readUTF();


                        if(!inputLine.equals("null")){
                            int count = 0;

                            for(ChatModel i : clients){

                                if(i.uid.equals(inputLine)){
                                    break;
                                }
                                count++;
                            }
                            clients.remove(count);
                            System.out.println("clientslist");

                            for(ChatModel i : clients){

                                System.out.println(i.uid);
                            }
                        }

                        break;
                    }
                    else if(inputLine.equals("getChatList")){
                        String uid = dis.readUTF();

                        ArrayList<ChatListModel> chatList = accountInfoDAO.getChatRoomList(uid);
                        int index = 0;
                        if(chatList.size() == 0){
                            dos.writeUTF("chatList_null");
                        }
//                        System.out.println("size : " + chatRoomList.size() );

                        for(ChatListModel i : chatList){
                            if (++index == chatList.size()) {
                                dos.writeUTF("chatList_end");
                            }else {
                                dos.writeUTF("not_end");
                            }

                            dos.writeUTF(i.getDesUid());
                            if(i.isRoom() == 0){
                                dos.writeUTF("noroom");
                            }else{
                                dos.writeUTF("room");
                            }
                        }
                    }
                    else if(inputLine.equals("make_chatRoom")){
                        String roomName = dis.readUTF();
                        String interesting = dis.readUTF();
                        String uid = dis.readUTF();

                        if(accountInfoDAO.makeChatroom2(roomName, interesting, uid)){
                            System.out.println("sucess make_chatRoom");
                        }else{
                            System.out.println("faile make_chatRoom");
                        }


                    }
                    else if(inputLine.equals("roomList")){
                        System.out.println("roomList Req");
                        dos.writeUTF("roomList_ok");
                        ArrayList<RoomModel> roomList = accountInfoDAO.getRoomList();
                        int index = 0;
                        if(roomList.size() == 0){
                            dos.writeUTF("null");
                        }else{
                            for(RoomModel i : roomList){
                                if(++index == roomList.size()){
                                    dos.writeUTF("roomList_end");
                                }else{
                                    dos.writeUTF("not_end");
                                }

                                dos.writeUTF(i.roomName);
                                dos.writeUTF(i.interesting);
                                System.out.println(i.roomName + "과 " + i.interesting + "을 보냄.");
                            }
                        }



                    }
                    else if(inputLine.equals("getInRoom")){
                        String roomName = dis.readUTF();
                        String uid = dis.readUTF();

                        accountInfoDAO.getInRoom(roomName, uid);
                        System.out.println("단체방참여완료");
                    }
                    else if(inputLine.equals("meetingList")){
                        String uid = dis.readUTF();

                        ArrayList<UserModel> userList = accountInfoDAO.getMeetingList(uid);
                        int index = 0;
                        int userListSize = userList.size();

                        if(userListSize == 0){
                            dos.writeUTF("null");
                        }

                        for(UserModel i : userList){
                            if (++index == userListSize) {
                                dos.writeUTF("meetingList_end");
                            }else {
                                dos.writeUTF("not_end");
                            }

                            dos.writeUTF(i.nickname);
                            dos.writeUTF(i.country);
                            dos.writeUTF(i.interesting);

                            System.out.println(i.nickname + "을 meetingList로 보내줌.");
                        }
//                        ArrayList<UserModel> newFriends = new ArrayList<>();
//
//                        for(UserModel i : userList){
//                            if(accountInfoDAO.checkFriend(uid, i.nickname)){
//                                newFriends.add(i);
//                            }
//
//                        }
//                        int first = 0;
//                        int second = 0;
//
//                        int meetingSize = userList.size();
//                        if(meetingSize == 0 ){
//                            dos.writeUTF("null");
//                            continue;
//                        }
//
//                        Random r = new Random();
//
//                        first = r.nextInt(meetingSize);
//                        if(meetingSize == 1){
//                            dos.writeUTF("meetingList_end");
//                            dos.writeUTF(userList.get(first).nickname);
//                            dos.writeUTF(userList.get(first).sex);
//                            dos.writeUTF(newFriends.get(first).interesting);
//                            continue;
//                        }
//
//                        while(true){
//                            second = r.nextInt(meetingSize);
//                            if(second != first){
//                                break;
//                            }
//                        }
//                        dos.writeUTF("not_end");
//                        dos.writeUTF(newFriends.get(first).nickname);
//                        dos.writeUTF(newFriends.get(first).sex);
//                        dos.writeUTF(newFriends.get(first).interesting);
//                        dos.writeUTF("meetingList_end");
//                        dos.writeUTF(newFriends.get(second).nickname);
//                        dos.writeUTF(newFriends.get(second).sex);
//                        dos.writeUTF(newFriends.get(second).interesting);
//                        System.out.println("first : " + newFriends.get(first).nickname + ", second : " + newFriends.get(second).nickname);
                    }
                    else if(inputLine.equals("add_friend")){
                        String uid = dis.readUTF();
                        String desUid = dis.readUTF();

                        accountInfoDAO.addFriend(uid, desUid);
                        System.out.println(uid + ", " + desUid + "가 친구가 되었습니다.");
                    }

                    else if(inputLine.equals("ImageReq")){
//                        synchronized (this){
                            String uid = dis.readUTF();

                            File file = new File(uid);

                            dos.writeUTF("Start" + file.length());
                            System.out.println("send 용량" + file.length());

                            FileInputStream fis = new FileInputStream(file);
                            byte[] buf = new byte[3000000];
                            int readSize = 0;


                            while((readSize = fis.read(buf)) > 0) {
                                System.out.println("read size : " + readSize);
                                dos.write(buf, 0, readSize);
                            }
//                        }





                    }
                    else if(inputLine.equals("getImagemessage")){
//                        synchronized (this){
                            String fileName = dis.readUTF();


                            String imagRecv = dis.readUTF();
                            int size = Integer.parseInt(imagRecv.substring(5));
                            File file = new File(fileName);
                            FileOutputStream fos = new FileOutputStream(fileName);

                            byte[] buf = new byte[30000];
                            int recvSize = 0;
                            int read = 0;
                            while (recvSize < size) {
                                read = dis.read(buf);
                                recvSize += read;
                                System.out.println(recvSize + " < " + size);
                                fos.write(buf, 0, read);
                            }


                            fos.flush();
                            fos.close();

                    }
                    else if(inputLine.equals("sendImageMessage")){
//                        String roomId = "chatlog";
//                        String sendUser = dis.readUTF();
//                        String recvUser = dis.readUTF();
//                        String message = dis.readUTF();
//                        System.out.println(roomId +", " + sendUser + ", "+ recvUser +", " + message);
//
//                        if(accountInfoDAO.checkIsRoom(sendUser, recvUser)){
//                            roomId = "roomlog";
//                            if(accountInfoDAO.sendMsg(roomId, recvUser, sendUser, message, true)){
//                                System.out.println("success");
//                            }else{
//                                System.out.println("false");
//                            }
//
//                            for(ChatModel i : clients){
//                                System.out.println("user: " + i.uid);
//                                if(i.destinationUid.equals(recvUser) && (!i.uid.equals(sendUser))){
//                                    i.dos.writeUTF(sendUser + "&" + recvUser + "&image&"  +message);
//                                    System.out.println(sendUser + "가 " + recvUser +"에게 " + message + "보냄");
//                                }
//                            }
//                        }else{
//                            System.out.println("1:1 채팅입니다.");
//                            if(accountInfoDAO.sendMsg(roomId, sendUser, recvUser, message, true)){
//                                System.out.println("success");
//                            }else{
//                                System.out.println("false");
//                            }
//
//                            accountInfoDAO.makeChatroom(sendUser, recvUser);
//                            accountInfoDAO.makeChatroom(recvUser, sendUser);
//
//                            //여기서 push해줘야된다.
//                            //해당 방에 있는 모든 사람에게
//
//                            //방에있는 사람. 지금 메시지 보낸 방과. 사용자의 접속한 닉네임이 일치하면
//                            //push해준다.
//                            for(ChatModel i : clients){
//                                System.out.println("user: " + i.uid);
//                                if(i.uid.equals(recvUser) && i.destinationUid.equals(sendUser)){
//                                    i.dos.writeUTF(sendUser + "&" + recvUser + "&image&"  +message);
//                                    System.out.println(sendUser + "가 " + recvUser +"에게 " + message + "보냄(이미지)");
//
//
//                                }
//                            }
//                        }

                        String roomId;
                        String sendUser = dis.readUTF();
                        String recvUser = dis.readUTF();
                        String message = dis.readUTF();
                        String isRoom = dis.readUTF();
                        if(isRoom.equals("noroom")){
                            roomId = "chatlog";

                            accountInfoDAO.makeChatroom(sendUser, recvUser);
                            accountInfoDAO.sendMsg(roomId, sendUser, recvUser, message, true);

                            for(ChatModel i : clients){
                                if(i.uid.equals(recvUser) && i.destinationUid.equals(sendUser)){
                                    i.dos.writeUTF(sendUser + "&" + recvUser + "&image&" + message);
                                    System.out.println(sendUser + "가 " + recvUser +"에게 " + message + "보냄");
                                }
                            }
                        }else{
                            roomId = "roomlog";
                            accountInfoDAO.sendMsg(roomId, recvUser, sendUser, message, true);

                            for(ChatModel i : clients){
                                if(i.destinationUid.equals(recvUser) && !(i.uid.equals(sendUser))){
                                    i.dos.writeUTF(sendUser + "&" + recvUser + "&image&" + message);
                                    System.out.println(sendUser + "가 " + recvUser +"에게 " + message + "보냄");
                                }
                            }
                        }

                    }
                    else {
                        System.out.println("here");
                        break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
            System.out.println("나 끝났어요");
            try {
                dis.close();
                dos.close();
                clientSock.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static void addAccount(DataInputStream in) {
            String recvMsg = null;
            try {
                recvMsg = in.readUTF();
                accountInfo.setId(recvMsg);

                recvMsg = in.readUTF();
                accountInfo.setPw(recvMsg);

                recvMsg = in.readUTF();
                accountInfo.setCountry(recvMsg);

                recvMsg = in.readUTF();
                accountInfo.setNickname(recvMsg);

                recvMsg = in.readUTF();
                accountInfo.setInteresting(recvMsg);

                boolean success = accountInfoDAO.insertDB(accountInfo);
                if (success) {
                    System.out.println(accountInfo.getNickname() + "님 회원가입 성공");
                } else {
                    System.out.println(accountInfo.getNickname() + "님 회원가입 실패");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private static void loginAccount(DataInputStream in, DataOutputStream out) {
        String loginId = null;
        String loginPw = null;
        String message = null;
        try {
            loginId = in.readUTF();
            loginPw = in.readUTF();
            message = accountInfoDAO.isPasswd(loginId, loginPw);

            out.writeUTF(message);

            System.out.println(loginId + " : " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        return success;
    }

    private static void getFriends(String uid, DataOutputStream out) {
        try {
            ArrayList<String> friendList = accountInfoDAO.getFriendsList(uid);
            System.out.println("friendList size : " + friendList.size());

            int index = 0;
            if(friendList.size() == 0){
                out.writeUTF("null");
                return;
            }

            for (String i : friendList) {
                if (++index == friendList.size()) {
                    out.writeUTF("friendList_end");
                } else {
                    out.writeUTF("not_end");
                }

                out.writeUTF(i); //nickname 보내줬고
                System.out.println(i + "를 "+uid + "의 친구로 보내줌.");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String translate(String myText, String from, String to){
        try {
            String text = URLEncoder.encode(myText, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/language/translate";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            // post request
            String postParams = "source="+from+"&target="+to+"&text=" + text;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            System.out.println(response.toString());
            String dData = response.substring(response.lastIndexOf("xt\":\"")+5, response.lastIndexOf("\",\"src"));
            return dData;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;

    }


}
