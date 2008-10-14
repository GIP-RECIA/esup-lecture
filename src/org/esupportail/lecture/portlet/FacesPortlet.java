/**
 * ESUP-Portail Commons - Copyright (c) 2006 ESUP-Portail consortium
 * http://sourcesup.cru.fr/projects/esup-commons
 */
package org.esupportail.lecture.portlet;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.myfaces.context.servlet.ServletFacesContextImpl;
import org.apache.myfaces.portlet.MyFacesGenericPortlet;
import org.esupportail.commons.services.application.VersionningUtils;
import org.esupportail.commons.services.database.DatabaseUtils;
import org.esupportail.commons.services.exceptionHandling.ExceptionService;
import org.esupportail.commons.services.exceptionHandling.ExceptionUtils;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.commons.utils.ContextUtils;
import org.esupportail.commons.web.portlet.SerializableServletFacesContextImpl;
import org.springframework.web.portlet.context.PortletRequestAttributes;

/**
 * A JSF-based portlet that catches exception and gives them to an exception service.
 */
public class FacesPortlet extends MyFacesGenericPortlet implements Serializable {
	
	/**
	 * The serialization id.
	 */
	private static final long serialVersionUID = -4232039696236207722L;

	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());

	/**
	 * Bean constructor.
	 */
	public FacesPortlet() {
		super();
	}

	/**
	 * @see javax.portlet.Portlet#init(javax.portlet.PortletConfig)
	 */
	@Override
	public void init(final PortletConfig portletConfig) {
		try {
			super.init(portletConfig);
		} catch (Exception e) {
			ExceptionUtils.catchException(e);
		}
	}

	/**
	 * Catch an exception.
	 * @param exception
	 * @return an exception service
	 */
	protected ExceptionService catchException(
			final Exception exception) {
		ExceptionUtils.markExceptionCaught(); 
		ExceptionService exceptionService = ExceptionUtils.catchException(exception);
		ExceptionUtils.markExceptionCaught(exceptionService); 
		return exceptionService;
	}

    /**
     * This method follows JSF Spec section 2.1.1.  It renders the default view from a non-faces
     * request.
     *
     * @param request The portlet render request.
     * @param response The portlet render response.
     */
    @Override
	protected void nonFacesRequest(
			final RenderRequest request, 
			final RenderResponse response) throws PortletException {
        nonFacesRequest(request, response, null);
    }

    /**
     * This method follows JSF Spec section 2.1.1.  It renders a view from a non-faces
     * request.  This is useful for a default view as well as for views that need to
     * be rendered from the portlet's edit and help buttons.
     *
     * @param request The portlet render request.
     * @param response The portlet render response.
     * @param view The name of the view that needs to be rendered.
     */
	@Override
	protected void nonFacesRequest(
    		final RenderRequest request, 
    		final RenderResponse response, 
    		final String view)
            throws PortletException {
		if (logger.isDebugEnabled()) {
			logger.debug("==== BEGIN nonFacesRequest with view \"" + view + "\" ====");
		}
    	 // do this in case nonFacesRequest is called by a subclass
		setContentType(request, response);
        ApplicationFactory appFactory =
            (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application application = appFactory.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        ServletFacesContextImpl facesContext; 
//      ServletFacesContextImpl facesContext = 
//        	(ServletFacesContextImpl) FacesContext.getCurrentInstance();
//        if (facesContext == null) {
        	facesContext = (ServletFacesContextImpl) facesContext(request, response);
    		facesContext.setExternalContext(makeExternalContext(request, response));
//        }
        String viewToRender = view;
        if (viewToRender == null) {
        	// the call to selectDefaultView was moved here to be sure 
        	// that the faces context has been initialized before
        	viewToRender = selectDefaultView(request, response);
        }
        UIViewRoot viewRoot = viewHandler.createView(facesContext, viewToRender);
        viewRoot.setViewId(viewToRender);
        facesContext.setViewRoot(viewRoot);
        setPortletRequestFlag(request);
        try {
			lifecycle.render(facesContext);
		} catch (Exception e) {
			throw new PortletException(e);
		}
    }
	
	/**
	 * @see org.apache.myfaces.portlet.MyFacesGenericPortlet#facesRender(
	 * javax.portlet.RenderRequest, javax.portlet.RenderResponse)
	 */
	@Override
	public void facesRender(
			final RenderRequest request, 
			final RenderResponse response) 
	throws PortletException, IOException {
		PortletRequestAttributes previousRequestAttributes = 
			ContextUtils.bindRequestAndContext(request, getPortletContext());
		boolean error = true;
		if (!ExceptionUtils.exceptionAlreadyCaught()) {
			try {
				DatabaseUtils.open();
				DatabaseUtils.begin();
				super.facesRender(request, response);
				DatabaseUtils.commit();
				if (logger.isDebugEnabled()) {
					logger.debug("==== END facesRender ====");
				}
				error = false;
				return;
			} catch (Exception e) {
				catchException(e);
			} finally {
				DatabaseUtils.close();
				if (!error) {
					//Just call ContextUtils.unbindRequest if no error because 
					//informations is used by ExceptionUtils.getMarkedExceptionService()
					ContextUtils.unbindRequest(previousRequestAttributes);									
				}
			}
		}
		try {
			ExceptionService exceptionService = ExceptionUtils.getMarkedExceptionService();
			if (exceptionService == null) {
				logger.error("An exception was thrown but no exception service was found!");
			} else {
				nonFacesRequest(request, response, exceptionService.getExceptionView());
			}
		} catch (Exception e) {
			logger.error("An exception was caught while rendering an exception, giving up", e);
			handleExceptionFromLifecycle(e);
		} finally {
			ContextUtils.unbindRequest(previousRequestAttributes);
		}
	}

	/**
	 * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     * A patched version of the original processAction method to catch exceptions and
     * get the facesContext in exception reports.
     * @param request 
     * @param response 
     */
 	@SuppressWarnings("unchecked")
	@Override
    public void processAction(
    		final ActionRequest request, 
    		final ActionResponse response) {
		if (logger.isDebugEnabled()) {
			logger.debug("==== BEGIN processAction ====");
		}
        if (sessionTimedOut(request)) {
        	return;
        }
		PortletRequestAttributes previousRequestAttributes = null;
        ServletFacesContextImpl facesContext = null;
        try {
    		previousRequestAttributes = ContextUtils.bindRequestAndContext(request, getPortletContext());
    		DatabaseUtils.open();
    		DatabaseUtils.begin();
    		VersionningUtils.checkVersion(true, false);
            facesContext = new SerializableServletFacesContextImpl(portletContext, request, response);
            request.getPortletSession().setAttribute(CURRENT_FACES_CONTEXT, facesContext);
    		facesContext.setExternalContext(makeExternalContext(request, response));
            setPortletRequestFlag(request);
            lifecycle.execute(facesContext);
            if (!facesContext.getResponseComplete()) {
                response.setRenderParameter(VIEW_ID, facesContext.getViewRoot().getViewId());
            }
            DatabaseUtils.commit();
    		if (logger.isDebugEnabled()) {
    			logger.debug("==== END processAction ====");
    		}
        } catch (Exception e) {
			ExceptionService exceptionService = catchException(e);
			response.setRenderParameter(VIEW_ID, exceptionService.getExceptionView());
//			if (facesContext != null) {
//				facesContext.release();
//			}
        } finally {
        	saveRequestAttributes(request);
            DatabaseUtils.close();
        	ContextUtils.unbindRequest(previousRequestAttributes);
        }
    }

	/**
	 * @see org.apache.myfaces.portlet.MyFacesGenericPortlet#logException(java.lang.Throwable, java.lang.String)
	 */
	@Override
	protected void logException(
			final Throwable e, 
			final String msgPrefix) {
		// logged by the exception manager
	}

}
