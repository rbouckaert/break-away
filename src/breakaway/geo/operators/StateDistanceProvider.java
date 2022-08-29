package breakaway.geo.operators;

import beast.base.core.Description;
import beast.base.evolution.tree.Node;

@Description("Provides distance between two nodes based on their state")
public interface StateDistanceProvider {
	
	public double distance(Node node1, Node node2, double time);

}
