package viewmodel;


import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.myModel;

import java.awt.Point;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

public class ViewModel extends Observable implements Observer {

    myModel model;
    public DoubleProperty joyStickX, joyStickY, throttle, rudder, longitude, latitude; //value of the joyStick position
    public StringProperty pathVm;
    

    public ViewModel(myModel m) {
        this.model = m;
        joyStickY=new SimpleDoubleProperty();
        joyStickX=new SimpleDoubleProperty();
        throttle=new SimpleDoubleProperty();
        rudder=new SimpleDoubleProperty();
        pathVm=new SimpleStringProperty();
        longitude = new SimpleDoubleProperty();
        latitude = new SimpleDoubleProperty();
    }

    public void runScriptVm(String script){ // running in its thread.
    	new Thread(()-> model.runScript(script.split("\n"))).start();
    	
    }
    public void controlElevatorAileronVm(){
        double elevatorVal;
        double aileronVal;
        elevatorVal=-joyStickY.doubleValue()/70; //Double.min(0-(joyStickY.doubleValue()/70), 1);
        aileronVal=joyStickX.doubleValue()/70; //Double.min(joyStickX.doubleValue()/70, 1);
        model.controlElevatorAileron(elevatorVal, aileronVal);
    }
    public void controlRudderVm(){
    	model.controlRudder(rudder.doubleValue());
    }
    public void controlThrottleVm(){
    	model.controlThrottle(throttle.doubleValue());
    }
    public void connectToSimVM(String ip, String port){
    	model.connectToSim(ip, port);
    }
    public String getPathFromCalcServerVm(Point p1, Point p2){
    	String initPoint = p1.x+","+p1.y; // initializing points to strings and send it to model
    	String goalPoint = p2.x+","+p2.y; // the model will calculate the path and send it to vm
        return model.getPathFromCalcServer(initPoint, goalPoint);

    }
    public String connectToCalcServerVm(String ip, String port, double[][] matrix, Point p1, Point p2){

    	String initPoint = p1.x+","+p1.y; // initializing points to strings and send it to model
    	String goalPoint = p2.x+","+p2.y; // the model will calculate the path and send it to vm
        //this.pathVm.setValue(model.connectToCalcServer(ip, port,matrix,initPoint, goalPoint));
    	return model.connectToCalcServer(ip, port,matrix,initPoint, goalPoint);
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o.equals(model)){        
           latitude.setValue(Double.parseDouble(((String[])arg)[0])); 
           longitude.setValue(Double.parseDouble(((String[])arg)[1]));
           setChanged();
           notifyObservers();
        }
    }
    public void getCurrentPosition() {
    	model.samplePlanePosition();
    }
}