package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

public class InputData {
	public static Reader r;

	public InputData() throws SQLException, IOException {
		super();
		r = new Reader();
		r.T = 0;
	//	r.read_from_api();
	}

	public double[][] logUserItemMatrix() {
		return r.spd.toArray();
	}

//	public HashMap<Integer, Integer> getUser_map() {//valodi id-nalam id
//		return r.user_map;
//	}

//	public HashMap<Integer, Integer> getItem_map() {
//		return r.item_map;
//	}

}
