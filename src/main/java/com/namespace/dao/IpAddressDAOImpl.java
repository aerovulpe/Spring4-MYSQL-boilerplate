package com.namespace.dao;

import com.namespace.model.IpAddress;
import org.hibernate.Query;
import org.springframework.stereotype.Component;

/**
 * Created by Aaron on 24/04/2016.
 */
@Component
public class IpAddressDAOImpl extends SessionDAO<IpAddress> implements IpAddressDAO {

    @Override
    public void create(IpAddress item) throws Exception {
        getCurrentSession().save(item);
    }

    @Override
    public boolean update(IpAddress item) throws Exception {
        getCurrentSession().update(item);
        return true;
    }

    @Override
    public boolean remove(IpAddress item) throws Exception {
        getCurrentSession().delete(item);
        return true;
    }

    @Override
    public IpAddress ipUsedByAccount(String ipAddress, String username) {
        Query query = getCurrentSession()
                .createQuery("select ip from IpAddress ip where ip.ipAddress = :ipAddress AND ip.username = :username")
                .setParameter("ipAddress", ipAddress)
                .setParameter("username", username);
        return (IpAddress) query.uniqueResult();
    }
}