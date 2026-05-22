package com.example.demo.controller;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

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

			int size = 100;
			byte[] receipt = resizeImage(details.get(0).getReceipt(), size, size);
			if (receipt != null) {
				String image = Base64.getEncoder().encodeToString(receipt);
				model.addAttribute("image", image);
			}
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

		Map<String, Integer> genreMap = new HashMap<String, Integer>();
		Map<String, Integer> monthMap = new HashMap<String, Integer>();
		for (Item item : allItems) {
			String date = item.getAddDate().format(DateTimeFormatter.ofPattern("yyyy年MM月"));
			String genreName = item.getGenre().getGenreName();

			Integer price = item.getPrice();
			if (!item.getGenre().getIsIncome()) {
				price *= -1;
			}
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

		return "accountDetail";
	}

	@GetMapping("/items/{id}/receipt/edit")
	public String receiptRegister(
			@PathVariable Integer id,
			Model model) {
		byte[] receipt = itemRepository.findById(id).get().getReceipt();
		if (receipt != null) {
			String image = Base64.getEncoder().encodeToString(receipt);
			model.addAttribute("image", image);
		}
		return "receipt";
	}

	@PostMapping("/items/{id}/receipt/edit")
	public String receiptUpdate(
			@PathVariable Integer id,
			@RequestParam MultipartFile file) {
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

	// 画像サイズを調整
	public byte[] resizeImage(byte[] imageBytes, int maxWidth, int maxHeight) {
		if (imageBytes == null) {
			return null;
		}
		try {
			// 画像を読み込む
			BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
			int originalWidth = originalImage.getWidth();
			int originalHeight = originalImage.getHeight();

			// サイズを計算する
			double widthRatio = (double) maxWidth / originalWidth;
			double heightRatio = (double) maxHeight / originalHeight;
			double ratio = Math.min(widthRatio, heightRatio);

			int newWidth = (int) (originalWidth * ratio);
			int newHeight = (int) (originalHeight * ratio);

			// 画像をリサイズする
			Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
			BufferedImage outputImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = outputImage.createGraphics();
			g2d.drawImage(resizedImage, 0, 0, null);
			g2d.dispose();

			// リサイズされた画像をバイト配列に変換する
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(outputImage, "jpg", baos);
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
