package org.com.smartpayments.subscription.core.ports.out.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewClientPaymentGatewayClientInput {
    private String name;
    private String cpfCnpj;
    private String email;
    private String phone;
    private String mobilePhone;
    private String address;
    private String addressNumber;
    private String complement;
    private String province; // neighbourhood
    private String postalCode;
    private String externalReference;
    private final Boolean foreignCustomer = false;
}
