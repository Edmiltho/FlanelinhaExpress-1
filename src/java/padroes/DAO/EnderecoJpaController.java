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
import modelo.locais.Bairro;
import modelo.componentes.Estacionamento;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import modelo.locais.Endereco;
import padroes.DAO.exceptions.NonexistentEntityException;
import padroes.DAO.exceptions.PreexistingEntityException;
import padroes.DAO.exceptions.RollbackFailureException;

/**
 *
 * @author Cacherow
 */
public class EnderecoJpaController implements Serializable {

    public EnderecoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Endereco endereco) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (endereco.getEstacionamentoCollection() == null) {
            endereco.setEstacionamentoCollection(new ArrayList<Estacionamento>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Bairro fkBairroId = endereco.getFkBairroId();
            if (fkBairroId != null) {
                fkBairroId = em.getReference(fkBairroId.getClass(), fkBairroId.getId());
                endereco.setFkBairroId(fkBairroId);
            }
            Collection<Estacionamento> attachedEstacionamentoCollection = new ArrayList<Estacionamento>();
            for (Estacionamento estacionamentoCollectionEstacionamentoToAttach : endereco.getEstacionamentoCollection()) {
                estacionamentoCollectionEstacionamentoToAttach = em.getReference(estacionamentoCollectionEstacionamentoToAttach.getClass(), estacionamentoCollectionEstacionamentoToAttach.getId());
                attachedEstacionamentoCollection.add(estacionamentoCollectionEstacionamentoToAttach);
            }
            endereco.setEstacionamentoCollection(attachedEstacionamentoCollection);
            em.persist(endereco);
            if (fkBairroId != null) {
                fkBairroId.getEnderecoCollection().add(endereco);
                fkBairroId = em.merge(fkBairroId);
            }
            for (Estacionamento estacionamentoCollectionEstacionamento : endereco.getEstacionamentoCollection()) {
                Endereco oldFkEnderecoIdOfEstacionamentoCollectionEstacionamento = estacionamentoCollectionEstacionamento.getFkEnderecoId();
                estacionamentoCollectionEstacionamento.setFkEnderecoId(endereco);
                estacionamentoCollectionEstacionamento = em.merge(estacionamentoCollectionEstacionamento);
                if (oldFkEnderecoIdOfEstacionamentoCollectionEstacionamento != null) {
                    oldFkEnderecoIdOfEstacionamentoCollectionEstacionamento.getEstacionamentoCollection().remove(estacionamentoCollectionEstacionamento);
                    oldFkEnderecoIdOfEstacionamentoCollectionEstacionamento = em.merge(oldFkEnderecoIdOfEstacionamentoCollectionEstacionamento);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findEndereco(endereco.getId()) != null) {
                throw new PreexistingEntityException("Endereco " + endereco + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Endereco endereco) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Endereco persistentEndereco = em.find(Endereco.class, endereco.getId());
            Bairro fkBairroIdOld = persistentEndereco.getFkBairroId();
            Bairro fkBairroIdNew = endereco.getFkBairroId();
            Collection<Estacionamento> estacionamentoCollectionOld = persistentEndereco.getEstacionamentoCollection();
            Collection<Estacionamento> estacionamentoCollectionNew = endereco.getEstacionamentoCollection();
            if (fkBairroIdNew != null) {
                fkBairroIdNew = em.getReference(fkBairroIdNew.getClass(), fkBairroIdNew.getId());
                endereco.setFkBairroId(fkBairroIdNew);
            }
            Collection<Estacionamento> attachedEstacionamentoCollectionNew = new ArrayList<Estacionamento>();
            for (Estacionamento estacionamentoCollectionNewEstacionamentoToAttach : estacionamentoCollectionNew) {
                estacionamentoCollectionNewEstacionamentoToAttach = em.getReference(estacionamentoCollectionNewEstacionamentoToAttach.getClass(), estacionamentoCollectionNewEstacionamentoToAttach.getId());
                attachedEstacionamentoCollectionNew.add(estacionamentoCollectionNewEstacionamentoToAttach);
            }
            estacionamentoCollectionNew = attachedEstacionamentoCollectionNew;
            endereco.setEstacionamentoCollection(estacionamentoCollectionNew);
            endereco = em.merge(endereco);
            if (fkBairroIdOld != null && !fkBairroIdOld.equals(fkBairroIdNew)) {
                fkBairroIdOld.getEnderecoCollection().remove(endereco);
                fkBairroIdOld = em.merge(fkBairroIdOld);
            }
            if (fkBairroIdNew != null && !fkBairroIdNew.equals(fkBairroIdOld)) {
                fkBairroIdNew.getEnderecoCollection().add(endereco);
                fkBairroIdNew = em.merge(fkBairroIdNew);
            }
            for (Estacionamento estacionamentoCollectionOldEstacionamento : estacionamentoCollectionOld) {
                if (!estacionamentoCollectionNew.contains(estacionamentoCollectionOldEstacionamento)) {
                    estacionamentoCollectionOldEstacionamento.setFkEnderecoId(null);
                    estacionamentoCollectionOldEstacionamento = em.merge(estacionamentoCollectionOldEstacionamento);
                }
            }
            for (Estacionamento estacionamentoCollectionNewEstacionamento : estacionamentoCollectionNew) {
                if (!estacionamentoCollectionOld.contains(estacionamentoCollectionNewEstacionamento)) {
                    Endereco oldFkEnderecoIdOfEstacionamentoCollectionNewEstacionamento = estacionamentoCollectionNewEstacionamento.getFkEnderecoId();
                    estacionamentoCollectionNewEstacionamento.setFkEnderecoId(endereco);
                    estacionamentoCollectionNewEstacionamento = em.merge(estacionamentoCollectionNewEstacionamento);
                    if (oldFkEnderecoIdOfEstacionamentoCollectionNewEstacionamento != null && !oldFkEnderecoIdOfEstacionamentoCollectionNewEstacionamento.equals(endereco)) {
                        oldFkEnderecoIdOfEstacionamentoCollectionNewEstacionamento.getEstacionamentoCollection().remove(estacionamentoCollectionNewEstacionamento);
                        oldFkEnderecoIdOfEstacionamentoCollectionNewEstacionamento = em.merge(oldFkEnderecoIdOfEstacionamentoCollectionNewEstacionamento);
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
                Integer id = endereco.getId();
                if (findEndereco(id) == null) {
                    throw new NonexistentEntityException("The endereco with id " + id + " no longer exists.");
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
            Endereco endereco;
            try {
                endereco = em.getReference(Endereco.class, id);
                endereco.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The endereco with id " + id + " no longer exists.", enfe);
            }
            Bairro fkBairroId = endereco.getFkBairroId();
            if (fkBairroId != null) {
                fkBairroId.getEnderecoCollection().remove(endereco);
                fkBairroId = em.merge(fkBairroId);
            }
            Collection<Estacionamento> estacionamentoCollection = endereco.getEstacionamentoCollection();
            for (Estacionamento estacionamentoCollectionEstacionamento : estacionamentoCollection) {
                estacionamentoCollectionEstacionamento.setFkEnderecoId(null);
                estacionamentoCollectionEstacionamento = em.merge(estacionamentoCollectionEstacionamento);
            }
            em.remove(endereco);
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

    public List<Endereco> findEnderecoEntities() {
        return findEnderecoEntities(true, -1, -1);
    }

    public List<Endereco> findEnderecoEntities(int maxResults, int firstResult) {
        return findEnderecoEntities(false, maxResults, firstResult);
    }

    private List<Endereco> findEnderecoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Endereco.class));
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

    public Endereco findEndereco(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Endereco.class, id);
        } finally {
            em.close();
        }
    }

    public int getEnderecoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Endereco> rt = cq.from(Endereco.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
