package edu.itcr.clinica.controller;

import edu.itcr.clinica.model.Doctor;
import edu.itcr.clinica.model.Especialidad;
import edu.itcr.clinica.repository.DoctorRepository;
import edu.itcr.clinica.repository.EspecialidadRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorRepository doctorRepo;
    private final EspecialidadRepository espRepo;

    public DoctorController(DoctorRepository doctorRepo, EspecialidadRepository espRepo) {
        this.doctorRepo = doctorRepo;
        this.espRepo = espRepo;
    }

    // Redirige al primer doctor existente
    @GetMapping
    public String root() {
        // Obtiene el primer ID disponible
        Long id = doctorRepo.findAll().stream()
                .map(Doctor::getIdDoctor)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No hay doctores registrados"));
        return "redirect:/doctor/" + id;
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model) {
        Doctor d = doctorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado: " + id));
        model.addAttribute("doctor", d);
        return "doctor";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        // Reutiliza el mismo patrón de búsqueda con error explícito
        Doctor d = doctorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado: " + id));
        model.addAttribute("doctor", d);
        return "doctor_form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("doctor") Doctor form) {
        // Evita sobreescrituras accidentales
        Doctor d = doctorRepo.findById(form.getIdDoctor())
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado: " + form.getIdDoctor()));
        d.setNombre(form.getNombre());
        d.setApellido(form.getApellido());
        d.setTelefono(form.getTelefono());
        d.setDireccion(form.getDireccion());
        doctorRepo.save(d);
        return "redirect:/doctor/" + d.getIdDoctor();
    }


    @GetMapping("/especialidades/{id}")
    public String editarEspecialidades(@PathVariable Long id, Model model) {
        // Carga del doctor y preparación de datos para checkboxes
        Doctor d = doctorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado: " + id));
        List<Especialidad> todas = espRepo.findAll();

        Set<Long> actuales = d.getEspecialidades().stream()
                .map(Especialidad::getIdEspecialidad)
                .collect(Collectors.toSet());

        model.addAttribute("doctor", d);
        model.addAttribute("todasEspecialidades", todas);
        model.addAttribute("idsActuales", actuales);
        return "doctor_especialidades_form";
    }

    @PostMapping("/especialidades/{id}")
    public String guardarEspecialidades(@PathVariable Long id,
                                        @RequestParam(name = "especialidadesIds", required = false) List<Long> especialidadesIds,
                                        RedirectAttributes ra) {
        // Sincroniza el many-to-many: reemplaza el set del doctor con la selección actual
        Doctor d = doctorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado: " + id));

        Set<Especialidad> nuevas = new HashSet<>();
        if (!CollectionUtils.isEmpty(especialidadesIds)) {
            // Trae sólo las especialidades seleccionadas
            nuevas.addAll(espRepo.findAllById(especialidadesIds));
        }
        d.setEspecialidades(nuevas);
        doctorRepo.save(d);

        ra.addFlashAttribute("ok", "Especialidades actualizadas correctamente.");
        return "redirect:/doctor/" + id;
    }

    // Crear especialidad
    @PostMapping("/especialidades/{id}/crear")
    public String crearEspecialidad(@PathVariable Long id,
                                    @RequestParam("nombre") String nombre,
                                    RedirectAttributes ra) {
        // Normaliza entrada y valida vacío para evitar registros inválidos
        String clean = nombre == null ? "" : nombre.trim();
        if (clean.isEmpty()) {
            ra.addFlashAttribute("errorEsp", "El nombre no puede estar vacío.");
            return "redirect:/doctor/especialidades/" + id;
        }

        espRepo.findByNomEspecialidadIgnoreCase(clean)
                .orElseGet(() -> espRepo.save(new Especialidad(clean)));

        ra.addFlashAttribute("okEsp", "Especialidad creada / disponible.");
        return "redirect:/doctor/especialidades/" + id;
    }

    @GetMapping(value = "/{id}/especialidades.json")
    @ResponseBody
    public List<EspecialidadDto> especialidadesJson(@PathVariable Long id) {
        var list = espRepo.findByDoctores_IdDoctor(id);
        return list.stream()
                .sorted(java.util.Comparator.comparing(
                        edu.itcr.clinica.model.Especialidad::getNomEspecialidad,
                        java.text.Collator.getInstance(new java.util.Locale("es","CR"))
                ))
                .map(e -> new EspecialidadDto(e.getIdEspecialidad(), e.getNomEspecialidad()))
                .toList();
    }

    public record EspecialidadDto(Long idEspecialidad, String nombre) {}
}
