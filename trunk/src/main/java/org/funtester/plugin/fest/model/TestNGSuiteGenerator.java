package org.funtester.plugin.fest.model;

import java.util.ArrayList;
import java.util.List;

import org.funtester.common.semantic.SemanticTestCase;
import org.funtester.common.semantic.SemanticTestSuite;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class TestNGSuiteGenerator {
	
	private static final String TEST_NAME = "funtester";
	
	public XmlSuite generate(
			final SemanticTestSuite suite,
			final String packageName
			) {
		XmlSuite xmlSuite = new XmlSuite();		
		xmlSuite.setName( suite.getName() );

		XmlTest xmlTest = new XmlTest( xmlSuite );
		xmlTest.setName( TEST_NAME );
		List< XmlClass > xmlClassList = new ArrayList< XmlClass >(); 
		for ( SemanticTestCase stc : suite.getTestCases() ) {
			String className = makeClassName( packageName, stc.getName() );
			System.out.println( "className is: " + className );
			/*
			try {				
				//URLClassLoader.getSystemClassLoader().loadClass( className );
				JarFileLoader l = new JarFileLoader();
				l.addDir( "file:///C:\\dev\\workspace\\\funtester_fest_plugin\\src" );
				l.addDir( "file:///C:\\dev\\workspace\\\funtester_fest_plugin\\src\\tests\\funtester" );
				l.loadClass( className );
			} catch ( Exception e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			
			
			XmlClass xmlClass = new XmlClass( className );
			xmlClassList.add( xmlClass );
		}
		xmlTest.setXmlClasses( xmlClassList );
		return xmlSuite;
	}
	
	private String makeClassName(final String testPackage, final String className) {
		if ( testPackage.isEmpty() ) {
			return className;
		}
		return testPackage + "." + className;
	}

}
