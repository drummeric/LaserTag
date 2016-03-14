package com.taserlag.lasertag.team;

public interface TeamIterator<T> {
    boolean hasNext();

    T next();

    String currentTeam();

    //void remove();
        //"this is an abomination"
        //  -Dave Small
}
