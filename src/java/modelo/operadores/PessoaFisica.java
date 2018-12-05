/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.operadores;

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
@Table(name = "pessoa_fisica")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PessoaFisica.findAll", query = "SELECT p FROM PessoaFisica p")
    , @NamedQuery(name = "PessoaFisica.findByCpf", query = "SELECT p FROM PessoaFisica p WHERE p.cpf = :cpf")
    , @NamedQuery(name = "PessoaFisica.findById", query = "SELECT p FROM PessoaFisica p WHERE p.id = :id")})
public class PessoaFisica implements Serializable {

    private static final long serialVersionUID = 1L;
    @Size(max = 14)
    @Column(name = "cpf")
    private String cpf;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @JoinColumn(name = "fk_pessoa_id", referencedColumnName = "id")
    @ManyToOne
    private Pessoa fkPessoaId;
    @OneToMany(mappedBy = "fkPessoaFisicaId")
    private Collection<Motorista> motoristaCollection;

    public PessoaFisica() {
    }

    public PessoaFisica(Integer id) {
        this.id = id;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
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
    public Collection<Motorista> getMotoristaCollection() {
        return motoristaCollection;
    }

    public void setMotoristaCollection(Collection<Motorista> motoristaCollection) {
        this.motoristaCollection = motoristaCollection;
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
        if (!(object instanceof PessoaFisica)) {
            return false;
        }
        PessoaFisica other = (PessoaFisica) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ClassesEntidade.PessoaFisica[ id=" + id + " ]";
    }
    
}
