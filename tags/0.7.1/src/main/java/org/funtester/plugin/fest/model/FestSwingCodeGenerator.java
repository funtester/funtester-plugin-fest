package org.funtester.plugin.fest.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.funtester.common.at.AbstractTestActionStep;
import org.funtester.common.at.AbstractTestCase;
import org.funtester.common.at.AbstractTestDatabaseConnection;
import org.funtester.common.at.AbstractTestDatabaseScript;
import org.funtester.common.at.AbstractTestElement;
import org.funtester.common.at.AbstractTestMethod;
import org.funtester.common.at.AbstractTestOracleStep;
import org.funtester.common.at.AbstractTestStep;
import org.funtester.common.at.AbstractTestSuite;
import org.funtester.common.util.StringUtil;
import org.funtester.plugin.code.java.JavaSyntax;
import org.funtester.plugin.fest.model.fest.FESTAction;
import org.funtester.plugin.fest.model.fest.FESTActionQuery;
import org.funtester.plugin.fest.model.fest.FESTVocabulary;
import org.funtester.plugin.fest.model.fest.FESTWidget;
import org.funtester.plugin.fest.model.fest.FESTWidgetQuery;
import org.joda.time.DateTime;
import org.testng.log4testng.Logger;

public class FestSwingCodeGenerator {
	
	private static final Logger logger = Logger.getLogger( FestSwingCodeGenerator.class );
	
	public static final String FUNCTIONAL_TEST_GROUP_NAME = "functional";
	
	/** This is not the *step* id but the *semantic step* id */
	public static final String SEMANTIC_STEP_ID ="id";
	public static final String SEMANTIC_STEP_ID_ASSIGNMENT = "=";
	public static final String SEMANTIC_STEP_ID_SEPARATOR ="|";
	public static final String SEMANTIC_STEP_ID_FORMAT = "%s%s%d%s%d"; // Example: id=234|987
	public static final String SEMANTIC_STEP_ID_COMMENT_START = " // ";
	
	public static final String FIND_METHOD_PREFIX = "find";
		
	private JavaSyntax stx = new JavaSyntax();
	
	
	private class FixtureInfo {
		public String internalName;
		public String fixtureVar;
		public String fixtureClass;
		public boolean wasDeclared = false;
		
		public FixtureInfo(
				final String internalName,
				final String fixtureName,
				final String fixtureClass,
				final boolean wasDeclared
				) {			
			this.internalName = internalName;
			this.fixtureVar = fixtureName;
			this.fixtureClass = fixtureClass;
			this.wasDeclared = wasDeclared;
		}
	}
	
	
	
