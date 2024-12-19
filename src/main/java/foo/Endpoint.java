package foo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.PreparedQuery;

import com.opencsv.CSVReader;


@Api(name = "myTinyApi",
	version = "v1",
	audiences = "588494979006-egm3iejpi8kbtc25i6691uaevhkfjtr3.apps.googleusercontent.com",
	clientIds = {"588494979006-egm3iejpi8kbtc25i6691uaevhkfjtr3.apps.googleusercontent.com",
	"1070713493928-c7g4vl5i7k6vcbaasgihfnajktpai0ut.apps.googleusercontent.com"},
	namespace =
     	@ApiNamespace(
		   ownerDomain = "helloworld.example.com",
		   ownerName = "helloworld.example.com",
		   packagePath = "")
    )

public class Endpoint {
	// URL follow the following form:
	// https://cloud-tinyldf.appspot.com/_ah/api/myTinyApi/v1/Hello?access_token=...
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
	public Entity addQuad(@Named("subject") String subject, @Named("predicate") String predicate, @Named("object") String object, @Named("graph") String graph) {
		
		Entity e = new Entity("Quad");
		e.setProperty("subject", subject);
		e.setProperty("predicate", predicate);
		e.setProperty("object", object);
		e.setProperty("graph", graph);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);

		return e;
	}

	// Secure version of the previous method
	// URL follow the following form:
	// https://cloud-tinyldf.appspot.com/_ah/api/myTinyApi/v1/addQuad?subject=Alice&predicate=knows&object=Bob&graph=hello&access_token=...
	@ApiMethod(name = "addQuadSec", path = "addQuadSec", httpMethod = HttpMethod.GET)
	public Entity addQuadSec(User user, @Named("subject") String subject, @Named("predicate") String predicate, @Named("object") String object, @Named("graph") String graph) throws UnauthorizedException {
		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}

		Entity e = new Entity("Quad");
		e.setProperty("subject", subject);
		e.setProperty("predicate", predicate);
		e.setProperty("object", object);
		e.setProperty("graph", graph);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);

		return e;
	}

	// Populate the DataStore from the dataset.csv file using batch put of size batchSize
	// Be extremely carefull while calling it.
	// If run a second time, the data will end up in double in the Datastore.
	// URL follow the following form:
	// https://cloud-tinyldf.appspot.com/_ah/api/myTinyApi/v1/populateDatastore?batchSize=1000&access_token=...
	@ApiMethod(name = "populateDatastore", path = "populateDatastore", httpMethod = HttpMethod.GET)
	public Entity populateDatastore(User user, @Named("batchSize") int batchSize) throws UnauthorizedException {
		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}

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

	// URL follow the following form:
	// https://cloud-tinyldf.appspot.com/_ah/api/myTinyApi/v1/ldf?predicate=http%3A%2F%2Fexample.org%2Fnuts3_population&graph=http%3A%2F%2Fexample.org%2Fgraph%2F2024_medalists_all
	@ApiMethod(name = "ldf", path = "ldf", httpMethod = HttpMethod.GET)
	public CollectionResponse<Entity> ldf(
    @Nullable @Named("subject") String subject,
    @Nullable @Named("predicate") String predicate,
    @Nullable @Named("object") String object,
    @Nullable @Named("graph") String graph,
	@Nullable @Named("cursor") String cursorString) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		long startTime = System.currentTimeMillis();

		// Initialize the base query
		Query query = new Query("Quad");

		// Create a list to hold query filters
		List<Query.Filter> filters = new ArrayList<>();
	
		// Add filters if the parameter is present
		if (subject != null && !subject.isEmpty()) {
			filters.add(new Query.FilterPredicate("subject", Query.FilterOperator.EQUAL, subject));
		}
		if (predicate != null && !predicate.isEmpty()) {
			filters.add(new Query.FilterPredicate("predicate", Query.FilterOperator.EQUAL, predicate));
		}
		if (object != null && !object.isEmpty()) {
			filters.add(new Query.FilterPredicate("object", Query.FilterOperator.EQUAL, object));
		}
		if (graph != null && !graph.isEmpty()) {
			filters.add(new Query.FilterPredicate("graph", Query.FilterOperator.EQUAL, graph));
		}
	
		// If present, combine filters with AND
		if (!filters.isEmpty()) {
			Query.Filter compositeFilter;
			if (filters.size() == 1) {
				// Only one filter
				compositeFilter = filters.get(0);
			} else {
				// Multiple filters combined with AND
				compositeFilter = Query.CompositeFilterOperator.and(filters);
			}
			query.setFilter(compositeFilter);
		}
		// Else equivalent to SELECT *

		// Prepare the query
		PreparedQuery preparedQuery = datastore.prepare(query);

		// Limit to 50 results per page
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(50);

		// Use the cursor if provided
		if (cursorString != null && !cursorString.isEmpty()) {
			fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
		}
	
		// Execute the query and retrieve results
		QueryResultList<Entity> results = preparedQuery.asQueryResultList(fetchOptions);
	
		// Get the next page cursor
		String nextCursorString = results.getCursor().toWebSafeString();

		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;
	
		// Return results along with the cursor for the next page
		return CollectionResponse.<Entity>builder()
			.setItems(results)
			.setNextPageToken(nextCursorString)
			.build();
	}
	
    // https://tinyldf-445019.ew.r.appspot.com/_ah/api/myTinyApi/v1/getQuads
    @ApiMethod(name = "getQuads", path = "getQuads", httpMethod = HttpMethod.GET)
    public List<Entity> getQuads() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();


        Query query = new Query("Quad");
        PreparedQuery pq = datastore.prepare(query);
        List<Entity> results = new ArrayList<>();
   
        for (Entity entity : pq.asIterable()) {
            results.add(entity);
        }
        return results;
    }

}
