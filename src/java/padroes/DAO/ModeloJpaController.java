/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package padroes.DAO;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import ClassesEntidade.Marca;
import ClassesEntidade.Modelo;
import modelo.componentes.Veiculo;
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
public class ModeloJpaController implements Serializable {

    public ModeloJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Modelo modelo) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (modelo.getVeiculoCollection() == null) {
            modelo.setVeiculoCollection(new ArrayList<Veiculo>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Marca fkMarcaId = modelo.getFkMarcaId();
            if (fkMarcaId != null) {
                fkMarcaId = em.getReference(fkMarcaId.getClass(), fkMarcaId.getId());
                modelo.setFkMarcaId(fkMarcaId);
            }
            Collection<Veiculo> attachedVeiculoCollection = new ArrayList<Veiculo>();
            for (Veiculo veiculoCollectionVeiculoToAttach : modelo.getVeiculoCollection()) {
                veiculoCollectionVeiculoToAttach = em.getReference(veiculoCollectionVeiculoToAttach.getClass(), veiculoCollectionVeiculoToAttach.getId());
                attachedVeiculoCollection.add(veiculoCollectionVeiculoToAttach);
            }
            modelo.setVeiculoCollection(attachedVeiculoCollection);
            em.persist(modelo);
            if (fkMarcaId != null) {
                fkMarcaId.getModeloCollection().add(modelo);
                fkMarcaId = em.merge(fkMarcaId);
            }
            for (Veiculo veiculoCollectionVeiculo : modelo.getVeiculoCollection()) {
                Modelo oldFkModeloIdOfVeiculoCollectionVeiculo = veiculoCollectionVeiculo.getFkModeloId();
                veiculoCollectionVeiculo.setFkModeloId(modelo);
                veiculoCollectionVeiculo = em.merge(veiculoCollectionVeiculo);
                if (oldFkModeloIdOfVeiculoCollectionVeiculo != null) {
                    oldFkModeloIdOfVeiculoCollectionVeiculo.getVeiculoCollection().remove(veiculoCollectionVeiculo);
                    oldFkModeloIdOfVeiculoCollectionVeiculo = em.merge(oldFkModeloIdOfVeiculoCollectionVeiculo);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findModelo(modelo.getId()) != null) {
                throw new PreexistingEntityException("Modelo " + modelo + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Modelo modelo) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Modelo persistentModelo = em.find(Modelo.class, modelo.getId());
            Marca fkMarcaIdOld = persistentModelo.getFkMarcaId();
            Marca fkMarcaIdNew = modelo.getFkMarcaId();
            Collection<Veiculo> veiculoCollectionOld = persistentModelo.getVeiculoCollection();
            Collection<Veiculo> veiculoCollectionNew = modelo.getVeiculoCollection();
            if (fkMarcaIdNew != null) {
                fkMarcaIdNew = em.getReference(fkMarcaIdNew.getClass(), fkMarcaIdNew.getId());
                modelo.setFkMarcaId(fkMarcaIdNew);
            }
            Collection<Veiculo> attachedVeiculoCollectionNew = new ArrayList<Veiculo>();
            for (Veiculo veiculoCollectionNewVeiculoToAttach : veiculoCollectionNew) {
                veiculoCollectionNewVeiculoToAttach = em.getReference(veiculoCollectionNewVeiculoToAttach.getClass(), veiculoCollectionNewVeiculoToAttach.getId());
                attachedVeiculoCollectionNew.add(veiculoCollectionNewVeiculoToAttach);
            }
            veiculoCollectionNew = attachedVeiculoCollectionNew;
            modelo.setVeiculoCollection(veiculoCollectionNew);
            modelo = em.merge(modelo);
            if (fkMarcaIdOld != null && !fkMarcaIdOld.equals(fkMarcaIdNew)) {
                fkMarcaIdOld.getModeloCollection().remove(modelo);
                fkMarcaIdOld = em.merge(fkMarcaIdOld);
            }
            if (fkMarcaIdNew != null && !fkMarcaIdNew.equals(fkMarcaIdOld)) {
                fkMarcaIdNew.getModeloCollection().add(modelo);
                fkMarcaIdNew = em.merge(fkMarcaIdNew);
            }
            for (Veiculo veiculoCollectionOldVeiculo : veiculoCollectionOld) {
                if (!veiculoCollectionNew.contains(veiculoCollectionOldVeiculo)) {
                    veiculoCollectionOldVeiculo.setFkModeloId(null);
                    veiculoCollectionOldVeiculo = em.merge(veiculoCollectionOldVeiculo);
                }
            }
            for (Veiculo veiculoCollectionNewVeiculo : veiculoCollectionNew) {
                if (!veiculoCollectionOld.contains(veiculoCollectionNewVeiculo)) {
                    Modelo oldFkModeloIdOfVeiculoCollectionNewVeiculo = veiculoCollectionNewVeiculo.getFkModeloId();
                    veiculoCollectionNewVeiculo.setFkModeloId(modelo);
                    veiculoCollectionNewVeiculo = em.merge(veiculoCollectionNewVeiculo);
                    if (oldFkModeloIdOfVeiculoCollectionNewVeiculo != null && !oldFkModeloIdOfVeiculoCollectionNewVeiculo.equals(modelo)) {
                        oldFkModeloIdOfVeiculoCollectionNewVeiculo.getVeiculoCollection().remove(veiculoCollectionNewVeiculo);
                        oldFkModeloIdOfVeiculoCollectionNewVeiculo = em.merge(oldFkModeloIdOfVeiculoCollectionNewVeiculo);
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
                Integer id = modelo.getId();
                if (findModelo(id) == null) {
                    throw new NonexistentEntityException("The modelo with id " + id + " no longer exists.");
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
            Modelo modelo;
            try {
                modelo = em.getReference(Modelo.class, id);
                modelo.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The modelo with id " + id + " no longer exists.", enfe);
            }
            Marca fkMarcaId = modelo.getFkMarcaId();
            if (fkMarcaId != null) {
                fkMarcaId.getModeloCollection().remove(modelo);
                fkMarcaId = em.merge(fkMarcaId);
            }
            Collection<Veiculo> veiculoCollection = modelo.getVeiculoCollection();
            for (Veiculo veiculoCollectionVeiculo : veiculoCollection) {
                veiculoCollectionVeiculo.setFkModeloId(null);
                veiculoCollectionVeiculo = em.merge(veiculoCollectionVeiculo);
            }
            em.remove(modelo);
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

    public List<Modelo> findModeloEntities() {
        return findModeloEntities(true, -1, -1);
    }

    public List<Modelo> findModeloEntities(int maxResults, int firstResult) {
        return findModeloEntities(false, maxResults, firstResult);
    }

    private List<Modelo> findModeloEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Modelo.class));
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

    public Modelo findModelo(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Modelo.class, id);
        } finally {
            em.close();
        }
    }

    public int getModeloCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Modelo> rt = cq.from(Modelo.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
