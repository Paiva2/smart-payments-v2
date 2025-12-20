package org.com.smartpayments.subscription.infra.adapters.utils;

import org.com.smartpayments.subscription.core.ports.out.utils.DateUtilsPort;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class DateUtilsAdapter implements DateUtilsPort {
    @Override
    public Date convertDate(String date, String pattern) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.parse(date);
    }

    @Override
    public String formatDate(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }
}