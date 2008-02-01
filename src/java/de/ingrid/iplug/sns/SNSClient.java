/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: DispatcherTest.java,v $
 */
package de.ingrid.iplug.sns;

import java.math.BigInteger;
import java.net.URL;
import java.rmi.RemoteException;

import com.slb.taxi.webservice.xtm.stubs.Anniversary;
import com.slb.taxi.webservice.xtm.stubs.AutoClassify;
import com.slb.taxi.webservice.xtm.stubs.FieldsType;
import com.slb.taxi.webservice.xtm.stubs.FindEvents;
import com.slb.taxi.webservice.xtm.stubs.FindTopics;
import com.slb.taxi.webservice.xtm.stubs.GetHierarchy;
import com.slb.taxi.webservice.xtm.stubs.GetPSI;
import com.slb.taxi.webservice.xtm.stubs.GetSimilarTerms;
import com.slb.taxi.webservice.xtm.stubs.GetTypes;
import com.slb.taxi.webservice.xtm.stubs.HttpSoapBindingStub;
import com.slb.taxi.webservice.xtm.stubs.SearchType;
import com.slb.taxi.webservice.xtm.stubs.TopicMapFragment;
import com.slb.taxi.webservice.xtm.stubs.XTMESoapPortType;
import com.slb.taxi.webservice.xtm.stubs.XTMserviceLocator;

/**
 * Adapter which provides the access to the sns webservice.
 * 
 * created on 21.07.2005
 * <p>
 * 
 * @author hs
 */
public class SNSClient {

    private String fUserName;

    private String fPassword;

    private String fLanguage;

    private XTMESoapPortType fXtmSoapPortType;

    private HttpSoapBindingStub fSoapBinding;

    /**
     * Constructs an instance by using the given parameters.
     * 
     * @param userName
     *            Is used for authentication on the webservice.
     * @param password
     *            Is used for authentication on the webservice.
     * @param language
     *            Is used to specify the preferred language for requests.
     * @throws Exception
     */
    public SNSClient(String userName, String password, String language) throws Exception {
        this(userName, password, language, null);
    }

    /**
     * Constructs an instance by using the given parameters.
     * 
     * @param userName
     *            Is used for authentication on the webservice.
     * @param password
     *            Is used for authentication on the webservice.
     * @param language
     *            Is used to specify the preferred language for requests.
     * @param url
     * @throws Exception
     */
    public SNSClient(String userName, String password, String language, URL url) throws Exception {
        this.fUserName = userName;
        this.fPassword = password;
        this.fLanguage = language;
        if (url == null) {
            this.fXtmSoapPortType = new XTMserviceLocator().getXTMSoapPort();
            this.fSoapBinding = (HttpSoapBindingStub) this.fXtmSoapPortType;
        } else {
            this.fXtmSoapPortType = new XTMserviceLocator().getXTMSoapPort(url);
            this.fSoapBinding = (HttpSoapBindingStub) this.fXtmSoapPortType;
        }
    }

    /**
     * Sends a findTopics request by using the underlying webservice client.<br>
     * All parameters will passed to the _findTopics request object.
     * 
     * @param queryTerm
     *            The Query.
     * @param path
     *            The path is used to qualify the result.
     * @param searchType
     *            Can be one of the provided <code>SearchType</code>s.
     * @param fieldsType
     *            Can be one of the provided <code>FieldsType</code>s.
     * @param offset
     *            Defines the number of topics to skip.
     * @param lang
     *            Is used to specify the preferred language for requests.
     * @param includeUse
     * @return The response object.
     * @throws Exception
     * @see SearchType
     * @see FieldsType
     */
    public synchronized TopicMapFragment findTopics(String queryTerm, String path, SearchType searchType,
            FieldsType fieldsType, long offset, String lang, boolean includeUse) throws Exception {
        if (queryTerm == null) {
            throw new IllegalArgumentException("QueryTerm can not be null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset can not be lower than 0");
        }
        FindTopics topicRequest = new FindTopics();
        topicRequest.setUser(this.fUserName);
        topicRequest.setPassword(this.fPassword);
        topicRequest.setLang(lang);
        topicRequest.setPath(path);
        topicRequest.setSearchType(searchType);
        topicRequest.setFields(fieldsType);
        topicRequest.setOffset(BigInteger.valueOf(offset));
        topicRequest.setQueryTerm(queryTerm);
        if (includeUse) {
            topicRequest.setIncludeUse("true");
        } else {
            topicRequest.setIncludeUse("false");
        }

        return this.fXtmSoapPortType.findTopicsOp(topicRequest);
    }

