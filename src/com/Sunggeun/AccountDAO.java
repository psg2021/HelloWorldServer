package com.Sunggeun;

import java.sql.*;
import java.util.ArrayList;

public class AccountDAO {
    private Connection conn = null; // DB연결을 관리
    private Statement stmt = null; // 쿼리 질의할 Statement
    private PreparedStatement pstmt = null; // Statement와 달리 캐시 사용(동일한 쿼리 반복 사용할때 사용)
    private ResultSet rs = null; // db로부터 결과값을 받을 떄 사용

    //private String driveName = "com.mysql.cj.jdbc.Driver"; // JDBC드라이버 이름 지정
    // 접속할 정보 지정 (JDBC : <sub protocol> : <subname(<ip> : <port> : <dbname>)>)
    // ? 뒤 첫번째 파라미터는 KST(Korea Time Zone)을 인식못하는 경우 사용
    // 두번째, 세번째 파리미터는 서버와 인코딩을 맟춰주기 위해 사용
    private String dbURL = "jdbc:mysql://127.0.0.1:3306/test"
            + "?serverTimezone=Asia/Seoul & useUnicode=true & characterEncoding=utf8";

    /* 데이터 베이스의 연결을 항상 유지시키는것은 좋지 않으므로 기능을 수행할때마다 DB연결/연결해제 수행 */
    // 데이터베이스 연결 메소드
    public void connect() {
        try {
//            Class.forName(driveName); // 드라이버 로드

            conn = DriverManager.getConnection(dbURL, "root","1234"); // 데이터베이스 연결

            // System.out.println("데이터 베이스 연결 성공");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 데이터 베이스 연결을 끊을 떄 사용하는 메소드
    public void disconnect() {
        if (stmt != null) {
            try {
                stmt.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (pstmt != null) {
            try {
                pstmt.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // DB insert 메소드
    public boolean insertDB(AccountDTO accountInfo) {
        connect(); // DB연결


        String sql = "insert into accountinfo values(?,?,?,?,?)";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, accountInfo.getId());
            pstmt.setString(2, accountInfo.getPw());
            pstmt.setString(3, accountInfo.getCountry());
            pstmt.setString(4, accountInfo.getNickname());
            pstmt.setString(5, accountInfo.getInteresting());

            pstmt.executeUpdate(); // 쿼리 실행

            //DB에 List 까지 한번에 해결.
            sql = "CREATE TABLE " + accountInfo.getNickname() +
                    " (id VARCHAR(50), identifier INT)";

            pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate(); // 쿼리 실행

        } catch (SQLException e) {
            e.printStackTrace();
            return false; // insert 실패 시 false를 반환
        } finally {
            disconnect(); // DB연결 해제
        }
        return true; // insert 성공 시 false를 반환
    }


    // 로그인 시 DB에 id와 pw를 확인하는 메소드
    public String isPasswd(String id, String passwd) {
        connect(); // DB연결

        String message = null; // 로그인 성공/실패를 true/false형태로 저장

        // 쿼리 작성
        String sql = "select * from accountinfo where id=?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);

            rs = pstmt.executeQuery(); // 쿼리수행 결과값을 받아옴
            if(!rs.next()){
                message = "can't find ID";
            }else{
                // id를 통해 DB로부터 받은 비밀번호와 파미미터로부터 받은 비밀번호가 같으면 로그인 성공
                String orgPasswd = rs.getString("pw");
                if (passwd.equals(orgPasswd)){
                    message = rs.getString("nickname");
                }else{
                    message = "password failed";
                }
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect(); // DB연결 해제
        }

        return message;
    }

//    public String getMyNickname(String id) {
//        connect(); // DB연결
//
//        // 쿼리 작성
//        String sql = "select nickname from accountinfo where id=?";
//
//        // 쿼리 수행 후 받는 값을ArrayList로 저장
//        String nickname = new String();
//
//        try {
//            pstmt = conn.prepareStatement(sql);
//            pstmt.setString(1, id);
//
//            rs = pstmt.executeQuery(); // 쿼리수행 결과값을 받아옴
//            rs.next();
//
//            // id를 통해 DB로부터 받은 비밀번호와 파미미터로부터 받은 비밀번호가 같으면 로그인 성공
//            nickname = rs.getString(1);
//
//            rs.close();
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            disconnect(); // DB연결 해제
//        }
//        return nickname; // 결과값 반환
//    }

    public ArrayList<String> getFriendsList(String uid) {
        connect(); // DB연결

        // 쿼리 작성
        String sql = "select id from "+ uid +" WHERE identifier=2 order by id desc";

        // 쿼리 수행 후 받는 값을ArrayList로 저장
        ArrayList<String> datas = new ArrayList<String>();

        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery(); // 쿼리수행 결과값을 받아옴

            while (rs.next()) { // 결과값이 있으면 저장 반복
                datas.add(rs.getString("id"));
            }
            rs.close(); // rs 리소스 해제

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect(); // DB연결 해제
        }
        return datas; // 결과값 반환
    }

    public void makeChatList(String i) {
        connect(); // DB연결

        String sql = "SELECT table_name FROM information_schema.tables WHERE table_name = ?";


        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, i);

            rs = pstmt.executeQuery();

            if(!rs.next()){
                System.out.println("DB에 "+ i +"의 Table이 없어서 만들어줌.");
                sql = "CREATE TABLE " + i +
                " (id VARCHAR(50), identifier INT)";

                pstmt = conn.prepareStatement(sql);
                pstmt.executeUpdate(); // 쿼리 실행
            }else{
                System.out.println(i + "의 Table이 이미 존재한다.");
            }

            rs.close();


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect(); // DB연결 해제
        }
        return; // 결과값 반환
    }

    public boolean sendMsg(String chatRoomId, String sendUser, String recvUser, String message, boolean isImg) {
        connect(); // DB연결
        String sql;

        // 쿼리 작성
        // String sql ="insert into studentInfo(id, name, password, deptName,
        // gender,phoneNum, address) values(?,?,?,?,?,?,?)";
        if(isImg){
            System.out.println("image");
             sql = "insert into "+chatRoomId+" values(?,?,?,1)";
        }else{
            sql = "insert into "+chatRoomId+" values(?,?,?,0)";
        }


        try {
            // 쿼리를 준비해놓고 파라미터를 순서대로 올림
            // ?(물음표)의 값은 모두 아래의 값으로 대체, 순서대로 값이 저장됨
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, sendUser);
            pstmt.setString(2, recvUser);
            pstmt.setString(3, message);

            pstmt.executeUpdate(); // 쿼리 실행
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // insert 실패 시 false를 반환
        } finally {
            disconnect(); // DB연결 해제
        }
        return true; // insert 성공 시 false를 반환
    }

    public boolean makeChatroom(String sendUser, String recvUser) {
        connect(); // DB연결

        // 쿼리 작성
        // String sql ="insert into studentInfo(id, name, password, deptName,
        // gender,phoneNum, address) values(?,?,?,?,?,?,?)";

        String sql = "SELECT id from "+sendUser+" where identifier=? && id=?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "0");
            pstmt.setString(2, recvUser);

            rs = pstmt.executeQuery();

            if(!rs.next()){
                System.out.println(sendUser + "table 에 "+ recvUser +"가 없어서 넣어줌.");
                sql = "insert into "+sendUser+" values(?, 0)";

                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, recvUser);
                pstmt.executeUpdate(); // 쿼리 실행

                sql = "insert into "+recvUser+" values(?, 0)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, sendUser);
                pstmt.executeUpdate(); // 쿼리 실행

            }else{
                System.out.println(sendUser + "의 Table에 이미 "+ recvUser +" 가 존재한다.");
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // insert 실패 시 false를 반환
        } finally {
            disconnect(); // DB연결 해제
        }
        return true; // insert 성공 시 false를 반환
    }

