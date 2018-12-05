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
import modelo.operacoes.Cartao;
import modelo.operacoes.Reserva;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import modelo.operacoes.Pagamento;
import padroes.DAO.exceptions.NonexistentEntityException;
import padroes.DAO.exceptions.PreexistingEntityException;
import padroes.DAO.exceptions.RollbackFailureException;

/**
 *
 * @author Cacherow
 */
public class PagamentoJpaController implements Serializable {

    public PagamentoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Pagamento pagamento) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (pagamento.getReservaCollection() == null) {
            pagamento.setReservaCollection(new ArrayList<Reserva>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Cartao fkCartaoId = pagamento.getFkCartaoId();
            if (fkCartaoId != null) {
                fkCartaoId = em.getReference(fkCartaoId.getClass(), fkCartaoId.getId());
                pagamento.setFkCartaoId(fkCartaoId);
            }
            Collection<Reserva> attachedReservaCollection = new ArrayList<Reserva>();
            for (Reserva reservaCollectionReservaToAttach : pagamento.getReservaCollection()) {
                reservaCollectionReservaToAttach = em.getReference(reservaCollectionReservaToAttach.getClass(), reservaCollectionReservaToAttach.getId());
                attachedReservaCollection.add(reservaCollectionReservaToAttach);
            }
            pagamento.setReservaCollection(attachedReservaCollection);
            em.persist(pagamento);
            if (fkCartaoId != null) {
                fkCartaoId.getPagamentoEstacionamentoCollection().add(pagamento);
                fkCartaoId = em.merge(fkCartaoId);
            }
            for (Reserva reservaCollectionReserva : pagamento.getReservaCollection()) {
                Pagamento oldFkPagamentoEstacionamentoIdOfReservaCollectionReserva = reservaCollectionReserva.getFkPagamentoEstacionamentoId();
                reservaCollectionReserva.setFkPagamentoEstacionamentoId(pagamento);
                reservaCollectionReserva = em.merge(reservaCollectionReserva);
                if (oldFkPagamentoEstacionamentoIdOfReservaCollectionReserva != null) {
                    oldFkPagamentoEstacionamentoIdOfReservaCollectionReserva.getReservaCollection().remove(reservaCollectionReserva);
                    oldFkPagamentoEstacionamentoIdOfReservaCollectionReserva = em.merge(oldFkPagamentoEstacionamentoIdOfReservaCollectionReserva);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findPagamento(pagamento.getId()) != null) {
                throw new PreexistingEntityException("Pagamento " + pagamento + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Pagamento pagamento) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Pagamento persistentPagamento = em.find(Pagamento.class, pagamento.getId());
            Cartao fkCartaoIdOld = persistentPagamento.getFkCartaoId();
            Cartao fkCartaoIdNew = pagamento.getFkCartaoId();
            Collection<Reserva> reservaCollectionOld = persistentPagamento.getReservaCollection();
            Collection<Reserva> reservaCollectionNew = pagamento.getReservaCollection();
            if (fkCartaoIdNew != null) {
                fkCartaoIdNew = em.getReference(fkCartaoIdNew.getClass(), fkCartaoIdNew.getId());
                pagamento.setFkCartaoId(fkCartaoIdNew);
            }
            Collection<Reserva> attachedReservaCollectionNew = new ArrayList<Reserva>();
            for (Reserva reservaCollectionNewReservaToAttach : reservaCollectionNew) {
                reservaCollectionNewReservaToAttach = em.getReference(reservaCollectionNewReservaToAttach.getClass(), reservaCollectionNewReservaToAttach.getId());
                attachedReservaCollectionNew.add(reservaCollectionNewReservaToAttach);
            }
            reservaCollectionNew = attachedReservaCollectionNew;
            pagamento.setReservaCollection(reservaCollectionNew);
            pagamento = em.merge(pagamento);
            if (fkCartaoIdOld != null && !fkCartaoIdOld.equals(fkCartaoIdNew)) {
                fkCartaoIdOld.getPagamentoEstacionamentoCollection().remove(pagamento);
                fkCartaoIdOld = em.merge(fkCartaoIdOld);
            }
            if (fkCartaoIdNew != null && !fkCartaoIdNew.equals(fkCartaoIdOld)) {
                fkCartaoIdNew.getPagamentoEstacionamentoCollection().add(pagamento);
                fkCartaoIdNew = em.merge(fkCartaoIdNew);
            }
            for (Reserva reservaCollectionOldReserva : reservaCollectionOld) {
                if (!reservaCollectionNew.contains(reservaCollectionOldReserva)) {
                    reservaCollectionOldReserva.setFkPagamentoEstacionamentoId(null);
                    reservaCollectionOldReserva = em.merge(reservaCollectionOldReserva);
                }
            }
            for (Reserva reservaCollectionNewReserva : reservaCollectionNew) {
                if (!reservaCollectionOld.contains(reservaCollectionNewReserva)) {
                    Pagamento oldFkPagamentoEstacionamentoIdOfReservaCollectionNewReserva = reservaCollectionNewReserva.getFkPagamentoEstacionamentoId();
                    reservaCollectionNewReserva.setFkPagamentoEstacionamentoId(pagamento);
                    reservaCollectionNewReserva = em.merge(reservaCollectionNewReserva);
                    if (oldFkPagamentoEstacionamentoIdOfReservaCollectionNewReserva != null && !oldFkPagamentoEstacionamentoIdOfReservaCollectionNewReserva.equals(pagamento)) {
                        oldFkPagamentoEstacionamentoIdOfReservaCollectionNewReserva.getReservaCollection().remove(reservaCollectionNewReserva);
                        oldFkPagamentoEstacionamentoIdOfReservaCollectionNewReserva = em.merge(oldFkPagamentoEstacionamentoIdOfReservaCollectionNewReserva);
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
                Integer id = pagamento.getId();
                if (findPagamento(id) == null) {
                    throw new NonexistentEntityException("The pagamento with id " + id + " no longer exists.");
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
            Pagamento pagamento;
            try {
                pagamento = em.getReference(Pagamento.class, id);
                pagamento.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The pagamento with id " + id + " no longer exists.", enfe);
            }
            Cartao fkCartaoId = pagamento.getFkCartaoId();
            if (fkCartaoId != null) {
                fkCartaoId.getPagamentoEstacionamentoCollection().remove(pagamento);
                fkCartaoId = em.merge(fkCartaoId);
            }
            Collection<Reserva> reservaCollection = pagamento.getReservaCollection();
            for (Reserva reservaCollectionReserva : reservaCollection) {
                reservaCollectionReserva.setFkPagamentoEstacionamentoId(null);
                reservaCollectionReserva = em.merge(reservaCollectionReserva);
            }
            em.remove(pagamento);
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

    public List<Pagamento> findPagamentoEntities() {
        return findPagamentoEntities(true, -1, -1);
    }

    public List<Pagamento> findPagamentoEntities(int maxResults, int firstResult) {
        return findPagamentoEntities(false, maxResults, firstResult);
    }

    private List<Pagamento> findPagamentoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Pagamento.class));
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

    public Pagamento findPagamento(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Pagamento.class, id);
        } finally {
            em.close();
        }
    }

    public int getPagamentoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Pagamento> rt = cq.from(Pagamento.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
