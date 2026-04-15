package dawn.httt.server.integration.email;

public interface EmailSender {

    void send(String to, String subject, String content);
}
