package com.bank;

import java.math.BigDecimal;

import com.bank.models.Account;
import com.bank.models.Company;
import com.bank.models.Customer;
import com.bank.models.Person;
import com.bank.utils.TransactionType;

public class App {

	public static void main(String[] args) {
		Account account1 = new Account(1, new BigDecimal(5000.00));
		Account account2 = new Account(2, new BigDecimal(1200.00));
		
		Customer company = new Company(1, account1, "Tony Oil", "Georgi Ivanov");
		Customer person = new Person(2, account2, "Miroslav Tonchev");
		
		company.makeTransactionViaReentrantLock(person, new BigDecimal(523), TransactionType.CREDIT);
		person.makeTransactionViaReentrantLock(company, new BigDecimal(280.23), TransactionType.CREDIT);
	}

}
