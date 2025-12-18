package org.com.smartpayments.authenticator.core.ports.out.utils;

import java.text.ParseException;
import java.util.Date;

public interface DateUtilsPort {
    Date convertDate(String date, String pattern) throws ParseException;
}
