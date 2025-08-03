package hyunul.schedly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor @Slf4j
@CrossOrigin(origins = "*")
public class MainController {
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
