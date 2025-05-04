package services;

import entities.Organisation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import utils.JPAUtil;

import java.util.List;

public class OrganisationService implements CRUD<Organisation>{

    @Override
    public void ajouter(Organisation organisation) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            entityManager.persist(organisation);
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
    public void supprimer(Organisation organisation) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            // Ensure the entity is managed before removing it
            if (!entityManager.contains(organisation)) {
                organisation = entityManager.merge(organisation);
            }
            entityManager.remove(organisation);
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
    public void modifier(Organisation organisation) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            entityManager.merge(organisation);
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
    public Organisation afficher(int id) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        Organisation organisation = null;
        
        try {
            organisation = entityManager.find(Organisation.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
        
        return organisation;
    }

    @Override
    public List<Organisation> afficher_tout() {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        List<Organisation> organisations = null;
        
        try {
            organisations = entityManager.createQuery("FROM Organisation", Organisation.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
        
        return organisations;
    }
}