    public ArrayList<String> getMessageList(String uid, String destinationUid, boolean isRoom) {
        connect(); // DB연결
        String sql;

        if(isRoom){
            sql = "select * from roomlog where roomName=?";
        }else{
            sql  = "select * from chatlog where (sendUser=? && recvUser=?) || (sendUser=? && recvUser=?)";
        }

        ArrayList<String> datas = new ArrayList<String>();

        try {
            if(isRoom){
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, destinationUid);
                rs = pstmt.executeQuery(); // 쿼리수행 결과값을 받아옴

                while (rs.next()) { // 결과값이 있으면 저장 반복
                    if(rs.getInt("isImg") == 1){
                        datas.add(rs.getString("sendUser")+"&"+rs.getString("roomName")+"&image&"+rs.getString("text"));
                    }else{
                        datas.add(rs.getString("sendUser")+"&"+rs.getString("roomName")+"&"+rs.getString("text"));
                    }
                }
            }else{
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, uid);
                pstmt.setString(2, destinationUid);
                pstmt.setString(3, destinationUid);
                pstmt.setString(4, uid);
                rs = pstmt.executeQuery(); // 쿼리수행 결과값을 받아옴
                while (rs.next()) { // 결과값이 있으면 저장 반복
                    if(rs.getInt("isImg") == 1){
                        datas.add(rs.getString("sendUser")+"&"+rs.getString("recvUser")+"&image&"+rs.getString("text"));
                    }else{
                        datas.add(rs.getString("sendUser")+"&"+rs.getString("recvUser")+"&"+rs.getString("text"));
                    }
                }
            }

