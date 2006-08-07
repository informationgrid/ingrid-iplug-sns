/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: DispatcherTest.java,v $
 */
package de.ingrid.iplug.sns;

import java.math.BigInteger;
import java.net.URL;
import java.rmi.RemoteException;

import com.slb.taxi.webservice.xtm.stubs.FieldsType;
import com.slb.taxi.webservice.xtm.stubs.HttpSoapBindingStub;
import com.slb.taxi.webservice.xtm.stubs.SearchType;
import com.slb.taxi.webservice.xtm.stubs.XTMESoapPortType;
import com.slb.taxi.webservice.xtm.stubs.XTMserviceLocator;
import com.slb.taxi.webservice.xtm.stubs._anniversary;
import com.slb.taxi.webservice.xtm.stubs._autoClassify;
import com.slb.taxi.webservice.xtm.stubs._findEvents;
import com.slb.taxi.webservice.xtm.stubs._findTopics;
import com.slb.taxi.webservice.xtm.stubs._getPSI;
import com.slb.taxi.webservice.xtm.stubs._getSimilarTerms;
import com.slb.taxi.webservice.xtm.stubs._getTypes;
import com.slb.taxi.webservice.xtm.stubs._topicMapFragment;

/**
 * Adapter which provides the access to the sns webservice.
 * 
 * created on 21.07.2005
 * <p>
 * 
 * @author hs
 */
public class SNSClient {

    private String fUserName = null;

    private String fPassword = null;

    private String fLanguage = null;

    private XTMESoapPortType fXtmSoapPortType = null;

    private HttpSoapBindingStub fSoapBinding = null;

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
     * @param path
     *            The path is used to qualify the result.
     * @param searchType
     *            Can be one of the provided <code>SearchType</code>s.
     * @param fieldsType
     *            Can be one of the provided <code>FieldsType</code>s.
     * @param offset
     *            Defines the number of topics to skip.
     * @param lang
     * @return The response object.
     * @throws Exception
     * @see SearchType
     * @see FieldsType
     */
    public synchronized _topicMapFragment findTopics(String queryTerm, String path, SearchType searchType,
            FieldsType fieldsType, long offset, String lang) throws Exception {
        if (queryTerm == null) {
            throw new IllegalArgumentException("QueryTerm can not be null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset can not be lower than 0");
        }
        _findTopics topicRequest = new _findTopics();
        topicRequest.setUser(this.fUserName);
        topicRequest.setPassword(this.fPassword);
        topicRequest.setLang(lang);
        topicRequest.setPath(path);
        topicRequest.setSearchType(searchType);
        topicRequest.setFields(fieldsType);
        topicRequest.setOffset(BigInteger.valueOf(offset));
        topicRequest.setQueryTerm(queryTerm);

        return this.fXtmSoapPortType.findTopicsOp(topicRequest);
    }

    /**
     * Sends a getPSI request by using the underlying webservice client. All parameters will passed to the _getPSI
     * request object.
     * 
     * @param topicID
     * @param distance
     * @param filter
     * @return The response object.
     * @throws Exception
     */
    public synchronized _topicMapFragment getPSI(String topicID, int distance, String filter) throws Exception {
        if (topicID == null) {
            throw new IllegalArgumentException("TopicID can not be null");
        }
        if (distance < 0 || distance > 3) {
            throw new IllegalArgumentException("Distance must have a value between 0 and 3");
        }

        _getPSI psiRequest = new _getPSI();
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
     * All parameters will passed to a _autoClassiy request object.
     * 
     * @param document
     *            The text to analyze.
     * @param analyzeMaxWords
     *            The number of words to analyze.
     * @param filter
     * @param ignoreCase
     *            Set to true ignore case of the document.
     * @return A topic map fragment.
     * @throws Exception
     */
    public synchronized _topicMapFragment autoClassify(String document, int analyzeMaxWords, String filter,
            boolean ignoreCase, String lang) throws Exception {
        if (document == null) {
            throw new IllegalArgumentException("document can not be null");
        }
        if (analyzeMaxWords < 0) {
            throw new IllegalArgumentException("AnalyzeMaxWords can not be lower than 0");
        }

        _autoClassify classifyRequest = new _autoClassify();
        classifyRequest.setUser(this.fUserName);
        classifyRequest.setPassword(this.fPassword);
        classifyRequest.setLang(lang);
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
     * 
     * @return A topic map fragment.
     * @throws RemoteException
     */
    public synchronized _topicMapFragment getTypes() throws RemoteException {
        _getTypes typeRequest = new _getTypes();
        typeRequest.setUser(this.fUserName);
        typeRequest.setPassword(this.fPassword);

        return this.fXtmSoapPortType.getTypesOp(typeRequest);
    }

    /**
     * Search the environment chronicles.
     * 
     * @param query
     * @param ignoreCase
     * @param searchType
     * @param pathArray
     * @param fieldsType
     * @param offset
     * @param at
     * @param lang
     * @param length
     * @return A topic map fragment.
     * @throws RemoteException
     */
    public synchronized _topicMapFragment findEvents(String query, boolean ignoreCase, SearchType searchType,
            String[] pathArray, FieldsType fieldsType, long offset, String at, String lang, int length)
            throws RemoteException {
        _findEvents findEvents = new _findEvents();
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
     * @param query
     * @param ignoreCase
     * @param searchType
     * @param pathArray
     * @param fieldsType
     * @param offset
     * @param from
     * @param to
     * @param lang
     * @return A topic map fragment.
     * @throws RemoteException
     */
    public synchronized _topicMapFragment findEvents(String query, boolean ignoreCase, SearchType searchType,
            String[] pathArray, FieldsType fieldsType, long offset, String from, String to, String lang, int length)
            throws RemoteException {
        _findEvents findEvents = new _findEvents();
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
     * Today before X years.
     * 
     * @param date
     * @return A topic map fragment.
     * @throws RemoteException
     */
    public synchronized _topicMapFragment anniversary(String date) throws RemoteException {
        if (null == date) {
            throw new IllegalArgumentException("Date must be set.");
        }

        _anniversary anniversary = new _anniversary();
        anniversary.setUser(this.fUserName);
        anniversary.setPassword(this.fPassword);
        anniversary.setRefDate(date);

        return this.fXtmSoapPortType.anniversaryOp(anniversary);
    }

    /**
     * Similar terms.
     * 
     * @param ignoreCase
     * @param terms
     * @param lang
     * @return A topic map fragment.
     * @throws RemoteException
     */
    public synchronized _topicMapFragment getSimilarTerms(boolean ignoreCase, String[] terms, String lang)
            throws RemoteException {
        if ((null == terms) || (terms.length < 1)) {
            throw new IllegalArgumentException("No terms set.");
        }

        _getSimilarTerms getSimilarTerms = new _getSimilarTerms();
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
     * @return The language for this client.
     */
    public String getLanguage() {
        return this.fLanguage;
    }
}
