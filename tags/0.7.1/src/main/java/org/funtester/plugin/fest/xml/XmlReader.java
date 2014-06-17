package org.funtester.plugin.fest.xml;

import java.io.File;
import java.io.FileReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class XmlReader {

	public <T> T readObject(
			final String fileName,
			final Class< T > clazz
			) throws Exception {	
		
		JacksonXmlModule module = new JacksonXmlModule();
		module.setDefaultUseWrapper( false );
		XmlMapper xmlMapper = new XmlMapper( module );
		xmlMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
		xmlMapper.registerModule( new JodaModule() );
		
		/* Commented after update funtester/pom.xml's jackson-core from
		 * version 2.1.3 to version 2.3.1.
		 *  
		// ANNOTATIONS SUPPORT -------------------------------------------------
		AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
		// if ONLY using JAXB annotations:
		xmlMapper.setAnnotationIntrospector(introspector);
		// if using BOTH JAXB annotations AND Jackson annotations:
		AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();			
		xmlMapper.setAnnotationIntrospector(new AnnotationIntrospector.Pair(introspector, secondary) );
		// ---------------------------------------------------------------------
		*/
		
		return xmlMapper.readValue( new File( fileName ), clazz );
	}
	
	public < T > T readObject2(
			final String fileName,
			final Class< T > clazz
			) throws Exception {
		JAXBContext jc = JAXBContext.newInstance( clazz );
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		FileReader fileReader = new FileReader( fileName );
		return (T) unmarshaller.unmarshal( fileReader );
	}
}
