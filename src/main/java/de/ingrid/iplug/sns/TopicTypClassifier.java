package de.ingrid.iplug.sns;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TopicTypClassifier {

    private static final Set<String> _locationTypeIds = new HashSet<String>(Arrays.asList("nationType", "use2Type", "use3Type", "use4Type", "use5Type", "use6Type", "urbanDistrictType", "quarterType",
            "catchmentAreaType", "islandType", "landscapeType", "archipelagoType", "mountainType", "mountainsType", "naturalLandscapeType", "biosphereType", "nationalParkType", "naturalParkType",
            "protectedAreaType", "channelType", "lakeType", "reservoirType", "riverType", "seaType"));

    /*
    public List<Topic> getLocationTopics(Topic[] topics) {
        if (topics != null) {
            return getLocationTypes(topics, _locationTypeIds);
        }

        return new ArrayList<Topic>();
    }

    private List<Topic> getLocationTypes(Topic[] topics, Set<String> targetTopicTypes) {
        List<Topic> ret = new ArrayList<Topic>();

        for (Topic topic : topics) {
            String instanceOfTopic = topic.getInstanceOf()[0].getTopicRef().getHref();

            for (String targetType : targetTopicTypes) {
                if (instanceOfTopic.endsWith(targetType)) {
                    ret.add(topic);
                    break;
                }
            }
        }
        return ret;
    }
    */
}