	/**
	 * Generates the test files, returning a map containing the file name and
	 * its content.
	 * 
	 * Each use case id control the screen variable. Each variable should be
	 * mapped to a use case id.
	 * 
	 * @param suite						the semantic test suite.
	 * @param mainClass					the main class name, used to trigger
	 * 									the SUT.
	 * @param sourceCodePackage			the package of the test files.
	 * @param timeoutToBeVisibleInMS	the number of milliseconds to wait for
	 * 									a component to be visible.
	 * @return							a map containing the file name and
	 * 									its content.
	 * @throws CodeGenerationException 
	 */
	public Map< String, StringBuilder > generate(
			final AbstractTestSuite suite,
			final String mainClass,
			final String sourceCodePackage,
			final int timeoutToBeVisibleInMS
			) throws CodeGenerationException {
		// Map a file name to its content
		Map< String, StringBuilder > map = new TreeMap< String, StringBuilder >();		
						
		logger.debug( suite.getName() );
		logger.debug( suite.getSoftwareName() );
		logger.debug( suite.getTestCases().size() );
		//logger.debug( suite.getTestCases().get( 0 ).getTestMethods().size() );
		//logger.debug( suite.getTestCases().get( 1 ).getTestMethods().size() );
		
		final List< AbstractTestCase > testCases = suite.getTestCases();
		for ( AbstractTestCase testCase : testCases ) {			
			// IMPORTANT: The className is the file name too!		
			String className = testCase.getName();
			
			boolean initialPartGenerated = false;
			StringBuilder sb = new StringBuilder();						
								
			final List< AbstractTestMethod > testMethods = testCase.getTestMethods();
			logger.debug( "methods size is " + testMethods.size() );
			int i = 0;				
			
			for ( AbstractTestMethod testMethod : testMethods ) {			
		
				// Map a use case id to its screen variable (used by its steps)
				Map< Long, FixtureInfo > screenMap = new TreeMap< Long, FixtureInfo >();
				makeFixtureInfoList( testMethod, screenMap );
								
				logger.debug( "method " + i++ + " is " + testMethod.toString() );
				boolean methodGenerated = false;
				
				final List< AbstractTestStep > steps = testMethod.getSteps();
				logger.debug( "steps size is " + steps.size() );
				int stepCount = 0;
				
				//int fixtureVarIndex = -1;
				
				long lastUseCaseId = -1;
				for ( AbstractTestStep step : steps ) {
					
					final long CURRENT_USE_CASE_ID = ( step.getUseCaseId() >= 0 )
						? step.getUseCaseId()
						: ( lastUseCaseId >= 0 ) ? lastUseCaseId : screenMap.entrySet().iterator().next().getKey();		
					logger.debug( "CURRENT_USE_CASE_ID: " + CURRENT_USE_CASE_ID );
											
					final long ssid = step.getId(); // Semantic step id
					final long stepId = step.getStepId(); // Step id
					
					final String ACTION_NAME = step.getActionName();

					logger.debug( "step " + stepCount++ + " is " + step.toString() );
					
					final boolean isShow = FESTActionQuery.isShowAction( ACTION_NAME );
					
					/*
					if ( isShow ) {														
						fixtureVarIndex++;													
					} else if ( FESTActionQuery.isCloseAction( ACTION_NAME ) ) {
						fixtureVarIndex--;											
					}
					
					logger.debug( "fixtureVarIndex is " + fixtureVarIndex );
					logger.debug( "fixtureInfoList.size() is " + fixtureInfoList.size() );
					
					if ( fixtureVarIndex < 0 ) {
						continue; // Could not use a negative index 
					}
					
					FixtureInfo fixtureInfo = fixtureInfoList.get( fixtureVarIndex );
					*/
					FixtureInfo fixtureInfo = screenMap.get( CURRENT_USE_CASE_ID );
					if ( null == fixtureInfo ) {
						String msg = String.format( "None screen was found for the use case with id %s. It is possible that the screen was not informed in the use case specification.",
								CURRENT_USE_CASE_ID );
						throw new CodeGenerationException( msg );
					}
																														
					
					// Oracle Step
					if ( step instanceof AbstractTestOracleStep ) {	
						
						AbstractTestOracleStep oracleStep = (AbstractTestOracleStep) step;
						
						String [] messages = oracleStep.getMessages().toArray( new String[0] );
						String regEx = "";
						if ( StringUtil.sumLength( messages ) > 0 ) {							
							final int MAX = messages.length;
							for ( int m = 0; ( m < MAX ); ++m ) {						
								messages[ m ] = StringUtil.convertFormattingToRegEx( messages[ m ] );
							}						
							regEx = StringUtil.applyOrOperationWithRegEx( messages );
						}
						/*						
						final String regEx = StringUtil.normalizeBackslashes(
								"(\\w|\\W)*" ); // << Any message (word or non word, zero or more times)
						*/
						final String call = stx.genCallWithArgs(
								fixtureInfo.fixtureVar, FESTAction.oracle( regEx ) );
						sb.append( call )
							.append( stx.SC )
							.append( genInstrumentationComment( ssid, stepId ) )
							.append( stx.ENDL );												
					}
					// ActionStep
					else {
						AbstractTestActionStep actionStep = (AbstractTestActionStep) step;								
						
						int numberOfElements = actionStep.numberOfElements();					
						if ( numberOfElements > 0  && ! initialPartGenerated ) {
							initialPartGenerated = true;																				
														
							String ini = buildInitialPart(
									testCase.getIncludeFiles(),
									mainClass,
									sourceCodePackage,
									testCase,
									className,
									screenMap,
									ssid,
									timeoutToBeVisibleInMS
									);
							sb.insert( 0, ini );
						}
						
						if ( ! methodGenerated ) {
							methodGenerated = true;
							
							String testAnnotation = makeTestWithGroupsAnnotation(
									FUNCTIONAL_TEST_GROUP_NAME,
									makeImportaceGroupName( testMethod.getImportance().toString() )
									);
							
							sb.append( stx.genTestMethodStart(
									testAnnotation,
									testMethod.getName()
									) );
						}
						
						if ( isShow ) {
							/*
							// -----------------------------------------------------
							//  Finish (close and cleanUp) the fixture BEFORE showing the other
							if ( fixtureVarIndex > 0 ) { 
								FixtureInfo fiBefore = fixtureInfoList.get( fixtureVarIndex - 1 );
								//sb.append( genCallWithArgs( fiBefore.fixtureVar, FESTVocabulary.close() ) ).append( SCE );								
								sb.append( genCallWithArgs( fiBefore.fixtureVar, FESTVocabulary.cleanUp() ) ).append( SCE );
								sb.append( ENDL );
							}
							// -----------------------------------------------------									
							*/
							
							// -----------------------------------------------------
							// Creates the screen var
							
							/*
							FixtureInfo fiAfter = fixtureInfoList.get( fixtureVarIndex );
							final String attr = stx.genAttr(
									fiAfter.fixtureClass + " " + fiAfter.fixtureVar,
									FIND_METHOD_PREFIX + fiAfter.internalName + stx.PAR ); 
							sb.append( attr ).append( stx.SCE );
							*/
							
							FixtureInfo fiAfter = screenMap.get( step.getUseCaseId() );							
							if ( ! fiAfter.wasDeclared ) {
								fiAfter.wasDeclared = true;
							
								final String attr = stx.genAttr(
										fiAfter.fixtureClass + " " + fiAfter.fixtureVar,
										FIND_METHOD_PREFIX + fiAfter.internalName + stx.PAR );
								
								sb.append( attr ).append( stx.SCE );
							}
							
							// -----------------------------------------------------							
						}
						
						
						//
						// If the action REALLY accepts more than one element,
						// (ie: menu click, drag-and-drop, etc.) then use the
						// current elements as parameters.
						//			
						
						if ( numberOfElements > 1 ) {
							if ( FESTActionQuery.actionAcceptsMoreThanOneElement(
									step.getActionName() ) ) {
								
								sb.append( genCallFromElementList(
										fixtureInfo.fixtureVar,
										ACTION_NAME,
										actionStep.getElements(),
										ssid,
										stepId
										) );	
							} else {
								for ( AbstractTestElement se : actionStep.getElements() ) {
									sb.append( genCallFromElement( fixtureInfo.fixtureVar, ACTION_NAME, se, ssid, stepId ) );		
								}
							}
						} else if ( 1 == numberOfElements ) { // (can be zero)
							
							logger.debug( "sb" + ( sb != null ? " not null" : "null" ) );
							logger.debug( "ACTION_NAME" + ( ACTION_NAME != null ? " not null" : "null" ) );							
							logger.debug( "fixtureInfo" + ( fixtureInfo != null ? " not null" : "null" ) );
							logger.debug( "fixtureInfo.fixtureVar" + ( fixtureInfo != null && fixtureInfo.fixtureVar != null ? " not null" : "null" ) );
							logger.debug( "actionStep" + ( actionStep != null ? " not null" : "null" ) );
							logger.debug( "actionStep.getElements()" + ( actionStep != null && actionStep.getElements() != null ? " not null" : "null" ) );
							
							AbstractTestElement firstSE = actionStep.getElements().get( 0 );
							logger.debug( "firstSE" + ( firstSE != null ? " not null" : "null" ) );
							
							String call = genCallFromElement( fixtureInfo.fixtureVar, ACTION_NAME, firstSE, ssid, stepId );
							logger.debug( "call" + ( call != null ? " not null" : "null" ) );
							sb.append( call );								
						} // else if
						
					} // else if (Action Step)
					
					// Saves the last use case id to use in case of activation
					// steps that we don't known where is started
					// Only saves if >= 0
					if ( step.getUseCaseId() >= 0 ) {
						lastUseCaseId = step.getUseCaseId();
					}					
				} // for step
				
				/*
				// Closes and clean up
				if ( fixtureVarIndex >= 0 ) {
					FixtureInfo fi = fixtureInfoList.get( fixtureVarIndex );
					//sb.append( genCallWithArgs( fi.fixtureVar, FESTVocabulary.close() ) ).append( SCE );
					sb.append( genCallWithArgs( fi.fixtureVar, FESTVocabulary.cleanUp() ) ).append( SCE );	
					fixtureVarIndex--;
				}
				*/
				
				screenMap = null; // Destroy the map
				
				sb.append( stx.genMethodEnd() ).append( stx.ENDL );
			} // for testMethod
			
			// Class End
			sb.append( stx.genClassEnd() );
			
			// Add the content
			map.put( className, sb );
		}				
		return map;
	}


