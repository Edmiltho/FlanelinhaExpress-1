/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.operacoes;

import java.io.Serializable;
import java.math.BigInteger;
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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Cacherow
 */
@Entity
@Table(name = "pagamento_estacionamento")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PagamentoEstacionamento.findAll", query = "SELECT p FROM PagamentoEstacionamento p")
    , @NamedQuery(name = "PagamentoEstacionamento.findByData", query = "SELECT p FROM PagamentoEstacionamento p WHERE p.data = :data")
    , @NamedQuery(name = "PagamentoEstacionamento.findByValor", query = "SELECT p FROM PagamentoEstacionamento p WHERE p.valor = :valor")
    , @NamedQuery(name = "PagamentoEstacionamento.findById", query = "SELECT p FROM PagamentoEstacionamento p WHERE p.id = :id")})
public class Pagamento implements Serializable {

    private static final long serialVersionUID = 1L;
    @Column(name = "data")
    @Temporal(TemporalType.DATE)
    private Date data;
    @Column(name = "valor")
    private BigInteger valor;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @JoinColumn(name = "fk_cartao_id", referencedColumnName = "id")
    @ManyToOne
    private Cartao fkCartaoId;
    @OneToMany(mappedBy = "fkPagamentoEstacionamentoId")
    private Collection<Reserva> reservaCollection;

    public Pagamento() {
    }

    public Pagamento(Integer id) {
        this.id = id;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public BigInteger getValor() {
        return valor;
    }

    public void setValor(BigInteger valor) {
        this.valor = valor;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Cartao getFkCartaoId() {
        return fkCartaoId;
    }

    public void setFkCartaoId(Cartao fkCartaoId) {
        this.fkCartaoId = fkCartaoId;
    }

    @XmlTransient
    public Collection<Reserva> getReservaCollection() {
        return reservaCollection;
    }

    public void setReservaCollection(Collection<Reserva> reservaCollection) {
        this.reservaCollection = reservaCollection;
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
        if (!(object instanceof Pagamento)) {
            return false;
        }
        Pagamento other = (Pagamento) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ClassesEntidade.PagamentoEstacionamento[ id=" + id + " ]";
    }
    
}
