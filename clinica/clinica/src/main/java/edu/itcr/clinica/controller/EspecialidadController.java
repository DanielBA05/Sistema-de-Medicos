package edu.itcr.clinica.controller;

import edu.itcr.clinica.model.Especialidad;
import edu.itcr.clinica.repository.EspecialidadRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/especialidades")
public class EspecialidadController {

    private final EspecialidadRepository repo;

    public EspecialidadController(EspecialidadRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String listar(Model model) {
        List<Especialidad> especialidades = repo.findAll();
        model.addAttribute("especialidades", especialidades); // ahora Model.addAttribute existe
        return "especialidades"; // templates/especialidades.html
    }

    @PostMapping
    public String crear(@RequestParam("nombre") String nombre) {
        String clean = nombre == null ? "" : nombre.trim();
        if (!clean.isEmpty()) {
            repo.save(new Especialidad(clean));
        }
        return "redirect:/especialidades";
    }
}
