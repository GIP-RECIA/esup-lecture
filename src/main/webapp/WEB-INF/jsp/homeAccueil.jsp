<%@ page language="java" contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<div id="${n}homeAccueilJspDiv1">
  <div class="round-top padding-botom-head">
    <div>
      <div class="row panel panelHome">
        <div class="col-sm-7 col-xs-7"
          onclick="lecture.${n}.AfficherTouteActualite()">
          <spring:message code="unreadNews" var="default_unreadNews" scope="session"></spring:message>
          <spring:message code="${ctx_name}.unreadNews" text="${default_unreadNews}"></spring:message>
          <strong><c:out value="${contexte.nbrUnreadItem}"></c:out></strong>
        </div>
        <c:if test="${contexte.lienVue!=null && contexte.lienVue!=''}">
          <div class="col-sm-5 col-xs-5">
            <a type="button" class="btn btn-default pull-right large-btn"
              href="${contexte.lienVue}"><spring:message
                code="seeAll" /></a>
          </div>
        </c:if>
      </div>
    </div>
  </div>
</div>
<div id="${n}homeAccueilJspDiv2">
  <div>
    <div class="panel-body scrollDivArticle ${n}"
      id="<portlet:namespace />zoneArticles">
      <c:forEach items="${listeItemAcceuil}" var="article">
<%--        <c:if test="${article.read=='true'}">
          <input type="hidden" id="arti${article.id}"
            class="listeIdArti${contexte.modePublisher}"
            value="contenuArti${contexte.modePublisher}${article.id}" />
        </c:if>
--%>
        <div
          id="contenuArti${contexte.modePublisher}${article.id}"
          class="itemOpacifiable   ${article.read ? 'dejaLue' : ''} ${article.rubriques != null ? 'modePublisher' : 'modeNoPublisher'} contenuArticle"
          >
          <c:out value="${article.htmlContent}" escapeXml="false"></c:out>
        </div>
      </c:forEach>
    </div>
  </div>
</div>