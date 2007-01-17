/**
* ESUP-Portail Lecture - Copyright (c) 2006 ESUP-Portail consortium
* For any information please refer to http://esup-helpdesk.sourceforge.net
* You may obtain a copy of the licence at http://www.esup-portail.org/license/
*/
package org.esupportail.lecture.domain.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.XPathException;
import org.esupportail.lecture.domain.DomainTools;
import org.esupportail.lecture.exceptions.domain.ComputeItemsException;
import org.esupportail.lecture.exceptions.domain.MappingNotFoundException;
import org.esupportail.lecture.exceptions.domain.Xml2HtmlException;

/**
 * Source element : a source can be a managed or personal one.
 * A source is got from an URL, given by source Profile.
 * This source contains an xmlStream that, after xslt parsing, provides a list
 * of items that can be displed on user interface
 * @author gbouteil
 *
 */

public abstract class Source implements Element,Serializable {

	/*
	 *************************** PROPERTIES ******************************** */	

	/**
	 * Log instance 
	 */
	protected static final Log log = LogFactory.getLog(Source.class); 

	/**
	 * xmlStream (XML content) of the source
	 */
	private String xmlStream = "";
	
	/**
	 * URL of the source (also used by xslt mapping to find ItemXpath and Xslt File)
	 */
	private String URL = "";

	/**
	 * profile Id of the source
	 */
	private int profileId;

	/**
	 * Opitionnal : DTD of the source (one of these parameter is required : xmlns, xmlType, dtd,rootElement)
	 */
	private String dtd;

	/**
	 * Optionnal : xmlType of the source (one of these parameter is required : xmlns, xmlType, dtd,rootElement)
	 */
	private String xmlType;
	
	/**
	 * Optionnal : xmlns of the source (one of these parameter is required : xmlns, xmlType, dtd,rootElement)
	 */
	private String xmlns;
	
	/**
	 * Optionnal : rootElement of the xmlStream (one of these parameter is required : xmlns, xmlType, dtd,rootElement)
	 */
	private String rootElement;
	
	/**
	 * URL of the xslt file to display xml content
	 */
	private String xsltURL;
	
	/**
	 * Xpath to access item in the XML source file correspoding to this source profile
	 */
	private String itemXPath;
	
	/**
	 * flag used to know if computeXslt() used one time or not  
	 */
	private boolean isXsltComputed = false;

	/**
	 * flag used to know if computeItems() used one time or not  
	 */
	private boolean isItemComputed = false;
	
	/**
	 * Map of namespaces used by Xpath (key: NamesSpace prefix; value: NamaSpace URI)
	 */
	private HashMap<String, String> XPathNameSpaces = new HashMap<String, String>();

	/**
	 * Items List of this source
	 */
	private List<Item> Items = new ArrayList<Item>();
	
	
	/*
	 *************************** INIT ******************************** */	
		

	/*
	 *************************** METHODS ******************************** */	
	
	
	/**
	 * find item XPath and url of Xslt file, in list of Mappings in channel (from mapping file), in fonction of dtd, xmlType, 
	 * xmlns or XML root element of the source XML content
	 * @throws MappingNotFoundException 
	 */
	synchronized private void computeXslt() throws MappingNotFoundException{
	   	if (log.isDebugEnabled()){
    		log.debug("id="+this.profileId+" - computeXslt()");
    	}
		// TODO (GB later) revoir cet algo
		Channel channel = DomainTools.getChannel();
		
//		String setXsltURL = xsltURL;
//		String setItemXPath = itemXPath;
			
		if (log.isDebugEnabled()) {
			log.debug("Source::computeXslt() : "+profileId);
			log.debug("DTD : "+dtd);
			log.debug("xmlType : "+xmlType);		
			log.debug("xmlns : "+xmlns);
			log.debug("rootElement : "+rootElement);			
		}
		
		if (xsltURL == null || itemXPath == null || XPathNameSpaces.size() == 0) {
			Mapping m = new Mapping();
			if (URL != null) {
				//Try to find a mapping from url
				m = channel.getMappingBySourceURL(URL);
			}else {
				log.error("Source "+this.profileId +"does not have any URL defined");
			}
			if (m == null) {
				//no mapping find from url so using XML content caracteristics
				if (dtd != null) {
					m = channel.getMappingByDtd(dtd);
				} else {
					if (xmlType != null) {
						m = channel.getMappingByXmlType(xmlType);
					} else {
						if (xmlns != null) {
							m = channel.getMappingByXmlns(xmlns);
						} else {
							if (rootElement != null) {
								m = channel.getMappingByRootElement(rootElement);
							} else {
								log.warn("Source "+profileId+" does not have any entry key to find xslt information : no dtd, xmlType, xmlns, rootElement");
							}}}}}
		
			if (m == null) {
				String errorMsg = "Mapping not found for source "+ profileId;
				log.error(errorMsg);
				throw new MappingNotFoundException("Mapping not found for source "+ profileId);
			}
			if (xsltURL == null) {
				xsltURL = m.getXsltUrl();
			}
			if (itemXPath == null) {
				itemXPath = m.getItemXPath();
			}
			if (XPathNameSpaces.size() == 0) {
				XPathNameSpaces = m.getXPathNameSpaces();
			}
		}
		isXsltComputed = true;
	}
	
