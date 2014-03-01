import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.Rotation;

public class ResultPlot extends JFrame {

  private static final long serialVersionUID = 1L;

  public ResultPlot(String applicationTitle, String chartTitle) {
	  super(applicationTitle);
      XYDataset data=null;
	try {
		data = createDataset();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      JFreeChart chart = ChartFactory.createScatterPlot(
          "Scatter Plot Demo",
          "X", "Y", 
          data, 
          PlotOrientation.VERTICAL,
          true, 
          true, 
          false
      );
      NumberAxis domainAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
      domainAxis.setAutoRangeIncludesZero(false);
      ChartPanel chartPanel = new ChartPanel(chart);
      chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
      chartPanel.setVerticalAxisTrace(true);
      chartPanel.setHorizontalAxisTrace(true);
      //chartPanel.setVerticalZoom(true);
      //chartPanel.setHorizontalZoom(true);
      setContentPane(chartPanel);

    }


  private static XYDataset createDataset() throws IOException{
	  
	  XYSeriesCollection dataset = new XYSeriesCollection();
      XYSeries series = new XYSeries("Scatter");
      
      double lineCount=0;
      
      System.out.println("elkezdődött a sorszámlálás");
      
//      BufferedReader br0 = new BufferedReader(new FileReader("/Users/huszarcsaba/Desktop/newcoords.txt"));
//	    try {
//	        String line = br0.readLine();
//	        while (line != null) {
//	        	 lineCount++;
//	        	 System.out.println("sorszámlálás: "+lineCount + " " +line);
//	        	 line = br0.readLine();
//	        }
//	    } finally {
//	        br0.close();
//	    }
//      
	    double nowLine=0;
	  
	  BufferedReader br = new BufferedReader(new FileReader("C:/Users/Otthon/Desktop/newcoords.txt"));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	        	nowLine++;
	        	System.out.println("százalék: "+nowLine/lineCount*100);
	        	 String[] str = line.split(",");
	        	   double x = Double.valueOf(str[0]);
	        	   double y = Double.valueOf(str[1]);
	        	   series.add(x, y);
	        	   line = br.readLine();
	        }
	    } finally {
	        br.close();
	    }
      dataset.addSeries(series);
      return dataset;
  }
} 