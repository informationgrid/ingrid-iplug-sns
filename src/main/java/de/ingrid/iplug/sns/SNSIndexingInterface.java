/*
 * Copyright (c) 1997-2006 by media style GmbH
 */

package de.ingrid.iplug.sns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.slb.taxi.webservice.xtm.stubs.TopicMapFragment;
import com.slb.taxi.webservice.xtm.stubs.xtm.Occurrence;
import com.slb.taxi.webservice.xtm.stubs.xtm.ResourceData;
import com.slb.taxi.webservice.xtm.stubs.xtm.Topic;

import de.ingrid.external.sns.SNSClient;
import de.ingrid.utils.tool.SNSUtil;

/**
 * An interface to the SN Service. It is mainly used to get some data for indexing.
 */
public class SNSIndexingInterface {

    private SNSClient fSNSClient;

    private final Pattern fCoordPattern = Pattern.compile("^(.*),(.*) (.*),(.*)$");

    private final Pattern fDateYearPattern = Pattern.compile("^[0-9]{4,4}$");

    private final Pattern fDateYearMonthPattern = Pattern.compile("^[0-9]{4,4}-[0-9]{2,2}$");

    private final Pattern fDateYearMonthDayPattern = Pattern.compile("^[0-9]{4,4}-[0-9]{2,2}-[0-9]{2,2}$");

    private Topic[] fTopics = new Topic[0];

    private List<Temporal> fTemporal = new ArrayList<Temporal>();

    private List<Wgs84Box> fWgs84Box = new ArrayList<Wgs84Box>();

    private String fLanguage;

    private List<String> fTopicIds = new ArrayList<String>();

