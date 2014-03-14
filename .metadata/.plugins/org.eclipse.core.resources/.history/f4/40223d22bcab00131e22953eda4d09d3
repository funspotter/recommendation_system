package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;

import com.incredibles.storage.ClientDbService;
import com.incredibles.storage.ClientDbServiceCreator;

public class FirstStep {
	
	public String makeFirstStep(int userid){
		ClientDbService dbService = null;
		try {
			dbService = ClientDbServiceCreator.createCloud();
			dbService.copyFromTempToRec(userid);
	
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			if (dbService != null) {
				try {
					dbService.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
}