    /**
     * Sends a getPSI request by using the underlying webservice client. All parameters will passed to the _getPSI
     * request object.
     * 
     * @param topicID
     *            Current topic ID.
     * @param distance
     *            The distance-Parameter isn't used. Source: Interface-Spec. version 0.6.
     * @param filter
     *            Define filter for limit to a topic.
     * @return The response object.
     * @throws Exception
     */
    public synchronized TopicMapFragment getPSI(String topicID, int distance, String filter) throws Exception {
        if (topicID == null) {
            throw new IllegalArgumentException("TopicID can not be null");
        }
        if (distance < 0 || distance > 3) {
            throw new IllegalArgumentException("Distance must have a value between 0 and 3");
        }

        GetPSI psiRequest = new GetPSI();
        psiRequest.setUser(this.fUserName);
        psiRequest.setPassword(this.fPassword);
        // The distance-Parameter isn't used. Source: Interface-Spec. version 0.6.
        psiRequest.setDistance(BigInteger.valueOf(distance));
        psiRequest.setId(topicID);
        if (null != filter) {
            psiRequest.setFilter(filter);
        }

        return this.fXtmSoapPortType.getPSIOp(psiRequest);
    }

    /**
     * Sends a autoClassify request by using the underlying webservice client.<br>
     * All parameters will passed to a _autoClassify request object.
     * 
     * @param document
     *            The text to analyze.
     * @param analyzeMaxWords
     *            The maximal number of words to analyze for a document.
     * @param filter
     *            Define filter for limit to a topic.
     * @param ignoreCase
     *            Set to true ignore capitalization of the document.
     * @param lang
     *            Language distinction.
     * @return A topic map fragment.
     * @throws Exception
     */
    public synchronized TopicMapFragment autoClassify(String document, int analyzeMaxWords, String filter,
            boolean ignoreCase, String lang) throws Exception {
        if (document == null) {
            throw new IllegalArgumentException("document can not be null");
        }
        if (analyzeMaxWords < 0) {
            throw new IllegalArgumentException("AnalyzeMaxWords can not be lower than 0");
        }

        AutoClassify classifyRequest = new AutoClassify();
        classifyRequest.setUser(this.fUserName);
        classifyRequest.setPassword(this.fPassword);
        if (lang != null) {
            classifyRequest.setLang(lang);
        }
        classifyRequest.setDocument(document);
        classifyRequest.setAnalyzeMaxWords("" + analyzeMaxWords);
        if (ignoreCase) {
            classifyRequest.setIgnoreCase("true");
        } else {
            classifyRequest.setIgnoreCase("false");
        }
        if (null != filter) {
            classifyRequest.setFilter(filter);
        }

        return this.fXtmSoapPortType.autoClassifyOp(classifyRequest);
    }

    /**
     * Sends a autoClassify request by using the underlying webservice client.<br>
     * All parameters will passed to a _autoClassify request object.
     * 
     * @param url
     *            The url to analyze.
     * @param analyzeMaxWords
     *            The maximal number of words to analyze for a document.
     * @param filter
     *            Define filter for limit to a topic.
     * @param ignoreCase
     *            Set to true ignore capitalization of the document.
     * @param lang
     *            Language distinction.
     * @return A topic map fragment.
     * @throws Exception
     */
    public synchronized TopicMapFragment autoClassifyToUrl(String url, int analyzeMaxWords, String filter,
            boolean ignoreCase, String lang) throws Exception {
        if (url == null) {
            throw new IllegalArgumentException("Url can not be null");
        }
        if (analyzeMaxWords < 0) {
            throw new IllegalArgumentException("AnalyzeMaxWords can not be lower than 0");
        }

        AutoClassify classifyRequest = new AutoClassify();
        classifyRequest.setUser(this.fUserName);
        classifyRequest.setPassword(this.fPassword);
        if (lang != null) {
            classifyRequest.setLang(lang);
        }
        classifyRequest.setUrl(url);
        classifyRequest.setAnalyzeMaxWords("" + analyzeMaxWords);
        if (ignoreCase) {
            classifyRequest.setIgnoreCase("true");
        } else {
            classifyRequest.setIgnoreCase("false");
        }
        if (null != filter) {
            classifyRequest.setFilter(filter);
        }

        return this.fXtmSoapPortType.autoClassifyOp(classifyRequest);
    }

