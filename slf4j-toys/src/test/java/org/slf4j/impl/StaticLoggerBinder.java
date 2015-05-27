package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

public final class StaticLoggerBinder implements LoggerFactoryBinder {

    public static final String REQUESTED_API_VERSION = "1.6";
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    @Override
	public ILoggerFactory getLoggerFactory() {
        return TestLoggerFactory.getInstance();
    }

    @Override
	public String getLoggerFactoryClassStr() {
        return TestLoggerFactory.class.getName();
    }
}
