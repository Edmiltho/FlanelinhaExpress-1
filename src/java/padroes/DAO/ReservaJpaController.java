/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package padroes.DAO;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;
import modelo.operacoes.Pagamento;
import modelo.componentes.Vaga;
import modelo.componentes.Veiculo;
import modelo.operacoes.Reserva;
import padroes.DAO.exceptions.NonexistentEntityException;
import padroes.DAO.exceptions.PreexistingEntityException;
import padroes.DAO.exceptions.RollbackFailureException;

/**
 *
 * @author Cacherow
 */
public class ReservaJpaController implements Serializable {

    public ReservaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Reserva reserva) throws PreexistingEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Pagamento fkPagamentoEstacionamentoId = reserva.getFkPagamentoEstacionamentoId();
            if (fkPagamentoEstacionamentoId != null) {
                fkPagamentoEstacionamentoId = em.getReference(fkPagamentoEstacionamentoId.getClass(), fkPagamentoEstacionamentoId.getId());
                reserva.setFkPagamentoEstacionamentoId(fkPagamentoEstacionamentoId);
            }
            Vaga fkVagaId = reserva.getFkVagaId();
            if (fkVagaId != null) {
                fkVagaId = em.getReference(fkVagaId.getClass(), fkVagaId.getId());
                reserva.setFkVagaId(fkVagaId);
            }
            Veiculo fkVeiculoId = reserva.getFkVeiculoId();
            if (fkVeiculoId != null) {
                fkVeiculoId = em.getReference(fkVeiculoId.getClass(), fkVeiculoId.getId());
                reserva.setFkVeiculoId(fkVeiculoId);
            }
            em.persist(reserva);
            if (fkPagamentoEstacionamentoId != null) {
                fkPagamentoEstacionamentoId.getReservaCollection().add(reserva);
                fkPagamentoEstacionamentoId = em.merge(fkPagamentoEstacionamentoId);
            }
            if (fkVagaId != null) {
                fkVagaId.getReservaCollection().add(reserva);
                fkVagaId = em.merge(fkVagaId);
            }
            if (fkVeiculoId != null) {
                fkVeiculoId.getReservaCollection().add(reserva);
                fkVeiculoId = em.merge(fkVeiculoId);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findReserva(reserva.getId()) != null) {
                throw new PreexistingEntityException("Reserva " + reserva + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Reserva reserva) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Reserva persistentReserva = em.find(Reserva.class, reserva.getId());
            Pagamento fkPagamentoEstacionamentoIdOld = persistentReserva.getFkPagamentoEstacionamentoId();
            Pagamento fkPagamentoEstacionamentoIdNew = reserva.getFkPagamentoEstacionamentoId();
            Vaga fkVagaIdOld = persistentReserva.getFkVagaId();
            Vaga fkVagaIdNew = reserva.getFkVagaId();
            Veiculo fkVeiculoIdOld = persistentReserva.getFkVeiculoId();
            Veiculo fkVeiculoIdNew = reserva.getFkVeiculoId();
            if (fkPagamentoEstacionamentoIdNew != null) {
                fkPagamentoEstacionamentoIdNew = em.getReference(fkPagamentoEstacionamentoIdNew.getClass(), fkPagamentoEstacionamentoIdNew.getId());
                reserva.setFkPagamentoEstacionamentoId(fkPagamentoEstacionamentoIdNew);
            }
            if (fkVagaIdNew != null) {
                fkVagaIdNew = em.getReference(fkVagaIdNew.getClass(), fkVagaIdNew.getId());
                reserva.setFkVagaId(fkVagaIdNew);
            }
            if (fkVeiculoIdNew != null) {
                fkVeiculoIdNew = em.getReference(fkVeiculoIdNew.getClass(), fkVeiculoIdNew.getId());
                reserva.setFkVeiculoId(fkVeiculoIdNew);
            }
            reserva = em.merge(reserva);
            if (fkPagamentoEstacionamentoIdOld != null && !fkPagamentoEstacionamentoIdOld.equals(fkPagamentoEstacionamentoIdNew)) {
                fkPagamentoEstacionamentoIdOld.getReservaCollection().remove(reserva);
                fkPagamentoEstacionamentoIdOld = em.merge(fkPagamentoEstacionamentoIdOld);
            }
            if (fkPagamentoEstacionamentoIdNew != null && !fkPagamentoEstacionamentoIdNew.equals(fkPagamentoEstacionamentoIdOld)) {
                fkPagamentoEstacionamentoIdNew.getReservaCollection().add(reserva);
                fkPagamentoEstacionamentoIdNew = em.merge(fkPagamentoEstacionamentoIdNew);
            }
            if (fkVagaIdOld != null && !fkVagaIdOld.equals(fkVagaIdNew)) {
                fkVagaIdOld.getReservaCollection().remove(reserva);
                fkVagaIdOld = em.merge(fkVagaIdOld);
            }
            if (fkVagaIdNew != null && !fkVagaIdNew.equals(fkVagaIdOld)) {
                fkVagaIdNew.getReservaCollection().add(reserva);
                fkVagaIdNew = em.merge(fkVagaIdNew);
            }
            if (fkVeiculoIdOld != null && !fkVeiculoIdOld.equals(fkVeiculoIdNew)) {
                fkVeiculoIdOld.getReservaCollection().remove(reserva);
                fkVeiculoIdOld = em.merge(fkVeiculoIdOld);
            }
            if (fkVeiculoIdNew != null && !fkVeiculoIdNew.equals(fkVeiculoIdOld)) {
                fkVeiculoIdNew.getReservaCollection().add(reserva);
                fkVeiculoIdNew = em.merge(fkVeiculoIdNew);
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
                Integer id = reserva.getId();
                if (findReserva(id) == null) {
                    throw new NonexistentEntityException("The reserva with id " + id + " no longer exists.");
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
            Reserva reserva;
            try {
                reserva = em.getReference(Reserva.class, id);
                reserva.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The reserva with id " + id + " no longer exists.", enfe);
            }
            Pagamento fkPagamentoEstacionamentoId = reserva.getFkPagamentoEstacionamentoId();
            if (fkPagamentoEstacionamentoId != null) {
                fkPagamentoEstacionamentoId.getReservaCollection().remove(reserva);
                fkPagamentoEstacionamentoId = em.merge(fkPagamentoEstacionamentoId);
            }
            Vaga fkVagaId = reserva.getFkVagaId();
            if (fkVagaId != null) {
                fkVagaId.getReservaCollection().remove(reserva);
                fkVagaId = em.merge(fkVagaId);
            }
            Veiculo fkVeiculoId = reserva.getFkVeiculoId();
            if (fkVeiculoId != null) {
                fkVeiculoId.getReservaCollection().remove(reserva);
                fkVeiculoId = em.merge(fkVeiculoId);
            }
            em.remove(reserva);
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

    public List<Reserva> findReservaEntities() {
        return findReservaEntities(true, -1, -1);
    }

    public List<Reserva> findReservaEntities(int maxResults, int firstResult) {
        return findReservaEntities(false, maxResults, firstResult);
    }

    private List<Reserva> findReservaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Reserva.class));
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

    public Reserva findReserva(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Reserva.class, id);
        } finally {
            em.close();
        }
    }

    public int getReservaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Reserva> rt = cq.from(Reserva.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
