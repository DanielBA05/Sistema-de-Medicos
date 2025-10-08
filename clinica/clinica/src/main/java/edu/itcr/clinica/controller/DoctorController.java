package edu.itcr.clinica.controller;

import edu.itcr.clinica.model.Doctor;
import edu.itcr.clinica.model.Especialidad;
import edu.itcr.clinica.repository.DoctorRepository;
import edu.itcr.clinica.repository.EspecialidadRepository;
import org.springframework.stereotype.Controller;
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

    // NUNCA static
    @GetMapping
    public String root() {
        Long id = doctorRepo.findAll().stream()
                .map(Doctor::getIdDoctor)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No hay doctores"));
        return "redirect:/doctor/" + id;
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model) {
        Doctor d = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
        model.addAttribute("doctor", d);
        return "doctor"; // templates/doctor.html
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Doctor d = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
        model.addAttribute("doctor", d);
        return "doctor_form"; // templates/doctor_form.html
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("doctor") Doctor form) {
        Doctor d = doctorRepo.findById(form.getIdDoctor())
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
        d.setNombre(form.getNombre());
        d.setApellido(form.getApellido());
        d.setTelefono(form.getTelefono());
        d.setDireccion(form.getDireccion());
        doctorRepo.save(d); // <-- instancia, no estático
        return "redirect:/doctor/" + d.getIdDoctor();
    }

    // ---------- Gestionar especialidades ----------
    @GetMapping("/especialidades/{id}")
    public String editarEspecialidades(@PathVariable Long id, Model model) {
        Doctor d = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
        List<Especialidad> todas = espRepo.findAll(); // <-- instancia

        Set<Long> actuales = d.getEspecialidades().stream()
                .map(Especialidad::getIdEspecialidad)
                .collect(Collectors.toSet());

        model.addAttribute("doctor", d);
        model.addAttribute("todasEspecialidades", todas);
        model.addAttribute("idsActuales", actuales);
        return "doctor_especialidades_form"; // asegúrate del archivo
    }

    @PostMapping("/especialidades/{id}")
    public String guardarEspecialidades(@PathVariable Long id,
                                        @RequestParam(name = "especialidadesIds", required = false) List<Long> especialidadesIds,
                                        RedirectAttributes ra) {
        Doctor d = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor no encontrado"));

        Set<Especialidad> nuevas = new HashSet<>();
        if (!CollectionUtils.isEmpty(especialidadesIds)) {
            nuevas.addAll(espRepo.findAllById(especialidadesIds)); // <-- instancia
        }
        d.setEspecialidades(nuevas);
        doctorRepo.save(d); // <-- instancia

        ra.addFlashAttribute("ok", "Especialidades actualizadas correctamente.");
        return "redirect:/doctor/" + id;
    }

    // opcional: crear especialidad “al vuelo”
    @PostMapping("/especialidades/{id}/crear")
    public String crearEspecialidad(@PathVariable Long id,
                                    @RequestParam("nombre") String nombre,
                                    RedirectAttributes ra) {
        String clean = nombre == null ? "" : nombre.trim();
        if (clean.isEmpty()) {
            ra.addFlashAttribute("errorEsp", "El nombre no puede estar vacío.");
            return "redirect:/doctor/especialidades/" + id;
        }

        // Optional<Especialidad> findByNomEspecialidadIgnoreCase(String nomEspecialidad);
        espRepo.findByNomEspecialidadIgnoreCase(clean)
                .orElseGet(() -> espRepo.save(new Especialidad(clean)));

        ra.addFlashAttribute("okEsp", "Especialidad creada / disponible.");
        return "redirect:/doctor/especialidades/" + id;
    }
}
