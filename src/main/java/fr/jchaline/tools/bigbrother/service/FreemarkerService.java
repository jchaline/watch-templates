package fr.jchaline.tools.bigbrother.service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerService {

	public static String process(Path file, Map<String, Object> model) throws IOException, TemplateException {
		Configuration cfg = new Configuration();
		cfg.setDirectoryForTemplateLoading(file.toFile().getParentFile());
		Template template = cfg.getTemplate(file.getFileName().toString());
		Writer stringBuffer = new StringWriter();
		template.process(model == null ? new HashMap<String, Object>() : model, stringBuffer);
		return stringBuffer.toString();
	}
}
