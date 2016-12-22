package beast.geo;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import beast.core.Description;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.parameter.IntegerParameter;
import beast.core.parameter.RealParameter;
import beast.core.util.Log;
import beast.evolution.alignment.TaxonSet;
//import beast.evolution.alignment.distance.GreatCircleDistance;
import beast.evolution.datatype.DataType;
import beast.evolution.substitutionmodel.EigenDecomposition;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;
import sphericalGeo.GreatCircleDistance;


@Description("Diffusion model based on great circle distance between two nodes in graph = taxon set locations")
public class DistanceBasedDiffusionModel extends GraphSubstitutionModel {
	public Input<RealParameter> precisionInput = new Input<>("precision", "precision governing diffusion process", Validate.REQUIRED);

	public Input<IntegerParameter> initInput = new Input<IntegerParameter>("init","location parameter to be initialised", Validate.REQUIRED);
	public Input<String> locationsInput = new Input<String>("value","comma separated string encoding locations in the form {taxon}=${latitude} ${longitude}", Validate.REQUIRED);
	public Input<TaxonSet> taxonsetInput = new Input<TaxonSet>("taxonset","set of taxa for which we have locations", Validate.REQUIRED);
	public Input<Tree> treeInput = new Input<Tree>("tree", "tree structure to sample from", Validate.REQUIRED);
	
	
	public DistanceBasedDiffusionModel() {
		graphInput.setRule(Validate.OPTIONAL);
		frequenciesInput.setRule(Validate.OPTIONAL);
	}
	
	RealParameter precision;

	double [][] distances;
	double [] uniformfrequencies;
	int dim;
	Integer [] locations;
	double [][] position;

	@Override
	public void initAndValidate() {
		precision = precisionInput.get();
		location = locationInput.get();
		
		TaxonSet taxa = taxonsetInput.get();
		List<String> taxonNames = new ArrayList<String>();
		// make all lowercase
		for (String name: taxa.asStringList()) {
			taxonNames.add(name.toLowerCase());
		}
		dim = initInput.get().getMinorDimension1();
		locations = new Integer[(taxonNames.size() * 2 - 1) * dim];
		Arrays.fill(locations, -1);
		for (int iTaxon = 0; iTaxon < taxonNames.size(); iTaxon++) {
			for (int i = 0; i < dim; i++) {
				locations[iTaxon * dim + i] = iTaxon;
			}
		}

		int n = taxonNames.size();
		position = new double[n][2];
		for (int i = 0; i < n; i++) {
			position[i][0]= - 360.0;
		}
		
		// set locations of tip nodes
		String [] sPositions = locationsInput.get().split(",");
		for (String sPosition: sPositions) {
			String [] sStrs = sPosition.split("=");
			String sTaxon = sStrs[0].replaceAll("\\s", "");
			int iTaxon = taxonNames.indexOf(sTaxon.toLowerCase());
			if (iTaxon < 0) {
				Log.warning.println("WARNING: Could not find taxon \"" + sTaxon + "\" in taxon set, but a location was specified.");
			} else {
				String sLoc = " " + sStrs[1];
				sStrs = sLoc.split("\\s+");
				position[iTaxon][0] = Double.parseDouble(sStrs[1]);
				position[iTaxon][1] = Double.parseDouble(sStrs[2]);
			}
		}
		
		// check all tip locations are catered for
		boolean found = false;
		for (int i = 0; i < taxonNames.size(); i++) {
			if (position[i][0] == -360.0) {
				Log.warning.println("WARNING: No location found for " + taxonNames.get(i) + ". Typo perhaps?");
				found = true;
			}
		}
		if (found) {
			throw new RuntimeException("Locations for all taxa should be specified");
		}
		
		distances = new double[n][n];
		for (int i = 0; i < n; i++) {
			double [] start = position[i];
			for (int j = i + 1; j < n; j++) {
				distances[i][j] = GreatCircleDistance.pairwiseDistance(start, position[j]);
				distances[j][i] = distances[i][j];
			}
		}
		super.initAndValidate();
		

		positionNodes();
		IntegerParameter traitParameter = new IntegerParameter(locations);
		traitParameter.setUpper(n);
		initInput.get().assignFromWithoutID(traitParameter);
		
		uniformfrequencies = new double[n];
		Arrays.fill(uniformfrequencies, 1.0/n);
	}

