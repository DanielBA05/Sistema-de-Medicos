package edu.itcr.clinica.repository;

import edu.itcr.clinica.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    // Lista ordenada por apellido y nombre
    List<Paciente> findAllByOrderByApellidoAscNombreAsc();


    @Query("""
        SELECT p FROM Paciente p
        WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :q, '%'))
        ORDER BY p.apellido ASC, p.nombre ASC
    """)
    List<Paciente> searchByNombreOrApellido(@Param("q") String q);
}
