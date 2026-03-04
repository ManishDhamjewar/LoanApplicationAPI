package com.rbih.loanservice.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LmsIdProperties {

    public static String PREFIX ;
    public static String SUFFIX;
    public static int LENGTH ;

    @Value("${lms.id.prefix}")
    public void setPrefix(String prefix) { LmsIdProperties.PREFIX = prefix; }

    @Value("${lms.id.suffix}")
    public void setSuffix(String suffix) { LmsIdProperties.SUFFIX = suffix; }

    @Value("${lms.id.length}")
    public void setLength(int length) { LmsIdProperties.LENGTH = length; }
}