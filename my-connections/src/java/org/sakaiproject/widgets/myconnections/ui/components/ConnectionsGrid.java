
package org.sakaiproject.widgets.myconnections.ui.components;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.profile2.model.BasicConnection;

/**
 * Generic grid panel that can be used to render a list of {@link BasicConnection}s as their images
 */
public class ConnectionsGrid extends Panel {

	private static final long serialVersionUID = 1L;

	public ConnectionsGrid(final String id, final IModel<List<? extends BasicConnection>> iModel) {
		super(id, iModel);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		@SuppressWarnings("unchecked")
		final List<BasicConnection> connections = (List<BasicConnection>) getDefaultModelObject();

		final ListDataProvider<BasicConnection> dataProvider = new ListDataProvider<BasicConnection>(connections);

		final GridView<BasicConnection> gridView = new GridView<BasicConnection>("rows", dataProvider) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final Item<BasicConnection> item) {
				final BasicConnection connection = item.getModelObject();
				final ProfileThumbnail img = new ProfileThumbnail("img", Model.of(connection.getUuid()));
				
				final String url = "/direct/my/profile-view/" + connection.getUuid();
				
				img.add(new AttributeModifier("href", url));
				img.add(new AttributeModifier("target", "_top"));
				//img.add(new AttributeModifier("title", connection.getDisplayName()));
				item.add(img);
				
				//name link
				item.add(new ExternalLink("name", url, connection.getDisplayName()));
			}

			@Override
			protected void populateEmptyItem(final Item<BasicConnection> item) {
				item.add(new EmptyPanel("img"));
				item.add(new EmptyPanel("name"));
				item.setVisible(false);
			}
		};

		gridView.setRows(8);
		gridView.setColumns(4);

		add(gridView);

	}

}
