import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class QuestionInstanceClassifiy {

    public static void main(String[] args) {
        QuestionInstanceClassifiy q = new QuestionInstanceClassifiy();
        double result = q.classify();
        System.out.println(result);
    }

    private Instance inst_co;

    public double classify()  {

        // Create attributes to be used with classifiers
        // Test the model
        double result = -1;
        try {

            ArrayList<Attribute> attributeList = new ArrayList<Attribute>(1);

            ArrayList<String> classVal = new ArrayList<String>();
            classVal.add("akcio");
            classVal.add("animacio");
            classVal.add("dokumentumfilm");
            classVal.add("drama");
            classVal.add("fantasy");
            classVal.add("horror");
            classVal.add("katasztrofafilm");
            classVal.add("krimi");
            classVal.add("romantikus");
            classVal.add("scifi");
            classVal.add("thriller");
            classVal.add("vigjatek");

            attributeList.add(new Attribute("@@class@@",classVal));

            Instances data = new Instances("TestInstances",attributeList,0);

            // Create instances for each pollutant with attribute values latitude,
            // longitude and pollutant itself
            inst_co = new DenseInstance(data.numAttributes());
            data.add(inst_co);

            // Set instance's values for the attributes "latitude", "longitude", and
            // "pollutant concentration"

            // load classifier from file
            Classifier cls_co = (Classifier) weka.core.SerializationHelper.read("/users/huszarcsaba/Desktop/faszamodell.model");

            result = cls_co.classifyInstance(inst_co);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }
}