    /**
     * Sets user name and password for a topic map fragment.
     * 
     * @return A topic map fragment.
     * @throws RemoteException
     */
    public synchronized TopicMapFragment getTypes() throws RemoteException {
        GetTypes typeRequest = new GetTypes();
        typeRequest.setUser(this.fUserName);
        typeRequest.setPassword(this.fPassword);

        return this.fXtmSoapPortType.getTypesOp(typeRequest);
    }

    /**
     * Search the environment chronicles bases on findTopicslimits his however on the event types and extends the search
     * conditions by a time range or date.
     * 
     * @param query
     *            The Query.
     * @param ignoreCase
     *            Set to true ignore capitalization of the document.
     * @param searchType
     *            Can be one of the provided <code>SearchType</code>s.
     * @param pathArray
     *            Array of paths for a topic type as search criterion.
     * @param fieldsType
     *            Can be one of the provided <code>FieldsType</code>s.
     * @param offset
     *            Defines the number of topics to skip.
     * @param at
     *            Exact time as parameter for the search for events.
     * @param lang
     *            Is used to specify the preferred language for requests.
     * @param length
     *            Number of elements that should be retrieved.
     * @return A topic map fragment.
     * @throws RemoteException
     * @see SearchType
     * @see FieldsType
     */
    public synchronized TopicMapFragment findEvents(String query, boolean ignoreCase, SearchType searchType,
            String[] pathArray, FieldsType fieldsType, long offset, String at, String lang, int length)
            throws RemoteException {
        FindEvents findEvents = new FindEvents();
        findEvents.setUser(this.fUserName);
        findEvents.setPassword(this.fPassword);
        findEvents.setQueryTerm(query);

        if (ignoreCase) {
            findEvents.setIgnoreCase("true");
        } else {
            findEvents.setIgnoreCase("false");
        }
        findEvents.setSearchType(searchType);
        findEvents.setLang(lang);
        findEvents.setPath(pathArray);
        findEvents.setFields(fieldsType);
        findEvents.setOffset(BigInteger.valueOf(offset));
        findEvents.setPageSize(BigInteger.valueOf(length));
        findEvents.setAt(at);

        return this.fXtmSoapPortType.findEventsOp(findEvents);
    }

    /**
     * The request findEvents bases on findTopics, limits his however on the event types and extends the search
     * conditions by a time range or date.
     * 
     * @param query
     *            The Que
     * @param ignoreCase
     *            Set to true ignore capitalization of the document.
     * @param searchType
     *            Can be one of the provided <code>SearchType</code>s.
     * @param pathArray
     *            Array of paths for a topic type as search criterion.
     * @param fieldsType
     *            Can be one of the provided <code>FieldsType</code>s.
     * @param offset
     *            Defines the number of topics to skip.
     * @param from
     *            Search from a time point in histrory on.
     * @param to
     *            Search until to time point in histrory on.
     * @param lang
     *            Is used to specify the preferred language for requests.
     * @param length
     *            Number of elements that should be retrieved.
     * @return A topic map fragment.
     * @throws RemoteException
     * @see SearchType
     * @see FieldsType
     */
    public synchronized TopicMapFragment findEvents(String query, boolean ignoreCase, SearchType searchType,
            String[] pathArray, FieldsType fieldsType, long offset, String from, String to, String lang, int length)
            throws RemoteException {
        FindEvents findEvents = new FindEvents();
        findEvents.setUser(this.fUserName);
        findEvents.setPassword(this.fPassword);
        findEvents.setQueryTerm(query);
        if (ignoreCase) {
            findEvents.setIgnoreCase("true");
        } else {
            findEvents.setIgnoreCase("false");
        }
        findEvents.setSearchType(searchType);
        findEvents.setLang(lang);
        findEvents.setPath(pathArray);
        findEvents.setFields(fieldsType);
        findEvents.setOffset(BigInteger.valueOf(offset));
        findEvents.setPageSize(BigInteger.valueOf(length));
        findEvents.setFrom(from);
        findEvents.setTo(to);

        return this.fXtmSoapPortType.findEventsOp(findEvents);
    }

