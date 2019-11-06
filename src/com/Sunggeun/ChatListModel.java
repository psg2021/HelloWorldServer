package com.Sunggeun;

public class ChatListModel {
    private String desUid;
    private int isRoom;

    public ChatListModel(String desUid, int isRoom) {
        this.desUid = desUid;
        this.isRoom = isRoom;
    }

    public String getDesUid() {
        return desUid;
    }

    public void setDesUid(String desUid) {
        this.desUid = desUid;
    }

    public int isRoom() {
        return isRoom;
    }

    public void setRoom(int room) {
        isRoom = room;
    }
}
