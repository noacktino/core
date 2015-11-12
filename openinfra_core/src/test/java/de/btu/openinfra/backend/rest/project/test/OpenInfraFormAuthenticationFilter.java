package de.btu.openinfra.backend.rest.project.test;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class OpenInfraFormAuthenticationFilter implements ClientRequestFilter {

	private List<Object> cookies = null;
	private static final String utf8 = "UTF-8";

	public OpenInfraFormAuthenticationFilter() {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:8080");
		Form form = new Form();
		form.param("username", "root");
		form.param("password", "root");

		Response res = target.path("openinfra_core/login.jsp").request(
				MediaType.APPLICATION_JSON_TYPE).post(
						Entity.entity(form,
								MediaType.MULTIPART_FORM_DATA));
		cookies = new ArrayList<Object>();
		System.out.println(res.getCookies().values().size());
		try {
			for(Cookie c : res.getCookies().values()) {
				if(c.getDomain() != null) {
					cookies.add(
							new Cookie(
									URLEncoder.encode(c.getName(), utf8),
									URLEncoder.encode(c.getValue(), utf8),
									URLEncoder.encode(c.getPath(), utf8),
									URLEncoder.encode(c.getDomain(), utf8)));
				} else {
					cookies.add(
							new Cookie(
									URLEncoder.encode(c.getName(), utf8),
									URLEncoder.encode(c.getValue(), utf8)));
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		System.out.println(cookies);
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		System.out.println("filter called");
		if(cookies != null) {
			requestContext.getHeaders().put("Cookie", cookies);
		}
	}

}
