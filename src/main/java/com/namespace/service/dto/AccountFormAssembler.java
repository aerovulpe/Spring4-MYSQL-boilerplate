package com.namespace.service.dto;

import com.namespace.model.Account;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashSet;

public class AccountFormAssembler {

    public static AccountForm createAccountForm(@NotNull Account account) {
        AccountForm form = new AccountForm();
        extractCommons(form, account);
        form.setPassword(account.getPassword());
        return form;
    }

    public static Account copyNewAccountFromAccountForm(@NotNull AccountForm form) {
        return new Account(form.getUsername(), form.getPassword(), form.getFirstName(), form.getLastName(),
                form.getEmail(), new HashSet<>(Collections.singletonList(Account.ROLE_USER)), new HashSet<String>());
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
        HashSet<String> roles = new HashSet<>();
        HashSet<String> permissions = new HashSet<>();

        roles.add(Account.ROLE_USER);
        if (form.isAdmin())
            roles.add(Account.ROLE_ADMIN);
        if (form.isEnabled())
            permissions.add(Account.PERMISSION_ENABLED);

        return new Account(form.getUsername(), form.getPassword(), form.getFirstName(), form.getLastName(),
                form.getEmail(), roles, permissions);
    }

    private static void extractCommons(AccountForm form, @NotNull Account account) {
        form.setFirstName(account.getFirstName());
        form.setLastName(account.getLastName());
        form.setEmail(account.getEmail());
    }
}
