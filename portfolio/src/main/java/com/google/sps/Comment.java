package com.google.sps;

import java.time.Instant;

public class Comment {
    private final String id;
    private final String author;
    private final String text;
    private final Instant timestamp;

    public Comment(final String id, final String author, final String text, final Instant timestamp) {
        this.id = id;
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getId() {
        return this.id;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getText() {
        return this.text;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }
}