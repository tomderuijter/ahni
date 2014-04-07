package com.ojcoleman.ahni.experiments.polebalancing;

public class RK4 {

	/**
	 * RK4 - Runge-Kutta approximation of systems of ordinary differential equations.
	 * In this case, approximates the position and velocity of a cart and its poles
	 * given a force that is acted upon the cart.
	 * 
	 * @param f Force acted upon cart k
	 * @param k State object for cart and its poles
	 * @param firstOrderK Velocities of cart k and its poles
	 * @param secondOrderK Accelerations of cart k and its poles
	 */
	public static void rk4(double f, Cart k, Derivatives firstOrderK, Derivatives secondOrderK, double gravity, double timeDelta) {
				
		double hh = timeDelta * 0.5;
		double h6 = timeDelta / 6.0;
		
		// Calculate k2
		Cart k2 = new Cart(k);
		k2.move(firstOrderK, secondOrderK, hh);
		Derivatives firstOrderK2 = k2.firstOrder();
		Derivatives secondOrderK2 = k2.secondOrder(f, gravity);
		
		// Calculate k3
		Cart k3 = new Cart(k);
		k3.move(firstOrderK2, secondOrderK2, hh);
		Derivatives firstOrderK3 = k3.firstOrder();
		Derivatives secondOrderK3 = k3.secondOrder(f, gravity);
				
		// Calculate k4
		Cart k4 = new Cart(k);
		k4.move(firstOrderK3, secondOrderK3, timeDelta);
		Derivatives firstOrderK4 = k4.firstOrder();
		Derivatives secondOrderK4 = k4.secondOrder(f, gravity);
		
		// Approximation
		Derivatives firstOrderK14, firstOrderK23, secondOrderK14, secondOrderK23;
		firstOrderK14 = Derivatives.sum(firstOrderK, firstOrderK4);
		firstOrderK23 = Derivatives.mult(Derivatives.sum(firstOrderK2, firstOrderK3), 2);
		firstOrderK = Derivatives.sum(firstOrderK14, firstOrderK23);		// By reference update
		
		secondOrderK14 = Derivatives.sum(secondOrderK, secondOrderK4);
		secondOrderK23 = Derivatives.mult(Derivatives.sum(secondOrderK2, secondOrderK3), 2);
		secondOrderK = Derivatives.sum(secondOrderK14, secondOrderK23); 	// By reference update
		
		k.move(firstOrderK,secondOrderK,h6);								// By reference update
	}	
}
