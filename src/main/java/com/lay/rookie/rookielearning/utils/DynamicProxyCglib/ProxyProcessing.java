package com.example.demo.cglib;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ProxyProcessing {

	public default void doProcessing() {
	}
	
	public default Map<String, Function<Object, Object>> doFunctionProcessing() {
		return null;
	}
	
	public default Consumer<Object> doConsumerProcessing() {
		return null;
	}
}
