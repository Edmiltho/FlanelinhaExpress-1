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
import modelo.operadores.Pessoa;
import modelo.componentes.Estacionamento;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import modelo.operadores.PessoaJuridica;
import padroes.DAO.exceptions.NonexistentEntityException;
import padroes.DAO.exceptions.PreexistingEntityException;
import padroes.DAO.exceptions.RollbackFailureException;

/**
 *
 * @author Cacherow
 */
public class PessoaJuridicaJpaController implements Serializable {

    public PessoaJuridicaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(PessoaJuridica pessoaJuridica) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (pessoaJuridica.getEstacionamentoCollection() == null) {
            pessoaJuridica.setEstacionamentoCollection(new ArrayList<Estacionamento>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Pessoa fkPessoaId = pessoaJuridica.getFkPessoaId();
            if (fkPessoaId != null) {
                fkPessoaId = em.getReference(fkPessoaId.getClass(), fkPessoaId.getId());
                pessoaJuridica.setFkPessoaId(fkPessoaId);
            }
            Collection<Estacionamento> attachedEstacionamentoCollection = new ArrayList<Estacionamento>();
            for (Estacionamento estacionamentoCollectionEstacionamentoToAttach : pessoaJuridica.getEstacionamentoCollection()) {
                estacionamentoCollectionEstacionamentoToAttach = em.getReference(estacionamentoCollectionEstacionamentoToAttach.getClass(), estacionamentoCollectionEstacionamentoToAttach.getId());
                attachedEstacionamentoCollection.add(estacionamentoCollectionEstacionamentoToAttach);
            }
            pessoaJuridica.setEstacionamentoCollection(attachedEstacionamentoCollection);
            em.persist(pessoaJuridica);
            if (fkPessoaId != null) {
                fkPessoaId.getPessoaJuridicaCollection().add(pessoaJuridica);
                fkPessoaId = em.merge(fkPessoaId);
            }
            for (Estacionamento estacionamentoCollectionEstacionamento : pessoaJuridica.getEstacionamentoCollection()) {
                PessoaJuridica oldFkPessoaJuridicaIdOfEstacionamentoCollectionEstacionamento = estacionamentoCollectionEstacionamento.getFkPessoaJuridicaId();
                estacionamentoCollectionEstacionamento.setFkPessoaJuridicaId(pessoaJuridica);
                estacionamentoCollectionEstacionamento = em.merge(estacionamentoCollectionEstacionamento);
                if (oldFkPessoaJuridicaIdOfEstacionamentoCollectionEstacionamento != null) {
                    oldFkPessoaJuridicaIdOfEstacionamentoCollectionEstacionamento.getEstacionamentoCollection().remove(estacionamentoCollectionEstacionamento);
                    oldFkPessoaJuridicaIdOfEstacionamentoCollectionEstacionamento = em.merge(oldFkPessoaJuridicaIdOfEstacionamentoCollectionEstacionamento);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findPessoaJuridica(pessoaJuridica.getId()) != null) {
                throw new PreexistingEntityException("PessoaJuridica " + pessoaJuridica + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(PessoaJuridica pessoaJuridica) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            PessoaJuridica persistentPessoaJuridica = em.find(PessoaJuridica.class, pessoaJuridica.getId());
            Pessoa fkPessoaIdOld = persistentPessoaJuridica.getFkPessoaId();
            Pessoa fkPessoaIdNew = pessoaJuridica.getFkPessoaId();
            Collection<Estacionamento> estacionamentoCollectionOld = persistentPessoaJuridica.getEstacionamentoCollection();
            Collection<Estacionamento> estacionamentoCollectionNew = pessoaJuridica.getEstacionamentoCollection();
            if (fkPessoaIdNew != null) {
                fkPessoaIdNew = em.getReference(fkPessoaIdNew.getClass(), fkPessoaIdNew.getId());
                pessoaJuridica.setFkPessoaId(fkPessoaIdNew);
            }
            Collection<Estacionamento> attachedEstacionamentoCollectionNew = new ArrayList<Estacionamento>();
            for (Estacionamento estacionamentoCollectionNewEstacionamentoToAttach : estacionamentoCollectionNew) {
                estacionamentoCollectionNewEstacionamentoToAttach = em.getReference(estacionamentoCollectionNewEstacionamentoToAttach.getClass(), estacionamentoCollectionNewEstacionamentoToAttach.getId());
                attachedEstacionamentoCollectionNew.add(estacionamentoCollectionNewEstacionamentoToAttach);
            }
            estacionamentoCollectionNew = attachedEstacionamentoCollectionNew;
            pessoaJuridica.setEstacionamentoCollection(estacionamentoCollectionNew);
            pessoaJuridica = em.merge(pessoaJuridica);
            if (fkPessoaIdOld != null && !fkPessoaIdOld.equals(fkPessoaIdNew)) {
                fkPessoaIdOld.getPessoaJuridicaCollection().remove(pessoaJuridica);
                fkPessoaIdOld = em.merge(fkPessoaIdOld);
            }
            if (fkPessoaIdNew != null && !fkPessoaIdNew.equals(fkPessoaIdOld)) {
                fkPessoaIdNew.getPessoaJuridicaCollection().add(pessoaJuridica);
                fkPessoaIdNew = em.merge(fkPessoaIdNew);
            }
            for (Estacionamento estacionamentoCollectionOldEstacionamento : estacionamentoCollectionOld) {
                if (!estacionamentoCollectionNew.contains(estacionamentoCollectionOldEstacionamento)) {
                    estacionamentoCollectionOldEstacionamento.setFkPessoaJuridicaId(null);
                    estacionamentoCollectionOldEstacionamento = em.merge(estacionamentoCollectionOldEstacionamento);
                }
            }
            for (Estacionamento estacionamentoCollectionNewEstacionamento : estacionamentoCollectionNew) {
                if (!estacionamentoCollectionOld.contains(estacionamentoCollectionNewEstacionamento)) {
                    PessoaJuridica oldFkPessoaJuridicaIdOfEstacionamentoCollectionNewEstacionamento = estacionamentoCollectionNewEstacionamento.getFkPessoaJuridicaId();
                    estacionamentoCollectionNewEstacionamento.setFkPessoaJuridicaId(pessoaJuridica);
                    estacionamentoCollectionNewEstacionamento = em.merge(estacionamentoCollectionNewEstacionamento);
                    if (oldFkPessoaJuridicaIdOfEstacionamentoCollectionNewEstacionamento != null && !oldFkPessoaJuridicaIdOfEstacionamentoCollectionNewEstacionamento.equals(pessoaJuridica)) {
                        oldFkPessoaJuridicaIdOfEstacionamentoCollectionNewEstacionamento.getEstacionamentoCollection().remove(estacionamentoCollectionNewEstacionamento);
                        oldFkPessoaJuridicaIdOfEstacionamentoCollectionNewEstacionamento = em.merge(oldFkPessoaJuridicaIdOfEstacionamentoCollectionNewEstacionamento);
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
                Integer id = pessoaJuridica.getId();
                if (findPessoaJuridica(id) == null) {
                    throw new NonexistentEntityException("The pessoaJuridica with id " + id + " no longer exists.");
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
            PessoaJuridica pessoaJuridica;
            try {
                pessoaJuridica = em.getReference(PessoaJuridica.class, id);
                pessoaJuridica.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The pessoaJuridica with id " + id + " no longer exists.", enfe);
            }
            Pessoa fkPessoaId = pessoaJuridica.getFkPessoaId();
            if (fkPessoaId != null) {
                fkPessoaId.getPessoaJuridicaCollection().remove(pessoaJuridica);
                fkPessoaId = em.merge(fkPessoaId);
            }
            Collection<Estacionamento> estacionamentoCollection = pessoaJuridica.getEstacionamentoCollection();
            for (Estacionamento estacionamentoCollectionEstacionamento : estacionamentoCollection) {
                estacionamentoCollectionEstacionamento.setFkPessoaJuridicaId(null);
                estacionamentoCollectionEstacionamento = em.merge(estacionamentoCollectionEstacionamento);
            }
            em.remove(pessoaJuridica);
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

    public List<PessoaJuridica> findPessoaJuridicaEntities() {
        return findPessoaJuridicaEntities(true, -1, -1);
    }

    public List<PessoaJuridica> findPessoaJuridicaEntities(int maxResults, int firstResult) {
        return findPessoaJuridicaEntities(false, maxResults, firstResult);
    }

    private List<PessoaJuridica> findPessoaJuridicaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(PessoaJuridica.class));
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

    public PessoaJuridica findPessoaJuridica(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(PessoaJuridica.class, id);
        } finally {
            em.close();
        }
    }

    public int getPessoaJuridicaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<PessoaJuridica> rt = cq.from(PessoaJuridica.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
