/*
 * Copyright (c) 1997-2006 by media style GmbH
 */

package de.ingrid.iplug.sns;

import java.util.ArrayList;

import com.slb.taxi.webservice.xtm.stubs._topicMapFragment;
import com.slb.taxi.webservice.xtm.stubs.xtm._occurrence;
import com.slb.taxi.webservice.xtm.stubs.xtm._resourceData;
import com.slb.taxi.webservice.xtm.stubs.xtm._topic;

/**
 * An interface to the SN Service. It is mainly used to get some data for indexing.
 */
public class SNSIndexingInterface {

    private SNSClient fSNSClient;

    private _topic[] fTopics = new _topic[0];

    private ArrayList fTemporal = new ArrayList();

    private ArrayList fWgs84Box = new ArrayList();

    /**
     * Interface for SN service connection handling.
     * 
     * @param login
     *            Username for the SN service.
     * @param password
     *            Password for the SN service.
     * @param language
     *            The language the result should be e.g. "de".
     * @throws Exception
     *             If we cannot connect to the sns server.
     */
    public SNSIndexingInterface(final String login, final String password, final String language) throws Exception {
        this.fSNSClient = new SNSClient(login, password, language);
    }

    /**
     * All buzzwords to the given document.
     * 
     * @param text
     *            The document to analyze.
     * @param maxToAnalyzeWords
     *            The first <code>maxToAnalyzeWords</code> words of the document that should be analyzed.
     * @return A string array filled with all buzzwords.
     * @throws Exception
     *             If we cannot connect to the sns server.
     */
    public String[] getBuzzwords(final String text, final int maxToAnalyzeWords) throws Exception {
        String[] result = new String[0];

        final _topicMapFragment mapFragment = this.fSNSClient.autoClassify(text, maxToAnalyzeWords);
        this.fTopics = mapFragment.getTopicMap().getTopic();

        this.fTemporal.clear();
        this.fWgs84Box.clear();

        result = getBasenames(this.fTopics);

        return result;
    }

    private String[] getBasenames(_topic[] topics) {
        ArrayList result = new ArrayList();

        for (int i = 0; i < topics.length; i++) {
            result.add(topics[i].getBaseName(0).getBaseNameString().getValue());
        }

        return (String[]) result.toArray(new String[result.size()]);
    }

    private void getReferences() throws Exception {
        for (int i = 0; i < this.fTopics.length; i++) {
            final _topicMapFragment mapFragment = this.fSNSClient.getPSI(this.fTopics[i].getId(), 3);
            _topic[] topics = mapFragment.getTopicMap().getTopic();

            for (int j = 0; j < topics.length; j++) {
                _occurrence[] occ = topics[j].getOccurrence();
                if (null != occ) {
                    for (int k = 0; k < occ.length; k++) {
                        final _resourceData data = occ[k].getResourceData();
                        if (data != null) {
                            final String topicRef = occ[k].getInstanceOf().getTopicRef().getHref();
                            if (topicRef.endsWith("temporalAtOcc") || topicRef.endsWith("temporalFromOcc")
                                    || topicRef.endsWith("temporalToOcc")) {
                                this.fTemporal.add(data.getValue());
                            } else if (topicRef.endsWith("wgs84BoxOcc")) {
                                this.fWgs84Box.add(data.getValue());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * All references to a time.
     * 
     * @return Array of strings. It is empty if nothing is found.
     * @throws Exception
     *             If we cannot connect to the sns server.
     */
    public String[] getReferencesToTime() throws Exception {
        if (this.fTemporal.isEmpty()) {
            getReferences();
        }
        return (String[]) this.fTemporal.toArray(new String[this.fTemporal.size()]);
    }

    /**
     * All references to a coordinates.
     * 
     * @return Array of strings. It is empty if nothing is found.
     * @throws Exception
     *             If we cannot connect to the sns server.
     */
    public String[] getReferencesToSpace() throws Exception {
        if (this.fTemporal.isEmpty()) {
            getReferences();
        }
        return (String[]) this.fWgs84Box.toArray(new String[this.fWgs84Box.size()]);
    }
}
