
package org.sakaiproject.widgets.sitemembers.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.model.BasicConnection;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.widgets.sitemembers.SiteRole;
import org.sakaiproject.widgets.sitemembers.ui.components.ConnectionsGrid;

import lombok.extern.apachecommons.CommonsLog;

/**
 * Main page for the Site Members widget
 */
@CommonsLog
public class WidgetPage extends WebPage {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
	private ProfileConnectionsLogic connectionsLogic;

	@SpringBean(name = "org.sakaiproject.tool.api.ToolManager")
	private ToolManager toolManager;

	@SpringBean(name = "org.sakaiproject.site.api.SiteService")
	private SiteService siteService;

	@SpringBean(name = "org.sakaiproject.user.api.UserDirectoryService")
	private UserDirectoryService userDirectoryService;

	public WidgetPage() {
		log.debug("WidgetPage()");
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// get current site id
		final String currentSiteId = this.toolManager.getCurrentPlacement().getContext();

		// get members
		final List<BasicConnection> instructors = getMembersWithRole(currentSiteId, SiteRole.INSTRUCTOR);
		final List<BasicConnection> students = getMembersWithRole(currentSiteId, SiteRole.STUDENT);

		// add instructors grid
		add(new ConnectionsGrid("instructors", Model.ofList(instructors)));

		// add students grid
		add(new ConnectionsGrid("members", Model.ofList(students)));

	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		// get the Sakai skin header fragment from the request attribute
		final HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();

		response.render(new PriorityHeaderItem(
				JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference())));

		response.render(StringHeaderItem.forString((String) request.getAttribute("sakai.html.head")));
		response.render(OnLoadHeaderItem.forScript("setMainFrameHeight( window.name )"));

		// Tool additions (at end so we can override if required)
		response.render(StringHeaderItem.forString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"));

		// widget specific styles
		final String version = ServerConfigurationService.getString("portal.cdn.version", "");
		response.render(CssHeaderItem.forUrl(String.format("/site-members/styles/widget-styles.css?version=%s", version)));

	}

	/**
	 * Get the members with the given role
	 *
	 * @param siteId the site id to get the members for
	 * @param role the role we want to get the users for
	 * @return list of {@link BasicConnection} or an empty list if none
	 */
	final List<BasicConnection> getMembersWithRole(final String siteId, final SiteRole role) {

		List<BasicConnection> rval = new ArrayList<>();

		try {
			final Set<String> userUuids = this.siteService.getSite(siteId).getUsersIsAllowed(role.getValue());
			final List<User> users = this.userDirectoryService.getUsers(userUuids);
			rval = this.connectionsLogic.getBasicConnections(users);
		} catch (final IdUnusedException e) {
			e.printStackTrace();
		}

		return rval;
	}

}
