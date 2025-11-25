package org.com.smartpayments.authenticator.core.ports.out.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.authenticator.core.domain.enums.EBrState;
import org.com.smartpayments.authenticator.core.domain.enums.ECountry;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressOutput implements Serializable {
    private Long id;
    private String street;
    private String neighborhood;
    private String number;
    private String zipcode;
    private String complement;
    private String city;
    private EBrState state;
    private ECountry country;
    private Date updatedAt;
}
