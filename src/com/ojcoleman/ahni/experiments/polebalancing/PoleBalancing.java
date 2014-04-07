package com.ojcoleman.ahni.experiments.polebalancing;

/**
 * Stub. Can be extended to single pole balancing framework
 * @author tom
 *
 */
public class PoleBalancing {	
	
	/* Environment parameters */
	static final double TimeDelta = 0.01;
	static final double Gravity = -9.8;
	static final double ForceMag = 10.0;

	public PoleBalancing() {
	}
	
	// Given an action, moves a cart.
	public void performAction(double action, Cart cart) {
		
		double force = (action - 0.5) * ForceMag * 2;
		
		// Perform iteration twice
		for(int i = 0; i < 2; i++) {
			Derivatives firstOrder = cart.firstOrder();
			Derivatives secondOrder = cart.secondOrder(force, Gravity);
			RK4.rk4(force, cart, firstOrder, secondOrder, Gravity, TimeDelta);
		}
	}	
}
