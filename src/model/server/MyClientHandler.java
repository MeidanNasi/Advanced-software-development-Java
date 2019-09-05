package model.server;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class MyClientHandler<Problem,Solution> implements ClientHandler{

	private Solver<Searchable<Point>,String> solver;
	private CacheManager<Searchable<Point>,String> cm;
	private Searchable ms;

	public MyClientHandler(){
		this.solver=new SolverSearcher<>(new BFS<>());
		this.cm=new FileCacheManager<>();
	}
	
	@Override
		public void handleClient(InputStream in, OutputStream out) throws IOException {
			
			BufferedReader userInput = new BufferedReader(new InputStreamReader(in));
			PrintWriter outToScreen=new PrintWriter(out);
			
			ArrayList<String[]> list = new ArrayList<>();
			String outputToScreen = "SP";
			String recieveProb;			
			
			while(!(recieveProb = userInput.readLine()).equals("end")){
				list.add(recieveProb.split(","));
			}
			
			int column=list.get(0).length;
			int row=list.size();

			double[][] matrix = new double[list.size()][list.get(0).length];
			for(int i=0; i<row; i++) {
				for(int j=0; j<column; j++) {
					matrix[i][j] = Double.parseDouble(list.get(i)[j]);
					System.out.println(matrix[i][j]);
				}
			}
			
		
			String[] receiveSource = userInput.readLine().split(",");
			Point sourcePoint= new Point(Integer.parseInt(receiveSource[0]),Integer.parseInt(receiveSource[1]));

			String[] receiveGoal = userInput.readLine().split(",");
			Point goalPoint = new Point(Integer.parseInt(receiveGoal[0]),Integer.parseInt(receiveGoal[1]));

			ms = new MatrixSearchable(matrix,sourcePoint,goalPoint,row,column);
//			outToScreen.println(cm.retrieveSol(ms));
//			outToScreen.flush();
			
			
			outToScreen.println(this.solver.solve(ms));
			outToScreen.flush();
//			if (this.cm.DoesSolutionExist(ms)) {
//				outToScreen.println(cm.retrieveSol(ms));
//				outToScreen.flush();
//			}
//			else {
//				outputToScreen=this.solver.solve(ms);
//				this.cm.SolutionSaver(outputToScreen,ms);
//				outToScreen.println(outputToScreen);
//				outToScreen.flush();
//			}


			userInput.close();
			outToScreen.close();
		}

	}

