package com.own.demo.account.dao;

import com.own.demo.account.domain.AccountTransaction;
import org.mybatis.spring.SqlSessionTemplate;

import java.sql.SQLException;


public class AccountTransactionDAOImpl implements AccountTransactionDAO {

    public SqlSessionTemplate sqlSession;

    public void setSqlSession(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }


    @Override
    public void addTransaction(AccountTransaction accountTransaction) throws SQLException {
        sqlSession.insert("addAccountTransaction", accountTransaction);
    }

    @Override
    public AccountTransaction findTransaction(String txId) throws SQLException {
        return (AccountTransaction) sqlSession.selectOne("getAccountTransaction", txId);
    }


    @Override
    public void deleteTransaction(String txId) throws SQLException {
        sqlSession.delete("deleteAccountTransaction", txId);
    }

    @Override
    public void deleteAllTransaction() throws SQLException {
        sqlSession.delete("deleteAllTransaction");
    }


}
