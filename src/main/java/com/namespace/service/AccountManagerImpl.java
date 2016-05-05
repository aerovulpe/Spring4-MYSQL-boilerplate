package com.namespace.service;

import com.namespace.dao.AccountDAO;
import com.namespace.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AccountManagerImpl implements AccountManager {

    private static final Logger logger = LoggerFactory.getLogger(AccountManagerImpl.class);

    @Autowired
    private AccountDAO accountDAO;

    public AccountManagerImpl() {
    }

    @Override
    public boolean updateAccount(Account account) {
        logger.info("updateAccount()");

        if (account == null)
            return false;

        try {
            logger.info("Trying to update the account using  accountDAO.update() ");
            boolean isUpdatedSuccessfully = this.accountDAO.update(account);
            if (isUpdatedSuccessfully) {
                logger.info("This account was updated successfully" + account.toString());
            } else {
                logger.info("This account was not updated successfully" + account.toString());
            }
            return isUpdatedSuccessfully;

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    @Override
    public Account getEnabledAccount(String naturalId) {
        Account account = accountDAO.getAccount(naturalId);
        return account.hasPermission(Account.PERMISSION_ENABLED) ? account : null;
    }

    @Override
    public Account closeAccount(String naturalId) {
        Account account = accountDAO.getAccount(naturalId);
        account.getPermissions().clear();
        account.getRoles().clear();
        try {
            return accountDAO.update(account) ? account : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Account getAccountByNaturalId(String naturalId) {
        return accountDAO.getAccount(naturalId);
    }

    @Override
    public Long createNewAccount(Account account) throws Exception {
        logger.info("createNewAccount()");

        try {
            logger.info("Trying to create a new account: " + account.toString());
            return this.accountDAO.create(account);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

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
        try {
            return accountDAO.update(account);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Account deleteAccountByNaturalId(String naturalId) {
        Account account = accountDAO.getAccount(naturalId);

        try {
            return accountDAO.delete(account) ? account : null;
        } catch (Exception e) {
            return null;
        }
    }
}
