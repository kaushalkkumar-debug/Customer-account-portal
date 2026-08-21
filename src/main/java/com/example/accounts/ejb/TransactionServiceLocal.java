package com.example.accounts.ejb;

import com.example.accounts.domain.Transaction;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@Local
public interface TransactionServiceLocal {
    int recordTransaction(int accountId, BigDecimal amount, String description) throws SQLException;

    List<Transaction> getTransactionHistory(int accountId) throws SQLException;

    BigDecimal getCurrentBalance(int accountId) throws SQLException;
}
