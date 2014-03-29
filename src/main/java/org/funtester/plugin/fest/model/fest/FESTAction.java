package org.funtester.plugin.fest.model.fest;

import org.funtester.common.util.StringUtil;
import org.funtester.plugin.code.KeyHelper;


public class FESTAction {

	// The regular expression . matches any character except a line terminator unless the DOTALL flag is specified.
	// @see http://docs.oracle.com/javase/1.5.0/docs/api/java/util/regex/Pattern.html#DOTALL	
	private static final String PATTERN_COMPILE_STR = "Pattern.compile( \"%s\", Pattern.DOTALL )"; 

	//
	// ACTIONS
	// show, close, cleanUp, setText, selectItem, selectItems, selectFirstItem,
	// clearSelecion, check, uncheck, click, requireMessage,
	// pressAndReleaseKeys, enterTableValue
	//
	
	public static String show() {
		return "show()";
	}
	
	public static String close() {
		return "close()";
	}	
	
	public static String cleanUp() {
		return "cleanUp()";
	}
	
	public static String setText(final String text) {
		return String.format( "setText( \"%s\" )", text );
	}
	 
	public static String selectItem(final String text) {
		return String.format( "selectItem( \"%s\" )", text );
	}
	
	// Compatible with 'list'
	public static String selectItems(final String ...text) {
		StringBuffer sb = new StringBuffer();
		boolean useComma = false;
		for ( String s : text ) {
			if ( useComma ) {
				sb.append( ", " );
			}
			sb.append( String.format( "\"%s\"", s ) );
			useComma = true;
		}
		return String.format( "selectItems( %s )", sb.toString() );
	}
	
	public static String selectFirstItem() {
		return "selectItem( 0 )";
	}
	
	public static String clearSelecion() {
		return "clearSelecion()";
	}
	
	public static String check() {
		return "check()";
	}	
	
	public static String uncheck() {
		return "uncheck()";
	}
	
	public static String enterText(final String text) {
		return String.format( "enterText( \"%s\" )", text );
	}	
	
	public static String click() {
		return "click()";
	}
	
	public static String doubleClick() {
		return "doubleClick()";
	}	
	
	public static String answerYes(final String message) {
		return String.format( "optionPane().requireMessage( \"%s\" ).yesButton().click()", message );
	}
	
	public static String answerNo(final String message) {
		return String.format( "optionPane().requireMessage( \"%s\" ).noButton().click()", message );
	}
	
	public static String answerOk(final String message) {
		return String.format( "optionPane().requireMessage( \"%s\" ).okButton().click()", message );
	}
	
	public static String answerCancel(final String message) {
		return String.format( "optionPane().requireMessage( \"%s\" ).cancelButton().click()", message );
	}
	
	public static String answerWithText(final String message, final String buttonTextRegEx) {
		return String.format( "optionPane().requireMessage( \"%s\" ).buttonWithText( "
				+ PATTERN_COMPILE_STR + " ).click()", message, buttonTextRegEx );
	}	
	
	public static String pressAndReleaseKeys(final String ...keys) {
		final String virtualKeys = KeyHelper.toVirtualKeySequence( keys );
		return "pressAndReleaseKeys( " + virtualKeys + " )";
	}
	
	public static String enterTableValue(final String row, final String col, final String value) {
		return String.format( "enterValue( row( %s ).column( %s ), \"%s\" )",
				row, col, value	);
	}	
	
	
	//
	// ORACLE
	//
	
	public static String oracle(final String regEx) {
		final String re = regEx.isEmpty() ? ".*" : StringUtil.normalizeCarriageReturn( regEx );
		return String.format( "optionPane().requireMessage( " + PATTERN_COMPILE_STR + " )", re );		
	}
}
