package com.namespace.dao;

import com.namespace.model.Account;

import java.util.List;

public interface AccountDAO extends GenericDAO<Account> {

    List<Account> findAll();

    List<Account> findEnabled();

    List<Account> findDisabled();

    Account getAccount(String username);
}