package edu.itcr.clinica.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "hibernateLazyInitializer" }) // 👈

@Entity
@Table(name = "especialidad", schema = "clinica")
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especialidad")
    private Long idEspecialidad;

    @Column(name = "nom_especialidad", nullable = false, length = 100, unique = true)
    private String nomEspecialidad;

    @ManyToMany(mappedBy = "especialidades", fetch = FetchType.LAZY)
    @JsonIgnore // evita ciclos al serializar
    private Set<Doctor> doctores = new HashSet<>();

    public Especialidad() {}
    public Especialidad(String nomEspecialidad) {
        this.nomEspecialidad = nomEspecialidad;
    }

    public Long getIdEspecialidad() { return idEspecialidad; }
    public void setIdEspecialidad(Long idEspecialidad) { this.idEspecialidad = idEspecialidad; }

    public String getNomEspecialidad() { return nomEspecialidad; }
    public void setNomEspecialidad(String nomEspecialidad) { this.nomEspecialidad = nomEspecialidad; }

    public Set<Doctor> getDoctores() { return doctores; }
    public void setDoctores(Set<Doctor> doctores) {
        this.doctores = doctores != null ? doctores : new HashSet<>();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Especialidad)) return false;
        Especialidad that = (Especialidad) o;
        return Objects.equals(idEspecialidad, that.idEspecialidad);
    }
    @Override public int hashCode() { return Objects.hash(idEspecialidad); }

    @Override
    public String toString() {
        return "Especialidad{" +
                "idEspecialidad=" + idEspecialidad +
                ", nomEspecialidad='" + nomEspecialidad + '\'' +
                '}';
    }
}
