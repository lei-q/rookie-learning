package com.lay.rookie.rookielearning.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 父进程
 */
public class ProcessParent {

    public static void main(String[] args) throws IOException, InterruptedException {

//        Process process = exec(ProcessSon.class, Collections.singletonList("-Xmx200m"), Collections.singletonList("argument"));
        Process process = exec(ProcessSon.class, null, null);

        Thread.sleep(10000);

        // 销毁子进程
        process.destroy();
    }

    public static Process exec(Class clazz, List<String> jvmArgs, List<String> args) throws IOException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = clazz.getName();
        List<String> command = new ArrayList<>();
        command.add(javaBin);
//        command.addAll(jvmArgs);
        command.add("-cp");
        command.add(classpath);
        command.add(className);
//        command.addAll(args);
        ProcessBuilder builder = new ProcessBuilder(command);

        return builder.inheritIO().start();
    }
}
