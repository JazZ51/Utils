package com.distrimind.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ListClasses {
	/**
	 * This class enables to filter the files of a directory. It accepts only .class
	 * files
	 */
	protected static class DotClassFilter implements FilenameFilter {

		@Override
		public boolean accept(File arg0, String arg1) {
			return arg1.endsWith(".class");
		}

	}

	private static final HashMap<Package, Set<Class<?>>> cache = new HashMap<>();

	/**
	 * This method enables to list all classes contained into a given package
	 * 
	 * @param _package
	 *            the name of the considered package
	 * @return the list of classes

	 */
	public static Set<Class<?>> getClasses(Package _package) {
		Set<Class<?>> classes = cache.get(_package);
		if (classes != null)
			return classes;
		// creation of the list which will be returned
		classes = new HashSet<>();

		// We get all CLASSPATH entries
		String[] entries = System.getProperty("java.class.path").split(System.getProperty("path.separator"));

		// For all these entries, we check if they contains a directory, or a
		// jar file
		for (String entry : entries) {

			if (entry.endsWith(".jar")) {
				File jar = new File(entry);
				if (jar.isFile())
					classes.addAll(processJar(jar, _package));
			} else {
				File dir = new File(entry);
				if (dir.isDirectory()) {
					classes.addAll(processDirectory(dir, _package));
				}

			}

		}
		cache.put(_package, classes);
		return classes;
	}

	public static void main(String[] args) {
		File f = new File("/home/jason/misfont.log");
		System.out.println(f.getName());
	}

	/**
	 * This method enables to list all classes contained into a directory for a
	 * given package
	 * 
	 * @param _directory
	 *            the considered directory
	 * @param _package
	 *            the package name
	 * @return the list of classes
	 */
	private static Set<Class<?>> processDirectory(File _directory, Package _package) {
		Set<Class<?>> classes = new HashSet<>();

		// we generate the absolute path of the package
		ArrayList<String> repsPkg = splitPoint(_package.getName());

		for (String aRepsPkg : repsPkg) {
			_directory = new File(_directory, aRepsPkg);
		}

		// if the directory exists and if it is a directory, we list it
		if (_directory.exists() && _directory.isDirectory()) {
			// we filter the directory entries
			FilenameFilter filter = new DotClassFilter();
			File[] list = _directory.listFiles(filter);
			// for each element present on the directory, we add it into the
			// classes list.
			if (list!=null) {

				for (File aList : list) {
					try {
						classes.add(Class.forName(_package.getName() + "."
								+ aList.getName().substring(0, aList.getName().length() - 6)));
					} catch (Exception ignored) {

					}

				}
			}
		}

		return classes;
	}

	/**
	 * This method enables to list all classes contained into a jar file for a given
	 * package
	 *
	 * @param _jar_file
	 *            the considered jar file
	 * @param _package
	 *            the package name
	 * @return the list of classes
	 *
	 */
	private static Set<Class<?>> processJar(File _jar_file, Package _package) {
		Set<Class<?>> classes = new HashSet<>();

		try {
			JarFile jFile = new JarFile(_jar_file);
			String pkgPath = _package.getName().replace(".", "/");

			// for each jar entry
			for (Enumeration<JarEntry> entries = jFile.entries(); entries.hasMoreElements();) {
				JarEntry element = entries.nextElement();

				// if the name begins with the package path and ends with .class
				if (element.getName().startsWith(pkgPath) && element.getName().endsWith(".class")) {

					String class_name = element.getName().substring(pkgPath.length() + 1,
							element.getName().length() - 6);

					try {
						classes.add(Class.forName(_package.getName() + "." + class_name));
					} catch (Exception ignored) {

					}

				}

			}
			jFile.close();
		} catch (Exception ignored) {

		}
		return classes;
	}

	private static ArrayList<String> splitPoint(String s) {
		ArrayList<String> res = new ArrayList<>(10);
		int last_index = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '.') {
				if (i != last_index) {
					res.add(s.substring(last_index, i));
				}
				last_index = i + 1;
			}
		}
		if (s.length() != last_index) {
			res.add(s.substring(last_index));
		}

		return res;
	}
}
