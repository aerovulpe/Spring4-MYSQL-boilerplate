package com.namespace.dao;


import com.namespace.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountDAOImpl extends AbstractHibernateDAO<Account, Long> implements AccountDAO {
    private static final Logger logger = LoggerFactory.getLogger(AccountDAOImpl.class);

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
}
