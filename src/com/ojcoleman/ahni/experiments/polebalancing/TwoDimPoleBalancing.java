package com.ojcoleman.ahni.experiments.polebalancing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jgapcustomised.Chromosome;

import com.anji.integration.Activator;
import com.ojcoleman.ahni.hyperneat.Properties;
import com.ojcoleman.ahni.evaluation.novelty.Behaviour;
import com.ojcoleman.ahni.experiments.doublepolebalancing.JiggleBuffer;
import com.ojcoleman.ahni.experiments.polebalancing.Pole;
import com.ojcoleman.ahni.evaluation.BulkFitnessFunctionMT;

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
	
//	TwoDimCart cart;

	// Initializes a cart in the specified starting position
	public TwoDimCart initCart () {
		// Initialize poles
		double scaling = 0.1;
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
		return new TwoDimCart(cartMass,cartFriction,position,velocity,polesX,polesY);
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
		try {
			_evaluate(genotype, substrate, null, false, false, fitnessValues, behaviours);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void evaluate(Chromosome genotype, Activator substrate, String baseFileName, boolean logText, boolean logImage) {
		try {
			_evaluate(genotype, substrate, baseFileName, logText, logImage, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void _evaluate(Chromosome genotype, Activator substrate, String baseFileName, boolean logText, boolean logImage, double[] fitnessValues, Behaviour[] behaviours) throws IOException { 

		// Take new cart.
		TwoDimCart cart = initCart();
		double[] input;
		int timeStep = 0;
		
		// Prepare objects for optional output
		File file = null;
		FileWriter fw = null; 
		BufferedWriter bw = null;
		
		// Initialize writers for optional output
		if(logText) {
			file = new File(baseFileName + "positions.csv");
			if (!file.exists()) {
				file.createNewFile();
			}

			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
		}
		
		JiggleBuffer jiggleBuffer1 = null;
		if(!includeVelocity) {
			jiggleBuffer1 = new JiggleBuffer(100);
		}
		
		while(timeStep < maxTimeSteps && legalSolution(cart)) {
			
			// Write state variables to file
			if(logText) {
				bw.write(outputState(cart));
				bw.newLine();
			}
			
			// Construct network input values
			input = convertState(cart);
	
			// Clamp input to network
			double[] output = substrate.next(input);
			
			// Propagate network output through cart state
			performAction(output[0], output[1], cart);
			
			// Place the latest jiggle value into buffer1.
			if (jiggleBuffer1 != null) {
				jiggleBuffer1.enqueue(sumState(cart));
			}
			
			timeStep++;
		}
		
		fitness(timeStep, fitnessValues, cart, jiggleBuffer1);
		
		// Close file logging;
		if(logText) {
			bw.close();
		}
	}
	
	// Writes a single line with cart and pole positions in CSV format.
	private String outputState(TwoDimCart cart) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(cart.x.position);
		sb.append(",");
		sb.append(cart.y.position);
		
		for(int i = 0; i < cart.poleCount(); i++) {
			sb.append(",");
			sb.append(cart.x.poles[i].angle);
			sb.append(",");
			sb.append(cart.y.poles[i].angle);
		}
		
		return sb.toString();

	}
	
	// Converts cart object to form accepted by substrate
	private double[] convertState (TwoDimCart cart) {
		double[] input;
		int x = 0;
		int y = 1;
		
		Derivatives[] position = cart.zeroOrder();
		
		// TODO Include input bias
//		double[] input = new double[3 + (biasViaInput ? 1 : 0)];
//		if (biasViaInput)
//			input[3] = 0.5;
		
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
	
	// Sums the positions and velocities of the cart and the main pole
	private double sumState(TwoDimCart cart) {
		double sum = 0;
		int x = 0;
		int y = 1;
		Derivatives[] position = cart.zeroOrder();
		Derivatives[] velocity = cart.firstOrder();
		
		sum += Math.abs(position[x].cart) + Math.abs(position[y].cart);
		sum += Math.abs(position[x].poles[0]) + Math.abs(position[y].poles[0]);
		sum += Math.abs(velocity[x].cart) + Math.abs(velocity[y].cart);
		sum += Math.abs(velocity[x].poles[0]) + Math.abs(position[y].poles[0]);
		return sum;
	}
	
	// Abort simulation when:
	// - cart runs off track
	// - pole is below threshold
	private boolean legalSolution(TwoDimCart cart) {

		boolean valid = cart.x.onTrack(trackLength * 0.5) && cart.y.onTrack(trackLength * 0.5);
//		if(!valid)
//			System.out.println("Cart went off track!");
		
		for(int i = 0; i < cart.poleCount(); i++) {
			valid = valid && cart.x.poles[i].onTrack(poleAngleThreshold)
						  && cart.y.poles[i].onTrack(poleAngleThreshold);
//			if(!valid)
//				System.out.println("Pole " + i + " fell down!");
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
			RK4.rk4(forceX, cart.x, firstOrder[0], secondOrder[0], gravity, TimeDelta);
			RK4.rk4(forceY, cart.y, firstOrder[1], secondOrder[1], gravity, TimeDelta);
		}
	}	
	// Fraction of time elapsed before failure.
	// Normalized to a continuous [0,1] domain.
	private void fitness(double timeStep, double[] fitnessValues, TwoDimCart cart, JiggleBuffer jiggleBuffer1) {
		
		if (fitnessValues != null) {
			double f1 = (double) timeStep / maxTimeSteps;
			
			fitnessValues[0] = f1;
	
			if (f1 == 1.0) {
				fitnessValues[0] = f1;
				return;
			}
			
			double f2 = 0;
			if(jiggleBuffer1 != null) {
				f2 = timeStep < 100 ? 0 : 0.75 / jiggleBuffer1.getTotal();
			}
			
			if(includeVelocity)
				fitnessValues[0] = f1;
			else
				fitnessValues[0] = 0.1 * f1 + 0.9 * f2 ;
			
			// Penalize with distance from center
			double factor = timeStep / (maxTimeSteps * 2);
			double distanceError = factor * Math.pow((cart.x.position + cart.y.position) / (trackLength * 2),2);
			if (distanceError < fitnessValues[0] && fitnessValues[0] != 1.0) 
				fitnessValues[0] -= distanceError;
		}
		

	}
	
	@Override
	public int fitnessObjectivesCount() {
		return 1;
	}
}
