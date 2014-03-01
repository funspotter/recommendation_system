package com.incredibles.reclib;

public class OutputData {

	public static String doubleMatrixToString(double[][] m) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m.length; j++) {
				System.out.println(m[i][j]);
				str.append(Double.toString(m[i][j]) + ",");
			}
			str.append("\n");
		}

		return str.toString();
	}

}
