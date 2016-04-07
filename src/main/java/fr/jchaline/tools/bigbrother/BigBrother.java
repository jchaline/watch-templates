package fr.jchaline.tools.bigbrother;

import java.io.IOException;
import java.util.Optional;

import org.apache.log4j.Logger;

import fr.jchaline.tools.bigbrother.service.SpringService;
import fr.jchaline.tools.bigbrother.service.TemplateService;

public class BigBrother {

	private static Logger logger = Logger.getLogger(BigBrother.class);

	public static void main(String[] args) throws IOException {
		
		SpringService.init();
		
		Optional<TemplateService> findFirst = SpringService.getBean(TemplateService.class).stream().findFirst();
		
		findFirst.ifPresent(service -> {
			try {
				service.watch();
			} catch (Exception e) {
				logger.error("Error with watching service", e);
			}
		});
		
		
	}

}
