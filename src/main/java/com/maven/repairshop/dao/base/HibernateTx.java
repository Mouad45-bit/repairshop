package com.maven.repairshop.dao.base;

import com.maven.repairshop.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.function.Consumer;
import java.util.function.Function;

public final class HibernateTx {

    private HibernateTx() {}

    public static void runInTx(Consumer<Session> action) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            action.accept(session);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public static <T> T callInTx(Function<Session, T> action) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            T result = action.apply(session);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}