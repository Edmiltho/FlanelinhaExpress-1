/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.operacoes;

import modelo.componentes.Veiculo;
import modelo.componentes.Vaga;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Cacherow
 */
@Entity
@Table(name = "reserva")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Reserva.findAll", query = "SELECT r FROM Reserva r")
    , @NamedQuery(name = "Reserva.findById", query = "SELECT r FROM Reserva r WHERE r.id = :id")
    , @NamedQuery(name = "Reserva.findByHorareserva", query = "SELECT r FROM Reserva r WHERE r.horareserva = :horareserva")
    , @NamedQuery(name = "Reserva.findByDatareserva", query = "SELECT r FROM Reserva r WHERE r.datareserva = :datareserva")
    , @NamedQuery(name = "Reserva.findByHorasaida", query = "SELECT r FROM Reserva r WHERE r.horasaida = :horasaida")
    , @NamedQuery(name = "Reserva.findBySaidaprevista", query = "SELECT r FROM Reserva r WHERE r.saidaprevista = :saidaprevista")})
public class Reserva implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @Column(name = "horareserva")
    @Temporal(TemporalType.TIME)
    private Date horareserva;
    @Column(name = "datareserva")
    @Temporal(TemporalType.DATE)
    private Date datareserva;
    @Column(name = "horasaida")
    @Temporal(TemporalType.TIME)
    private Date horasaida;
    @Column(name = "saidaprevista")
    @Temporal(TemporalType.TIME)
    private Date saidaprevista;
    @JoinColumn(name = "fk_pagamento_estacionamento_id", referencedColumnName = "id")
    @ManyToOne
    private Pagamento fkPagamentoEstacionamentoId;
    @JoinColumn(name = "fk_vaga_id", referencedColumnName = "id")
    @ManyToOne
    private Vaga fkVagaId;
    @JoinColumn(name = "fk_veiculo_id", referencedColumnName = "id")
    @ManyToOne
    private Veiculo fkVeiculoId;

    public Reserva() {
    }

    public Reserva(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getHorareserva() {
        return horareserva;
    }

    public void setHorareserva(Date horareserva) {
        this.horareserva = horareserva;
    }

    public Date getDatareserva() {
        return datareserva;
    }

    public void setDatareserva(Date datareserva) {
        this.datareserva = datareserva;
    }

    public Date getHorasaida() {
        return horasaida;
    }

    public void setHorasaida(Date horasaida) {
        this.horasaida = horasaida;
    }

    public Date getSaidaprevista() {
        return saidaprevista;
    }

    public void setSaidaprevista(Date saidaprevista) {
        this.saidaprevista = saidaprevista;
    }

    public Pagamento getFkPagamentoEstacionamentoId() {
        return fkPagamentoEstacionamentoId;
    }

    public void setFkPagamentoEstacionamentoId(Pagamento fkPagamentoEstacionamentoId) {
        this.fkPagamentoEstacionamentoId = fkPagamentoEstacionamentoId;
    }

    public Vaga getFkVagaId() {
        return fkVagaId;
    }

    public void setFkVagaId(Vaga fkVagaId) {
        this.fkVagaId = fkVagaId;
    }

    public Veiculo getFkVeiculoId() {
        return fkVeiculoId;
    }

    public void setFkVeiculoId(Veiculo fkVeiculoId) {
        this.fkVeiculoId = fkVeiculoId;
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
        if (!(object instanceof Reserva)) {
            return false;
        }
        Reserva other = (Reserva) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ClassesEntidade.Reserva[ id=" + id + " ]";
    }
    
}
