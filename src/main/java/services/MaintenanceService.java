package services;

import java.util.List;

import entities.Maintenance;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import utils.JPAUtil;

public class MaintenanceService implements CRUD<Maintenance> {
    
    @Override
    public void ajouter(Maintenance maintenance) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            entityManager.persist(maintenance);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void supprimer(Maintenance maintenance) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            // Ensure the entity is managed before removing it
            if (!entityManager.contains(maintenance)) {
                maintenance = entityManager.merge(maintenance);
            }
            entityManager.remove(maintenance);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void modifier(Maintenance maintenance) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            entityManager.merge(maintenance);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Maintenance afficher(int id) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        Maintenance maintenance = null;
        
        try {
            maintenance = entityManager.find(Maintenance.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
        
        return maintenance;
    }

    @Override
    public List<Maintenance> afficher_tout() {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        List<Maintenance> maintenances = null;
        
        try {
            maintenances = entityManager.createQuery("FROM Maintenance", Maintenance.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
        
        return maintenances;
    }
}
