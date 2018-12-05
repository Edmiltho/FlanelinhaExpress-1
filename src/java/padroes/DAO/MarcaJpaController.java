/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package padroes.DAO;

import ClassesEntidade.Marca;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import ClassesEntidade.Modelo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import padroes.DAO.exceptions.NonexistentEntityException;
import padroes.DAO.exceptions.PreexistingEntityException;
import padroes.DAO.exceptions.RollbackFailureException;

/**
 *
 * @author Cacherow
 */
public class MarcaJpaController implements Serializable {

    public MarcaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Marca marca) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (marca.getModeloCollection() == null) {
            marca.setModeloCollection(new ArrayList<Modelo>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<Modelo> attachedModeloCollection = new ArrayList<Modelo>();
            for (Modelo modeloCollectionModeloToAttach : marca.getModeloCollection()) {
                modeloCollectionModeloToAttach = em.getReference(modeloCollectionModeloToAttach.getClass(), modeloCollectionModeloToAttach.getId());
                attachedModeloCollection.add(modeloCollectionModeloToAttach);
            }
            marca.setModeloCollection(attachedModeloCollection);
            em.persist(marca);
            for (Modelo modeloCollectionModelo : marca.getModeloCollection()) {
                Marca oldFkMarcaIdOfModeloCollectionModelo = modeloCollectionModelo.getFkMarcaId();
                modeloCollectionModelo.setFkMarcaId(marca);
                modeloCollectionModelo = em.merge(modeloCollectionModelo);
                if (oldFkMarcaIdOfModeloCollectionModelo != null) {
                    oldFkMarcaIdOfModeloCollectionModelo.getModeloCollection().remove(modeloCollectionModelo);
                    oldFkMarcaIdOfModeloCollectionModelo = em.merge(oldFkMarcaIdOfModeloCollectionModelo);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findMarca(marca.getId()) != null) {
                throw new PreexistingEntityException("Marca " + marca + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Marca marca) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Marca persistentMarca = em.find(Marca.class, marca.getId());
            Collection<Modelo> modeloCollectionOld = persistentMarca.getModeloCollection();
            Collection<Modelo> modeloCollectionNew = marca.getModeloCollection();
            Collection<Modelo> attachedModeloCollectionNew = new ArrayList<Modelo>();
            for (Modelo modeloCollectionNewModeloToAttach : modeloCollectionNew) {
                modeloCollectionNewModeloToAttach = em.getReference(modeloCollectionNewModeloToAttach.getClass(), modeloCollectionNewModeloToAttach.getId());
                attachedModeloCollectionNew.add(modeloCollectionNewModeloToAttach);
            }
            modeloCollectionNew = attachedModeloCollectionNew;
            marca.setModeloCollection(modeloCollectionNew);
            marca = em.merge(marca);
            for (Modelo modeloCollectionOldModelo : modeloCollectionOld) {
                if (!modeloCollectionNew.contains(modeloCollectionOldModelo)) {
                    modeloCollectionOldModelo.setFkMarcaId(null);
                    modeloCollectionOldModelo = em.merge(modeloCollectionOldModelo);
                }
            }
            for (Modelo modeloCollectionNewModelo : modeloCollectionNew) {
                if (!modeloCollectionOld.contains(modeloCollectionNewModelo)) {
                    Marca oldFkMarcaIdOfModeloCollectionNewModelo = modeloCollectionNewModelo.getFkMarcaId();
                    modeloCollectionNewModelo.setFkMarcaId(marca);
                    modeloCollectionNewModelo = em.merge(modeloCollectionNewModelo);
                    if (oldFkMarcaIdOfModeloCollectionNewModelo != null && !oldFkMarcaIdOfModeloCollectionNewModelo.equals(marca)) {
                        oldFkMarcaIdOfModeloCollectionNewModelo.getModeloCollection().remove(modeloCollectionNewModelo);
                        oldFkMarcaIdOfModeloCollectionNewModelo = em.merge(oldFkMarcaIdOfModeloCollectionNewModelo);
                    }
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = marca.getId();
                if (findMarca(id) == null) {
                    throw new NonexistentEntityException("The marca with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Marca marca;
            try {
                marca = em.getReference(Marca.class, id);
                marca.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The marca with id " + id + " no longer exists.", enfe);
            }
            Collection<Modelo> modeloCollection = marca.getModeloCollection();
            for (Modelo modeloCollectionModelo : modeloCollection) {
                modeloCollectionModelo.setFkMarcaId(null);
                modeloCollectionModelo = em.merge(modeloCollectionModelo);
            }
            em.remove(marca);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Marca> findMarcaEntities() {
        return findMarcaEntities(true, -1, -1);
    }

    public List<Marca> findMarcaEntities(int maxResults, int firstResult) {
        return findMarcaEntities(false, maxResults, firstResult);
    }

    private List<Marca> findMarcaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Marca.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Marca findMarca(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Marca.class, id);
        } finally {
            em.close();
        }
    }

    public int getMarcaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Marca> rt = cq.from(Marca.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
