package com.namespace.service;

import com.namespace.dao.AccountDAO;
import com.namespace.dao.IpAddressDAO;
import com.namespace.model.Account;
import com.namespace.model.IpAddress;
import com.namespace.security.BannedIpException;
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

import java.sql.Timestamp;
import java.util.List;

@Service
@Transactional
public class AccountManagerImpl implements AccountManager {

    private static final Logger logger = LoggerFactory.getLogger(AccountManagerImpl.class);

    @Autowired
    private AccountDAO accountDAO;
    @Autowired
    private IpAddressDAO ipAddressDAO;
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
            account.addRole(Account.ROLE_USER);
            account.addPermission(Account.PERMISSION_ENABLED);
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
    public Account updateAccount(String naturalId, boolean details, AccountForm model, BindingResult result) {
        Account account;
        if (details) {
            logger.info("updateAccount() : details");

            accountUpdateDetailsValidator.validate(model, result);

            if (result.hasErrors()) {
                return null;
            } else {
                account = AccountFormAssembler
                        .updateAccountDetailsFromAccountForm(model, getAccountByNaturalId(naturalId));
            }
        } else {
            logger.info("updating password");

            accountUpdatePasswordValidator.validate(model, result);

            if (result.hasErrors()) {
                logger.info("validation error!");
                return null;
            } else {
                account = getAccountByNaturalId(naturalId);
                account.setPassword(model.getPassword());
            }
        }

        updateAccount(account);
        return account;
    }

    @Override
    public IpAddress seenIpAddress(String ipAddress) throws Exception {
        IpAddress item = ipAddressDAO.getIpAddress(ipAddress);
        if (item == null) {
            item = new IpAddress(ipAddress);
            ipAddressDAO.create(item);
        } else {
            item.setLastSeen(new Timestamp(System.currentTimeMillis()));
            item.incrementTimesSeen();
            ipAddressDAO.update(item);
            if (item.isBanned()) {
                // Uh oh
                throw new BannedIpException();
            }
        }

        return item;
    }
}
