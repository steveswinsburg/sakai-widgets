
package org.sakaiproject.widgets.siteinformation.ui;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;

import lombok.extern.apachecommons.CommonsLog;

/**
 * Main page for the Site Information widget
 */
@CommonsLog
public class WidgetPage extends WebPage {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.site.api.SiteService")
	private SiteService siteService;
	
	@SpringBean(name = "org.sakaiproject.tool.api.ToolManager")
	private ToolManager toolManager;

	@SpringBean(name = "org.sakaiproject.component.api.ServerConfigurationService")
	private ServerConfigurationService serverConfigurationService;

	public WidgetPage() {
		log.debug("WidgetPage()");
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final String currentSiteId = this.toolManager.getCurrentPlacement().getContext();
		
		try {
			Site site = siteService.getSite(currentSiteId);
			add(new Label("siteinfo", Model.of(site.getDescription())).setEscapeModelStrings(false));
		} catch (IdUnusedException e) {
			//almost impossible since we just got the tool placement, but anyway...
			e.printStackTrace();
		}
		
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		
		// get the Sakai skin header fragment from the request attribute
		final HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();

		response.render(StringHeaderItem.forString((String) request.getAttribute("sakai.html.head")));
		response.render(OnLoadHeaderItem.forScript("setMainFrameHeight( window.name )"));

		// Tool additions (at end so we can override if required)
		response.render(StringHeaderItem.forString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"));
	}

	
}
