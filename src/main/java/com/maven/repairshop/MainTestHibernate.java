package com.maven.repairshop;

import com.maven.repairshop.util.HibernateUtil;
import org.hibernate.Session;

public class MainTestHibernate {
    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("Hibernate OK, session ouverte !");
        } catch (Exception e) {
            System.out.println("Problème Hibernate :");
            e.printStackTrace();
        }
    }
}