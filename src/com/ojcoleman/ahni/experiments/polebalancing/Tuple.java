package com.ojcoleman.ahni.experiments.polebalancing;

// Help class for defining 2D vectors.
public class Tuple {
	public double x;
	public double y;
	public Tuple(double x, double y) {
		this.x = x;
		this.y = y;
	}
	// Deep copy constructor
	public Tuple( Tuple t) {
		this.x = t.x;
		this.y = t.y;
	}
	
	public static Tuple sum( Tuple a, Tuple b) {
		return new Tuple(a.x+b.x, a.y+b.y);
	}
	public static Tuple sum( Tuple a, double c) {
		return new Tuple(a.x+c, a.y+c);
	}
	
	public static Tuple mult( Tuple a, Tuple b ) {
		return new Tuple(a.x*b.x, a.y*b.y);
	}
	
	public static Tuple mult( Tuple a, double c ) {
		return new Tuple(a.x*c, a.x*c);
	}
}