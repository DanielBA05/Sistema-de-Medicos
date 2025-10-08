package edu.itcr.clinica.service;

import edu.itcr.clinica.model.Paciente;
import edu.itcr.clinica.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {
    private final PacienteRepository repo;

    public PacienteService(PacienteRepository repo){ this.repo = repo; }

    public List<Paciente> listar(){ return repo.findAll(); }
    public Paciente guardar(Paciente p){ return repo.save(p); }
    public Optional<Paciente> porId(Long id){ return repo.findById(id); }
    public void borrar(Long id){ repo.deleteById(id); }
}
