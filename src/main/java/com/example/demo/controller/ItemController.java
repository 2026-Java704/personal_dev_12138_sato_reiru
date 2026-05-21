package com.example.demo.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Genre;
import com.example.demo.entity.Item;
import com.example.demo.model.Account;
import com.example.demo.model.Method;
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
			@RequestParam(defaultValue = "") Integer id,
			@RequestParam(defaultValue = "") Integer genreId,
			Model model) {
		List<Genre> genres = genreRepository.findAll();
		model.addAttribute("genres", genres);

		Integer userId = account.getId();
		Integer totalPrice = Method.calcTotalPriceMonth(itemRepository.findByUserId(userId));
		model.addAttribute("totalPrice", totalPrice);

		List<Item> items = null;
		if (genreId != null) {
			items = itemRepository.findByUserIdAndGenreId(userId, genreId);
		} else {
			items = itemRepository.findByUserId(userId);
		}
		Method.sortByDate(items);
		model.addAttribute("items", items);

		if (id != null) {
			List<Item> details = new ArrayList<Item>();
			details.add(itemRepository.findById(id).get());
			model.addAttribute("details", details);
		}

		return "items";
	}

	@GetMapping("/items/add")
	public String add(Model model) {
		model.addAttribute("genres", genreRepository.findAll());
		return "add";
	}

	@PostMapping("/items/add")
	public String store(
			@RequestParam(defaultValue = "") String name,
			@RequestParam(defaultValue = "") Integer genreId,
			@RequestParam(defaultValue = "") Integer price,
			@RequestParam(defaultValue = "") LocalDate addDate,
			@RequestParam(defaultValue = "") String comment) {
		Genre genre = genreRepository.findById(genreId).get();
		Item item = new Item(
				name,
				userRepository.findById(account.getId()).get(),
				genre,
				price,
				addDate,
				comment);
		itemRepository.save(item);
		return "redirect:/items";
	}

	@GetMapping("/items/{id}/edit")
	public String edit(
			@PathVariable Integer id,
			Model model) {
		model.addAttribute("item", itemRepository.findById(id).get());
		model.addAttribute("genres", genreRepository.findAll());
		return "edit";
	}

	@PostMapping("/items/{id}/edit")
	public String update(
			@PathVariable Integer id,
			@RequestParam(defaultValue = "") String name,
			@RequestParam(defaultValue = "") Integer genreId,
			@RequestParam(defaultValue = "") Integer price,
			@RequestParam(defaultValue = "") LocalDate addDate,
			@RequestParam(defaultValue = "") String comment) {
		Item item = itemRepository.findById(id).get();
		Genre genre = genreRepository.findById(genreId).get();
		item.update(
				name,
				userRepository.findById(account.getId()).get(),
				genre,
				price,
				addDate,
				comment);
		itemRepository.save(item);
		return "redirect:/items";
	}

	@PostMapping("/items/{id}/delete")
	public String delete(@PathVariable Integer id) {
		itemRepository.deleteById(id);
		return "redirect:/items";
	}

	@GetMapping("/items/{id}/detail")
	public String detail(
			@PathVariable Integer id,
			Model model) {
		model.addAttribute("item", itemRepository.findById(id).get());
		return "detail";
	}

	@GetMapping("/account/detail")
	public String showAccount(Model model) {
		model.addAttribute("user", userRepository.findById(account.getId()).get());

		List<Item> allItems = itemRepository.findByUserId(account.getId());
		List<String> monthes = new ArrayList<String>();
		List<Integer> prices = new ArrayList<Integer>();
		for (Item item : allItems) {
			String monthKey = item.getAddDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
			if (monthes.contains(monthKey)) {
				int index = monthes.indexOf(monthKey);
				if (item.getGenre().getIsIncome()) {
					prices.set(index, prices.get(index) + item.getPrice());
				} else {
					prices.set(index, prices.get(index) - item.getPrice());
				}
			} else {
				monthes.add(monthKey);
				prices.add(item.getPrice());
			}
		}

		for (String key : monthes) {
			int index = monthes.indexOf(key);
			System.out.println("month:" + key);
			System.out.println("price:" + prices.get(index));
		}
		model.addAttribute("monthes", monthes);
		model.addAttribute("prices", prices);

		return "accountDetail";
	}
}
