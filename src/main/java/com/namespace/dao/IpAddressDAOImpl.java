package com.namespace.dao;

import com.namespace.model.IpAddress;
import org.hibernate.Query;
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
    public IpAddress ipUsedByAccount(String ipAddress, String naturalId) {
        Query query = getCurrentSession()
                .createQuery("select ip from IpAddress ip where ip.ipAddress = :ipAddress AND ip.accountNaturalId = :userNaturalId")
                .setParameter("ipAddress", ipAddress)
                .setParameter("userNaturalId", naturalId);
        return (IpAddress) query.uniqueResult();
    }
}
