package com.bank.models;

import java.math.BigDecimal;

public class Account {
	private Integer id;
	private BigDecimal amount;

	public Account(Integer id, BigDecimal amount) {
		super();
		this.id = id;
		this.amount = amount;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
}
