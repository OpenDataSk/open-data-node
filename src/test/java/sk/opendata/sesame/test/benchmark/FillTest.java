package sk.opendata.sesame.test.benchmark;

import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;

public class FillTest {

	private SesameBenchmark sesameBenchmark = null;

	@Rule
	public MethodRule benchmarkRun = new BenchmarkRule();

	@Before
	public void setUp() throws Exception {
		sesameBenchmark = new SesameBenchmark();
		sesameBenchmark.createRepository(SesameBenchmark.SESAME_DATA_DIR);
		sesameBenchmark.prepareConnection();
	}

	@BenchmarkOptions(benchmarkRounds = 10)
	@Test
	public void testFillRepository() {
		boolean exception = false;

		try {
			sesameBenchmark.fillRepository(SesameBenchmark.ORGANISATIONS_DATA);
		} catch (RepositoryException e) {
			exception = true;
		} catch (RDFParseException e) {
			exception = true;
		} catch (IOException e) {
			exception = true;
		}

		assertFalse(exception);
	}

	@After
	public void tearDown() throws Exception {
		sesameBenchmark.close();
	}

}
