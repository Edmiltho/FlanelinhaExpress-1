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
import modelo.operadores.Motorista;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import modelo.operadores.PessoaFisica;
import padroes.DAO.exceptions.NonexistentEntityException;
import padroes.DAO.exceptions.PreexistingEntityException;
import padroes.DAO.exceptions.RollbackFailureException;

/**
 *
 * @author Cacherow
 */
public class PessoaFisicaJpaController implements Serializable {

    public PessoaFisicaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(PessoaFisica pessoaFisica) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (pessoaFisica.getMotoristaCollection() == null) {
            pessoaFisica.setMotoristaCollection(new ArrayList<Motorista>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Pessoa fkPessoaId = pessoaFisica.getFkPessoaId();
            if (fkPessoaId != null) {
                fkPessoaId = em.getReference(fkPessoaId.getClass(), fkPessoaId.getId());
                pessoaFisica.setFkPessoaId(fkPessoaId);
            }
            Collection<Motorista> attachedMotoristaCollection = new ArrayList<Motorista>();
            for (Motorista motoristaCollectionMotoristaToAttach : pessoaFisica.getMotoristaCollection()) {
                motoristaCollectionMotoristaToAttach = em.getReference(motoristaCollectionMotoristaToAttach.getClass(), motoristaCollectionMotoristaToAttach.getId());
                attachedMotoristaCollection.add(motoristaCollectionMotoristaToAttach);
            }
            pessoaFisica.setMotoristaCollection(attachedMotoristaCollection);
            em.persist(pessoaFisica);
            if (fkPessoaId != null) {
                fkPessoaId.getPessoaFisicaCollection().add(pessoaFisica);
                fkPessoaId = em.merge(fkPessoaId);
            }
            for (Motorista motoristaCollectionMotorista : pessoaFisica.getMotoristaCollection()) {
                PessoaFisica oldFkPessoaFisicaIdOfMotoristaCollectionMotorista = motoristaCollectionMotorista.getFkPessoaFisicaId();
                motoristaCollectionMotorista.setFkPessoaFisicaId(pessoaFisica);
                motoristaCollectionMotorista = em.merge(motoristaCollectionMotorista);
                if (oldFkPessoaFisicaIdOfMotoristaCollectionMotorista != null) {
                    oldFkPessoaFisicaIdOfMotoristaCollectionMotorista.getMotoristaCollection().remove(motoristaCollectionMotorista);
                    oldFkPessoaFisicaIdOfMotoristaCollectionMotorista = em.merge(oldFkPessoaFisicaIdOfMotoristaCollectionMotorista);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findPessoaFisica(pessoaFisica.getId()) != null) {
                throw new PreexistingEntityException("PessoaFisica " + pessoaFisica + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(PessoaFisica pessoaFisica) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            PessoaFisica persistentPessoaFisica = em.find(PessoaFisica.class, pessoaFisica.getId());
            Pessoa fkPessoaIdOld = persistentPessoaFisica.getFkPessoaId();
            Pessoa fkPessoaIdNew = pessoaFisica.getFkPessoaId();
            Collection<Motorista> motoristaCollectionOld = persistentPessoaFisica.getMotoristaCollection();
            Collection<Motorista> motoristaCollectionNew = pessoaFisica.getMotoristaCollection();
            if (fkPessoaIdNew != null) {
                fkPessoaIdNew = em.getReference(fkPessoaIdNew.getClass(), fkPessoaIdNew.getId());
                pessoaFisica.setFkPessoaId(fkPessoaIdNew);
            }
            Collection<Motorista> attachedMotoristaCollectionNew = new ArrayList<Motorista>();
            for (Motorista motoristaCollectionNewMotoristaToAttach : motoristaCollectionNew) {
                motoristaCollectionNewMotoristaToAttach = em.getReference(motoristaCollectionNewMotoristaToAttach.getClass(), motoristaCollectionNewMotoristaToAttach.getId());
                attachedMotoristaCollectionNew.add(motoristaCollectionNewMotoristaToAttach);
            }
            motoristaCollectionNew = attachedMotoristaCollectionNew;
            pessoaFisica.setMotoristaCollection(motoristaCollectionNew);
            pessoaFisica = em.merge(pessoaFisica);
            if (fkPessoaIdOld != null && !fkPessoaIdOld.equals(fkPessoaIdNew)) {
                fkPessoaIdOld.getPessoaFisicaCollection().remove(pessoaFisica);
                fkPessoaIdOld = em.merge(fkPessoaIdOld);
            }
            if (fkPessoaIdNew != null && !fkPessoaIdNew.equals(fkPessoaIdOld)) {
                fkPessoaIdNew.getPessoaFisicaCollection().add(pessoaFisica);
                fkPessoaIdNew = em.merge(fkPessoaIdNew);
            }
            for (Motorista motoristaCollectionOldMotorista : motoristaCollectionOld) {
                if (!motoristaCollectionNew.contains(motoristaCollectionOldMotorista)) {
                    motoristaCollectionOldMotorista.setFkPessoaFisicaId(null);
                    motoristaCollectionOldMotorista = em.merge(motoristaCollectionOldMotorista);
                }
            }
            for (Motorista motoristaCollectionNewMotorista : motoristaCollectionNew) {
                if (!motoristaCollectionOld.contains(motoristaCollectionNewMotorista)) {
                    PessoaFisica oldFkPessoaFisicaIdOfMotoristaCollectionNewMotorista = motoristaCollectionNewMotorista.getFkPessoaFisicaId();
                    motoristaCollectionNewMotorista.setFkPessoaFisicaId(pessoaFisica);
                    motoristaCollectionNewMotorista = em.merge(motoristaCollectionNewMotorista);
                    if (oldFkPessoaFisicaIdOfMotoristaCollectionNewMotorista != null && !oldFkPessoaFisicaIdOfMotoristaCollectionNewMotorista.equals(pessoaFisica)) {
                        oldFkPessoaFisicaIdOfMotoristaCollectionNewMotorista.getMotoristaCollection().remove(motoristaCollectionNewMotorista);
                        oldFkPessoaFisicaIdOfMotoristaCollectionNewMotorista = em.merge(oldFkPessoaFisicaIdOfMotoristaCollectionNewMotorista);
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
                Integer id = pessoaFisica.getId();
                if (findPessoaFisica(id) == null) {
                    throw new NonexistentEntityException("The pessoaFisica with id " + id + " no longer exists.");
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
            PessoaFisica pessoaFisica;
            try {
                pessoaFisica = em.getReference(PessoaFisica.class, id);
                pessoaFisica.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The pessoaFisica with id " + id + " no longer exists.", enfe);
            }
            Pessoa fkPessoaId = pessoaFisica.getFkPessoaId();
            if (fkPessoaId != null) {
                fkPessoaId.getPessoaFisicaCollection().remove(pessoaFisica);
                fkPessoaId = em.merge(fkPessoaId);
            }
            Collection<Motorista> motoristaCollection = pessoaFisica.getMotoristaCollection();
            for (Motorista motoristaCollectionMotorista : motoristaCollection) {
                motoristaCollectionMotorista.setFkPessoaFisicaId(null);
                motoristaCollectionMotorista = em.merge(motoristaCollectionMotorista);
            }
            em.remove(pessoaFisica);
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

    public List<PessoaFisica> findPessoaFisicaEntities() {
        return findPessoaFisicaEntities(true, -1, -1);
    }

    public List<PessoaFisica> findPessoaFisicaEntities(int maxResults, int firstResult) {
        return findPessoaFisicaEntities(false, maxResults, firstResult);
    }

    private List<PessoaFisica> findPessoaFisicaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(PessoaFisica.class));
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

    public PessoaFisica findPessoaFisica(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(PessoaFisica.class, id);
        } finally {
            em.close();
        }
    }

    public int getPessoaFisicaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<PessoaFisica> rt = cq.from(PessoaFisica.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
