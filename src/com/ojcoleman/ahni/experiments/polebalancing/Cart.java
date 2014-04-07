package com.ojcoleman.ahni.experiments.polebalancing;

import com.ojcoleman.ahni.experiments.polebalancing.Pole;

public class Cart {
	double mass;
	double friction;
	double position;
	double velocity;
	Pole[] poles;
	
	public Cart(double m, double friction, double x, double v, Pole[] poles) {
		this.mass = m;
		this.friction = friction;
		this.position = x;
		this.velocity = v;
		this.poles = poles;
	}
	
	// Deep copy constructor
	public Cart( Cart c ) {
		this.mass = c.mass;
		this.friction = c.friction;
		this.position = c.position;
		this.velocity = c.velocity;
		
		// Deep copy of list of poles
		this.poles = new Pole[c.poleCount()];
		for(int i = 0; i < c.poleCount(); i++) {
			this.poles[i] = new Pole(c.poles[i]);
		}
	}
	
	public int poleCount() {
		return poles.length;			
	}
	
	public void move(Derivatives firstOrder, Derivatives secondOrder, double stepSize) {
		
		this.position += firstOrder.cart * stepSize;
		this.velocity += secondOrder.cart * stepSize;
		
		// Moves poles
		for(int i = 0; i < this.poleCount(); i++) {
			this.poles[i].move(firstOrder.poles[i], secondOrder.poles[i], stepSize);
		}
	}
	
	public Derivatives zeroOrder() {
		double[] poles = new double[this.poleCount()];
		for(int i = 0; i < this.poleCount(); i++) {
			poles[i] = this.poles[i].angle;
		}
		return new Derivatives(this.position, poles);
	}
	
	public Derivatives firstOrder() {
		
		double[] poles = new double[this.poleCount()];
		for(int i = 0; i < this.poleCount(); i++) {
			poles[i] = this.poles[i].velocity;
		}
		return new Derivatives(this.velocity, poles);
	}
	
	// Given an external force, returns a derivative object containing the 
	// accelerations of the cart and its poles.
	public Derivatives secondOrder(double force, double gravity) {

		int c = this.poleCount();
		
		double effectiveForce = 0;
		double effectiveMass = 0;
		
		for(int i = 0; i < c; i++) {
			effectiveForce += this.poles[i].effectiveForce(gravity);
			effectiveMass += this.poles[i].effectiveMass();
		}
		
		double cartAcceleration = (force + effectiveForce) / (effectiveMass + this.mass);
		
		double[] poleAccelerations = new double[c];
		for(int i = 0; i < c; i++) {
			poleAccelerations[i] = this.poles[i].secondOrder(cartAcceleration, gravity);
		}
		
		return new Derivatives(cartAcceleration, poleAccelerations);
	}
	
	public boolean onTrack(double trackLength) {
		return (this.position > -trackLength * 0.5) && 
			   (this.position < trackLength * 0.5);
	}
	
	public String toString() {
		String s = 	"=Cart=\n" +
					"position: " + this.position + "\n" +
					"velocity: " + this.velocity + "\n";
		int i = 1;
		for(Pole p : this.poles) {
			s += 	"=Pole " + i + "=\n" +
					p.toString() + "\n";
			i++;
		}
		
		return s;
	}
	
}
