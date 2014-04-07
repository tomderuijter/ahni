/**
 * 
 */
package com.ojcoleman.ahni.experiments.polebalancing;

/**
 * @author Tom de Ruijter
 *
 */
public class TwoDimCart {
	
	// Design choice: derivatives are provided composite and should first be decomposed.
	// Updates the state for each dimension separately.
	// Composite results are then returned.
	// Possibly not very fast, but my guess is this is not very significant.
	
	Cart x;
	Cart y;
	
	// For sake of simplicity, pole initialization is copied into two dimensions.
	public TwoDimCart(double m, double friction, double[] pos, double[] vel, Pole[] polesX, Pole[] polesY) {
				
		x = new Cart(m,friction,pos[0],vel[0], polesX);
		y = new Cart(m,friction,pos[1],vel[1], polesY);
	}
	
	public int poleCount() {
		return x.poleCount();
	}	

	public Derivatives[] zeroOrder() {
		Derivatives[] position = {x.zeroOrder(), y.zeroOrder()};
		return position;
	}
	
	// Returns Two dimensional cart and pole velocity vectors
	public Derivatives[] firstOrder() {
		Derivatives[] velocity = {x.firstOrder(), y.firstOrder()};
		return velocity;
	}
	
	public Derivatives[] secondOrder(double forceX, double forceY, double gravity) {
		Derivatives[] acceleration = {x.secondOrder(forceX, gravity), y.secondOrder(forceY,gravity)};
		return acceleration;
	}
	
	public String toString() {
		return "X - " + x.toString() + "Y - " + y.toString();
	}
}
