package sk.opendata.sesame.test.benchmark;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.nativerdf.NativeStore;

public class SesameBenchmark {

	public final static String SESAME_DATA_DIR = "/tmp/sesame-test-datadir";
	public final static String ORGANISATIONS_DATA = "/tmp/organisations-dump.rdf";
	public final static String TEST_ICO = "30018340";

	private Repository myRepository = null;
	private RepositoryConnection con = null;
	private TupleQuery nameIcoListQuery = null;
	private TupleQuery orgByIcoQuery = null;
	private ValueFactory factory = null;

	/**
	 * Create a Sesame repository.
	 *
	 * @param dataDirName name of the directory used as Sesame homedir
	 * @throws RepositoryException
	 * @throws IOException
	 */
	public void createRepository(String dataDirName) throws RepositoryException, IOException {
		File dataDir = new File(dataDirName);

		// clean-up the repository
		FileUtils.deleteDirectory(dataDir);

		// create the repository
		// TODO:
		//String indexes = "spoc,posc,cosp";
		myRepository = new SailRepository(new NativeStore(dataDir/*, indexes*/));
		myRepository.initialize();
	}

	/**
	 * Open the connection to the repository and prepare query statements
	 * for later look-up operations.
	 *
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 */
	public void prepareConnection() throws RepositoryException, MalformedQueryException {
		con = myRepository.getConnection();

		final String PREFIXES =
			"PREFIX dc:<http://purl.org/dc/elements/1.1/>\n" +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
			"PREFIX owl:<http://www.w3.org/2002/07/owl#>\n" +
			"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n" +
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX opendata:<http://sk.eea.opendata/2011/02/opendicts>\n" +
			"PREFIX procurement:<http://opendata.cz/vocabulary/procurement.rdf#>\n" +
			"PREFIX skos:<http://www.w3.org/2004/02/skos/core#>\n";

		nameIcoListQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
				PREFIXES +
				"SELECT ?name ?ico\n" +
				"WHERE {\n" +
				"  ?org skos:prefLabel ?name .\n" +
				"  ?org opendata:ico ?ico .\n" +
				"}");

		orgByIcoQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
				PREFIXES +
				"SELECT DISTINCT ?name\n" +
				"WHERE {\n" +
				"  ?org skos:prefLabel ?name ;\n" +
				"    opendata:ico ?ico\n" +
				"}");

		factory = myRepository.getValueFactory();
	}

	public TupleQueryResult getNameIcoList() throws QueryEvaluationException {
		return nameIcoListQuery.evaluate();
	}

	public TupleQueryResult getOrgByIco(String ico) throws QueryEvaluationException {
		orgByIcoQuery.setBinding("ico", factory.createLiteral(ico));

		//System.out.println("  query: " + orgByIcoQuery.toString());

		return orgByIcoQuery.evaluate();
	}

	/**
	 * Fill sample data into the repository.
	 * @param organisationsRdfFileName sample RDF data created from Datanest CSV dump
	 * @throws RepositoryException
	 * @throws RDFParseException
	 * @throws IOException
	 */
	public void fillRepository(String organisationsRdfFileName) throws RepositoryException, RDFParseException, IOException {
		File organisationsRdfFile = new File(organisationsRdfFileName);
		con.add(organisationsRdfFile,
				"file://" + organisationsRdfFileName,
				RDFFormat.RDFXML);
	}

	public void close() throws RepositoryException {
		if (con != null)
			con.close();
	}


	public static void main(String [ ] args) {
		SesameBenchmark sesameBenchmark = new SesameBenchmark();

		try {
			System.out.println("--- creating repository");
			sesameBenchmark.createRepository(SESAME_DATA_DIR);
			System.out.println("--- preparing connection");
			sesameBenchmark.prepareConnection();

			System.out.println("--- filling repository");
			sesameBenchmark.fillRepository(ORGANISATIONS_DATA);

			System.out.println("--- listing all organizations");
			TupleQueryResult nameIcoListTQR = sesameBenchmark.getNameIcoList();
			int count = 0;
			while(nameIcoListTQR.hasNext()) {
				count++;
				nameIcoListTQR.next();
			}
			nameIcoListTQR.close();
			System.out.println("organisations: " + String.valueOf(count));

			System.out.println("--- looking for specific ICO");
			TupleQueryResult orgTQR = sesameBenchmark.getOrgByIco(TEST_ICO);
			count = 0;
			while(orgTQR.hasNext()) {
				count++;
				BindingSet bindingSet = orgTQR.next();
				Value orgName = bindingSet.getBinding("name").getValue();
				System.out.println("  org: " + orgName);
			}
			orgTQR.close();
			System.out.println("organisations with ICO "
					+ TEST_ICO
					+ ": " + String.valueOf(count));

			System.out.println("--- closing");
			sesameBenchmark.close();
		} catch (RepositoryException e) {
			System.err.println("Sesame repository exception: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOy exception: " + e.getMessage());
			e.printStackTrace();
		} catch (RDFParseException e) {
			System.err.println("RDF parsing exception: " + e.getMessage());
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			System.err.println("malformed query exception: " + e.getMessage());
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			System.err.println("query evaluation exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
