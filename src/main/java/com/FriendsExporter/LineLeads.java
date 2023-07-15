package com.FriendsExporter;

public enum LineLeads {
    None("None", ""),
    Number("[Number]", " "),
    Number1("[Number].", ". "),
    Number2("[Number])", ") "),
    Number3("[Number].)", ".) ")
    ;

    private final String name;
    private final String punctuation;

    public String toString() {
        return this.getName();
    }

    public String getName() {
        return this.name;
    }
    public String getPunctuation() {return this.punctuation;}

    private LineLeads(String name, String punctuation) {
        this.name = name;
        this.punctuation = punctuation;
    }
}
