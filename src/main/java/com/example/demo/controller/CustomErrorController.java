package com.example.demo.controller;

import java.util.Objects;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CustomErrorController implements ErrorController {
	@GetMapping("/error")
	@PostMapping("/error")
	public String handleError(HttpServletRequest request, Model model) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

		if (Objects.nonNull(status)) {
			int statusCode = Integer.parseInt(status.toString());
			if (statusCode == HttpStatus.NOT_FOUND.value()) {
				return "errors";
			}
		}
		return "error";
	}
}
