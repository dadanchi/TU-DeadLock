package com.bank.models;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

import com.bank.utils.TransactionType;

public abstract class Customer extends Thread {
	protected Integer id;
	protected Account account;

	private final ReentrantLock lock = new ReentrantLock();

	public Customer(Integer id, Account account) {
		super();
		this.id = id;
		this.account = account;
	}

	public Integer getID() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public ReentrantLock lock() {
		return lock;
	}

	public abstract void makeTransaction(Customer customer, BigDecimal amount, TransactionType type);
	
	public abstract void makeTransactionViaAccountLockPriority(Customer customer, BigDecimal amount, TransactionType type);
	
	public abstract void makeTransactionViaReentrantLock(Customer customer, BigDecimal amount, TransactionType type);
}
