package com.bank.utils;

public class ConsoleLogger {
	public void log(String msg) {
		System.out.println(Thread.currentThread().getName() + ": " + msg);
	}
}
