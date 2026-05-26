package com.example.demo.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import org.springframework.web.multipart.MultipartFile;

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
			@RequestParam(defaultValue = "") Integer sortType,
			@RequestParam(defaultValue = "false") Boolean isAsc,
			Model model,
			HttpServletRequest request) {
		model.addAttribute("sortType", sortType);
		model.addAttribute("isAsc", isAsc);
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
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
		if (Objects.nonNull(sortType) && sortType == 1) {
			Method.sortByPrice(items, isAsc);
		} else {
			Method.sortByDate(items, isAsc);
		}
		model.addAttribute("items", items);

		if (id != null) {
			List<Item> details = new ArrayList<Item>();
			details.add(itemRepository.findById(id).get());
			model.addAttribute("details", details);

			Map<Integer, String> images = new HashMap<>();
			int size = 100;
			byte[] data = Method.resizeImage(details.get(0).getReceipt(), size, size);
			if (data != null) {
				String image = Base64.getEncoder().encodeToString(data);
				images.put(id, image);
				model.addAttribute("images", images);
			}
		}

		model.addAttribute("path", request.getRequestURI().toString());

		return "items";
	}

	@GetMapping("/items/add")
	public String add(Model model) {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
		model.addAttribute("genres", genreRepository.findAll());
		return "add";
	}

	@PostMapping("/items/add")
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
		return "redirect:/items";
	}

	@GetMapping("/items/{id}/edit")
	public String edit(
			@PathVariable Integer id,
			Model model) {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
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
		return "redirect:/items";
	}

	@PostMapping("/items/{id}/delete")
	public String delete(@PathVariable Integer id) {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
		itemRepository.deleteById(id);
		return "redirect:/items";
	}

	@GetMapping("/items/{id}/detail")
	public String detail(
			@PathVariable Integer id,
			Model model) {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
		model.addAttribute("item", itemRepository.findById(id).get());
		return "detail";
	}

	@GetMapping("/account/detail")
	public String showAccount(Model model) {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
		model.addAttribute("user", userRepository.findById(account.getId()).get());

		Integer sum = 0;
		List<Item> allItems = itemRepository.findByUserId(account.getId());

		Map<String, Integer> genreMap = new HashMap<String, Integer>();
		Map<String, Integer> monthMap = new HashMap<String, Integer>();
		for (Item item : allItems) {
			String date = item.getAddDate().format(DateTimeFormatter.ofPattern("yyyy年MM月"));
			String genreName = item.getGenre().getGenreName();

			Integer price = item.getPrice();
			if (!item.getGenre().getIsIncome()) {
				price *= -1;
			}
			sum += price;
			if (monthMap.containsKey(date)) {
				monthMap.put(date, monthMap.get(date) + price);
			} else {
				monthMap.put(date, price);
			}

			if (genreMap.containsKey(genreName)) {
				genreMap.put(genreName, genreMap.get(genreName) + price);
			} else {
				genreMap.put(genreName, price);
			}
		}
		model.addAttribute("monthMap", monthMap);
		model.addAttribute("genreMap", genreMap);
		model.addAttribute("wholePeriod", sum);

		return "accountDetail";
	}

	@GetMapping("/items/{id}/image/edit")
	public String imageRegister(
			@PathVariable Integer id,
			Model model) {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
		byte[] data = itemRepository.findById(id).get().getReceipt();
		if (data != null) {
			String image = Base64.getEncoder().encodeToString(data);
			model.addAttribute("image", image);
		}
		return "image";
	}

	@PostMapping("/items/{id}/image/edit")
	public String imageUpdate(
			@PathVariable Integer id,
			@RequestParam MultipartFile file) {
		if (Method.nonLogin(account)) {
			return "redirect:/";
		}
		try {
			Item item = itemRepository.findById(id).get();
			item.setReceipt(file.getBytes());
			item.setFileType(file.getContentType());
			itemRepository.save(item);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/items";
	}
}
