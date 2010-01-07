package de.ingrid.iplug.sns;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.slb.taxi.webservice.xtm.stubs.xtm.BaseName;
import com.slb.taxi.webservice.xtm.stubs.xtm.BaseNameString;
import com.slb.taxi.webservice.xtm.stubs.xtm.InstanceOf;
import com.slb.taxi.webservice.xtm.stubs.xtm.Topic;
import com.slb.taxi.webservice.xtm.stubs.xtm.TopicRef;

public class TopicTypClassifierTest extends TestCase {

    private final String[] _allTopicInstanceOfTypes = { "/event/activity", "/event/anniversary", "/event/conference", "/event/disaster", "/event/historical", "/event/interYear", "/event/legal",
            "/event/observation", "/event/natureOfTheYear", "/event/publication", "/location/admin/nation", "/location/admin/use2", "/location/admin/use3", "/location/admin/use4",
            "/location/admin/use5", "/location/admin/use6", "/location/admin/urbanDistrict", "/location/admin/quarter", "/location/land/catchmentArea", "/location/land/island",
            "/location/land/landscape", "/location/land/landscape/archipelago", "/location/land/landscape/mountain", "/location/land/landscape/mountains", "/location/land/naturalLandscape",
            "/location/protected/biosphere", "/location/protected/nationalPark", "/location/protected/naturalPark", "/location/protected/protectedArea", "/location/waters/channel",
            "/location/waters/lake", "/location/waters/reservoir", "/location/waters/river", "/location/waters/sea", "/thesa/descriptor", "/thesa/descriptor/nodeLabel",
            "/thesa/descriptor/nodeLabel/topTerm", "/thesa/nonDescriptor" };

    private List<Topic> _testTopics;
    
    public void setUp() {
        _testTopics = new ArrayList<Topic>();

        for(String type : _allTopicInstanceOfTypes){
            final TopicRef topicRef = new TopicRef();
            topicRef.setHref("http://www.semantic-network.de/xmlns/XTM/2005/2.0/sns-classes_2.0.xtm#"+type+"Type");
            final InstanceOf instanceOf = new InstanceOf();
            instanceOf.setTopicRef(topicRef);
            final BaseName baseName = new BaseName();
            baseName.setBaseNameString(new BaseNameString("type: "+type));
            final Topic newTopic = new Topic();
            newTopic.setInstanceOf(new InstanceOf[]{instanceOf});
            newTopic.setBaseName(new BaseName[]{baseName});
            _testTopics.add(newTopic);
        }
    }

    public void testGetLocationTopics() {

        TopicTypClassifier topicTypeClassifier = new TopicTypClassifier();

        List<Topic> locationTopics = topicTypeClassifier.getLocationTopics(_testTopics.toArray(new Topic[0]));

        assertEquals(24, locationTopics.size());
    }
}
