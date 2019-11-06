package com.Sunggeun;

public class UserModel {
    String nickname;
    String country;
    String interesting;

    public UserModel(String nickname, String country, String interesting) {
        this.nickname = nickname;
        this.country = country;
        this.interesting = interesting;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSex() {
        return country;
    }

    public void setSex(String country) {
        this.country = country;
    }

    public String getInteresting() {
        return interesting;
    }

    public void setInteresting(String interesting) {
        this.interesting = interesting;
    }
}
