
package org.sakaiproject.widgets.sitemembers.ui.components;

import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.profile2.model.BasicConnection;

/**
 * Generic grid panel that can be used to render a list of {@link BasicConnection}s
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

				final String url = "/direct/my/profile-view/" + connection.getUuid();

				// This is currently a popup as we are in a frame. Once we acn render inline this can be changed to a normal link
				final PopupSettings settings = new PopupSettings();
				settings.setTarget("_parent");

				final ExternalLink itemWrap = new ExternalLink("itemWrap", url);
				itemWrap.setPopupSettings(settings);

				itemWrap.add(new ProfileThumbnail("img", Model.of(connection.getUuid())));
				// itemWrap.add(new Label("name", Model.of(connection.getDisplayName())));
				item.add(itemWrap);

			}

			@Override
			protected void populateEmptyItem(final Item<BasicConnection> item) {

				final WebMarkupContainer itemWrap = new WebMarkupContainer("itemWrap");
				itemWrap.add(new EmptyPanel("img"));
				// itemWrap.add(new EmptyPanel("name"));
				item.add(itemWrap);
				item.setVisible(false);
			}
		};

		gridView.setRows(4);
		gridView.setColumns(3);

		add(gridView);

	}

}
