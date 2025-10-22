package edu.itcr.clinica.repository;

import edu.itcr.clinica.model.HistorialMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistorialMedicoRepository extends JpaRepository<HistorialMedico, Long> {

    // Verifica si un historial ya existe para una cita específica
    boolean existsByCita_IdCita(Long idCita);

    // Obtener historial asociado a una cita
    Optional<HistorialMedico> findByCita_IdCita(Long idCita);

    // Obtener todos los historiales de un paciente ordenados por fecha descendente
    List<HistorialMedico> findByPaciente_IdPacienteOrderByFechaConsultaDesc(Long idPaciente);
}
