package com.example.demo.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;

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
public class CalendarController {
	private final Account account;
	private final ItemRepository itemRepository;
	private final GenreRepository genreRepository;
	private final UserRepository userRepository;

	public CalendarController(
			Account account,
			ItemRepository itemRepository,
			GenreRepository genreRepository,
			UserRepository userRepository) {
		this.account = account;
		this.itemRepository = itemRepository;
		this.genreRepository = genreRepository;
		this.userRepository = userRepository;
	}

	@GetMapping("/calendar")
	public String calcLatestMonth() {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
		LocalDate today = LocalDate.now();
		return "redirect:/calendar/" + today.getYear() + "/" + today.getMonthValue();
	}

	@GetMapping("/calendar/add")
	public String add(Model model) {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
		model.addAttribute("genres", genreRepository.findAll());
		return "add";
	}

	@PostMapping("/calendar/add")
	public String store(
			@RequestParam(defaultValue = "") String name,
			@RequestParam(defaultValue = "") Integer genreId,
			@RequestParam(defaultValue = "") Integer price,
			@RequestParam(defaultValue = "") LocalDate addDate,
			@RequestParam(defaultValue = "") String comment,
			Model model) {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
		List<String> errors = new ArrayList<String>();
		if (name.isEmpty()) {
			errors.add("件名を入力してください");
		}
		if (Objects.isNull(genreId)) {
			errors.add("ジャンルは必須です");
		}
		if (Objects.isNull(price)) {
			errors.add("金額は必須です");
		}
		if (Objects.isNull(addDate)) {
			errors.add("日付は必須です");
		}
		if (!errors.isEmpty()) {
			model.addAttribute("errors", errors);
			model.addAttribute("genres", genreRepository.findAll());
			return "add";
		}

		Genre genre = genreRepository.findById(genreId).get();
		Item item = new Item(
				name,
				userRepository.findById(account.getId()).get(),
				genre,
				price,
				addDate,
				comment);
		itemRepository.save(item);
		return "redirect:/calendar";
	}

	@GetMapping("/calendar/{year}/{month}")
	public String calendar(
			@PathVariable Integer year,
			@PathVariable Integer month,
			@RequestParam(defaultValue = "") Integer day,
			Model model,
			HttpServletRequest request) {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
		model.addAttribute("year", year);
		model.addAttribute("month", month);

		List<Item> items = itemRepository.findByUserId(account.getId());
		model.addAttribute("items", items);

		if (day != null) {
			LocalDate date = LocalDate.of(year, month, day);
			List<Item> details = new ArrayList<Item>();
			Map<Integer, String> images = new HashMap<>();
			int size = 100;
			for (Item item : items) {
				System.out.println(item.getAddDate());
				if (date.equals(item.getAddDate())) {
					details.add(item);
				}
				if (Objects.nonNull(item.getReceipt())) {
					Integer id = item.getId();
					byte[] receipt = Method.resizeImage(item.getReceipt(), size, size);
					String image = Base64.getEncoder().encodeToString(receipt);
					images.put(id, image);
				}
			}
			model.addAttribute("details", details);
			if (!images.isEmpty()) {
				model.addAttribute("images", images);
			}
		}

		model.addAttribute("path", request.getRequestURI().toString());

		return "calendar";
	}

	@GetMapping("/calendar/this_month")
	public String showThisMonth() {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
		LocalDate today = LocalDate.now();
		return "redirect:/calendar/" + today.getYear() + "/" + today.getMonthValue();
	}

	@PostMapping("/calendar/prev")
	public String showPrevMonth(
			@RequestParam Integer year,
			@RequestParam Integer month) {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
		LocalDate date = LocalDate.of(year, month, 1).minusMonths(1);
		return "redirect:/calendar/" + date.getYear() + "/" + date.getMonthValue();
	}

	@PostMapping("/calendar/next")
	public String showNextMonth(
			@RequestParam Integer year,
			@RequestParam Integer month) {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
		LocalDate date = LocalDate.of(year, month, 1).plusMonths(1);
		return "redirect:/calendar/" + date.getYear() + "/" + date.getMonthValue();
	}

	@PostMapping("/calendar/select")
	public String showSelectedMonth(
			@RequestParam String yearMonth) {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
		Integer year = Integer.parseInt(yearMonth.split("-")[0]);
		Integer month = Integer.parseInt(yearMonth.split("-")[1]);
		return "redirect:/calendar/" + year + "/" + month;
	}

	@GetMapping("/calendar/{year}/{month}/{id}/edit")
	public String edit(
			@PathVariable Integer year,
			@PathVariable Integer month,
			@PathVariable Integer id,
			Model model) {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
		model.addAttribute("item", itemRepository.findById(id).get());
		model.addAttribute("genres", genreRepository.findAll());
		return "edit";
	}

	@PostMapping("/calendar/{year}/{month}/{id}/edit")
	public String update(
			@PathVariable Integer id,
			@RequestParam(defaultValue = "") String name,
			@RequestParam(defaultValue = "") Integer genreId,
			@RequestParam(defaultValue = "") Integer price,
			@RequestParam(defaultValue = "") LocalDate addDate,
			@RequestParam(defaultValue = "") String comment) {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
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
		return "redirect:/calendar";
	}
}
