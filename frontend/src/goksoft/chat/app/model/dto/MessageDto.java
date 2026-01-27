package goksoft.chat.app.model.dto;

public class MessageDto {
    private String sender;
    private String receiver;
    private String content;
    private String timestamp;

    public MessageDto() {}

    public MessageDto(String sender, String receiver, String content, String timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "MessageDto{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", content='" + content + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}