	private void makeFixtureInfoList(
			final AbstractTestMethod testMethod,
			final Map< Long, FixtureInfo > screenMap
			) {
//		List< FixtureInfo > list = new ArrayList< FixtureInfo >();
		final List< AbstractTestStep > allSteps = testMethod.getSteps();
		
		Map< String, Integer > counterMap = new HashMap< String, Integer >();
		
		for ( final AbstractTestStep step : allSteps ) {
			// Only the SHOW action
			if ( FESTActionQuery.isShowAction( step.getActionName() ) ) {
				final AbstractTestActionStep actionStep = (AbstractTestActionStep) step;
				final AbstractTestElement firstElement = actionStep.getElements().get( 0 );
				
				final String INTERNAL_NAME = firstElement.getInternalName();

				// Helps to create the right variable name using a counter (i.e.: var1, var2, etc.)
				if ( counterMap.containsKey( INTERNAL_NAME ) ) {					
					Integer count = counterMap.get( INTERNAL_NAME );
					logger.debug( ">>>>>>> BEFORE - " + INTERNAL_NAME +  ": " + count );
					counterMap.put( INTERNAL_NAME, ++count );
					logger.debug( ">>>>>>> AFTER - " + INTERNAL_NAME +  ": " + counterMap.get( INTERNAL_NAME ) );
				} else {
					counterMap.put( INTERNAL_NAME, 1 );
					logger.debug( ">>>>>>> CREATED: "+ INTERNAL_NAME );
				}
				
				// i.e.: loginDialog1
				final String FIXTURE_NAME =
					StringUtil.lowerCaseFirstCharacter( INTERNAL_NAME )
					+ counterMap.get( INTERNAL_NAME );
				
				FixtureInfo fi = new FixtureInfo(
						INTERNAL_NAME,
						FIXTURE_NAME,
						screenClassForElementType( firstElement.getType() ),
						false
						);
				
				screenMap.put( step.getUseCaseId(), fi );
				
				//list.add( fi );
			}
		}
		//return list;
	}


