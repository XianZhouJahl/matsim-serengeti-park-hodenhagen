package org.matsim.analysis;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.network.Link;

import java.util.HashMap;
import java.util.Map;

public class LinkEventHandler implements LinkLeaveEventHandler {

    private static final Id<Link> linkOfInterest = Id.createLinkId("7232382780000f");
    private final Map<String, Integer> volumes = new HashMap<>();

    public Map<String, Integer> getVolumes() {
        return volumes;
    }

    private String getKey(double time){
        int timeInHour = (int) (time/3600);
        return Integer.toString(timeInHour);
    }

    @Override
    public void handleEvent(LinkLeaveEvent linkLeaveEvent) {
        if (linkLeaveEvent.getLinkId().equals(linkOfInterest)){
            String time = getKey(linkLeaveEvent.getTime());
            int currentVolume = volumes.getOrDefault(time, 0);
            int newVolume = currentVolume + 1;
            volumes.put(time, newVolume);


        }


    }



}
