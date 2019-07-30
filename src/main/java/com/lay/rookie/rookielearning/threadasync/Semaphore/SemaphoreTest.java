package threadasync.Semaphore;

import java.util.Random;
import java.util.concurrent.Semaphore;

public class SemaphoreTest {
	
	
//多线程锁
	public static void main(String[] args) {
		Semaphore windows = new Semaphore(5);  // 声明5个窗口（5个线程5个锁）
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        windows.acquire();  // 占用窗口
                        System.out.println(Thread.currentThread().getName() + ": 开始买票");
                        sleep(random.nextInt(1000));  // 
                        System.out.println(Thread.currentThread().getName() + ": 购票成功");
                        windows.release();  // 释放窗口
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

	}

}
