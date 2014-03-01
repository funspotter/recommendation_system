import java.io.IOException;
import java.sql.SQLException;

import com.incredibles.reclib.InputData;


public class kk {
	public void test(){
		InputData r=null;
		try {
			r = new InputData();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double [][]rt= r.logUserItemMatrix();
		
		
		
	}
}
