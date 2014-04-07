package com.ojcoleman.ahni.experiments.polebalancing;

import org.jgapcustomised.Chromosome;

import com.anji.integration.Activator;
import com.anji.neat.NeatConfiguration;
import com.ojcoleman.ahni.evaluation.BulkFitnessFunctionMT;
import com.ojcoleman.ahni.evaluation.novelty.Behaviour;
import com.ojcoleman.ahni.experiments.polebalancing.Cart;
import com.ojcoleman.ahni.experiments.polebalancing.Pole;
import com.ojcoleman.ahni.hyperneat.Properties;

/**
 * 
 * @author Tom de Ruijter
 */
public class TwoDimPoleBalancing extends BulkFitnessFunctionMT {
	
	private static final long serialVersionUID = 2789827587948971178L;
	
	// Domain parameters
	double trackLength, gravity;
	
	// Simulation parameters
	int maxTimeSteps;
	double poleAngleThreshold;
	boolean includeVelocity = true;
	public static final double TimeDelta = 0.01;
	public static final double ForceMag = 10;
	
	// Agent parameters
	double cartMass, cartFriction, poleMass, poleLength, poleFriction;
	int poleCount;
	
	// Initialization parameters
	double cartPosX, cartPosY, cartVelX, cartVelY, initPoleAngle, initPoleVel;
	
	TwoDimCart cart;
	
	boolean biasViaInput = false;	// TODO: keep or not?

	public TwoDimPoleBalancing() {
	}
	
	// Initializes a cart in the specified starting position
	public void initCart () {
		// Initialize poles
		double scaling = 0.5;
		Pole[] polesX = new Pole[poleCount];
		Pole[] polesY = new Pole[poleCount];
		if(poleCount > 0) {
			polesX[0] = new Pole(poleLength, poleMass, poleFriction, initPoleAngle, initPoleVel);
			polesY[0] = new Pole(poleLength, poleMass, poleFriction, initPoleAngle, initPoleVel);
		}
		
		// Initial angle only applies to first pole
		for(int i = 1; i < poleCount; i++) {
			double c = Math.pow(scaling,(double) i);
			polesX[i] = new Pole(poleLength*c, poleMass*c, poleFriction,0,0);
			polesY[i] = new Pole(poleLength*c, poleMass*c, poleFriction,0,0);
		}
		
		double[] position = {cartPosX,cartPosY};
		double[] velocity = {cartVelX,cartVelY};
		this.cart = new TwoDimCart(cartMass,cartFriction,position,velocity,polesX,polesY);
	}
	
	@Override
	public void init(Properties props) {		
		super.init(props);

		// Domain parameters
		this.trackLength = props.getDoubleProperty("fitness.environment.trackLength");
		this.gravity = props.getDoubleProperty("fitness.environment.gravity");
		
		// Simulation parameters
		this.maxTimeSteps = props.getIntProperty("fitness.simulation.maxTimeSteps");
		this.poleAngleThreshold = props.getDoubleProperty("fitness.simulation.poleAngleThreshold");
		this.includeVelocity = props.getBooleanProperty("fitness.simulation.includeVelocity");
		
		// Agent parameters
		this.cartMass = props.getDoubleProperty("fitness.agent.cartMass");
		this.cartFriction = props.getDoubleProperty("fitness.agent.cartFriction");
		this.poleCount = props.getIntProperty("fitness.agent.poleCount");
		this.poleMass = props.getDoubleProperty("fitness.agent.poleMass");
		this.poleLength = props.getDoubleProperty("fitness.agent.poleLength");
		this.poleFriction = props.getDoubleProperty("fitness.agent.poleFriction");
		
		// Initialization parameters
		this.cartPosX = props.getDoubleProperty("fitness.agent.initial.cartPositionX");
		this.cartPosY = props.getDoubleProperty("fitness.agent.initial.cartPositionY");
		this.cartVelX = props.getDoubleProperty("fitness.agent.initial.cartVelocityX");
		this.cartVelY = props.getDoubleProperty("fitness.agent.initial.cartVelocityY");
		this.initPoleAngle = props.getDoubleProperty("fitness.agent.initial.poleAngle");
		this.initPoleVel = props.getDoubleProperty("fitness.agent.initial.poleVelocity");			

	}
	
