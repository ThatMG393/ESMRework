package com.thatmg393.esmanager.classes;

public class Tuple<X, Y> {
	private final X value1;
	private final Y value2; 
	
	public Tuple(X value1, Y value2) { 
		this.value1 = value1; 
		this.value2 = value2; 
	}
	
	public X getX() {
		return value1;
	}
	
	public Y getY() {
		return value2;
	}
}