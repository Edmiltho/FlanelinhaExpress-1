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
import modelo.locais.Endereco;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import modelo.locais.Bairro;
import padroes.DAO.exceptions.NonexistentEntityException;
import padroes.DAO.exceptions.PreexistingEntityException;
import padroes.DAO.exceptions.RollbackFailureException;

/**
 *
 * @author Cacherow
 */
public class BairroJpaController implements Serializable {

    public BairroJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Bairro bairro) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (bairro.getEnderecoCollection() == null) {
            bairro.setEnderecoCollection(new ArrayList<Endereco>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Cidade fkCidadeId = bairro.getFkCidadeId();
            if (fkCidadeId != null) {
                fkCidadeId = em.getReference(fkCidadeId.getClass(), fkCidadeId.getId());
                bairro.setFkCidadeId(fkCidadeId);
            }
            Collection<Endereco> attachedEnderecoCollection = new ArrayList<Endereco>();
            for (Endereco enderecoCollectionEnderecoToAttach : bairro.getEnderecoCollection()) {
                enderecoCollectionEnderecoToAttach = em.getReference(enderecoCollectionEnderecoToAttach.getClass(), enderecoCollectionEnderecoToAttach.getId());
                attachedEnderecoCollection.add(enderecoCollectionEnderecoToAttach);
            }
            bairro.setEnderecoCollection(attachedEnderecoCollection);
            em.persist(bairro);
            if (fkCidadeId != null) {
                fkCidadeId.getBairroCollection().add(bairro);
                fkCidadeId = em.merge(fkCidadeId);
            }
            for (Endereco enderecoCollectionEndereco : bairro.getEnderecoCollection()) {
                Bairro oldFkBairroIdOfEnderecoCollectionEndereco = enderecoCollectionEndereco.getFkBairroId();
                enderecoCollectionEndereco.setFkBairroId(bairro);
                enderecoCollectionEndereco = em.merge(enderecoCollectionEndereco);
                if (oldFkBairroIdOfEnderecoCollectionEndereco != null) {
                    oldFkBairroIdOfEnderecoCollectionEndereco.getEnderecoCollection().remove(enderecoCollectionEndereco);
                    oldFkBairroIdOfEnderecoCollectionEndereco = em.merge(oldFkBairroIdOfEnderecoCollectionEndereco);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findBairro(bairro.getId()) != null) {
                throw new PreexistingEntityException("Bairro " + bairro + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Bairro bairro) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Bairro persistentBairro = em.find(Bairro.class, bairro.getId());
            Cidade fkCidadeIdOld = persistentBairro.getFkCidadeId();
            Cidade fkCidadeIdNew = bairro.getFkCidadeId();
            Collection<Endereco> enderecoCollectionOld = persistentBairro.getEnderecoCollection();
            Collection<Endereco> enderecoCollectionNew = bairro.getEnderecoCollection();
            if (fkCidadeIdNew != null) {
                fkCidadeIdNew = em.getReference(fkCidadeIdNew.getClass(), fkCidadeIdNew.getId());
                bairro.setFkCidadeId(fkCidadeIdNew);
            }
            Collection<Endereco> attachedEnderecoCollectionNew = new ArrayList<Endereco>();
            for (Endereco enderecoCollectionNewEnderecoToAttach : enderecoCollectionNew) {
                enderecoCollectionNewEnderecoToAttach = em.getReference(enderecoCollectionNewEnderecoToAttach.getClass(), enderecoCollectionNewEnderecoToAttach.getId());
                attachedEnderecoCollectionNew.add(enderecoCollectionNewEnderecoToAttach);
            }
            enderecoCollectionNew = attachedEnderecoCollectionNew;
            bairro.setEnderecoCollection(enderecoCollectionNew);
            bairro = em.merge(bairro);
            if (fkCidadeIdOld != null && !fkCidadeIdOld.equals(fkCidadeIdNew)) {
                fkCidadeIdOld.getBairroCollection().remove(bairro);
                fkCidadeIdOld = em.merge(fkCidadeIdOld);
            }
            if (fkCidadeIdNew != null && !fkCidadeIdNew.equals(fkCidadeIdOld)) {
                fkCidadeIdNew.getBairroCollection().add(bairro);
                fkCidadeIdNew = em.merge(fkCidadeIdNew);
            }
            for (Endereco enderecoCollectionOldEndereco : enderecoCollectionOld) {
                if (!enderecoCollectionNew.contains(enderecoCollectionOldEndereco)) {
                    enderecoCollectionOldEndereco.setFkBairroId(null);
                    enderecoCollectionOldEndereco = em.merge(enderecoCollectionOldEndereco);
                }
            }
            for (Endereco enderecoCollectionNewEndereco : enderecoCollectionNew) {
                if (!enderecoCollectionOld.contains(enderecoCollectionNewEndereco)) {
                    Bairro oldFkBairroIdOfEnderecoCollectionNewEndereco = enderecoCollectionNewEndereco.getFkBairroId();
                    enderecoCollectionNewEndereco.setFkBairroId(bairro);
                    enderecoCollectionNewEndereco = em.merge(enderecoCollectionNewEndereco);
                    if (oldFkBairroIdOfEnderecoCollectionNewEndereco != null && !oldFkBairroIdOfEnderecoCollectionNewEndereco.equals(bairro)) {
                        oldFkBairroIdOfEnderecoCollectionNewEndereco.getEnderecoCollection().remove(enderecoCollectionNewEndereco);
                        oldFkBairroIdOfEnderecoCollectionNewEndereco = em.merge(oldFkBairroIdOfEnderecoCollectionNewEndereco);
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
                Integer id = bairro.getId();
                if (findBairro(id) == null) {
                    throw new NonexistentEntityException("The bairro with id " + id + " no longer exists.");
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
            Bairro bairro;
            try {
                bairro = em.getReference(Bairro.class, id);
                bairro.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The bairro with id " + id + " no longer exists.", enfe);
            }
            Cidade fkCidadeId = bairro.getFkCidadeId();
            if (fkCidadeId != null) {
                fkCidadeId.getBairroCollection().remove(bairro);
                fkCidadeId = em.merge(fkCidadeId);
            }
            Collection<Endereco> enderecoCollection = bairro.getEnderecoCollection();
            for (Endereco enderecoCollectionEndereco : enderecoCollection) {
                enderecoCollectionEndereco.setFkBairroId(null);
                enderecoCollectionEndereco = em.merge(enderecoCollectionEndereco);
            }
            em.remove(bairro);
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

    public List<Bairro> findBairroEntities() {
        return findBairroEntities(true, -1, -1);
    }

    public List<Bairro> findBairroEntities(int maxResults, int firstResult) {
        return findBairroEntities(false, maxResults, firstResult);
    }

    private List<Bairro> findBairroEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Bairro.class));
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

    public Bairro findBairro(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Bairro.class, id);
        } finally {
            em.close();
        }
    }

    public int getBairroCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Bairro> rt = cq.from(Bairro.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
