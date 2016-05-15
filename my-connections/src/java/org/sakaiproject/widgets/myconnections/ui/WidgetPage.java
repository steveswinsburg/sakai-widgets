
package org.sakaiproject.widgets.myconnections.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.model.BasicConnection;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.widgets.myconnections.ui.components.ConnectionsGrid;

import lombok.extern.apachecommons.CommonsLog;

/**
 * Main page for the My Connections widget
 */
@CommonsLog
public class WidgetPage extends WebPage {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
	private ProfileConnectionsLogic connectionsLogic;

	@SpringBean(name = "org.sakaiproject.tool.api.SessionManager")
	private SessionManager sessionManager;

	@SpringBean(name = "org.sakaiproject.component.api.ServerConfigurationService")
	private ServerConfigurationService serverConfigurationService;

	/**
	 * Maximum number of profiles to show.
	 *
	 * Can be overridden via: widget.myconnections.maxusers=30
	 */
	int maxUsers = 30;

	public WidgetPage() {
		log.debug("WidgetPage()");
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// get max users
		this.maxUsers = this.serverConfigurationService.getInt("widget.myconnections.maxusers", this.maxUsers);

		// get current user
		final String currentUserUuid = this.sessionManager.getCurrentSessionUserId();

		// get connections, sort and slice
		List<BasicConnection> connections = this.connectionsLogic.getBasicConnectionsForUser(currentUserUuid);

		// sort
		Collections.sort(connections, new Comparator<BasicConnection>() {

			@Override
			public int compare(final BasicConnection o1, final BasicConnection o2) {
				return new CompareToBuilder()
						.append(o1.getOnlineStatus(), o2.getOnlineStatus())
						.append(o1.getDisplayName(), o2.getDisplayName())
						.toComparison();
			}

		});

		// get slice
		connections = connections
				.stream()
				.limit(this.maxUsers)
				.collect(Collectors.toList());

		// add connections grid or label
		if (!connections.isEmpty()) {
			add(new ConnectionsGrid("connections", Model.ofList(connections)));
		} else {
			add(new Label("connections", new ResourceModel("label.noconnections"))
					.add(new AttributeAppender("class", "instruction")));
		}

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
		final String version = this.serverConfigurationService.getString("portal.cdn.version", "");
		response.render(CssHeaderItem.forUrl(String.format("/my-connections/styles/widget-styles.css?version=%s", version)));

	}

}
