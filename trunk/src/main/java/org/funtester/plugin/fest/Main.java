package org.funtester.plugin.fest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.funtester.common.generation.TestGenerationConfiguration;
import org.funtester.common.generation.TestGenerationConfigurationRepository;
import org.funtester.common.report.TestCaseExecution;
import org.funtester.common.report.TestExecutionReport;
import org.funtester.common.report.TestExecutionStatus;
import org.funtester.common.report.TestMethodExecution;
import org.funtester.common.report.TestSuiteExecution;
import org.funtester.common.semantic.SemanticTestCase;
import org.funtester.common.semantic.SemanticTestMethod;
import org.funtester.common.semantic.SemanticTestSuite;
import org.funtester.common.util.ArgUtil;
import org.funtester.common.util.CommandRunner;
import org.funtester.common.util.SourceCodeReader;
import org.funtester.common.util.TimeConverter;
import org.funtester.plugin.fest.json.JsonMapper;
import org.funtester.plugin.fest.model.FestSwingCodeGenerator;
import org.funtester.plugin.fest.model.JsonSemanticTestSuiteRepository;
import org.funtester.plugin.fest.model.JsonTestGenerationConfigurationRepository;
import org.funtester.plugin.fest.model.SemanticTestSuiteRepository;
import org.funtester.plugin.fest.model.TransformException;
import org.funtester.plugin.fest.model.Transformer;
import org.funtester.plugin.fest.xml.XmlReader;
import org.funtester.plugin.report.junit.JUnitXmlReportTestSuite;
import org.funtester.plugin.report.testng.TestNGReportTransformer;
import org.funtester.plugin.report.testng.TestNGXmlReportResult;
import org.testng.log4testng.Logger;

/**
 * Application entry point.
 * 
 * TODO: Separate UI and functionality.
 * 
 * @author Thiago Delgado Pinto
 *
 */
public class Main {
	
	public static final String APP = "funtester-ext-fest";
	public static final String NAME = "FunTester plugin for FEST";
	public static final String VERSION = "1.0";
	
	private static final Logger logger = Logger.getLogger( Main.class ); // TestNG Logger
	
	
	private static final String PARAM_HELP[] = { "h", "help", "?" };
	private static final String PARAM_NOT_GENERATE_TESTS[] = { "ng" };
	private static final String PARAM_NOT_RUN[] = { "nr" };
	
	private static final String DESC_HELP = "for help.";
	private static final String DESC_NOT_GENERATE_TESTS = "for not generating tests.";
	private static final String DESC_NOT_RUN = "for not running tests.";
	
	private static final int LINE_MAX = 79;
	
	
	private static final void terminate() {
		out( "Finished." );
		printFooter();
		System.exit( 0 );
	}
	
	synchronized private static void out(final String text) {
		System.out.println( text );
		try { Thread.sleep( 1 ); } catch(Exception e) {}
	}
	
	synchronized private static void err(final String text) {
		System.err.println( text );
		try { Thread.sleep( 1 ); } catch(Exception e) {}
	}
	
