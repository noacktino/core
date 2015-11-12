package de.btu.openinfra.backend.rest.project.test;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.btu.openinfra.backend.db.pojos.LocalizedString;
import de.btu.openinfra.backend.db.pojos.project.ProjectPojo;

public class OpenInfraJunitTest {

	private static final String SERVER =
			"http://localhost:8080/";
	private static final String REST_URI = "openinfra_core/rest/v1/";

	private WebTarget target;
	ArrayList<Cookie> cookies = null;

	@Before
	public void setUp() {
		Client client = ClientBuilder.newClient();
		client.register(new OpenInfraFormAuthenticationFilter());
		target = client.target(SERVER);

		//cookies = login("root", "root");
	}

//	private ArrayList<Cookie> login(String username, String password) {
//		Form form = new Form();
//		form.param("username", username);
//		form.param("password", password);
//
//		Response res = target.path("openinfra_core/login.jsp").request(
//				MediaType.APPLICATION_JSON_TYPE).post(
//						Entity.entity(form,
//								MediaType.MULTIPART_FORM_DATA_TYPE));
//		ArrayList<Cookie> cc = new ArrayList<Cookie>();
//		cc.addAll(res.getCookies().values());
//		return cc;
//	}

	@Test
	public void getVersion() {
		String version = target.path(
				"openinfra_core/rest/version").request().get(String.class);
		System.out.print("Testing version ");
		Assert.assertNotNull(version);
		System.out.println("--> " + version);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getProjects() {
		//System.out.println("Cookie: " + cookies);

		Response res = target.path("openinfra_core/rest/v1/projects")
				.request(MediaType.APPLICATION_JSON).get();

		Assert.assertNotNull(res);
		System.out.println(res.getEntity()  + " -- " + Status.fromStatusCode(res.getStatus()));
		for(ProjectPojo pp : (List<ProjectPojo>)res.getEntity()) {
			for(LocalizedString ls : pp.getNames().getLocalizedStrings()) {
				System.out.println(ls.getCharacterString());
			}
		}
	}

}
