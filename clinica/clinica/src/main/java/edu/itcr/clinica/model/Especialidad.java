package edu.itcr.clinica.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "hibernateLazyInitializer" }) // Evita errores al serializar objetos proxy de Hibernate

@Entity
@Table(name = "especialidad", schema = "clinica")
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especialidad")
    private Long idEspecialidad; // Identificador único de la especialidad

    @Column(name = "nom_especialidad", nullable = false, length = 100, unique = true)
    private String nomEspecialidad; // Nombre de la especialidad médica

    @ManyToMany(mappedBy = "especialidades", fetch = FetchType.LAZY)
    @JsonIgnore // Evita bucles infinitos en la serialización JSON
    private Set<Doctor> doctores = new HashSet<>(); // Doctores asociados a esta especialidad

    public Especialidad() {} // Constructor vacío requerido por JPA

    public Especialidad(String nomEspecialidad) {
        this.nomEspecialidad = nomEspecialidad; // Constructor que tiene un parámetro útil para inicialización directa
    }

    // Getters y setters estándar
    public Long getIdEspecialidad() { return idEspecialidad; }
    public void setIdEspecialidad(Long idEspecialidad) { this.idEspecialidad = idEspecialidad; }

    public String getNomEspecialidad() { return nomEspecialidad; }
    public void setNomEspecialidad(String nomEspecialidad) { this.nomEspecialidad = nomEspecialidad; }

    public Set<Doctor> getDoctores() { return doctores; }
    public void setDoctores(Set<Doctor> doctores) {
        this.doctores = doctores != null ? doctores : new HashSet<>(); // Evita asignar null al conjunto
    }

    // Igualdad basada en el ID para asegurar consistencia en colecciones
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Especialidad)) return false;
        Especialidad that = (Especialidad) o;
        return Objects.equals(idEspecialidad, that.idEspecialidad);
    }

    @Override public int hashCode() { return Objects.hash(idEspecialidad); }

    // Representación legible de la entidad
    @Override
    public String toString() {
        return "Especialidad{" +
                "idEspecialidad=" + idEspecialidad +
                ", nomEspecialidad='" + nomEspecialidad + '\'' +
                '}';
    }
}
