package org.esupportail.lecture.web.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.esupportail.lecture.exceptions.domain.InternalDomainException;
import org.esupportail.lecture.exceptions.web.WebException;
import org.esupportail.lecture.utils.ServiceUtilLecture;
import org.esupportail.lecture.web.beans.CategoryWebBean;
import org.esupportail.lecture.web.beans.ContextWebBean;
import org.esupportail.lecture.web.beans.ItemWebBean;
import org.esupportail.lecture.web.beans.SourceWebBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

@Controller
@RequestMapping("VIEW")
public class HomeController extends TwoPanesController {

	final String TREE_VISIBLE = "treeVisible";
	final String CHANGE_ITEM_DISPLAY_MODE = "changeItemDisplayMode";
	final String AVAILABLE_ITEM_DISPLAY_MODE = "availableItemDisplayModes";
	/**
	 * Log instance.
	 */
	private static final Log LOG = LogFactory.getLog(HomeController.class);

	/**
	 * render home page
	 *
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RenderMapping
	public String goHome(RenderRequest request, RenderResponse response, ModelMap model) {
		if (request.getWindowState().equals(WindowState.MINIMIZED)) {
			return "mini";
		}
		ContextWebBean contexte = getContext();
		List<CategoryWebBean> listCat = contexte.getCategories();
		List<ItemWebBean> listeItemAcceuil = null;
		List<ItemWebBean> listeItemAcceuilMasquerDejaLues = null;
		int nbrArticleNonLu = 0;
		if (contexte.isViewDef()) {
			// la liste des articles à afficher+nombre d'articles non lus
			listeItemAcceuil = ServiceUtilLecture.getListItemAccueil(contexte, listCat);
			// la liste des articles à afficher dans le cas ou on veut masquer les déjà lu
			listeItemAcceuilMasquerDejaLues = ServiceUtilLecture.getListItemAccueilMasquerDejaLues(contexte, listCat);

		} else {
		//	 listeItemAcceuil = new ArrayList<ItemWebBean>();
			nbrArticleNonLu = ServiceUtilLecture.compteNombreArticleNonLu(contexte);
		}
		LOG.debug("goHome context ItemDisplayMode = " +  contexte.getItemDisplayMode() );
		model = bindInitialModel(model, response, request);
		model.addAttribute("testListCatSize", listCat==null ? "null" : listCat.size());
		model.addAttribute("listCat", listCat);
		model.addAttribute("contexte", contexte);
		model.addAttribute("nombreArticleNonLu", nbrArticleNonLu);
		model.addAttribute("listeItemAcceuil", listeItemAcceuil);
		model.addAttribute("listeItemAcceuilMasquerDejaLues", listeItemAcceuilMasquerDejaLues);
	//	model.addAttribute("", )
		return "home";
		
		
	}

	@ResourceMapping(value = "getJSON")
	public View getJSON(ResourceRequest request, ResourceResponse response) {
		MappingJacksonJsonView view = new MappingJacksonJsonView();
		view.addStaticAttribute(CONTEXT, getContext());
		view.addStaticAttribute(GUEST_MODE, isGuestMode());
		Locale locale = request.getLocale();
		view.addStaticAttribute(MESSAGES, i18nService.getStrings(locale));
		return view;
	}

	/**
	 * action : toggle item from read to unread and unread to read.
	 *
	 * @param catID
	 *            Category ID
	 * @param srcID
	 *            Source ID
	 * @param itemID
	 *            Item ID
	 * @param isRead
	 *            is source read ?
	 */
	// @ResourceMapping(value = "toggleItemReadState")
	@RequestMapping(value = { "VIEW" }, params = { "action=toggleItemReadState" })
	public @ResponseBody void toggleItemReadState(@RequestParam(required = true, value = "p1") String catID,
			@RequestParam(required = true, value = "p2") String srcID,
			@RequestParam(required = true, value = "p3") String itemID,
			@RequestParam(required = true, value = "p4") boolean isRead,
			@RequestParam(required = true, value = "p5") boolean isPublisherMode) {
		if (isGuestMode()) {
			throw new SecurityException("Try to access restricted function is guest mode");
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("toggleItemReadState(" + catID + ", " + srcID + ", " + itemID + ")");
		}
		try {
			facadeService.markItemReadMode(getUID(), srcID, itemID, isRead);
		} catch (Exception e) {
			throw new WebException("Error in toggleItemReadState", e);
		}
	}
	
