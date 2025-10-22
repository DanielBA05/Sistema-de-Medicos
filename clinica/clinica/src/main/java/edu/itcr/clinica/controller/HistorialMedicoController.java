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

    public static class RecetaDTO {
        public String medicamento;
        public String dosis;
        public String frecuencia;
        public Integer duracion;
    }

    // DTO de registro de atención
    public static class RegistrarAtencionRequest {
        public Long citaId;
        public String diagnostico;
        public String tratamiento;
        public List<RecetaDTO> recetas;
        public LocalDate fechaConsulta;
    }

    @GetMapping("/vista")
    public String vistaPacientes(@RequestParam(value = "q", required = false) String q, Model model) {
        // Búsqueda simple por nombre/apellido o listado ordenado
        List<Paciente> pacientes = (q == null || q.isBlank())
                ? pacienteRepo.findAllByOrderByApellidoAscNombreAsc()
                : pacienteRepo.searchByNombreOrApellido(q.trim());

        model.addAttribute("pacientes", pacientes);
        model.addAttribute("q", q);

        return "listaPacientes";
    }

    @GetMapping("/{idPaciente}")
    @Transactional(readOnly = true)
    public String vistaHistorialPorPaciente(@PathVariable Long idPaciente, Model model) {
        Paciente paciente = pacienteRepo.findById(idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));

        // Historiales ordenados descendente por fecha de consulta
        List<HistorialMedico> historiales =
                historialRepo.findByPaciente_IdPacienteOrderByFechaConsultaDesc(idPaciente);

        model.addAttribute("paciente", paciente);
        model.addAttribute("historiales", historiales);
        return "detalleHistorial";
    }

    @GetMapping
    @ResponseBody
    public List<HistorialMedico> listar() {
        // Endpoint simple para obtener todos los historiales
        return historialRepo.findAll();
    }

    @GetMapping("/por-paciente/{idPaciente}")
    @ResponseBody
    public List<HistorialMedico> listarPorPaciente(@PathVariable Long idPaciente) {
        // Filtra por paciente manteniendo el orden por fecha
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
        // Validación de campos obligatorios
        if (req.citaId == null || isBlank(req.diagnostico) || isBlank(req.tratamiento)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "citaId, diagnostico y tratamiento son obligatorios.");
        }

        // Verifica existencia de la cita
        Cita cita = citaRepo.findById(req.citaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada: " + req.citaId));

        if (cita.getEstado() != Cita.CitaEstado.PROGRAMADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cita no está en estado PROGRAMADA.");
        }
        if (historialRepo.existsByCita_IdCita(cita.getIdCita())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cita ya tiene historial.");
        }

        // Construcción del historial a partir de la cita y datos provistos
        HistorialMedico h = new HistorialMedico();
        h.setCita(cita);
        h.setPaciente(cita.getPaciente());
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
        // Inserta recetas válidas (ignora entradas vacías) vinculadas al historial
        List<Receta> creadas = new ArrayList<>();
        if (req.recetas != null) {
            for (RecetaDTO r : req.recetas) {
                if (r == null || isBlank(r.medicamento) || isBlank(r.dosis)) continue;
                Receta rec = new Receta();
                rec.setHistorial(h);
                rec.setMedicamento(r.medicamento.trim());
                rec.setDosis(r.dosis.trim());
                rec.setFrecuencia(isBlank(r.frecuencia) ? null : r.frecuencia.trim());
                rec.setDuracion(r.duracion);
                creadas.add(recetaRepo.save(rec));
            }
        }
        // Al registrar la atención, se marca la cita como ATENDIDA
        cita.setEstado(Cita.CitaEstado.ATENDIDA);
        citaRepo.save(cita);

        // Devuelve el historial con las recetas creadas
        h.setRecetas(creadas);
        return h;
    }
    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
