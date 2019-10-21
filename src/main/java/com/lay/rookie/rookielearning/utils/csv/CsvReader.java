package com.lay.rookie.rookielearning.utils.csv;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * csv类型文件读取器
 */
@Component
public class CsvReader {
    private static Logger logger = Logger.getLogger(CsvReader.class.getName());
    /**
     * 按文件路径读取文件内容
     *
     * @param path 文件路径
     * @return 文件内容行集合
     * @throws IOException
     */
    public List<String> read(String path) throws IOException {
        List<String> readResult = new ArrayList<>();
        FileReader fileReader = new FileReader(path);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            readResult.add(line);
        }
        return readResult;
    }

    /**
     * 按文件路径读取文件内容并封装到实体类中
     * 实体类属性顺序和csv文件数据顺序一致
     *
     * @param path  文件路径
     * @param clazz 实体类型
     * @param <T>   泛型
     * @return 实体类集合
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public <T> List<T> read(String path, Class<T> clazz) {
        List<T> readResult = new ArrayList<>();
        try {
            Field[] declaredFields = clazz.getDeclaredFields();
            FileReader fileReader = new FileReader(path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (StringUtils.isBlank(line)) {
                    continue;//跳过空行
                }
                T instance = clazz.newInstance();
                String[] split = line.split(",");
                for (int i = 0; i < split.length; i++) {
                    Field declaredField = declaredFields[i];
                    declaredField.setAccessible(true);
                    Class<?> type = declaredField.getType();
                    String value = split[i].replace("\"", "").trim();
                    if (type == Integer.class || type.toString().equalsIgnoreCase("int")) {
                        declaredField.set(instance, Integer.valueOf(value));
                    } else if (type == Long.class || type.toString().equalsIgnoreCase("long")) {
                        declaredField.set(instance, Long.valueOf(value));
                    } else if (type == Double.class || type.toString().equalsIgnoreCase("double")) {
                        declaredField.set(instance, Double.valueOf(value));
                    } else if (type == Boolean.class || type.toString().equalsIgnoreCase("boolean")) {
                        declaredField.set(instance, Boolean.valueOf(value));
                    } else if (type == String.class) {
                        declaredField.set(instance, value);
                    } else if (type == Date.class) {
                        declaredField.set(instance, new Date(Long.valueOf(value)));
                    }
                }
                readResult.add(instance);
            }
        } catch (Exception e) {
            logger.debug(e);
            return null;
        }
        return readResult;
    }
}
