package edu.itcr.clinica.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cita", schema = "clinica")
public class Cita {

    // Enum que define los posibles estados de una cita
    public enum CitaEstado {
        PROGRAMADA,
        CANCELADA,
        ATENDIDA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Long idCita; // Identificador único de la cita

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora; // Fecha y hora de la cita

    @Column(length = 255)
    private String motivo; // Motivo o descripción breve de la cita

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente; // Paciente asociado a la cita

    @ManyToOne
    @JoinColumn(name = "id_doctor", nullable = false)
    @JsonIgnoreProperties({ "especialidades", "hibernateLazyInitializer" })
    private Doctor doctor; // Doctor asignado a la cita

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_especialidad")
    @JsonIgnoreProperties({ "doctores", "hibernateLazyInitializer" })
    private Especialidad especialidad; // Especialidad médica de la cita

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private CitaEstado estado = CitaEstado.PROGRAMADA; // Estado inicial de la cita

    // Métodos getters y setters para acceder y modificar los atributos
    public Long getIdCita() { return idCita; }
    public void setIdCita(Long idCita) { this.idCita = idCita; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public CitaEstado getEstado() { return estado; }
    public void setEstado(CitaEstado estado) { this.estado = estado; }

    public Especialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }
}
