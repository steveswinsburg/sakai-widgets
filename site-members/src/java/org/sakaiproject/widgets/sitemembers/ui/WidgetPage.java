
package org.sakaiproject.widgets.sitemembers.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.api.ServerConfigurationService;
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

	@SpringBean(name = "org.sakaiproject.component.api.ServerConfigurationService")
	private ServerConfigurationService serverConfigurationService;

	/**
	 * Maximum number of users to show in each section
	 *
	 * Can be overridden via: widget.sitemembers.maxusers=30
	 */
	int maxUsers = 30;

	public WidgetPage() {
		log.debug("WidgetPage()");
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// get max profiles
		this.maxUsers = this.serverConfigurationService.getInt("widget.sitemembers.maxusers", this.maxUsers);

		// get current site id
		final String currentSiteId = this.toolManager.getCurrentPlacement().getContext();

		// get members
		final List<BasicConnection> instructors = getMembersWithRole(currentSiteId, SiteRole.INSTRUCTOR);
		final List<BasicConnection> tas = getMembersWithRole(currentSiteId, SiteRole.TA);
		final List<BasicConnection> students = getMembersWithRole(currentSiteId, SiteRole.STUDENT);

		// note that none of these sections show if they are empty

		// add instructors grid
		add(new ConnectionsGrid("instructors", Model.ofList(instructors)) {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public boolean isVisible() {
				return !((List<BasicConnection>) getDefaultModelObject()).isEmpty();
			}
		});

		// add TAs grid
		add(new ConnectionsGrid("tas", Model.ofList(tas)) {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public boolean isVisible() {
				return !((List<BasicConnection>) getDefaultModelObject()).isEmpty();
			}
		});

		// add students grid
		add(new ConnectionsGrid("members", Model.ofList(students)) {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public boolean isVisible() {
				return !((List<BasicConnection>) getDefaultModelObject()).isEmpty();
			}
		});

	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		
		final String version = this.serverConfigurationService.getString("portal.cdn.version", "");

		// get the Sakai skin header fragment from the request attribute
		final HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();

		response.render(StringHeaderItem.forString((String) request.getAttribute("sakai.html.head")));
		response.render(OnLoadHeaderItem.forScript("setMainFrameHeight( window.name )"));

		// Tool additions (at end so we can override if required)
		response.render(StringHeaderItem.forString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"));

		// widget specific styles
		response.render(CssHeaderItem.forUrl(String.format("/site-members/styles/widget-styles.css?version=%s", version)));
		
		// render jQuery and the Wicket event library
		// Both must be priority so they are emitted into the head
		response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forUrl(String.format("/library/webjars/jquery/1.11.3/jquery.min.js?version=%s", version))));
		response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forUrl(String.format("/my-calendar/scripts/wicket/wicket-event-jquery.min.js?version=%s", version))));
		
		// NOTE: All libraries apart from jQuery and Wicket Event must be rendered inline with the application. See WidgetPage.html.
	}

	/**
	 * Get the members with the given role.
	 *
	 * Sorted by online status then display name. Maximum returned is 30 but can be overridden.
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

			// sort
			Collections.sort(rval, new Comparator<BasicConnection>() {

				@Override
				public int compare(final BasicConnection o1, final BasicConnection o2) {
					return new CompareToBuilder()
							.append(o1.getOnlineStatus(), o2.getOnlineStatus())
							.append(o1.getDisplayName(), o2.getDisplayName())
							.toComparison();
				}

			});

			// get slice
			rval = rval
					.stream()
					.limit(this.maxUsers)
					.collect(Collectors.toList());

		} catch (final IdUnusedException e) {
			e.printStackTrace();
		}

		return rval;
	}

}
