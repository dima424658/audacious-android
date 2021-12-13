package com.maul.audacious;

public class Song {
    private String name;
    private String length;
    private int id;
    private boolean play = false;

    public String getName() {
        return name;
    }

    public void setName(String arg) {
        name = arg;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String arg) {
        length = arg;
    }

    public void setId(int arg) {
        id = arg;
    }

    public int getId() {
        return id;
    }

    public void setPlay() {
        play = true;
    }

    public boolean getPlay()
    {
        return play;
    }

}
