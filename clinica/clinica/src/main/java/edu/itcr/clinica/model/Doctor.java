package edu.itcr.clinica.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "doctor", schema = "clinica")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_doctor")
    private Long idDoctor;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(length = 50)
    private String telefono;

    @Column(length = 150)
    private String direccion;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "doc_especialidad",
            schema = "clinica",
            joinColumns = @JoinColumn(name = "id_doctor"),
            inverseJoinColumns = @JoinColumn(name = "id_especialidad")
    )
    @JsonIgnore
    private Set<Especialidad> especialidades = new HashSet<>();


    // ====== Constructores ======
    public Doctor() {}

    public Doctor(String nombre, String apellido) {
        this.nombre = nombre;
        this.apellido = apellido;
    }

    // ====== Getters & Setters ======
    public Long getIdDoctor() { return idDoctor; }
    public void setIdDoctor(Long idDoctor) { this.idDoctor = idDoctor; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Set<Especialidad> getEspecialidades() { return especialidades; }
    public void setEspecialidades(Set<Especialidad> especialidades) {
        this.especialidades = especialidades != null ? especialidades : new HashSet<>();
    }

    // ====== Helpers convenientes ======
    public void addEspecialidad(Especialidad e) {
        if (e == null) return;
        this.especialidades.add(e);
        e.getDoctores().add(this);
    }

    public void removeEspecialidad(Especialidad e) {
        if (e == null) return;
        this.especialidades.remove(e);
        e.getDoctores().remove(this);
    }

    // ====== equals & hashCode (por id) ======
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Doctor)) return false;
        Doctor doctor = (Doctor) o;
        return Objects.equals(idDoctor, doctor.idDoctor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDoctor);
    }

    // ====== toString ======
    @Override
    public String toString() {
        return "Doctor{" +
                "idDoctor=" + idDoctor +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                '}';
    }
}
