package com.zia.payments.global.util;

import java.time.LocalDateTime;

public class ClockHolder {
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}