	/**
	 * Make Items objects in fonction of itemXPath, xsltURL, xmlStream
	 * @throws MappingNotFoundException 
	 * @throws ComputeItemsException 
	 * @throws Xml2HtmlException 
	 */
	@SuppressWarnings("unchecked")
	synchronized private void computeItems() throws MappingNotFoundException, ComputeItemsException, Xml2HtmlException {
	   	if (log.isDebugEnabled()){
    		log.debug("id="+this.profileId+" - computeItems()");
    	}
		if (!isXsltComputed){
			computeXslt();
		}
		try {
			//create dom4j document
			Document document = DocumentHelper.parseText(xmlStream);
			//get encoding
			String encoding = document.getXMLEncoding();
			//lauch Xpath find
			XPath xpath = document.createXPath(getItemXPath());
			xpath.setNamespaceURIs(XPathNameSpaces);
			List<Node> list = xpath.selectNodes(document);
			//List<Node> list = document.selectNodes(itemXPath);
			Iterator<Node> iter = list.iterator();
			while (iter.hasNext()) {
				Node node = iter.next();
				Item item = new Item();
				StringBuffer XML = new StringBuffer("<?xml version=\"1.0\" encoding=\"");
				XML.append(encoding);
				XML.append("\" ?>");
				XML.append(node.asXML());
				String XMLasString = XML.toString();
				String htmlContent = xml2html(XMLasString, getXsltURL());
				item.setHtmlContent(htmlContent);
				//find MD5 of item content for his ID
				byte[] hash = MessageDigest.getInstance("MD5").digest(XMLasString.getBytes());
				StringBuffer hashString = new StringBuffer();
				for ( int i = 0; i < hash.length; ++i ) {
					String hex = Integer.toHexString(hash[i]);
					if ( hex.length() == 1 ) {
						hashString.append('0');
						hashString.append(hex.charAt(hex.length()-1));
					} 
					else {
						hashString.append(hex.substring(hex.length()-2));
					}
				}
				item.setId(hashString.toString());
				Items.add(item);
			}
		} catch (DocumentException e) {
			String errorMsg = "Error parsing XML content of the source";
			log.error(errorMsg);
			throw new ComputeItemsException(errorMsg,e);
		} catch (NoSuchAlgorithmException e) {
			String errorMsg = "MD5 algorithm not supported";
			log.error(errorMsg);
			throw new ComputeItemsException(errorMsg,e);
		} catch (XPathException e) {
			String errorMsg = "Xpath with NameSpace not specified in mappings.xml";
			log.error(errorMsg);
			throw new ComputeItemsException(errorMsg,e);
		}
		isItemComputed = true;
	}
	
	/**
	 * transform a xml String in a html String with an XSLT
	 * @param xml to transform
	 * @param xsltFileURL URL of XSLT file
	 * @return html content
	 * @throws Xml2HtmlException 
	 */
	synchronized private String xml2html(String xml, String xsltFileURL) throws Xml2HtmlException {
	   	if (log.isDebugEnabled()){
    		log.debug("id="+this.profileId+" - xml2html(xml,xsltFileURL)");
    	}
		log.debug("voici le xsltFileUrl : "+xsltFileURL);
		String ret = null;
		try {
			//		 1. Instantiate a TransformerFactory.
			TransformerFactory tFactory = TransformerFactory.newInstance();
			//		2. Use the TransformerFactory to process the stylesheet Source and
			//		generate a Transformer.
			Transformer transformer;
			URL url = new URL(xsltFileURL);
			StreamSource streamSource = new StreamSource(url.openStream());
			transformer = tFactory.newTransformer(streamSource);
			//		3. Use the Transformer to transform an XML Source and send the
			//		output to a Result object.
			ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
			StreamSource source = new StreamSource(inputStream);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(outputStream);
			transformer.transform(source, result);
			ret = outputStream.toString("UTF-8"); //"ISO-8859-1"
		} catch (TransformerConfigurationException e) {
			String errorMsg = "Error in XSTL Transformation configuration";
			log.error(errorMsg);
			throw new Xml2HtmlException(errorMsg,e);
		} catch (TransformerException e) {
			String errorMsg = "Error in XSTL Transformation";
			log.error(errorMsg);
			throw new Xml2HtmlException(errorMsg,e);
		} catch (IOException e) {
			String errorMsg = "IO Error in xml2html";
			log.error(errorMsg);
			throw new Xml2HtmlException(errorMsg,e);
		}
		return ret;
	}
	