            rs.close(); // rs 리소스 해제
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect(); // DB연결 해제
        }
        return datas; // 결과값 반환
    }

    public ArrayList<ChatListModel> getChatRoomList(String uid) {
        connect(); // DB연결

        // 쿼리 작성
        String sql = "select * from "+ uid + " WHERE NOT identifier=2" ;

        // 쿼리 수행 후 받는 값을ArrayList로 저장
        ArrayList<ChatListModel> datas = new ArrayList<>();

        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery(); // 쿼리수행 결과값을 받아옴

            while (rs.next()) { // 결과값이 있으면 저장 반복
                datas.add(new ChatListModel(rs.getString("id"), rs.getInt("identifier")));
            }
            rs.close(); // rs 리소스 해제

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect(); // DB연결 해제
        }
        return datas; // 결과값 반환
    }

    public boolean makeChatroom2(String roomName, String interesting, String uid) {
        connect(); // DB연결

        // 쿼리 작성
        // String sql ="insert into studentInfo(id, name, password, deptName,
        // gender,phoneNum, address) values(?,?,?,?,?,?,?)";
        String sql = "insert into roomlist values(?, ?)";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, roomName);
            pstmt.setString(2, interesting);

            pstmt.executeUpdate(); // 쿼리 실행


            sql = "insert into "+uid+" values(?, 1)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, roomName);
            pstmt.executeUpdate(); // 쿼리 실행


        } catch (SQLException e) {
            e.printStackTrace();
            return false; // insert 실패 시 false를 반환
        } finally {
            disconnect(); // DB연결 해제
        }
        return true; // insert 성공 시 false를 반환
    }

    public boolean checkIsRoom(String sendUser, String recvUser) {
        connect(); // DB연결
        System.out.println("sendUser : " + sendUser + " recvUser : " + recvUser);

        // 쿼리 작성
        // String sql ="insert into studentInfo(id, name, password, deptName,
        // gender,phoneNum, address) values(?,?,?,?,?,?,?)";
        String sql = "SELECT identifier from "+sendUser+" where id=? && identifier=1";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, recvUser);

            rs = pstmt.executeQuery();
            if(!rs.next()){
                rs.close();
                return false;
            }else{

                rs.close();
                return true;
            }



        } catch (SQLException e) {
            e.printStackTrace();
            return false; // insert 실패 시 false를 반환
        } finally {
            disconnect(); // DB연결 해제
        }
    }

