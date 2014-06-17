package org.funtester.plugin.fest.model.fest;

public class FESTActionQuery {

	public static boolean actionAcceptsMoreThanOneElement(String actionName) {
		return isClickAction( actionName )
			|| isDragAction( actionName )
			|| isPressAction( actionName )
			|| isSelectAction( actionName ) // multiple selection
			|| isTypeAction( actionName ) // just for tables
			;
	}	

	//
	// QUERY ACTION
	// show, close, answer.yes, answer.no, answer.cancel, answer.withtext,
	// {check, verify}, click, click.double, drag, {type, inform},
	// {select, choose}, {select.first, choose.first}, {deselect, unchoose},
	// press
	//
	
	public static boolean isShowAction(final String actionName) {
		return actionName.equalsIgnoreCase( "show" );
	}
	
	public static boolean isCloseAction(final String actionName) {
		return actionName.equalsIgnoreCase( "close" );
	}
	
	public static boolean isAnswerYesAction(final String actionName) {
		return actionName.equalsIgnoreCase( "answer.yes" );
	}	
	
	public static boolean isAnswerNoAction(final String actionName) {
		return actionName.equalsIgnoreCase( "answer.no" );
	}
	
	public static boolean isAnswerOkAction(final String actionName) {
		return actionName.equalsIgnoreCase( "answer.ok" );
	}	
	
	public static boolean isAnswerCancelAction(final String actionName) {
		return actionName.equalsIgnoreCase( "answer.cancel" );
	}
	
	public static boolean isAnswerWithTextAction(final String actionName) {
		return actionName.equalsIgnoreCase( "answer.withtext" );
	}	
	
	public static boolean isOracleAction(final String actionName) {
		return actionName.equalsIgnoreCase( "check" )
			|| actionName.equalsIgnoreCase( "verify" );
	}
	
	public static boolean isClickAction(final String actionName) {
		return actionName.equalsIgnoreCase( "click" );
	}
	
	public static boolean isDoubleClickAction(final String actionName) {
		return actionName.equalsIgnoreCase( "click.double" );
	}	
	
	public static boolean isDragAction(final String actionName) {
		return actionName.equalsIgnoreCase( "drag" );
	}
	
	public static boolean isTypeAction(final String actionName) {
		return actionName.equalsIgnoreCase( "type" )
			|| actionName.equalsIgnoreCase( "inform" );
	}
	
	public static boolean isSelectAction(final String actionName) {
		return actionName.equalsIgnoreCase( "select" )
			|| actionName.equalsIgnoreCase( "choose" );
	}
	
	public static boolean isSelectFirstAction(final String actionName) {
		return actionName.equalsIgnoreCase( "select.first" )
			|| actionName.equalsIgnoreCase( "choose.first" );
	}
	
	public static boolean isDeselectAction(final String actionName) {
		return actionName.equalsIgnoreCase( "deselect" )
			|| actionName.equalsIgnoreCase( "unchoose" );
	}	
	
	public static boolean isPressAction(final String actionName) {
		return actionName.equalsIgnoreCase( "press" );
	}
	
}
