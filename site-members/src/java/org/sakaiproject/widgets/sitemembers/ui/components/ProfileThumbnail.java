
package org.sakaiproject.widgets.sitemembers.ui.components;

import java.util.concurrent.TimeUnit;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;

/**
 * Renders a user's profile thumbnail via the direct entity URL.
 *
 * Attach to an img tag and provide the user uuid in the model
 *
 * e.g. <code>
 * &lt;img wicket:id="photo" /&gt;
 * </code> <br />
 * <code>
 * add(new ProfileImage("photo", new Model<String>(userUuid)));
 * </code>
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class ProfileThumbnail extends WebComponent {

	private static final long serialVersionUID = 1L;

	public ProfileThumbnail(final String id, final IModel<String> model) {
		super(id, model);
	}

	@Override
	protected void onComponentTag(final ComponentTag tag) {
		super.onComponentTag(tag);
		checkComponentTag(tag, "img");

		final String userUuid = this.getDefaultModelObjectAsString();

		// Cache for a minute
		final String url = "/direct/profile/" + userUuid + "/image/thumb" + "?t="
				+ TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis());

		tag.put("src", url);
	}

}
