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
import modelo.operadores.Motorista;
import modelo.operacoes.Pagamento;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import modelo.operacoes.Cartao;
import padroes.DAO.exceptions.NonexistentEntityException;
import padroes.DAO.exceptions.PreexistingEntityException;
import padroes.DAO.exceptions.RollbackFailureException;

/**
 *
 * @author Cacherow
 */
public class CartaoJpaController implements Serializable {

    public CartaoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Cartao cartao) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (cartao.getPagamentoEstacionamentoCollection() == null) {
            cartao.setPagamentoEstacionamentoCollection(new ArrayList<Pagamento>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Motorista fkMotoristaId = cartao.getFkMotoristaId();
            if (fkMotoristaId != null) {
                fkMotoristaId = em.getReference(fkMotoristaId.getClass(), fkMotoristaId.getId());
                cartao.setFkMotoristaId(fkMotoristaId);
            }
            Collection<Pagamento> attachedPagamentoEstacionamentoCollection = new ArrayList<Pagamento>();
            for (Pagamento pagamentoEstacionamentoCollectionPagamentoToAttach : cartao.getPagamentoEstacionamentoCollection()) {
                pagamentoEstacionamentoCollectionPagamentoToAttach = em.getReference(pagamentoEstacionamentoCollectionPagamentoToAttach.getClass(), pagamentoEstacionamentoCollectionPagamentoToAttach.getId());
                attachedPagamentoEstacionamentoCollection.add(pagamentoEstacionamentoCollectionPagamentoToAttach);
            }
            cartao.setPagamentoEstacionamentoCollection(attachedPagamentoEstacionamentoCollection);
            em.persist(cartao);
            if (fkMotoristaId != null) {
                fkMotoristaId.getCartaoCollection().add(cartao);
                fkMotoristaId = em.merge(fkMotoristaId);
            }
            for (Pagamento pagamentoEstacionamentoCollectionPagamento : cartao.getPagamentoEstacionamentoCollection()) {
                Cartao oldFkCartaoIdOfPagamentoEstacionamentoCollectionPagamento = pagamentoEstacionamentoCollectionPagamento.getFkCartaoId();
                pagamentoEstacionamentoCollectionPagamento.setFkCartaoId(cartao);
                pagamentoEstacionamentoCollectionPagamento = em.merge(pagamentoEstacionamentoCollectionPagamento);
                if (oldFkCartaoIdOfPagamentoEstacionamentoCollectionPagamento != null) {
                    oldFkCartaoIdOfPagamentoEstacionamentoCollectionPagamento.getPagamentoEstacionamentoCollection().remove(pagamentoEstacionamentoCollectionPagamento);
                    oldFkCartaoIdOfPagamentoEstacionamentoCollectionPagamento = em.merge(oldFkCartaoIdOfPagamentoEstacionamentoCollectionPagamento);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findCartao(cartao.getId()) != null) {
                throw new PreexistingEntityException("Cartao " + cartao + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Cartao cartao) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Cartao persistentCartao = em.find(Cartao.class, cartao.getId());
            Motorista fkMotoristaIdOld = persistentCartao.getFkMotoristaId();
            Motorista fkMotoristaIdNew = cartao.getFkMotoristaId();
            Collection<Pagamento> pagamentoEstacionamentoCollectionOld = persistentCartao.getPagamentoEstacionamentoCollection();
            Collection<Pagamento> pagamentoEstacionamentoCollectionNew = cartao.getPagamentoEstacionamentoCollection();
            if (fkMotoristaIdNew != null) {
                fkMotoristaIdNew = em.getReference(fkMotoristaIdNew.getClass(), fkMotoristaIdNew.getId());
                cartao.setFkMotoristaId(fkMotoristaIdNew);
            }
            Collection<Pagamento> attachedPagamentoEstacionamentoCollectionNew = new ArrayList<Pagamento>();
            for (Pagamento pagamentoEstacionamentoCollectionNewPagamentoToAttach : pagamentoEstacionamentoCollectionNew) {
                pagamentoEstacionamentoCollectionNewPagamentoToAttach = em.getReference(pagamentoEstacionamentoCollectionNewPagamentoToAttach.getClass(), pagamentoEstacionamentoCollectionNewPagamentoToAttach.getId());
                attachedPagamentoEstacionamentoCollectionNew.add(pagamentoEstacionamentoCollectionNewPagamentoToAttach);
            }
            pagamentoEstacionamentoCollectionNew = attachedPagamentoEstacionamentoCollectionNew;
            cartao.setPagamentoEstacionamentoCollection(pagamentoEstacionamentoCollectionNew);
            cartao = em.merge(cartao);
            if (fkMotoristaIdOld != null && !fkMotoristaIdOld.equals(fkMotoristaIdNew)) {
                fkMotoristaIdOld.getCartaoCollection().remove(cartao);
                fkMotoristaIdOld = em.merge(fkMotoristaIdOld);
            }
            if (fkMotoristaIdNew != null && !fkMotoristaIdNew.equals(fkMotoristaIdOld)) {
                fkMotoristaIdNew.getCartaoCollection().add(cartao);
                fkMotoristaIdNew = em.merge(fkMotoristaIdNew);
            }
            for (Pagamento pagamentoEstacionamentoCollectionOldPagamento : pagamentoEstacionamentoCollectionOld) {
                if (!pagamentoEstacionamentoCollectionNew.contains(pagamentoEstacionamentoCollectionOldPagamento)) {
                    pagamentoEstacionamentoCollectionOldPagamento.setFkCartaoId(null);
                    pagamentoEstacionamentoCollectionOldPagamento = em.merge(pagamentoEstacionamentoCollectionOldPagamento);
                }
            }
            for (Pagamento pagamentoEstacionamentoCollectionNewPagamento : pagamentoEstacionamentoCollectionNew) {
                if (!pagamentoEstacionamentoCollectionOld.contains(pagamentoEstacionamentoCollectionNewPagamento)) {
                    Cartao oldFkCartaoIdOfPagamentoEstacionamentoCollectionNewPagamento = pagamentoEstacionamentoCollectionNewPagamento.getFkCartaoId();
                    pagamentoEstacionamentoCollectionNewPagamento.setFkCartaoId(cartao);
                    pagamentoEstacionamentoCollectionNewPagamento = em.merge(pagamentoEstacionamentoCollectionNewPagamento);
                    if (oldFkCartaoIdOfPagamentoEstacionamentoCollectionNewPagamento != null && !oldFkCartaoIdOfPagamentoEstacionamentoCollectionNewPagamento.equals(cartao)) {
                        oldFkCartaoIdOfPagamentoEstacionamentoCollectionNewPagamento.getPagamentoEstacionamentoCollection().remove(pagamentoEstacionamentoCollectionNewPagamento);
                        oldFkCartaoIdOfPagamentoEstacionamentoCollectionNewPagamento = em.merge(oldFkCartaoIdOfPagamentoEstacionamentoCollectionNewPagamento);
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
                Integer id = cartao.getId();
                if (findCartao(id) == null) {
                    throw new NonexistentEntityException("The cartao with id " + id + " no longer exists.");
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
            Cartao cartao;
            try {
                cartao = em.getReference(Cartao.class, id);
                cartao.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The cartao with id " + id + " no longer exists.", enfe);
            }
            Motorista fkMotoristaId = cartao.getFkMotoristaId();
            if (fkMotoristaId != null) {
                fkMotoristaId.getCartaoCollection().remove(cartao);
                fkMotoristaId = em.merge(fkMotoristaId);
            }
            Collection<Pagamento> pagamentoEstacionamentoCollection = cartao.getPagamentoEstacionamentoCollection();
            for (Pagamento pagamentoEstacionamentoCollectionPagamento : pagamentoEstacionamentoCollection) {
                pagamentoEstacionamentoCollectionPagamento.setFkCartaoId(null);
                pagamentoEstacionamentoCollectionPagamento = em.merge(pagamentoEstacionamentoCollectionPagamento);
            }
            em.remove(cartao);
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

    public List<Cartao> findCartaoEntities() {
        return findCartaoEntities(true, -1, -1);
    }

    public List<Cartao> findCartaoEntities(int maxResults, int firstResult) {
        return findCartaoEntities(false, maxResults, firstResult);
    }

    private List<Cartao> findCartaoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Cartao.class));
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

    public Cartao findCartao(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Cartao.class, id);
        } finally {
            em.close();
        }
    }

    public int getCartaoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Cartao> rt = cq.from(Cartao.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
