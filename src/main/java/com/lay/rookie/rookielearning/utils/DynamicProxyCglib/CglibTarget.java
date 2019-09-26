package com.example.demo.cglib;

public class CglibTarget {

	public void show() {
		
	}
	
	public void save() {
		System.out.println("save...");
	}
	
	public Integer add(String one, Integer two) {
		System.out.println("add...");
		return two;
	}
}
