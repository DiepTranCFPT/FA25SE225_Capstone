package com.fa25se225.capstone.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.ZoneId;

@Component
public class RequestContextUtil {

    public String getTimezoneOrDefault() {
        try {
            HttpServletRequest request = getHttpServletRequest();
            String tz = request.getHeader("X-Timezone");
            return StringUtils.hasText(tz) ? tz : "Asia/Ho_Chi_Minh";
        } catch (Exception e) {
            return "Asia/Ho_Chi_Minh";
        }
    }

    public ZoneId getZoneIdOrDefault() {
        return ZoneId.of(getTimezoneOrDefault());
    }

    public String getCurrentIpAddress() {
        try {
            return getHttpServletRequest().getRemoteAddr();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public String getCurrentUserAgent() {
        try {
            return getHttpServletRequest().getHeader("User-Agent");
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private HttpServletRequest getHttpServletRequest(){
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
    }
}
