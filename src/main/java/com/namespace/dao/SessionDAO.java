package com.namespace.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Aaron on 24/04/2016.
 */
public abstract class SessionDAO<T, I> implements GenericDAO<T, I> {
    @Autowired
    private SessionFactory sessionFactory;

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
}
