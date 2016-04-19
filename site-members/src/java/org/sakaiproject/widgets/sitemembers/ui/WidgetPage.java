
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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.model.BasicConnection;
import org.sakaiproject.profile2.model.BasicPerson;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.widgets.sitemembers.ui.components.ProfileThumbnail;

import lombok.extern.apachecommons.CommonsLog;

/**
 * Main page for the Site Members widget
 */
@CommonsLog
public class WidgetPage extends WebPage {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
	private ProfileConnectionsLogic connectionsLogic;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileLogic")
	private ProfileLogic profileLogic;

	@SpringBean(name = "org.sakaiproject.tool.api.SessionManager")
	private SessionManager sessionManager;

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

		// get current user
		final String currentUserUuid = this.sessionManager.getCurrentSessionUserId();

		// get current site id
		final String currentSiteId = this.toolManager.getCurrentPlacement().getContext();

		// get connections
		final List<BasicConnection> connections = this.connectionsLogic.getBasicConnectionsForUser(currentUserUuid);

		final ListDataProvider<BasicConnection> dataProvider = new ListDataProvider<BasicConnection>(connections);

		final GridView<BasicConnection> gridView = new GridView<BasicConnection>("rows", dataProvider) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final Item<BasicConnection> item) {
				final BasicConnection connection = item.getModelObject();

				final String url = "/direct/my/profile-view/" + connection.getUuid();

				final PopupSettings settings = new PopupSettings();
				settings.setTarget("_parent");

				final ExternalLink itemWrap = new ExternalLink("itemWrap", url);
				itemWrap.setPopupSettings(settings);

				itemWrap.add(new ProfileThumbnail("img", Model.of(connection.getUuid())));
				itemWrap.add(new Label("name", Model.of(connection.getDisplayName())));
				item.add(itemWrap);

			}

			@Override
			protected void populateEmptyItem(final Item<BasicConnection> item) {

				final WebMarkupContainer itemWrap = new WebMarkupContainer("itemWrap");
				itemWrap.add(new EmptyPanel("img"));
				itemWrap.add(new EmptyPanel("name"));
				item.add(itemWrap);
				item.setVisible(false);
			}
		};

		gridView.setRows(4);
		gridView.setColumns(3);

		add(gridView);

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
	 * Get the instructors
	 *
	 * @param siteId the site id to get the instructors for
	 * @return list of {@link BasicPerson} or an empty list if none
	 */
	final List<BasicPerson> getInstructors(final String siteId) {

		List<BasicPerson> rval = new ArrayList<>();

		try {
			final Set<String> userUuids = this.siteService.getSite(siteId).getUsersIsAllowed("section.role.instructor");
			final List<User> users = this.userDirectoryService.getUsers(userUuids);
			rval = this.profileLogic.getBasicPersons(users);
		} catch (final IdUnusedException e) {
			e.printStackTrace();
		}

		return rval;
	}

}
