package edu.itcr.clinica.repository;

import edu.itcr.clinica.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Obtiene citas de un doctor en una fecha/hora específica
    List<Cita> findByDoctor_IdDoctorAndFechaHora(Long idDoctor, LocalDateTime fechaHora);

    // Obtiene citas de un doctor dentro de un rango de fechas, ordenadas ascendente
    List<Cita> findByDoctor_IdDoctorAndFechaHoraBetweenOrderByFechaHoraAsc(
            Long idDoctor, LocalDateTime start, LocalDateTime end
    );

    // Obtiene citas de un doctor que no tengan un estado específico, en un rango de fechas
    List<Cita> findByDoctor_IdDoctorAndEstadoNotAndFechaHoraBetweenOrderByFechaHoraAsc(
            Long idDoctor, Cita.CitaEstado estadoExcluido, LocalDateTime start, LocalDateTime end
    );

    // Obtiene historial de citas de un paciente, ordenadas descendente por fecha
    List<Cita> findByPaciente_IdPacienteOrderByFechaHoraDesc(Long idPaciente);

    // Verifica si existe una cita de un doctor en una fecha/hora, excluyendo un estado
    boolean existsByDoctor_IdDoctorAndEstadoNotAndFechaHora(
            Long idDoctor, Cita.CitaEstado estadoExcluido, LocalDateTime fechaHora);

    // Verifica existencia de citas similares excluyendo un id específico (útil en actualizaciones)
    boolean existsByDoctor_IdDoctorAndEstadoNotAndFechaHoraAndIdCitaNot(
            Long idDoctor, Cita.CitaEstado estadoExcluido, LocalDateTime fechaHora, Long idCita);
}
