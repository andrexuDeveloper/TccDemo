package com.own.demo.local.impl;

import com.own.demo.account.dao.AccountDAO;
import com.own.demo.account.dao.AccountTransactionDAO;
import com.own.demo.account.domain.Account;
import com.own.demo.account.domain.AccountTransaction;
import com.own.demo.local.TccActionLocalAccount;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

/**
 * @author : xuhongcao
 * @date Date : 2021-08-01
 */
@Slf4j
public class TccActionLocalAccountImpl implements TccActionLocalAccount {

    protected final static Logger logger = LoggerFactory.getLogger(TccActionLocalAccountImpl.class);
    //账户dao
    private AccountDAO firstAccountDAO;
    //账户流水dao
    private AccountTransactionDAO firstAccountTransactionDAO;
    //A银行事务模版
    private TransactionTemplate tccFirstActionTransactionTemplate;

    @Override
    public boolean prepare(Map<String, Object> actionContext, String accountNo,
                           double amount) {

        String txId= (String) actionContext.get("txId");

        try {
            return tccFirstActionTransactionTemplate.execute(new TransactionCallback<Boolean>() {
                @Override
                public Boolean doInTransaction(TransactionStatus status) {
                    try {
                        System.out.println("执行扣钱参与者(TCC)一阶段，txId:" + txId);

                        //1. 校验账户余额
                        Account account = firstAccountDAO.getAccountForUpdate(accountNo);
                        if (account.getAmount() - account.getFreezedAmount() - amount < 0) {
                            throw new RuntimeException("余额不足");
                        }
                        logger.info("一阶段-校验账户号{}余额{}", account.getAccountNo(), amount);

                        //2. 记录账户操作流水,确保后续对账
                        AccountTransaction accountTransaction = new AccountTransaction();
                        accountTransaction.setTxId(txId);
                        accountTransaction.setAccountNo(accountNo);
                        accountTransaction.setAmount(amount);
                        accountTransaction.setType("minus");
                        accountTransaction.setTxStatus(AccountTransaction.TX_STATUS_PENDING);
                        firstAccountTransactionDAO.addTransaction(accountTransaction);

                        //3. 冻结资金
                        double freezedAmount = account.getFreezedAmount() + amount;
                        account.setFreezedAmount(freezedAmount);
                        firstAccountDAO.updateFreezedAmount(account);
                        logger.info("一阶段-冻结账户号{}金额{}", account.getAccountNo(), freezedAmount);

                    } catch (Exception e) {
                        logger.error("一阶段操作失败", e);
                        throw new RuntimeException("一阶段操作失败", e);
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            System.out.print("first prepare:" + e);
            return false;
        }
    }



    @Override
    public boolean commit(Map<String, Object> actionContext) {
        String txId= (String) actionContext.get("txId");

        return tccFirstActionTransactionTemplate.execute(new TransactionCallback<Boolean>() {


            @Override
            public Boolean doInTransaction(TransactionStatus status) {
                try {
                    System.out.println("执行扣钱参与者(TCC)二阶段（提交），txId:" + txId);

                    //校验账户
                    AccountTransaction accountTransaction = firstAccountTransactionDAO
                            .findTransaction(txId);
                    if(accountTransaction == null){
                        return true;
                    }
                    Account account = firstAccountDAO.getAccountForUpdate(accountTransaction.getAccountNo());
                    logger.info("二阶段-校验账户号{}在扣钱事务{}内", account.getAccountNo(), txId);
                    //扣钱
                    double amount = account.getAmount() - accountTransaction.getAmount();
                    if (amount < 0) {
                        throw new RuntimeException("余额不足");
                    }
                    account.setAmount(amount);
                    firstAccountDAO.updateAmount(account);
                    logger.info("二阶段-扣除账户号{}后的余额为：{}", account.getAccountNo(), amount);
                    //释放冻结金额
                    account.setFreezedAmount(account.getFreezedAmount()  - accountTransaction.getAmount());
                    firstAccountDAO.updateFreezedAmount(account);

                    AccountTransaction updateTrans=new AccountTransaction();
                    updateTrans.setTxId(txId);
                    updateTrans.setTxStatus(AccountTransaction.TX_STATUS_SUCCESS);
                    firstAccountTransactionDAO.updateSelection(updateTrans);

                    logger.info("二阶段-释放扣除账户号{}后的冻结额为：{}", account.getAccountNo(), account.getFreezedAmount());
                    return true;
                } catch (Exception e) {
                    System.out.print("first commit:" + e);
                    status.setRollbackOnly();
                    return false;
                }
            }
        });
    }

    @Override
    public boolean rollback(Map<String, Object> actionContext) {
        String txId= (String) actionContext.get("txId");

        return tccFirstActionTransactionTemplate.execute(new TransactionCallback<Boolean>() {

            @Override
            public Boolean doInTransaction(TransactionStatus status) {
                try {
                    System.out.println("执行扣钱参与者(TCC)二阶段（回滚），txId:" + txId);
                    //
                    AccountTransaction accountTransaction = firstAccountTransactionDAO
                            .findTransaction(txId);
                    if (accountTransaction == null) {
                        return true;
                    }
                    //释放冻结金额
                    Account account = firstAccountDAO.getAccountForUpdate(accountTransaction.getAccountNo());
                    account.setFreezedAmount(account.getFreezedAmount()
                            - accountTransaction.getAmount());
                    firstAccountDAO.updateFreezedAmount(account);
                    //删除流水
                    AccountTransaction updateTrans=new AccountTransaction();
                    updateTrans.setTxId(txId);
                    updateTrans.setTxStatus(AccountTransaction.TX_STATUS_FAIL);
                    firstAccountTransactionDAO.updateSelection(updateTrans);
                    return true;
                } catch (Exception e) {
                    System.out.print("first rollback:" + e);
                    status.setRollbackOnly();
                    return false;
                }
            }
        });
    }

    public void setFirstAccountDAO(AccountDAO firstAccountDAO) {
        this.firstAccountDAO = firstAccountDAO;
    }

    public void setFirstAccountTransactionDAO(
            AccountTransactionDAO firstAccountTransactionDAO) {
        this.firstAccountTransactionDAO = firstAccountTransactionDAO;
    }

    public void setTccFirstActionTransactionTemplate(
            TransactionTemplate tccFirstActionTransactionTemplate) {
        this.tccFirstActionTransactionTemplate = tccFirstActionTransactionTemplate;
    }
}
