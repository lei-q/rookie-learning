package threadasync;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class FutureTest {

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		List<Book> books = Arrays.asList(
				new Book("java编程思想", 56.5), 
				new Book("java并发编程实战", 78.9),
				new Book("java8实战", 76.9), 
				new Book("java高并发程序设计", 69.0),
				new Book("javaEE SpringBoot实战", 69.0),
				new Book("java编程思想", 56.5), 
				new Book("java并发编程实战", 78.9),
				new Book("java8实战", 76.9), 
				new Book("java高并发程序设计", 69.0),
				new Book("javaEE SpringBoot实战", 69.0),
				new Book("java编程思想", 56.5), 
				new Book("java并发编程实战", 78.9),
				new Book("java8实战", 76.9), 
				new Book("java高并发程序设计", 69.0),
				new Book("javaEE SpringBoot实战", 69.0),
				new Book("java编程思想", 56.5), 
				new Book("java并发编程实战", 78.9),
				new Book("java8实战", 76.9), 
				new Book("java高并发程序设计", 69.0),
				new Book("javaEE SpringBoot实战", 69.0));

		// 顺序流
		long nanoTime = System.nanoTime();
		books.stream().map(book -> book.getName() + ":" + book.getPrice()).collect(Collectors.toList());
		System.out.println("耗时：" + (System.nanoTime() - nanoTime) / 1_000_000);

		// 并行流
		long nanoTime2 = System.nanoTime();
		books.parallelStream().map(book -> book.getName() + ":" + book.getPrice()).collect(Collectors.toList());
		System.out.println("耗时：" + (System.nanoTime() - nanoTime2) / 1_000_000);
		
		// 异步线程
		long nanoTime3 = System.nanoTime();
		List<CompletableFuture<String>> completableFutures = books.stream()
				.map(book -> CompletableFuture.supplyAsync(() -> book.getName() + ":" + book.getPrice()))
				.collect(Collectors.toList());
		completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList());
		System.out.println("耗时：" + (System.nanoTime() - nanoTime3) / 1_000_000);
	}
}

class Book {
	private String name;
	private double price = 0.0;

	Book() {
	}

	Book(String name, double price) {
		this.name = name;
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
}
