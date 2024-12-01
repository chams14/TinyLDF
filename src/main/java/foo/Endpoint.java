package foo;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.UnauthorizedException;


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
}
