package com.namespace.service;

import com.namespace.model.Account;
import com.namespace.model.IpAddress;
import com.namespace.service.dto.AccountForm;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface AccountManager {
    boolean updateAccount(Account account);

    Account getEnabledAccount(String naturalId);

    Account getAccountByUserNaturalId(String naturalId);

    Long createNewAccount(Account account) throws Exception;

    List<Account> getEnabledAccounts();

    List<Account> getDisabledAccounts();

    boolean deactivateAccountByNaturalId(String naturalId);

    Account closeAccount(String naturalId);

    Account deleteAccountByNaturalId(String naturalId);

    Account createNewAccount(AccountForm model, BindingResult result) throws Exception;

    Account updateAccount(String naturalId, boolean details, AccountForm model, BindingResult result);

    IpAddress seenIpAddress(Account account, String ipAddress) throws Exception;
}
