package breakaway.geo;

import beast.base.core.Citation;
import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Input.Validate;
import beast.base.evolution.branchratemodel.BranchRateModel;
import beast.base.evolution.likelihood.GenericTreeLikelihood;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;

@Description("Treelikelihood that provides positions and calculates likelihood based on a graph based substitution model")
@Citation(value="Remco R. Bouckaert, Claire Bowern, Quentin D. Atkinson. "
		+ "The origin and expansion of Pama-Nyungan languages across Australia. "
		+ "Nature Ecology and Evolution, 2, 741–749 (2018)", year=2018, DOI="doi:10.1038/s41559-018-0489-3")
public class SampledTraitLikelihood extends GenericTreeLikelihood implements PositionProvider {
	public Input<GraphSubstitutionModel> modelInput = new Input<GraphSubstitutionModel>("model" , "model for the diffusion of the trait",Validate.REQUIRED);

	GraphSubstitutionModel model;
	Tree tree;
	BranchRateModel clockModel;
	double [] partialLogP;
	double [] storedPartialLogP;

	public SampledTraitLikelihood() {
		dataInput.setRule(Validate.OPTIONAL);
		siteModelInput.setRule(Validate.OPTIONAL);
	}
	
	@Override
	public void initAndValidate() {
		model = modelInput.get();
		tree = (Tree) treeInput.get();
		partialLogP = new double[tree.getNodeCount()];
		storedPartialLogP = new double[tree.getNodeCount()];
		clockModel = branchRateModelInput.get();
	}	
	
	@Override
	public double calculateLogP() {
		logP = 0;
		for (Node node : tree.getNodesAsArray()) {
			if (!node.isRoot()) {
				double rate = clockModel.getRateForBranch(node);
				double p = model.getLogTransitionProbability(node, node.getParent().getHeight(), node.getHeight(), rate);
				if (Double.isNaN(p) || Double.isInfinite(p)) {
					p = -1000;
				}
				partialLogP[node.getNr()] = p;				
				logP += p;
			} else {
				// handle root location
				double p = model.getLogRootFrequency(node);
				partialLogP[node.getNr()] = p;				
				logP += p;
			}
		}
		return logP;
	}

	@Override
	public void store() {
		System.arraycopy(partialLogP, 0, storedPartialLogP, 0, partialLogP.length);
		super.store();
	}
	
	@Override
	public void restore() {
		double [] tmp = partialLogP; partialLogP = storedPartialLogP; storedPartialLogP = tmp;
		super.restore();
	}

	@Override
	public int getPosition(int dim) {
		return model.location.getValue(dim);
	}
	
}
