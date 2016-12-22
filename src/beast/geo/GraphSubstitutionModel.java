package beast.geo;

import java.util.ArrayList;
import java.util.List;

import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.parameter.IntegerParameter;
import beast.evolution.datatype.DataType;
import beast.evolution.substitutionmodel.EigenDecomposition;
import beast.evolution.substitutionmodel.SubstitutionModel.Base;
import beast.evolution.tree.Node;
import beast.geo.operators.StateDistanceProvider;

abstract public class GraphSubstitutionModel extends Base implements StateDistanceProvider {
	public Input<IntegerParameter> locationInput = new Input<IntegerParameter>("location", "location, one for each node in the tree, representing the node number in the graph", Validate.REQUIRED);
	public Input<Graph> graphInput = new Input<Graph>("graph", "the graph specifying rates among nodes", Validate.REQUIRED);

	protected Graph graph;
	protected IntegerParameter location;
	// n = number of nodes in graph
	int n;

	
	
	public List<GraphNode> calcMAPPath(Node from, Node too, double fStartTime, double fEndTime, double fRate) {
        double distance = (fEndTime - fStartTime) * fRate;

        int source = from.getParent().getNr();
		source = location.getValue(source);
		GraphNode sourceNode = graph.nodes.get(source); 
        int target = too.getNr();
		target = location.getValue(target);
		GraphNode targetNode = graph.nodes.get(target); 

		List<GraphNode> path = new ArrayList<GraphNode>();
		path.add(sourceNode);
		calcMAPPath(path, sourceNode, targetNode, distance, new boolean[n]);
		return path;
		
	}
		
	void calcMAPPath(List<GraphNode> path, GraphNode sourceNode, GraphNode targetNode, double distance, boolean [] used) {
		used[sourceNode.id] = true;
		used[targetNode.id] = true;
		
		if (sourceNode.id == targetNode.id) {
			return;
		}
		
		if (targetNode.isNeighbour(sourceNode)) {
			path.add(targetNode);
			return;
		}
		
		double [] pSourceHalfway = getTransitionProbabilities(sourceNode, distance/2.0); 
		double [] pTargetHalfway = getTransitionProbabilities(targetNode, distance/2.0);
		double maxP = Double.NEGATIVE_INFINITY;
		int maxi = -1;
		for (int i = 0; i < n; i++) {
			double p = pSourceHalfway[i] * pTargetHalfway[i];
			if (p > maxP && !used[i]) {
				maxP = p;
				maxi = i;
			}
		}
		if (maxi < 0) {
			path.add(targetNode);
			return;
		}

		GraphNode halfwayNode = graph.nodes.get(maxi);
		used[halfwayNode.id] = true;
		calcMAPPath(path, sourceNode, halfwayNode, distance / 2.0, used);
		calcMAPPath(path, halfwayNode, targetNode, distance / 2.0, used);
	
	}
	
	
	@Override
	public EigenDecomposition getEigenDecomposition(Node node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canHandleDataType(DataType dataType) {
		// TODO Auto-generated method stub
		return false;
	}

	//@Override
	//public void getTransitionProbabilities(Node node, double fStartTime, double fEndTime, double fRate, double[] matrix) {}
	
	abstract  public double [] getTransitionProbabilities(GraphNode node, double distance);
	/** 
	 * get trans probs from a range of points to another range of points
	 * result goes into probabilities matrix
	 * return logScale -- scale factor used for normalising the probabilities matrix
	 * **/
	abstract  public double getTransitionProbabilities(int [] origRange, int [] destRange, double time, double [] probabilities);
	

	/** log probability of coming from a node's parent to the node at given rate and time interval **/
	abstract public double getLogTransitionProbability(Node node, double fStartTime, double fEndTime, double fRate);


	
	abstract public double getLogLikelihood(int source, int target, double time);
	
	/** log probability of root location **/
	public double getLogRootFrequency(Node node) {
        int target = node.getNr();
		target = location.getValue(target);
		double f = getFrequencies()[target];
		return Math.log(f);
	}

	public static void logPtoP(double [] logP) {
		int n = logP.length;
		// exponentiate log probabilities
		double max = Double.NEGATIVE_INFINITY;
		for (double d : logP) {
			if (!Double.isNaN(d)) {
				max = Math.max(max, d);
			}
		}
		for (int i = 0; i < n; i++) {
			if (!Double.isNaN(logP[i])) {
				logP[i] = Math.exp(logP[i] - max);
			} else {
				logP[i] = 0.0;
			}
		}
		// normalise probabilities so they sum to unity
		double sum = 0;
		for (int i = 0; i < n; i++) {
			sum += logP[i];
		}
		for (int i = 0; i < n; i++) {
			logP[i] /= sum;
		}
	}

	abstract public double [] getLogTransitionProbabilities(GraphNode startNode, double time);

	GraphNode getGraphNode(Node node) {
		int target = node.getNr();
		target = location.getValue(target);
		GraphNode targetNode = graph.nodes.get(target);
		return targetNode;
	}
	
	public double distance(Node node1, Node node2, double time) {
		return graph.getDistance(getGraphNode(node1), getGraphNode(node2));
	}

	public double getTransitionProbabilities(Node node, double height, double height2, double jointBranchRate, int[] nodeRange,
			int[] parentRange, double[] matrix) {
		double time = (height2 - height) * jointBranchRate;
		double logScale = getTransitionProbabilities(nodeRange, parentRange, time, matrix);
		return logScale;
	}


}
