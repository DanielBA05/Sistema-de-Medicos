package edu.itcr.clinica.controller;

import edu.itcr.clinica.model.HistorialMedico;
import edu.itcr.clinica.repository.HistorialMedicoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/historiales")
public class HistorialMedicoController {

    private final HistorialMedicoRepository repo;

    public HistorialMedicoController(HistorialMedicoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<HistorialMedico> listar() {
        return repo.findAll();
    }

    @PostMapping
    public HistorialMedico crear(@RequestBody HistorialMedico h) {
        return repo.save(h);
    }
}
