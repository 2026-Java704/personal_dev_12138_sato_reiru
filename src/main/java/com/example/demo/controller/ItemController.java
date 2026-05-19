package com.example.demo.controller;

import java.time.LocalDate;
import java.util.Comparator;
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
		List<Genre> genres = genreRepository.findAll();
		model.addAttribute("genres", genres);

		Integer userId = account.getId();
		Integer totalPrice = calcTotalPriceMonth(itemRepository.findByUserId(userId));
		model.addAttribute("totalPrice", totalPrice);

		List<Item> items = null;
		if (genreId != null) {
			items = itemRepository.findByUserIdAndGenreId(userId, genreId);
		} else {
			items = itemRepository.findByUserId(userId);
		}
		sortByDate(items);
		model.addAttribute("items", items);
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

	@GetMapping("/items/calendar")
	public String calcLatestMonth(Model model) {
		List<Item> items = itemRepository.findByUserId(account.getId());
		sortByDate(items);
		model.addAttribute("items", items);
		return "calendar";
	}

	@GetMapping("/items/{id}/detail")
	public String detail(
			@PathVariable Integer id,
			Model model) {
		model.addAttribute("item", itemRepository.findById(id).get());
		return "detail";
	}

	// 日付順にソート
	public void sortByDate(List<Item> list) {
		list.sort(Comparator.comparing(Item::getAddDate).reversed());
	}

	// 今月の収支を計算
	public Integer calcTotalPriceMonth(List<Item> list) {
		Integer totalPrice = 0;
		Integer nowMonth = LocalDate.now().getMonthValue();
		for (Item item : list) {
			if (item.getAddDate().getMonthValue() == nowMonth) {
				if (item.getGenre().getIsIncome()) {
					totalPrice += item.getPrice();
				} else {
					totalPrice -= item.getPrice();
				}
			}
		}
		return totalPrice;
	}
}
