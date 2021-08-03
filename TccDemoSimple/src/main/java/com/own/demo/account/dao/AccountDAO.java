
package com.own.demo.account.dao;

import com.own.demo.account.domain.Account;

import java.sql.SQLException;


public interface AccountDAO {

    void addAccount(Account account) throws SQLException;
    
    int updateAmount(Account account) throws SQLException;
    
    int updateFreezedAmount(Account account) throws SQLException;
    
    Account getAccount(String accountNo) throws SQLException;
    
    Account getAccountForUpdate(String accountNo) throws SQLException;
    
    void deleteAllAccount() throws SQLException;

}
