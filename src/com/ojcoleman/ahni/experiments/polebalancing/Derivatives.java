package com.ojcoleman.ahni.experiments.polebalancing;

public class Derivatives {
	public double cart;
	public double[] poles;
	
	public Derivatives(double cart, double[] poles) {
		this.cart = cart;
		this.poles = poles;
	}
	
	public static Derivatives sum(Derivatives a, Derivatives b) {
		double cart = a.cart + b.cart;
		double[] poles = new double[a.poles.length];
		
		for(int i = 0; i < poles.length; i++) {
			poles[i] = a.poles[i] + b.poles[i];
		}
		
		return new Derivatives(cart, poles);
	}
	
	public static Derivatives mult(Derivatives a, Derivatives b) {
		double cart = a.cart * b.cart;
		double[] poles = new double[a.poles.length];
		
		for(int i = 0; i < poles.length; i++) {
			poles[i] = a.poles[i] * b.poles[i];
		}
		
		return new Derivatives(cart, poles);
	}
	
	// Scalar multiplication
	public static Derivatives mult(Derivatives a, double c) {
		double cart = a.cart * c;
		double[] poles = new double[a.poles.length];
		
		for(int i = 0; i < poles.length; i++) {
			poles[i] = a.poles[i] * c;
		}
		
		return new Derivatives(cart, poles);
	}
}