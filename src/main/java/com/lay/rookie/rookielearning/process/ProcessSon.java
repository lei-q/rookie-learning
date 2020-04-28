package com.lay.rookie.rookielearning.process;

import java.util.concurrent.CompletableFuture;

/**
 * 子进程
 */
public class ProcessSon {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("success!");
        int i = 0;
        while (true) {
            System.out.println(i);

            int finalI = i;
            CompletableFuture.runAsync(() -> {
                int j = 0;
                while (true) {
                    System.out.println("inner son" + finalI + " : " + j);
                    j++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            i++;
            Thread.sleep(1000);
        }
    }
}
