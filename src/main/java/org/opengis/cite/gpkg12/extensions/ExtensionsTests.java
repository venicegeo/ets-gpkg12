package org.opengis.cite.gpkg12.extensions;

import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.opengis.cite.gpkg12.CommonFixture;
import org.opengis.cite.gpkg12.ErrorMessage;
import org.opengis.cite.gpkg12.ErrorMessageKeys;
import org.opengis.cite.gpkg12.TestRunArg;
import org.opengis.cite.gpkg12.util.DatabaseUtility;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Defines test methods that apply to descriptive information about a
 * GeoPackage's content as it pertains to the metadata extension.
 *
 * <p style="margin-bottom: 0.5em">
 * <strong>Sources</strong>
 * </p>
 * <ul>
 * <li><a href="http://www.geopackage.org/spec/#_extension_mechanism" target= "_blank">
 * GeoPackage Encoding Standard - 2.3 Extension Mechanism</a> (OGC 12-128r13)</li>
 * </ul>
 *
 * @author Jeff Yutzler
 */
public class ExtensionsTests extends CommonFixture
{
//    /**
//     * Sets up variables used across methods
//     *
//     * @throws SQLException if there is a database error
//     */
//    @BeforeClass
//    public void setUp() throws SQLException
//    {
////        this.hasExtensionsTable          = ;
//    }

    @BeforeTest
    public void validateClassEnabled(ITestContext testContext) throws IOException {
      Map<String, String> params = testContext.getSuite().getXmlSuite().getParameters();
      final String pstr = params.get(TestRunArg.ICS.toString());
      final String testName = testContext.getName();
      HashSet<String> set = new HashSet<String>(Arrays.asList(pstr.split(",")));
      if (set.contains(testName)){
        Assert.assertTrue(true);
      } else {
        Assert.assertTrue(false, String.format("Conformance class %s is not enabled", testName));
      }
    }
    
    @BeforeClass
    public void validateTableExists(ITestContext testContext) throws SQLException {
    	Assert.assertTrue(DatabaseUtility.doesTableOrViewExist(this.databaseConnection, "gpkg_extensions"), "Extensions table does not exist.");
    }

    
    /**
     * A GeoPackage MAY contain a table or updateable view named 
     * gpkg_extensions. If present this table SHALL be defined per clause 
     * 2.3.2.1.1 Table Definition, GeoPackage Extensions Table or View 
     * Definition (Table or View Name: gpkg_extensions) and gpkg_extensions 
     * Table Definition SQL. An extension SHALL NOT modify the definition or 
     * semantics of existing columns. An extension MAY define additional tables 
     * or columns. An extension MAY allow new values or encodings for existing 
     * columns.
     *
     * @see <a href="http://www.geopackage.org/spec/#_r58" target=
     *      "_blank">2.3.2.1.1. Extensions Table Definition - Requirement 58</a>
     *
     * @throws SQLException
     *             If an SQL query causes an error
     */
    @Test(description = "See OGC 12-128r13: Requirement 58")
    public void extensionsTableDefinition() throws SQLException
    {
		// 1
		final Statement statement = this.databaseConnection.createStatement();

		final ResultSet resultSet = statement.executeQuery("PRAGMA table_info('gpkg_extensions');");

		// 2
		int passFlag = 0;
		final int flagMask = 0b00011111;
		
		while (resultSet.next()) {
			// 3
			final String name = resultSet.getString("name");
			if ("table_name".equals(name)){
				assertTrue("TEXT".equals(resultSet.getString("type")), ErrorMessage.format(ErrorMessageKeys.TABLE_DEFINITION_INVALID, "gpkg_extensions", "table_name type"));
				assertTrue(resultSet.getInt("notnull") == 0, ErrorMessage.format(ErrorMessageKeys.TABLE_DEFINITION_INVALID, "gpkg_extensions", "table_name notnull"));
				passFlag |= 1;
			} else if ("column_name".equals(name)){
				assertTrue("TEXT".equals(resultSet.getString("type")), ErrorMessage.format(ErrorMessageKeys.TABLE_DEFINITION_INVALID, "gpkg_extensions", "column_name type"));
				assertTrue(resultSet.getInt("notnull") == 0, ErrorMessage.format(ErrorMessageKeys.TABLE_DEFINITION_INVALID, "gpkg_extensions", "column_name notnull"));
				passFlag |= (1 << 1);
			} else if ("extension_name".equals(name)){
				assertTrue("TEXT".equals(resultSet.getString("type")), ErrorMessage.format(ErrorMessageKeys.TABLE_DEFINITION_INVALID, "gpkg_extensions", "extension_name type"));
				assertTrue(resultSet.getInt("notnull") == 1, ErrorMessage.format(ErrorMessageKeys.TABLE_DEFINITION_INVALID, "gpkg_extensions", "extension_name notnull"));
				passFlag |= (1 << 2);
			} else if ("definition".equals(name)){
				assertTrue("TEXT".equals(resultSet.getString("type")), "definition type");
				assertTrue(resultSet.getInt("notnull") == 1, ErrorMessage.format(ErrorMessageKeys.TABLE_DEFINITION_INVALID, "gpkg_extensions", "definition notnull"));
				passFlag |= (1 << 3);
			} else if ("scope".equals(name)){
				assertTrue("TEXT".equals(resultSet.getString("type")), ErrorMessage.format(ErrorMessageKeys.TABLE_DEFINITION_INVALID, "gpkg_extensions", "scope type"));
				assertTrue(resultSet.getInt("notnull") == 1, "scope notnull");
				passFlag |= (1 << 4);
			}
		} 
		assertTrue((passFlag & flagMask) == flagMask, ErrorMessage.format(ErrorMessageKeys.TABLE_DEFINITION_INVALID, "gpkg_extensions", "missing column(s)"));
    }
}