package org.whz.webshell.service;

import java.io.Serializable;

public abstract class BaseService implements Serializable {
    protected String threadName;

    public BaseService setThreadName(String threadName) {
        this.threadName = threadName;
        return this;
    }

    public String getThreadName() {
        return threadName;
    }
}
