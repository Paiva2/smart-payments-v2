package org.com.smartpayments.subscription.core.domain.enums;

import lombok.Getter;

@Getter
public enum ECountry {
    BR("Brasil");

    private final String description;

    ECountry(String country) {
        this.description = country;
    }
}
