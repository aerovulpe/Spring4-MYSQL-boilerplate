package com.namespace.dao;

public interface GenericDAO<T, I> {

    I create(T item);

    T retrieve(I id);

    boolean update(T item);

    boolean delete(T item);
}
