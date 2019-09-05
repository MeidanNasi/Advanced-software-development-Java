package application;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


// this class represents the logic and presentation of the map.
public class ColorfulMapDisplayer extends Canvas{

	// ** Data members ** //
	private MainWindowController mwc;
	private Image plane; // plane icon
	private Image target; // x icon
	private Image path; // black circle icon
	private int pCol,pRow; // the plane icon character
	double[][] matrix;  // our csv value matrix 
	private int rowMatrix, columnMatrix; // of the matrix.
	private Point2D cordinates; // cordinates of the sim
	private double cordinateVal;
	private double minHeights, maxHeights, deltaHeight; // for calculation
	private Point targetPosition; // holds target for the x icon , we will send it to bfs algorithm
	Point planePosition; // holds plane icon position , we will send it to bfs algorithm
	DoubleProperty longitude, latitude; // sample plane members
	private double relativeW, relativeH;
	double targetClickedValX,targetClickedValY;
	private Point firstPlanePosition;
	private boolean firstTime = false;

	// ** Ctor ** // 
	
	public ColorfulMapDisplayer(){
		
		longitude = new SimpleDoubleProperty();
		latitude = new SimpleDoubleProperty();
		this.targetPosition = new Point(0, 0);
		this.planePosition = new Point(0, 0);
		this.firstPlanePosition = new Point(0,0);
		pCol=0; pRow=0; //init starting location
		setOnMouseClicked((e)->{ // puts the icon in the position the mouse clicked.
			redrawMap();
			setPlanePosition(planePosition.x, planePosition.y);
			targetClickedValX = e.getSceneX()-45; 
			targetClickedValY = e.getSceneY()-125;
			setTragetPosition(e.getSceneX()-45, e.getSceneY()-125);
			System.out.println(e.getSceneX()+"-"+e.getSceneY());}
		); 
		try {path = new Image(new FileInputStream("./resources/blackpointicon.png"));} // getting path icon
		catch (FileNotFoundException e) {e.printStackTrace();}
		try {target = new Image(new FileInputStream("./resources/X icon.png"));} // getting X icon
		catch (FileNotFoundException e) {e.printStackTrace();}
		try {plane = new Image(new FileInputStream("./resources/planeiconX.png"));} // getting plane icon
		catch (FileNotFoundException e) {e.printStackTrace();}
		}  
	
	void setMwc(MainWindowController mwc) {
		this.mwc=mwc;
	}
	
	// ** Methods ** //
	private int getpCol() {return pCol;}
	private void setpCol(int cCol) { this.pCol = cCol; }
	private int getpRow() { return pRow; }
	private void setpRow(int cRow) { this.pRow = cRow; }
	public double[][] getMatrix() { return matrix; }
	
	// getting position of the plane on the map
	Point getPlanePosition() {
		return planePosition;
	}
	//drawing plane due to init position
	void setPlanePosition(int x, int y){
		setpRow(x); setpCol(y);
		planePosition.setLocation(getpRow(), getpCol());
		GraphicsContext gc = getGraphicsContext2D();
		redrawMap();
		gc.drawImage(plane, y*relativeW,x*relativeH , 40, 40);	
	}
	// getting target position
	Point getTargetPosition() { return targetPosition; }
	