	@Override
	protected void evaluate(Chromosome genotype, Activator substrate, int evalThreadIndex, double[] fitnessValues, Behaviour[] behaviours) {
		_evaluate(genotype, substrate, null, false, false, fitnessValues, behaviours);
	}
	
	@Override
	public void evaluate(Chromosome genotype, Activator substrate, String baseFileName, boolean logText, boolean logImage) {
		_evaluate(genotype, substrate, baseFileName, logText, logImage, null, null);
	}
	
	public void _evaluate(Chromosome genotype, Activator substrate, String baseFileName, boolean logText, boolean logImage, double[] fitnessValues, Behaviour[] behaviours) {

		// Take new cart.
		initCart();
		double[] input;
		int timeStep = 0;
		while(timeStep < maxTimeSteps && legalSolution()) {
			
			// Construct network input values
			input = convertState();
			
			// Clamp input to network
			double[] output = substrate.next(input);
			
			// Propagate network output through cart state
			performAction(output[0], output[1], cart);
			timeStep++;
		}
		fitness(timeStep, fitnessValues);
	}
	
	// Converts cart object to form accepted by substrate
	private double[] convertState () {
		double[] input;
		int x = 0;
		int y = 1;
		
		Derivatives[] position = cart.zeroOrder();
		
		if(includeVelocity) {
			input = new double[4+(4*cart.poleCount())];
			
			Derivatives[] velocity = cart.firstOrder();
			input[0] = position[x].cart / (trackLength * 0.5);
			input[1] = position[y].cart / (trackLength * 0.5);
			input[2] = velocity[x].cart / 0.75;
			input[3] = velocity[y].cart / 0.75;
			for(int i = 0; i < cart.poleCount(); i++) {
				input[4*i + 4] 		= position[x].poles[i] / poleAngleThreshold;
				input[4*i + 4+1]	= position[y].poles[i] / poleAngleThreshold;
				input[4*i + 4+2]	= velocity[x].poles[i];
				input[4*i + 4+3]	= velocity[y].poles[i];
			}
			
		} else {
			input = new double[2+(2*cart.poleCount())];
			
			input[0] = position[x].cart / (trackLength * 0.5);
			input[1] = position[y].cart / (trackLength * 0.5);
			
			for(int i = 0; i < cart.poleCount(); i++) {
				input[2*i + 2]		= position[x].poles[i] / poleAngleThreshold;
				input[2*i + 2+1]	= position[y].poles[i] / poleAngleThreshold;
			}
		}
		
		return input;
	}
	
	// Abort simulation when:
	// - cart runs off track
	// - pole is below threshold
	private boolean legalSolution() {

		boolean valid = cart.x.onTrack(trackLength * 0.5) && cart.y.onTrack(trackLength * 0.5);
		for(int i = 0; i < cart.poleCount(); i++) {
			valid = valid && cart.x.poles[i].onTrack(poleAngleThreshold)
						  && cart.y.poles[i].onTrack(poleAngleThreshold);
		}
		return valid;
	}
		
	// Given an action, moves a cart.
	public void performAction(double actionX, double actionY, TwoDimCart cart) {
		
		double forceX = (actionX - 0.5) * ForceMag * 2;
		double forceY = (actionY - 0.5) * ForceMag * 2;
		
		// Perform iteration twice
		for(int i = 0; i < 2; i++) {
			Derivatives[] firstOrder = cart.firstOrder();
			Derivatives[] secondOrder = cart.secondOrder(forceX, forceY, gravity);
			RK4.rk4(forceX, cart.x, firstOrder[0], secondOrder[0],gravity, TimeDelta);
			RK4.rk4(forceY, cart.y, firstOrder[1], secondOrder[1],gravity, TimeDelta);
		}
	}	
	// Fraction of time elapsed before failure.
	// Normalized to a continuous [0,1] domain.
	private void fitness(double timeStep, double[] fitnessValues) {
		
		if (fitnessValues != null) {
			fitnessValues[0] = (double) timeStep / maxTimeSteps;
		}
		
		
		
		// TODO: Penalize with distance from center
	}
	
	@Override
	public int fitnessObjectivesCount() {
		return 1;
	}
}
