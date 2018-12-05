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
import modelo.componentes.Estacionamento;
import modelo.operacoes.Reserva;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import modelo.componentes.Vaga;
import padroes.DAO.exceptions.NonexistentEntityException;
import padroes.DAO.exceptions.PreexistingEntityException;
import padroes.DAO.exceptions.RollbackFailureException;

/**
 *
 * @author Cacherow
 */
public class VagaJpaController implements Serializable {

    public VagaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Vaga vaga) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (vaga.getReservaCollection() == null) {
            vaga.setReservaCollection(new ArrayList<Reserva>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Estacionamento fkEstacionamentoId = vaga.getFkEstacionamentoId();
            if (fkEstacionamentoId != null) {
                fkEstacionamentoId = em.getReference(fkEstacionamentoId.getClass(), fkEstacionamentoId.getId());
                vaga.setFkEstacionamentoId(fkEstacionamentoId);
            }
            Collection<Reserva> attachedReservaCollection = new ArrayList<Reserva>();
            for (Reserva reservaCollectionReservaToAttach : vaga.getReservaCollection()) {
                reservaCollectionReservaToAttach = em.getReference(reservaCollectionReservaToAttach.getClass(), reservaCollectionReservaToAttach.getId());
                attachedReservaCollection.add(reservaCollectionReservaToAttach);
            }
            vaga.setReservaCollection(attachedReservaCollection);
            em.persist(vaga);
            if (fkEstacionamentoId != null) {
                fkEstacionamentoId.getVagaCollection().add(vaga);
                fkEstacionamentoId = em.merge(fkEstacionamentoId);
            }
            for (Reserva reservaCollectionReserva : vaga.getReservaCollection()) {
                Vaga oldFkVagaIdOfReservaCollectionReserva = reservaCollectionReserva.getFkVagaId();
                reservaCollectionReserva.setFkVagaId(vaga);
                reservaCollectionReserva = em.merge(reservaCollectionReserva);
                if (oldFkVagaIdOfReservaCollectionReserva != null) {
                    oldFkVagaIdOfReservaCollectionReserva.getReservaCollection().remove(reservaCollectionReserva);
                    oldFkVagaIdOfReservaCollectionReserva = em.merge(oldFkVagaIdOfReservaCollectionReserva);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findVaga(vaga.getId()) != null) {
                throw new PreexistingEntityException("Vaga " + vaga + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Vaga vaga) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Vaga persistentVaga = em.find(Vaga.class, vaga.getId());
            Estacionamento fkEstacionamentoIdOld = persistentVaga.getFkEstacionamentoId();
            Estacionamento fkEstacionamentoIdNew = vaga.getFkEstacionamentoId();
            Collection<Reserva> reservaCollectionOld = persistentVaga.getReservaCollection();
            Collection<Reserva> reservaCollectionNew = vaga.getReservaCollection();
            if (fkEstacionamentoIdNew != null) {
                fkEstacionamentoIdNew = em.getReference(fkEstacionamentoIdNew.getClass(), fkEstacionamentoIdNew.getId());
                vaga.setFkEstacionamentoId(fkEstacionamentoIdNew);
            }
            Collection<Reserva> attachedReservaCollectionNew = new ArrayList<Reserva>();
            for (Reserva reservaCollectionNewReservaToAttach : reservaCollectionNew) {
                reservaCollectionNewReservaToAttach = em.getReference(reservaCollectionNewReservaToAttach.getClass(), reservaCollectionNewReservaToAttach.getId());
                attachedReservaCollectionNew.add(reservaCollectionNewReservaToAttach);
            }
            reservaCollectionNew = attachedReservaCollectionNew;
            vaga.setReservaCollection(reservaCollectionNew);
            vaga = em.merge(vaga);
            if (fkEstacionamentoIdOld != null && !fkEstacionamentoIdOld.equals(fkEstacionamentoIdNew)) {
                fkEstacionamentoIdOld.getVagaCollection().remove(vaga);
                fkEstacionamentoIdOld = em.merge(fkEstacionamentoIdOld);
            }
            if (fkEstacionamentoIdNew != null && !fkEstacionamentoIdNew.equals(fkEstacionamentoIdOld)) {
                fkEstacionamentoIdNew.getVagaCollection().add(vaga);
                fkEstacionamentoIdNew = em.merge(fkEstacionamentoIdNew);
            }
            for (Reserva reservaCollectionOldReserva : reservaCollectionOld) {
                if (!reservaCollectionNew.contains(reservaCollectionOldReserva)) {
                    reservaCollectionOldReserva.setFkVagaId(null);
                    reservaCollectionOldReserva = em.merge(reservaCollectionOldReserva);
                }
            }
            for (Reserva reservaCollectionNewReserva : reservaCollectionNew) {
                if (!reservaCollectionOld.contains(reservaCollectionNewReserva)) {
                    Vaga oldFkVagaIdOfReservaCollectionNewReserva = reservaCollectionNewReserva.getFkVagaId();
                    reservaCollectionNewReserva.setFkVagaId(vaga);
                    reservaCollectionNewReserva = em.merge(reservaCollectionNewReserva);
                    if (oldFkVagaIdOfReservaCollectionNewReserva != null && !oldFkVagaIdOfReservaCollectionNewReserva.equals(vaga)) {
                        oldFkVagaIdOfReservaCollectionNewReserva.getReservaCollection().remove(reservaCollectionNewReserva);
                        oldFkVagaIdOfReservaCollectionNewReserva = em.merge(oldFkVagaIdOfReservaCollectionNewReserva);
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
                Integer id = vaga.getId();
                if (findVaga(id) == null) {
                    throw new NonexistentEntityException("The vaga with id " + id + " no longer exists.");
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
            Vaga vaga;
            try {
                vaga = em.getReference(Vaga.class, id);
                vaga.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The vaga with id " + id + " no longer exists.", enfe);
            }
            Estacionamento fkEstacionamentoId = vaga.getFkEstacionamentoId();
            if (fkEstacionamentoId != null) {
                fkEstacionamentoId.getVagaCollection().remove(vaga);
                fkEstacionamentoId = em.merge(fkEstacionamentoId);
            }
            Collection<Reserva> reservaCollection = vaga.getReservaCollection();
            for (Reserva reservaCollectionReserva : reservaCollection) {
                reservaCollectionReserva.setFkVagaId(null);
                reservaCollectionReserva = em.merge(reservaCollectionReserva);
            }
            em.remove(vaga);
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

    public List<Vaga> findVagaEntities() {
        return findVagaEntities(true, -1, -1);
    }

    public List<Vaga> findVagaEntities(int maxResults, int firstResult) {
        return findVagaEntities(false, maxResults, firstResult);
    }

    private List<Vaga> findVagaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Vaga.class));
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

    public Vaga findVaga(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Vaga.class, id);
        } finally {
            em.close();
        }
    }

    public int getVagaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Vaga> rt = cq.from(Vaga.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
