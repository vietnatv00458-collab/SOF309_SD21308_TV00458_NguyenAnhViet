package utils;

public interface EmailGateway {

    void send(String toAddress, String subject, String body);
}
