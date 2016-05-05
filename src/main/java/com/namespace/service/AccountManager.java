package com.namespace.service;

import com.namespace.model.Account;

import java.util.List;

public interface AccountManager {
    boolean updateAccount(Account account);

    Account getEnabledAccount(String naturalId);

    Account getAccountByNaturalId(String naturalId);

    Long createNewAccount(Account account) throws Exception;

    List<Account> getEnabledAccounts();

    List<Account> getDisabledAccounts();

    boolean deactivateAccountByNaturalId(String naturalId);

    Account closeAccount(String naturalId);

    Account deleteAccountByNaturalId(String naturalId);
}
