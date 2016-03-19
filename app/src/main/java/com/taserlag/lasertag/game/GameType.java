package com.taserlag.lasertag.game;


public enum GameType {
    TDM("TDM"),
    FFA("FFA"),
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
            case "TDM":
                return TDM;
            case "FFA":
                return FFA;
            case "VIP":
                return VIP;
            default:
                return TDM;
        }
    }
}
