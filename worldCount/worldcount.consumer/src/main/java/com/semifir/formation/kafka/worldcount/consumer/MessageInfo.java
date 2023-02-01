package com.semifir.formation.kafka.worldcount.consumer;

public class MessageInfo {
    private String message;
    private String author;
    private Integer size;

    public String getMessage() {
            return message;
    }

    public void setMessage(String message) {
            this.message = message;
    }

    public String getAuthor() {
            return author;
    }

    public void setAuthor(String author) {
            this.author = author;
    }

    public Integer getSize() {
            return size;
    }

    public void setSize(Integer size) {
            this.size = size;
    }

    @Override
    public String toString() {
        return "MessageInfo{" +
                "message='" + message + '\'' +
                ", author='" + author + '\'' +
                ", size=" + size +
                '}';
    }
}
