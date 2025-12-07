package org.com.smartpayments.authenticator.core.ports.out.dataProvider;

public interface AsyncMessageDataProviderPort {
    void sendMessage(String destination, Object data);

    void sendMessage(String destination, String key, Object data);
}
