package com.own.demo.thrid.Impl;

import com.own.demo.thrid.RemoteThirdAccount;
import com.own.demo.thrid.TccActionThirdAccount;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : xuhongcao
 * @date Date : 2021-08-03
 */
public class RemoteThirdAccountImpl implements RemoteThirdAccount {


    TccActionThirdAccount tccActionThirdAccount;

    /**
     * 假设包装 第三方接口，异常返回处理中
     * @param txId
     * @param accountNo
     * @param amount
     * @return
     */
    @Override
    public int transfer(String txId, String accountNo, double amount) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("txId", txId);
            tccActionThirdAccount.prepare(params, accountNo, amount);
            tccActionThirdAccount.commit(params);
            return 0;
        }catch (Exception e){
            return 3;
        }

    }

    @Override
    public int queryTransferResult() {
        return 0;
    }
}
