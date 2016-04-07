package fr.jchaline.tools.bigbrother.utils;

public class Constants {
	
	//Spring context
    public static final String SPRING_BEANS_XML = "SpringBeans.xml";
    
	//skeleton file for common content of each file
	public static final String SKELETON_FILE = "skeleton_.html";
	
	//page file to process
	public static final String PATTERN_PAGE_FILE = "p_(.*\\.html)";

	//standard file to process
	public static final String PATTERN_OTHER_FILE = "f_(.*)";

	//whatever, this files aren't supposed to be managed here
	public static final String PATTERN_INCLUDE_FILE = "i_(.*)";
    
    public static final int STATUS_OK = 0;
    
    public static final int STATUS_ERROR = 1;
}
