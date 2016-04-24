package com.namespace.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.Hibernate;
import org.pac4j.core.profile.Gender;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;

@Entity
@JsonIgnoreProperties({"username", "password", "remembered", "ipAddresses"})
@Table(name = "accounts")
public class Account {
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public static final String PERMISSION_ENABLED = "ENABLED";
    public static final String PERMISSION_EMAIL_VERTIFIED = "EMAIL_VERIFIED";

    @Id
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    private String password;

    private String firstName;
    private String lastName;
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    private Gender gender;
    private String locale;
    private String pictureUrl;
    private String location;
    private boolean isRemembered;

    @ElementCollection
    @CollectionTable(
            name = "roles",
            joinColumns = @JoinColumn(name = "username")
    )
    @Column(name = "role")
    private Set<String> roles;

    @ElementCollection
    @CollectionTable(
            name = "permissions",
            joinColumns = @JoinColumn(name = "username")
    )
    @Column(name = "permission")
    private Set<String> permissions;

    @JoinColumn(name = "username")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "")
    @MapKey(name = "username")
    private Map<String, IpAddress> ipAddresses;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Enumerated(EnumType.STRING)
    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public boolean isRemembered() {
        return isRemembered;
    }

    public void setRemembered(boolean remembered) {
        isRemembered = remembered;
    }

    public Map<String, IpAddress> getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(Map<String, IpAddress> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public void addRole(String role) {
        roles.add(role);
    }

    public void removeRole(String role) {
        roles.remove(role);
    }

    public void addPermission(String permission) {
        permissions.add(permission);
    }

    public void removePermission(String permission) {
        permissions.remove(permission);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public boolean hasPreviouslySeenIp(IpAddress ipAddress) {
        return ipAddresses.containsKey(ipAddress.getIpAddress());
    }

    public Account(@NotNull String username, String password, @NotNull String firstName, @NotNull String lastName,
                   @NotNull String email, @NotNull Set<String> roles,
                   @NotNull Set<String> permissions) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.permissions = permissions;
    }

    public Account() {
    }

    @Override
    public String toString() {
        return "Account{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", gender=" + gender +
                ", locale='" + locale + '\'' +
                ", pictureUrl='" + pictureUrl + '\'' +
                ", location='" + location + '\'' +
                ", roles=" + roles +
                ", permissions=" + permissions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        if (!getUsername().equals(account.getUsername())) return false;
        if (getPassword() != null ? !getPassword().equals(account.getPassword()) : account.getPassword() != null)
            return false;
        if (!getFirstName().equals(account.getFirstName())) return false;
        if (!getLastName().equals(account.getLastName())) return false;
        if (!getEmail().equals(account.getEmail())) return false;
        if (getGender() != account.getGender()) return false;
        if (getLocale() != null ? !getLocale().equals(account.getLocale()) : account.getLocale() != null) return false;
        if (getPictureUrl() != null ? !getPictureUrl().equals(account.getPictureUrl()) : account.getPictureUrl() != null)
            return false;
        if (getLocation() != null ? !getLocation().equals(account.getLocation()) : account.getLocation() != null)
            return false;
        if (!getRoles().equals(account.getRoles())) return false;
        return getPermissions().equals(account.getPermissions());

    }

    @Override
    public int hashCode() {
        int result = getUsername().hashCode();
        result = 31 * result + (getPassword() != null ? getPassword().hashCode() : 0);
        result = 31 * result + getFirstName().hashCode();
        result = 31 * result + getLastName().hashCode();
        result = 31 * result + getEmail().hashCode();
        result = 31 * result + (getGender() != null ? getGender().hashCode() : 0);
        result = 31 * result + (getLocale() != null ? getLocale().hashCode() : 0);
        result = 31 * result + (getPictureUrl() != null ? getPictureUrl().hashCode() : 0);
        result = 31 * result + (getLocation() != null ? getLocation().hashCode() : 0);
        result = 31 * result + getRoles().hashCode();
        result = 31 * result + getPermissions().hashCode();
        return result;
    }
}
