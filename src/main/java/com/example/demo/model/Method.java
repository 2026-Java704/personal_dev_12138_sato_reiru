package com.example.demo.model;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import com.example.demo.entity.Item;

public class Method {

	// 日付順にソート
	public static void sortByDate(List<Item> list) {
		list.sort(Comparator.comparing(Item::getAddDate).reversed());
	}

	// 今月の収支を計算
	public static Integer calcTotalPriceMonth(List<Item> list) {
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
