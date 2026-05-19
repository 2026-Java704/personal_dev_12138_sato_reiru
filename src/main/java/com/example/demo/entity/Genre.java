package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity(name = "genres")
public class Genre {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "genre_name")
	private String genreName;

	@Column(name = "is_income")
	private boolean isIncome;

	public Genre() {
	}

	public Genre(String name, boolean isIncome) {
		this.genreName = name;
		this.isIncome = isIncome;
	}

	public String getGenreName() {
		return genreName;
	}

	public boolean getIsIncome() {
		return isIncome;
	}
}
