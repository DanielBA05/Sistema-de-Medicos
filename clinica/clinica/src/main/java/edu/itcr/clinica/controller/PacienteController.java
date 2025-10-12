package edu.itcr.clinica.controller;

import edu.itcr.clinica.model.HistorialMedico;
import edu.itcr.clinica.model.Paciente;
import edu.itcr.clinica.repository.HistorialMedicoRepository;
import edu.itcr.clinica.repository.PacienteRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteRepository pacienteRepository;
    private final HistorialMedicoRepository historialRepository;

    public PacienteController(PacienteRepository pacienteRepository,
                              HistorialMedicoRepository historialRepository) {
        this.pacienteRepository = pacienteRepository;
        this.historialRepository = historialRepository;
    }

    // ===================== VISTA (Thymeleaf) =====================

    @GetMapping("/vista")
    public String vista(
            @RequestParam(value = "edit", required = false) Long editId,
            @RequestParam(value = "id", required = false) Long searchId,
            Model model
    ) {
        if (searchId != null) {
            pacienteRepository.findById(searchId)
                    .ifPresentOrElse(
                            p -> model.addAttribute("pacientes", List.of(p)),
                            () -> {
                                model.addAttribute("pacientes", List.of());
                                model.addAttribute("mensaje", "No se encontró el paciente con ID " + searchId);
                            }
                    );
            // Para prellenar el input del buscador si usas th:value="${param.id}" o "searchId"
            model.addAttribute("searchId", searchId);
        } else {
            model.addAttribute("pacientes", pacienteRepository.findAll());
        }

        if (editId != null) {
            pacienteRepository.findById(editId)
                    .ifPresent(p -> model.addAttribute("pacienteEdit", p));
        }

        return "pacientes"; // -> src/main/resources/templates/pacientes.html
    }

    // Crear desde vista (form-url-encoded)
    @PostMapping("/vista/crear")
    public String crearDesdeVista(@ModelAttribute Paciente paciente) {
        Paciente guardado = pacienteRepository.save(paciente);

        // Crear historial vacío automáticamente (igual que en POST JSON)
        HistorialMedico historial = new HistorialMedico();
        historial.setPaciente(guardado);
        historial.setCita(null);
        historial.setFechaConsulta(null);
        historial.setDiagnostico(null);
        historial.setTratamiento(null);
        historialRepository.save(historial);

        return "redirect:/pacientes/vista";
    }

    // Editar desde vista
    @PostMapping("/vista/{id}/editar")
    public String editarDesdeVista(@PathVariable Long id, @ModelAttribute Paciente cambios) {
        pacienteRepository.findById(id).ifPresent(p -> {
            p.setNombre(cambios.getNombre());
            p.setApellido(cambios.getApellido());
            p.setFechaNacimiento(cambios.getFechaNacimiento());
            p.setSexo(cambios.getSexo());
            p.setDireccion(cambios.getDireccion());
            p.setTelefono(cambios.getTelefono());
            p.setCorreo(cambios.getCorreo());
            pacienteRepository.save(p);
        });
        return "redirect:/pacientes/vista";
    }

    // Eliminar desde vista
    @PostMapping("/vista/{id}/eliminar")
    public String eliminarDesdeVista(@PathVariable Long id) {
        pacienteRepository.deleteById(id);
        return "redirect:/pacientes/vista";
    }

    // ===================== REST (JSON) =====================

    @GetMapping(produces = "application/json")
    @ResponseBody
    public List<Paciente> listarPacientes() {
        return pacienteRepository.findAll();
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseBody
    public Paciente crearPaciente(@RequestBody Paciente paciente) {
        Paciente guardado = pacienteRepository.save(paciente);

        // Crear historial vacío automáticamente
        HistorialMedico historial = new HistorialMedico();
        historial.setPaciente(guardado);
        historial.setCita(null);
        historial.setFechaConsulta(null);
        historial.setDiagnostico(null);
        historial.setTratamiento(null);
        historialRepository.save(historial);

        return guardado;
    }

    @PutMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public Paciente editarPaciente(@PathVariable Long id, @RequestBody Paciente cambios) {
        return pacienteRepository.findById(id).map(p -> {
            p.setNombre(cambios.getNombre());
            p.setApellido(cambios.getApellido());
            p.setFechaNacimiento(cambios.getFechaNacimiento());
            p.setSexo(cambios.getSexo());
            p.setDireccion(cambios.getDireccion());
            p.setTelefono(cambios.getTelefono());
            p.setCorreo(cambios.getCorreo());
            return pacienteRepository.save(p);
        }).orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public void eliminarPaciente(@PathVariable Long id) {
        pacienteRepository.deleteById(id);
        // Si tu FK tiene ON DELETE CASCADE, historiales se eliminan automáticamente.
    }
}
