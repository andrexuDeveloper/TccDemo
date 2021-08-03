/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package com.own.demo.account.domain;

import lombok.Data;

@Data
public class AccountTransaction {

    public static final int TX_STATUS_PENDING=1;
    public static final int TX_STATUS_FAIL=2;
    public static final int TX_STATUS_SUCCESS=3;


    /**
     * 事务id
     */
    private String txId;
    /**
     * 操作账户
     */
    private String accountNo;
    /**
     * 操作金额
     */
    private double amount;
    /**
     * 操作类型，加钱还是减钱
     * 类型 1扣钱 2加钱
     */
    private String type;

    /**
     * 状态 1处理，2失败 3成功
     */
    private int txStatus;



    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    /**
     * Getter method for property <tt>accountNo</tt>.
     * 
     * @return property value of accountNo
     */
    public String getAccountNo() {
        return accountNo;
    }

    /**
     * Setter method for property <tt>accountNo</tt>.
     * 
     * @param accountNo value to be assigned to property accountNo
     */
    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    /**
     * Getter method for property <tt>amount</tt>.
     * 
     * @return property value of amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Setter method for property <tt>amount</tt>.
     * 
     * @param amount value to be assigned to property amount
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Getter method for property <tt>type</tt>.
     * 
     * @return property value of type
     */
    public String getType() {
        return type;
    }

    /**
     * Setter method for property <tt>type</tt>.
     * 
     * @param type value to be assigned to property type
     */
    public void setType(String type) {
        this.type = type;
    }

}
