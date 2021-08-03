package com.own.demo.thrid.Impl;

import com.own.demo.account.dao.AccountDAO;
import com.own.demo.account.dao.AccountTransactionDAO;
import com.own.demo.account.domain.Account;
import com.own.demo.account.domain.AccountTransaction;
import com.own.demo.thrid.TccActionThirdAccount;
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
public class TccActionThirdAccountImpl  implements TccActionThirdAccount {

    protected final static Logger logger = LoggerFactory.getLogger(TccActionThirdAccountImpl.class);

    private AccountDAO secondAccountDAO;

    private AccountTransactionDAO secondAccountTransactionDAO;

    private TransactionTemplate tccSecondActionTransactionTemplate;

    @Override
    public boolean prepare(Map<String, Object> actionContext, final String accountNo, final double amount) {
        try {
            String txId= (String) actionContext.get("txId");
            tccSecondActionTransactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction(TransactionStatus status) {
                    try {
                        System.out.println("执行加钱参与者(TCC)一阶段，txId:" + txId);

                        //先记一笔账户操作流水
                        AccountTransaction accountTransaction = new AccountTransaction();
                        accountTransaction.setTxId(txId);
                        accountTransaction.setAccountNo(accountNo);
                        accountTransaction.setAmount(amount);
                        accountTransaction.setType("add");
                        accountTransaction.setTxStatus(AccountTransaction.TX_STATUS_PENDING);
                        secondAccountTransactionDAO.addTransaction(accountTransaction);
                        //再递增冻结金额，表示这部分钱已经被冻结，不能使用
                        Account account = secondAccountDAO.getAccountForUpdate(accountNo);
                        double freezedAmount = account.getFreezedAmount() + amount;
                        account.setFreezedAmount(freezedAmount);
                        secondAccountDAO.updateFreezedAmount(account);
                        logger.info("一阶段-准备从账户-{}增加冻结金额：{}", accountNo, freezedAmount);
                    } catch (Exception e) {
                        System.out.print("second prepare:"+ e);
                        throw new RuntimeException("", e);
                    }

                    return true;
                }
            });
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean commit(Map<String, Object> actionContext) {
        String txId= (String) actionContext.get("txId");
        return tccSecondActionTransactionTemplate.execute(new TransactionCallback<Boolean>() {

            @Override
            public Boolean doInTransaction(TransactionStatus status) {
                try {
                    System.out.println("执行加钱参与者(TCC)二阶段（提交），txId:" + txId);

                    //找到账户操作流水
                    AccountTransaction accountTransaction = secondAccountTransactionDAO
                            .findTransaction(txId);
                    if(accountTransaction == null){
                        return true;
                    }
                    Account account = secondAccountDAO.getAccountForUpdate(accountTransaction.getAccountNo());
                    //加钱
                    double amount = account.getAmount() + accountTransaction.getAmount();
                    account.setAmount(amount);
                    secondAccountDAO.updateAmount(account);
                    logger.info("二阶段-提交从账户号{}加钱后的余额：{}", account.getAccountNo(), amount);
                    //冻结金额相应减少
                    account.setFreezedAmount(account.getFreezedAmount()
                            - accountTransaction.getAmount());
                    secondAccountDAO.updateFreezedAmount(account);

                    AccountTransaction updateTrans=new AccountTransaction();
                    updateTrans.setTxId(txId);
                    updateTrans.setTxStatus(AccountTransaction.TX_STATUS_SUCCESS);
                    secondAccountTransactionDAO.updateSelection(updateTrans);
                    logger.info("二阶段-减少账户号{}冻结金额后的冻结额为：{}", account.getAccountNo(), account.getFreezedAmount());
                } catch (Exception e) {
                    System.out.print("second commit:" + e);
                    status.setRollbackOnly();
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public boolean rollback(Map<String, Object> actionContext) {
        String txId= (String) actionContext.get("txId");
        return tccSecondActionTransactionTemplate.execute(new TransactionCallback<Boolean>() {

            @Override
            public Boolean doInTransaction(TransactionStatus status) {
                try {
                    System.out.println("执行加钱参与者(TCC)二阶段（回滚），txId:" + txId);

                    AccountTransaction accountTransaction = secondAccountTransactionDAO
                            .findTransaction(txId);
                    if(accountTransaction == null){
                        return true;
                    }
                    //释放冻结金额
                    Account account = secondAccountDAO.getAccountForUpdate(accountTransaction.getAccountNo());
                    account.setFreezedAmount(account.getFreezedAmount()
                            - accountTransaction.getAmount());
                    secondAccountDAO.updateFreezedAmount(account);

                    AccountTransaction updateTrans=new AccountTransaction();
                    updateTrans.setTxId(txId);
                    updateTrans.setTxStatus(AccountTransaction.TX_STATUS_FAIL);
                    secondAccountTransactionDAO.updateSelection(updateTrans);
                    return true;
                } catch (Exception e) {
                    System.out.print("second rollback:" + e);
                    status.setRollbackOnly();
                    return false;
                }

            }
        });
    }



    public void setSecondAccountDAO(AccountDAO secondAccountDAO) {
        this.secondAccountDAO = secondAccountDAO;
    }

    public void setSecondAccountTransactionDAO(AccountTransactionDAO secondAccountTransactionDAO) {
        this.secondAccountTransactionDAO = secondAccountTransactionDAO;
    }

    public void setTccSecondActionTransactionTemplate(TransactionTemplate tccSecondActionTransactionTemplate) {
        this.tccSecondActionTransactionTemplate = tccSecondActionTransactionTemplate;
    }
}
