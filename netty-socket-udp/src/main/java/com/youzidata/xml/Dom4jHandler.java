package com.youzidata.xml;

import com.youzidata.udpnettysocket.dto.ASMGCSDto;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xml dom 处理
 */
public class Dom4jHandler {

    /**
     * 根据目标对象创建xml dom
     *
     * @param asmgcsDto 目标对象
     * @return
     * @throws IntrospectionException
     */
    public static Document createDocument(ASMGCSDto asmgcsDto) throws IntrospectionException, InvocationTargetException, IllegalAccessException {

        Document document = DocumentHelper.createDocument();

        Element rootElement = document.addElement(asmgcsDto.getRoot());

        document.setRootElement(rootElement);

        objectToXml(rootElement, asmgcsDto);

        return document;
    }

    /**
     * 对象转xml dom
     *
     * @param parentElement 父元素
     * @param target        目标对象
     * @throws IntrospectionException
     */
    public static void objectToXml(Element parentElement, Object target) throws IntrospectionException, InvocationTargetException, IllegalAccessException {

        BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());
        List<PropertyDescriptor> descriptors = Arrays.stream(beanInfo.getPropertyDescriptors())
                .filter(p -> {
                    //过滤掉不需要修改的属性
                    String name = p.getName();
                    return !"class".equals(name) && !"id".equals(name) && !"root".equals(name);
                }).collect(Collectors.toList());

