package com.esb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	// Opening http://localhost:8080/ shows the Swagger UI (API documentation + testing page)
	@GetMapping("/")
	public String home() {
		return "redirect:/swagger-ui.html";
	}
}
