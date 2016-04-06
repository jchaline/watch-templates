package fr.jchaline.tools.bigbrother;

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

public class BigBrother {

	/**
	 * Default directory NOTES: The Watch Service API does not allow registering
	 * an individual file. We’ll get a java.nio.file.NotDirectoryException if
	 * trying to do so.
	 */
	public static final String DEFAULT_DIRECTORY = "templates";

	public static void main(String[] args) throws IOException {
		WatchService watcher = FileSystems.getDefault().newWatchService();

		Path dir = Paths.get(DEFAULT_DIRECTORY);
		registerAll(watcher, dir);

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

				System.out.println(kind.name() + ": " + fileName);

				// gère la création de répertoire mais pas la modification ou la
				// suppression !
				Path context = (Path) key.watchable();
				if (Files.isDirectory(context.resolve(fileName))) {
					key.reset();
					watcher.close();
					watcher = FileSystems.getDefault().newWatchService();
					registerAll(watcher, dir);
				} else {
					if (kind == OVERFLOW) {
						continue;
					} else if (kind == ENTRY_CREATE) {
						System.out.println("process create event");
						create(ev);
					} else if (kind == ENTRY_DELETE) {
						System.out.println("process delete event");
					} else if (kind == ENTRY_MODIFY) {
						System.out.println("process modify event");
						modify(ev);
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

	public static void create(WatchEvent<Path> ev) {

	}

	public static void modify(WatchEvent<Path> ev) {

	}

	private static void registerAll(WatchService watcher, final Path start) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				registerDir(watcher, dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static void registerDir(WatchService watcher, Path dir) throws IOException {
		dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		System.out.println("register " + dir);
	}

}
