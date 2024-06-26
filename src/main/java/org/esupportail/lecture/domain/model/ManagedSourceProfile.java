/**
* ESUP-Portail Lecture - Copyright (c) 2006 ESUP-Portail consortium
* For any information please refer to http://esup-helpdesk.sourceforge.net
* You may obtain a copy of the licence at http://www.esup-portail.org/license/
*/
package org.esupportail.lecture.domain.model;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.esupportail.lecture.domain.DomainTools;
import org.esupportail.lecture.exceptions.dao.InternalDaoException;
import org.esupportail.lecture.exceptions.domain.SourceNotLoadedException;

/**
 * Managed source profile element. It refers a source and is defined in a
 * managedCategory
 *
 * @author gbouteil
 * @see org.esupportail.lecture.domain.model.SourceProfile
 * @see org.esupportail.lecture.domain.model.ManagedElementProfile
 *
 */
public class ManagedSourceProfile extends SourceProfile implements ManagedElementProfile {

	/*
	 ************************** PROPERTIES ********************************
	 */
	/**
	 * Log instance.
	 */
	protected static final Log LOG = LogFactory.getLog(ManagedSourceProfile.class);

	/**
	 * Specific user content parameter. Indicates source multiplicity : - true :
	 * source is specific to a user, it is loaded in user profile => source is a
	 * SingleSource - false : source is global to users, it is loaded in channel
	 * environnement => source is a GlobalSource
	 */
	private boolean specificUserContent;

	/**
	 * parent category of this managed source profile.
	 */
	private ManagedCategory category;

	/**
	 * profile of the parent category of this managed source profile.
	 */
	private ManagedCategoryProfile categoryProfile;

	/**
	 * source profile Id. Defined in the xml file : interne Id of the source
	 * Profile
	 */
	private String fileId;

	/**
	 * Inner features declared in XML file.
	 */
	private InnerFeatures inner;
	/**
	 * Inheritance rules are applied on feature (take care of inner features).
	 */
	private boolean featuresComputed;
	/**
	 * Access mode on the Source.
	 */
	private Accessibility access;
	/**
	 * Visibility rights for groups on the managed element Its values depends on
	 * trustCategory parameter.
	 */
	private VisibilitySets visibility;
	/**
	 * timeOut to get the Source.
	 */
	private int timeOut;
	/**
	 * ttl of the Source.
	 */
	private int ttl;

	private boolean highLight;

	private String color;
	private int uuid;
	private boolean hiddenIfEmpty;
	/*
	 ************************** INIT ********************************
	 */

