package com.rbih.loanservice.config;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class LmsIdGenerator implements IdentifierGenerator {

    private static final AtomicLong counter = new AtomicLong(1);

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        long nextVal = counter.getAndIncrement();
        String paddedNumber = String.format("%0" + LmsIdProperties.LENGTH + "d", nextVal);
        return LmsIdProperties.PREFIX + paddedNumber + LmsIdProperties.SUFFIX;
    }
}