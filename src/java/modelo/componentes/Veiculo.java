/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.componentes;

import ClassesEntidade.Modelo;
import modelo.operadores.Motorista;
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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Cacherow
 */
@Entity
@Table(name = "veiculo")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Veiculo.findAll", query = "SELECT v FROM Veiculo v")
    , @NamedQuery(name = "Veiculo.findByAno", query = "SELECT v FROM Veiculo v WHERE v.ano = :ano")
    , @NamedQuery(name = "Veiculo.findByPlaca", query = "SELECT v FROM Veiculo v WHERE v.placa = :placa")
    , @NamedQuery(name = "Veiculo.findById", query = "SELECT v FROM Veiculo v WHERE v.id = :id")})
public class Veiculo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Column(name = "ano")
    private Integer ano;
    @Size(max = 8)
    @Column(name = "placa")
    private String placa;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @JoinColumn(name = "fk_modelo_id", referencedColumnName = "id")
    @ManyToOne
    private Modelo fkModeloId;
    @JoinColumn(name = "fk_motorista_id", referencedColumnName = "id")
    @ManyToOne
    private Motorista fkMotoristaId;
    @OneToMany(mappedBy = "fkVeiculoId")
    private Collection<Reserva> reservaCollection;

    public Veiculo() {
    }

    public Veiculo(Integer id) {
        this.id = id;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Modelo getFkModeloId() {
        return fkModeloId;
    }

    public void setFkModeloId(Modelo fkModeloId) {
        this.fkModeloId = fkModeloId;
    }

    public Motorista getFkMotoristaId() {
        return fkMotoristaId;
    }

    public void setFkMotoristaId(Motorista fkMotoristaId) {
        this.fkMotoristaId = fkMotoristaId;
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
        if (!(object instanceof Veiculo)) {
            return false;
        }
        Veiculo other = (Veiculo) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ClassesEntidade.Veiculo[ id=" + id + " ]";
    }
    
}
