
package org.sakaiproject.widgets.myconnections;

import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.model.BasicConnection;
import org.sakaiproject.tool.api.SessionManager;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class BasePage extends WebPage {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
	private ProfileConnectionsLogic connectionsLogic;

	@SpringBean(name = "org.sakaiproject.tool.api.SessionManager")
	private SessionManager sessionManager;

	public BasePage() {
		log.debug("BasePage()");

	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// get current user
		final String currentUserUuid = this.sessionManager.getCurrentSessionUserId();

		// get connections
		final List<BasicConnection> connections = this.connectionsLogic.getBasicConnectionsForUser(currentUserUuid);

		final ListDataProvider<BasicConnection> dataProvider = new ListDataProvider<BasicConnection>(connections);

		final GridView<BasicConnection> gridView = new GridView<BasicConnection>("rows", dataProvider) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final Item<BasicConnection> item) {
				final BasicConnection connection = item.getModelObject();
				item.add(new ProfileThumbnail("img", Model.of(connection.getUuid())));
			}

			@Override
			protected void populateEmptyItem(final Item<BasicConnection> item) {
				item.add(new EmptyPanel("img"));
			}
		};

		gridView.setRows(4);
		gridView.setColumns(3);

		add(gridView);

	}

}
