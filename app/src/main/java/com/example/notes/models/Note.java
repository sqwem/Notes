package com.example.notes.models;

import com.google.firebase.Timestamp;

public class Note {

    private String title;
    private String content;
    private Timestamp timestamp;
    private String noteId;
    private int noteColor;
    private int textColor;

    public Note() {
    }

    public Note(String title, String content, Timestamp timestamp, String noteId, int noteColor, int textColor) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.noteId = noteId;
        this.noteColor = noteColor;
        this.textColor = textColor;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public int getNoteColor() {
        return noteColor;
    }

    public void setNoteColor(int noteColor) {
        this.noteColor = noteColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
}
