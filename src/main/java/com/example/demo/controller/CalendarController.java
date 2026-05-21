package com.example.demo.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Item;
import com.example.demo.model.Account;
import com.example.demo.repository.ItemRepository;

@Controller
public class CalendarController {
	private final Account account;
	private final ItemRepository itemRepository;

	public CalendarController(
			Account account,
			ItemRepository itemRepository) {
		this.account = account;
		this.itemRepository = itemRepository;
	}

	@GetMapping("/calendar")
	public String calcLatestMonth() {
		LocalDate today = LocalDate.now();
		return "redirect:/calendar/" + today.getYear() + "/" + today.getMonthValue();
	}

	@GetMapping("/calendar/{year}/{month}")
	public String calendar(
			@PathVariable Integer year,
			@PathVariable Integer month,
			@RequestParam(defaultValue = "") Integer day,
			Model model) {
		model.addAttribute("year", year);
		model.addAttribute("month", month);

		List<Item> items = itemRepository.findByUserId(account.getId());
		model.addAttribute("items", items);

		if (day != null) {
			LocalDate date = LocalDate.of(year, month, day);
			List<Item> details = new ArrayList<Item>();
			for (Item item : items) {
				System.out.println(item.getAddDate());
				if (date.equals(item.getAddDate())) {
					details.add(item);
				}
			}
			model.addAttribute("details", details);
		}
		return "calendar";
	}

	@GetMapping("/calendar/this_month")
	public String showThisMonth() {
		LocalDate today = LocalDate.now();
		return "redirect:/calendar/" + today.getYear() + "/" + today.getMonthValue();
	}

	@PostMapping("/calendar/prev")
	public String showPrevMonth(
			@RequestParam Integer year,
			@RequestParam Integer month) {
		LocalDate date = LocalDate.of(year, month, 1).minusMonths(1);
		return "redirect:/calendar/" + date.getYear() + "/" + date.getMonthValue();
	}

	@PostMapping("/calendar/next")
	public String showNextMonth(
			@RequestParam Integer year,
			@RequestParam Integer month) {
		LocalDate date = LocalDate.of(year, month, 1).plusMonths(1);
		return "redirect:/calendar/" + date.getYear() + "/" + date.getMonthValue();
	}

	@PostMapping("/calendar/select")
	public String showSelectedMonth(
			@RequestParam String yearMonth) {
		Integer year = Integer.parseInt(yearMonth.split("-")[0]);
		Integer month = Integer.parseInt(yearMonth.split("-")[1]);
		return "redirect:/calendar/" + year + "/" + month;
	}
}
