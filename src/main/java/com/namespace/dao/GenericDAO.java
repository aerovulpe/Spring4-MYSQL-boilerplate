package com.namespace.dao;

public interface GenericDAO<T> {

    void create(T item) throws Exception;

    boolean update(T item) throws Exception;

    boolean remove(T item) throws Exception;
}
