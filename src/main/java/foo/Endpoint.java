package foo;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;


@Api(name = "myTinyApi",
	version = "v1",
	audiences = "1070713493928-2131512am08sqkkbu9bt9l6236luga1k.apps.googleusercontent.com",
	clientIds = {"1070713493928-2131512am08sqkkbu9bt9l6236luga1k.apps.googleusercontent.com",
	"1070713493928-c7g4vl5i7k6vcbaasgihfnajktpai0ut.apps.googleusercontent.com"},
	namespace =
     	@ApiNamespace(
		   ownerDomain = "helloworld.example.com",
		   ownerName = "helloworld.example.com",
		   packagePath = "")
    )

public class Endpoint {
	@ApiMethod(name = "hello", httpMethod = HttpMethod.GET)
	public User Hello(User user) throws UnauthorizedException {
        if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
        System.out.println("Yeah : "+user.toString());
		return user;
	}

	// URL follow the following form:
	// https://cloud-tinyldf.appspot.com/_ah/api/myTinyApi/v1/addQuad?subject=Alice&predicate=knows&object=Bob&graph=hello
	@ApiMethod(name = "addQuad", path = "addQuad", httpMethod = HttpMethod.GET)
	public Entity addQuad(@Named("subject") String subject, @Named("predicate") String predicate, @Named("object") String object, @Named("graph") String graph) throws UnauthorizedException {

		Entity e = new Entity("Quad");
		e.setProperty("subject", subject);
		e.setProperty("predicate", predicate);
		e.setProperty("object", object);
		e.setProperty("graph", graph);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);

		return e;
	}

	// TODO: addQuadSecure
}
