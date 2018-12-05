/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.operacoes;

import modelo.operadores.Motorista;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Cacherow
 */
@Entity
@Table(name = "cartao")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Cartao.findAll", query = "SELECT c FROM Cartao c")
    , @NamedQuery(name = "Cartao.findById", query = "SELECT c FROM Cartao c WHERE c.id = :id")
    , @NamedQuery(name = "Cartao.findByNumero", query = "SELECT c FROM Cartao c WHERE c.numero = :numero")
    , @NamedQuery(name = "Cartao.findByCsv", query = "SELECT c FROM Cartao c WHERE c.csv = :csv")
    , @NamedQuery(name = "Cartao.findByValidade", query = "SELECT c FROM Cartao c WHERE c.validade = :validade")})
public class Cartao implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @Size(max = 16)
    @Column(name = "numero")
    private String numero;
    @Size(max = 10)
    @Column(name = "csv")
    private String csv;
    @Column(name = "validade")
    @Temporal(TemporalType.DATE)
    private Date validade;
    @JoinColumn(name = "fk_motorista_id", referencedColumnName = "id")
    @ManyToOne
    private Motorista fkMotoristaId;
    @OneToMany(mappedBy = "fkCartaoId")
    private Collection<Pagamento> pagamentoEstacionamentoCollection;

    public Cartao() {
    }

    public Cartao(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getCsv() {
        return csv;
    }

    public void setCsv(String csv) {
        this.csv = csv;
    }

    public Date getValidade() {
        return validade;
    }

    public void setValidade(Date validade) {
        this.validade = validade;
    }

    public Motorista getFkMotoristaId() {
        return fkMotoristaId;
    }

    public void setFkMotoristaId(Motorista fkMotoristaId) {
        this.fkMotoristaId = fkMotoristaId;
    }

    @XmlTransient
    public Collection<Pagamento> getPagamentoEstacionamentoCollection() {
        return pagamentoEstacionamentoCollection;
    }

    public void setPagamentoEstacionamentoCollection(Collection<Pagamento> pagamentoEstacionamentoCollection) {
        this.pagamentoEstacionamentoCollection = pagamentoEstacionamentoCollection;
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
        if (!(object instanceof Cartao)) {
            return false;
        }
        Cartao other = (Cartao) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ClassesEntidade.Cartao[ id=" + id + " ]";
    }
    
}
