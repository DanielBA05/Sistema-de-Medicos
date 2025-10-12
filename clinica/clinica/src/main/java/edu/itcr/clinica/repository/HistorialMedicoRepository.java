package edu.itcr.clinica.repository;

import edu.itcr.clinica.model.HistorialMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistorialMedicoRepository extends JpaRepository<HistorialMedico, Long> {

    // ¿ya existe historial para esta cita?
    boolean existsByCita_IdCita(Long idCita);

    // traer historial de una cita específica
    Optional<HistorialMedico> findByCita_IdCita(Long idCita);

    // listar historiales de un paciente, más recientes primero
    List<HistorialMedico> findByPaciente_IdPacienteOrderByFechaConsultaDesc(Long idPaciente);
}
