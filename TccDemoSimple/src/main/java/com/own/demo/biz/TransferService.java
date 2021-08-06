package com.own.demo.biz;

/**
 * @author : xuhongcao
 * @date Date : 2021-08-03
 */
public interface TransferService {

    /**
     * TCC 转账
     *
     * @param from
     * @param to
     * @param amount
     * @param businessId
     */
    public boolean transferByTcc(final String from, final String to, final double amount);

    /**
     * 定时任务弥补机制
     */
    public void transferTimerJob();

}
