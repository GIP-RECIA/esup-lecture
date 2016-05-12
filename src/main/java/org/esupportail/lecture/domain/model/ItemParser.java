package org.esupportail.lecture.domain.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.esupportail.lecture.dao.XMLUtil;

public class ItemParser {

	protected static final Log LOG = LogFactory.getLog(SourceProfile.class);
	private String XMLStream;
	private Source source;
	private ArrayList<ComplexItem> visibleItems;

	public ItemParser(Source s) {
		this.setSource(s);
		this.init();
	}

	private void init() {
		this.parseItemsXML();
		this.produceXMLStream();
	}

	private void produceXMLStream() {
		String stuff = "";
		if (!this.visibleItems.isEmpty()) {
			stuff += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			stuff += "<items>";
			for (ComplexItem complexItem : visibleItems) {
				stuff += "<item>";
				stuff += complexItem.getHtmlContent();
				stuff += "</item>";
			}
		}
		stuff += "</items>";
		this.setXMLStream(stuff);

	}

	private VisibilityMode evaluateVisibility(ComplexItem ci) {
		VisibilitySets visibilitySets = ci.getVisibility();

		VisibilityMode mode = VisibilityMode.NOVISIBLE;

		mode = visibilitySets.whichVisibility();

		return mode;
	}

	private void parseItemsXML() {
		ArrayList<ComplexItem> ret = new ArrayList<ComplexItem>();
		try {
			// get the XML
			String categoryURL = this.source.getProfile().getSourceURL();
			SAXReader reader = new SAXReader();
			Document document = reader.read(categoryURL);
			Element root = document.getRootElement();
			List<Node> items = root.selectNodes("/items/item");
			for (Node item : items) {
				ComplexItem sp = new ComplexItem(this.source);
				VisibilitySets visibilitySets = new VisibilitySets();
				visibilitySets.setObliged(XMLUtil.loadDefAndContentSets(item.selectSingleNode("visibility/obliged")));
				sp.setHtmlContent(item.selectSingleNode("article").asXML());
				sp.setVisibility(visibilitySets);
				if (this.evaluateVisibility(sp) != VisibilityMode.NOVISIBLE) {
					ret.add(sp);
				}
			}
		} catch (DocumentException e) {
			String msg = "parseItemsXML(). Error parsing stuffs.";
			LOG.error(msg, e);
		}
		this.setVisibleItems(ret);
	}

	private void setVisibleItems(ArrayList<ComplexItem> ret) {
		this.visibleItems = ret;

	}

	public ArrayList<ComplexItem> getItems() {
		return this.visibleItems;
	}

	public String getXMLStream() {

		return this.XMLStream;
	}

	public void setXMLStream(String xMLStream) {
		XMLStream = xMLStream;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

}
