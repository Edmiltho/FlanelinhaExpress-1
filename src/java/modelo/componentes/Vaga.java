/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.componentes;

import modelo.operacoes.Reserva;
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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Cacherow
 */
@Entity
@Table(name = "vaga")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Vaga.findAll", query = "SELECT v FROM Vaga v")
    , @NamedQuery(name = "Vaga.findByNumero", query = "SELECT v FROM Vaga v WHERE v.numero = :numero")
    , @NamedQuery(name = "Vaga.findById", query = "SELECT v FROM Vaga v WHERE v.id = :id")})
public class Vaga implements Serializable {

    private static final long serialVersionUID = 1L;
    @Column(name = "numero")
    private Integer numero;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @JoinColumn(name = "fk_estacionamento_id", referencedColumnName = "id")
    @ManyToOne
    private Estacionamento fkEstacionamentoId;
    @OneToMany(mappedBy = "fkVagaId")
    private Collection<Reserva> reservaCollection;

    public Vaga() {
    }

    public Vaga(Integer id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Estacionamento getFkEstacionamentoId() {
        return fkEstacionamentoId;
    }

    public void setFkEstacionamentoId(Estacionamento fkEstacionamentoId) {
        this.fkEstacionamentoId = fkEstacionamentoId;
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
        if (!(object instanceof Vaga)) {
            return false;
        }
        Vaga other = (Vaga) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ClassesEntidade.Vaga[ id=" + id + " ]";
    }
    
}
