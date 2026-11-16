package utils;

public final class EmailGatewayFactory {

    private EmailGatewayFactory() {
    }

    public static EmailGateway createDefault() {
        return new JakartaMailEmailGateway(
                EmailConfig.getSmtpHost(),
                EmailConfig.getSmtpPort(),
                EmailConfig.isStartTlsEnabled(),
                EmailConfig.isSslEnabled(),
                EmailConfig.getUsername(),
                EmailConfig.getPassword(),
                EmailConfig.getFromAddress(),
                EmailConfig.getFromName()
        );
    }
}
