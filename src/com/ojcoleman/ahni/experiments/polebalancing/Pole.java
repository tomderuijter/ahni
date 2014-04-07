package com.ojcoleman.ahni.experiments.polebalancing;

public class Pole {
	double length;
	double mass;
	double friction;
	double angle;
	double velocity;
	
	public Pole(double l, double m, double friction, double angle, double v) {
		this.length = l;
		this.mass = m;
		this.friction = friction;
		this.angle = angle;
		this.velocity = v;
	}
	
	// Deep copy constructor
	public Pole(Pole p) {
		this.length = p.length;
		this.mass = p.mass;
		this.friction = p.friction;
		this.angle = p.angle;
		this.velocity = p.velocity;
	}
	
	public void move(double firstOrder, double secondOrder, double stepSize) {
		this.angle += firstOrder * stepSize;
		this.velocity += secondOrder * stepSize;
	}
	
	// Returns the pole's effective force. This is dependent on the gravitational field.
	public double effectiveForce(double gravity) {
		
		double costheta = Math.cos(this.angle);
		double sintheta = Math.sin(this.angle);
		double ml = this.length * this.mass;
		double temp = this.friction * this.velocity / ml;
		
		return (ml * this.velocity * this.velocity * sintheta) + (0.75 * this.mass * costheta * (temp + (gravity * sintheta)));
	}
	
	// Returns the pole's effective mass.
	public double effectiveMass() {
		
		double costheta = Math.cos(this.angle);
		
		return this.mass * (1 - (0.75 * costheta * costheta));
	}
	
	// Returns angular velocity of this pole given cart acceleration.
	public double secondOrder(double cartAcceleration, double gravity) {
		
		double costheta = Math.cos(this.angle);
		double sintheta = Math.sin(this.angle);
		double ml = this.length * this.mass;
		double temp = this.friction * this.velocity / ml;
		
		return -0.75 * (cartAcceleration * costheta + (gravity * sintheta) + temp) / this.length;
	}
	
	public String toString() {
		return "angle: " + this.angle + "\n" +
			   "velocity: " + this.velocity;
	}
	
	public boolean onTrack(double threshold) {
		return (this.angle > -threshold) && 
			   (this.angle < threshold);
	}
}