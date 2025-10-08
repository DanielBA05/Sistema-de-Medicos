package edu.itcr.clinica.model;

import jakarta.persistence.*;

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

    @Column(length = 100)
    private String frecuencia;

    private Integer duracion;

    @ManyToOne
    @JoinColumn(name = "id_historial", nullable = false)
    private HistorialMedico historial;

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
}
