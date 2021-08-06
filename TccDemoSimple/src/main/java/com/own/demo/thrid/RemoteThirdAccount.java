package com.own.demo.thrid;

/**
 * @author : xuhongcao
 * @date Date : 2021-08-03
 */
public interface RemoteThirdAccount {

    /**
     *
      * @param txId
     * @param accountNo
     * @param amount
     * @return  0:成功，2 失败，3处理中
     */
    public int transfer(String txId, final String accountNo, final double amount);


    /**
     *
     * @param txId
     * @return  0:成功，2 失败，3处理中
     */
    public int queryTransferResult(String txId);
}
