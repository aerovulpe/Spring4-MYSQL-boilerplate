package com.namespace.dao;

import com.namespace.model.Account;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountDAOImpl extends SessionDAO<Account> implements AccountDAO{

    private static final Logger logger = LoggerFactory.getLogger(AccountDAOImpl.class);

    @Override
    @SuppressWarnings("unchecked")
    public List<Account> findAll() {
        List accounts = getCurrentSession().createQuery("from Account").list();
        logger.info("retrieving the accounts from the database: " + accounts.toString());
        return accounts;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Account> findEnabled() {
        List<Account> accounts = getCurrentSession()
                .createQuery("from Account as user where 'ENABLED' in elements(permissions)").list();

        logger.info("retrieving the accounts from the database: " + accounts.toString());

        return accounts;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Account> findDisabled() {
        List<Account> accounts = getCurrentSession()
                .createQuery("from Account as user where 'ENABLED' not in elements(permissions)").list();

        logger.info("retrieving the accounts from the database: " + accounts.toString());

        return accounts;
    }

    @Override
    public Account getAccount(String username) {
        try {
            Account account = getCurrentSession().get(Account.class, username);

            logger.info("retrieving this account from the database: " + account.toString());

            return account;

        } catch (Exception e) {
            logger.info("cannot retrieve " + username + "'s account from the database.");
            return null;
        }
    }


    @Override
    public void create(Account account) {
        if (account.getPassword() != null)
            account.setPassword(getHashPassword(account.getPassword()));
        getCurrentSession().save(account);
    }

    @Override
    public boolean update(Account account) {
        logger.info("update()");

        if (account == null || account.getUsername() == null)
            return false;

        logger.info("verify if this account already exist " +
                "in the database: " + account.toString());

        Account accountToUpdate = getAccount(account.getUsername());
        if (accountToUpdate == null) {
            logger.info("This account doesn't exist at the database or " +
                    "something was wrong.");
            return false;
        }

        if (CommonHelper.areNotEquals(account.getPassword(), accountToUpdate.getPassword()))
            accountToUpdate.setPassword(getHashPassword(account.getPassword()));
        accountToUpdate.setFirstName(account.getFirstName());
        accountToUpdate.setLastName(account.getLastName());
        accountToUpdate.setEmail(account.getEmail());
        accountToUpdate.setGender(account.getGender());
        accountToUpdate.setLocale(account.getLocale());
        accountToUpdate.setPictureUrl(account.getPictureUrl());
        accountToUpdate.setLocation(account.getLocation());
        accountToUpdate.setRoles(account.getRoles());
        accountToUpdate.setPermissions(account.getPermissions());

        logger.info("Confirmed: this account already exist.");
        getCurrentSession().update(accountToUpdate);
        return true;
    }

    @Override
    public boolean remove(Account account) {
        Account accountToDelete = getAccount(account.getUsername());
        if (accountToDelete == null) {
            logger.info("This account doesn't exist at the database or " +
                    "something was wrong.");
            return false;
        }

        logger.info("Confirmed: this account already exist.");
        getCurrentSession().delete(accountToDelete);
        return true;
    }

    private String getHashPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }
}
