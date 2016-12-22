package beast.geo;

import java.io.Serializable;

import beast.core.Description;


@Description("serialisable wrapper around a distance matrix")
public class DistanceMatrix implements Serializable {
	private static final long serialVersionUID = 1L;
	
	double [][] distances;
	
	DistanceMatrix(double [][] distances) {
		this.distances = distances;
	}
}
