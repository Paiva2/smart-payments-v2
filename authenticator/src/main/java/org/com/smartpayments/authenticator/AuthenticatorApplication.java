package org.com.smartpayments.authenticator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class AuthenticatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthenticatorApplication.class, args);
    }

}
