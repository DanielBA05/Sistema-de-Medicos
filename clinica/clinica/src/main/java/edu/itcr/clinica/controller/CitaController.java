package edu.itcr.clinica.controller;

import edu.itcr.clinica.model.Cita;
import edu.itcr.clinica.repository.CitaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/citas")
public class CitaController {

    private final CitaRepository repo;

    public CitaController(CitaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Cita> listar() {
        return repo.findAll();
    }

    @PostMapping
    public Cita crear(@RequestBody Cita c) {
        return repo.save(c);
    }
}
