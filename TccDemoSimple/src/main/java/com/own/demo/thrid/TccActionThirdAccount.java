package com.own.demo.thrid;


import java.util.Map;

public interface TccActionThirdAccount {

    /**
     *
     * @param actionContext
     * @param a
     * @return
     */
    public boolean prepare(Map<String, Object> actionContext,
                           final String accountNo, final double amount);


    public boolean commit(Map<String, Object> actionContext);


    public boolean rollback(Map<String, Object> actionContext);
}
