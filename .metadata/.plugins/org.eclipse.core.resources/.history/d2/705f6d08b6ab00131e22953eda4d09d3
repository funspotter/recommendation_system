package com.incredibles.reclib;

public class Count implements Comparable<Count> {
	int value;
	int count;
	public Count(int value) {
		this.value = value;
		this.count = 1;
	}
	public void increment() {
		count++;
	}
	public int compareTo(Count other) {
		return other.count - count;
	}
}