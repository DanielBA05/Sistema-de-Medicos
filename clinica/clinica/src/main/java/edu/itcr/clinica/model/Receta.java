package edu.itcr.clinica.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "receta", schema = "clinica")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receta")
    private Long idReceta;

    @Column(nullable = false, length = 200)
    private String medicamento;

    @Column(nullable = false, length = 100)
    private String dosis;

    @Column(length = 100)               // opcional
    private String frecuencia;

    @Column(name = "duracion")          // opcional (p. ej. en días)
    private Integer duracion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_historial", nullable = false)
    @JsonIgnoreProperties({ "cita", "recetas", "hibernateLazyInitializer" })
    private HistorialMedico historial;

    // ===== Getters/Setters =====
    public Long getIdReceta() { return idReceta; }
    public void setIdReceta(Long idReceta) { this.idReceta = idReceta; }

    public String getMedicamento() { return medicamento; }
    public void setMedicamento(String medicamento) { this.medicamento = medicamento; }

    public String getDosis() { return dosis; }
    public void setDosis(String dosis) { this.dosis = dosis; }

    public String getFrecuencia() { return frecuencia; }
    public void setFrecuencia(String frecuencia) { this.frecuencia = frecuencia; }

    public Integer getDuracion() { return duracion; }
    public void setDuracion(Integer duracion) { this.duracion = duracion; }

    public HistorialMedico getHistorial() { return historial; }
    public void setHistorial(HistorialMedico historial) { this.historial = historial; }

    // ===== equals/hashCode por id =====
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Receta)) return false;
        Receta receta = (Receta) o;
        return Objects.equals(idReceta, receta.idReceta);
    }
    @Override public int hashCode() { return Objects.hash(idReceta); }

    @Override
    public String toString() {
        return "Receta{" +
                "idReceta=" + idReceta +
                ", medicamento='" + medicamento + '\'' +
                ", dosis='" + dosis + '\'' +
                ", frecuencia='" + frecuencia + '\'' +
                ", duracion=" + duracion +
                '}';
    }
}
