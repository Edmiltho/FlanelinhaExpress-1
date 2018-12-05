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
import modelo.locais.Estado;
import modelo.locais.Bairro;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import modelo.locais.Cidade;
import padroes.DAO.exceptions.NonexistentEntityException;
import padroes.DAO.exceptions.PreexistingEntityException;
import padroes.DAO.exceptions.RollbackFailureException;

/**
 *
 * @author Cacherow
 */
public class CidadeJpaController implements Serializable {

    public CidadeJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Cidade cidade) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (cidade.getBairroCollection() == null) {
            cidade.setBairroCollection(new ArrayList<Bairro>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Estado fkEstadoId = cidade.getFkEstadoId();
            if (fkEstadoId != null) {
                fkEstadoId = em.getReference(fkEstadoId.getClass(), fkEstadoId.getId());
                cidade.setFkEstadoId(fkEstadoId);
            }
            Collection<Bairro> attachedBairroCollection = new ArrayList<Bairro>();
            for (Bairro bairroCollectionBairroToAttach : cidade.getBairroCollection()) {
                bairroCollectionBairroToAttach = em.getReference(bairroCollectionBairroToAttach.getClass(), bairroCollectionBairroToAttach.getId());
                attachedBairroCollection.add(bairroCollectionBairroToAttach);
            }
            cidade.setBairroCollection(attachedBairroCollection);
            em.persist(cidade);
            if (fkEstadoId != null) {
                fkEstadoId.getCidadeCollection().add(cidade);
                fkEstadoId = em.merge(fkEstadoId);
            }
            for (Bairro bairroCollectionBairro : cidade.getBairroCollection()) {
                Cidade oldFkCidadeIdOfBairroCollectionBairro = bairroCollectionBairro.getFkCidadeId();
                bairroCollectionBairro.setFkCidadeId(cidade);
                bairroCollectionBairro = em.merge(bairroCollectionBairro);
                if (oldFkCidadeIdOfBairroCollectionBairro != null) {
                    oldFkCidadeIdOfBairroCollectionBairro.getBairroCollection().remove(bairroCollectionBairro);
                    oldFkCidadeIdOfBairroCollectionBairro = em.merge(oldFkCidadeIdOfBairroCollectionBairro);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findCidade(cidade.getId()) != null) {
                throw new PreexistingEntityException("Cidade " + cidade + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Cidade cidade) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Cidade persistentCidade = em.find(Cidade.class, cidade.getId());
            Estado fkEstadoIdOld = persistentCidade.getFkEstadoId();
            Estado fkEstadoIdNew = cidade.getFkEstadoId();
            Collection<Bairro> bairroCollectionOld = persistentCidade.getBairroCollection();
            Collection<Bairro> bairroCollectionNew = cidade.getBairroCollection();
            if (fkEstadoIdNew != null) {
                fkEstadoIdNew = em.getReference(fkEstadoIdNew.getClass(), fkEstadoIdNew.getId());
                cidade.setFkEstadoId(fkEstadoIdNew);
            }
            Collection<Bairro> attachedBairroCollectionNew = new ArrayList<Bairro>();
            for (Bairro bairroCollectionNewBairroToAttach : bairroCollectionNew) {
                bairroCollectionNewBairroToAttach = em.getReference(bairroCollectionNewBairroToAttach.getClass(), bairroCollectionNewBairroToAttach.getId());
                attachedBairroCollectionNew.add(bairroCollectionNewBairroToAttach);
            }
            bairroCollectionNew = attachedBairroCollectionNew;
            cidade.setBairroCollection(bairroCollectionNew);
            cidade = em.merge(cidade);
            if (fkEstadoIdOld != null && !fkEstadoIdOld.equals(fkEstadoIdNew)) {
                fkEstadoIdOld.getCidadeCollection().remove(cidade);
                fkEstadoIdOld = em.merge(fkEstadoIdOld);
            }
            if (fkEstadoIdNew != null && !fkEstadoIdNew.equals(fkEstadoIdOld)) {
                fkEstadoIdNew.getCidadeCollection().add(cidade);
                fkEstadoIdNew = em.merge(fkEstadoIdNew);
            }
            for (Bairro bairroCollectionOldBairro : bairroCollectionOld) {
                if (!bairroCollectionNew.contains(bairroCollectionOldBairro)) {
                    bairroCollectionOldBairro.setFkCidadeId(null);
                    bairroCollectionOldBairro = em.merge(bairroCollectionOldBairro);
                }
            }
            for (Bairro bairroCollectionNewBairro : bairroCollectionNew) {
                if (!bairroCollectionOld.contains(bairroCollectionNewBairro)) {
                    Cidade oldFkCidadeIdOfBairroCollectionNewBairro = bairroCollectionNewBairro.getFkCidadeId();
                    bairroCollectionNewBairro.setFkCidadeId(cidade);
                    bairroCollectionNewBairro = em.merge(bairroCollectionNewBairro);
                    if (oldFkCidadeIdOfBairroCollectionNewBairro != null && !oldFkCidadeIdOfBairroCollectionNewBairro.equals(cidade)) {
                        oldFkCidadeIdOfBairroCollectionNewBairro.getBairroCollection().remove(bairroCollectionNewBairro);
                        oldFkCidadeIdOfBairroCollectionNewBairro = em.merge(oldFkCidadeIdOfBairroCollectionNewBairro);
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
                Integer id = cidade.getId();
                if (findCidade(id) == null) {
                    throw new NonexistentEntityException("The cidade with id " + id + " no longer exists.");
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
            Cidade cidade;
            try {
                cidade = em.getReference(Cidade.class, id);
                cidade.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The cidade with id " + id + " no longer exists.", enfe);
            }
            Estado fkEstadoId = cidade.getFkEstadoId();
            if (fkEstadoId != null) {
                fkEstadoId.getCidadeCollection().remove(cidade);
                fkEstadoId = em.merge(fkEstadoId);
            }
            Collection<Bairro> bairroCollection = cidade.getBairroCollection();
            for (Bairro bairroCollectionBairro : bairroCollection) {
                bairroCollectionBairro.setFkCidadeId(null);
                bairroCollectionBairro = em.merge(bairroCollectionBairro);
            }
            em.remove(cidade);
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

    public List<Cidade> findCidadeEntities() {
        return findCidadeEntities(true, -1, -1);
    }

    public List<Cidade> findCidadeEntities(int maxResults, int firstResult) {
        return findCidadeEntities(false, maxResults, firstResult);
    }

    private List<Cidade> findCidadeEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Cidade.class));
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

    public Cidade findCidade(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Cidade.class, id);
        } finally {
            em.close();
        }
    }

    public int getCidadeCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Cidade> rt = cq.from(Cidade.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
