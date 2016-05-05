package com.namespace.dao;

import com.namespace.model.IpAddress;
import org.springframework.stereotype.Component;

/**
 * Created by Aaron on 24/04/2016.
 */
@Component
public class IpAddressDAOImpl extends SessionDAO<IpAddress, Integer> implements IpAddressDAO {

    @Override
    public Integer create(IpAddress item) throws Exception {
        return (Integer) getCurrentSession().save(item);
    }

    @Override
    public IpAddress retrieve(Integer id) throws Exception {
        return getCurrentSession().get(IpAddress.class, id);
    }

    @Override
    public boolean update(IpAddress item) throws Exception {
        getCurrentSession().update(item);
        return true;
    }

    @Override
    public boolean delete(IpAddress item) throws Exception {
        getCurrentSession().delete(item);
        return true;
    }

    @Override
    public boolean isBanned(String ipAddress) {
        return getIpAddress(ipAddress).isBanned();
    }

    @Override
    public IpAddress getIpAddress(String ipAddress) {
        return getCurrentSession().bySimpleNaturalId(IpAddress.class).load(ipAddress);
    }
}
