package com.FriendsExporter;


import net.runelite.client.util.Text;

public class PlayerListItem {

    private String name;
    private String previousName;
    private String note;
    private String rank;


    PlayerListItem() {
        this.name = "";
        this.previousName = "";
        this.note = "";
        this.rank = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Text.toJagexName(Text.removeTags(name));
    }

    public String getPreviousName() {
        return previousName;
    }

    public void setPreviousName(String previousName) {
        this.previousName = Text.toJagexName(Text.removeTags(previousName));
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }
}
