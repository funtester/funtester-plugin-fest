package org.funtester.plugin.fest.model.fest;

public class FESTVocabulary {
	
	public static final String ANNOTATION__BEFORE_CLASS		= "@BeforeClass";
	public static final String ANNOTATION__BEFORE_METHOD	= "@BeforeMethod";
	public static final String ANNOTATION__AFTER_METHOD		= "@AfterMethod";
	//public static final String ANNOTATION__TEST				= "@Test";
	public static final String ANNOTATION__TEST_WITH_GROUPS	= "@Test( groups={ %s } )";
	
	public static final String CLASS_DIALOG_FIXTURE			= "DialogFixture";
	public static final String CLASS_FRAME_FIXTURE			= "FrameFixture";
//	public static final String CLASS_WINDOW_FIXTURE			= "WindowFixture"; // <<< WindowFixture is abstract. Child classes are DialogFixture and FrameFixture.
	
	public static final int EXPECTED_TABLE_PARAMETERS		= 3;

}
