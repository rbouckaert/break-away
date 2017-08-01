package beast.geo;


import beast.core.Description;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.parameter.IntegerParameter;
import beast.core.parameter.RealParameter;
import beast.evolution.likelihood.GenericTreeLikelihood;

@Description("For letting TreeWithMetaDataLogger know about lat/longs of nodes in trees")
public class LocationToLatLong2 extends RealParameter {
	public Input<DistanceBasedDiffusionModel> graphInput = new Input<DistanceBasedDiffusionModel>("model","DistanceBasedDiffusionModel containing nodes at lat/long to be logged", Validate.REQUIRED);

	public Input<IntegerParameter> locationInput = new Input<IntegerParameter>("location", "mapping of tree nodes to graph nodes");
	public Input<PositionProvider> likelihoodInput = new Input<>("likelihood", "Trait Likelihood for logging locations", Validate.XOR, locationInput);
	
	final public Input<Double> longitudeThresholdInput = new Input<>("longitudeThreshold", "longitudes below this threshold will get 360 added. "
			+ "This is useful for calculating the mean location when a point jumps the boundary of the world map.", -180.0);

	
	DistanceBasedDiffusionModel model;
	IntegerParameter location;
	PositionProvider likelihood;
	int dim = 2;
	
	@Override
	public void initAndValidate() {
		super.initAndValidate();
		model = graphInput.get();
		location = locationInput.get();
		if (location != null) {
			dim = location.getMinorDimension1();
		}
		likelihood = likelihoodInput.get();
		if (likelihood != null) {
			dim = 2;
		}
		
		setDimension(2*model.getSize());
		setMinorDimension(2);
	}
	
	@Override
	public Double getValue(int iParam) {
		int i = iParam/2;
		int j = iParam % 2;
		int k = location.getValue(i * dim);
		double [] c = model.getPosition(k);
		if (j == 0) {
			return c[0];
		} else {
			return c[j];
		}
	}

	@Override
	public Double getMatrixValue(int i, int j) {
		int k = -1;
		if (location != null) {
			k = location.getValue(i * dim);
		} else {
			k = likelihood.getPosition(i);
		}
		double [] c = model.getPosition(k);
		if (j == 0) {
			return c[0];
		} else {
			if (c[1] < longitudeThresholdInput.get()) {
				c[1] += 360;
			}
			return c[1];
		}
	}

}
