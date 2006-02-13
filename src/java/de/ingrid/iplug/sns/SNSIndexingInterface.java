/*
 * Copyright (c) 1997-2006 by media style GmbH
 */

package de.ingrid.iplug.sns;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.slb.taxi.webservice.xtm.stubs._topicMapFragment;
import com.slb.taxi.webservice.xtm.stubs.xtm._occurrence;
import com.slb.taxi.webservice.xtm.stubs.xtm._resourceData;
import com.slb.taxi.webservice.xtm.stubs.xtm._topic;

/**
 * An interface to the SN Service. It is mainly used to get some data for indexing.
 */
public class SNSIndexingInterface {

    private SNSClient fSNSClient;

    private final Pattern fCoordPattern = Pattern.compile("^(.*),(.*) (.*),(.*)$");

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
     * All buzzwords to the given document. You must call this method first to get results from
     * <code>getReferencesToTime</code> and <code>getReferencesToSpace</code>.
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
            _occurrence[] occ = this.fTopics[i].getOccurrence();
            if (null != occ) {
                final String baseName = this.fTopics[i].getBaseName(0).getBaseNameString().getValue();
                final Temporal temporal = new Temporal();

                for (int k = 0; k < occ.length; k++) {
                    final _resourceData data = occ[k].getResourceData();
                    if (data != null) {
                        final String topicRef = occ[k].getInstanceOf().getTopicRef().getHref();
                        if (topicRef.endsWith("temporalFromOcc")) {
                            final String date = data.getValue();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
                            Date javaDate = sdf.parse(date);
                            temporal.setFrom(javaDate);
                        } else if (topicRef.endsWith("temporalAtOcc")) {
                            new Date();
                            final String date = data.getValue();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
                            Date javaDate = sdf.parse(date);
                            temporal.setAt(javaDate);
                        } else if (topicRef.endsWith("temporalToOcc")) {
                            final String date = data.getValue();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
                            Date javaDate = sdf.parse(date);
                            temporal.setTo(javaDate);
                        } else if (topicRef.endsWith("wgs84BoxOcc")) {
                            final String coords = data.getValue();

                            Matcher m = this.fCoordPattern.matcher(coords);
                            if (m.matches() && m.groupCount() == 4) {
                                final double x1 = new Double(m.group(1)).doubleValue();
                                final double x2 = new Double(m.group(2)).doubleValue();
                                final double y1 = new Double(m.group(3)).doubleValue();
                                final double y2 = new Double(m.group(4)).doubleValue();
                                this.fWgs84Box.add(new Wgs84Box(baseName, x1, x2, y1, y2));
                            }
                        }
                    }
                }
                if (!temporal.isEmpty()) {
                    this.fTemporal.add(temporal);
                }
            }
        }
    }

    /**
     * All time references to a document that is analyzed by <code>getBuzzwords</code>.
     * 
     * @return Array of strings. It is empty if nothing is found.
     * @throws Exception
     *             If we cannot connect to the sns server.
     */
    public Temporal[] getReferencesToTime() throws Exception {
        if (this.fTemporal.isEmpty()) {
            getReferences();
        }

        return (Temporal[]) this.fTemporal.toArray(new Temporal[this.fTemporal.size()]);
    }

    /**
     * All coordinate references to a document that is analyzed by <code>getBuzzwords</code>.
     * 
     * @return Array of strings. It is empty if nothing is found.
     * @throws Exception
     *             If we cannot connect to the sns server.
     */
    public Wgs84Box[] getReferencesToSpace() throws Exception {
        if (this.fTemporal.isEmpty()) {
            getReferences();
        }

        return (Wgs84Box[]) this.fWgs84Box.toArray(new Wgs84Box[this.fWgs84Box.size()]);
    }
}
