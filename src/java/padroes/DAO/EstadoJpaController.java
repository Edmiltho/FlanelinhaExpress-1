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
import modelo.locais.Cidade;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import modelo.locais.Estado;
import padroes.DAO.exceptions.NonexistentEntityException;
import padroes.DAO.exceptions.PreexistingEntityException;
import padroes.DAO.exceptions.RollbackFailureException;

/**
 *
 * @author Cacherow
 */
public class EstadoJpaController implements Serializable {

    public EstadoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Estado estado) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (estado.getCidadeCollection() == null) {
            estado.setCidadeCollection(new ArrayList<Cidade>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<Cidade> attachedCidadeCollection = new ArrayList<Cidade>();
            for (Cidade cidadeCollectionCidadeToAttach : estado.getCidadeCollection()) {
                cidadeCollectionCidadeToAttach = em.getReference(cidadeCollectionCidadeToAttach.getClass(), cidadeCollectionCidadeToAttach.getId());
                attachedCidadeCollection.add(cidadeCollectionCidadeToAttach);
            }
            estado.setCidadeCollection(attachedCidadeCollection);
            em.persist(estado);
            for (Cidade cidadeCollectionCidade : estado.getCidadeCollection()) {
                Estado oldFkEstadoIdOfCidadeCollectionCidade = cidadeCollectionCidade.getFkEstadoId();
                cidadeCollectionCidade.setFkEstadoId(estado);
                cidadeCollectionCidade = em.merge(cidadeCollectionCidade);
                if (oldFkEstadoIdOfCidadeCollectionCidade != null) {
                    oldFkEstadoIdOfCidadeCollectionCidade.getCidadeCollection().remove(cidadeCollectionCidade);
                    oldFkEstadoIdOfCidadeCollectionCidade = em.merge(oldFkEstadoIdOfCidadeCollectionCidade);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findEstado(estado.getId()) != null) {
                throw new PreexistingEntityException("Estado " + estado + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Estado estado) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Estado persistentEstado = em.find(Estado.class, estado.getId());
            Collection<Cidade> cidadeCollectionOld = persistentEstado.getCidadeCollection();
            Collection<Cidade> cidadeCollectionNew = estado.getCidadeCollection();
            Collection<Cidade> attachedCidadeCollectionNew = new ArrayList<Cidade>();
            for (Cidade cidadeCollectionNewCidadeToAttach : cidadeCollectionNew) {
                cidadeCollectionNewCidadeToAttach = em.getReference(cidadeCollectionNewCidadeToAttach.getClass(), cidadeCollectionNewCidadeToAttach.getId());
                attachedCidadeCollectionNew.add(cidadeCollectionNewCidadeToAttach);
            }
            cidadeCollectionNew = attachedCidadeCollectionNew;
            estado.setCidadeCollection(cidadeCollectionNew);
            estado = em.merge(estado);
            for (Cidade cidadeCollectionOldCidade : cidadeCollectionOld) {
                if (!cidadeCollectionNew.contains(cidadeCollectionOldCidade)) {
                    cidadeCollectionOldCidade.setFkEstadoId(null);
                    cidadeCollectionOldCidade = em.merge(cidadeCollectionOldCidade);
                }
            }
            for (Cidade cidadeCollectionNewCidade : cidadeCollectionNew) {
                if (!cidadeCollectionOld.contains(cidadeCollectionNewCidade)) {
                    Estado oldFkEstadoIdOfCidadeCollectionNewCidade = cidadeCollectionNewCidade.getFkEstadoId();
                    cidadeCollectionNewCidade.setFkEstadoId(estado);
                    cidadeCollectionNewCidade = em.merge(cidadeCollectionNewCidade);
                    if (oldFkEstadoIdOfCidadeCollectionNewCidade != null && !oldFkEstadoIdOfCidadeCollectionNewCidade.equals(estado)) {
                        oldFkEstadoIdOfCidadeCollectionNewCidade.getCidadeCollection().remove(cidadeCollectionNewCidade);
                        oldFkEstadoIdOfCidadeCollectionNewCidade = em.merge(oldFkEstadoIdOfCidadeCollectionNewCidade);
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
                Integer id = estado.getId();
                if (findEstado(id) == null) {
                    throw new NonexistentEntityException("The estado with id " + id + " no longer exists.");
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
            Estado estado;
            try {
                estado = em.getReference(Estado.class, id);
                estado.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The estado with id " + id + " no longer exists.", enfe);
            }
            Collection<Cidade> cidadeCollection = estado.getCidadeCollection();
            for (Cidade cidadeCollectionCidade : cidadeCollection) {
                cidadeCollectionCidade.setFkEstadoId(null);
                cidadeCollectionCidade = em.merge(cidadeCollectionCidade);
            }
            em.remove(estado);
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

    public List<Estado> findEstadoEntities() {
        return findEstadoEntities(true, -1, -1);
    }

    public List<Estado> findEstadoEntities(int maxResults, int firstResult) {
        return findEstadoEntities(false, maxResults, firstResult);
    }

    private List<Estado> findEstadoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Estado.class));
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

    public Estado findEstado(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Estado.class, id);
        } finally {
            em.close();
        }
    }

    public int getEstadoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Estado> rt = cq.from(Estado.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
