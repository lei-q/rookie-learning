package com.lay.rookie.rookielearning.utils.porxy;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.beans.BeanMap;

/**
 * cglib为bean动态添加属性
 * 
 * <pre>
 * 使用前需要添加maven依赖
 *	<dependency>
 *		<groupId>cglib</groupId>
 *		<artifactId>cglib-nodep</artifactId>
 *		<version>3.2.4</version>
 *	</dependency>
 *
 *	<dependency>
 *		<groupId>commons-beanutils</groupId>
 *		<artifactId>commons-beanutils</artifactId>
 *		<version>1.9.3</version>
 *	</dependency>
 * </pre>
 * 
 * @author leiqiang
 *
 */
public class DynamicBean {
	static Logger logger = LoggerFactory.getLogger(DynamicBeanUtils.class);
	 
	/*** String DYNAMIC_FIELD_PREFIX 动态代理字段前缀 */
	private static final String DYNAMIC_FIELD_PREFIX = "$cglib_prop_";
	
	private static final String OBJECT_TYPE = "java.lang.Object";
	
    
    /**
     * @Describe 以代理形式为目标对象新增成员变量（属性），并返回代理后的对象
     *
     * @param <T> 目标对象类型
     * @param targer 目标对象
     * @param addProperties 新增属性<属性名,属性值>
     * @return 返回代理后的目标对象
     *
     * @Author leiqiang
     *
     * @DateTime 2019年4月24日上午9:59:15
     *
     * @Copyright (c) 上海置维信息科技有限公司-版权所有
     */
    @SuppressWarnings("unchecked")
	public static <T>T getDynamicTarget(T targer, Map<String, Object> addProperties) {
    	
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        PropertyDescriptor[] descriptors = propertyUtilsBean.getPropertyDescriptors(targer);
        
        //<属性名，属性类型>
        Map<String, Class<?>> propertyMap = Maps.newHashMap();
        
        //解析目标对象的属性，并添加进propertyMap
        for (PropertyDescriptor d : descriptors) {
            if (!"class".equalsIgnoreCase(d.getName())) {
                propertyMap.put(d.getName(), d.getPropertyType());
            }
        }
        
        //将新增属性添加进propertyMap
        for (Entry<String, Object> entry : addProperties.entrySet()) { 
        	Class<? extends Object> clazz = entry.getValue() != null ? entry.getValue().getClass() : Object.class;
        	propertyMap.put(entry.getKey(), clazz);
        }
        
        //生成动态对象
        DynamicBean dynamicBean = DynamicBean.newInstance(targer, propertyMap);
          
        //为动态对象赋值
        for (Entry<String, Class<?>> entry : propertyMap.entrySet()) { 
        	try {
              if (!addProperties.containsKey(entry.getKey())) {
                  dynamicBean.setValue(entry.getKey(), propertyUtilsBean.getNestedProperty(targer, entry.getKey()));
              }
          } catch (Exception e) {
              logger.error(e.getMessage(), e);
          }
        }
        for (Entry<String, Object> entry : addProperties.entrySet()) { 
        	try {
                dynamicBean.setValue(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        
        //返回代理后的对象
        return (T) dynamicBean.getTarget();
    }
 
    /**
     * 动态Bean
     * @author leiqiang
     *
     */
    public static class DynamicBean {
        /**
         * 目标对象
         */
        private Object target;
 
        /**
         * 属性集合
         */
        private BeanMap beanMap;
 
        public DynamicBean(Class<?> superclass, Map<String, Class<?>> propertyMap) {
            this.target = generateBean(superclass, propertyMap);
            this.beanMap = BeanMap.create(this.target);
        }
        public static DynamicBean newInstance(Object targer, Map<String, Class<?>> propertyMap) {
        	return new DynamicBean(targer.getClass(), propertyMap);
        }
 
        /**
         * bean 添加属性和值
         *
         * @param property
         * @param value
         */
        public void setValue(String property, Object value) {
            beanMap.put(property, value);
        }
 
        /**
         * 获取属性值
         *
         * @param property
         * @return
         */
        public Object getValue(String property) {
            return beanMap.get(property);
        }
 
        /**
         * 获取对象
         *
         * @return
         */
        public Object getTarget() {
            return this.target;
        }
 
        /**
         * 根据属性生成对象
         *
         * @param superclass
         * @param propertyMap
         * @return
         */
        private Object generateBean(Class<?> superclass, Map<String, Class<?>> propertyMap) {
            BeanGenerator generator = new BeanGenerator();
            if (null != superclass) {
                generator.setSuperclass(superclass);
            }
            BeanGenerator.addProperties(generator, propertyMap);
            return generator.create();
        }
    }

    /**
     * @Describe 通过参数名获取参数值
     *
     * @param paramName 参数名
     * @param obj 动态代码对象
     * @return 返回值（Object）
     * @throws Exception
     *
     * @Author leiqiang
     *
     * @DateTime 2019年4月23日下午6:28:02
     *
     * @Copyright (c) 上海置维信息科技有限公司-版权所有
     */
    public static Object getParamValue(String paramName, Object obj){
    	Class<?> cl = obj.getClass();
    	Field[] declaredFields = cl.getDeclaredFields();
    	for (Field field : declaredFields) {
    		field.setAccessible(true);
    		String name = field.getName();
    		if(StringUtils.equals(DYNAMIC_FIELD_PREFIX.concat(paramName), name)) {
    			try {
					return field.get(obj);
				}catch (IllegalArgumentException | IllegalAccessException e) {
					logger.error("动态bean获取属性值异常：{}",ExceptionUtils.getMessage(e));
					throw new RuntimeException("动态bean获取属性值异常：".concat(ExceptionUtils.getMessage(e)));
				} 
    		}
		}
    	return null;
    }
    
    /**
     * @Describe 通过参数名获取参数值（手动设置返回值类型）
     *
     * @param <T>
     * @param paramName 参数名
     * @param obj 动态代理对象
     * @param returnType 返回值类型
     * @return
     * @throws Exception
     *
     * @Author leiqiang
     *
     * @DateTime 2019年4月23日下午6:28:47
     *
     * @Copyright (c) 上海置维信息科技有限公司-版权所有
     */
    @SuppressWarnings("unchecked")
	public static <T>T getParamValue(String paramName, Object obj, Class<T> returnType){
    	Class<?> cl = obj.getClass();
    	Field[] declaredFields = cl.getDeclaredFields();
    	for (Field field : declaredFields) {
    		field.setAccessible(true);
    		String name = field.getName();
    		if(StringUtils.equals(DYNAMIC_FIELD_PREFIX.concat(paramName), name)) {
    			//判断手动设置的返回值与实际参数值类型是否一致
    			if(field.getType() != returnType && 
    					!OBJECT_TYPE.equals(field.getType().getName())) {
    				logger.error("The parameter'returnType [return value type]'is not consistent with the actual field type");
        			throw new RuntimeException("The parameter'returnType [return value type]'is not consistent with the actual field type");
        		}
    			try {
					return (T)field.get(obj);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.error("动态bean获取属性值异常：{}",ExceptionUtils.getMessage(e));
					throw new RuntimeException("动态bean获取属性值异常：".concat(ExceptionUtils.getMessage(e)));
				} 
    		}
		}
    	return null;
    }

}
