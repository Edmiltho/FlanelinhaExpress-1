/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.componentes;

import modelo.locais.Endereco;
import modelo.operadores.PessoaJuridica;
import java.io.Serializable;
import java.math.BigInteger;
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
@Table(name = "estacionamento")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Estacionamento.findAll", query = "SELECT e FROM Estacionamento e")
    , @NamedQuery(name = "Estacionamento.findByLatitude", query = "SELECT e FROM Estacionamento e WHERE e.latitude = :latitude")
    , @NamedQuery(name = "Estacionamento.findByValorhora", query = "SELECT e FROM Estacionamento e WHERE e.valorhora = :valorhora")
    , @NamedQuery(name = "Estacionamento.findByNome", query = "SELECT e FROM Estacionamento e WHERE e.nome = :nome")
    , @NamedQuery(name = "Estacionamento.findById", query = "SELECT e FROM Estacionamento e WHERE e.id = :id")
    , @NamedQuery(name = "Estacionamento.findByLongitude", query = "SELECT e FROM Estacionamento e WHERE e.longitude = :longitude")})
public class Estacionamento implements Serializable {

    private static final long serialVersionUID = 1L;
    @Size(max = 15)
    @Column(name = "latitude")
    private String latitude;
    @Column(name = "valorhora")
    private BigInteger valorhora;
    @Size(max = 100)
    @Column(name = "nome")
    private String nome;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @Size(max = 15)
    @Column(name = "longitude")
    private String longitude;
    @JoinColumn(name = "fk_endereco_id", referencedColumnName = "id")
    @ManyToOne
    private Endereco fkEnderecoId;
    @JoinColumn(name = "fk_pessoa_juridica_id", referencedColumnName = "id")
    @ManyToOne
    private PessoaJuridica fkPessoaJuridicaId;
    @OneToMany(mappedBy = "fkEstacionamentoId")
    private Collection<Vaga> vagaCollection;

    public Estacionamento() {
    }

    public Estacionamento(Integer id) {
        this.id = id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public BigInteger getValorhora() {
        return valorhora;
    }

    public void setValorhora(BigInteger valorhora) {
        this.valorhora = valorhora;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Endereco getFkEnderecoId() {
        return fkEnderecoId;
    }

    public void setFkEnderecoId(Endereco fkEnderecoId) {
        this.fkEnderecoId = fkEnderecoId;
    }

    public PessoaJuridica getFkPessoaJuridicaId() {
        return fkPessoaJuridicaId;
    }

    public void setFkPessoaJuridicaId(PessoaJuridica fkPessoaJuridicaId) {
        this.fkPessoaJuridicaId = fkPessoaJuridicaId;
    }

    @XmlTransient
    public Collection<Vaga> getVagaCollection() {
        return vagaCollection;
    }

    public void setVagaCollection(Collection<Vaga> vagaCollection) {
        this.vagaCollection = vagaCollection;
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
        if (!(object instanceof Estacionamento)) {
            return false;
        }
        Estacionamento other = (Estacionamento) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ClassesEntidade.Estacionamento[ id=" + id + " ]";
    }
    
}
