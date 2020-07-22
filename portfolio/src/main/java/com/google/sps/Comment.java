package com.google.sps;

public class Comment {
    private final long id;
    private final String author;
    private final String text;
    private final long timestamp;

    public Comment(final long id, final String author, final String text, final long timestamp) {
        this.id = id;
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
    }

    public long getId() {
        return this.id;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getText() {
        return this.text;
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}