    private String fGemeindeKennzifferPrefix = "ags:";

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
        this.fLanguage = language;
    }

    /**
     * Allows to set the timeout for the sn-service connection.
     * 
     * @param timeout
     *            The timeout in milliseconds.
     */
    public void setTimeout(final int timeout) {
        this.fSNSClient.setTimeout(timeout);
    }

    /**
     * @param prefix
     */
    public void setGemeindeKennzifferPrefix(String prefix) {
        this.fGemeindeKennzifferPrefix = prefix;
    }

    /**
     * All buzzwords to the given document. You must call this method first to get results from
     * <code>getReferencesToTime</code> and <code>getReferencesToSpace</code>.
     * 
     * @param text
     *            The document to analyze.
     * @param maxToAnalyzeWords
     *            The first <code>maxToAnalyzeWords</code> words of the document that should be
     *            analyzed.
     * @param ignoreCase
     *            Set to true ignore case of the document.
     * @return A string array filled with all buzzwords.
     * @throws Exception
     *             If we cannot connect to the sns server.
     */
    public String[] getBuzzwords(final String text, final int maxToAnalyzeWords, boolean ignoreCase) throws Exception {
        return getBuzzwords(text, maxToAnalyzeWords, ignoreCase, this.fLanguage);
    }

    /**
     * All buzzwords to the given document. You must call this method first to get results from
     * <code>getReferencesToTime</code> and <code>getReferencesToSpace</code>.
     * 
     * @param text
     *            The document to analyze.
     * @param maxToAnalyzeWords
     *            The first <code>maxToAnalyzeWords</code> words of the document that should be
     *            analyzed.
     * @param ignoreCase
     *            Set to true ignore case of the document.
     * @param language
     *            The language the text is encoded.
     * @return A string array filled with all buzzwords.
     * @throws Exception
     *             If we cannot connect to the sns server.
     */
    public String[] getBuzzwords(final String text, final int maxToAnalyzeWords, boolean ignoreCase, String language) throws Exception {
        String[] result = new String[0];

        final TopicMapFragment mapFragment = this.fSNSClient.autoClassify(text, maxToAnalyzeWords, null, ignoreCase, language);
        this.fTopics = mapFragment.getTopicMap().getTopic();

        this.fTopicIds.clear();
        this.fTemporal.clear();
        this.fWgs84Box.clear();

        if (null != this.fTopics) {
            result = getBasenames(this.fTopics);
        }

        return result;
    }

    /**
     * All buzzwords to the given URL. You must call this method first to get results from
     * <code>getReferencesToTime</code> and <code>getReferencesToSpace</code>.
     * 
     * @param url
     *            The url to analyze.
     * @param maxToAnalyzeWords
     *            The first <code>maxToAnalyzeWords</code> words of the document that should be
     *            analyzed.
     * @param ignoreCase
     *            Set to true ignore case of the document.
     * @param language
     *            The language the text is encoded.
     * @return A string array filled with all buzzwords.
     * @throws Exception
     *             If we cannot connect to the sns server.
     */
    public String[] getBuzzwordsToUrl(final String url, final int maxToAnalyzeWords, boolean ignoreCase, String language) throws Exception {
        String[] result = new String[0];

        final TopicMapFragment mapFragment = this.fSNSClient.autoClassifyToUrl(url, maxToAnalyzeWords, null, ignoreCase, language);
        this.fTopics = mapFragment.getTopicMap().getTopic();

        this.fTopicIds.clear();
        this.fTemporal.clear();
        this.fWgs84Box.clear();

        if (null != this.fTopics) {
            result = getBasenames(this.fTopics);
        }

        return result;
    }

    private String[] getBasenames(Topic[] topics) {
        List<String> result = new ArrayList<String>();

        for (int i = 0; i < topics.length; i++) {
            result.add(topics[i].getBaseName(0).getBaseNameString().get_value());
        }

        return result.toArray(new String[result.size()]);
    }

    private void getReferences() throws Exception, ParseException {
        if (this.fTopics != null) {
            for (int i = 0; i < this.fTopics.length; i++) {
                Occurrence[] occ = this.fTopics[i].getOccurrence();
                if (null != occ) {
                    final String baseName = this.fTopics[i].getBaseName(0).getBaseNameString().get_value();
                    this.fTopicIds.add(this.fTopics[i].getId());
                    final Temporal temporal = new Temporal();
                    final Wgs84Box wgs84Box = new Wgs84Box(baseName, 0, 0, 0, 0, "");
                    boolean wgs84BoxSet = false;
                    for (int k = 0; k < occ.length; k++) {
                        final ResourceData data = occ[k].getResourceData();
                        if (data != null) {
                            final String topicRef = occ[k].getInstanceOf().getTopicRef().getHref();
                            if (topicRef.endsWith("temporalFromOcc")) {
                                final String date = data.get_value();

                                Date javaDate = parseDate(date);
                                temporal.setFrom(javaDate);
                            } else if (topicRef.endsWith("temporalAtOcc")) {
                                final String date = data.get_value();

                                Date javaDate = parseDate(date);
                                temporal.setAt(javaDate);
                            } else if (topicRef.endsWith("temporalToOcc")) {
                                final String date = data.get_value();

                                Date javaDate = parseDate(date);
                                temporal.setTo(javaDate);
                            } else if (topicRef.endsWith("wgs84BoxOcc")) {
                                final String coords = data.get_value();
                                Matcher m = this.fCoordPattern.matcher(coords);
                                if (m.matches() && m.groupCount() == 4) {
                                    final double x1 = new Double(m.group(1)).doubleValue();
                                    final double y1 = new Double(m.group(2)).doubleValue();
                                    final double x2 = new Double(m.group(3)).doubleValue();
                                    final double y2 = new Double(m.group(4)).doubleValue();
                                    wgs84Box.setX1(x1);
                                    wgs84Box.setY1(y1);
                                    wgs84Box.setX2(x2);
                                    wgs84Box.setY2(y2);
                                    wgs84BoxSet = true;
                                }
                            } else if (topicRef.endsWith("nativeKeyOcc")) {
                                String gemeindekennziffer = SNSUtil.transformSpacialReference(this.fGemeindeKennzifferPrefix, data.get_value());
                                if (gemeindekennziffer.startsWith("lawa:")) {
                                    gemeindekennziffer = SNSUtil.transformSpacialReference("lawa:", data.get_value());
                                }
                                wgs84Box.setGemeindekennziffer(gemeindekennziffer);
                                wgs84BoxSet = true;
                            }
                        }
                    }
                    if (wgs84BoxSet) {
                        this.fWgs84Box.add(wgs84Box);
                    }
                    if (!temporal.isEmpty()) {
                        this.fTemporal.add(temporal);
                    }
                }
            }
        }
    }

    private Date parseDate(final String date) throws ParseException {
        Date result = null;

        Matcher m = this.fDateYearMonthDayPattern.matcher(date);
        if (m.matches()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            result = sdf.parse(date);
        } else {
            m = this.fDateYearMonthPattern.matcher(date);
            if (m.matches()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                result = sdf.parse(date);
            } else {
                m = this.fDateYearPattern.matcher(date);
                if (m.matches()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                    result = sdf.parse(date);
                }
            }
        }

        return result;
    }

    /**
     * All topic ids to the result.
     * 
     * @return
     * @throws Exception
     * @throws ParseException
     */
    public String[] getTopicIds() throws Exception, ParseException {
        if (this.fTopicIds.isEmpty()) {
            getReferences();
        }

        return (String[]) this.fTopicIds.toArray(new String[this.fTopicIds.size()]);
    }

    /**
     * All time references to a document that is analyzed by <code>getBuzzwords</code>.
     * 
     * @return Array of strings. It is empty if nothing is found.
     * @throws Exception
     *             If we cannot connect to the sns server.
     * @throws ParseException
     *             If the date cannot be parsed.
     */
    public Temporal[] getReferencesToTime() throws Exception, ParseException {
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
        if (this.fWgs84Box.isEmpty()) {
            getReferences();
        }

        return (Wgs84Box[]) this.fWgs84Box.toArray(new Wgs84Box[this.fWgs84Box.size()]);
    }

    /**
     * The method searches all topics of type <b>location</b> and provides the baseNames. The topics
     * came from the document that was analyzed by a previous
     * {@linkplain #getBuzzwords(String, int, boolean)} call.
     * 
     * @return A set of locations. It is empty, if no location topics are available.
     */
    public Set<String> getLocations() {
        Set<String> ret = new LinkedHashSet<String>();
        TopicTypClassifier topicClassifier = new TopicTypClassifier();
        List<Topic> locationTopics = topicClassifier.getLocationTopics(this.fTopics);

        for (Topic locationTopic : locationTopics) {
            ret.add(locationTopic.getBaseName(0).getBaseNameString().get_value());
        }
        return ret;
    }
}
