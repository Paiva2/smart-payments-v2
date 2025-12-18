package org.com.smartpayments.authenticator.infra.adapters.utils;

import org.com.smartpayments.authenticator.core.ports.out.utils.DateUtilsPort;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class DateUtilsAdapter implements DateUtilsPort {
    @Override
    public Date convertDate(String date, String pattern) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.parse(date);
    }
}
