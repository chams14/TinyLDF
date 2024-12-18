package foo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

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

import com.opencsv.CSVReader;


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

	// TODO: Make sure can only be run by someone logged
	// Populate the DataStore from the dataset.csv file
	// Be extremely carefull while calling it, if run a second time, will add the data in double
	// URL follow the following form:
	// https://cloud-tinyldf.appspot.com/_ah/api/myTinyApi/v1/populateDatastore?batchSize=1000
	@ApiMethod(name = "populateDatastore", path = "populateDatastore", httpMethod = HttpMethod.GET)
	public Entity populateDatastore(@Named("batchSize") int batchSize) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		String csvFile = getClass().getClassLoader().getResource("dataset.csv").getPath();
		int totalAdded = 0;

		try (CSVReader csvReader = new CSVReader(new BufferedReader(new FileReader(csvFile)))) {
			String[] row;
			List<Entity> batch = new ArrayList<>();

			// Skip header
			csvReader.readNext();

			while ((row = csvReader.readNext()) != null) {
				// Create a Quad entity
				Entity e = new Entity("Quad");
				e.setProperty("subject", row[0]);
				e.setProperty("predicate", row[1]);
				e.setProperty("object", row[2]);
				e.setProperty("graph", "http://example.org/graph/2024_medalists_all");

				// Add to batch
				batch.add(e);

				// Process batch when it reaches the batch size
				if (batch.size() >= batchSize) {
					datastore.put(batch);
					totalAdded += batch.size();
					batch.clear(); // Clear batch
				}
			}

			// Write remaining entities in the batch
			if (!batch.isEmpty()) {
				datastore.put(batch);
				totalAdded += batch.size();
			}

			// Return summary
			Entity result = new Entity("Result");
			result.setProperty("elementsAdded", totalAdded);
			return result;

		} catch (Exception e) {
			e.printStackTrace();
			Entity errorEntity = new Entity("Error");
			errorEntity.setProperty("message", "Error processing file: " + e.getMessage());
			return errorEntity;
		}
	}
}
