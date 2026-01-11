package com.maven.repairshop.dao.base;

import org.hibernate.Session;

import java.util.List;

public abstract class BaseDao<T> {

    protected final Class<T> type;

    protected BaseDao(Class<T> type) {
        this.type = type;
    }

    public void save(T entity) {
        HibernateTx.runInTx(session -> session.persist(entity));
    }

    public T update(T entity) {
        return HibernateTx.callInTx(session -> (T) session.merge(entity));
    }

    public void delete(T entity) {
        HibernateTx.runInTx(session -> session.remove(entity));
    }

    public T findById(Long id) {
        return HibernateTx.callInTx(session -> session.get(type, id));
    }

    public List<T> findAll() {
        return HibernateTx.callInTx(session ->
                session.createQuery("from " + type.getSimpleName(), type).getResultList()
        );
    }

    protected Session currentSession(org.hibernate.SessionFactory sf) {
        return sf.getCurrentSession();
    }
}