    /**
     * Anniversaries for past years are returned for a given date .
     * 
     * @param date
     *            The date which can be indicated.
     * @return A topic map fragment.
     * @throws RemoteException
     */
    public synchronized TopicMapFragment anniversary(String date) throws RemoteException {
        if (null == date) {
            throw new IllegalArgumentException("Date must be set.");
        }

        Anniversary anniversary = new Anniversary();
        anniversary.setUser(this.fUserName);
        anniversary.setPassword(this.fPassword);
        anniversary.setRefDate(date);

        return this.fXtmSoapPortType.anniversaryOp(anniversary);
    }

    /**
     * Request to get a hierachical notion.
     * 
     * @param association
     *            Name of association to map hierachy. For now only "narrowerTermAssoc" is supported.
     * @param depth
     *            Returned hierachy depth.
     * @param direction
     *            Direction of hierachy. "up" and "down" are supported.
     * @param includeSiblings
     *            Get all siblings of the topics even if they aren't in the hierachy.
     * @param lang
     *            Language of the request.
     * @param root
     *            Topic id of the start notion.
     * @return The requestet hierachical notion.
     * @throws RemoteException
     */
    public TopicMapFragment getHierachy(String association, long depth, String direction, boolean includeSiblings,
            String lang, String root) throws RemoteException {
        GetHierarchy hierarchy = new GetHierarchy();
        hierarchy.setAssociation(association);
        hierarchy.setDepth(BigInteger.valueOf(depth));
        hierarchy.setDirection(direction);
        hierarchy.setIncludeSiblings(Boolean.valueOf(includeSiblings));
        hierarchy.setLang(lang);
        hierarchy.setRoot(root);
        hierarchy.setUser(this.fUserName);
        hierarchy.setPassword(this.fPassword);

        return this.fXtmSoapPortType.getHierarchyOp(hierarchy);
    }

    /**
     * To a handed over output term SNS determines syntactically or semantically similar thesaurus terms. This request
     * can be implemented for several terms at the same time, whereby the results are assigned to their output term in
     * each case.
     * 
     * @param ignoreCase
     *            Set to true ignore capitalization of the document.
     * @param terms
     *            Output term, to which similar terms are looked for.
     * @param lang
     *            Is used to specify the preferred language for requests.
     * @return A topic map fragment.
     * @throws RemoteException
     */
    public synchronized TopicMapFragment getSimilarTerms(boolean ignoreCase, String[] terms, String lang)
            throws RemoteException {
        if ((null == terms) || (terms.length < 1)) {
            throw new IllegalArgumentException("No terms set.");
        }

        GetSimilarTerms getSimilarTerms = new GetSimilarTerms();
        getSimilarTerms.setUser(this.fUserName);
        getSimilarTerms.setPassword(this.fPassword);
        getSimilarTerms.setLang(lang);
        if (ignoreCase) {
            getSimilarTerms.setIgnoreCase("true");
        } else {
            getSimilarTerms.setIgnoreCase("false");
        }
        getSimilarTerms.setTerm(terms);

        return this.fXtmSoapPortType.getSimilarTermsOp(getSimilarTerms);
    }

    /**
     * Set timeout in milliseconds for the SN-Service connection.
     * 
     * @param timeout
     *            Timeout in milliseconds.
     */
    public void setTimeout(final int timeout) {
        this.fSoapBinding.setTimeout(timeout);
    }

    /**
     * Get the preferred language for requests.
     * 
     * @return The language for this client.
     */
    public String getLanguage() {
        return this.fLanguage;
    }
}
