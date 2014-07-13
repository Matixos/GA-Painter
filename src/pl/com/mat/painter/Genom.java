package pl.com.mat.painter;

import java.util.HashMap;
import java.util.Map;

//basic bean
public class Genom {
	
	@SuppressWarnings("rawtypes")
	private Map<Integer, Feature> features = new HashMap<Integer, Feature>();

	/*   PARAMS
	 * 
	private Feature<Double> zoom;
	private Feature<Double> x;
	private Feature<Double> y ;
	
	private Feature<Long> depth;
	private Feature<Double> glow;
	private Feature<Double> brightness;
	private Feature<Double> complexScale;
	
	private Feature<Integer> p_r, p_g, p_b;
	private Feature<Double> useRandCols;
	
	private Feature<Integer> quad; */   // was 10 
	
	private int quality = 0;
	
	public Genom() {}
	
	public Genom(double zoom, double x, double y, long depth, double glow,
			double brightness, double complexScale, int p_r, int p_g, int p_b, double useRandCols, int quad) {
		features.put(0, new Feature<Double>(zoom));
		features.put(1, new Feature<Double>(x));
		features.put(2, new Feature<Double>(y));
		features.put(3, new Feature<Long>(depth));
		features.put(4, new Feature<Double>(glow));
		features.put(5, new Feature<Double>(brightness));
		features.put(6, new Feature<Double>(complexScale));
		
		features.put(7, new Feature<Integer>(p_r));
		features.put(8, new Feature<Integer>(p_g));
		features.put(9, new Feature<Integer>(p_b));
		
		features.put(10, new Feature<Double>(useRandCols));
		features.put(11, new Feature<Integer>(quad));
	}
	
	public Genom cross(Genom g, int point) {
		Genom result = new Genom();
		for(int i = 0; i<12; i++) {
			if(i < point)
				result.features.put(i, this.features.get(i));
			else
				result.features.put(i, g.features.get(i));
		}
		
		return result;
	}
	
	@SuppressWarnings("rawtypes")
	public Genom mutate(int featureIdx, Feature f) {
		this.features.put(featureIdx, f);
		
		return this;
	}

	public double getZoom() {
		return (double)features.get(0).getValue();
	}

	public void setZoom(double zoom) {
		this.features.put(0, new Feature<Double>(zoom));
	}

	public double getX() {
		return (double)features.get(1).getValue();
	}

	public void setX(double x) {
		this.features.put(1, new Feature<Double>(x));
	}

	public double getY() {
		return (double)features.get(2).getValue();
	}

	public void setY(double y) {
		this.features.put(2, new Feature<Double>(y));
	}

	public long getDepth() {
		return (long)features.get(3).getValue();
	}

	public void setDepth(long depth) {
		this.features.put(3, new Feature<Long>(depth));
	}

	public double getGlow() {
		return (double)features.get(4).getValue();
	}

	public void setGlow(double glow) {
		this.features.put(4, new Feature<Double>(glow));
	}

	public double getBrightness() {
		return (double)features.get(5).getValue();
	}

	public void setBrightness(double brightness) {
		this.features.put(5, new Feature<Double>(brightness));
	}

	public double getComplexScale() {
		return (double)features.get(6).getValue();
	}

	public void setComplexScale(double complexScale) {
		this.features.put(6, new Feature<Double>(complexScale));
	}

	public int getRedParam() {
		return (int)features.get(7).getValue();
	}

	public void setRedParam(int p_r) {
		this.features.put(7, new Feature<Integer>(p_r));
	}

	public int getGreenParam() {
		return (int)features.get(8).getValue();
	}

	public void setGreenParam(int p_g) {
		this.features.put(8, new Feature<Integer>(p_g));
	}

	public int getBlueParam() {
		return (int)features.get(9).getValue();
	}

	public void setBlueParam(int p_b) {
		this.features.put(9, new Feature<Integer>(p_b));
	}
	
	public double getUseRandCols() {
		return (double)features.get(10).getValue();
	}

	public void setUseRandCols(double useRandCols) {
		this.features.put(10, new Feature<Double>(useRandCols));
	}

	public int getQuad() {
		return (int)features.get(11).getValue();
	}

	public void setQuad(int quad) {
		this.features.put(11, new Feature<Integer>(quad));
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}
}
