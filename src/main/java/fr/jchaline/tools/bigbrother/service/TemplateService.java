package fr.jchaline.tools.bigbrother.service;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.jchaline.tools.bigbrother.json.JsonImport;
import fr.jchaline.tools.bigbrother.utils.Constants;
import freemarker.template.TemplateException;

@Service
public class TemplateService {

	private static Logger logger = Logger.getLogger(TemplateService.class);

	@Value("${bigbrother.src}")
	private String src;

	@Value("${bigbrother.target}")
	private String target;

	@Autowired
	private FileService fileService;
	
	@Autowired
	private FreemarkerService freemarkerService;

	public TemplateService() {
	}

	public void watch() throws IOException, TemplateException {
		WatchService watcher = FileSystems.getDefault().newWatchService();

		Path dir = Paths.get(src);
		registerAll(watcher, dir);

		Path skeletonPath = dir.resolve(Constants.SKELETON_FILE);

		while (true) {
			WatchKey key;
			try {
				// wait for a key to be available
				key = watcher.take();
			} catch (InterruptedException ex) {
				return;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				// get event type
				WatchEvent.Kind<?> kind = event.kind();

				// get file name
				@SuppressWarnings("unchecked")
				WatchEvent<Path> ev = (WatchEvent<Path>) event;
				Path fileName = ev.context();

				logger.info(kind.name() + ": " + fileName);

				// gère la création de répertoire mais pas la modification ou la
				// suppression !
				Path context = (Path) key.watchable();
				Path absoluteFile = context.resolve(fileName);
				if (Files.isDirectory(absoluteFile)) {
					key.reset();
					watcher.close();
					watcher = FileSystems.getDefault().newWatchService();
					registerAll(watcher, dir);
				} else {
					if (kind == OVERFLOW) {
						continue;
					} else if (kind == ENTRY_CREATE) {
						modify(ev, skeletonPath, absoluteFile);
					} else if (kind == ENTRY_DELETE) {
						logger.info("process delete event");
						delete(ev, absoluteFile);
					} else if (kind == ENTRY_MODIFY) {
						modify(ev, skeletonPath, absoluteFile);
					}

					// IMPORTANT: The key must be reset after processed
					boolean valid = key.reset();
					if (!valid) {
						break;
					}
				}
			}
		}
	}

	private void delete(WatchEvent<Path> ev, Path absoluteFile) throws IOException {
		String pageFileGenerated = getPageFileGenerated(absoluteFile);
		
		Path toDelete = Paths.get(pageFileGenerated);
		Files.delete(toDelete);
	}

	private void modify(WatchEvent<Path> ev, Path skeletonFile, Path absoluteFile) throws IOException, TemplateException {
		logger.info("process modify event : " + absoluteFile);

		String pageFile = getPageFile(absoluteFile);
		if (StringUtils.isNotBlank(pageFile)) {
			// process this page, remove first line first (css and js include)
			List<String> read = fileService.read(absoluteFile);

			if (read.size() > 0) {
				String firstLine = read.get(0);
				if (firstLine.startsWith("@{")) {
					ObjectMapper mapper = new ObjectMapper();
					
					JsonImport imports = mapper.readValue(firstLine.substring(1), JsonImport.class);
					
					String pageBody = freemarkerService.process(absoluteFile, null);
					pageBody = pageBody.substring(pageBody.indexOf('\n') + 1);
					
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("jsFiles", imports.getJsFiles());
					params.put("cssFiles", imports.getCssFiles());
					params.put("body", pageBody);

					String result = freemarkerService.process(skeletonFile, params);
					
					fileService.write(getPageFileGenerated(absoluteFile), result);
					
					logger.info(imports);
				}
			}
		}
	}

	private String getPageFileGenerated(Path absoluteFile) {
		String fileName = absoluteFile.getName(absoluteFile.getNameCount() - 1).toString();
		
		Pattern p = Pattern.compile(Constants.PATTERN_PAGE_FILE);
		Matcher m = p.matcher(fileName);
		
		m.find();
		String newFileName = m.group(1);
		
		return absoluteFile.toString().replace(src, target).replace(fileName, newFileName);
	}

	private void registerAll(WatchService watcher, final Path start) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				registerDir(watcher, dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private void registerDir(WatchService watcher, Path dir) throws IOException {
		dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		logger.info("register " + dir);
	}

	/**
	 * Détermine le nom du fichier, afin d'en déduire sa nature (page, include, fichier ...)
	 * @param file
	 * @return
	 */
	private String getPageFile(Path file) {
		String fileName = file.getName(file.getNameCount() - 1).toString();

		Pattern p = Pattern.compile(Constants.PATTERN_PAGE_FILE);
		Matcher m = p.matcher(fileName);

		if (m.find()) {
			return m.group(1);
		} else {
			return StringUtils.EMPTY;
		}
	}

}
