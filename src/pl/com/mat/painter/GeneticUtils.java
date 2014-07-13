package pl.com.mat.painter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GeneticUtils {
	
	private final boolean SHOW_STATS = false;
	
	private double p_cross;
	private double p_mutate;
	
	private Random rn;

	public GeneticUtils(double p_cross, double p_mutate) {
		this.p_cross = p_cross;
		this.p_mutate = p_mutate;
		
		rn = new Random();
	}
	
	public Genom[] gerenatePopulation() {
		Genom[] genoms = new Genom[8];
		
		for(int i = 0; i < genoms.length; i++) {
			genoms[i] = new Genom();
			genoms[i].setZoom(randZoom());
			genoms[i].setX(randDoubleInRange(-12, 12));
			genoms[i].setY(randDoubleInRange(-12, 12));
			genoms[i].setDepth((long)randDoubleInRange(5, 20));
			genoms[i].setGlow(randDoubleInRange(-20, 8));
			genoms[i].setBrightness(randDoubleInRange(0.3, 1.0));
			genoms[i].setComplexScale(randDoubleInRange(0, 7));
			genoms[i].setRedParam(rn.nextInt(12)+1);
			genoms[i].setGreenParam(rn.nextInt(12)+1);
			genoms[i].setBlueParam(rn.nextInt(12)+1);
			genoms[i].setUseRandCols(Math.random());
			genoms[i].setQuad(rn.nextInt(20)+1);
			
			if(SHOW_STATS) {
				System.out.println("Genom nr. " + i + "\n");
				System.out.println("Zoom: " + genoms[i].getZoom());
				System.out.println("X: " + genoms[i].getX());
				System.out.println("Y: " + genoms[i].getY());
				System.out.println("Depth: " + genoms[i].getDepth());
				System.out.println("Glow: " + genoms[i].getGlow());
				System.out.println("Brightness: " + genoms[i].getBrightness());
				System.out.println("ComplexScale: " + genoms[i].getComplexScale());
				System.out.println("RedParam: " + genoms[i].getRedParam());
				System.out.println("GreenParam: " + genoms[i].getGreenParam());
				System.out.println("BlueParam: " + genoms[i].getBlueParam());
				System.out.println("Quad: " + genoms[i].getQuad());
				System.out.println("Use Rand Colors: " + genoms[i].getUseRandCols());
				System.out.println("\n");
			}
		}
		
		return genoms;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Feature generateNewFeature(int idx) {
		switch(idx) {
		case 0:
			return new Feature(randZoom());
		case 1:
			return new Feature(randDoubleInRange(-12, 12));
		case 2:
			return new Feature(randDoubleInRange(-12, 12));
		case 3:
			return new Feature(randDoubleInRange(5, 20));
		case 4:
			return new Feature(randDoubleInRange(-20, 8));
		case 5:
			return new Feature(randDoubleInRange(0.3, 1.0));
		case 6:
			return new Feature(randDoubleInRange(0, 7));
		case 7:
			return new Feature(rn.nextInt(12)+1);
		case 8:
			return new Feature(rn.nextInt(12)+1);
		case 9:
			return new Feature(rn.nextInt(12)+1);
		case 10:
			return new Feature(Math.random());
		default:
			return new Feature(rn.nextInt(20)+1);
		}
	}
	
	private double randZoom() {
		rn.setSeed(rn.nextLong());
		
		double rand = randDoubleInRange(-50, 50);
		while(rand > -2 && rand < 2) {
			rand = randDoubleInRange(-50, 50);
		}
		
		return rand;
	}
	
	private double randDoubleInRange(double min, double max) {
		rn.setSeed(rn.nextLong());
		return min + (rn.nextDouble() * (max - min));
	}
	
	public Genom[] enrichPopulationWithQuality(Genom[] g, int[] quality) {
		for(int i = 0; i< g.length; i++) {
			g[i].setQuality(quality[i]);
		}
		
		return g;
	}
	
	public Genom[] geneticAlgorithm(Genom[] g) {
		return mutate(crossing(tournamentSelection(g)));
	}
	
	private Genom[] tournamentSelection(Genom[] g) {
		Genom [] result = new Genom[g.length];
		
		int[] indexesOfGenoms;
		Genom best;
		for(int i=0; i< g.length; i++) {
			best = new Genom();
			rn.setSeed(System.currentTimeMillis());
			indexesOfGenoms = pickNRandomIndexes(8, rn.nextInt(2)+2);
			
			for(int j = 0; j< indexesOfGenoms.length; j++) {
				if(g[indexesOfGenoms[j]].getQuality() > best.getQuality())
					best = g[indexesOfGenoms[j]];
				//System.out.print(indexesOfGenoms[j] + " ");
			}
			//System.out.println();
			result[i] = best;
		}
		
		return result;
	}
	
	private int[] pickNRandomIndexes(int numbers, int n) {
		Integer[] indexes = new Integer[numbers];
		for(int i = 0; i<numbers; i++)
			indexes[i] = i;
		
		List<Integer> list = Arrays.asList(indexes);
		Collections.shuffle(list);
		
		int[] result = new int[n];
		for(int i = 0; i < n; i++)
			result[i] = list.get(i);
		
		return result;
	}
	
	private Genom[] crossing(Genom[] g) {
		Genom [] result = new Genom[g.length];
		
		int[] indexesToCross;
		int pointToCross;
		for(int i = 0; i< g.length; i++) {
			indexesToCross = pickNRandomIndexes(8, 2);
			rn.setSeed(System.currentTimeMillis());
			
			if(rn.nextDouble() < p_cross) {
				pointToCross = pickNRandomIndexes(12, 1)[0];
				result[i] = g[indexesToCross[0]].cross(g[indexesToCross[1]], pointToCross);
			} else {
				result[i] = rn.nextDouble() < 0.5 ? g[indexesToCross[0]] : g[indexesToCross[1]]; 
			}
		}
		
		return result;
	}
	
	private Genom[] mutate(Genom[] g) {
		Genom [] result = new Genom[g.length];
		
		int featureToMutate;
		for(int i = 0; i< g.length; i++) {
			rn.setSeed(System.currentTimeMillis());
			if(rn.nextDouble() < p_mutate) {
				featureToMutate = pickNRandomIndexes(12, 1)[0];
				result[i] = g[i].mutate(featureToMutate, generateNewFeature(featureToMutate));
			} else {
				result[i] = g[i];
			}
		}
		
		return result;
	}
	
}