	/**
	 * Constructor.
	 *
	 * @param mc
	 *            managedCategory parent of this ManagedSourceProfile
	 */
	public ManagedSourceProfile(final ManagedCategory mc) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("ManagedSourceProfile(" + mc.getProfileId() + ")");
		}
		category = mc;
		categoryProfile = mc.getProfile();
		inner = new InnerFeatures();
		featuresComputed = false;
	}

	/*
	 *************************** METHODS ********************************
	 */
	/**
	 * Return access of the source, taking care of inheritance regulars.
	 *
	 * @return access
	 */
	public Accessibility getAccess() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("id = " + this.getId() + " - getAccess()");
		}
		computeFeatures();
		return access;
	}

	/**
	 * @see ManagedSourceProfile#access
	 * @see org.esupportail.lecture.domain.model.ManagedElementProfile#setAccess(
	 *      org.esupportail.lecture.domain.model.Accessibility)
	 */
	public void setAccess(final Accessibility access) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("id=" + this.getId() + " - setAccess()");
		}
		inner.setAccess(access);
		featuresComputed = false;
	}

	/**
	 * @param fileId
	 *            sourceProfileId defined in xml category file
	 */
	public void setFileId(final String fileId) {
		this.fileId = fileId;
		super.setId(super.makeId("m", categoryProfile.getId(), fileId));
	}

	/**
	 * Return visibility of the source, taking care of inheritance regulars.
	 *
	 * @return visibility
	 * @see org.esupportail.lecture.domain.model.ManagedElementProfile#getVisibility()
	 */
	public VisibilitySets getVisibility() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("id=" + this.getId() + " - getVisibility()");
		}
		computeFeatures();
		return visibility;
	}

	/**
	 * Sets visibility of source profile (value defined in XML file).
	 *
	 * @see org.esupportail.lecture.domain.model.ManagedElementProfile#setVisibility(
	 *      org.esupportail.lecture.domain.model.VisibilitySets)
	 */
	public void setVisibility(final VisibilitySets visibility) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("id=" + this.getId() + " - setVisibility(visibility)");
		}
		inner.setVisibility(visibility);
		featuresComputed = false;
	}

	/**
	 * Return timeOut of the source, taking care of inheritance regulars.
	 *
	 * @return timeOut
	 */
	@Override
	public int getTimeOut() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("id=" + this.getId() + " - getTimeOut()");
		}
		computeFeatures();
		return timeOut;
	}

	/**
	 * Return ttl of the source, taking care of inheritance regulars.
	 *
	 * @return ttl
	 */
	@Override
	public int getTtl() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("id=" + this.getId() + " - getTtl()");
		}
		computeFeatures();
		return ttl;
	}
	// GB : changement de specifs
	// /**
	// * @see org.esupportail.lecture.domain.model.SourceProfile#getTtl()
	// */
	// @Override
	// public int getTtl() {
	// return getParent().getTtl();
	// }

	/**
	 * @param timeOut
	 *
	 */
	public void setTimeOut(final int timeOut) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("id=" + this.getId() + " - setTimeOut(" + timeOut + ")");
		}
		inner.setTimeOut(timeOut);
		featuresComputed = false;
	}

	/**
	 * @param ttl
	 *
	 */
	public void setTtl(final int ttl) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("id=" + this.getId() + " - setTtl(" + ttl + ")");
		}
		inner.setTtl(ttl);
		featuresComputed = false;
	}

	/**
	 * Computes rights on parameters shared between parent ManagedCategory and
	 * managedSourceProfile. (timeOut, visibility,access)
	 */
	private void computeFeatures() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("id = " + this.getId() + " - computeFeatures()");
		}
		if (!featuresComputed) {
			if (categoryProfile.getTrustCategory()) {
				access = inner.getAccess();
				visibility = inner.getVisibility();
				timeOut = inner.getTimeOut();
				ttl = inner.getTtl();

				if (access == null) {
					access = category.getAccess();
				}
				VisibilitySets v = null;
				v = category.getVisibility();

				if (visibility == null) {
					visibility = v;
				} else if (visibility.isEmpty()) {
					visibility = v;
				}
				if (timeOut == 0) {
					timeOut = category.getTimeOut();
				}
				if (ttl == 0) {
					ttl = category.getTtl();
				}
			} else {
				// No trust => features of categoryProfile
				// GB access = categoryProfile.getAccess();
				access = category.getAccess();
				visibility = category.getVisibility();
				timeOut = category.getTimeOut();
				ttl = category.getTtl();
			}
			featuresComputed = true;
		}
	}

	/*
	 *************************** INNER CLASS ********************************
	 */

	/**
	 * Inner Features (accessibility, visibility, timeOut,ttl) declared in xml
	 * file. These values are used according to inheritance regulars
	 *
	 * @author gbouteil
	 */
	private class InnerFeatures implements Serializable {

		/**
		 * Access mode on the remote source.
		 */
		private Accessibility access;
		/**
		 * Visibility rights for groups on the remote source.
		 */
		private VisibilitySets visibility;
		/**
		 * timeOut to get the remote source.
		 */
		private int timeOut;

		/**
		 * ttl to get the remote source.
		 */
		private int ttl;

		/**
		 * Constructor.
		 */
		protected InnerFeatures() {
			// Nothing to do
		}

		/**
		 * @return access
		 */
		protected Accessibility getAccess() {
			return access;
		}

		/**
		 * @param access
		 */
		protected void setAccess(final Accessibility access) {
			this.access = access;
		}

		/**
		 * @return visibility
		 */
		protected VisibilitySets getVisibility() {
			return visibility;
		}

		/**
		 * @param visibility
		 */
		protected void setVisibility(final VisibilitySets visibility) {
			this.visibility = visibility;
		}

		/**
		 * @return timeOut
		 */
		protected int getTimeOut() {
			return timeOut;
		}

		/**
		 * @param timeOut
		 */
		protected void setTimeOut(final int timeOut) {
			this.timeOut = timeOut;
		}

		/**
		 * @return ttl
		 */
		protected int getTtl() {
			return ttl;
		}

		/**
		 * @param ttl
		 */
		protected void setTtl(final int ttl) {
			this.ttl = ttl;
		}

		@Override
		public String toString() {
			return "InnerFeatures{" +
					"access=" + access +
					", visibility=" + visibility +
					", timeOut=" + timeOut +
					", ttl=" + ttl +
					'}';
		}
	}

	/* UPDATING */

	/**
	 * Update CustomCategory with this ManagedSourceProfile. It evaluates
	 * visibility for user profile and subscribe it or not to customCategory.
	 *
	 * @param customManagedCategory
	 *            the customManagedCategory to update
	 * @return true if the source is visible by the userProfile
	 */
	protected VisibilityMode updateCustomCategory(final CustomManagedCategory customManagedCategory) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("id = " + this.getId() + " - updateCustomCategory(" + customManagedCategory.getElementId() + ")");
		}
		return setUpCustomCategoryVisibility(customManagedCategory);
	}

	/**
	 * Load the source referenced by this ManagedSourceProfile.
	 *
	 * @throws SourceNotLoadedException
	 * @see org.esupportail.lecture.domain.model.SourceProfile#loadSource()
	 */
	@Override
	protected void loadSource() throws SourceNotLoadedException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("id = " + this.getId() + " - loadSource()");
		}
		Source source = null;
		try {
			Accessibility accessibility = getAccess();
			if (Accessibility.PUBLIC.equals(accessibility)) {
				source = DomainTools.getDaoService().getSource(this);
				setElement(source);
			} else if (Accessibility.CAS.equals(accessibility)) {
				source = DomainTools.getDaoService().getSource(this, true);
				setElement(source);
			}
			if (source == null) {
				String errorMsg = "The source " + this.getId() + " is not loaded : DAO return null.";
				LOG.error(errorMsg);
				throw new SourceNotLoadedException(errorMsg);
			}
		} catch (InternalDaoException e) {
			String errorMsg = "The source " + this.getId() + " is impossible to be loaded because of DaoException.";
			LOG.error(errorMsg);
			throw new SourceNotLoadedException(errorMsg, e);
		}

	}

	/**
	 * Evaluate visibility of current user for this managed source profile.
	 * Update customManagedCategory (belongs to user) if needed : add or remove
	 * customManagedSources associated with this ManagedSourceProfile
	 *
	 * @param customManagedCategory
	 *            customManagedCategory to set up
	 * @return true if sourceProfile is visible by user (in Obliged or in
	 *         autoSubscribed, or in Allowed)
	 */

	private VisibilityMode setUpCustomCategoryVisibility(final CustomManagedCategory customManagedCategory) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("id = " + this.getId() + " - setUpCustomCategoryVisibility("
					+ customManagedCategory.getElementId() + ")");
		}
		/*
		 * Algo pour gerer les customSourceProfiles :
		 * ------------------------------------ user app. obliged => enregistrer
		 * la source dans le user profile + sortir user app. autoSub =>
		 * enregistrer la source dans le user profile si c'est la première fois
		 * + sortir user app.allowed => rien à faire + sortir user n'app. rien
		 * => effacer la custom source .
		 *
		 */

		// get visibilitySets of the current sourceProfile
		VisibilitySets visibilitySets = getVisibility();

		// GB : J'ai tout commenté car c'est géré par la méthode
		// computeFeatures()
		// //if visibilitySets is NOT defined on sourceProfile
		// if (visibilitySets.isEmpty()) {
		// //we get, as default, the VisibilityMode from the CategoryProfile
		// containing the sourceProfile.
		// //Please note that in case of CategoryProfile with
		// TrustedCategory=yes attribute then the
		// //visibilitySets of CategoryProfile was replaced by the
		// CategoryProfile of the trusted Category.
		// visibilitySets = customManagedCategory.getProfile().getVisibility();
		// }

		VisibilityMode mode = VisibilityMode.NOVISIBLE;

		mode = visibilitySets.whichVisibility();

		if (mode == VisibilityMode.OBLIGED) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("IsInObliged : " + this.getId());
			}
			customManagedCategory.addSubscription(this);
			return mode;
		}

		if (mode == VisibilityMode.AUTOSUBSCRIBED) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("IsInAutoSubscribed : " + this.getId());
			}
			// Enregistrer uniquement si pas desabonne
			if (!customManagedCategory.isUnsubscribedAutoSubscribedSource(this.getId())) {
				customManagedCategory.addSubscription(this);
			}
			// TODO (GB later) l'ajouter dans le custom category si c'est la
			// premiere fois
			// customManagedCategory.addSubscription(this);
			return mode;
		}

		if (mode == VisibilityMode.ALLOWED) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("IsInAllowed : " + this.getId());
			}
			// Nothing to do
			return mode;
		}

		// ELSE not Visible
		customManagedCategory.removeCustomManagedSourceFromProfile(this.getId());
		mode = VisibilityMode.NOVISIBLE;
		return mode;
	}

	/*
	 *************************** ACCESSORS ********************************
	 */

	/**
	 * @return the parent of this managed source profile
	 */
	protected ManagedCategory getParent() {
		return category;
	}

	/**
	 * Returns specificUserContent value.
	 *
	 * @return specificUserContent
	 * @see ManagedSourceProfile#specificUserContent
	 */
	public boolean isSpecificUserContent() {
		return specificUserContent;
	}

	/**
	 * Sets specificUserContent.
	 *
	 * @param specificUserContent
	 * @see ManagedSourceProfile#specificUserContent
	 */
	public void setSpecificUserContent(final boolean specificUserContent) {
		this.specificUserContent = specificUserContent;
	}

	/**
	 * @return fileId : sourceProfileId defined in xml file category
	 */
	protected String getFileId() {
		return fileId;
	}

	public boolean isHighLight() {
		return highLight;
	}

	public void setHighLight(boolean highLight) {
		this.highLight = highLight;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getUuid() {
		return uuid;
	}

	public void setUuid(int uuid) {
		this.uuid = uuid;
	}

	public boolean isHiddenIfEmpty(){
		return this.hiddenIfEmpty;
	}

	public void setHiddenIfEmpty(boolean hiddenIfEmpty){
		this.hiddenIfEmpty = hiddenIfEmpty;
	}

	@Override
	public String toString() {
		return "ManagedSourceProfile{" +
				"specificUserContent=" + specificUserContent +
				", category=" + category +
				", categoryProfile=" + categoryProfile +
				", fileId='" + fileId + '\'' +
				", inner=" + inner +
				", featuresComputed=" + featuresComputed +
				", access=" + access +
				", visibility=" + visibility +
				", timeOut=" + timeOut +
				", ttl=" + ttl +
				", highLight=" + highLight +
				", color='" + color + '\'' +
				", uuid=" + uuid +
				"} " + super.toString();
	}
}
