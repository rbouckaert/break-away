/*
 * GreatCircleDiffusionModel.java
 *
 * Copyright (C) 2002-2009 Alexei Drummond and Andrew Rambaut
 *
 * This file is part of BEAST.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * BEAST is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * BEAST is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BEAST; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package beast.geo;




import beast.core.Description;

@Description("math on a sphere")
public class SphericalMath  {


	/** Convert spherical coordinates (latitude,longitude) in degrees on unit sphere 
	 * to Cartesian (x,y,z) coordinates **/
	public static double [] spherical2Cartesian(double fLat, double fLong) {
		double fPhi = (fLong * Math.PI / 180.0);
		double fTheta = (90 - fLat) * Math.PI / 180.0;
	    //{x}=\rho \, \sin\theta \, \cos\phi  
	    //{y}=\rho \, \sin\theta \, \sin\phi  
	    //{z}=\rho \, \cos\theta 
		double [] fNorm = new double[3];
		fNorm[0] = Math.sin(fTheta) * Math.cos(fPhi);
		fNorm[1] = Math.sin(fTheta) * Math.sin(fPhi);
		fNorm[2] = Math.cos(fTheta);
		return fNorm;
	} // spherical2Cartesian
	
	/** inverse of spherical2Cartesian **/
	public static double [] cartesian2Sperical(double[] f3dRotated2) {
		return 	new double[]{
				Math.acos(-f3dRotated2[2]) * 180/Math.PI - 90,
				Math.atan2(f3dRotated2[1], f3dRotated2[0]) * 180.0/Math.PI
		};
	}

	public static double [] reverseMap(double fLat, double fLong, double fLatT, double fLongT) {
		// from spherical to Cartesian coordinates
		double [] f3DPoint = spherical2Cartesian(fLong, fLat);
		// rotate, first latitude, then longitude
		double [] f3DRotated = new double[3];
		double fC = Math.cos(fLongT * Math.PI / 180);
		double fS = Math.sin(fLongT * Math.PI / 180);
		double [] f3DRotated2 = new double[3];
		double fC2 = Math.cos(-fLatT * Math.PI / 180);
		double fS2 = Math.sin(-fLatT * Math.PI / 180);

		// rotate over latitude
		f3DRotated[0] = f3DPoint[0] * fC2 + f3DPoint[2] * fS2;
		f3DRotated[1] = f3DPoint[1];
		f3DRotated[2] = -f3DPoint[0] * fS2 + f3DPoint[2] * fC2;

		// rotate over longitude
		f3DRotated2[0] = f3DRotated[0] * fC - f3DRotated[1] * fS; 
		f3DRotated2[1] = f3DRotated[0] * fS + f3DRotated[1] * fC; 
		f3DRotated2[2] = f3DRotated[2]; 

		double [] point = cartesian2Sperical(f3DRotated2); 
		return point;
	} // map
	
	/** convert spherical coordinates (latitude,longitude) to
	 * 2D point in interval [-180, -90] x [180, 90] 
	 * wrt plane defined by fNorm
	 * 
	 * http://en.wikipedia.org/wiki/Sinusoidal_projection
	 *
	 * Alternatives:
	 * http://en.wikipedia.org/wiki/Hammer_projection <= looks very promissing, comes with inverse
	 * http://en.wikipedia.org/wiki/Category:Equal-area_projections
	 */
	public static double [] map(double fLat, double fLong, double fLatT, double fLongT) {
		// from spherical to Cartesian coordinates
		double [] f3DPoint = spherical2Cartesian(fLong, fLat);
		// rotate, first longitude, then latitude
		double [] f3DRotated = new double[3];
		double fC = Math.cos(-fLongT * Math.PI / 180);
		double fS = Math.sin(-fLongT * Math.PI / 180);
		double [] f3DRotated2 = new double[3];
		double fC2 = Math.cos(fLatT * Math.PI / 180);
		double fS2 = Math.sin(fLatT * Math.PI / 180);

		// rotate over longitude
		f3DRotated[0] = f3DPoint[0] * fC - f3DPoint[1] * fS; 
		f3DRotated[1] = f3DPoint[0] * fS + f3DPoint[1] * fC; 
		f3DRotated[2] = f3DPoint[2]; 

		// rotate over latitude
//		f3DRotated2 = f3DRotated;
		f3DRotated2[0] = f3DRotated[0] * fC2 + f3DRotated[2] * fS2; 
		f3DRotated2[1] = f3DRotated[1];
		f3DRotated2[2] = -f3DRotated[0] * fS2 + f3DRotated[2] * fC2;
		
		// translate back to (longitude, latitude)
		double [] point = cartesian2Sperical(f3DRotated2); 
		return point;
	} // map
    
}
