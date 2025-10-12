package edu.itcr.clinica.repository;

import edu.itcr.clinica.model.HistorialMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistorialMedicoRepository extends JpaRepository<HistorialMedico, Long> {

    
    boolean existsByCita_IdCita(Long idCita);

    
    Optional<HistorialMedico> findByCita_IdCita(Long idCita);

    
    List<HistorialMedico> findByPaciente_IdPacienteOrderByFechaConsultaDesc(Long idPaciente);
}
