/* *********************************************************************** *
 * project: org.matsim.*																															*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.prepare;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author ikaddoura
 *
 */
public class CreatePopulation {

	private static final Logger log = LogManager.getLogger(CreatePopulation.class);

	private int personCounter = 0;
	private final Map<String, SimpleFeature> features = new HashMap<>();
	private final Map<Id<Link>, Integer> linkId2numberOfVisitorsSerengetiParkplatz = new HashMap<>();
	private final Map<Id<Link>, Integer> linkId2numberOfVisitorsWasserland = new HashMap<>();
	private final Map<Id<Link>, Integer> linkId2numberOfVisitorsSerengetiPark = new HashMap<>();

	private final String activityType = "park";
	private final String serengetiParkplatzDestination = "serengetiParkplatz";
	private final String wasserlandParkplatzDestination = "wasserlandParkplatz";
	private final String serengetiParkDestination = "serengetiPark";

	private final String serengetiParkplatzShp = "./original-input-data/shp-files/serengeti-parkplatz/serengeti-parkplatz.shp";
	private final String wasserlandParkplatzShp = "./original-input-data/shp-files/wasserland-parkplatz/wasserland-parkplatz.shp";
	private final String serengetiParkShp = "./original-input-data/shp-files/serengeti-park/serengeti-park.shp";

	public static void main(String[] args) throws IOException {

		final String networkFile = "./scenarios/serengeti-park-v1.0/input/serengeti-park-network-v1.0.xml.gz";
		final String outputFilePopulation = "./scenarios/serengeti-park-v1.0/input/serengeti-park-population-v1.0.xml.gz";

		Config config = ConfigUtils.createConfig();
		config.network().setInputFile(networkFile);
		Scenario scenario = ScenarioUtils.loadScenario(config);

		CreatePopulation popGenerator = new CreatePopulation(1000, 675, 1569);
		popGenerator.run(scenario);	
		
		new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write(outputFilePopulation);
		log.info("Population written to: " + outputFilePopulation);
	
	}


	public CreatePopulation(int numberOfSafariVisitors, int safariParkplatzVisitors, int wasserlandParkplatzVisitors) throws IOException {
		
		// capacity 675
		linkId2numberOfVisitorsSerengetiParkplatz.put(Id.createLinkId("2344590910000r"), (int) (safariParkplatzVisitors * 0.8)); // Motorway
		linkId2numberOfVisitorsSerengetiParkplatz.put(Id.createLinkId("44371520007f"), (int) (safariParkplatzVisitors * 0.1)); // North
		linkId2numberOfVisitorsSerengetiParkplatz.put(Id.createLinkId("377320760000r"), (int) (safariParkplatzVisitors * 0.1)); // Hodenhagen
		
		// capacity 1569
		linkId2numberOfVisitorsWasserland.put(Id.createLinkId("2344590910000r"), (int) (wasserlandParkplatzVisitors * 0.8)); // Motorway
		linkId2numberOfVisitorsWasserland.put(Id.createLinkId("44371520007f"), (int) (wasserlandParkplatzVisitors * 0.1)); // North
		linkId2numberOfVisitorsWasserland.put(Id.createLinkId("377320760000r"), (int) (wasserlandParkplatzVisitors * 0.1)); // Hodenhagen
		
		linkId2numberOfVisitorsSerengetiPark.put(Id.createLinkId("2344590910000r"), (int) (numberOfSafariVisitors * 0.8)); // Motorway
		linkId2numberOfVisitorsSerengetiPark.put(Id.createLinkId("44371520007f"), (int) (numberOfSafariVisitors * 0.1)); // North
		linkId2numberOfVisitorsSerengetiPark.put(Id.createLinkId("377320760000r"), (int) (numberOfSafariVisitors * 0.1)); // Hodenhagen
		
		log.info("Reading shp files...");

		{
			SimpleFeatureIterator it = ShapeFileReader.readDataFile(serengetiParkplatzShp).getFeatures().features();
			while (it.hasNext()) {
				SimpleFeature ft = it.next();
				features.put(this.serengetiParkplatzDestination, ft);
			}
			it.close();
		}
		
		{
			SimpleFeatureIterator it = ShapeFileReader.readDataFile(wasserlandParkplatzShp).getFeatures().features();
			while (it.hasNext()) {
				SimpleFeature ft = it.next();
				features.put(this.wasserlandParkplatzDestination, ft);
			}
			it.close();
		}
		
		{
			SimpleFeatureIterator it = ShapeFileReader.readDataFile(serengetiParkShp).getFeatures().features();
			while (it.hasNext()) {
				SimpleFeature ft = it.next();
				features.put(this.serengetiParkDestination, ft);
			}
			it.close();
		}
		
		log.info("Reading shp files... Done.");
		
	}


