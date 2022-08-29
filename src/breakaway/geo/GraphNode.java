package breakaway.geo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import sphericalGeo.GreatCircleDistance;

//import beast.continuous.SphericalDiffusionModel;
//import beast.evolution.alignment.distance.GreatCircleDistance;

abstract public class GraphNode {
	public int id;

	/** adjacent triangles **/
	GraphNode [] neighbours;

	/** distance to neighbor **/
	double [] distance;
	
	/** type of node, e.g. whether it is over land, water or the coast 
	 * interpretations depends on the kind of graph **/
	int type = 0;

	/** return center of node in [latitude, longitude] **/
	abstract public double [] getCenter();
	
	/** ensure vector of Cartesian coordinates has length of 1 **/ 
	static void normalise(double[] position) {
		double len = Math.sqrt(position[0] * position[0] + position[1] * position[1] + position[2] * position[2]);
		position[0] /= len;
		position[1] /= len;
		position[2] /= len;
	}
	
	
	/** return halfway point between two points of the triangle in latitude/longitude 
	 * @param leaveout: identify which corner (1,2,3) to leave out
	 * **/
	public static Vertex getHalfway(Vertex c1, Vertex c2) {
		double [] mean = new double[3];
		
		for (int i = 0; i < 3; i++) {
			mean[i] = (c1.cart[i] + c2.cart[i]) / 2.0;
		}
		normalise(mean);
		double [] center = SphericalMath.cartesian2Sperical(mean);
		return new Vertex(center[0], center[1]);
	}

	public static Vertex getHalfway(Vertex c1, Vertex c2, Vertex c3, Vertex c4) {
		double [] mean = new double[3];
		
		for (int i = 0; i < 3; i++) {
			mean[i] = (c1.cart[i] + c2.cart[i] + c3.cart[i] + c4.cart[i]) / 4.0;
		}
		normalise(mean);
		double [] center = SphericalMath.cartesian2Sperical(mean);
		return new Vertex(center[0], center[1]);
	}

	abstract public boolean hasPointsInside(double minLat, double minLong,
			double maxLat, double maxLong);


	abstract void calcNeighbours(boolean allNeighborsInput, boolean useGreatCircleDistance);

	@Override
	public String toString() {
		return id + "";//"<" + v1.toString() + ", " + v2.toString() + ", " + v3.toString() +">";
	}

	abstract public void addVertices(Set<Vertex> vertices);
	
	Set<Vertex> getVertices() {
		Set<Vertex> vertices = new HashSet<Vertex>();
		addVertices(vertices);
		return vertices;
	}

	void setUpDistances(boolean useGreatCircle) {
		distance = new double[neighbours.length];
		
		if (!useGreatCircle) {
			Arrays.fill(distance, 1.0);
		} else {
			double [] center = getCenter();
			for (int i = 0; i < neighbours.length; i++) {
				double [] nbcenter = neighbours[i].getCenter();
				distance[i] = GreatCircleDistance.pairwiseDistance(center, nbcenter);
			}
		}
	}

	public void scaleDistance(double scale) {
		for (int i = 0; i < distance.length; i++) {
			distance[i] *= scale;
		}
	}

	public void scaleDistance(double scale, int i) {
//		if (i >= distance.length) {
//			int h = 3;
//			h++;
//		}
		distance[i] *= scale;
	}
	
	public double getDistance(int i) {
		return distance[i];
	}

	public boolean isNeighbour(GraphNode sourceNode) {
		for (GraphNode neighbour : neighbours) {
			if (neighbour.id == sourceNode.id) {
				return true;
			}
		}
		return false;
	}

}
