package com.bank.models;

import java.math.BigDecimal;

import com.bank.utils.TransactionType;

public class Person extends Customer {
	private String fullName;

	public Person(Integer id, Account account, String fullName) {
		super(id, account);

		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@Override
	public void makeTransaction(Customer customer, BigDecimal amount, TransactionType type) {
		new Thread() {

			@Override
			public void run() {
				try {
					consoleLogger.log("Sleeping for 2 seconds.");
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				Account customerAccount = customer.getAccount();

				consoleLogger.log(String.format("Attempting to acquire account with id %d", account.getId()));
				synchronized (account) {
					consoleLogger.log(String.format("Account with id %d started in transaction of type %s  for %.2f",
							account.getId(), type.toString(), amount));
					consoleLogger.log(
							String.format("Attempting to acquire account with id %d", customer.getAccount().getId()));

					synchronized (customerAccount) {
						if (type.equals(TransactionType.CREDIT)) {
							account.setAmount(account.getAmount().subtract(amount));
							customerAccount.setAmount(customerAccount.getAmount().add(amount));
						} else {
							account.setAmount(account.getAmount().add(amount));
							customerAccount.setAmount(customerAccount.getAmount().subtract(amount));
						}
					}

					consoleLogger.log(String.format(
							"Account with id %d finished a transaction of type %s with accound with id %d",
							account.getId(), type.toString(), customerAccount.getId()));
				}
			}
		}.start();
	}

	@Override
	public void makeTransactionViaAccountLockPriority(Customer customer, BigDecimal amount, TransactionType type) {
		new Thread() {
			@Override
			public void run() {
				Account priorityAccount = account.getId() < customer.getAccount().getId() ? account
						: customer.getAccount();
				Account secondAccount = account.getId() > customer.getAccount().getId() ? account
						: customer.getAccount();

				try {
					consoleLogger.log("Sleeping for 2 seconds.");
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}

				consoleLogger.log(String.format("Attempting to acquire account with id %d", priorityAccount.getId()));

				synchronized (priorityAccount) {
					consoleLogger.log(String.format("Account with id %d started in transaction of type %s for %.2f",
							priorityAccount.getId(), type.toString(), amount));
					consoleLogger.log(String.format("Attempting to acquire account with id %d", secondAccount.getId()));

					synchronized (secondAccount) {
						if (type.equals(TransactionType.CREDIT)) {
							account.setAmount(account.getAmount().subtract(amount));
							customer.getAccount().setAmount(customer.getAccount().getAmount().add(amount));
						} else {
							account.setAmount(account.getAmount().add(amount));
							customer.getAccount().setAmount(customer.getAccount().getAmount().subtract(amount));
						}
					}
				}

				consoleLogger.log(
						String.format("Account with id %d finished a transaction of type %s with accound with id %d",
								account.getId(), type.toString(), customer.getAccount().getId()));
			}
		}.start();
	}

	@Override
	public void makeTransactionViaReentrantLock(Customer customer, BigDecimal amount, TransactionType type) {
		new Thread() {
			@Override
			public void run() {
				try {
					consoleLogger.log("Sleeping for 2 seconds.");
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				Account customerAccount = customer.getAccount();
				consoleLogger.log(String.format("Attempting to acquire account with id %d", account.getId()));

				if (lock().tryLock()) {
					try {
						consoleLogger.log(String.format("Account with id %d started in transaction of type %s",
								account.getId(), type.toString()));
						consoleLogger.log(String.format("Attempting to acquire account with id %d",
								customer.getAccount().getId()));
						if (customer.lock().tryLock()) {
							try {
								if (type.equals(TransactionType.CREDIT)) {
									account.setAmount(account.getAmount().subtract(amount));
									customerAccount.setAmount(customerAccount.getAmount().add(amount));
								} else {
									account.setAmount(account.getAmount().add(amount));
									customerAccount.setAmount(customerAccount.getAmount().subtract(amount));
								}

								consoleLogger.log(String.format(
										"Account with id %d finished a transaction of type %s with accound with id %d",
										account.getId(), type.toString(), customerAccount.getId()));
							} finally {
								customer.lock().unlock();
							}
						} else {
							consoleLogger.log("Transaction failed, try again.");
						}
					} finally {
						lock().unlock();
					}
				} else {
					consoleLogger.log("Transaction failed, try again.");
				}
			}
		}.start();
	}
}
