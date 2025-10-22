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

    @GetMapping("/vista")
    public String vista(
            @RequestParam(value = "edit", required = false) Long editId,
            @RequestParam(value = "id", required = false) Long searchId,
            Model model
    ) {
        // Soporta búsqueda por ID o listado completo
        if (searchId != null) {
            pacienteRepository.findById(searchId)
                    .ifPresentOrElse(
                            p -> model.addAttribute("pacientes", List.of(p)),
                            () -> {
                                // Mensaje cuando no hay coincidencias
                                model.addAttribute("pacientes", List.of());
                                model.addAttribute("mensaje", "No se encontró el paciente con ID " + searchId);
                            }
                    );

            model.addAttribute("searchId", searchId);
        } else {
            model.addAttribute("pacientes", pacienteRepository.findAll());
        }
        if (editId != null) {
            pacienteRepository.findById(editId)
                    .ifPresent(p -> model.addAttribute("pacienteEdit", p));
        }

        return "pacientes";
    }

    @PostMapping("/vista/crear")
    public String crearDesdeVista(@ModelAttribute Paciente paciente) {
        // Crea paciente desde formulario
        Paciente guardado = pacienteRepository.save(paciente);

        // Genera un historial base para asegurar relación inicial
        HistorialMedico historial = new HistorialMedico();
        historial.setPaciente(guardado);
        historial.setCita(null);
        historial.setFechaConsulta(null);
        historial.setDiagnostico(null);
        historial.setTratamiento(null);
        historialRepository.save(historial);

        return "redirect:/pacientes/vista";
    }


    @PostMapping("/vista/{id}/editar")
    public String editarDesdeVista(@PathVariable Long id, @ModelAttribute Paciente cambios) {
        // Actualiza campos permitidos si el paciente existe
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


    @PostMapping("/vista/{id}/eliminar")
    public String eliminarDesdeVista(@PathVariable Long id) {
        // Eliminación directa
        pacienteRepository.deleteById(id);
        return "redirect:/pacientes/vista";
    }



    @GetMapping(produces = "application/json")
    @ResponseBody
    public List<Paciente> listarPacientes() {
        // Devuelve todos los pacientes
        return pacienteRepository.findAll();
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseBody
    public Paciente crearPaciente(@RequestBody Paciente paciente) {
        Paciente guardado = pacienteRepository.save(paciente);

        // Crea historial vacío para consistencia de dominio desde el inicio
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

    }
}
