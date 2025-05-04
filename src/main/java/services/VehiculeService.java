package services;

import entities.Vehicule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import utils.JPAUtil;

import java.util.List;

public class VehiculeService implements CRUD<Vehicule> {
    
    @Override
    public void ajouter(Vehicule vehicule) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            entityManager.persist(vehicule);
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
    public void supprimer(Vehicule vehicule) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            // Ensure the entity is managed before removing it
            if (!entityManager.contains(vehicule)) {
                vehicule = entityManager.merge(vehicule);
            }
            entityManager.remove(vehicule);
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
    public void modifier(Vehicule vehicule) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            entityManager.merge(vehicule);
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
    public Vehicule afficher(int id) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        Vehicule vehicule = null;
        
        try {
            vehicule = entityManager.find(Vehicule.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
        
        return vehicule;
    }

    @Override
    public List<Vehicule> afficher_tout() {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        List<Vehicule> vehicules = null;
        
        try {
            vehicules = entityManager.createQuery("FROM Vehicule", Vehicule.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
        
        return vehicules;
    }
}
