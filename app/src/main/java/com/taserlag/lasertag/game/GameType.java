package com.taserlag.lasertag.game;

import com.google.api.client.util.Value;

public enum GameType {
    @Value
    TDM("Team Deathmatch"),
    @Value
    FFA("Free for All"),
    @Value
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
