package com.namespace.dao;


import com.namespace.model.DomainModel;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Aaron on 24/04/2016.
 */
public abstract class AbstractHibernateDAO<T extends DomainModel<I>, I extends Serializable> implements GenericDAO<T, I> {
    @Autowired
    private SessionFactory sessionFactory;
    private final Class<T> genericType;

    @SuppressWarnings("unchecked")
    public AbstractHibernateDAO() {
        this.genericType = (Class<T>) GenericTypeResolver
                .resolveTypeArguments(getClass(), AbstractHibernateDAO.class)[0];
    }

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    @SuppressWarnings("unchecked")
    public I create(T item) {
        return (I) getCurrentSession().save(item);
    }

    @Override
    public T retrieve(I id) {
        return getCurrentSession().get(genericType, id);
    }

    @Override
    public boolean update(T item) {
        if (item == null) {
            return false;
        }

        I id = item.getId();

        if (id == null) {
            return false;
        }

        T itemToUpdate = getCurrentSession().get(genericType, id);
        if (itemToUpdate == null) {
            return false;
        }

        BeanUtils.copyProperties(item, itemToUpdate);

        getCurrentSession().update(itemToUpdate);
        return true;
    }

    @Override
    public boolean delete(T item) {
        if (item == null) {
            return false;
        }

        I id = item.getId();

        if (id == null) {
            return false;
        }

        T itemToDelete = getCurrentSession().get(genericType, id);
        if (itemToDelete == null) {
            return false;
        }

        getCurrentSession().delete(itemToDelete);
        return true;
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        return getCurrentSession().createQuery("from " + genericType.getSimpleName()).list();
    }
}
