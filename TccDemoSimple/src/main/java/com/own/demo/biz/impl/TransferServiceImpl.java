package com.own.demo.biz.impl;


import com.own.demo.biz.TransferService;
import com.own.demo.local.TccActionLocalAccount;
import com.own.demo.thrid.RemoteThirdAccount;
import com.own.demo.thrid.TccActionThirdAccount;
import com.own.demo.utils.UUIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 提现交易
 */
public class TransferServiceImpl implements TransferService {

    protected final static Logger logger = LoggerFactory.getLogger(TransferServiceImpl.class);


    private TccActionLocalAccount tccActionLocalAccount;
    private TccActionThirdAccount tccActionThirdAccount;
    private RemoteThirdAccount remoteThirdAccount;


    @Override
    public boolean transferByTcc(String from, String to, double amount) {
        //获取当前事务上下文的事务ID号
        String txId = UUIDUtils.getUUID();
        logger.info("事务开启，txId:{}", txId);
        //交易上下文
        Map<String, Object> actionContext = new HashMap<>();
        actionContext.put("txId", txId);
        try {
            //准备从账户from扣钱
            boolean ret = tccActionLocalAccount.prepare(actionContext, from, amount);
            if (!ret) {
                //事务回滚
                throw new RuntimeException("firstTccAction failed.");
            }
            logger.info("一阶段-准备从账户-{}扣钱{}", from, amount);
            //准备从账户to加钱
            int result = remoteThirdAccount.transfer(txId, to, amount);
            //处理中
            if (result == 3) {
                //事务回滚
                return true;
            }
            //成功
            if (result == 0) {
                //事务回滚
                tccActionLocalAccount.commit(actionContext);
            }

            if (result == 2) {
                //事务回滚
                tccActionLocalAccount.rollback(actionContext);
            }

            logger.info("一阶段-准备从账户-{}加钱{}", to, amount);
            logger.info("事务结束，txId:" + txId);

            return ret;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    @Override
    public void transferTimerJob() {
        String txId = "";
        //交易上下文
        Map<String, Object> actionContext = new HashMap<>();
        actionContext.put("txId", txId);
        int result = remoteThirdAccount.queryTransferResult(txId);
        //处理中
        if (result == 3) {
            //事务回滚
            return;
        }
        //成功
        if (result == 0) {
            //事务回滚
            tccActionLocalAccount.commit(actionContext);
            return;
        }

        if (result == 2) {
            //事务回滚
            tccActionLocalAccount.rollback(actionContext);
            return;
        }
    }



}
