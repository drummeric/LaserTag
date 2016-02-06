package com.taserlag.lasertag.game;


public enum GameType {
    TDM("Team Deathmatch"),
    FFA("Free for All"),
    VIP("VIP");

    private String gameName;

    GameType(String string){
       gameName = string;
    }

    public String toString(){
       return gameName;
    }

    public static GameType decodeType(String str){
        switch(str){
            case "Team Deathmatch":
                return TDM;
            case "Free for All":
                return FFA;
            case "VIP":
                return VIP;
            default:
                return TDM;
        }
    }
}
