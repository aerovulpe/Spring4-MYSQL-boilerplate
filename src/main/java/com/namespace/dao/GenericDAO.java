package com.namespace.dao;

public interface GenericDAO<T, I> {

    I create(T item) throws Exception;

    T retrieve(I id) throws Exception;

    boolean update(T item) throws Exception;

    boolean delete(T item) throws Exception;
}
