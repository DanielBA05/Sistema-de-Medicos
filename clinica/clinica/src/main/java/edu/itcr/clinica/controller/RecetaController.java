package edu.itcr.clinica.controller;

import edu.itcr.clinica.model.Receta;
import edu.itcr.clinica.repository.RecetaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recetas")
public class RecetaController {

    private final RecetaRepository repo;

    public RecetaController(RecetaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Receta> listar() {
        // Devuelve todas las recetas almacenadas
        return repo.findAll();
    }

    @PostMapping
    public Receta crear(@RequestBody Receta r) {
        // Inserta una nueva receta
        return repo.save(r);
    }
}