	/**
	 * @return Returns the itemXPath.
	 * @throws MappingNotFoundException 
	 */
	private String getItemXPath() throws MappingNotFoundException {
	   	if (log.isDebugEnabled()){
    		log.debug("id="+this.profileId+" - getItemXPath()");
    	}
		if (!isXsltComputed){
			computeXslt();
		}
		return itemXPath;
	}

	/**
	 * @param itemXPath The itemXPath to set.
	 */
	synchronized private void setItemXPath(String itemXPath) {
	   	if (log.isDebugEnabled()){
    		log.debug("id="+this.profileId+" - setItemXPath("+itemXPath+")");
    	}
		this.itemXPath = itemXPath;
		isXsltComputed = false;
	}

	/**
	 * @return Returns the xsltURL.
	 * @throws MappingNotFoundException 
	 */
	private String getXsltURL() throws MappingNotFoundException {
	   	if (log.isDebugEnabled()){
    		log.debug("id="+this.profileId+" - getXsltURL()");
    	}
		if (!isXsltComputed){
			computeXslt();
		}
		return xsltURL;
	}

	/**
	 * @param xsltURL The xsltURL to set.
	 */
	synchronized private void setXsltURL(String xsltURL) {
	   	if (log.isDebugEnabled()){
    		log.debug("id="+this.profileId+" - setXsltURL("+xsltURL+")");
    	}
		this.xsltURL = xsltURL;
		isXsltComputed = false;
	}

	/**
	 * @return a hash of XPathNameSpace of this source
	 * @throws MappingNotFoundException
	 */
	private HashMap<String, String> getXPathNameSpaces() throws MappingNotFoundException {
	   	if (log.isDebugEnabled()){
    		log.debug("id="+this.profileId+" - getXPathNameSpaces()");
    	}
		if (!isXsltComputed){
			computeXslt();
		}
		return XPathNameSpaces;
	}

	/**Sets the hash of XPathNameSpace of this source
	 * @param pathNameSpaces
	 */
	synchronized private void setXPathNameSpaces(HashMap<String, String> pathNameSpaces) {
	   	if (log.isDebugEnabled()){
    		log.debug("id="+this.profileId+" - setXPathNameSpaces("+pathNameSpaces+")");
    	}
		XPathNameSpaces = pathNameSpaces;
		isXsltComputed = false;
	}

	/**
	 * get Items list of this source
	 * @return the items lits
	 * @throws Xml2HtmlException 
	 * @throws ComputeItemsException 
	 * @throws MappingNotFoundException 
	 */
	protected List<Item> getItems() throws MappingNotFoundException, ComputeItemsException, Xml2HtmlException {
	   	if (log.isDebugEnabled()){
    		log.debug("id="+this.profileId+" - getItems()");
    	}
		if (!isItemComputed){
			computeItems();
		}
		return Items;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer out = new StringBuffer();
		out.append("\n dtd --> ").append(getDtd());
		out.append("\n rootElement --> ").append(getRootElement());
		out.append("\n xmlns --> ").append(getXmlns());
		out.append("\n xmlType --> ").append(getXmlType());
		out.append("\n xmlStream --> ").append(getXmlStream());
		try {
			out.append("\n xsltURL --> ").append(getXsltURL());
			out.append("\n itemXPath --> ").append(getItemXPath());
		} catch (MappingNotFoundException e) {
			out.append("\n MappingNotFoundException !");
		}
		return out.toString();
	}
	
	
	/* 
	 ************************* ACCESSORS ******************************** */	

	/**
	 * @return the dtd of source XML content
	 */
	private String getDtd() {
		return dtd;
	}


	/**
	 * set the dtd of source XML content
	 * @param dtd
	 */
	synchronized public void setDtd(String dtd) {
		this.dtd = dtd;
	}


	/**
	 * @return Returns the rootElement.
	 */
	private String getRootElement() {
		return rootElement;
	}


	/**
	 * @param rootElement The rootElement to set.
	 */
	synchronized public void setRootElement(String rootElement) {
		this.rootElement = rootElement;
	}


	/**
	 * @return Returns the xmlns.
	 */
	private String getXmlns() {
		return xmlns;
	}


	/**
	 * @param xmlns The xmlns to set.
	 */
	synchronized public void setXmlns(String xmlns) {
		this.xmlns = xmlns;
	}


	/**
	 * @return Returns the xmlType.
	 */
	private String getXmlType() {
		return xmlType;
	}


	/**
	 * @param xmlType The xmlType to set.
	 */
	synchronized public void setXmlType(String xmlType) {
		this.xmlType = xmlType;
	}


	/**
	 * @return XML Stream (XML content) of the source
	 */
	private String getXmlStream() {
		return xmlStream;
	}


	/**
	 * set XML Stream (XML content) of the source
	 * @param xmlStream
	 */
	synchronized public void setXmlStream(String xmlStream) {
		this.xmlStream = xmlStream;
	}


	/**
	 * set the Source URL
	 * @param url
	 */
	public void setURL(String url) {
		URL = url;
	}



}