	private Integer[] positionNodes() {
		Tree tree = treeInput.get();
		
		// first dimension by mean location
		positionInternlNodes(tree.getRoot());
		
		// other dimensions randomly
		for (int i = tree.getLeafNodeCount(); i < tree.getNodeCount(); i++) {
			for (int j = 1; j < dim; j++) {
				boolean isDup = false;
				do {
					locations[i * dim + j] = Randomizer.nextInt(graph.getSize());
					isDup = false;
					for (int k = 0; j < j; k++) {
						if (locations[i*dim + k] == locations[i*dim + j]) {
							isDup = true;
						}
					}
				} while (isDup);
			}
		}
		return locations;
	}
	
	private void positionInternlNodes(Node node) {
		if (!node.isLeaf()) {
			positionInternlNodes(node.getLeft());
			positionInternlNodes(node.getRight());
			
			if (Randomizer.nextBoolean()) {
				locations[node.getNr() * dim] = node.getLeft().getNr();
			} else {
				locations[node.getNr() * dim] = node.getRight().getNr();				
			}
		}
		
	}
	
	
    @Override
    public double[] getFrequencies() {
        return uniformfrequencies;
    }

	@Override
	public void getTransitionProbabilities(Node node, double fStartTime, double fEndTime, double fRate, double[] matrix) {
		matrix[0] = getLogTransitionProbability(node, fStartTime, fEndTime, fRate);
	}
	
	@Override
	public double getLogTransitionProbability(Node node, double fStartTime, double fEndTime, double fRate) {
        double time = (fStartTime - fEndTime) * fRate;
		
        int target = node.getNr();
		int source = node.getParent().getNr();
		target = location.getValue(target);
		source = location.getValue(source);
		if (source == target) {
			return 0;
		}
		double distance = distances[source][target];
		return getLogLForDistance(distance, time);
	}

	@Override
	public double getLogLikelihood(int source, int target, double time) {
		double distance = distances[source][target];
		return getLogLForDistance(distance, time);
	}	

	@Override
	public EigenDecomposition getEigenDecomposition(Node node) {
		return null;
	}


	@Override
	public boolean canHandleDataType(DataType dataType) {
		if (dataType.getStateCount() > 0) {
			return true;
		}
		return false;
	}

	double getLogLForDistance(double distance, double time) {
		if (distance == 0) {
			distance = 1e-5;
		}
		double inverseVariance = precision.getValue(0) / time;
        double logP = Math.log(distance) + 0.5 * Math.log(inverseVariance) -0.5 * distance * distance * inverseVariance;
        return logP;
	}


	public double [] getLogTransitionProbabilities(GraphNode startNode, double time) {
		double [] logP = new double[n];
		
		for (GraphNode node : graph.nodes) {
			double distance = graph.getDistance(startNode, node);
			logP[node.id] = getLogLForDistance(distance, time);
		}
		return logP;
	}	

	public double [] getTransitionProbabilities(GraphNode startNode, double time) {
		double [] logP = getLogTransitionProbabilities(startNode, time);
		logPtoP(logP);
		return logP;
	}

	

	@Override
	public double getTransitionProbabilities(int[] origRange, int[] destRange, double time, double[] probabilities) {
		int k = 0;
		double [] logP = new double[probabilities.length];
		for (int i = 0; i < origRange.length; i++) {
			int source = origRange[i];
			for (int j = 0; j < destRange.length; j++) {
				int target = destRange[j];
				if (source > 855 || target > 855) {
					int h = 3;
					 h++;
				}
				logP[k++] = getLogLikelihood(source, target, time);
			}
		}
		// normalise
		double logScale = logP[0];
		for (double d : logP) {
			logScale = Math.max(logScale, d);
		}
		// exponentiate
		for (int i = 0; i < probabilities.length; i++) {
			probabilities[i] = Math.exp(logP[i] - logScale);
			if (probabilities[i] < 1e-4) {
				probabilities[i] = 1e-4 / (logScale - logP[i]);
				
			}
		}
		return logScale;
	}

	
	double [] getPosition(int i) {
		return position[i];
	}

	int getSize() {
		return position.length;
	}
}
