package org.openstreetmap.josm;

import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.data.projection.Puwg;

public class Main {
	private static Projection proj = new Puwg();

	public static Projection getProjection() {
		return proj;
	}

}
