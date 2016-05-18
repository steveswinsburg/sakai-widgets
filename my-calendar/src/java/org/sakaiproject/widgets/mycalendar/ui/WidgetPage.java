
package org.sakaiproject.widgets.mycalendar.ui;

import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;

public class WidgetPage extends WebPage {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.tool.api.ToolManager")
	private ToolManager toolManager;

	@SpringBean(name = "org.sakaiproject.tool.api.SessionManager")
	private SessionManager sessionManager;

	@SpringBean(name = "org.sakaiproject.user.api.PreferencesService")
	private PreferencesService preferencesService;

	@SpringBean(name = "org.sakaiproject.component.api.ServerConfigurationService")
	private ServerConfigurationService serverConfigurationService;

	public WidgetPage() {
		// log.debug("WidgetPage()");
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// setup the data for the page
		final Label data = new Label("data");
		data.add(new AttributeAppender("data-siteid", getCurrentSiteId()));
		data.add(new AttributeAppender("data-tz", getUserTimeZone().getID()));

		add(data);
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

		// for datepicker
		response.render(new PriorityHeaderItem(
				JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference())));
		response.render(CssHeaderItem.forUrl(String.format("/my-calendar/styles/jquery-ui.min.css?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/my-calendar/scripts/jquery-ui.min.js?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/my-calendar/scripts/moment.js?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/my-calendar/scripts/moment-timezone.js?version=%s", version)));

		// widget specific styles
		response.render(CssHeaderItem.forUrl(String.format("/my-calendar/styles/widget-styles.css?version=%s", version)));
	}

	/**
	 * Get the current siteId
	 *
	 * @return
	 */
	private String getCurrentSiteId() {
		try {
			return this.toolManager.getCurrentPlacement().getContext();
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Get current user id
	 *
	 * @return
	 */
	private String getCurrentUserId() {
		return this.sessionManager.getCurrentSessionUserId();
	}

	/**
	 * Get a user's timezone from their preferences.
	 *
	 * @param userUuid uuid of the user to get preferences for
	 * @return TimeZone from user preferences or the default timezone of the server if none is set
	 */
	private TimeZone getUserTimeZone() {

		TimeZone timezone;
		final Preferences prefs = this.preferencesService.getPreferences(getCurrentUserId());
		final ResourceProperties props = prefs.getProperties(TimeService.APPLICATION_ID);
		final String tzPref = props.getProperty(TimeService.TIMEZONE_KEY);

		if (StringUtils.isNotBlank(tzPref)) {
			timezone = TimeZone.getTimeZone(tzPref);
		} else {
			timezone = TimeZone.getDefault();
		}

		return timezone;
	}

}
