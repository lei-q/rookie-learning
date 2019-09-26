package com.example.demo.cglib;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class DynamicProxy {
	
	/**
	 * 	目标对象
	 */
	private Object target;
	
	DynamicProxy(){}
	DynamicProxy(Object target){
		this.target = target;
	}
	
	/**
	 * 	保存处理有返回值的方法后的结果
	 */
	public Map<String, Object> result = new HashMap<String, Object>();
	
	// 前置处理器链
	public List<ProxyProcessing> preprocessingChain = new ArrayList<>();
	// 后置处理器链
	public List<ProxyProcessing> afterpreprocessingChain = new ArrayList<>();
	
	/**获取代理对象
	 * @return proxy object
	 */
	public Object getProxyInstance() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(target.getClass());
		enhancer.setCallback(getMethodInterceptor());
		return enhancer.create();
	}
	
	public MethodInterceptor getMethodInterceptor() {
		return new MethodInterceptor() {
			
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
				System.out.println("====================");
				System.out.println("|***华丽的分割线***|");
				System.out.println("====================");
				
				// 前置处理链preprocessingchain
				preprocessingChain.forEach(preprocessing -> {
					if(preprocessing != null) {
						preprocessing.doProcessing();
						Consumer<Object> doConsumerProcessing = preprocessing.doConsumerProcessing();
						doConsumerProcessing.accept("");
					}
				});
				
				// 执行目标方法
				Object invokeSuper = methodProxy.invokeSuper(obj, args);
				
				// 后置处理链afterpreprocessingchain
				afterpreprocessingChain.forEach(afterProcessing -> {
					if(afterProcessing != null) {
						if(invokeSuper != null) {
							Map<String, Function<Object, Object>> doFunctionProcessing = afterProcessing.doFunctionProcessing();
							Entry<String, Function<Object, Object>> next = doFunctionProcessing.entrySet().iterator().next();
							result.put(next.getKey(), next.getValue().apply(invokeSuper));
						}
					}
				});
				return invokeSuper;
			}
		};
	}
	
	/**	设置代理对象
	 * @param target
	 * @return
	 */
	public static DynamicProxy setTarget(Object target) {
		return new DynamicProxy(target);
	}
	
	/**	添加前置处理器
	 * @param proxyProcessing
	 * @return
	 */
	public DynamicProxy addPreProcessing(ProxyProcessing proxyProcessing) {
		preprocessingChain.add(proxyProcessing);
		return this;
	}
	
	/**	添加后置处理器
	 * @param proxyProcessing
	 * @return
	 */
	public DynamicProxy addAfterProcessing(ProxyProcessing proxyProcessing) {
		afterpreprocessingChain.add(proxyProcessing);
		return this;
	}
	
	public static void main(String[] args) {
		// 目标对象（不需要有接口实现）
		CglibTarget target = new CglibTarget();
		
		DynamicProxy proxyCglib = DynamicProxy.setTarget(target);
				
		CglibTarget proxy = (CglibTarget) proxyCglib.addPreProcessing(new ProxyProcessing() {

			@Override
			public void doProcessing() {
				System.out.println("----doProcessing...");
			}
			
			@Override
			public Consumer<Object> doConsumerProcessing() {
				return t->{System.out.println("----doConsumerProcessing...");};
			}
			
		}).addAfterProcessing(new ProxyProcessing() {
			
			@Override
			public Map<String, Function<Object, Object>> doFunctionProcessing() {
				System.out.println("----doFunctionProcessing...");
				
				HashMap<String, Function<Object, Object>> hashMap = new HashMap<>();
				hashMap.put("key", new Function<Object, Object>() {

					@Override
					public Object apply(Object t) {
						return t = (Integer)t + 100;
					}
				});
				return hashMap;
			}
		}).getProxyInstance();

		// 执行代理方法
		proxy.save();
		Integer add = proxy.add("1", 1);
		System.out.println(add);
		System.out.println(proxyCglib.result);
	}
	
//	执行结果：
//	====================
//	|***华丽的分割线***|
//	====================
//	----doProcessing...
//	----doConsumerProcessing...
//	save...
//	====================
//	|***华丽的分割线***|
//	====================
//	----doProcessing...
//	----doConsumerProcessing...
//	add...
//	----doFunctionProcessing...
//	1
//	{key=101}
}