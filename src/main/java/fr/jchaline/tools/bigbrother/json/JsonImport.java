package fr.jchaline.tools.bigbrother.json;

import java.util.List;

public class JsonImport {
	
	private List<String> jsFiles;
	
	private List<String> cssFiles;

	public JsonImport () {
		
	}

	public List<String> getJsFiles() {
		return jsFiles;
	}

	public void setJsFiles(List<String> jsFiles) {
		this.jsFiles = jsFiles;
	}

	public List<String> getCssFiles() {
		return cssFiles;
	}

	public void setCssFiles(List<String> cssFiles) {
		this.cssFiles = cssFiles;
	}
	
}
