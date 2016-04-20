package com.namespace.dao;

import com.namespace.model.Account;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountDAOImpl implements AccountDAO {

    private static final Logger logger = LoggerFactory.getLogger(AccountDAOImpl.class);
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    @SuppressWarnings("unchecked")
    public List<Account> findAll() {
        List accounts = getCurrentSession().createQuery("from com.namespace.model.Account").list();
        logger.info("retrieving the accounts from the database: " + accounts.toString());
        return accounts;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Account> findEnabled() {
        List<Account> accounts = getCurrentSession()
                .createQuery("from com.namespace.model.Account as Account where Account.enabled = TRUE").list();

        logger.info("retrieving the accounts from the database: " + accounts.toString());

        return accounts;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Account> findDisabled() {
        List<Account> accounts = getCurrentSession()
                .createQuery("from com.namespace.model.Account as Account where Account.enabled = FALSE").list();

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
            logger.info("cannot retrieve the " + username + "'s account from the database. Should be for two reasons: " +
                    "The account associated with this user doesn't exist, of there are not any accounts in the database");
            return null;
        }
    }


    @Override
    public void create(Account account) {
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
        if(accountToUpdate == null){
            logger.info("This account doesn't exist at the database or " +
                    "something was wrong.");
            return false;
        }

        accountToUpdate.setPassword(account.getPassword());
        accountToUpdate.setFirstName(account.getFirstName());
        accountToUpdate.setLastName(account.getLastName());
        accountToUpdate.setEmail(account.getEmail());
        accountToUpdate.setAdmin(account.isAdmin());
        accountToUpdate.setEnabled(account.isEnabled());
        accountToUpdate.setBannedUser(account.isBannedUser());
        accountToUpdate.setAccountNonExpired(account.isAccountNonExpired());

        logger.info("Confirmed: this account already exist.");
        getCurrentSession().update(accountToUpdate);
        return true;
    }

    @Override
    public boolean remove(Account account) {
        Account accountToDelete = getAccount(account.getUsername());
        if(accountToDelete == null){
            logger.info("This account doesn't exist at the database or " +
                    "something was wrong.");
            return false;
        }

        logger.info("Confirmed: this account already exist.");
        getCurrentSession().delete(accountToDelete);
        return true;
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
}
