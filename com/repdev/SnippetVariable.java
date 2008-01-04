package com.repdev;

public class SnippetVariable{
	String id, tooltip, value, defaultValue;
	private boolean edited = false;

	public SnippetVariable(String id, String tooltip, String value) {
		super();
		this.id = id;
		this.tooltip = tooltip;
		
		if( value.trim().equals("") )
			value = id;
		
		this.value = value;
		this.defaultValue = new String(value);
	}

	public String getId() {
		return id;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void reset(){
		value = new String(defaultValue);
		setEdited(false);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setEdited(boolean edited) {
		this.edited = edited;
	}

	public boolean isEdited() {
		return edited;
	}
	
	
}