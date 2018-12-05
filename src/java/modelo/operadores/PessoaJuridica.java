/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.operadores;

import modelo.componentes.Estacionamento;
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
@Table(name = "pessoa_juridica")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PessoaJuridica.findAll", query = "SELECT p FROM PessoaJuridica p")
    , @NamedQuery(name = "PessoaJuridica.findByCnpj", query = "SELECT p FROM PessoaJuridica p WHERE p.cnpj = :cnpj")
    , @NamedQuery(name = "PessoaJuridica.findById", query = "SELECT p FROM PessoaJuridica p WHERE p.id = :id")})
public class PessoaJuridica implements Serializable {

    private static final long serialVersionUID = 1L;
    @Size(max = 18)
    @Column(name = "cnpj")
    private String cnpj;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @JoinColumn(name = "fk_pessoa_id", referencedColumnName = "id")
    @ManyToOne
    private Pessoa fkPessoaId;
    @OneToMany(mappedBy = "fkPessoaJuridicaId")
    private Collection<Estacionamento> estacionamentoCollection;

    public PessoaJuridica() {
    }

    public PessoaJuridica(Integer id) {
        this.id = id;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Pessoa getFkPessoaId() {
        return fkPessoaId;
    }

    public void setFkPessoaId(Pessoa fkPessoaId) {
        this.fkPessoaId = fkPessoaId;
    }

    @XmlTransient
    public Collection<Estacionamento> getEstacionamentoCollection() {
        return estacionamentoCollection;
    }

    public void setEstacionamentoCollection(Collection<Estacionamento> estacionamentoCollection) {
        this.estacionamentoCollection = estacionamentoCollection;
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
        if (!(object instanceof PessoaJuridica)) {
            return false;
        }
        PessoaJuridica other = (PessoaJuridica) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ClassesEntidade.PessoaJuridica[ id=" + id + " ]";
    }
    
}
