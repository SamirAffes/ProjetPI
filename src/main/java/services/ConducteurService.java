package services;

import entities.Conducteur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import utils.JPAUtil;

import java.util.List;

public class ConducteurService implements CRUD<Conducteur> {

    @Override
    public void ajouter(Conducteur conducteur) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            entityManager.persist(conducteur);
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
    public void supprimer(Conducteur conducteur) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            // Ensure the entity is managed before removing it
            if (!entityManager.contains(conducteur)) {
                conducteur = entityManager.merge(conducteur);
            }
            entityManager.remove(conducteur);
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
    public void modifier(Conducteur conducteur) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            // Explicitly load the existing entity with its collections
            Conducteur existingConducteur = entityManager.find(Conducteur.class, conducteur.getId());

            System.out.println("[DEBUG_LOG] Updating driver: " + existingConducteur.getNom() + " " + existingConducteur.getPrenom());


            // Update the fields
            existingConducteur.setNom(conducteur.getNom());
            existingConducteur.setPrenom(conducteur.getPrenom());
            existingConducteur.setCin(conducteur.getCin());
            existingConducteur.setAdresse(conducteur.getAdresse());
            existingConducteur.setTelephone(conducteur.getTelephone());
            existingConducteur.setEmail(conducteur.getEmail());
            existingConducteur.setDateNaissance(conducteur.getDateNaissance());
            existingConducteur.setDateEmbauche(conducteur.getDateEmbauche());
            existingConducteur.setNumeroPermis(conducteur.getNumeroPermis());
            existingConducteur.setPhoto(conducteur.getPhoto());
            existingConducteur.setStatut(conducteur.getStatut());
            existingConducteur.setVehiculeId(conducteur.getVehiculeId());


            // Merge the updated entity
            entityManager.merge(existingConducteur);
            transaction.commit();

            System.out.println("[DEBUG_LOG] Driver updated successfully");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println("[DEBUG_LOG] Error updating driver: " + e.getMessage());
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Conducteur afficher(int id) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        Conducteur conducteur = null;

        try {
            conducteur = entityManager.find(Conducteur.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }

        return conducteur;
    }

    @Override
    public List<Conducteur> afficher_tout() {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        List<Conducteur> conducteurs = null;

        try {
            conducteurs = entityManager.createQuery("FROM Conducteur", Conducteur.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }

        return conducteurs;
    }
}
