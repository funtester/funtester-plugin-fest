package org.funtester.plugin.fest.model.fest;

public class FESTWidgetQuery {

	//
	// QUERY ELEMENT TYPE
	// dialog, frame, {textbox, text, edit}, button, {menu, menuitem, submenu},
	// {message, dialog}, key, {table, grid},
	// {combobox, combo, select, selectbox}, {checkbox, check},
	// {radio, radiobutton}, {list, listbox}
	//
	
	public static boolean isDialog(final String elementType) {
		return elementType.equalsIgnoreCase( "dialog" );
	}
	
	public static boolean isFrame(final String elementType) {
		return elementType.equalsIgnoreCase( "frame" );
	}		
	
	public static boolean isTextBox(final String elementType) {
		return elementType.equalsIgnoreCase( "textbox" )
			|| elementType.equalsIgnoreCase( "text" )
			|| elementType.equalsIgnoreCase( "edit" );
	}
	
	public static boolean isButton(final String elementType) {
		return elementType.equalsIgnoreCase( "button" );
	}
	
	public static boolean isMenu(final String elementType) {
		return elementType.equalsIgnoreCase( "menu" )
			|| elementType.equalsIgnoreCase( "menuitem" )
			|| elementType.equalsIgnoreCase( "submenu" );
	}	
	
	public static boolean isMessage(final String elementType) {
		return elementType.equalsIgnoreCase( "message" )
			|| elementType.equalsIgnoreCase( "dialog" );
	}
	
	public static boolean isKey(final String elementType) {
		return elementType.equalsIgnoreCase( "key" );
	}

	public static boolean isTable(final String elementType) {
		return elementType.equalsIgnoreCase( "table" )
			|| elementType.equalsIgnoreCase( "grid" );
	}

	public static boolean isComboBox(final String elementType) {
		return elementType.equalsIgnoreCase( "combobox" )
			|| elementType.equalsIgnoreCase( "combo" )
			|| elementType.equalsIgnoreCase( "select" )
			|| elementType.equalsIgnoreCase( "selectbox" );
	}
	
	public static boolean isCheckBox(final String elementType) {
		return elementType.equalsIgnoreCase( "checkbox" )
			|| elementType.equalsIgnoreCase( "check" );
	}
	
	public static boolean isRadioButton(final String elementType) {
		return elementType.equalsIgnoreCase( "radio" )
			|| elementType.equalsIgnoreCase( "radiobutton" );
	}
	
	public static boolean isList(final String elementType) {
		return elementType.equalsIgnoreCase( "list" )
			|| elementType.equalsIgnoreCase( "listbox" );
	}
}
