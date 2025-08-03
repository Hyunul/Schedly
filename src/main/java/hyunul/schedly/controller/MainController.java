package hyunul.schedly.controller;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import hyunul.schedly.dto.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MainController {

    @GetMapping("/")
    public String index(Model model, Principal principal) {
        if (principal != null) {
            CustomUserPrincipal user = (CustomUserPrincipal) ((Authentication) principal).getPrincipal();
            model.addAttribute("user", user);
        }
        return "index";
    }
}