	public static String makeLine(int size) {
		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < size; ++i ) sb.append( "-" );
		return sb.toString();
	}
	
	synchronized public static void printHeader() {
		final String CONTENT = NAME + " - v" + VERSION;		
		final String line = makeLine( LINE_MAX );
		out( line + "\n" + CONTENT + "\n" + line );
	}
	
	synchronized public static void printFooter() {
		out( makeLine( LINE_MAX ) );
	}
	
	synchronized public static void printUsage(final String exeName) {
		out( new StringBuilder()		
			.append( "> Usage:\n  " + exeName + " <configuration-file> <options>\n\n" )
			.append( "> <options> can be:\n" )
			
			.append( "  --" )
			.append( StringUtils.join( PARAM_HELP, " or --" ) )
			.append( ", " ).append( DESC_HELP ).append( "\n" )			
			
			.append( "  --" )
			.append( StringUtils.join( PARAM_NOT_GENERATE_TESTS, " or --" ) )
			.append( ", " ).append( DESC_NOT_GENERATE_TESTS ).append( "\n" )
			
			.append( "  --" )
			.append( StringUtils.join( PARAM_NOT_RUN, " or --" ) )
			.append( ", " ).append( DESC_NOT_RUN ).append( "\n" )
			
			.toString()
			);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean isToGenerateTestSourceCode = true;
		boolean isToRunTests = true;
		
		// Save the start time		
		final long START_TIME = System.currentTimeMillis();
		
		File f = new File( Main.class.getProtectionDomain()
				.getCodeSource().getLocation().getPath() );
		final String EXE_NAME = f.getName().contains( ".jar" ) ? f.getName() : APP;
		

		printHeader();
		
		//
		// Parameters
		//
		
		// "help" parameter
		final int helpIndex = ArgUtil.argumentIndex( args, PARAM_HELP );
		if ( helpIndex >= 0 || args.length < 1 ) {
			printUsage( EXE_NAME );
			terminate();
		}
		
		// "not generate tests" parameter	
		final int notGenerateTestsIndex = ArgUtil.argumentIndex( args, PARAM_NOT_GENERATE_TESTS );
		if ( notGenerateTestsIndex >= 0 ) {
			isToGenerateTestSourceCode = false;
			out( "> Passed option " + DESC_NOT_GENERATE_TESTS );
		}
		
		// "not run" parameter		
		final int notRunIndex = ArgUtil.argumentIndex( args, PARAM_NOT_RUN );
		if ( notRunIndex >= 0 ) {
			isToRunTests = false;
			out( "> Passed option " + DESC_NOT_RUN );
		}
		
		// parameter "configuration file" not supplied
		
		if ( notGenerateTestsIndex == 1 || notRunIndex == 1 ) {
			err( "> The first parameter should be the configuration file." );
			terminate();
		}
		
		
		// Verifies if the file exists
		String fileName = args[ 0 ];
		File file = new File( fileName );
		if ( ! file.exists() ) {
			err( "> A file named '" + fileName + "' not exists. Please inform a existing file." );
			terminate();
		}
		
		//
		// Reading the configuration file
		//
		
		TestGenerationConfigurationRepository configurationRepository =
			createConfigurationRepository( fileName );
		
		TestGenerationConfiguration cfg = null;
		try {
			cfg = configurationRepository.first();
		} catch ( Exception e1 ) {			
			err( "> Error reading the file '" + fileName + "'. Details:\n" );
			e1.printStackTrace();
			terminate();
		}
		
		//
		// Reading the semantic test file
		//
		
		final long SOURCE_GENERATION_START_TIME = System.currentTimeMillis();
		
		SemanticTestSuiteRepository semanticTestSuiteRepository =
			createSemanticTestSuiteRepository( cfg.getSemanticTestFile() );
		
		SemanticTestSuite semanticTestSuite = null;
		try {
			semanticTestSuite = semanticTestSuiteRepository.first();
		} catch ( Exception e1 ) {
			err( "> Error reading the semantic test file '" + cfg.getSemanticTestFile() + "'. Details:\n" );
			e1.printStackTrace();
			terminate();
		}				
		// logger.debug( System.getProperties() );
		// logger.debug( "CLASSPATH is: \"" + System.getProperty( "java.class.path" ) + "\"" );
		
		//
		// Transforming the semantic tests in java test files
		//
		
		List< String > fileNames = new ArrayList< String >();
		if ( isToGenerateTestSourceCode ) {			
			Transformer transformer = new Transformer();
			try {
				//transformer.transform( inputFilePath, outputDir, packageName );
				fileNames.addAll( transformer.transform(
						semanticTestSuite,
						cfg.getOutputDirectory(),
						cfg.getMainClass(),
						cfg.getPackageName(),
						cfg.getTimeoutToBeVisibleInMS()
						) );
			} catch ( TransformException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			transformer = null; // force to destroy
		}
		
		final long SOURCE_GENERATION_TIME = System.currentTimeMillis() - SOURCE_GENERATION_START_TIME;
		
		//
		// Executing the tests (using the commands)
		//
		
		if ( isToRunTests ) {		
			final boolean isToRunCommands = cfg.isToRunCommands();			 
			if ( isToRunCommands ) {		
				if ( cfg.getCommandsToRun().size() < 1 ) {
					out( "There are no commands to run." );
				} else {
					CommandRunner runner = new CommandRunner();
					for ( String cmd : cfg.getCommandsToRun() ) {
						try {
							runner.runAndWait( cmd );
						} catch ( Exception e ) {
							//
							// This is a workaround to not give a error if exit value is
							// equals to 1 (one) because Apache Exec does not detects
							// correctly that a test failed but the test application
							// terminates successfully (a test failure or error is not a
							// application execution error).
							//
							if ( ! e.getLocalizedMessage().toLowerCase().contains( "exit value: 1" ) ) {
								e.printStackTrace();
								return;
							}
						} // catch
					} // for
				} // else
			} // if
			
					
			
			if ( cfg.isToTryToRunTheTestsInternally() ) {
				
				// TODO
				
			} else if ( ! isToRunCommands ) {
				out( "It was configured to not run any command nor the tests. Please adjust the configuration and try again." );
				terminate();
			}
		}
		
		final long ANALYSIS_START_TIME = System.currentTimeMillis();
				
		//
		// Reading the XML report
		//
		
		TestExecutionReport report = null;		
		XmlReader xmlReader = new XmlReader();
		
		if ( cfg.getTestingFramework().equalsIgnoreCase( "testng" ) ) {
			TestNGXmlReportResult content = null;
			try {			
				//logger.debug( "Report is on " + cfg.getReportFile());
				content = xmlReader.readObject2( cfg.getOriginalResultsFile(), TestNGXmlReportResult.class );
			} catch ( Exception e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.debug( "Do something with TESTNG !" );
						
			TestNGReportTransformer reportTransformer = new TestNGReportTransformer( cfg.getOriginalResultsFile() );
			report = reportTransformer.transform( content );
			
		} else if ( cfg.getTestingFramework().equalsIgnoreCase( "junit" ) ) {
			JUnitXmlReportTestSuite content = null;
			try {			
				content = xmlReader.readObject( cfg.getOriginalResultsFile(), JUnitXmlReportTestSuite.class );
			} catch ( Exception e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.debug( "Do something with JUNIT !" );
		} else {
			err( "Unknown reporting tool: " + cfg.getTestingFramework() );
		}
	
				
		//
		// Analyzing the report
		//
		
		final String idCommentToFind =
				FestSwingCodeGenerator.SEMANTIC_STEP_ID_COMMENT_START +
				FestSwingCodeGenerator.SEMANTIC_STEP_ID	+ "=";
		
		logger.info( "\nAnalyzing report..." );
		for ( TestSuiteExecution suite : report.getSuites() ) {
			for ( TestCaseExecution testCase : suite.getTestCases() ) {
				for ( TestMethodExecution method : testCase.getMethods() ) {
					
					
					logger.debug( "TestCase is " +  testCase.getClassName() );
					logger.debug( "testMethod is " +  method.getName() );
					// Ignore successful tests
					if ( null == method.getStackTrace() ) {
						logger.debug( "ignored for not having stack trace!" );	
						continue;
					}
					logger.debug( "Stack trace is not null" );
					logger.debug( "Erroneous testMethod is " +  method.getName() );
					final String trace = method.getStackTrace();
					
					for ( String fName : fileNames ) {
						String name = ( new File( fName ) ).getName();
						logger.debug( "trying file: " + fName + " => (" + name + ")" );
						
						//int beginIndex = trace.indexOf( "(" + name + ":" );
						int beginIndex = trace.lastIndexOf( "(" + name + ":" );
						if ( beginIndex < 0 ) {
							continue; // It is not the file
						}						
						logger.debug( "Found file: " + name );
						
						method.setErroneousFile( fName ); // Set the file
						
						String piece = trace.substring( beginIndex );
						int colonIndex = piece.indexOf( ":" );
						int endIndex = piece.indexOf( ")" );
						if ( colonIndex >= endIndex ) {
							continue;
						}
						
						String lineNumber = piece.substring( colonIndex + 1, endIndex );
						logger.debug( fName + ":" + lineNumber );
						
						int line = Integer.parseInt( lineNumber );
						
						method.setErroreousFileLineNumber( line ); // Set the line number
						
						SourceCodeReader scr = new SourceCodeReader();
						try {
							String command = scr.readCommandStartingAtLine(
									new FileInputStream( fName ), line );
							logger.debug( "Command is " + command );
							
							method.setErroreousLineOfCode( command ); // Set the line of code
							
							int idCommentPos = command.indexOf( idCommentToFind );
							if ( idCommentPos >= 0 ) {
								
								final String commandPiece = command.substring( idCommentPos );
								
								final int assignmentIndex = commandPiece.indexOf(
										FestSwingCodeGenerator.SEMANTIC_STEP_ID_ASSIGNMENT );
								
								final int separatorIndex = commandPiece.indexOf(
										FestSwingCodeGenerator.SEMANTIC_STEP_ID_SEPARATOR );
								
								
								final String ssid = commandPiece.substring( assignmentIndex + 1, separatorIndex );
								final String stepId = commandPiece.substring( separatorIndex + 1 );
								
								logger.debug( "ssid is " + ssid + " stepId is " + stepId );
								
								long erroneousStepId;
								try {
									erroneousStepId = Long.parseLong( ssid.trim() );
								} catch (Exception e) {
									erroneousStepId = 0;
								}
								
								method.setErroneousSemanticStepId( erroneousStepId ); // Set the semantic step id
							} else {
								logger.debug( FestSwingCodeGenerator.SEMANTIC_STEP_ID + " not found.");
							}
						} catch ( FileNotFoundException e ) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch ( IOException e ) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
					} // for fName		
								
					// ------- TEST RESULT CORRECTION -------
					
					logger.debug( ">>>>>>>>>>>>>>>>>>>>>>>>> Exception class is: " + method.getExceptionClass() );
					
					if ( method.getExceptionClass().trim().contains(
							"LocationUnavailableException" ) ) {
						
						logger.debug( ">>>>>>>>>>>>>>>>>>>>>>>>> Exception class compatible!" );
					
						final String testCaseName = extractClassNameWithoutPackage( testCase.getClassName() );					
						SemanticTestCase semanticTestCase =
							semanticTestSuite.testCaseWithName( testCaseName );
						if ( semanticTestCase != null ) {
							SemanticTestMethod semanticTestMethod =
								semanticTestCase.testMethodWithName( method.getName() );
							if ( semanticTestMethod != null ) {
								// Expected FAIL and really FAIL -> PASS !
								if ( ! semanticTestMethod.isExpectedSuccess() ) {
									
									method.setStatus( TestExecutionStatus.PASS );								
									// Important: adjust the totals in the
									// suite AND the report
									
									suite.decreaseTotalFailures();
									suite.increaseTotalPassed();

									report.decreaseTotalFailures();
									report.increaseTotalPassed();
								}
							} else {
								logger.debug( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> SemanticTestMethod not found witn name: " + method.getName() );
							}
						} else {
							logger.debug( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> SemanticTestCase not found with name: " + testCase.getClassName() );
						}
					}
					// --------------------------------------	
					
				} // for method
			} // for testCase 
		} // for suite
		
		logger.info( "Converting results to JSON..." );
				
		//
		// Converting the report to a JSON file
		//
		try {
			JsonMapper.writeObject( cfg.getConvertedResultsFile(), report );
		} catch ( Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
		
		final long ANALYSIS_EXECUTION_TIME = System.currentTimeMillis() - ANALYSIS_START_TIME;
		final long EXECUTION_TIME = System.currentTimeMillis() - START_TIME;
		
		if ( isToGenerateTestSourceCode ) {
			out( "Source code generation time\t\t: " + TimeConverter.toHMS( SOURCE_GENERATION_TIME )
				+ " (" + SOURCE_GENERATION_TIME + " ms)" );
		}
		
		out( "Analysis and convertion execution time\t: " + TimeConverter.toHMS( ANALYSIS_EXECUTION_TIME )
				+ " (" + ANALYSIS_EXECUTION_TIME + " ms)" );
		
		out( "Total execution time\t\t\t: " + TimeConverter.toHMS( EXECUTION_TIME )
				+ " (" + EXECUTION_TIME + " ms)" );
		
		terminate();
	}

	private static String extractClassNameWithoutPackage(final String className) {
		int index = className.lastIndexOf( "." );
		if ( index < 0 ) {
			return className;
		}
		return className.substring( index + 1 );
	}

	private static TestGenerationConfigurationRepository createConfigurationRepository(
			final String fileName) {
		return new JsonTestGenerationConfigurationRepository( fileName );
	}

	private static SemanticTestSuiteRepository createSemanticTestSuiteRepository(
			final String fileName) {
		return new JsonSemanticTestSuiteRepository( fileName );
	}

}