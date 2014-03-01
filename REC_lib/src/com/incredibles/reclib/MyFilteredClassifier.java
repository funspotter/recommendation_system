package com.incredibles.reclib;

/**
 * A Java class that implements a simple text classifier, based on WEKA.
 * To be used with MyFilteredLearner.java.
 * WEKA is available at: http://www.cs.waikato.ac.nz/ml/weka/
 * Copyright (C) 2013 Jose Maria Gomez Hidalgo - http://www.esp.uem.es/jmgomez
 *
 * This program is free software: you can redistribute it and/or modify
 * it for any purpose.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
 
import weka.core.*;
import weka.classifiers.meta.FilteredClassifier;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.List;
import java.util.ArrayList;
import java.io.*;

/**
 * This class implements a simple text classifier in Java using WEKA.
 * It loads a file with the text to classify, and the model that has been
 * learnt with MyFilteredLearner.java.
 * @author Jose Maria Gomez Hidalgo - http://www.esp.uem.es/jmgomez
 * @see MyFilteredLearner
 */
 public class MyFilteredClassifier {

	/**
	 * String that stores the text to classify
	 */
	String text;
	/**
	 * Object that stores the instance.
	 */
	Instances instances;
	/**
	 * Object that stores the classifier.
	 */
	FilteredClassifier classifier;

	/**
	 * This method loads the text to be classified.
	 * @param fileName The name of the file that stores the text.
	 */
	public void load(String fileName) {
//		try {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("model/Rocky.txt"), "UTF-8"));
//			String line;
//			text = "";
//			while ((line = reader.readLine()) != null) {
//                text = text + " " + line;
//            }
//			text=fileName;
//			
//			System.out.println("===== Loaded text data: " + fileName + " =====");
//			//reader.close();
//			System.out.println(text);
//		}
//		catch (IOException e) {
//			System.out.println("Problem found when reading: " + fileName);
//		}
		text=fileName;
	}

	/**
	 * This method loads the model to be used as classifier.
	 * @param fileName The name of the file that stores the text.
	 */
	public void loadModel(String fileName) {
		try {

			//ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));

			ObjectInputStream in = new ObjectInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("model/davidshit.model"));

			Object tmp = in.readObject();
			classifier = (FilteredClassifier) tmp;
            in.close();
 			//System.out.println("===== Loaded model: " + fileName + " =====");
       } 
		catch (Exception e) {
			// Given the cast, a ClassNotFoundException must be caught along with the IOException
			//System.out.println("Problem found when reading: " + fileName);
		}
	}

	/**
	 * This method creates the instance to be classified, from the text that has been read.
	 * @throws Exception 
	 */
	public void makeInstance() throws Exception {
		// Create the attributes, class and text
		FastVector fvNominalVal = new FastVector(6);
		
		fvNominalVal.addElement("1");//excited
		fvNominalVal.addElement("2");//tender
		fvNominalVal.addElement("3");//scared
		fvNominalVal.addElement("4");//angry
		fvNominalVal.addElement("5");//sad
		fvNominalVal.addElement("6");//happy
		
		
		Attribute attribute1 = new Attribute("class", fvNominalVal);
		Attribute attribute2 = new Attribute("text",(FastVector) null);
		// Create list of instances with one element
		FastVector fvWekaAttributes = new FastVector(2);
		fvWekaAttributes.addElement(attribute1);
		fvWekaAttributes.addElement(attribute2);
		instances = new Instances("Test relation", fvWekaAttributes, 1);           
		// Set class index
		instances.setClassIndex(0);
		// Create and add the instance
		DenseInstance instance = new DenseInstance(2);
		instance.setValue(attribute2, text);
		// Another way to do it:
		// instance.setValue((Attribute)fvWekaAttributes.elementAt(1), text);
		instances.add(instance);
		
		StringToWordVector filter = new StringToWordVector();
		filter.setInputFormat(instances);
		instances = Filter.useFilter(instances, filter);
		
 		//System.out.println("===== Instance created with reference dataset =====");
		//System.out.println(instances);
	}

	/**
	 * This method performs the classification of the instance.
	 * Output is done at the command-line.
	 */
	public String classify() {
		String ret = null;
		try {
			double pred = classifier.classifyInstance(instances.instance(0));
			//System.out.println("===== Classified instance =====");
			//System.out.println("Class predicted: " + instances.classAttribute().value((int) pred));
			ret = instances.classAttribute().value((int) pred);
		}
		catch (Exception e) {
			//e.printStackTrace();
			//System.out.println("Problem found when classifying the text");
		}	
		return ret;
	}

	/**
	 * Main method. It is an example of the usage of this class.
	 * @param args Command-line arguments: fileData and fileModel.
	 */
	public String makeClassifier (String text) {

		String ret = null;
		
		MyFilteredClassifier classifier;
		
		classifier = new MyFilteredClassifier();
		//classifier.load("/Users/huszarcsaba/Desktop/lll/Rocky.txt");
		//classifier.loadModel("/Users/huszarcsaba/Desktop/weka_cuccok/funspotter_ultimatetag8.model");
		
		classifier.load(text);
		classifier.loadModel("");
		try {
			classifier.makeInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ret = classifier.classify();
		return ret;
	}
}