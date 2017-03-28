package org.betasearch.arvdroid.BetaSearch;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author avijayvargiy
 * Class Filter helps in keeping track of various documents present in a directory. It is basically a file picker.
 */
public class Filter {

	public File[] finder( String dirName) //It helps in picking all files of .txt extention in adirectory dirName.
	{
		File dir = new File(dirName);

		return dir.listFiles(new FilenameFilter() { 
			public boolean accept(File dir, String filename)
			{ return filename.endsWith(".txt"); }
		} );

	}

}
