package threadasync;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class RunAsyncExecutors {
	private final static  ExecutorService executor = Executors.newFixedThreadPool(1);

	public static AtomicInteger AtomicInteger = new AtomicInteger();
	public static void main(String[] args) {
		int i = 1;
		while(true) {
			if(i>20000000) {
				break;
			}
//			System.out.println("i:"+i);
			test();
			i++;
		}
	}
	public static void test() {
		CompletableFuture.runAsync(() -> {
			int threadCount = ((ThreadPoolExecutor)executor).getActiveCount();
			System.out.println("当前活动线程数："+threadCount);
			test1();
		}, executor);
	}
	
	public static void test1() {
		int addAndGet = AtomicInteger.addAndGet(1);
		if(addAndGet>20) {
			System.out.println("full");
			return;
		}
		System.out.println(addAndGet);
	}

}
