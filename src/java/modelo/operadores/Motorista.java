/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.operadores;

import modelo.operacoes.Cartao;
import modelo.componentes.Veiculo;
import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Cacherow
 */
@Entity
@Table(name = "motorista")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Motorista.findAll", query = "SELECT m FROM Motorista m")
    , @NamedQuery(name = "Motorista.findByCnh", query = "SELECT m FROM Motorista m WHERE m.cnh = :cnh")
    , @NamedQuery(name = "Motorista.findById", query = "SELECT m FROM Motorista m WHERE m.id = :id")})
public class Motorista implements Serializable {

    private static final long serialVersionUID = 1L;
    @Size(max = 11)
    @Column(name = "cnh")
    private String cnh;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @OneToMany(mappedBy = "fkMotoristaId")
    private Collection<Veiculo> veiculoCollection;
    @OneToMany(mappedBy = "fkMotoristaId")
    private Collection<Cartao> cartaoCollection;
    @JoinColumn(name = "fk_pessoa_fisica_id", referencedColumnName = "id")
    @ManyToOne
    private PessoaFisica fkPessoaFisicaId;

    public Motorista() {
    }

    public Motorista(Integer id) {
        this.id = id;
    }

    public String getCnh() {
        return cnh;
    }

    public void setCnh(String cnh) {
        this.cnh = cnh;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @XmlTransient
    public Collection<Veiculo> getVeiculoCollection() {
        return veiculoCollection;
    }

    public void setVeiculoCollection(Collection<Veiculo> veiculoCollection) {
        this.veiculoCollection = veiculoCollection;
    }

    @XmlTransient
    public Collection<Cartao> getCartaoCollection() {
        return cartaoCollection;
    }

    public void setCartaoCollection(Collection<Cartao> cartaoCollection) {
        this.cartaoCollection = cartaoCollection;
    }

    public PessoaFisica getFkPessoaFisicaId() {
        return fkPessoaFisicaId;
    }

    public void setFkPessoaFisicaId(PessoaFisica fkPessoaFisicaId) {
        this.fkPessoaFisicaId = fkPessoaFisicaId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Motorista)) {
            return false;
        }
        Motorista other = (Motorista) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ClassesEntidade.Motorista[ id=" + id + " ]";
    }
    
}
