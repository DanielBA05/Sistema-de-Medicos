package edu.itcr.clinica.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "historial_medico", schema = "clinica",
     
        uniqueConstraints = @UniqueConstraint(name = "uk_historial_id_cita", columnNames = "id_cita"))
public class HistorialMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;

    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_paciente", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer" })
    private Paciente paciente;

   
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cita", unique = true, nullable = false)
    @JsonIgnoreProperties({ "doctor", "especialidad", "paciente", "hibernateLazyInitializer" })
    private Cita cita;

    @Column(name = "fecha_consulta", nullable = false)
    private LocalDate fechaConsulta;

    @Column(name = "diagnostico", columnDefinition = "TEXT", nullable = false)
    private String diagnostico;

    @Column(name = "tratamiento", columnDefinition = "TEXT", nullable = false)
    private String tratamiento;

    
    @OneToMany(mappedBy = "historial", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({ "historial", "hibernateLazyInitializer" })
    private List<Receta> recetas = new ArrayList<>();

   
    @PrePersist
    public void prePersist() {
        if (this.cita != null) {
            if (this.fechaConsulta == null && this.cita.getFechaHora() != null) {
                this.fechaConsulta = this.cita.getFechaHora().toLocalDate();
            }
            if (this.paciente == null && this.cita.getPaciente() != null) {
                this.paciente = this.cita.getPaciente();
            }
        }
        if (this.fechaConsulta == null) {
            this.fechaConsulta = LocalDate.now();
        }
    }

    
    public void addReceta(Receta r) {
        if (r == null) return;
        this.recetas.add(r);
        r.setHistorial(this);
    }
    public void removeReceta(Receta r) {
        if (r == null) return;
        this.recetas.remove(r);
        r.setHistorial(null);
    }

    // ===== Getters/Setters =====
    public Long getIdHistorial() { return idHistorial; }
    public void setIdHistorial(Long idHistorial) { this.idHistorial = idHistorial; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }

    public LocalDate getFechaConsulta() { return fechaConsulta; }
    public void setFechaConsulta(LocalDate fechaConsulta) { this.fechaConsulta = fechaConsulta; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }

    public List<Receta> getRecetas() { return recetas; }
    public void setRecetas(List<Receta> recetas) {
        this.recetas.clear();
        if (recetas != null) {
            recetas.forEach(this::addReceta);
        }
    }

    
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HistorialMedico)) return false;
        HistorialMedico that = (HistorialMedico) o;
        return Objects.equals(idHistorial, that.idHistorial);
    }
    @Override public int hashCode() { return Objects.hash(idHistorial); }
}