	private boolean contains(final String where, final String what) {
		return where.toLowerCase().contains( what.toLowerCase() );
	}


	private String buildInitialPart(
			final Collection< String > includeFiles,
			final String mainClass,
			final String sourceCodePackage,
			final AbstractTestCase testCase,
			final String className,
			final Map< Long, FixtureInfo > screenMap,
			final long ssid,
			final int timeoutToBeVisibleInMS
			) {		
		/*
		Map< String, SemanticDatabaseConnection > connections =
			new HashMap< String, SemanticDatabaseConnection >();
		
		final boolean HAS_DATABASE_SCRIPTS = ! testCase.getScripts().isEmpty();
		if ( HAS_DATABASE_SCRIPTS ) {
			connections.putAll( extractConnections(	testCase.getScripts() ) );
		}
		*/
		
		StringBuilder sb = new StringBuilder();
		
		sb.append( stx.genComment( "" ) );
		sb.append( stx.genComment( "This file was automatically generated by FunTester (http://www.funtester.org)" ) );
		sb.append( stx.genComment( "at " + DateTime.now().toString() ) );
		sb.append( stx.genComment( "" ) );
		sb.append( stx.genComment( "Please do not remove any comments after command lines." ) );
		sb.append( stx.genComment( "" ) );
		
		// Package -------------------------------------------------------------
		
		sb.append( stx.genPackage( sourceCodePackage ) );
		sb.append( stx.ENDL );
		
		// Imports -------------------------------------------------------------
		
		/*
		if ( HAS_DATABASE_SCRIPTS ) { // Just for database script execution
			sb.append( genImport( "java.util.ArrayList" ) );
			sb.append( genImport( "java.util.List" ) ); 
		}
		*/
		
		sb.append( stx.genImportStatic( "java.awt.event.KeyEvent.*" ) ); // Press action		
		
		sb.append( stx.genImport( "java.util.regex.Pattern" ) ); // Oracle
		
		sb.append( stx.genImport( "org.fest.swing.core.BasicRobot" ) );
		sb.append( stx.genImportStatic( "org.fest.swing.data.TableCell.row" ) ); // Table
		
		sb.append( stx.genImport( "org.fest.swing.edt.FailOnThreadViolationRepaintManager" ) );
		sb.append( stx.genImport( "org.fest.swing.finder.WindowFinder" ) );
		sb.append( stx.genImport( "org.fest.swing.fixture.DialogFixture" ) );
		sb.append( stx.genImport( "org.fest.swing.fixture.FrameFixture" ) );
		sb.append( stx.genImport( "org.fest.swing.launcher.ApplicationLauncher" ) );
		sb.append( stx.genImportStatic( "org.fest.swing.util.Platform.controlOrCommandKey" ) ); // Press action
		
		sb.append( stx.genImport( "org.testng.annotations.AfterMethod" ) );
		sb.append( stx.genImport( "org.testng.annotations.BeforeClass" ) );
		sb.append( stx.genImport( "org.testng.annotations.BeforeMethod" ) );
		sb.append( stx.genImport( "org.testng.annotations.Test" ) );				
		sb.append( stx.ENDL );
		
		// User defined imports ------------------------------------------------
		
		for ( String fileName : includeFiles ) {
			sb.append( stx.genImport( fileName ) );			
		}
		sb.append( stx.ENDL );
		
		// Class ---------------------------------------------------------------
		
		sb.append( stx.genBlockCommentStart() );
		sb.append( stx.genBlockComment( "Test the use case \"" + testCase.getUseCaseName() + "\"" ) );
		sb.append( stx.genBlockComment( "in the following scenarios: " + testCase.getScenarioName() ) );
		sb.append( stx.genBlockComment( "" ) );
		sb.append( stx.genBlockComment( "@author	FunTester" ) );
		sb.append( stx.genBlockComment( "@see		http://www.funtester.org" ) );
		sb.append( stx.genBlockCommentEnd() );
		sb.append( stx.genClassStart( className ) );
		sb.append( stx.ENDL );
		
		// Class attributes ----------------------------------------------------

		sb.append( stx.genField( "BasicRobot", "robot" ) ); // FEST Swing
			
		/*
		final Set< Entry< String, SemanticDatabaseConnection > > connectionEntries = connections.entrySet();
		for ( Entry< String, SemanticDatabaseConnection > e : connectionEntries ) {
			sb.append( genField( "DBScriptRunner", ) )
			e.getKey()
		}
		*/
		
		sb.append( stx.ENDL );
		
		// Creator Factory Helper Methods --------------------------------------
				
		Set< String > fixtureClassTypes = new LinkedHashSet< String >();
		for ( FixtureInfo fi : screenMap.values() ) {
			// Ignore repeated classes		
			if ( fixtureClassTypes.contains( fi.internalName )) {
				continue;
			}
			fixtureClassTypes.add( fi.internalName );
		
			
			sb.append( stx.genComment( "Helper method" ) );
			sb.append( stx.genMethodStart( fi.fixtureClass, FIND_METHOD_PREFIX + fi.internalName ) );			
			final String FIND = "WindowFinder.%s( %s.class ).using( robot )";
			
			if ( contains( fi.fixtureClass, "frame" ) ) {
				sb.append( stx.genReturn( String.format( FIND, "findFrame", fi.internalName ) ) ).append( stx.SCE );	
			} else {
				sb.append( stx.genReturn( String.format( FIND, "findDialog", fi.internalName ) ) ).append( stx.SCE );
			}						
			sb.append( stx.genMethodEnd() ).append( stx.ENDL );
		}
		
		// Before Class Method (one_time_setup) --------------------------------		

		sb.append( stx.genTestConfigurationMethodStart(
				FESTVocabulary.ANNOTATION__BEFORE_CLASS, "one_time_setup" ) );
		
		sb.append( stx.gen( "FailOnThreadViolationRepaintManager.install();" ) ).append( stx.ENDL );
		
		sb.append( stx.genMethodEnd() ).append( stx.ENDL );		
		
		// Before Method (setup) -----------------------------------------------
		
		sb.append( stx.genTestConfigurationMethodStart(
				FESTVocabulary.ANNOTATION__BEFORE_METHOD, "setup" ) );
		
		final int MIN_TIMEOUT_MS = 0;
		final int MAX_TIMEOUT_MS = 60000;
		final int DEF_TIMEOUT_MS = 30000;
		final int timeoutMS = ( timeoutToBeVisibleInMS < MIN_TIMEOUT_MS
				|| timeoutToBeVisibleInMS > MAX_TIMEOUT_MS )
				? DEF_TIMEOUT_MS : timeoutToBeVisibleInMS;
		
		final String APP_START = "ApplicationLauncher.application( \"%s\" ).start(); // start application via main()";
		sb.append( stx.gen( "robot = (BasicRobot) BasicRobot.robotWithNewAwtHierarchy();" ) ).append( stx.ENDL );
		sb.append( stx.gen( "robot.settings().timeoutToBeVisible( " + timeoutMS + " );" ) ).append( stx.ENDL );
		sb.append( stx.gen( String.format( APP_START, mainClass ) ) ).append( stx.ENDL );
		sb.append( stx.genMethodEnd() ).append( stx.ENDL );
						
		// After Method (tear_down) --------------------------------------------
		
		sb.append( stx.genTestConfigurationMethodStart(
				FESTVocabulary.ANNOTATION__AFTER_METHOD, "tear_down" ) );
		
		sb.append( stx.genCallWithoutArgs( "robot", "cleanUp" ) ).append( stx.SCE );
		
		sb.append( stx.genMethodEnd() ).append( stx.ENDL );
		
		// ---------------------------------------------------------------------
		
		return sb.toString();
	}

	
	private Map< String, AbstractTestDatabaseConnection > extractConnections(
			final List< AbstractTestDatabaseScript > scripts
			) {
		Map< String, AbstractTestDatabaseConnection > map =
			new HashMap< String, AbstractTestDatabaseConnection >();
		for ( AbstractTestDatabaseScript script : scripts ) {
			final String connectionName = script.getConnection().getName();
			if ( ! map.containsKey( connectionName ) ) {
				map.put( connectionName, script.getConnection() );
			}
		}
		return map;
	}


