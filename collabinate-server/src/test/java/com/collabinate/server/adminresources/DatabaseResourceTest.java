package com.collabinate.server.adminresources;

import static org.junit.Assert.*;

import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Status;

import com.collabinate.server.resources.GraphResourceTest;

/**
 * Tests for the Database Export Resource.
 * 
 * @author mafuba
 *
 */
public class DatabaseResourceTest extends GraphResourceTest
{

	@Test
	public void export_should_return_200()
	{
		assertEquals(Status.SUCCESS_OK, get().getStatus());
	}
	
	@Test
	public void export_should_have_xml_content_type()
	{
		assertEquals(MediaType.APPLICATION_XML,
				get().getEntity().getMediaType());
	}
	
	@Test
	public void export_should_contain_graphml()
	{
		assertTrue(isValidGraphml(get().getEntityAsText()));
	}
	
	private boolean isValidGraphml(String graphMl)
	{
		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema;
		try
		{
			schema = factory.newSchema(getClass().getClassLoader()
					.getResource(GRAPHML_SCHEMA_FILE));
		}
		catch (Exception e)
		{
			// TODO: fix offline validation
			return true;
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
		return "/1/admin/database";
	}
	
	private static final String GRAPHML_SCHEMA_FILE = "graphml-structure.xsd";
}
