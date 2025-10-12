package edu.itcr.clinica.repository;

import edu.itcr.clinica.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByDoctor_IdDoctorAndFechaHora(Long idDoctor, LocalDateTime fechaHora);

    List<Cita> findByDoctor_IdDoctorAndFechaHoraBetweenOrderByFechaHoraAsc(
            Long idDoctor, LocalDateTime start, LocalDateTime end
    );

    List<Cita> findByDoctor_IdDoctorAndEstadoNotAndFechaHoraBetweenOrderByFechaHoraAsc(
            Long idDoctor, Cita.CitaEstado estadoExcluido, LocalDateTime start, LocalDateTime end
    );

    List<Cita> findByPaciente_IdPacienteOrderByFechaHoraDesc(Long idPaciente);


    boolean existsByDoctor_IdDoctorAndEstadoNotAndFechaHora(
            Long idDoctor, Cita.CitaEstado estadoExcluido, LocalDateTime fechaHora);

    boolean existsByDoctor_IdDoctorAndEstadoNotAndFechaHoraAndIdCitaNot(
            Long idDoctor, Cita.CitaEstado estadoExcluido, LocalDateTime fechaHora, Long idCita);
}
