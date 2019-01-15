package io.github.haohoangtran.music.mail;

public class MailPending {
    private String to;
    private String body;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public MailPending(String to, String body) {

        this.to = to;
        this.body = body;
    }
}
