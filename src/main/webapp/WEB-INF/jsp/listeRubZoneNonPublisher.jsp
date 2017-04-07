<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<c:set var="nbCat"  value="0" />
<c:set var="nbSrc" value="0" />
<nav class="col-xs-12 col-sm-3" id='${n}listOfCat'>
	<ul class="nav nav-pills nav-stacked">
		<li class="li_cat" onclick="lecture.${n}.filterByRubriqueClass('rubrique_all', this)">
			<strong><c:out value="${contexte.name}"></c:out></strong>
			<input type="hidden" class="titleName" value="${contexte.name}"/>				
	</li>
					
		<c:forEach items="${listCat}" var="cat">
			<c:set var="nbCat" value="${nbCat+1}" />
					
			<c:set var="idcat" value="${fn:replace(fn:replace(cat.id,' ', ''),':', '')}"></c:set>
					
			<li><c:if test="${not empty cat.sources}">
					<div class="row">
						<div class="cursPoint div_cat"
							onclick="lecture.${n}.filterByRubriqueClass('cat_${nbCat}', this)">
							<!-- onclick="lecture.${n}.filtrerParCategorie('${cat.id}')" -->
							<c:out value="${cat.name}"></c:out>
							<input type="hidden" class="titleName" value="${cat.name}"/>
						</div>
						<div id="divThem${idcat}" data-toggle="collapse"
							data-target="#liThem${idcat}" aria-expanded="true"
							aria-controls="liThem${idcat}">
							<span class="caret pull-right"></span>
							
						</div>
					</div>
					<ul class="collapse in" id="liThem${idcat}" aria-expanded="true"
						aria-labelledby="divThem${idcat}">
						<c:forEach items="${cat.sources}" var="src">
						<c:set var="nbSrc" value="${nbSrc+1}" />
						
							<li><div class="row ligneRubriqueMenu cursPoint"
							 		onclick="lecture.${n}.filterByRubriqueClass('src_${nbSrc}', this)"> 
									<!-- onclick="lecture.${n}.filtrerParRubrique('${cat.id}','${src.id}','','')" -->
									<c:out value="${src.name}"></c:out>
									<c:if test="${cat.userCanMarkRead=='true'}">
										<span class="badge pull-right"><c:out
												value="${src.unreadItemsNumber}"></c:out></span>
									</c:if>
									<input type="hidden" class="titleName" value="${cat.name} > ${src.name}"/>
								</div></li>
						</c:forEach>
					</ul>
				</c:if></li>
		</c:forEach>
	</ul>
</nav>
