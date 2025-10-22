package edu.itcr.clinica.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VistaController {

    @GetMapping("/citas/vista")
    public String citas() {
        // Retorna la plantilla de gestión de citas
        return "citas";
    }

    @GetMapping("/historial/vista")
    public String historial() {
        // Retorna la vista principal del historial médico
        return "historial";
    }
}
