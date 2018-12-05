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
import modelo.locais.Endereco;
import modelo.operadores.PessoaJuridica;
import modelo.componentes.Vaga;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import modelo.componentes.Estacionamento;
import padroes.DAO.exceptions.NonexistentEntityException;
import padroes.DAO.exceptions.PreexistingEntityException;
import padroes.DAO.exceptions.RollbackFailureException;

/**
 *
 * @author Cacherow
 */
public class EstacionamentoJpaController implements Serializable {

    public EstacionamentoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Estacionamento estacionamento) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (estacionamento.getVagaCollection() == null) {
            estacionamento.setVagaCollection(new ArrayList<Vaga>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Endereco fkEnderecoId = estacionamento.getFkEnderecoId();
            if (fkEnderecoId != null) {
                fkEnderecoId = em.getReference(fkEnderecoId.getClass(), fkEnderecoId.getId());
                estacionamento.setFkEnderecoId(fkEnderecoId);
            }
            PessoaJuridica fkPessoaJuridicaId = estacionamento.getFkPessoaJuridicaId();
            if (fkPessoaJuridicaId != null) {
                fkPessoaJuridicaId = em.getReference(fkPessoaJuridicaId.getClass(), fkPessoaJuridicaId.getId());
                estacionamento.setFkPessoaJuridicaId(fkPessoaJuridicaId);
            }
            Collection<Vaga> attachedVagaCollection = new ArrayList<Vaga>();
            for (Vaga vagaCollectionVagaToAttach : estacionamento.getVagaCollection()) {
                vagaCollectionVagaToAttach = em.getReference(vagaCollectionVagaToAttach.getClass(), vagaCollectionVagaToAttach.getId());
                attachedVagaCollection.add(vagaCollectionVagaToAttach);
            }
            estacionamento.setVagaCollection(attachedVagaCollection);
            em.persist(estacionamento);
            if (fkEnderecoId != null) {
                fkEnderecoId.getEstacionamentoCollection().add(estacionamento);
                fkEnderecoId = em.merge(fkEnderecoId);
            }
            if (fkPessoaJuridicaId != null) {
                fkPessoaJuridicaId.getEstacionamentoCollection().add(estacionamento);
                fkPessoaJuridicaId = em.merge(fkPessoaJuridicaId);
            }
            for (Vaga vagaCollectionVaga : estacionamento.getVagaCollection()) {
                Estacionamento oldFkEstacionamentoIdOfVagaCollectionVaga = vagaCollectionVaga.getFkEstacionamentoId();
                vagaCollectionVaga.setFkEstacionamentoId(estacionamento);
                vagaCollectionVaga = em.merge(vagaCollectionVaga);
                if (oldFkEstacionamentoIdOfVagaCollectionVaga != null) {
                    oldFkEstacionamentoIdOfVagaCollectionVaga.getVagaCollection().remove(vagaCollectionVaga);
                    oldFkEstacionamentoIdOfVagaCollectionVaga = em.merge(oldFkEstacionamentoIdOfVagaCollectionVaga);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findEstacionamento(estacionamento.getId()) != null) {
                throw new PreexistingEntityException("Estacionamento " + estacionamento + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Estacionamento estacionamento) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Estacionamento persistentEstacionamento = em.find(Estacionamento.class, estacionamento.getId());
            Endereco fkEnderecoIdOld = persistentEstacionamento.getFkEnderecoId();
            Endereco fkEnderecoIdNew = estacionamento.getFkEnderecoId();
            PessoaJuridica fkPessoaJuridicaIdOld = persistentEstacionamento.getFkPessoaJuridicaId();
            PessoaJuridica fkPessoaJuridicaIdNew = estacionamento.getFkPessoaJuridicaId();
            Collection<Vaga> vagaCollectionOld = persistentEstacionamento.getVagaCollection();
            Collection<Vaga> vagaCollectionNew = estacionamento.getVagaCollection();
            if (fkEnderecoIdNew != null) {
                fkEnderecoIdNew = em.getReference(fkEnderecoIdNew.getClass(), fkEnderecoIdNew.getId());
                estacionamento.setFkEnderecoId(fkEnderecoIdNew);
            }
            if (fkPessoaJuridicaIdNew != null) {
                fkPessoaJuridicaIdNew = em.getReference(fkPessoaJuridicaIdNew.getClass(), fkPessoaJuridicaIdNew.getId());
                estacionamento.setFkPessoaJuridicaId(fkPessoaJuridicaIdNew);
            }
            Collection<Vaga> attachedVagaCollectionNew = new ArrayList<Vaga>();
            for (Vaga vagaCollectionNewVagaToAttach : vagaCollectionNew) {
                vagaCollectionNewVagaToAttach = em.getReference(vagaCollectionNewVagaToAttach.getClass(), vagaCollectionNewVagaToAttach.getId());
                attachedVagaCollectionNew.add(vagaCollectionNewVagaToAttach);
            }
            vagaCollectionNew = attachedVagaCollectionNew;
            estacionamento.setVagaCollection(vagaCollectionNew);
            estacionamento = em.merge(estacionamento);
            if (fkEnderecoIdOld != null && !fkEnderecoIdOld.equals(fkEnderecoIdNew)) {
                fkEnderecoIdOld.getEstacionamentoCollection().remove(estacionamento);
                fkEnderecoIdOld = em.merge(fkEnderecoIdOld);
            }
            if (fkEnderecoIdNew != null && !fkEnderecoIdNew.equals(fkEnderecoIdOld)) {
                fkEnderecoIdNew.getEstacionamentoCollection().add(estacionamento);
                fkEnderecoIdNew = em.merge(fkEnderecoIdNew);
            }
            if (fkPessoaJuridicaIdOld != null && !fkPessoaJuridicaIdOld.equals(fkPessoaJuridicaIdNew)) {
                fkPessoaJuridicaIdOld.getEstacionamentoCollection().remove(estacionamento);
                fkPessoaJuridicaIdOld = em.merge(fkPessoaJuridicaIdOld);
            }
            if (fkPessoaJuridicaIdNew != null && !fkPessoaJuridicaIdNew.equals(fkPessoaJuridicaIdOld)) {
                fkPessoaJuridicaIdNew.getEstacionamentoCollection().add(estacionamento);
                fkPessoaJuridicaIdNew = em.merge(fkPessoaJuridicaIdNew);
            }
            for (Vaga vagaCollectionOldVaga : vagaCollectionOld) {
                if (!vagaCollectionNew.contains(vagaCollectionOldVaga)) {
                    vagaCollectionOldVaga.setFkEstacionamentoId(null);
                    vagaCollectionOldVaga = em.merge(vagaCollectionOldVaga);
                }
            }
            for (Vaga vagaCollectionNewVaga : vagaCollectionNew) {
                if (!vagaCollectionOld.contains(vagaCollectionNewVaga)) {
                    Estacionamento oldFkEstacionamentoIdOfVagaCollectionNewVaga = vagaCollectionNewVaga.getFkEstacionamentoId();
                    vagaCollectionNewVaga.setFkEstacionamentoId(estacionamento);
                    vagaCollectionNewVaga = em.merge(vagaCollectionNewVaga);
                    if (oldFkEstacionamentoIdOfVagaCollectionNewVaga != null && !oldFkEstacionamentoIdOfVagaCollectionNewVaga.equals(estacionamento)) {
                        oldFkEstacionamentoIdOfVagaCollectionNewVaga.getVagaCollection().remove(vagaCollectionNewVaga);
                        oldFkEstacionamentoIdOfVagaCollectionNewVaga = em.merge(oldFkEstacionamentoIdOfVagaCollectionNewVaga);
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
                Integer id = estacionamento.getId();
                if (findEstacionamento(id) == null) {
                    throw new NonexistentEntityException("The estacionamento with id " + id + " no longer exists.");
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
            Estacionamento estacionamento;
            try {
                estacionamento = em.getReference(Estacionamento.class, id);
                estacionamento.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The estacionamento with id " + id + " no longer exists.", enfe);
            }
            Endereco fkEnderecoId = estacionamento.getFkEnderecoId();
            if (fkEnderecoId != null) {
                fkEnderecoId.getEstacionamentoCollection().remove(estacionamento);
                fkEnderecoId = em.merge(fkEnderecoId);
            }
            PessoaJuridica fkPessoaJuridicaId = estacionamento.getFkPessoaJuridicaId();
            if (fkPessoaJuridicaId != null) {
                fkPessoaJuridicaId.getEstacionamentoCollection().remove(estacionamento);
                fkPessoaJuridicaId = em.merge(fkPessoaJuridicaId);
            }
            Collection<Vaga> vagaCollection = estacionamento.getVagaCollection();
            for (Vaga vagaCollectionVaga : vagaCollection) {
                vagaCollectionVaga.setFkEstacionamentoId(null);
                vagaCollectionVaga = em.merge(vagaCollectionVaga);
            }
            em.remove(estacionamento);
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

    public List<Estacionamento> findEstacionamentoEntities() {
        return findEstacionamentoEntities(true, -1, -1);
    }

    public List<Estacionamento> findEstacionamentoEntities(int maxResults, int firstResult) {
        return findEstacionamentoEntities(false, maxResults, firstResult);
    }

    private List<Estacionamento> findEstacionamentoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Estacionamento.class));
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

    public Estacionamento findEstacionamento(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Estacionamento.class, id);
        } finally {
            em.close();
        }
    }

    public int getEstacionamentoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Estacionamento> rt = cq.from(Estacionamento.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
