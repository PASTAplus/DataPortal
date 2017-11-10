package edu.lternet.pasta.portal;

/*
* Copyright 2017 the University of New Mexico.
*
* This work was supported by National Science Foundation Cooperative
* Agreements #DEB-0832652 and #DEB-0936498.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0.
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
* either express or implied. See the License for the specific
* language governing permissions and limitations under the License.
*
*/

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.portal.ConfigurationListener;


/**
* @author Duane Costa
* @since  November 9, 2017
* 
*/
public class HarvestReportTest {

	/*
	 * Class variables
	 */

	
	/*
	 * Instance variables
	 */

	
	/*
	 * Methods
	 */

	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConfigurationListener.configure();
		PropertiesConfiguration options = ConfigurationListener.getOptions();

		if (options == null) {
			fail("Failed to load the DataPortal properties file: 'dataportal.properties'");
		} 
	}

	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	
	/**
	 * Test PastaClient.pastaURLtoPackageId() method.
	 */
	@Test 
	public void testComposeHarvesterPathSubdir() {
		final String path1 = HarvestReport.composeHarvesterPathSubdir("/home/pasta/local/harvester",
				"uid=LNO,o=LTER,dc=ecoinformatics,dc=org");
		assertEquals(path1, "/home/pasta/local/harvester/LTER-ecoinformatics-org");

		final String path2 = HarvestReport.composeHarvesterPathSubdir("/home/pasta/local/harvester",
				"uid=jsmith,o=EDI,dc=edirepository,dc=org");
		assertEquals(path2, "/home/pasta/local/harvester/EDI-edirepository-org");
	}

}
