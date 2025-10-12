// VistaController.java
package edu.itcr.clinica.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VistaController {



    

    @GetMapping("/citas/vista")
    public String citas() { return "citas"; }

    @GetMapping("/historial/vista")
    public String historial() { return "historial"; }

    
}