	public Scenario run(Scenario scenario) {

		Random rnd = MatsimRandom.getRandom();

		for (Id<Link> linkId : linkId2numberOfVisitorsSerengetiParkplatz.keySet()) {
			createVisitors(scenario, rnd, linkId, linkId2numberOfVisitorsSerengetiParkplatz.get(linkId), this.serengetiParkplatzDestination);
		}	
		
		for (Id<Link> linkId : linkId2numberOfVisitorsWasserland.keySet()) {
			createVisitors(scenario, rnd, linkId, linkId2numberOfVisitorsWasserland.get(linkId), this.wasserlandParkplatzDestination);
		}
		
		for (Id<Link> linkId : linkId2numberOfVisitorsSerengetiPark.keySet()) {
			createVisitors(scenario, rnd, linkId, linkId2numberOfVisitorsSerengetiPark.get(linkId), this.serengetiParkDestination);
		}

		log.info("Population contains " + personCounter + " agents.");
		
		return scenario;
	}


	private void createVisitors(Scenario scenario, Random rnd, Id<Link> linkId, double odSum, String type) {
		Population population = scenario.getPopulation();
		PopulationFactory popFactory = population.getFactory();

		for (int i = 0; i < odSum; i++) {
			Person pers = popFactory.createPerson(Id.create("visitor_" + personCounter + "_" + linkId.toString() + "-" + type, Person.class));
			
			Plan plan = popFactory.createPlan();
						
			Activity startActivity = popFactory.createActivityFromCoord("home", scenario.getNetwork().getLinks().get(linkId).getFromNode().getCoord());

//			double startTime = calculateNormallyDistributedTime(11 * 3600., 1 * 3600.); // normally distributed
			double startTime = calculateRandomlyDistributedValue(11 * 3600., 1 * 3600.); // randomly distributed 

			startActivity.setEndTime(startTime);
			plan.addActivity(startActivity);

			Leg leg1 = popFactory.createLeg("car");
			plan.addLeg(leg1);

			Point endPoint = getRandomPointInFeature(rnd, features.get(type));
			if ( endPoint==null ) log.warn("Point is null.");
			
			Activity endActivity = popFactory.createActivityFromCoord(this.activityType, MGC.point2Coord(endPoint) ) ;
			plan.addActivity(endActivity);

			pers.addPlan(plan);
			population.addPerson(pers);

			pers.getAttributes().putAttribute("subpopulation", type);

			personCounter++;
		}
	}

	private static Point getRandomPointInFeature(Random rnd, SimpleFeature ft) {

		if (ft != null) {

			Point p;
			double x, y;
			do {
				x = ft.getBounds().getMinX() + rnd.nextDouble() * (ft.getBounds().getMaxX() - ft.getBounds().getMinX());
				y = ft.getBounds().getMinY() + rnd.nextDouble() * (ft.getBounds().getMaxY() - ft.getBounds().getMinY());
				p = MGC.xy2Point(x, y);
			} while (!((Geometry) ft.getDefaultGeometry()).contains(p));
			return p;

		} else {
			return null;
		}


	}

	private double calculateRandomlyDistributedValue(double i, double abweichung) {
		Random rnd = MatsimRandom.getRandom();
		double rnd1 = rnd.nextDouble();
		double rnd2 = rnd.nextDouble();

		double vorzeichen;
		if (rnd1 <= 0.5) {
			vorzeichen = -1.0;
		} else {
			vorzeichen = 1.0;
		}
		return (i + (rnd2 * abweichung * vorzeichen));
	}
	
	private double calculateNormallyDistributedTime(double mean, double stdDev) {
		Random random = MatsimRandom.getRandom();
		boolean leaveLoop = false;
		double endTimeInSec = Double.MIN_VALUE;

		while (!leaveLoop) {
			double normal = random.nextGaussian();
			endTimeInSec = mean + stdDev * normal;

			if (endTimeInSec >= 9. * 3600 && endTimeInSec <= 13. * 3600.) {
				leaveLoop = true;
			}
		}

		if (endTimeInSec < 0. || endTimeInSec > 24. * 3600) {
			throw new RuntimeException("Shouldn't happen. Aborting...");
		}
		return endTimeInSec;
	}
	
}