	private String screenClassForElementType(final String elementType) {
		if ( FESTWidgetQuery.isFrame( elementType ) ) {
			return FESTVocabulary.CLASS_FRAME_FIXTURE;
		}
		return FESTVocabulary.CLASS_DIALOG_FIXTURE;	
	}


	private String genCallFromElementList(
			final String variable,
			final String actionName,
			final List< AbstractTestElement > elements,
			final long ssid,
			final long stepId
			) {
		StringBuilder sb = new StringBuilder();
		boolean found = false;
		
		// Oracle Action
		
		// Type Action
		if ( FESTActionQuery.isTypeAction( actionName ) ) {
			
			// Table Widget
			if ( elements.size() == FESTVocabulary.EXPECTED_TABLE_PARAMETERS
					&& FESTWidgetQuery.isTable( elements.get( 0 ).getType() )
					) {										
				found = true;
				
				final String row = elements.get( 1 ).getInternalName();
				final String col = elements.get( 2 ).getInternalName();
				final String value = null == elements.get( 0 ).getValue()
					? "" : elements.get( 0 ).getValue().toString();
									
				sb.append( variable )
					.append( stx.DOT )
					.append( FESTWidget.table( elements.get( 0 ).getInternalName() ) )
					.append( FESTAction.enterTableValue( row, col, value ) );
					;			
			}
		}
		// Press Action
		else if ( FESTActionQuery.isPressAction( actionName ) ) {
			found = true;
			List< String > list = new ArrayList< String >();
			for ( AbstractTestElement element : elements ) {
				list.add( element.getInternalName() );
			}			
			sb.append( variable )
				.append( stx.DOT )			
				.append( FESTAction.pressAndReleaseKeys( list.toArray( new String[ 0 ] ) ) );
		}
		
		if ( found ) {
			sb.append( stx.SC ).append( genInstrumentationComment( ssid, stepId ) ).append( stx.ENDL );
			return stx.levelStr() + sb.toString();
		}
						
		
		for ( AbstractTestElement element : elements ) {	
			sb.append( genCallFromElement( variable, actionName, element, ssid, stepId ) );
		}
		return sb.toString();
	}


