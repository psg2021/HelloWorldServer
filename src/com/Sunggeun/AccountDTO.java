package com.Sunggeun;

public class AccountDTO {
    private String id;
    private String pw;
    private String country;
    private String nickname;
    private String interesting;

    public AccountDTO() {
    }

    public AccountDTO(String id, String pw, String country, String nickname, String interesting) {
        this.id = id;
        this.pw = pw;
        this.country = country;
        this.nickname = nickname;
        this.interesting = interesting;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getInteresting() {
        return interesting;
    }

    public void setInteresting(String interesting) {
        this.interesting = interesting;
    }
}
