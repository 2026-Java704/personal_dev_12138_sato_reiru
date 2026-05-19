package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Item;
import com.example.demo.model.Account;
import com.example.demo.repository.GenreRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.UserRepository;

@Controller
public class ItemController {
	private final Account account;
	private final ItemRepository itemRepository;
	private final UserRepository userRepository;
	private final GenreRepository genreRepository;

	public ItemController(Account account,
			ItemRepository itemRepository,
			UserRepository userRepository,
			GenreRepository genreRepository) {
		this.account = account;
		this.itemRepository = itemRepository;
		this.userRepository = userRepository;
		this.genreRepository = genreRepository;
	}

	@GetMapping("/items")
	public String index(
			@RequestParam(defaultValue = "") Integer genreId,
			Model model) {
		Integer userId = account.getId();
		List<Item> items = null;
		if (genreId != null) {
			items = itemRepository.findByUserIdAndGenreId(userId, genreId);
		} else {
			items = itemRepository.findByUserId(userId);
		}
		model.addAttribute("items", items);
		return "items";
	}

	@GetMapping("/items/add")
	public String add() {
		return "add";
	}
}
