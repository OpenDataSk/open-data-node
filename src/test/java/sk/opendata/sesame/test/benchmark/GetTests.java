package sk.opendata.sesame.test.benchmark;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

// looks like it needs H2 DB to work - see http://labs.carrotsearch.com/junit-benchmarks-tutorial.html
@BenchmarkMethodChart(filePrefix = "benchmark-gets")
public class GetTests {

	private static SesameBenchmark sesameBenchmark = null;

	public final static String FULL_ORGANISATIONS_DATA = "/tmp/organisations-dump-full.rdf";

	@Rule
	public MethodRule benchmarkRun = new BenchmarkRule();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sesameBenchmark = new SesameBenchmark();
		sesameBenchmark.createRepository(SesameBenchmark.SESAME_DATA_DIR);
		sesameBenchmark.prepareConnection();

		sesameBenchmark.fillRepository(FULL_ORGANISATIONS_DATA);
		sesameBenchmark.fillRepository(SesameBenchmark.PROCUREMENT_DATA);
		sesameBenchmark.fillRepository(SesameBenchmark.DONORS_DATA);
	}

	@BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 0)
	@Test
	public void testGetNameIcoList() {
		TupleQueryResult nameIcoListTQR;
		int count = 0;

		try {
			nameIcoListTQR = sesameBenchmark.getNameIcoList();
			while(nameIcoListTQR.hasNext()) {
				count++;
				nameIcoListTQR.next();
			}
			nameIcoListTQR.close();
		} catch (QueryEvaluationException e) {
			assertFalse(true);
		}

		assertEquals(99999, count);
	}

	@BenchmarkOptions(benchmarkRounds = 1000, warmupRounds = 0)
	@Test
	public void testGetOrgByIco() {
		TupleQueryResult orgTQR;
		int count = 0;
		String orgName = null;

		try {
			orgTQR = sesameBenchmark.getOrgByIco(SesameBenchmark.TEST_ICO);
			while(orgTQR.hasNext()) {
				count++;
				BindingSet bindingSet = orgTQR.next();
				orgName = bindingSet.getBinding("name").getValue().stringValue();
			}
			orgTQR.close();
		} catch (QueryEvaluationException e) {
			assertFalse(true);
		}

		assertEquals(1, count);
		assertEquals("Roland Kolláth - COLLARD DESING", orgName);
	}

	@BenchmarkOptions(benchmarkRounds = 1000, warmupRounds = 0)
	@Test
	public void testGetDonorAndSupplierList() {
		TupleQueryResult donorAndSupplierListTQR;
		int count = 0;

		try {
			donorAndSupplierListTQR = sesameBenchmark.getDonorAndSupplierList();
			while(donorAndSupplierListTQR.hasNext()) {
				count++;
				donorAndSupplierListTQR.next();
			}
			donorAndSupplierListTQR.close();
		} catch (QueryEvaluationException e) {
			assertFalse(true);
		}

		assertEquals(20, count);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		sesameBenchmark.close();
	}

}
