package fr.jchaline.tools.bigbrother.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import fr.jchaline.tools.bigbrother.utils.Constants;

public class SpringService {
	private static Logger logger = Logger.getLogger(SpringService.class);
	private static ApplicationContext context;

	public static void init() {
		if (context == null) {
			context = new ClassPathXmlApplicationContext(Constants.SPRING_BEANS_XML);
		}
	}

	/**
	 * Find bean if exist
	 * 
	 * @param beanId the bean id
	 * @return the bean
	 */
	public static Object getBean(String beanId) {
		Object bean = context.getBean(beanId);
		return bean;
	}

	/**
	 * Find beans with class matching
	 * 
	 * @param type the class to match
	 * @return the beans list
	 */
	public static <T> List<T> getBean(Class<T> type) {
		List<T> list = new ArrayList<T>();
		logger.debug("Get bean for " + type);
		Map<String, T> beansOfType = context.getBeansOfType(type);
		list.addAll(beansOfType.values());
		return list;
	}
}