	private StringBuilder addIdSrc(StringBuilder sb, String id) {
		if (sb == null) {
			sb = new StringBuilder("{\"srcsIds\":[");
		}
		sb.append('"');
		sb.append(id);
		sb.append('"');
		sb.append(',');
		
		return sb;
	}
	
	@ResourceMapping(value = "markRead")
	public void markRead(final ResourceRequest request
			, final ResourceResponse response)  { 
		LOG.debug("markRead"); 
		for (Entry<String, String[]> entry	 : request.getParameterMap().entrySet()) {
			for (String val : entry.getValue()) {
				LOG.debug(entry.getKey() + " : "+ val);
			}
		}
		String catID = request.getParameter("catId");
		String srcID = request.getParameter("srcId");
		String itemID = request.getParameter("itemId");
		String uid = getUID();
		boolean isRead = "true".equals(request.getParameter("isRead"));
		boolean isPublisherMode = "true".equals(request.getParameter("isPublisherMode"));
		boolean isMarked = false;
		
		Appendable allSrcMarked = new Appendable() {
			private StringBuilder sb = new StringBuilder("{\"srcsIds\":[");
			@Override
			public Appendable append(CharSequence csq, int start, int end) throws IOException {
				return null;
			}
			@Override
			public Appendable append(char c) throws IOException {
				return null;
			}
			@Override
			public Appendable append(CharSequence csq) throws IOException {
				sb.append('"');
				sb.append(csq);
				sb.append('"');
				sb.append(',');
				return sb;
			}
			@Override 
			public String toString(){
				sb.setCharAt(sb.length()-1, ']');
				sb.append('}');
				return sb.toString();
			}
		};
		
		
		
		try {
			if (isPublisherMode) {
				// pour le mode publisher ou marque l'article dans toutes ses sources:
				// il faut donc les trouver:
				ContextWebBean contexte = getContext();
				if (contexte == null) {
					LOG.warn("contexte null pour : "+ uid);
				} else {
					List<CategoryWebBean> listCat = contexte.getCategories();
					if (listCat == null) {
						LOG.warn("list categorie  null pour : "+ uid);
					} else {
						for (CategoryWebBean categoryWebBean : listCat) {
							List<SourceWebBean> sources = categoryWebBean.getSources();
							if (sources != null){
								for (SourceWebBean sourceWebBean : sources) {
									ItemWebBean item =  sourceWebBean.getItem(itemID);
									if (item != null) {
										String idSrc = sourceWebBean.getId();
										LOG.debug("markRead(" + uid + " , " + idSrc + ", " + itemID + ")");
										facadeService.markItemReadMode(uid, idSrc, itemID, isRead);
										if (srcID.equals(idSrc)) {
											isMarked = true;
										}
										allSrcMarked.append(idSrc);
									}
								}
							}
						}
					}
				}
			}
			
			if (!isMarked) {
				facadeService.markItemReadMode(uid, srcID, itemID, isRead);
				allSrcMarked.append(srcID);
			}
			
			LOG.debug("markRead OK");
			response.setContentType("application/json");
	        response.getWriter().write(allSrcMarked.toString());
		} catch (Exception e) {
			throw new WebException("Error in markRead", e);
		}
		
	}
	

