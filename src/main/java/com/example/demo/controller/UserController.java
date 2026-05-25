package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.User;
import com.example.demo.model.Account;
import com.example.demo.repository.UserRepository;

@Controller
public class UserController {
	private final Account account;
	private final HttpSession session;
	private final UserRepository userRepository;

	public UserController(
			HttpSession session,
			Account account,
			UserRepository repository) {
		this.session = session;
		this.account = account;
		this.userRepository = repository;
	}

	@GetMapping("/")
	public String initialize() {
		session.invalidate();
		return "redirect:/login";
	}

	@GetMapping("/register")
	public String register(Model model) {
		return "register";
	}

	@PostMapping("/register")
	public String add(
			@RequestParam(defaultValue = "") String name,
			@RequestParam(defaultValue = "") String email,
			@RequestParam(defaultValue = "") String password,
			@RequestParam(defaultValue = "") String passwordConfirm,
			Model model) {
		model.addAttribute("name", name);
		model.addAttribute("email", email);
		model.addAttribute("password", password);
		model.addAttribute("passwordConfirm", passwordConfirm);
		List<String> errors = new ArrayList<String>();
		if (name.isEmpty()) {
			errors.add("名前は必須です");
		}
		if (email.isEmpty()) {
			errors.add("メールアドレスは必須です");
		} else if (!userRepository.findByEmail(email).isEmpty()) {
			errors.add("登録されたメールアドレスです");
		}
		if (password.isEmpty()) {
			errors.add("パスワードは必須です");
		} else if (!password.equals(passwordConfirm)) {
			errors.add("パスワードが一致していません");
		}
		if (!errors.isEmpty()) {
			model.addAttribute("errors", errors);
			return "register";
		}
		User user = new User(name, email, password);
		userRepository.save(user);
		model.addAttribute("message", "登録が完了しました");
		return "redirect:/login";
	}

	@GetMapping("/login")
	public String index(Model model) {
		model.addAttribute("message", "");
		return "login";
	}

	@PostMapping("/login")
	public String login(
			@RequestParam(defaultValue = "") String email,
			@RequestParam(defaultValue = "") String password,
			Model model) {
		User user = userRepository.findByEmailAndPassword(email, password);
		if (Objects.isNull(user)) {
			model.addAttribute("message", "メールアドレスとパスワードが一致しませんでした。");
			return "/login";
		}
		account.setId(user.getId());
		account.setName(user.getUserName());
		return "redirect:/items";
	}
}
