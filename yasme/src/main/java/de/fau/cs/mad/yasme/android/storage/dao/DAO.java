package de.fau.cs.mad.yasme.android.storage.dao;

import java.util.List;

/**
 * Created by bene on 11.07.14.
 */
public interface DAO<T> {


    public T addIfNotExists(T data);


    public T addOrUpdate(T data);


    public T update(T data);


    public T get(long id);


    public List<T> getAll();


    public boolean delete(T data);
}
