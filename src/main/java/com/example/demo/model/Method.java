package com.example.demo.model;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;

import com.example.demo.entity.Item;

public class Method {

	// ログインチェック
	public static boolean nonLogin(Account account) {
		return Objects.isNull(account.getId());
	}

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

	// 画像サイズを調整
	public static byte[] resizeImage(byte[] imageBytes, int maxWidth, int maxHeight) {
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
