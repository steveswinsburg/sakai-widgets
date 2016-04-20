
package org.sakaiproject.widgets.sitemembers;

/**
 * Represents the roles used in the site. Users are categorised to one of these.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public enum SiteRole {

	STUDENT("section.role.student"),
	TA("section.role.ta"),
	INSTRUCTOR("section.role.instructor");

	private String value;

	SiteRole(final String value) {
		this.value = value;
	}

	/**
	 * Get the Sakai value for the role
	 *
	 * @return
	 */
	public String getValue() {
		return this.value;
	}

}
