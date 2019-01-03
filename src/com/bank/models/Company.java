package com.bank.models;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.bank.utils.ConsoleLogger;
import com.bank.utils.TransactionType;

public class Company extends Customer {
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.00");

	private String companyName;
	private String owner;
	private ConsoleLogger consoleLogger;

	public Company(Integer id, Account account, String companyName, String owner) {
		super(id, account);

		this.companyName = companyName;
		this.owner = owner;
		consoleLogger = new ConsoleLogger();
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	@Override
	public void makeTransaction(Customer customer, BigDecimal amount, TransactionType type) {
		new Thread() {

			@Override
			public void run() {
				Account customerAccount = customer.getAccount();

				consoleLogger.log(String.format("Attempting to acquire account with id %d", account.getId()));
				synchronized (account) {
					consoleLogger.log(String.format("Account with id %d started in transaction of type %s for %.2f",
							account.getId(), type.toString(), amount));
					if (type.equals(TransactionType.CREDIT)) {
						account.setAmount(account.getAmount().subtract(amount));

						try {
							consoleLogger.log("Sleeping for 10 seconds.");
							Thread.sleep(10000);
						} catch (InterruptedException e) {
						}

						consoleLogger.log(String.format("Attempting to acquire account with id %d",
								customer.getAccount().getId()));

						synchronized (customerAccount) {
							customerAccount.setAmount(customerAccount.getAmount().add(amount));
						}
					} else {
						account.setAmount(account.getAmount().add(amount));

						try {
							consoleLogger.log("Sleeping for 10 seconds.");
							Thread.sleep(10000);
						} catch (InterruptedException e) {
						}

						consoleLogger.log(String.format("Attempting to acquire account with id %d",
								customer.getAccount().getId()));

						synchronized (customerAccount) {
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

				consoleLogger.log(String.format("Attempting to acquire account with id %d", priorityAccount.getId()));
				synchronized (priorityAccount) {
					consoleLogger.log(String.format("Account with id %d started in transaction of type %s for %.2f",
							priorityAccount.getId(), type.toString(), amount));

					if (type.equals(TransactionType.CREDIT)) {

						priorityAccount.setAmount(priorityAccount.getAmount().subtract(amount));

						consoleLogger
								.log(String.format("Attempting to acquire account with id %d", secondAccount.getId()));

						try {
							consoleLogger.log("Sleeping for 10 seconds.");
							Thread.sleep(10000);
						} catch (InterruptedException e) {
						}

						synchronized (secondAccount) {
							secondAccount.setAmount(secondAccount.getAmount().add(amount));
						}
					} else {
						priorityAccount.setAmount(priorityAccount.getAmount().add(amount));

						consoleLogger.log(String.format("Attempting to acquire account with id %d",
								customer.getAccount().getId()));

						try {
							consoleLogger.log("Sleeping for 10 seconds.");
							Thread.sleep(10000);
						} catch (InterruptedException e) {
						}

						synchronized (secondAccount) {
							secondAccount.setAmount(secondAccount.getAmount().subtract(amount));
						}
					}
					consoleLogger.log(String.format(
							"Account with id %d finished a transaction of type %s with accound with id %d",
							priorityAccount.getId(), type.toString(), secondAccount.getId()));
				}
			}
		}.start();
	}

	@Override
	public void makeTransactionViaReentrantLock(Customer customer, BigDecimal amount, TransactionType type) {
		new Thread() {

			@Override
			public void run() {
				try {
					Account customerAccount = customer.getAccount();
					consoleLogger.log(String.format("Attempting to acquire account with id %d", account.getId()));

					if (lock().tryLock()) {
						try {
							if (customer.lock().tryLock()) {
								consoleLogger.log(String.format("Account with id %d started in transaction of type %s",
										account.getId(), type.toString()));
								if (type.equals(TransactionType.CREDIT)) {
									account.setAmount(account.getAmount().subtract(amount));

									try {
										consoleLogger.log("Sleeping for 10 seconds.");
										Thread.sleep(10000);
									} catch (InterruptedException e) {
									}

									consoleLogger.log(String.format("Attempting to acquire account with id %d",
											customer.getAccount().getId()));

									customerAccount.setAmount(customerAccount.getAmount().add(amount));
								} else {
									account.setAmount(account.getAmount().add(amount));

									try {
										consoleLogger.log("Sleeping for 10 seconds.");
										Thread.sleep(10000);
									} catch (InterruptedException e) {
									}

									consoleLogger.log(String.format("Attempting to acquire account with id %d",
											customer.getAccount().getId()));

									customerAccount.setAmount(customerAccount.getAmount().subtract(amount));
								}

								consoleLogger.log(String.format(
										"Account with id %d finished a transaction of type %s with accound with id %d",
										account.getId(), type.toString(), customerAccount.getId()));
							}
						} catch(Exception e) {
							consoleLogger.log(String.format("ERROR: Could not acquire account with Id %d", customerAccount.getId()));
						} finally {
							customer.lock().unlock();
						}
					}
				} finally {
					lock().unlock();
				}
			}
		}.start();
	}
}
