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
import modelo.operadores.PessoaFisica;
import modelo.componentes.Veiculo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import modelo.operacoes.Cartao;
import modelo.operadores.Motorista;
import padroes.DAO.exceptions.NonexistentEntityException;
import padroes.DAO.exceptions.PreexistingEntityException;
import padroes.DAO.exceptions.RollbackFailureException;

/**
 *
 * @author Cacherow
 */
public class MotoristaJpaController implements Serializable {

    public MotoristaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Motorista motorista) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (motorista.getVeiculoCollection() == null) {
            motorista.setVeiculoCollection(new ArrayList<Veiculo>());
        }
        if (motorista.getCartaoCollection() == null) {
            motorista.setCartaoCollection(new ArrayList<Cartao>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            PessoaFisica fkPessoaFisicaId = motorista.getFkPessoaFisicaId();
            if (fkPessoaFisicaId != null) {
                fkPessoaFisicaId = em.getReference(fkPessoaFisicaId.getClass(), fkPessoaFisicaId.getId());
                motorista.setFkPessoaFisicaId(fkPessoaFisicaId);
            }
            Collection<Veiculo> attachedVeiculoCollection = new ArrayList<Veiculo>();
            for (Veiculo veiculoCollectionVeiculoToAttach : motorista.getVeiculoCollection()) {
                veiculoCollectionVeiculoToAttach = em.getReference(veiculoCollectionVeiculoToAttach.getClass(), veiculoCollectionVeiculoToAttach.getId());
                attachedVeiculoCollection.add(veiculoCollectionVeiculoToAttach);
            }
            motorista.setVeiculoCollection(attachedVeiculoCollection);
            Collection<Cartao> attachedCartaoCollection = new ArrayList<Cartao>();
            for (Cartao cartaoCollectionCartaoToAttach : motorista.getCartaoCollection()) {
                cartaoCollectionCartaoToAttach = em.getReference(cartaoCollectionCartaoToAttach.getClass(), cartaoCollectionCartaoToAttach.getId());
                attachedCartaoCollection.add(cartaoCollectionCartaoToAttach);
            }
            motorista.setCartaoCollection(attachedCartaoCollection);
            em.persist(motorista);
            if (fkPessoaFisicaId != null) {
                fkPessoaFisicaId.getMotoristaCollection().add(motorista);
                fkPessoaFisicaId = em.merge(fkPessoaFisicaId);
            }
            for (Veiculo veiculoCollectionVeiculo : motorista.getVeiculoCollection()) {
                Motorista oldFkMotoristaIdOfVeiculoCollectionVeiculo = veiculoCollectionVeiculo.getFkMotoristaId();
                veiculoCollectionVeiculo.setFkMotoristaId(motorista);
                veiculoCollectionVeiculo = em.merge(veiculoCollectionVeiculo);
                if (oldFkMotoristaIdOfVeiculoCollectionVeiculo != null) {
                    oldFkMotoristaIdOfVeiculoCollectionVeiculo.getVeiculoCollection().remove(veiculoCollectionVeiculo);
                    oldFkMotoristaIdOfVeiculoCollectionVeiculo = em.merge(oldFkMotoristaIdOfVeiculoCollectionVeiculo);
                }
            }
            for (Cartao cartaoCollectionCartao : motorista.getCartaoCollection()) {
                Motorista oldFkMotoristaIdOfCartaoCollectionCartao = cartaoCollectionCartao.getFkMotoristaId();
                cartaoCollectionCartao.setFkMotoristaId(motorista);
                cartaoCollectionCartao = em.merge(cartaoCollectionCartao);
                if (oldFkMotoristaIdOfCartaoCollectionCartao != null) {
                    oldFkMotoristaIdOfCartaoCollectionCartao.getCartaoCollection().remove(cartaoCollectionCartao);
                    oldFkMotoristaIdOfCartaoCollectionCartao = em.merge(oldFkMotoristaIdOfCartaoCollectionCartao);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findMotorista(motorista.getId()) != null) {
                throw new PreexistingEntityException("Motorista " + motorista + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Motorista motorista) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Motorista persistentMotorista = em.find(Motorista.class, motorista.getId());
            PessoaFisica fkPessoaFisicaIdOld = persistentMotorista.getFkPessoaFisicaId();
            PessoaFisica fkPessoaFisicaIdNew = motorista.getFkPessoaFisicaId();
            Collection<Veiculo> veiculoCollectionOld = persistentMotorista.getVeiculoCollection();
            Collection<Veiculo> veiculoCollectionNew = motorista.getVeiculoCollection();
            Collection<Cartao> cartaoCollectionOld = persistentMotorista.getCartaoCollection();
            Collection<Cartao> cartaoCollectionNew = motorista.getCartaoCollection();
            if (fkPessoaFisicaIdNew != null) {
                fkPessoaFisicaIdNew = em.getReference(fkPessoaFisicaIdNew.getClass(), fkPessoaFisicaIdNew.getId());
                motorista.setFkPessoaFisicaId(fkPessoaFisicaIdNew);
            }
            Collection<Veiculo> attachedVeiculoCollectionNew = new ArrayList<Veiculo>();
            for (Veiculo veiculoCollectionNewVeiculoToAttach : veiculoCollectionNew) {
                veiculoCollectionNewVeiculoToAttach = em.getReference(veiculoCollectionNewVeiculoToAttach.getClass(), veiculoCollectionNewVeiculoToAttach.getId());
                attachedVeiculoCollectionNew.add(veiculoCollectionNewVeiculoToAttach);
            }
            veiculoCollectionNew = attachedVeiculoCollectionNew;
            motorista.setVeiculoCollection(veiculoCollectionNew);
            Collection<Cartao> attachedCartaoCollectionNew = new ArrayList<Cartao>();
            for (Cartao cartaoCollectionNewCartaoToAttach : cartaoCollectionNew) {
                cartaoCollectionNewCartaoToAttach = em.getReference(cartaoCollectionNewCartaoToAttach.getClass(), cartaoCollectionNewCartaoToAttach.getId());
                attachedCartaoCollectionNew.add(cartaoCollectionNewCartaoToAttach);
            }
            cartaoCollectionNew = attachedCartaoCollectionNew;
            motorista.setCartaoCollection(cartaoCollectionNew);
            motorista = em.merge(motorista);
            if (fkPessoaFisicaIdOld != null && !fkPessoaFisicaIdOld.equals(fkPessoaFisicaIdNew)) {
                fkPessoaFisicaIdOld.getMotoristaCollection().remove(motorista);
                fkPessoaFisicaIdOld = em.merge(fkPessoaFisicaIdOld);
            }
            if (fkPessoaFisicaIdNew != null && !fkPessoaFisicaIdNew.equals(fkPessoaFisicaIdOld)) {
                fkPessoaFisicaIdNew.getMotoristaCollection().add(motorista);
                fkPessoaFisicaIdNew = em.merge(fkPessoaFisicaIdNew);
            }
            for (Veiculo veiculoCollectionOldVeiculo : veiculoCollectionOld) {
                if (!veiculoCollectionNew.contains(veiculoCollectionOldVeiculo)) {
                    veiculoCollectionOldVeiculo.setFkMotoristaId(null);
                    veiculoCollectionOldVeiculo = em.merge(veiculoCollectionOldVeiculo);
                }
            }
            for (Veiculo veiculoCollectionNewVeiculo : veiculoCollectionNew) {
                if (!veiculoCollectionOld.contains(veiculoCollectionNewVeiculo)) {
                    Motorista oldFkMotoristaIdOfVeiculoCollectionNewVeiculo = veiculoCollectionNewVeiculo.getFkMotoristaId();
                    veiculoCollectionNewVeiculo.setFkMotoristaId(motorista);
                    veiculoCollectionNewVeiculo = em.merge(veiculoCollectionNewVeiculo);
                    if (oldFkMotoristaIdOfVeiculoCollectionNewVeiculo != null && !oldFkMotoristaIdOfVeiculoCollectionNewVeiculo.equals(motorista)) {
                        oldFkMotoristaIdOfVeiculoCollectionNewVeiculo.getVeiculoCollection().remove(veiculoCollectionNewVeiculo);
                        oldFkMotoristaIdOfVeiculoCollectionNewVeiculo = em.merge(oldFkMotoristaIdOfVeiculoCollectionNewVeiculo);
                    }
                }
            }
            for (Cartao cartaoCollectionOldCartao : cartaoCollectionOld) {
                if (!cartaoCollectionNew.contains(cartaoCollectionOldCartao)) {
                    cartaoCollectionOldCartao.setFkMotoristaId(null);
                    cartaoCollectionOldCartao = em.merge(cartaoCollectionOldCartao);
                }
            }
            for (Cartao cartaoCollectionNewCartao : cartaoCollectionNew) {
                if (!cartaoCollectionOld.contains(cartaoCollectionNewCartao)) {
                    Motorista oldFkMotoristaIdOfCartaoCollectionNewCartao = cartaoCollectionNewCartao.getFkMotoristaId();
                    cartaoCollectionNewCartao.setFkMotoristaId(motorista);
                    cartaoCollectionNewCartao = em.merge(cartaoCollectionNewCartao);
                    if (oldFkMotoristaIdOfCartaoCollectionNewCartao != null && !oldFkMotoristaIdOfCartaoCollectionNewCartao.equals(motorista)) {
                        oldFkMotoristaIdOfCartaoCollectionNewCartao.getCartaoCollection().remove(cartaoCollectionNewCartao);
                        oldFkMotoristaIdOfCartaoCollectionNewCartao = em.merge(oldFkMotoristaIdOfCartaoCollectionNewCartao);
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
                Integer id = motorista.getId();
                if (findMotorista(id) == null) {
                    throw new NonexistentEntityException("The motorista with id " + id + " no longer exists.");
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
            Motorista motorista;
            try {
                motorista = em.getReference(Motorista.class, id);
                motorista.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The motorista with id " + id + " no longer exists.", enfe);
            }
            PessoaFisica fkPessoaFisicaId = motorista.getFkPessoaFisicaId();
            if (fkPessoaFisicaId != null) {
                fkPessoaFisicaId.getMotoristaCollection().remove(motorista);
                fkPessoaFisicaId = em.merge(fkPessoaFisicaId);
            }
            Collection<Veiculo> veiculoCollection = motorista.getVeiculoCollection();
            for (Veiculo veiculoCollectionVeiculo : veiculoCollection) {
                veiculoCollectionVeiculo.setFkMotoristaId(null);
                veiculoCollectionVeiculo = em.merge(veiculoCollectionVeiculo);
            }
            Collection<Cartao> cartaoCollection = motorista.getCartaoCollection();
            for (Cartao cartaoCollectionCartao : cartaoCollection) {
                cartaoCollectionCartao.setFkMotoristaId(null);
                cartaoCollectionCartao = em.merge(cartaoCollectionCartao);
            }
            em.remove(motorista);
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

    public List<Motorista> findMotoristaEntities() {
        return findMotoristaEntities(true, -1, -1);
    }

    public List<Motorista> findMotoristaEntities(int maxResults, int firstResult) {
        return findMotoristaEntities(false, maxResults, firstResult);
    }

    private List<Motorista> findMotoristaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Motorista.class));
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

    public Motorista findMotorista(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Motorista.class, id);
        } finally {
            em.close();
        }
    }

    public int getMotoristaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Motorista> rt = cq.from(Motorista.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
