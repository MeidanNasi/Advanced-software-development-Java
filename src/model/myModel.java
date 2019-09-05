package model;

import model.client.Command;
import model.client.ConnectCommand;
import model.client.Interpreter;
import model.client.OpenServerDataCommand;
import model.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;

import static model.client.ConnectCommand.output;

public class myModel extends Observable {

    Server calcServer; //server to calc best path
    private Interpreter aircraftControl; //incharge of controlling the aircraft by joystick/script
    private int calcServerPort;
    private PrintWriter outTocalcServer;
    private BufferedReader inFromCalcServer;
    private double [][] matrix;
    private String path;
    private String ip;
    private int port;

    

    public String getPath() {
        return path;
    }

    public myModel(int calcServerPort) {
        this.calcServerPort=calcServerPort;
        aircraftControl=new Interpreter("./resources/simulator_vars.txt");
        Command openServer=new OpenServerDataCommand();
        openServer.doCommand(new ArrayList<>(Arrays.asList("openDataServer", "5400", "10")),0);
       
    }
    public void runScript(String[] lines){
        aircraftControl.lexer(lines);
        aircraftControl.parser();
    }

    public void controlElevatorAileron(double elevator, double aileron){
        output.println("set /controls/flight/elevator "+elevator);
        output.println("set /controls/flight/aileron "+aileron);
        output.flush();
    }
    public void controlRudder(double rudder) {
    	output.println("set /controls/flight/rudder "+rudder);
    	output.flush();
    }
    public void controlThrottle(double throttle){
    	output.println("set /controls/engines/current-engine/throttle "+throttle);
    	output.flush();
    }
    public void connectToSim(String ip, String port){
        new ConnectCommand().doCommand(new ArrayList<>(Arrays.asList("connect", ip, port)),0);
    }
    
    
    
    	// *** connecting to calculate path server and returning the solution *** // 
    public String connectToCalcServer(String ip, String port, double [][] matrix, String init, String goal){
    	this.ip = ip;
    	this.port = Integer.parseInt(port);
        this.matrix=matrix;
        return  getPathFromCalcServer(init, goal);// returning solution from the algorithm
    }
    
    	// *** getting the solution from the server and "send" it to connect ** // 
    public String getPathFromCalcServer (String init, String goal){
    	try {
    		Socket theServer=new Socket(ip, port);
            System.out.println("connected to calc server");
            outTocalcServer=new PrintWriter(theServer.getOutputStream());
            inFromCalcServer=new BufferedReader(new InputStreamReader(theServer.getInputStream()));
        } catch (IOException ignored) {}
        int i, j;
        System.out.println("sending problem...");
        for(i=0;i<matrix.length;i++){
            System.out.print("\t");
            for(j=0;j<matrix[i].length-1;j++){
                outTocalcServer.print(matrix[i][j]+",");
            }
            outTocalcServer.println(matrix[i][j]);
        }
        outTocalcServer.println("end");
        outTocalcServer.println(init);
        outTocalcServer.println(goal);
        outTocalcServer.flush();
        System.out.println("\tend\n\t"+init+"\n\t"+goal);
        System.out.println("\tproblem sent, waiting for solution...");
        try {
            path=inFromCalcServer.readLine();
            System.out.println(path);
        } catch (IOException ignored) {}
        System.out.println("\tsolution received");
    	return path;
    }
    
    // we sample the plane position due to simulator every 4 times each second.
    public void samplePlanePosition() {
    	new Thread(()-> {
    		while(true) {
	    		String[] positions = new String[2];
	    		positions[0] = String.valueOf(Interpreter.symbolTable.get("/position/latitude-deg").value);
	    		positions[1] = String.valueOf(Interpreter.symbolTable.get("/position/longitude-deg").value);
	    		this.setChanged();
	    		this.notifyObservers(positions);
	    		try {Thread.sleep(250);}
	    		catch (InterruptedException ignored) {}
    		}
    	}).start();
    }
    
    
    
}