package beast.geo.operators;

import beast.core.Description;
import beast.evolution.tree.Node;

@Description("Provides distance between two nodes based on their state")
public interface StateDistanceProvider {
	
	public double distance(Node node1, Node node2, double time);

}
