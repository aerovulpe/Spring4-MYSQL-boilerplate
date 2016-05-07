package com.namespace.service;

import com.namespace.dao.AccountDAO;
import com.namespace.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AccountManagerImpl implements AccountManager {

    @Autowired
    private AccountDAO accountDAO;

    public AccountManagerImpl() {
    }

    @Override
    public boolean updateAccount(Account account) {
        return account != null && this.accountDAO.update(account);
    }

    @Override
    public Account getEnabledAccount(String naturalId) {
        Account account = accountDAO.getAccount(naturalId);
        return account.hasPermission(Account.PERMISSION_ENABLED) ? account : null;
    }

    @Override
    public Account getAccountByNaturalId(String naturalId) {
        return accountDAO.getAccount(naturalId);
    }

    @Override
    public Long createNewAccount(Account account) {
        return this.accountDAO.create(account);
    }

    @Override
    public List<Account> getEnabledAccounts() {
        return accountDAO.findEnabled();
    }

    @Override
    public List<Account> getDisabledAccounts() {
        return accountDAO.findDisabled();
    }

    @Override
    public boolean deactivateAccountByNaturalId(String naturalId) {
        Account account = accountDAO.getAccount(naturalId);
        account.removePermission(Account.PERMISSION_ENABLED);
        return accountDAO.update(account);
    }

    @Override
    public Account closeAccount(String naturalId) {
        Account account = accountDAO.getAccount(naturalId);
        account.getPermissions().clear();
        account.getRoles().clear();
        return accountDAO.update(account) ? account : null;
    }

    @Override
    public Account deleteAccountByNaturalId(String naturalId) {
        Account account = accountDAO.getAccount(naturalId);
        return accountDAO.delete(account) ? account : null;
    }
}
