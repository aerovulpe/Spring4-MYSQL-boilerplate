package com.namespace.service;

import com.namespace.dao.AccountDAO;
import com.namespace.model.Account;
import com.namespace.service.dto.AccountForm;
import com.namespace.service.dto.AccountFormAssembler;
import com.namespace.service.validator.AccountCreationValidator;
import com.namespace.service.validator.AccountUpdateDetailsValidator;
import com.namespace.service.validator.AccountUpdatePasswordValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;

@Service
@Transactional
public class AccountManagerImpl implements AccountManager {

    private static final Logger logger = LoggerFactory.getLogger(AccountManagerImpl.class);

    @Autowired
    private AccountDAO accountDAO;
    @Autowired
    private AccountCreationValidator accountCreationValidator;
    @Autowired
    private AccountUpdatePasswordValidator accountUpdatePasswordValidator;
    @Autowired
    private AccountUpdateDetailsValidator accountUpdateDetailsValidator;

    public AccountManagerImpl(AccountDAO accountDAO, AccountCreationValidator accountCreationValidator,
                              AccountUpdatePasswordValidator accountUpdatePasswordValidator,
                              AccountUpdateDetailsValidator accountUpdateDetailsValidator) {
        this.accountDAO = accountDAO;
        this.accountCreationValidator = accountCreationValidator;
        this.accountUpdatePasswordValidator = accountUpdatePasswordValidator;
        this.accountUpdateDetailsValidator = accountUpdateDetailsValidator;
    }

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
    public Account getEnabledAccount(String username) {
        Account account = accountDAO.getAccount(username);
        return account.hasPermission(Account.PERMISSION_ENABLED) ? account : null;
    }

    @Override
    public Account closeAccount(String username) {
        Account account = accountDAO.getAccount(username);
        account.getPermissions().clear();
        account.getRoles().clear();
        try {
            return accountDAO.update(account) ? account : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Account getAccountByUsername(String username) {
        return accountDAO.getAccount(username);
    }

    @Override
    public void createNewAccount(Account account) throws Exception {
        logger.info("createNewAccount()");

        try {
            logger.info("Trying to create a new account: " + account.toString());
            account.addRole(Account.ROLE_USER);
            account.addPermission(Account.PERMISSION_ENABLED);
            this.accountDAO.create(account);
            logger.info("New account created successfully");
        } catch (Exception e) {
            logger.error(e.toString());
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
    public boolean deactivateAccountByUsername(String username) {
        Account account = accountDAO.getAccount(username);
        account.removePermission(Account.PERMISSION_ENABLED);
        try {
            return accountDAO.update(account);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Account deleteAccountByUsername(String username) {
        Account account = accountDAO.getAccount(username);

        try {
            return accountDAO.remove(account) ? account : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Account createNewAccount(AccountForm model, BindingResult result) throws Exception {
        accountCreationValidator.validate(model, result);

        if (result.hasErrors()) {
            return null;
        } else {
            Account account = AccountFormAssembler.copyNewAccountFromAccountForm(model);
            createNewAccount(account);
            return account;
        }
    }

    @Override
    public Account updateAccount(String username, boolean details, AccountForm model, BindingResult result) {
        Account account;
        if (details) {
            logger.info("updateAccount() : details");

            accountUpdateDetailsValidator.validate(model, result);

            if (result.hasErrors()) {
                return null;
            } else {
                account = AccountFormAssembler
                        .updateAccountDetailsFromAccountForm(model, getAccountByUsername(username));
            }
        } else {
            logger.info("updating password");

            accountUpdatePasswordValidator.validate(model, result);

            if (result.hasErrors()) {
                logger.info("validation error!");
                return null;
            } else {
                account = getAccountByUsername(username);
                account.setPassword(model.getPassword());
            }
        }

        updateAccount(account);
        return account;
    }
}
