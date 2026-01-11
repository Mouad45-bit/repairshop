package com.maven.repairshop;

import com.maven.repairshop.util.HibernateUtil;
import org.hibernate.Session;

public class MainSchemaTest {
    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("Session ouverte. Si hbm2ddl.auto=update, les tables doivent être créées.");
        }
    }
}