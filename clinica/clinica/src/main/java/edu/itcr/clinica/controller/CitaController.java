package edu.itcr.clinica.controller;

import edu.itcr.clinica.model.Cita;
import edu.itcr.clinica.model.Doctor;
import edu.itcr.clinica.model.Especialidad;
import edu.itcr.clinica.model.Paciente;
import edu.itcr.clinica.repository.CitaRepository;
import edu.itcr.clinica.repository.EspecialidadRepository;
import edu.itcr.clinica.repository.PacienteRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/citas")
@CrossOrigin
public class CitaController {

    private final CitaRepository repo;
    private final PacienteRepository pacienteRepo;
    private final EspecialidadRepository especialidadRepo;

    public CitaController(CitaRepository repo,
                          PacienteRepository pacienteRepo,
                          EspecialidadRepository especialidadRepo) {
        this.repo = repo;
        this.pacienteRepo = pacienteRepo;
        this.especialidadRepo = especialidadRepo;
    }

    public static class CrearCitaRequest {
        public String fecha;
        public String hora;
        public String motivo;
        public Long pacienteId;
        public Long doctorId;
        public Long especialidadId;
    }

    public static class ReprogramarCitaRequest {
        public String fecha;
        public String hora;
    }

    @GetMapping("/dia")
    public List<Cita> listarPorDia(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        LocalDateTime start = fecha.atStartOfDay();
        LocalDateTime end = fecha.atTime(LocalTime.MAX);
        return repo.findByDoctor_IdDoctorAndFechaHoraBetweenOrderByFechaHoraAsc(doctorId, start, end);
    }

    @PostMapping
    public org.springframework.http.ResponseEntity<?> crear(@RequestBody CrearCitaRequest req) {
        if (req.doctorId == null || req.pacienteId == null || req.fecha == null || req.hora == null || req.especialidadId == null) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(Map.of("ok", false, "mensaje", "doctorId, pacienteId, fecha, hora y especialidadId son obligatorios"));
        }

        LocalDateTime fechaHora = LocalDateTime.of(
                LocalDate.parse(req.fecha), LocalTime.parse(req.hora));

        List<Cita> citasEnEseHorario = repo.findByDoctor_IdDoctorAndFechaHora(req.doctorId, fechaHora);

        boolean atendida = citasEnEseHorario.stream()
                .anyMatch(c -> c.getEstado() == Cita.CitaEstado.ATENDIDA);
        boolean programada = citasEnEseHorario.stream()
                .anyMatch(c -> c.getEstado() == Cita.CitaEstado.PROGRAMADA);

        if (atendida) {
            return org.springframework.http.ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "ok", false,
                            "codigo", "HORARIO_ATENDIDO",
                            "mensaje", "Ya se atendió una cita a esta hora. Por favor elija otra hora."
                    ));
        }

        if (programada) {
            return org.springframework.http.ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "ok", false,
                            "codigo", "HORARIO_OCUPADO",
                            "mensaje", "Ese horario ya está ocupado por otra cita activa. Si la que ves está CANCELADA, sí puedes usar ese horario; si no, elige otra hora."
                    ));
        }

        Paciente paciente = pacienteRepo.findById(req.pacienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paciente no existe: " + req.pacienteId));

        Doctor doctor = new Doctor();
        doctor.setIdDoctor(req.doctorId);

        Especialidad especialidad = especialidadRepo.findById(req.especialidadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Especialidad no existe: " + req.especialidadId));

        Cita c = new Cita();
        c.setFechaHora(fechaHora);
        c.setMotivo(req.motivo);
        c.setPaciente(paciente);
        c.setDoctor(doctor);
        c.setEspecialidad(especialidad);
        c.setEstado(Cita.CitaEstado.PROGRAMADA);

        Cita guardada = repo.save(c);
        return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }


    @PatchMapping("/{id}/cancelar")
    public Cita cancelar(@PathVariable Long id) {
        Cita c = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
        c.setEstado(Cita.CitaEstado.CANCELADA);
        return repo.save(c);
    }

    @PatchMapping("/{id}/atender")
    public Cita atender(@PathVariable Long id) {
        Cita c = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
        c.setEstado(Cita.CitaEstado.ATENDIDA);
        return repo.save(c);
    }

    @PatchMapping("/{id}/reprogramar")
    public Cita reprogramar(@PathVariable Long id, @RequestBody ReprogramarCitaRequest req) {
        if (req.fecha == null || req.hora == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fecha y hora son obligatorios");
        }
        Cita c = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        LocalDateTime nuevaFechaHora = LocalDateTime.of(LocalDate.parse(req.fecha), LocalTime.parse(req.hora));

        boolean ocupado = repo.existsByDoctor_IdDoctorAndEstadoNotAndFechaHoraAndIdCitaNot(
                c.getDoctor().getIdDoctor(), Cita.CitaEstado.CANCELADA, nuevaFechaHora, c.getIdCita()
        );
        if (ocupado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede reprogramar: ya existe una cita activa para este médico a " + nuevaFechaHora + "."
            );
        }

        c.setFechaHora(nuevaFechaHora);
        return repo.save(c);
    }

    @GetMapping("/disponible")
    public Map<String, Object> validarDisponibilidad(
            @RequestParam Long doctorId,
            @RequestParam String fecha,
            @RequestParam String hora
    ) {
        LocalDateTime fechaHora = LocalDateTime.of(LocalDate.parse(fecha), LocalTime.parse(hora));

        boolean ocupado = repo.existsByDoctor_IdDoctorAndEstadoNotAndFechaHora(
                doctorId, Cita.CitaEstado.CANCELADA, fechaHora
        );

        Map<String, Object> resp = new HashMap<>();
        resp.put("doctorId", doctorId);
        resp.put("fechaHora", fechaHora.toString());
        resp.put("disponible", !ocupado);
        resp.put("mensaje", ocupado
                ? "Ya existe una cita activa a esa hora."
                : "Horario disponible.");
        return resp;
    }
}
