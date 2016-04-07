package fr.jchaline.tools.bigbrother.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import fr.jchaline.tools.bigbrother.utils.Constants;

@Service
public class FileService {
	
	private static final Logger logger = Logger.getLogger(FileService.class);

	public FileService() {
	}

	/**
	 * Read all the line of the file and return the list
	 * 
	 * @param path the path to the file
	 * @return the list of the correct line
	 * @throws IOException
	 */
	public List<String> read(String path) throws IOException {
		return read(Paths.get(path));
	}

	public List<String> read(Path path) throws IOException {
		return Files.readAllLines(path);
	}

	public List<String> read(String path, Charset charset) throws IOException {
		return Files.readAllLines(Paths.get(path), charset);
	}

	public List<String> read(Path path, Charset charset) throws IOException {
		return read(path, charset);
	}

	/**
	 * Create a file in the folder given, with the content given
	 * 
	 * @param filePath the path to the folder
	 * @param text the text content
	 */
	public int write(String filePath, String text) {
		int error = Constants.STATUS_OK;
		// on met try si jamais il y a une exception
		BufferedWriter output = null;
		try {
			FileWriter fw = new FileWriter(filePath, false);
			// le BufferedWriter output auquel on donne comme argument le FileWriter fw cree juste au dessus
			output = new BufferedWriter(fw);

			// on marque dans le fichier ou plutot dans le BufferedWriter qui sert comme un tampon(stream)
			output.write(text);
			// on peut utiliser plusieurs fois methode write

			output.flush();
			// ensuite flush envoie dans le fichier, ne pas oublier cette methode pour le BufferedWriter

			output.close();
		} catch (IOException ioe) {
			error = Constants.STATUS_ERROR;
			logger.error(ioe);
		}
		return error;
	}

	/**
	 * Take an url and create all folders needed
	 * 
	 * @param path the path to the last folder to create
	 */
	public void makeFolder(String path) {
		File file = new File(path);
		file.mkdirs();

	}

	/**
	 * Find all the file in the directory given
	 * 
	 * @param deep the deep for recursively search. Stop at 0, negative parameter
	 *            for boundless
	 * @param fileList the list of the files
	 * @param directoryPath the directory to search recursively
	 * @param pattern the pattern the file must match, can be null
	 */
	public List<String> findFiles(int deep, String directoryPath, String pattern) {
		List<String> fileList = new ArrayList<String>();
		if (fileList != null) {
			File directory = new File(directoryPath);

			// si le fichier courant n'existe pas, on arrête cette passe
			if (!directory.exists()) {
			}
			// si le fichier courant n'est pas un repertoire, on l'ajoute ou non
			// en fonction du respect du pattern
			else if (!directory.isDirectory()) {
				boolean matches = Pattern.compile(pattern).matcher(directoryPath).matches();
				if (pattern == null || matches) {
					fileList.add(directoryPath);
				}
			}
			// sinon, en fonction de la profondeur paramétrée, on descend dans les sous répertoires
			else if (deep != 0) {
				File[] subfiles = directory.listFiles();
				for (int i = 0; i < subfiles.length; i++) {
					String name = subfiles[i].getName();
					String path = directoryPath + File.separator + name;
					fileList.addAll(findFiles(deep - 1, path, pattern));
				}
			}
		}
		return fileList;
	}

	/**
	 * Make a copy of the original file, rename it and insert in the new file
	 * the content given at the bigining
	 * 
	 * @param filename the path to the file
	 * @param offset the position to insert content
	 * @param content the content to insert
	 * @throws IOException the exception
	 */
	public void insert(String filename, long offset, byte[] content) throws IOException {
		RandomAccessFile r = new RandomAccessFile(new File(filename), "rw");
		RandomAccessFile rtemp = new RandomAccessFile(new File(filename + "~"), "rw");
		long fileSize = r.length();
		FileChannel sourceChannel = r.getChannel();
		FileChannel targetChannel = rtemp.getChannel();
		sourceChannel.transferTo(offset, (fileSize - offset), targetChannel);
		sourceChannel.truncate(offset);
		r.seek(offset);
		r.write(content);
		long newOffset = r.getFilePointer();
		targetChannel.position(0L);
		sourceChannel.transferFrom(targetChannel, newOffset, (fileSize - offset));
		sourceChannel.close();
		targetChannel.close();
		r.close();
		rtemp.close();
	}
}