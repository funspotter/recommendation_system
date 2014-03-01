import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.TextDirectoryLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class TextCategorizationTest {
	public void test() throws Exception {
		TextDirectoryLoader loader = new TextDirectoryLoader();
		loader.setCharSet("UTF-8");
		//loader.setDirectory(new File("/Users/huszarcsaba/Desktop/text_example"));
		loader.setDirectory(new File("/Users/huszarcsaba/Desktop/weka_cuccok/csopi"));
		Instances dataRaw = loader.getDataSet();
		
		
		 //System.out.println("\n\nImported data:\n\n" + dataRaw);
		 
		 String fileName = "csopi.arff";
	     	File file = new File("/Users/huszarcsaba/Desktop/", fileName);
	     	file.createNewFile(); // Creates file crawl_html/abc.txt
	    	BufferedWriter out = new BufferedWriter(new FileWriter(file));
	     	
//	     	out.write(dataRaw.toString());
//	     	
//	         out.close();

		// apply the StringToWordVector
		// (see the source code of setOptions(String[]) method of the filter
		// if you want to know which command-line option corresponds to which
		// bean property)
		StringToWordVector filter = new StringToWordVector();
		filter.setInputFormat(dataRaw);
		Instances dataFiltered = Filter.useFilter(dataRaw, filter);
		System.out.println("\n\nFiltered data:\n\n" + dataFiltered);
		
	
		out.write(dataFiltered.toString());
		out.close();
		 
//		 String fileName = "test.txt";
//     	File file = new File("/Users/huszarcsaba/Desktop/", fileName);
//     	file.createNewFile(); // Creates file crawl_html/abc.txt
     	
     	
//     	BufferedWriter out = new BufferedWriter(new FileWriter(file));
//     	
//     	out.write(dataFiltered.toString());
//     	
//      out.close();
		 
		 
		 

		// train J48 and output model
//		J48 classifier = new J48();
//		classifier.buildClassifier(dataFiltered);
//		
//		System.out.println("\n\nClassifier model:\n\n" + classifier);
	}
}