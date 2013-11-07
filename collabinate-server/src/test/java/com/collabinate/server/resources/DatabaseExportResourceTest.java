package com.collabinate.server.resources;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Test;
import org.restlet.data.Status;

/**
 * Tests for the Database Export Resource.
 * 
 * @author mafuba
 *
 */
public class DatabaseExportResourceTest extends GraphResourceTest
{

	@Test
	public void export_should_return_200()
	{
		assertEquals(Status.SUCCESS_OK, get().getStatus());
	}
	
	@Test
	public void export_should_contain_graphml()
	{
		assertTrue(isValidGraphml(get().getEntityAsText()));
	}
	
	//TODO: put the graphml schema in a local file to speed the test up
	private boolean isValidGraphml(String graphMl)
	{
		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema;
		try
		{
			schema = factory.newSchema(new URL(GRAPHML_SCHEMA_URL));
		}
		catch (Exception e)
		{
			return false;
		}
		
		Validator validator = schema.newValidator();

		// create a source from a string
		Source source = new StreamSource(new StringReader(graphMl));

		// check input
		boolean isValid = true;
		try
		{
			validator.validate(source);
		}
		catch (Exception e)
		{
			isValid = false;
		}

		return isValid;
	}
	
	@Override
	protected String getResourcePath()
	{
		return "/1/admin/database/export";
	}
	
	private static final String GRAPHML_SCHEMA_URL = 
			"http://graphml.graphdrawing.org/xmlns/1.0/graphml-structure.xsd";
}
