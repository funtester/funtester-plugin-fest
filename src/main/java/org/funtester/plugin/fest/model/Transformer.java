package org.funtester.plugin.fest.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.funtester.common.semantic.SemanticTestSuite;
import org.funtester.common.util.FileUtil;
import org.funtester.plugin.report.testng.TestNGXmlGenerator;

public class Transformer {
	
	private static final String JAVA_FILE_EXTENSION = ".java";

	public List< String > transform(
			final SemanticTestSuite suite,
			final String baseOutputDirectory,
			final String mainClass,
			final String packageName,
			final int timeoutToBeVisibleInMS
			) throws TransformException {		
		
		//
		// Generates folders according to the package structure
		//
		String sourceCodeDir = baseOutputDirectory;
		if ( generateDirStructure( baseOutputDirectory, packageName ) ) {
			sourceCodeDir = dirFromPackageName( baseOutputDirectory, packageName );
		}
		

		List< String > fileNames = null;
		try {
			fileNames = generateCode( suite, sourceCodeDir, mainClass,
					packageName, timeoutToBeVisibleInMS );

			//
			// Generates a TestNG XML configuration file containing the
			// necessary parameters to run the tests
			//
			generateConfiguration( suite, baseOutputDirectory, packageName );
		} catch ( Exception e ) {
			e.printStackTrace();
			throw new TransformException( e );
		}
		
/*		
		
		//
		// Tries to compile the generated java files
		//
		if ( fileNames != null ) {	
			try {
				Class.forName( "com.sun.tools.javac.api.JavacTool" );
			} catch ( ClassNotFoundException e ) {
				System.out.println( "Not found. Trying to 'load tools.jar'...");
				JarFileLoader l = new JarFileLoader();
				try {
					l.addJarFile( "C:\\dev\\Java\\jdk1.7.0_05\\lib\\tools.jar" );
					Class.forName( "com.sun.tools.javac.api.JavacTool" );
				} catch ( Exception e1 ) {
					e1.printStackTrace();
				}
			}
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			System.out.println( compiler );
			for ( String javaFileName : fileNames ) {
				System.out.println( "Compiling '" + javaFileName + "'...");
				//int result = compiler.run( null, null, null, javaFileName );
				int result = compiler.run( System.in, System.out, System.err, javaFileName );
				System.out.println( "Compiling result is: " + result );
			}
		}
		//
		// Load and instantiate compiled class.
		//
		System.out.println( "Trying to load the class..." );
		URLClassLoader classLoader = null;
		try {
			classLoader = URLClassLoader.newInstance(
					new URL[] {
							new URL( "file:///C:\\dev\\workspace\\funtester_fest_plugin\\src\\" )
					} );
			Class<?> cls = Class.forName("tests.funtester.LoginTest", true, classLoader); 
			//Object instance = cls.newInstance(); 
			//System.out.println(instance);
		} catch ( Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		//
		// Execute the tests using TestNG libraries
		//
		TestNG tng = new TestNG();
		tng.addClassLoader( classLoader );
		
		TestNGSuiteGenerator xmlGen = new TestNGSuiteGenerator();
		XmlSuite xmlSuite = xmlGen.generate( suite, packageName );
		
		List< XmlSuite > xmlSuiteList = new ArrayList< XmlSuite >();
		xmlSuiteList.add( xmlSuite );
		
		
		tng.setXmlSuites( xmlSuiteList );
		tng.setSourcePath( sourceCodeDir );				
		tng.run();
		
*/		
	
		return fileNames;
	}


	private boolean generateDirStructure(
			final String baseOutputDirectory,
			final String packageName
			) {
		File baseDir = new File( baseOutputDirectory );
		if ( ! baseDir.isDirectory() ) {
			throw new RuntimeException( baseOutputDirectory + " should be a directory." );
		}
		if ( ! baseDir.exists() ) {
			throw new RuntimeException( "The baseOutputDirectory should exists: " + baseOutputDirectory );
		}		
		String packagePath = dirFromPackageName( baseOutputDirectory, packageName );
		File packageDir = new File( packagePath );		
		if ( packageDir.exists() ) {
			return true; // Nothing to be done
		}
		return packageDir.mkdirs();	
	}
	
	private String dirFromPackageName(
			final String baseDir,
			final String packageName
			) {
		String packageDirs = packageName.replace( ".", File.separator );
		return FileUtil.directoryWithSeparator( baseDir ) + packageDirs;
	}
	
	private List< String > generateCode(
			final SemanticTestSuite suite,
			final String outputDirectory,
			final String mainClass,
			final String packageName,
			final int timeoutToBeVisibleInMS
			) throws IOException, CodeGenerationException {		
		if ( null == suite ) {
			throw new IllegalArgumentException( "suite can't be null." );
		}
		
		List< String > fileNames = new ArrayList< String >();
		
		// TESTNG + FEST SWING 
		FestSwingCodeGenerator codeGen = new FestSwingCodeGenerator();
		Map< String, StringBuilder > codeContent = codeGen.generate(
				suite, mainClass, packageName, timeoutToBeVisibleInMS );
		for ( Entry< String, StringBuilder > e : codeContent.entrySet() ) {
			String className = e.getKey();
			String fileName = FileUtil.makeFileName( outputDirectory, className + JAVA_FILE_EXTENSION );
			fileNames.add( fileName );
			FileUtil.saveContentToFile( e.getValue().toString(), fileName );
		}		
		return fileNames;
	}	
	
	
	private void generateConfiguration(
			final SemanticTestSuite suite,
			final String outputDirectory,
			final String packageName
			) throws IOException {		
		TestNGXmlGenerator xmlGen = new TestNGXmlGenerator();
		Map< String, StringBuilder > xmlContent = xmlGen.generate( packageName, suite );
		String fileName = FileUtil.makeFileName( outputDirectory,
				TestNGXmlGenerator.FILE_NAME );
		FileUtil.saveContentToFile(
				xmlContent.get( suite.getName() ).toString(), fileName );
	}
}
