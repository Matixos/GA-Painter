package pl.com.mat.painter;

public class Feature<T> {

	private T value;
	
	public Feature(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

}