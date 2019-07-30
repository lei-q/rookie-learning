package threadasync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import javassist.compiler.ast.Symbol;

public class ThreadasyncTest {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		int sec = 999;

//		long currentTimeMillis = System.currentTimeMillis();
//		List<Integer> integerList = new ArrayList<>();
//		List<String> stringList = Collections.synchronizedList(new ArrayList<>());
//		for (int i = 0; i < sec; i++) {
//			integerList.add(new Random().nextInt(sec));
//		}
//		System.out.println("integerList.size(): " + integerList.size());
//		integerList.parallelStream().forEach(i -> stringList.add(i.toString()));
//		System.out.println("stringList.size(): " + stringList.size());
//		System.out.println("1:"+(System.currentTimeMillis() - currentTimeMillis));
//		
		
		
		
//		long currentTimeMillis2 = System.currentTimeMillis();
//		CompletableFuture.supplyAsync(() -> {
//			List<Integer> integerList2 = Collections.synchronizedList(new ArrayList<>());
//			for (int i = 0; i < sec; i++) {
//				integerList2.add(new Random().nextInt(sec));
//			}
//			System.out.println("integerList2.size(): " + integerList2.size());
//			return integerList2;
//		}).thenApply(integerList2 -> {
//			List<String> stringList2 = Collections.synchronizedList(new ArrayList<>());
//			integerList2.parallelStream().forEach(i -> stringList2.add(i.toString()));
//			System.out.println("stringList2.size(): " + stringList2.size());
//			return stringList2;
//		}).join();
//		System.out.println("2:"+(System.currentTimeMillis() - currentTimeMillis2));
		
		
		
		long currentTimeMillis3 = System.currentTimeMillis();
		List<Integer> integerList3 = Collections.synchronizedList(new ArrayList<>());
		CompletableFuture[] array = IntStream.rangeClosed(0, sec).mapToObj(i->{
			return CompletableFuture.supplyAsync(() -> {
				integerList3.add(new Random().nextInt(sec));
				return integerList3;
			},Executors.newFixedThreadPool(2));
		}).toArray(size->new CompletableFuture[size]);
		
		CompletableFuture.allOf(array).join();
		
		
		
		
//		CompletableFuture[] array = new CompletableFuture[sec];
//		for(int i = 0; i<sec; i++) {
//			CompletableFuture<List<Integer>> supplyAsync = CompletableFuture.supplyAsync(() -> {
//				integerList3.add(new Random().nextInt(sec));
//				return integerList3;
//			});
//			array[i] = supplyAsync;
//		}
//		CompletableFuture.allOf(array).join();
//		Thread.sleep(10000);
		System.out.println("integerList3.size(): " + integerList3.size());
		List<String> stringList3 = Collections.synchronizedList(new ArrayList<>());
		integerList3.parallelStream().forEach(System.out::println);
//		System.out.println("stringList3.size(): " + stringList3.size());
		System.out.println("3:"+(System.currentTimeMillis() - currentTimeMillis3));
		
		
	}
}