        for (PropertyDescriptor descriptor : descriptors) {

            Class<?> propertyType = descriptor.getPropertyType();

            //descriptor.getWriteMethod()方法对应set方法
            Method readMethod = descriptor.getReadMethod();
            Object o = readMethod.invoke(target);
            if (o == null) {
                continue;
            }

            // 复杂类型 的处理
            if (!isSimpleType(propertyType)) {
                // 集合处理
                if (propertyType.isAssignableFrom(List.class)) {
                    List list = (List) o;
                    for (Object li : list) {
                        // xml节点名称大写
                        Element element = parentElement.addElement(descriptor.getName().toUpperCase());
                        objectToXml(element, li);
                    }
                    continue;
                }
                Element element = parentElement.addElement(descriptor.getName().toUpperCase());
                objectToXml(element, o);
                continue;
            }

            // 简单类型直接赋值
            Element element = parentElement.addElement(descriptor.getName().toUpperCase());
            element.setText(o + "");
        }

    }

    /**
     * xml dom 转字符串
     *
     * @param document xml dom
     * @return
     * @throws IOException
     */
    public static String xmlToString(Document document) throws IOException {

        // 自定义xml样式
        OutputFormat format = new OutputFormat();
        format.setEncoding("GB2312");
        format.setIndentSize(2);  // 行缩进
        format.setNewlines(true); // 一个结点为一行
        format.setTrimText(true); // 去重空格
        format.setPadText(true);
        format.setNewLineAfterDeclaration(false); // 放置xml文件中第二行为空白行

        StringWriter stringWriter = new StringWriter();

        XMLWriter writer = new XMLWriter(stringWriter, format);

        writer.write(document);

        return stringWriter.toString();
    }

    public static Document stringToXml(String xmlText) throws DocumentException {
        return DocumentHelper.parseText(xmlText);
    }

    /**
     * xml转对象
     *
     * @param xmlString xml字符串
     * @param clazz     class
     * @param <T>       对象类型
     * @return
     * @throws DocumentException
     * @throws IllegalAccessException
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    public static <T> T xmlToObject(String xmlString, Class<T> clazz) throws DocumentException, IllegalAccessException, IntrospectionException, InvocationTargetException, InstantiationException, ClassNotFoundException {

        Document document = DocumentHelper.parseText(xmlString);

        return xmlToObject(document, clazz);
    }

    /**
     * xml dom 转对象
     *
     * @param document xml dom
     * @param clazz    目标对象
     * @param <T>      目标对象类型
     * @return
     * @throws IllegalAccessException
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    public static <T> T xmlToObject(Document document, Class<T> clazz) throws IllegalAccessException, IntrospectionException, InvocationTargetException, InstantiationException, ClassNotFoundException {

        Element element = document.getRootElement();

        Object o = clazz.newInstance();

        return (T) xmlToObject(element, o);
    }

    /**
     * xml dom 转对象
     *
     * @param parentElement xml dom
     * @param target        目标对象
     * @return
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    private static Object xmlToObject(Element parentElement, Object target) throws IntrospectionException, InvocationTargetException, IllegalAccessException, InstantiationException, ClassNotFoundException {

        // 反射获取BeanInfo
        BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());

        // 过滤掉不需要修改的属性
        List<PropertyDescriptor> descriptors = Arrays.stream(beanInfo.getPropertyDescriptors()).filter(p -> !"class".equals(p.getName()) && !"id".equals(p.getName())).collect(Collectors.toList());

        // 获取子节点元素
        List<Element> elements = parentElement.elements();

        for (PropertyDescriptor descriptor : descriptors) {

            Class<?> propertyType = descriptor.getPropertyType();

            // Getter
            Method readMethod = descriptor.getReadMethod();
            // Setter
            Method writeMethod = descriptor.getWriteMethod();

            // 处理对象属性是List类型的情况
            List<Object> list = null;
            if (propertyType.isAssignableFrom(List.class)) {
                Object obj = readMethod.invoke(target);
                list = obj == null ? new ArrayList<>() : (List<Object>) obj;
            }

            for (Element element : elements) {
                // 根路径
                if (parentElement.isRootElement() && descriptor.getName().equalsIgnoreCase("root")) {
                    writeMethod.invoke(target, parentElement.getName());
                    break;
                }

                if (element.getName().equalsIgnoreCase(descriptor.getName())) {
                    // 复杂类型 的处理
                    if (!isSimpleType(propertyType)) {
                        // List集合处理
                        if (propertyType.isAssignableFrom(List.class)) {
                            // 获取返回值类型
                            Type returnType = readMethod.getGenericReturnType();
                            if (returnType instanceof ParameterizedType) {
                                // 获取返回值泛型类型
                                Type[] genericTypes = ((ParameterizedType) returnType).getActualTypeArguments();
                                Type genericType = genericTypes[0];

                                String typeName = genericType.getTypeName();
                                Object o = Class.forName(typeName).newInstance();
                                // 递归继续给对象赋值
                                xmlToObject(element, o);
                                if (list != null) {
                                    list.add(o);
                                }
                            }
                        } else {
                            // 对象类型处理
                            Object o = readMethod.invoke(target);
                            o = o == null ? propertyType.newInstance() : o;
                            // 递归继续给对象赋值
                            xmlToObject(element, o);
                            writeMethod.invoke(target, o);
                        }
                    } else {
                        // 简单类型 赋值
                        Object value = simpleTypeSetValue(element, propertyType);
                        writeMethod.invoke(target, value);
                    }

                    // 非List类型不需要循环赋值，所以直接跳出循环
                    if (!propertyType.isAssignableFrom(List.class)) {
                        break;
                    }
                }
            }
        }
        return target;
    }

    /**
     * 简单类型赋值
     *
     * @param element      xml dom
     * @param propertyType 对象类型
     * @return
     */
    public static Object simpleTypeSetValue(Element element, Class<?> propertyType) {
        Object value = null;
        try {
            String eleValue = element.getText();

            if (propertyType.isAssignableFrom(Integer.class) || propertyType.isAssignableFrom(int.class)) {
                value = Integer.parseInt(eleValue);
            } else if (propertyType.isAssignableFrom(Long.class) || propertyType.isAssignableFrom(long.class)) {
                value = Long.parseLong(eleValue);
            } else if (propertyType.isAssignableFrom(Double.class) || propertyType.isAssignableFrom(double.class)) {
                value = Double.parseDouble(eleValue);
            } else if (propertyType.isAssignableFrom(Float.class) || propertyType.isAssignableFrom(float.class)) {
                value = Float.parseFloat(eleValue);
            } else if (propertyType.isAssignableFrom(Boolean.class) || propertyType.isAssignableFrom(boolean.class)) {
                value = Boolean.parseBoolean(eleValue);
            } else if (propertyType.isAssignableFrom(Byte.class) || propertyType.isAssignableFrom(byte.class)) {
                value = Byte.parseByte(eleValue);
            } else if (propertyType.isAssignableFrom(Short.class) || propertyType.isAssignableFrom(short.class)) {
                value = Short.parseShort(eleValue);
            } else if (propertyType.isAssignableFrom(Character.class) || propertyType.isAssignableFrom(char.class)) {
                value = eleValue.charAt(0);
            } else {
                value = eleValue + "";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + e);
        }
        return value;
    }

    /**
     * 判断是否是简单类型
     *
     * @param type 数据类型
     * @return
     */
    public static boolean isSimpleType(Class<?> type) {

        return type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class) ||
                type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class) ||
                type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class) ||
                type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class) ||
                type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class) ||
                type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class) ||
                type.isAssignableFrom(Short.class) || type.isAssignableFrom(short.class) ||
                type.isAssignableFrom(Character.class) || type.isAssignableFrom(char.class) ||
                type.isAssignableFrom(Void.class) || type.isAssignableFrom(void.class) ||
                type.isAssignableFrom(String.class);
    }

    public static void main(String[] args) throws IntrospectionException, IOException, DocumentException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        ASMGCSDto asmgcsDto = new ASMGCSDto();

        asmgcsDto.getHead().setTitle("SUBSCRIBE");
        asmgcsDto.getBody().setTitles_subscribe("RWYLIGHT");
        asmgcsDto.getBody().setError(400);
        List<ASMGCSDto.Runway> runways = new ArrayList<>();
        runways.add(new ASMGCSDto.Runway("Lo3", "ON", "200", null, null));
        runways.add(new ASMGCSDto.Runway("Lo2", "OFF", "200", null, null));
        asmgcsDto.getBody().setRwyalert(new ASMGCSDto.Rwyalert());
        asmgcsDto.getBody().getRwyalert().setRunway(runways);

        Document document = createDocument(asmgcsDto);

        String s = xmlToString(document);

        System.out.println(s);

        ASMGCSDto asmgcsDto1 = xmlToObject(s, ASMGCSDto.class);

        System.out.println(asmgcsDto1);

    }
}
