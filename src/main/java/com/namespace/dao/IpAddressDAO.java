package com.namespace.dao;

import com.namespace.model.IpAddress;

/**
 * Created by Aaron on 24/04/2016.
 */
public interface IpAddressDAO extends GenericDAO<IpAddress> {
    IpAddress ipUsedByAccount(String ipAddress, String username);
}