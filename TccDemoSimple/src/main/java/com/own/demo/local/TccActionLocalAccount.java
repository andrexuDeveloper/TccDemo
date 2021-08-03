package com.own.demo.local;


import java.util.Map;

public interface TccActionLocalAccount {

    /**
     *
     * @param actionContext
     * @param a
     * @return
     */
    public boolean prepare(Map<String, Object> actionContext,
                           String accountNo,
                           double amount);


    public boolean commit(Map<String, Object> actionContext);


    public boolean rollback(Map<String, Object> actionContext);
}
