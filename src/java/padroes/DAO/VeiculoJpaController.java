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
import ClassesEntidade.Modelo;
import modelo.operadores.Motorista;
import modelo.operacoes.Reserva;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import modelo.componentes.Veiculo;
import padroes.DAO.exceptions.NonexistentEntityException;
import padroes.DAO.exceptions.PreexistingEntityException;
import padroes.DAO.exceptions.RollbackFailureException;

/**
 *
 * @author Cacherow
 */
public class VeiculoJpaController implements Serializable {

    public VeiculoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Veiculo veiculo) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (veiculo.getReservaCollection() == null) {
            veiculo.setReservaCollection(new ArrayList<Reserva>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Modelo fkModeloId = veiculo.getFkModeloId();
            if (fkModeloId != null) {
                fkModeloId = em.getReference(fkModeloId.getClass(), fkModeloId.getId());
                veiculo.setFkModeloId(fkModeloId);
            }
            Motorista fkMotoristaId = veiculo.getFkMotoristaId();
            if (fkMotoristaId != null) {
                fkMotoristaId = em.getReference(fkMotoristaId.getClass(), fkMotoristaId.getId());
                veiculo.setFkMotoristaId(fkMotoristaId);
            }
            Collection<Reserva> attachedReservaCollection = new ArrayList<Reserva>();
            for (Reserva reservaCollectionReservaToAttach : veiculo.getReservaCollection()) {
                reservaCollectionReservaToAttach = em.getReference(reservaCollectionReservaToAttach.getClass(), reservaCollectionReservaToAttach.getId());
                attachedReservaCollection.add(reservaCollectionReservaToAttach);
            }
            veiculo.setReservaCollection(attachedReservaCollection);
            em.persist(veiculo);
            if (fkModeloId != null) {
                fkModeloId.getVeiculoCollection().add(veiculo);
                fkModeloId = em.merge(fkModeloId);
            }
            if (fkMotoristaId != null) {
                fkMotoristaId.getVeiculoCollection().add(veiculo);
                fkMotoristaId = em.merge(fkMotoristaId);
            }
            for (Reserva reservaCollectionReserva : veiculo.getReservaCollection()) {
                Veiculo oldFkVeiculoIdOfReservaCollectionReserva = reservaCollectionReserva.getFkVeiculoId();
                reservaCollectionReserva.setFkVeiculoId(veiculo);
                reservaCollectionReserva = em.merge(reservaCollectionReserva);
                if (oldFkVeiculoIdOfReservaCollectionReserva != null) {
                    oldFkVeiculoIdOfReservaCollectionReserva.getReservaCollection().remove(reservaCollectionReserva);
                    oldFkVeiculoIdOfReservaCollectionReserva = em.merge(oldFkVeiculoIdOfReservaCollectionReserva);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findVeiculo(veiculo.getId()) != null) {
                throw new PreexistingEntityException("Veiculo " + veiculo + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Veiculo veiculo) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Veiculo persistentVeiculo = em.find(Veiculo.class, veiculo.getId());
            Modelo fkModeloIdOld = persistentVeiculo.getFkModeloId();
            Modelo fkModeloIdNew = veiculo.getFkModeloId();
            Motorista fkMotoristaIdOld = persistentVeiculo.getFkMotoristaId();
            Motorista fkMotoristaIdNew = veiculo.getFkMotoristaId();
            Collection<Reserva> reservaCollectionOld = persistentVeiculo.getReservaCollection();
            Collection<Reserva> reservaCollectionNew = veiculo.getReservaCollection();
            if (fkModeloIdNew != null) {
                fkModeloIdNew = em.getReference(fkModeloIdNew.getClass(), fkModeloIdNew.getId());
                veiculo.setFkModeloId(fkModeloIdNew);
            }
            if (fkMotoristaIdNew != null) {
                fkMotoristaIdNew = em.getReference(fkMotoristaIdNew.getClass(), fkMotoristaIdNew.getId());
                veiculo.setFkMotoristaId(fkMotoristaIdNew);
            }
            Collection<Reserva> attachedReservaCollectionNew = new ArrayList<Reserva>();
            for (Reserva reservaCollectionNewReservaToAttach : reservaCollectionNew) {
                reservaCollectionNewReservaToAttach = em.getReference(reservaCollectionNewReservaToAttach.getClass(), reservaCollectionNewReservaToAttach.getId());
                attachedReservaCollectionNew.add(reservaCollectionNewReservaToAttach);
            }
            reservaCollectionNew = attachedReservaCollectionNew;
            veiculo.setReservaCollection(reservaCollectionNew);
            veiculo = em.merge(veiculo);
            if (fkModeloIdOld != null && !fkModeloIdOld.equals(fkModeloIdNew)) {
                fkModeloIdOld.getVeiculoCollection().remove(veiculo);
                fkModeloIdOld = em.merge(fkModeloIdOld);
            }
            if (fkModeloIdNew != null && !fkModeloIdNew.equals(fkModeloIdOld)) {
                fkModeloIdNew.getVeiculoCollection().add(veiculo);
                fkModeloIdNew = em.merge(fkModeloIdNew);
            }
            if (fkMotoristaIdOld != null && !fkMotoristaIdOld.equals(fkMotoristaIdNew)) {
                fkMotoristaIdOld.getVeiculoCollection().remove(veiculo);
                fkMotoristaIdOld = em.merge(fkMotoristaIdOld);
            }
            if (fkMotoristaIdNew != null && !fkMotoristaIdNew.equals(fkMotoristaIdOld)) {
                fkMotoristaIdNew.getVeiculoCollection().add(veiculo);
                fkMotoristaIdNew = em.merge(fkMotoristaIdNew);
            }
            for (Reserva reservaCollectionOldReserva : reservaCollectionOld) {
                if (!reservaCollectionNew.contains(reservaCollectionOldReserva)) {
                    reservaCollectionOldReserva.setFkVeiculoId(null);
                    reservaCollectionOldReserva = em.merge(reservaCollectionOldReserva);
                }
            }
            for (Reserva reservaCollectionNewReserva : reservaCollectionNew) {
                if (!reservaCollectionOld.contains(reservaCollectionNewReserva)) {
                    Veiculo oldFkVeiculoIdOfReservaCollectionNewReserva = reservaCollectionNewReserva.getFkVeiculoId();
                    reservaCollectionNewReserva.setFkVeiculoId(veiculo);
                    reservaCollectionNewReserva = em.merge(reservaCollectionNewReserva);
                    if (oldFkVeiculoIdOfReservaCollectionNewReserva != null && !oldFkVeiculoIdOfReservaCollectionNewReserva.equals(veiculo)) {
                        oldFkVeiculoIdOfReservaCollectionNewReserva.getReservaCollection().remove(reservaCollectionNewReserva);
                        oldFkVeiculoIdOfReservaCollectionNewReserva = em.merge(oldFkVeiculoIdOfReservaCollectionNewReserva);
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
                Integer id = veiculo.getId();
                if (findVeiculo(id) == null) {
                    throw new NonexistentEntityException("The veiculo with id " + id + " no longer exists.");
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
            Veiculo veiculo;
            try {
                veiculo = em.getReference(Veiculo.class, id);
                veiculo.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The veiculo with id " + id + " no longer exists.", enfe);
            }
            Modelo fkModeloId = veiculo.getFkModeloId();
            if (fkModeloId != null) {
                fkModeloId.getVeiculoCollection().remove(veiculo);
                fkModeloId = em.merge(fkModeloId);
            }
            Motorista fkMotoristaId = veiculo.getFkMotoristaId();
            if (fkMotoristaId != null) {
                fkMotoristaId.getVeiculoCollection().remove(veiculo);
                fkMotoristaId = em.merge(fkMotoristaId);
            }
            Collection<Reserva> reservaCollection = veiculo.getReservaCollection();
            for (Reserva reservaCollectionReserva : reservaCollection) {
                reservaCollectionReserva.setFkVeiculoId(null);
                reservaCollectionReserva = em.merge(reservaCollectionReserva);
            }
            em.remove(veiculo);
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

    public List<Veiculo> findVeiculoEntities() {
        return findVeiculoEntities(true, -1, -1);
    }

    public List<Veiculo> findVeiculoEntities(int maxResults, int firstResult) {
        return findVeiculoEntities(false, maxResults, firstResult);
    }

    private List<Veiculo> findVeiculoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Veiculo.class));
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

    public Veiculo findVeiculo(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Veiculo.class, id);
        } finally {
            em.close();
        }
    }

    public int getVeiculoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Veiculo> rt = cq.from(Veiculo.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
