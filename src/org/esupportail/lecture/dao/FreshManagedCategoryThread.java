package org.esupportail.lecture.dao;

import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.esupportail.lecture.domain.model.Accessibility;
import org.esupportail.lecture.domain.model.ManagedCategory;
import org.esupportail.lecture.domain.model.ManagedCategoryProfile;
import org.esupportail.lecture.domain.model.ManagedSourceProfile;
import org.esupportail.lecture.domain.model.SourceProfile;
import org.esupportail.lecture.domain.model.VisibilitySets;
import org.esupportail.lecture.exceptions.dao.XMLParseException;

/**
 * Get a Freash Managed Category from a distinct Thread
 * @author bourges
 */
public class FreshManagedCategoryThread extends Thread {

	/**
	 * Log instance 
	 */
	private static final Log log = LogFactory.getLog(FreshManagedCategoryThread.class);
	private ManagedCategory managedCategory = null;
	private ManagedCategoryProfile managedCategoryProfile;
	private Exception exception;
	
	public FreshManagedCategoryThread(ManagedCategoryProfile profile) {
		this.managedCategoryProfile = profile;
		this.exception = null;
	}

	/**
	 * @throws XMLParseException 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			this.managedCategory = getFreshManagedCategory(managedCategoryProfile);
		} catch (XMLParseException e) {
			this.exception = e;
		}
	}

	/**
	 * get a managed category from the web without cache
	 * @param profile ManagedCategoryProfile of Managed category to get
	 * @return Managed category
	 * @throws XMLParseException 
	 */
	@SuppressWarnings("unchecked")
	private synchronized ManagedCategory getFreshManagedCategory(ManagedCategoryProfile profile) throws XMLParseException {
		//TODO (RB) refactoring of exceptions
		if (log.isDebugEnabled()) {
			log.debug("in getFreshManagedCategory");
		}
		ManagedCategory ret = new ManagedCategory();
		try {
			//get the XML
			SAXReader reader = new SAXReader();
			String categoryURL = profile.getUrlCategory();
			Document document = reader.read(categoryURL);
			Element root = document.getRootElement();
			// Category properties
			ret.setName(root.valueOf("@name"));
			ret.setDescription(root.valueOf("/category/description"));
			ret.setProfileId(profile.getId());
			// SourceProfiles loop
			Hashtable<String, SourceProfile> sourceProfiles = new Hashtable<String,SourceProfile>();
			List<Node> srcProfiles = root.selectNodes("/category/sourceProfiles/sourceProfile");
			for (Node srcProfile : srcProfiles) {
				ManagedSourceProfile sp = new ManagedSourceProfile(profile);
				sp.setFileId(srcProfile.valueOf("@id"));
				sp.setName(srcProfile.valueOf("@name"));
				sp.setSourceURL(srcProfile.valueOf("@url"));
				sp.setTtl(Integer.parseInt(srcProfile.valueOf("@ttl")));
				String specificUserContentValue = srcProfile.valueOf("@specificUserContent");
				if (specificUserContentValue.equals("yes")) {
					sp.setSpecificUserContent(true);
				}
				else {
					sp.setSpecificUserContent(false);
				}
				sp.setXsltURL(srcProfile.valueOf("@xsltFile"));
				sp.setItemXPath(srcProfile.valueOf("@itemXPath"));
				String access = srcProfile.valueOf("@access");
				if (access.equalsIgnoreCase("public")) {
					sp.setAccess(Accessibility.PUBLIC);
				} else if (access.equalsIgnoreCase("cas")) {
					sp.setAccess(Accessibility.CAS);
				}
				// SourceProfile visibility
				VisibilitySets visibilitySets = new VisibilitySets();  
				// foreach (allowed / autoSubscribed / Obliged)
				visibilitySets.setAllowed(XMLUtil.loadDefAndContentSets(srcProfile.selectSingleNode("visibility/allowed")));
				visibilitySets.setObliged(XMLUtil.loadDefAndContentSets(srcProfile.selectSingleNode("visibility/obliged")));
				visibilitySets.setAutoSubscribed(XMLUtil.loadDefAndContentSets(srcProfile.selectSingleNode("visibility/autoSubscribed")));
				sp.setVisibility(visibilitySets);
				sp.setTtl(profile.getTtl());
				sourceProfiles.put(sp.getId(),sp);				
			}
			ret.setSourceProfilesHash(sourceProfiles);
			// Category visibility
			VisibilitySets visibilitySets = new VisibilitySets();  
			// foreach (allowed / autoSubscribed / Obliged)
			visibilitySets.setAllowed(XMLUtil.loadDefAndContentSets(root.selectSingleNode("/category/visibility/allowed")));
			visibilitySets.setObliged(XMLUtil.loadDefAndContentSets(root.selectSingleNode("/category/visibility/obliged")));
			visibilitySets.setAutoSubscribed(XMLUtil.loadDefAndContentSets(root.selectSingleNode("/category/visibility/autoSubscribed")));
			ret.setVisibility(visibilitySets);
		} catch (DocumentException e) {
			String profileId = (profile != null ? profile.getId() : "null");
			String msg = "getFreshManagedCategory("+profileId+"). Can't read configuration file.";
			log.error(msg);
			throw new XMLParseException(msg ,e);
		}
		return ret;
	}

	/**
	 * @return managedCategory
	 */
	public ManagedCategory getManagedCategory() {
		return managedCategory;
	}

	public Exception getException() {
		return exception;
	}

}
