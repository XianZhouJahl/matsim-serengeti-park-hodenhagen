package org.matsim.analysis;

import org.matsim.core.events.EventsUtils;

public class StartAnalysis {
    public static void main(String[] args) {
        // Die Verwendung von var anstelle der expliziten Typdeklaration wie EventsManager ist
        // eine Frage des persönlichen Stils und der Lesbarkeit des Codes.
        // In Java ab Version 10 ist var eine sogenannte lokale Typinferenz,
        // mit der der Compiler den Typ einer Variablen anhand des zugewiesenen Ausdrucks ableiten kann.
        var handler = new LinkCounterHandler();
        var manager = EventsUtils.createEventsManager();
        manager.addHandler(handler);

        var linkFlowHourly = new LinkEventHandler();
        manager.addHandler(linkFlowHourly);


        EventsUtils.readEvents(manager, "C:/Users/charl/Documents/TUB/SS24/MATSim/seminar/matsim-serengeti-park-hodenhagen/scenarios/serengeti-park-v1.0/output/output-serengeti-park-v1.0-run1/serengeti-park-v1.0-run1.output_events.xml.gz");
        System.out.println(handler.counter);
        System.out.println(linkFlowHourly.getVolumes().entrySet());



    }
}