	/**
	 * action : toggle all item from read to unread and unread to read.
	 *
	 * @param isRead
	 */
	// @RequestMapping(value = { "VIEW" }, params = {
	// "action=toggleAllItemReadState" })
	@ResourceMapping(value = "toggleAllItemReadState")
	public @ResponseBody ModelAndView toggleAllItemReadState(
			@RequestParam(required = true, value = "p1") boolean isRead,
			@RequestParam(required = true, value = "p2") String idCat,
			@RequestParam(required = true, value = "p3") String idSrc,
			@RequestParam(required = true, value = "p4") String filtreNonLu, Model model) {
		List<CategoryWebBean> listCatFiltre = new ArrayList<CategoryWebBean>();
		if (isGuestMode()) {
			throw new SecurityException("Try to access restricted function is guest mode");
		}
		try {
			ContextWebBean contexte = getContext();
			List<CategoryWebBean> listCat = contexte.getCategories();
			for (CategoryWebBean cat : listCat) {
				for (SourceWebBean src : cat.getSources()) {
					for (ItemWebBean item : src.getItems()) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("toggleAllItemReadState(" + cat.getId() + ", " + src.getId() + ", " + item.getId()
									+ ")");
						}
						facadeService.markItemReadMode(getUID(), src.getId(), item.getId(), isRead);
					}
				}
			}
			listCatFiltre = ServiceUtilLecture.trierListCategorie(listCat, idCat, idSrc, "", filtreNonLu);
			model.addAttribute("listCat", listCatFiltre);
			model.addAttribute("isRead", isRead);
		} catch (Exception e) {
			throw new WebException("Error in toggleAllItemReadState", e);
		}
		return new ModelAndView("articleZone");
	}

	
	@ResourceMapping(value = "filterUnreadOnly")
	public void filterUnreadOnl(final ResourceRequest request
		, final ResourceResponse response) throws IOException { 
			
			String uid = getUID();
			String ctxId = request.getParameter("idContexte");
			if (ctxId == null) {
				ctxId = facadeService.getCurrentContextId();
				LOG.debug("ctxId=" + ctxId);
			}
			String filtrer = request.getParameter("filter");
			if (filtrer != null) {
				try {
					facadeService.markItemDisplayModeContext(uid, ctxId, Boolean.parseBoolean(filtrer));
				} catch (Exception e) {
					throw new WebException("Error in FilterUnreadOnly", e);
				}
				LOG.debug("getContext : " + getContext().getItemDisplayMode());
			}
	}
	/**
	 * action : Filter items by idCat, idSrc,
	 *
	 * @param idCat
	 * @param idSrc
	 * @param filtreNonLu
	 * @param model
	 * @return
	 */

	@ActionMapping(value = "filteredItem")
	public void filteredItem(
			// @RequestParam(required = true, value = "p1") String idCat,
			// @RequestParam(required = true, value = "p2") String idSrc,
			// @RequestParam(required = true, value = "p3") String filtreNonLu,
			// @RequestParam(required = true, value = "nomSrc") String nameSrc,
			// @RequestParam(required = true, value = "idContexte") String
			// idContexte,
			ActionRequest request, ActionResponse response, Model model) {
		// List<CategoryWebBean> listCatFiltre = new
		// ArrayList<CategoryWebBean>();

		if (isGuestMode()) {
			throw new SecurityException("Try to access restricted function is guest mode");
		}
		try {
			String ctxId;
			ctxId = request.getParameter("idContexte");
			if (ctxId == null) {
				ctxId = facadeService.getCurrentContextId();
			}
			String filtreNonLu = request.getParameter("p3");

			// ContextWebBean contexte = getContext();
			if ("val2".equals(filtreNonLu)) {
				facadeService.markItemDisplayModeContext(getUID(), ctxId, true);
			} else {
				facadeService.markItemDisplayModeContext(getUID(), ctxId, false);
			}
			ContextWebBean contexte = getContext(ctxId);
			List<CategoryWebBean> listCat = contexte.getCategories();
			List<ItemWebBean> listeItemAcceuil = null ;
			int nbrArticleNonLu = 0;
			if (contexte.isViewDef()) {
				// la liste des articles à afficher+nombre d'articles non lus
				listeItemAcceuil = ServiceUtilLecture.getListItemAccueil(contexte, listCat);
			} else {
			//	listeItemAcceuil = new ArrayList<ItemWebBean>();
				nbrArticleNonLu = ServiceUtilLecture.compteNombreArticleNonLu(contexte);	
			}
			// listCatFiltre = SeviceUtilLecture.trierListCategorie(listCat, "",
			// "", "", filtreNonLu);
			// List<ItemWebBean> listeItemAcceuil = new
			// ArrayList<ItemWebBean>();
			// int nbrArticleNonLu = 0;
			// nbrArticleNonLu =
			// SeviceUtilLecture.compteNombreArticleNonLu(contexte);
			// model.addAttribute("contexte", contexte);
			// model.addAttribute("listCat", listCatFiltre);
			// model.addAttribute("nombreArticleNonLu", nbrArticleNonLu);
			// model.addAttribute("listeItemAcceuil", listeItemAcceuil);
		
			
			model.addAttribute("listCat", listCat);
			model.addAttribute("contexte", contexte);
			model.addAttribute("nombreArticleNonLu", nbrArticleNonLu);
			model.addAttribute("listeItemAcceuil", listeItemAcceuil);
			response.setRenderParameter("action", "success");
		} catch (Exception e) {
			throw new WebException("Error in FilteredItem", e);
		}

	}

	
	
	@RenderMapping(params = "action=success")
	public String viewSuccess() {
		return "home";

	}

	// @RequestMapping(value = { "VIEW" }, params = { "action=FilteredItem" })
	// public void FilteredItem(
	// @RequestParam(required = true, value = "p1") String idCat,
	// @RequestParam(required = true, value = "p2") String idSrc,
	// @RequestParam(required = true, value = "p3") String filtreNonLu,
	// @RequestParam(required = true, value = "nomSrc") String nameSrc,
	// @RequestParam(required = true, value = "idContexte") String idContexte,
	// ActionRequest request,
	// ActionResponse response) {
	// List<CategoryWebBean> listCatFiltre = new ArrayList<CategoryWebBean>();
	// ModelMap model = new ModelMap();
	// if (isGuestMode()) {
	// throw new SecurityException("Try to access restricted function is guest
	// mode");
	// }
	// try {
	// String ctxId ;
	// if (ctxId == null) {
	// ctxId = facadeService.getCurrentContextId();
	// }
	// String filtreNonLu = request.getParameter("p3");
	// // ContextWebBean contexte = getContext();
	// if ("val2".equals(filtreNonLu)) {
	// facadeService.markItemDisplayModeContext(getUID(), ctxId, true);
	// } else {
	// facadeService.markItemDisplayModeContext(getUID(), ctxId, false);
	// }
	// ContextWebBean contexte = getContext(ctxId);
	// List<CategoryWebBean> listCat = contexte.getCategories();
	// listCatFiltre = SeviceUtilLecture.trierListCategorie(listCat, idCat,
	// idSrc, nameSrc, filtreNonLu);
	// List<ItemWebBean> listeItemAcceuil = new ArrayList<ItemWebBean>();
	// int nbrArticleNonLu = 0;
	// nbrArticleNonLu = SeviceUtilLecture.compteNombreArticleNonLu(contexte);
	// model.addAttribute("contexte", contexte);
	// model.addAttribute("listCat", listCatFiltre);
	// model.addAttribute("nombreArticleNonLu", nbrArticleNonLu);
	// model.addAttribute("listeItemAcceuil", listeItemAcceuil);
	// } catch (Exception e) {
	// throw new WebException("Error in FilteredItem", e);
	// }
	// response.setRenderParameter("action", "home");
	// }

	/**
	 * Model : Context of the connected user.
	 *
	 * @return Returns the context.
	 */
	private ContextWebBean getContext() {
		return getContext(facadeService.getCurrentContextId()) ;
		
	}

	private ContextWebBean getContext(String ctxId) {
		boolean viewDef = facadeService.getCurrentViewDef();
		int nbreArticle = facadeService.getNombreArcticle();
		String lienVue = facadeService.getLienVue();
		LOG.debug(String.format(" facadeService.getContext(%s, %s, %s, %d, %s)", getUID(), ctxId, viewDef, nbreArticle, lienVue));
		return facadeService.getContext(getUID(), ctxId, viewDef, nbreArticle, lienVue);
	}
}
