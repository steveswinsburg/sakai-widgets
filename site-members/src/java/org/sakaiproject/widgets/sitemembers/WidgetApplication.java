package org.sakaiproject.widgets.sitemembers;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.sakaiproject.widgets.sitemembers.ui.WidgetPage;

/**
 * App class for My Connections widget
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class WidgetApplication extends WebApplication {

	@Override
	public void init() {
		super.init();

		// Configure for Spring injection
		getComponentInstantiationListeners().add(new SpringComponentInjector(this));

		// Don't throw an exception if we are missing a property, just fallback
		getResourceSettings().setThrowExceptionOnMissingResource(false);

		// Remove the wicket specific tags from the generated markup
		getMarkupSettings().setStripWicketTags(true);

		// Don't add any extra tags around a disabled link (default is <em></em>)
		getMarkupSettings().setDefaultBeforeDisabledLink(null);
		getMarkupSettings().setDefaultAfterDisabledLink(null);

		// On Wicket session timeout, redirect to main page
		getApplicationSettings().setPageExpiredErrorPage(getHomePage());

		// cleanup the HTML
		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setCompressWhitespace(true);
		
		// Suppress internal jQuery
		getJavaScriptLibrarySettings().setJQueryReference(new UrlResourceReference(Url.parse("/site-members/scripts/wicket/empty.js")));
		
		// To support inline rendering, these references need to be corrected so the URL to them renders correctly
		// Without this it sets the context path to /page/ instead of the webapp name.
		// These were extracted from Wicket and should be kept in sync if Wicket is upgraded
		getJavaScriptLibrarySettings().setWicketEventReference(new UrlResourceReference(Url.parse("/site-members/scripts/wicket/wicket-event-jquery.min.js")));

		// to put this app into deployment mode, see web.xml
	}

	/**
	 * The main page for our app
	 *
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<WidgetPage> getHomePage() {
		return WidgetPage.class;
	}

	/**
	 * Constructor
	 */
	public WidgetApplication() {
	}

}
