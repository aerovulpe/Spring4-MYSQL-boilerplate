package com.namespace.dao;

import com.namespace.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountDAOImpl extends SessionDAO<Account, Long> implements AccountDAO {

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
    public Account getAccount(String naturalId) {
        return getCurrentSession().bySimpleNaturalId(Account.class).load(naturalId);
    }


    @Override
    public Long create(Account account) {
        return (Long) getCurrentSession().save(account);
    }

    @Override
    public Account retrieve(Long id) {
        return getCurrentSession().get(Account.class, id);
    }

    @Override
    public boolean update(Account account) {
        logger.info("update()");

        if (account == null || account.getNaturalId() == null) {
            return false;
        }

        logger.info("verify if this account already exist " +
                "in the database: " + account.toString());

        Account accountToUpdate = getAccount(account.getNaturalId());
        if (accountToUpdate == null) {
            logger.info("This account doesn't exist at the database or " +
                    "something was wrong.");
            return false;
        }

        accountToUpdate.setFirstName(account.getFirstName());
        accountToUpdate.setLastName(account.getLastName());
        accountToUpdate.setPassword(account.getPassword());
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
    public boolean delete(Account account) {
        Account accountToDelete = getAccount(account.getNaturalId());
        if (accountToDelete == null) {
            logger.info("This account doesn't exist at the database or " +
                    "something was wrong.");
            return false;
        }

        logger.info("Confirmed: this account already exist.");
        getCurrentSession().delete(accountToDelete);
        return true;
    }
}
