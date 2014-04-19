package org.funtester.plugin.fest.model.fest;

public class FESTWidget {
	
	private static final String COMMA = ", ";

	//
	// WIDGETS
	// textBox, comboBox, checkBox, radioButton, button, menuItem, table, list
	// tree
	
	public static String textBox(final String name) {
		return "textBox( \"" + name + "\" )";
	}
	
	public static String comboBox(final String name) {
		return "comboBox( \"" + name + "\" )";
	}
	
	public static String checkBox(final String name) {
		return "checkBox( \"" + name + "\" )";
	}	
	
	public static String radioButton(final String name) {
		return "radioButton( \"" + name + "\" )";
	}
	
	public static String button(final String name) {
		return "button( \"" + name + "\" )";
	}
	
	public static String menuItem(final String name) {
		return "menuItem( \"" + name + "\" )";
	}
	
	public static String menuItem(final String ... names) {
		StringBuilder params = new StringBuilder();
		boolean shoudHaveComma = false;
		for ( String n : names ) {
			if ( shoudHaveComma ) {
				params.append( COMMA );
			} else {
				shoudHaveComma = true;
			}
			params.append( "\"" + n + "\"" );
		}
		return "menuItemWithPath( " + params.toString() +  " )";
	}
	
	public static String table(final String name) {
		return "table( \"" + name + "\" )";
	}
	
	public static String list(final String name) {
		return "list( \"" + name + "\" )";
	}
	
	public static String tree(final String name) {
		return "tree( \"" + name + "\" )";
	}
}
