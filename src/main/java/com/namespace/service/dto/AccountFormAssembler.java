package com.namespace.service.dto;

import com.namespace.model.Account;

import javax.validation.constraints.NotNull;

public class AccountFormAssembler {

    public static AccountForm createAccountForm(@NotNull Account account) {
        AccountForm form = new AccountForm();
        extractCommons(form, account);
        form.setPassword(account.getPassword());
        return form;
    }

    public static Account copyNewAccountFromAccountForm(@NotNull AccountForm form) {
        return new Account(form.getUsername(), form.getPassword(), form.getFirstName(), form.getLastName(),
                form.getEmail());
    }

    public static Account updateAccountDetailsFromAccountForm(@NotNull AccountForm form, Account account) {
        extractCommons(form, account);
        return account;
    }

    public static AccountForm createAccountFormAdmin(@NotNull Account account) {
        AccountForm form = new AccountForm();
        extractCommons(form, account);
        form.setAdmin(account.hasRole(Account.ROLE_ADMIN));
        form.setEnabled(account.hasPermission(Account.PERMISSION_ENABLED));
        form.setBannedUser(account.getRoles().isEmpty());
        form.setPassword(account.getPassword());
        return form;
    }

    public static Account copyNewAccountFromAccountFormAdmin(@NotNull AccountForm form) {
        Account account = new Account(form.getUsername(), form.getPassword(), form.getFirstName(), form.getLastName(),
                form.getEmail());
        account.addRole(Account.ROLE_USER);
        if (form.isAdmin())
            account.addRole(Account.ROLE_ADMIN);
        if (form.isEnabled())
            account.addPermission(Account.PERMISSION_ENABLED);

        return account;
    }

    private static void extractCommons(AccountForm form, @NotNull Account account) {
        form.setFirstName(account.getFirstName());
        form.setLastName(account.getLastName());
        form.setEmail(account.getEmail());
    }
}