//    public ArrayList<String> getRoomMessageList(String destinationUid) {
//        connect(); // DB연결
//
//        String sql = "select * from roomlog where roomName=?";
//
//        ArrayList<String> datas = new ArrayList<String>();
//
//        try {
//            pstmt = conn.prepareStatement(sql);
//            pstmt.setString(1, destinationUid);
//            rs = pstmt.executeQuery(); // 쿼리수행 결과값을 받아옴
//
//            while (rs.next()) { // 결과값이 있으면 저장 반복
//                if(rs.getInt("isImg") == 1){
//                    datas.add(rs.getString("sendUser")+"&"+rs.getString("roomName")+"&image&"+rs.getString("text"));
//                }else{
//                    datas.add(rs.getString("sendUser")+"&"+rs.getString("roomName")+"&"+rs.getString("text"));
//                }
//
//                System.out.println(rs.getString("sendUser")+"&"+rs.getString("roomName")+"&"+rs.getString("text"));
//            }
//            rs.close(); // rs 리소스 해제
//            System.out.println("끝남");
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            disconnect(); // DB연결 해제
//        }
//        return datas; // 결과값 반환
//    }


    public ArrayList<RoomModel> getRoomList() {
        connect(); // DB연결

        // 쿼리 작성
        String sql = "select * from roomlist";

        // 쿼리 수행 후 받는 값을ArrayList로 저장
        ArrayList<RoomModel> datas = new ArrayList<>();

        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery(); // 쿼리수행 결과값을 받아옴

            while (rs.next()) { // 결과값이 있으면 저장 반복
                datas.add(new RoomModel(rs.getString("roomName"), rs.getString("interesting")));
            }
            rs.close(); // rs 리소스 해제

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect(); // DB연결 해제
        }
        return datas; // 결과값 반환
    }

    public boolean getInRoom(String roomName, String uid) {
        connect(); // DB연결

        // 쿼리 작성
        // String sql ="insert into studentInfo(id, name, password, deptName,
        // gender,phoneNum, address) values(?,?,?,?,?,?,?)";
//        String sql = "insert into "+uid+" values(?, 1)";
        String sql = "select * from " + uid + " WHERE id=?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, roomName);

            rs = pstmt.executeQuery(); // 쿼리 실행
            if(!rs.next()){
                sql = "insert into "+uid+" values(?, 1)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, roomName);

                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false; // insert 실패 시 false를 반환
        } finally {
            disconnect(); // DB연결 해제
        }
        return true; // insert 성공 시 false를 반환
    }

    public ArrayList<UserModel> getMeetingList(String uid) {
        connect(); // DB연결

        // 쿼리 작성
        String sql = "select * from accountinfo WHERE NOT nickname=?";

        // 쿼리 수행 후 받는 값을ArrayList로 저장
        ArrayList<UserModel> datas = new ArrayList<>();

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,uid);
            rs = pstmt.executeQuery(); // 쿼리수행 결과값을 받아옴

            while (rs.next()) { // 결과값이 있으면 저장 반복
                String desUid = rs.getString("nickname");
                sql = "SELECT id from "+uid+" where identifier=2 && id=?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, desUid);
                ResultSet checkFriend = pstmt.executeQuery();

                if(checkFriend.next()){
                    System.out.println(uid + "와 " + desUid + "는 친구");
                    continue;
                }else{

                    System.out.println(uid + "와 " + desUid + "는 친구가 아니다.");
                    datas.add(new UserModel(desUid, rs.getString("country"), rs.getString("interesting")));
                }
            }
            rs.close(); // rs 리소스 해제

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect(); // DB연결 해제
        }
        return datas; // 결과값 반환
    }

    public boolean addFriend(String uid, String desUid) {
        connect(); // DB연결

        String sql = "SELECT id from "+uid+" where identifier=? && id=?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "2");
            pstmt.setString(2, desUid);

            rs = pstmt.executeQuery();

            if(!rs.next()){
                System.out.println(uid + "table 에 "+ desUid +"가 없어서 넣어줌.");
                sql = "insert into "+uid+" values(?, 2)";

                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, desUid);
                pstmt.executeUpdate(); // 쿼리 실행
                sql = "insert into "+desUid+" values(?, 2)";

                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, uid);
                pstmt.executeUpdate(); // 쿼리 실행
            }else{
                System.out.println(uid + "의 Table에 이미 "+ desUid +" 가 존재한다.");
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // insert 실패 시 false를 반환
        } finally {
            disconnect(); // DB연결 해제
        }
        return true; // insert 성공 시 false를 반환
    }

    public boolean checkFriend(String uid, String desUid) {
        connect(); // DB연결

        String sql = "SELECT id from "+uid+" where identifier=? && id=?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "2");
            pstmt.setString(2, desUid);

            rs = pstmt.executeQuery();

            if(!rs.next()){

                rs.close();
                return true;
            }else{

                rs.close();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // insert 실패 시 false를 반환
        } finally {
            disconnect(); // DB연결 해제
        }
    }

    public ArrayList<String> checkCountry(String uid, String desUid) {
        connect(); // DB연결

        String sql = "SELECT country from accountinfo WHERE nickname=?";
        ArrayList<String> data = new ArrayList<>();

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, uid);
            rs = pstmt.executeQuery();

            rs.next();
            data.add(rs.getString("country"));

            sql = "SELECT country from accountinfo WHERE nickname=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, desUid);
            rs = pstmt.executeQuery();

            rs.next();
            data.add(rs.getString("country"));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect(); // DB연결 해제
        }
        return data;
    }

}
