
package com.own.demo.account.dao;

import com.own.demo.account.domain.AccountTransaction;

import java.sql.SQLException;


public interface AccountTransactionDAO {

    void addTransaction(AccountTransaction accountTransaction) throws SQLException;
    
    AccountTransaction findTransaction(String txId) throws SQLException;
 
    void deleteTransaction(String txId) throws SQLException;
    
    void deleteAllTransaction() throws SQLException;

    int updateSelection(AccountTransaction accountTransaction)throws SQLException;
}
