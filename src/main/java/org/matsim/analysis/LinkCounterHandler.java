package org.matsim.analysis;

import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;

public class LinkCounterHandler implements LinkEnterEventHandler {

    // die Anzahl der Ereignisse zu zählen, bei denen ein Agent einen Link betritt

    public int counter = 0;

    @Override
    public void handleEvent(LinkEnterEvent linkEnterEvent) {
        counter ++;

    }
}