	//drawing X due to click position
	void setTragetPosition(double x, double y) {
		GraphicsContext gc = getGraphicsContext2D();
		gc.drawImage(target, x, y, 30, 30); 
		int rowT = Math.round((float) (rowMatrix * y / getHeight()));
		int columnT = Math.round((float) (columnMatrix * x / getWidth()));
		this.targetPosition.setLocation(rowT, columnT);
	}
	
	
	Point getFirstPosition() {
		return firstPlanePosition;
	}
	
	
	// creating point which will present the location of the map due to the sample we've done.
	void samplePointPosition() {
		double longi = ((longitude.doubleValue() - cordinates.getX())+ cordinateVal) / cordinateVal;
		double lati = (-(latitude.doubleValue() - cordinates.getY())+ cordinateVal) / cordinateVal;
		int row = Math.round((float)(rowMatrix * longi / getHeight()));
		int column = Math.round((float)(columnMatrix * lati / getWidth()));
		if (planePosition.x != row || planePosition.y != column) {
			if(!firstTime) {
				setPlanePosition(row, column);
				firstPlanePosition.setLocation(row, column);
			}
			else
				setPlanePosition(row*2, column*2);
			firstTime=true;
		}
	}
	
	
	
	
	// drawing map
	void redrawMap() {
		if(matrix!=null) {
			double W = getWidth(); // of the canvas, getWidth was inherited from canvas
			double H = getHeight(); // of the canvas
			double w = W / matrix[0].length; // each cell
			double h = H / matrix.length; // each cell
			GraphicsContext gc = getGraphicsContext2D(); // for printing things (ovals, lines, circles , etc..) on the canvas
			// on base of the matrix, we will draw rectangles due to relative width and height
			for(int i=0; i<matrix.length; i++) {
				for(int j=0; j<matrix[i].length; j++) { // as the number increases- the color gets more green (from red to green through yellow)
				     gc.setEffect(new Lighting());
					 gc.setFill(getColorDueMat(matrix[i][j]));
					 gc.fillRect(j*w, i*h, w, h);
				}
			}
//			if (mwc.isPathCalculated) {
//				solutionDrawer(mwc.path);
//				setTragetPosition(targetClickedValX, targetClickedValY);
//			}
		}
	}


	// filling up matrix due to csv file.
	void csvReader(File csvfile) {
		ArrayList<String[]> list = new ArrayList<>();
		Scanner s; // running along the CSV file
		try { s = new Scanner(csvfile).useDelimiter("\n");
			String[] temp = s.next().split(",");
			System.out.println(temp[0]+temp[1]);
			cordinates = new Point2D(Double.parseDouble(temp[0]), Double.parseDouble(temp[1]));
			cordinateVal = Double.parseDouble(s.next().split(",")[0]);
			while (s.hasNext()) {
				list.add(s.next().split(","));
			}
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		 rowMatrix = list.size();
		 columnMatrix = list.get(0).length;
		
		matrix = new double[rowMatrix][columnMatrix];
		for(int i=0; i<rowMatrix; i++) {
			for(int j=0; j<columnMatrix; j++) {
				matrix[i][j] = Double.parseDouble(list.get(i)[j]);
				if (matrix[i][j]<minHeights) {
					minHeights = matrix[i][j];
				}
				else if (matrix[i][j]>maxHeights) {
					maxHeights = matrix[i][j];
				}
			}
		}
			deltaHeight = maxHeights - minHeights;
			
			
			//some calculations
			double W = getWidth(); // of the canvas, getWidth was inherited from canvas
			double H = getHeight(); // of the canvas
			relativeW = W / matrix[0].length; // each cell
			relativeH = H / matrix.length; // each cell
	}
	
	// return map's colors due to the matrix values
	private Color getColorDueMat(double cellHeight) {
		Color c = Color.hsb(100*(cellHeight/deltaHeight), 1, 1);
		double red = c.getRed();
		double green = c.getGreen();
		double blue = c.getBlue();
		return new Color(red,green,blue, 1);
	}
	
	// drawing path's lines due to bfs algorithm
	void solutionDrawer(String pathVm) {
		double W = getWidth(); // of the canvas, getWidth was inherited from canvas
		double H = getHeight(); // of the canvas
		double w = W / matrix[0].length; // each cell
		double h = H / matrix.length; // each cell
		GraphicsContext gc = getGraphicsContext2D();
		String[] tmp = pathVm.split(",");
		int i=firstPlanePosition.x;
		int j=firstPlanePosition.y;
		for (String s : tmp) {
			switch (s) {
				case "Down":
					i++;
					gc.drawImage(path, getpCol() + j * w, getpRow() + i * h, 10, 10);
					break;
				case "Up":
					i--;
					gc.drawImage(path, getpCol() + j * w, getpRow() + i * h, 10, 10);
					break;
				case "Right":
					j++;
					gc.drawImage(path, getpCol() + j * w, getpRow() + i * h, 10, 10);
					break;
				case "Left":
					j--;
					gc.drawImage(path, getpCol() + j * w, getpRow() + i * h, 10, 10);
					break;
			}

			// redraw();
		}
	}

	public MainWindowController getMwc() {
		return mwc;
	}
}