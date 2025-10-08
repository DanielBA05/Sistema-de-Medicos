// edu.itcr.clinica.controller.DashboardController
package edu.itcr.clinica.controller;

import edu.itcr.clinica.model.Doctor;
import edu.itcr.clinica.repository.DoctorRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DoctorRepository doctorRepo;

    public DashboardController(DoctorRepository doctorRepo) {
        this.doctorRepo = doctorRepo;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Carga un doctor para mostrar el nombre en el header si quieres
        Doctor doctor = doctorRepo.findAll().stream().findFirst().orElse(null);
        model.addAttribute("doctor", doctor);
        return "dashboard"; // tu panel principal (el HTML que ya tienes)
    }


}
