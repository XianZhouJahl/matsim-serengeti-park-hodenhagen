package org.matsim.prepare;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.*;
import org.matsim.vehicles.MatsimVehicleWriter;
import org.matsim.vehicles.VehicleType;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

public class addAddizionalStopZooShuttle {
    private static final NetworkFactory networkFactory = NetworkUtils.createNetwork().getFactory();
    private static final TransitScheduleFactory scheduleFactory = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getTransitSchedule().getFactory();

    public static void main(String[] args) {


        // read in network and create scenario
        var root = Paths.get("./scenarios/serengeti-park-v1.0/input");
        var network = NetworkUtils.readNetwork(root.resolve("serengeti-park-network-v1.0.xml.gz").toString());
        var scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

        // create vehicle type
        var vehicleType = scenario.getVehicles().getFactory().createVehicleType(Id.create("shuttle", VehicleType.class));
        vehicleType.setLength(20);
        vehicleType.setPcuEquivalents(2);
        vehicleType.setMaximumVelocity(36);
        vehicleType.setNetworkMode(TransportMode.pt);
        vehicleType.setDescription("shuttle vehicle type");
        vehicleType.getCapacity().setSeats(10000);
        vehicleType.getCapacity().setStandingRoom(0);
        // add to our scenario
        scenario.getTransitVehicles().addVehicleType(vehicleType);


        // create pt nodes and links --> adapt network
        var startNode = network.getFactory().createNode(Id.createNodeId("pt_start"), new Coord(544006.781992937 + 100, 5847658.641000098 + 100));
        network.addNode(startNode);

        var fromNode = network.getNodes().get(Id.createNodeId("29589035"));
        var toNode = network.getNodes().get(Id.createNodeId("3667621813"));
        var toNode1 = network.getNodes().get(Id.createNodeId("3593964470"));
        var endNode = network.getFactory().createNode(Id.createNodeId("pt_end"), new Coord(544006.781992937 - 100, 5847658.641000098 - 100));
        network.addNode(endNode);


        var startLink = createLink("pt_1", startNode, fromNode);
        var connection = createLink("pt_2", fromNode, toNode);
        var connection1 = createLink("pt_3", toNode, toNode1);
        var endLink = createLink("pt_4", toNode1, endNode);


        network.addLink(connection);
        network.addLink(connection1);
        network.addLink(startLink);
        network.addLink(endLink);

        // create TransitStopFacility ( physical bus staion)
        var fromStopFacility = scheduleFactory.createTransitStopFacility(Id.create("Stop_1", TransitStopFacility.class), startNode.getCoord(), false);
        fromStopFacility.setLinkId(startLink.getId());


        var toStopFacility = scheduleFactory.createTransitStopFacility(Id.create("Stop_2", TransitStopFacility.class), endNode.getCoord(), false);
        toStopFacility.setLinkId(endLink.getId());

        var toStopFacility1 = scheduleFactory.createTransitStopFacility(Id.create("Stop_3", TransitStopFacility.class), toNode.getCoord(), false);
        toStopFacility1.setLinkId(connection.getId());

        var toStopFacility2 = scheduleFactory.createTransitStopFacility(Id.create("Stop_4", TransitStopFacility.class), toNode1.getCoord(), false);
        toStopFacility2.setLinkId(connection1.getId());


        scenario.getTransitSchedule().addStopFacility(fromStopFacility);
        scenario.getTransitSchedule().addStopFacility(toStopFacility);
        scenario.getTransitSchedule().addStopFacility(toStopFacility1);
        scenario.getTransitSchedule().addStopFacility(toStopFacility2);

        // create TransitRouteStop
        var fromStop = scheduleFactory.createTransitRouteStop(fromStopFacility, 0, 10); // offset
        // offset gibt an, dass es sich um die Zeitdifferenz zur geplanten Abfahrtszeit der Route handelt.

        var toStop = scheduleFactory.createTransitRouteStop(toStopFacility, 1000, 1010);

        var toStop1 = scheduleFactory.createTransitRouteStop(toStopFacility1, 2000, 2010);

        var toStop2 = scheduleFactory.createTransitRouteStop(toStopFacility2, 3600, 3610);







        // create TransitRoute smaller than transit line
        var networkRoute = RouteUtils.createLinkNetworkRouteImpl(startLink.getId(), List.of(connection.getId(), connection1.getId()), endLink.getId());
        var route = scheduleFactory.createTransitRoute(Id.create("route-1", TransitRoute.class), networkRoute, List.of(fromStop,toStop1, toStop2, toStop), "pt");

        // create Departures & corresponding Vehicles
        for (int i = 9 * 3600; i < 13 * 3600; i += 300) {
            var departure = scheduleFactory.createDeparture(Id.create("departure_" + i, Departure.class), i);
            var vehicle = scenario.getTransitVehicles().getFactory().createVehicle(Id.createVehicleId("shuttle_vehicle_" + i), vehicleType);
            departure.setVehicleId(vehicle.getId());

            scenario.getTransitVehicles().addVehicle(vehicle);
            route.addDeparture(departure);
        }

        // create TransitLine
        var line = scheduleFactory.createTransitLine(Id.create("Shuttle", TransitLine.class));
        line.addRoute(route);
        scenario.getTransitSchedule().addTransitLine(line);

        // export input files required for simulation.
        new NetworkWriter(network).write(root.resolve("network-with-pt.xml.gz").toString());
        new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile(root.resolve("transit-Schedule_test.xml.gz").toString());
        new MatsimVehicleWriter(scenario.getTransitVehicles()).writeFile(root.resolve("transit-vehicles_test.xml.gz").toString());
    }

    private static Link createLink(String id, Node from, Node to) {

        var link = networkFactory.createLink(Id.createLinkId(id), from, to);
        link.setAllowedModes(Set.of(TransportMode.pt));
        link.setFreespeed(100);
        link.setCapacity(10000);
        return link;
    }
}