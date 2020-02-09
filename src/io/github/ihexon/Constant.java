package io.github.ihexon;

import io.github.ihexon.common.PrintUtils;
import io.github.ihexon.utils.FileCopier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constant {

	private static Constant instance = null;
	private String InstallDir = null;

	public String FILE_CONFIG = FILE_CONFIG_NAME;
	public static final String FILE_CONFIG_NAME = "config.xml";
	public static final String PROGRAM_NAME_SHORT = "WatchMe";
	public static final String PROGRAM_NAME = "WatchMe";
	private static final String DEV_VERSION = "Dev Build";
	public static String PROGRAM_VERSION = DEV_VERSION;
	public static String PROGRAM_TITLE = PROGRAM_NAME + " " + PROGRAM_VERSION;
	private static String WatchMeHome = null;
	public static final String SYSTEM_WatchMe_USER_LOG = "watchme.user.log";

	public Constant() {
		initializeFilesAndDirectories();
	}

	public static Constant getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			instance = new Constant();
		}
	}


	public boolean isLinux() {
		Pattern patternLinux = Pattern.compile("linux", Pattern.CASE_INSENSITIVE);
		String os_name = System.getProperty("os.name");
		Matcher matcher = patternLinux.matcher(os_name);
		return matcher.find();
	}

	public boolean isMacOsX() {
		Pattern patternMacOsX = Pattern.compile("mac", Pattern.CASE_INSENSITIVE);
		String os_name = System.getProperty("os.name");
		Matcher matcher = patternMacOsX.matcher(os_name);
		return matcher.find();
	}

	private String wmStd = null;
	public final String FILE_SEPARATOR = System.getProperty("file.separator");

	public String getDefaultHomeDirectory(boolean incDevOption) {
		if (wmStd == null) {
			wmStd = System.getProperty("user.home");
			if (wmStd == null) {
				wmStd = ".";
			}
			if (isLinux()) {
				// Linux: Hidden wm directory in the user's home directory
				wmStd += FILE_SEPARATOR + "." + PROGRAM_NAME_SHORT;
			} else if (isMacOsX()) {
				wmStd +=
						FILE_SEPARATOR
								+ "Library"
								+ FILE_SEPARATOR
								+ "Application Support"
								+ FILE_SEPARATOR
								+ PROGRAM_NAME_SHORT;

			} else {
				wmStd += FILE_SEPARATOR + PROGRAM_NAME;
			}
		}
		if (incDevOption) {
			return wmStd + "_D";
		}
		return wmStd;
	}

	private String getAbsolutePath(String directory) {
		String realPath = Paths.get(directory).toAbsolutePath().toString();
		String separator = FileSystems.getDefault().getSeparator();
		if (! realPath.endsWith(separator)) {
			realPath += separator;
		}
		return realPath;
	}

	public String getInstallDir() {
		if (InstallDir == null) {
			String path = ".";
			Path localDir = Paths.get(path);
			try {
				Path sourceLocation = Paths.get(Main.class
						.getProtectionDomain()
						.getCodeSource()
						.getLocation()
						.toURI());
				if (! Files.isDirectory(sourceLocation)) {
					sourceLocation = sourceLocation.getParent();
				}
				path = sourceLocation.toString();
			} catch (URISyntaxException e) {
				PrintUtils.werrPrintln("Failed to determine the ZAP installation dir: \n" + e.getMessage());
				path = localDir.toAbsolutePath().toString();
			}
			System.out.println("Defaulting ZAP install dir to " + path);
			InstallDir = getAbsolutePath(path);
		}
		return InstallDir;
	}

	public void initializeFilesAndDirectories() {
		File f;

		if (WatchMeHome == null) {
			WatchMeHome = getDefaultHomeDirectory(true);
		}
		WatchMeHome = getAbsolutePath(WatchMeHome);
		f = new File(WatchMeHome);
		FILE_CONFIG = WatchMeHome + FILE_CONFIG;


		try {
			System.setProperty(SYSTEM_WatchMe_USER_LOG, WatchMeHome);
			if (! f.isDirectory()) {
				if (f.exists()) {
					System.err.println("The home path is not a directory: " + WatchMeHome);
					System.exit(1);
				}
				if (! f.mkdir()) {
					System.err.println("Unable to create home directory: " + WatchMeHome);
					System.err.println("Is the path correct and there's write permission?");
					System.exit(1);
				}
			} else if (! f.canWrite()) {
				System.err.println("The home path is not writable: " + WatchMeHome);
				System.exit(1);
			} else {
				Path installDir = Paths.get(getInstallDir()).toRealPath();
				if (installDir.equals(Paths.get(WatchMeHome).toRealPath())) {
					System.err.println(
							"The install dir should not be used as home dir: " + installDir);
					System.exit(1);
				}
			}
			f = new File(FILE_CONFIG);
			if (! f.isFile()) {
				this.copyDefaultConfigs(f, true);
			}
		} catch (Exception e) {
			System.err.println("Unable to initialize home directory! " + e.getMessage());
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

	private final String PATH_BUNDLED_CONFIG_XML =
			"/io/github/ihexon/resources/" + FILE_CONFIG_NAME;

	private void copyDefaultConfigs(File f, boolean forceRest) throws IOException {
		FileCopier copier = new FileCopier();
		System.out.println("Copying default configuration to " + FILE_CONFIG);
		copyDefaultConfigFile();
	}

	private void copyDefaultConfigFile() throws IOException {
		// Paths.get.FILE_CONFIG means the ~/.wm_D/config.xml , this is the target dir where
		// xml/config.xml copy into, or bundle config.xml copy into.
		copyFileToHome(Paths.get(FILE_CONFIG), "xml/" + FILE_CONFIG_NAME, PATH_BUNDLED_CONFIG_XML);
	}




	private void copyFileToHome(Path targetFile, String sourceFilePath, String fallbackResource) throws IOException {
		Path defaultConfig = Paths.get(InstallDir, sourceFilePath);
		if (Files.exists(defaultConfig)) {
			Files.copy(defaultConfig, targetFile, StandardCopyOption.REPLACE_EXISTING);
		} else {
			try (InputStream inputStream = Constant.class.getResourceAsStream(fallbackResource)) {
				if (inputStream == null) throw new IOException("Bundled resource not found: " + fallbackResource);
				Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}


	private void setUpLogging() throws IOException {
	}

}