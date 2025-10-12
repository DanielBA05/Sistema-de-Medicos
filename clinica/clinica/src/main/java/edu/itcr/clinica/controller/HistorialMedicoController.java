package edu.itcr.clinica.controller;

import edu.itcr.clinica.model.Cita;
import edu.itcr.clinica.model.HistorialMedico;
import edu.itcr.clinica.model.Paciente;
import edu.itcr.clinica.model.Receta;
import edu.itcr.clinica.repository.CitaRepository;
import edu.itcr.clinica.repository.HistorialMedicoRepository;
import edu.itcr.clinica.repository.PacienteRepository;
import edu.itcr.clinica.repository.RecetaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/historiales")
@CrossOrigin
public class HistorialMedicoController {

    private final HistorialMedicoRepository historialRepo;
    private final CitaRepository citaRepo;
    private final RecetaRepository recetaRepo;
    private final PacienteRepository pacienteRepo;

    public HistorialMedicoController(HistorialMedicoRepository historialRepo,
                                     CitaRepository citaRepo,
                                     RecetaRepository recetaRepo,
                                     PacienteRepository pacienteRepo) {
        this.historialRepo = historialRepo;
        this.citaRepo = citaRepo;
        this.recetaRepo = recetaRepo;
        this.pacienteRepo = pacienteRepo;
    }

    // ================= DTOs =================
    public static class RecetaDTO {
        public String medicamento;
        public String dosis;
        public String frecuencia; // opcional
        public Integer duracion;  // opcional (p. ej., días)
    }

    public static class RegistrarAtencionRequest {
        public Long citaId;            // requerido
        public String diagnostico;     // requerido
        public String tratamiento;     // requerido
        public List<RecetaDTO> recetas; // opcional
        public LocalDate fechaConsulta; // opcional
    }

    // ==================== VISTAS (Thymeleaf) ====================

    // Lista de pacientes para seleccionar
    @GetMapping("/vista")
    public String vistaPacientes(@RequestParam(value = "q", required = false) String q, Model model) {
        List<Paciente> pacientes = (q == null || q.isBlank())
                ? pacienteRepo.findAllByOrderByApellidoAscNombreAsc()
                : pacienteRepo.searchByNombreOrApellido(q.trim());

        model.addAttribute("pacientes", pacientes);
        model.addAttribute("q", q);
        // Sin subcarpeta: templates/listaPacientes.html
        return "listaPacientes";
    }

    // Detalle del historial del paciente (citas, recetas, etc.)
    @GetMapping("/{idPaciente}")
    @Transactional(readOnly = true)
    public String vistaHistorialPorPaciente(@PathVariable Long idPaciente, Model model) {
        Paciente paciente = pacienteRepo.findById(idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));

        List<HistorialMedico> historiales =
                historialRepo.findByPaciente_IdPacienteOrderByFechaConsultaDesc(idPaciente);

        // (Opcional) Si deseas mostrar TODAS las citas del paciente:
        // List<Cita> citas = citaRepo.findByPaciente_IdPacienteOrderByFechaHoraDesc(idPaciente);
        // model.addAttribute("citas", citas);

        model.addAttribute("paciente", paciente);
        model.addAttribute("historiales", historiales);
        // Sin subcarpeta: templates/detalleHistorial.html
        return "detalleHistorial";
    }

    // ==================== API REST (JSON) ====================

    @GetMapping
    @ResponseBody
    public List<HistorialMedico> listar() {
        return historialRepo.findAll();
    }

    @GetMapping("/por-paciente/{idPaciente}")
    @ResponseBody
    public List<HistorialMedico> listarPorPaciente(@PathVariable Long idPaciente) {
        return historialRepo.findByPaciente_IdPacienteOrderByFechaConsultaDesc(idPaciente);
    }

    @GetMapping("/por-cita/{idCita}")
    @ResponseBody
    public HistorialMedico buscarPorCita(@PathVariable Long idCita) {
        return historialRepo.findByCita_IdCita(idCita)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Historial no encontrado para la cita " + idCita));
    }

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @Transactional
    public HistorialMedico registrar(@RequestBody RegistrarAtencionRequest req) {
        if (req.citaId == null || isBlank(req.diagnostico) || isBlank(req.tratamiento)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "citaId, diagnostico y tratamiento son obligatorios.");
        }

        Cita cita = citaRepo.findById(req.citaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada: " + req.citaId));

        if (cita.getEstado() != Cita.CitaEstado.PROGRAMADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cita no está en estado PROGRAMADA.");
        }
        if (historialRepo.existsByCita_IdCita(cita.getIdCita())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cita ya tiene historial.");
        }

        // Crear historial
        HistorialMedico h = new HistorialMedico();
        h.setCita(cita);
        h.setPaciente(cita.getPaciente()); // redundante pero práctico para consultas
        h.setDiagnostico(req.diagnostico.trim());
        h.setTratamiento(req.tratamiento.trim());

        if (req.fechaConsulta != null) {
            h.setFechaConsulta(req.fechaConsulta);
        } else if (cita.getFechaHora() != null) {
            h.setFechaConsulta(cita.getFechaHora().toLocalDate());
        } else {
            h.setFechaConsulta(LocalDate.now());
        }

        h = historialRepo.save(h);

        // Crear recetas (si vienen)
        List<Receta> creadas = new ArrayList<>();
        if (req.recetas != null) {
            for (RecetaDTO r : req.recetas) {
                if (r == null || isBlank(r.medicamento) || isBlank(r.dosis)) continue;
                Receta rec = new Receta();
                rec.setHistorial(h);
                rec.setMedicamento(r.medicamento.trim());
                rec.setDosis(r.dosis.trim());
                rec.setFrecuencia(isBlank(r.frecuencia) ? null : r.frecuencia.trim());
                rec.setDuracion(r.duracion); // puede ser null
                creadas.add(recetaRepo.save(rec));
            }
        }

        // Marcar la cita como ATENDIDA
        cita.setEstado(Cita.CitaEstado.ATENDIDA);
        citaRepo.save(cita);

        // Adjuntar para serializar en la respuesta
        h.setRecetas(creadas);

        return h;
    }

    // ================= Util =================
    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
