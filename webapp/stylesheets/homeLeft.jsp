<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
	xmlns:f="http://java.sun.com/jsf/core"
	xmlns:h="http://java.sun.com/jsf/html"
	xmlns:c="http://java.sun.com/jsp/jstl/core"
	xmlns:t="http://myfaces.apache.org/tomahawk"
	xmlns:e="http://commons.esup-portail.org">
	<jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" />
	<f:subview id="leftSubview">
		<!-- TREE -->
		<t:htmlTag value="div" id="left" forceId="true">
			<!-- Title -->
			<t:htmlTag value="p" styleClass="portlet-section-header">
				<h:outputText value="#{homeController.context.name}"/>
			</t:htmlTag>
			<!-- Categories -->
			<t:htmlTag value="ul">
				<t:dataList value="#{homeController.context.categories}" var="cat" layout="simple">
					<t:htmlTag value="li"
						styleClass="#{cat.folded ? 'collapsed' : 'expanded' }">
						<h:commandButton action="#{homeController.toggleFoldedState}"
							image="/media/moins.gif" alt="#{msgs['colapseCategory']}"
							title="#{msgs['colapseCategory']}" rendered="#{!cat.folded}">
							<t:updateActionListener property="#{homeController.sourceId}" value="0" />
							<t:updateActionListener property="#{homeController.categoryId}"
								value="#{cat.id}" />
						</h:commandButton>
						<h:commandButton action="#{homeController.toggleFoldedState}"
							image="/media/plus.gif" alt="#{msgs['expandCategory']}"
							title="#{msgs['expandCategory']}" rendered="#{cat.folded}">
							<t:updateActionListener property="#{homeController.sourceId}" value="0" />
							<t:updateActionListener property="#{homeController.categoryId}"
								value="#{cat.id}" />
						</h:commandButton>
						<h:commandButton action="#{homeController.selectElement}" alt="#{cat.name}"
							title="#{cat.name}" value="#{cat.name}" styleClass="elementButton">
							<t:updateActionListener property="#{homeController.source}" value="#{null}" />
							<t:updateActionListener property="#{homeController.categoryId}" value="#{cat.id}" />
						</h:commandButton>
						<t:htmlTag value="ul" rendered="#{!cat.folded}">
							<!-- Souces -->
							<t:dataList value="#{cat.sources}" var="src" layout="simple">
								<t:htmlTag value="li">
									<h:commandButton action="#{homeController.selectElement}"
										image="/media/puce.gif" alt="#{msgs['selectSource']}" title="#{msgs['selectSource']}">
										<t:updateActionListener property="#{homeController.source}"
											value="#{src}" />
										<t:updateActionListener property="#{homeController.categoryId}"
											value="#{cat.id}" />
									</h:commandButton>
									<h:commandButton action="#{homeController.selectElement}"
										alt="#{src.name}" title="#{src.name}" value="#{src.name}" styleClass="elementButton">
										<t:updateActionListener property="#{homeController.source}"
											value="#{src}" />
										<t:updateActionListener property="#{homeController.categoryId}"
											value="#{cat.id}" />
									</h:commandButton>
								</t:htmlTag>
							</t:dataList>
						</t:htmlTag>
					</t:htmlTag>
				</t:dataList>
			</t:htmlTag>
		</t:htmlTag>
		<!-- Ajust Tree Size buttons -->
		<t:htmlTag value="hr" />
		<t:htmlTag value="div" id="menuLeft" forceId="true" rendered="#{!homeController.guestMode}">
			<t:htmlTag value="div" styleClass="menuTitle">
				<h:commandButton id="editButton"
					action="navigationEdit"
					image="/media/edit.png" alt="#{msgs['edit']}"
					title="#{msgs['edit']}" />
			</t:htmlTag>
			<t:htmlTag value="div" styleClass="menuButton">
				<t:htmlTag value="ul">
					<t:htmlTag value="li">
						<h:commandButton id="treeSmallerButton"
							actionListener="#{homeController.adjustTreeSize}"
							image="/media/retract.gif" alt="#{msgs['treeSmaller']}" title="#{msgs['treeSmaller']}"/>
					</t:htmlTag>
					<t:htmlTag value="li">
						<h:commandButton id="treeLargerButton"
							actionListener="#{homeController.adjustTreeSize}"
							image="/media/extand.gif" alt="#{msgs['treeLarger']}" title="#{msgs['treeLarger']}"/>
					</t:htmlTag>
				</t:htmlTag>
			</t:htmlTag>
		</t:htmlTag>
	</f:subview>
</jsp:root>