	private String genCallFromElement(
			final String variable,
			final String actionName,
			final AbstractTestElement element,
			final long ssid,
			final long stepId
			) {
		final String ELEMENT_TYPE = element.getType();
		final String WIDGET_NAME = element.getInternalName();
		final String USER_READABLE_NAME = element.getName();
		final String ELEMENT_VALUE = element.getValue() != null ? element.getValue().toString() : "";
		
		StringBuilder b = new StringBuilder();
		b.append( variable ).append( stx.DOT );
				
		
		boolean found = false; // Controls if found a action or element
		
		// 
		// Comparisons by Action
		//
		
		// Type Action
		if ( FESTActionQuery.isTypeAction( actionName ) ) {
			// TextBox Widget
			if ( FESTWidgetQuery.isTextBox( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTWidget.textBox( WIDGET_NAME ) )
					.append( stx.DOT )
					.append( FESTAction.setText( ELEMENT_VALUE.toString() ) );
			}
		}
		// Select Action
		else if ( FESTActionQuery.isSelectAction( actionName ) ) {
			// ComboBox Widget
			if ( FESTWidgetQuery.isComboBox( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTWidget.comboBox( WIDGET_NAME ) )
					.append( stx.DOT )
					.append( FESTAction.selectItem( ELEMENT_VALUE.toString() ) );
			}
			// List Widget
			if ( FESTWidgetQuery.isList( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTWidget.list( WIDGET_NAME ) )
					.append( stx.DOT )
					.append( FESTAction.selectItem( ELEMENT_VALUE.toString() ) );
			}			
			// CheckBox Widget
			else if ( FESTWidgetQuery.isCheckBox( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTWidget.checkBox( WIDGET_NAME ) )
					.append( stx.DOT )
					.append( FESTAction.check() );
			}			
			// RadioButon Widget
			else if ( FESTWidgetQuery.isRadioButton( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTWidget.radioButton( WIDGET_NAME ) )
					.append( stx.DOT )
					.append( FESTAction.check() );
			}
		}
		// SelectFirst Action
		else if ( FESTActionQuery.isSelectFirstAction( actionName ) ) {
			// ComboBox Widget
			if ( FESTWidgetQuery.isComboBox( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTWidget.comboBox( WIDGET_NAME ) )
					.append( stx.DOT )
					.append( FESTAction.selectFirstItem() );
			}
			// List Widget
			if ( FESTWidgetQuery.isList( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTWidget.list( WIDGET_NAME ) )
					.append( stx.DOT )
					.append( FESTAction.selectFirstItem() );
			}			
		}		
		// Deselect Action
		else if ( FESTActionQuery.isSelectAction( actionName ) ) {
			// ComboBox Widget
			if ( FESTWidgetQuery.isComboBox( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTWidget.comboBox( WIDGET_NAME ) )
					.append( stx.DOT )
					.append( FESTAction.clearSelecion() );
			}
			// List Widget
			if ( FESTWidgetQuery.isList( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTWidget.list( WIDGET_NAME ) )
					.append( stx.DOT )
					.append( FESTAction.clearSelecion() );
			}			
			// CheckBox Widget
			else if ( FESTWidgetQuery.isCheckBox( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTWidget.checkBox( WIDGET_NAME ) )
					.append( stx.DOT )
					.append( FESTAction.uncheck() );
			}		
			// RadioButon Widget
			else if ( FESTWidgetQuery.isRadioButton( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTWidget.radioButton( WIDGET_NAME ) )
					.append( stx.DOT )
					.append( FESTAction.uncheck() );
			}			
		}		
		// Press Action
		else if ( FESTActionQuery.isPressAction( actionName ) ) {
			if ( FESTWidgetQuery.isKey( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTAction.pressAndReleaseKeys( WIDGET_NAME ) );
			}
		}
		// Show Action
		else if ( FESTActionQuery.isShowAction( actionName ) ) {
			// Window Widget
			if ( FESTWidgetQuery.isDialog( ELEMENT_TYPE )
					|| FESTWidgetQuery.isFrame( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTAction.show() );
			}
		}
		// Close Action
		else if ( FESTActionQuery.isCloseAction( actionName ) ) {
			// Do nothing on close action ! TestNG would try to close the
			// window but it is not what the test expects to happen. The fact
			// of clicking in a OK button, for instance, already closes the
			// window. So it is not to we close the window.
			return "";  
		}
		// Click Action / Double Click Action
		else if ( FESTActionQuery.isClickAction( actionName )
				|| FESTActionQuery.isDoubleClickAction( actionName )	) {					
			// Button Widget
			if ( FESTWidgetQuery.isButton( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTWidget.button( WIDGET_NAME ) );
			}
			// Menu Widget
			else if ( FESTWidgetQuery.isMenu( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTWidget.menuItem( WIDGET_NAME ) );
			}	
			// Table Widget
			else if ( FESTWidgetQuery.isTable( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTWidget.table( WIDGET_NAME ) );
			}
			// CheckBox Widget
			else if ( FESTWidgetQuery.isCheckBox( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTWidget.checkBox( WIDGET_NAME ) );
			}
			// RadioButon Widget
			else if ( FESTWidgetQuery.isRadioButton( ELEMENT_TYPE ) ) {
				found = true;
				b.append( FESTWidget.radioButton( WIDGET_NAME ) );
			}				
			
			if ( found ) {
				final boolean IS_CLICK = FESTActionQuery.isClickAction( actionName );
				b.append( stx.DOT ).append(
						IS_CLICK ? FESTAction.click() : FESTAction.doubleClick() );
			}
		}
		
		//
		// Comparisons by Element Type
		//
		
		// Message Element Type
		else if ( FESTWidgetQuery.isMessage( ELEMENT_TYPE ) ) {
			// Answer Yes Action
			if ( FESTActionQuery.isAnswerYesAction( actionName ) ) {
				found = true;
				b.append( FESTAction.answerYes( USER_READABLE_NAME ) );		
			}
		}
		
		//
		// If not found then return a void command
		//
		if ( ! found ) {
			return "";
		}

		// Add the step id comment
		b.append( stx.SC ).append( genInstrumentationComment( ssid, stepId ) ).append( stx.ENDL );
		
		return stx.levelStr() + b.toString();
	}

	
	private String genInstrumentationComment(final long ssid, final long stepId) {
		// Do not use tab: this comment is put after the command line
		return SEMANTIC_STEP_ID_COMMENT_START + instrumentationFormat( ssid, stepId );
	}
	
	private String instrumentationFormat(final long ssid, final long stepId) {
		return String.format( SEMANTIC_STEP_ID_FORMAT,
				SEMANTIC_STEP_ID,
				SEMANTIC_STEP_ID_ASSIGNMENT,
				ssid,
				SEMANTIC_STEP_ID_SEPARATOR,
				stepId
				);
	}
	
	
	private String makeTestWithGroupsAnnotation(final String ... groups) {
		StringBuilder sb = new StringBuilder();
		int i = groups.length;
		for ( String g : groups ) {
			sb.append( stx.QUOTE ).append( g ).append( stx.QUOTE );
			if ( i > 1 ) { sb.append( stx.COMMA ).append( stx.SPC ); }
			--i;
		}
		
		return String.format( FESTVocabulary.ANNOTATION__TEST_WITH_GROUPS, sb.toString() );
	}
	
	private String makeImportaceGroupName(String importance) {
		return "importance." + importance.toLowerCase();
	}
	
}