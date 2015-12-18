//package csu.agent.at.rescueTools;
//
//
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.List;
//
//import rescuecore2.standard.entities.Area;
//import rescuecore2.worldmodel.EntityID;
//import Jama.LUDecomposition;
//import Jama.Matrix;
//import csu.model.AdvancedWorldModel;
//import csu.standard.Ruler;
//
///**
// * This class is used to get the estimated real distance from one area to another area
// * by the Euclidean distance between them.
// * <p>
// * Assume :
// * <LI> RD to describe the real distance (in 0.1m)
// * <LI> ED to describe the Euclidean distance (in 0.1m)
// * <p>
// * Contribute:<p>
// * 		RD = p[0] + p[1] * ED + p[2] * ED * ED   (not used) <p>
// * 		RD = p[0] + p[1] * ED
// * <P>
// * We contribute this quadratic polynomials to fitting the relation from ED to RD , 
// * and calculate the parameters a[] using Least Square Method.
// * 
// * @author Nale
// * Jun 29, 2014
// */
//public class DistanceCurveFitter {
//
//	/** The parameters of the curve*/
//	private double p[][] ; 
//	
//	/** The number of groups of data*/
//	private int sigma ;
//	
//	/** The accumulation of ED */
//	private double sigmaE ;
//	
//	/** The accumulation of ED*ED */
//	private double sigmaEE;
//	
////	/** The accumulation of ED*ED*ED */
////	private double sigmaEEE;
////	
////	/** The accumulation of ED*ED*ED*ED */
////	private double sigmaEEEE;
//	
//	/** The accumulation of RD*/
//	private double sigmaR;
//	
//	/** The accumulation of ED*RD */
//	private double sigmaER;
//	
////	/** The accumulation of ED*ED*RD */
////	private double sigmaEER;
//	
//	/** True if the parameters of the The parameters of the curve*/
//	private boolean Flag_Solved;
//	
//	/** the world model*/
//	private AdvancedWorldModel world;
//	
//	public DistanceCurveFitter(AdvancedWorldModel world){
//		this.world = world;		
//		sigma = 0;
//		sigmaE = 0.0;
//		sigmaEE = 0.0;
//		sigmaR = 0.0;
//		sigmaER = 0.0;
//		Flag_Solved = false;
//	}
//	
//	public void update(List<EntityID> path){
//		EntityID fromID = path.get(0);
//		EntityID toID = path.get(path.size() - 1);
//		Area from = (Area)world.getEntity(fromID);
//		Area to = (Area)world.getEntity(toID);
//		double directDistance = Ruler.getDistance(from, to) / 100;
//		double realDistance = 0.0;
//		for (int i = 0 ; i < path.size() - 1 ; i++){
//			EntityID currentID = path.get(i);
//			EntityID nextID = path.get(i+1);
//			Area currentPos = (Area)world.getEntity(currentID);
//			Area nextPos = (Area)world.getEntity(nextID);
//			realDistance += Ruler.getDistance(currentPos, nextPos);
//		}
//		realDistance = realDistance / 100;
//		recalculation(directDistance , realDistance);
//		printForTest((int)directDistance , (int)realDistance);
//	}
//	
//	public void recalculation(double ED , double RD){
//		sigma ++;
//		sigmaE += ED;
//		sigmaEE += ED * ED;
//		sigmaR += RD;
//		sigmaER += ED * RD;
//		System.out.println(world.me + "  Time : " + world.getTime() + "   print#1#" + sigmaEE);
//		double[][] a = {{sigma  ,  sigmaE},
//				
//						{sigmaE ,  sigmaEE}};
//		double[][] b = {{sigmaR} , {sigmaER}}; 
//		Matrix A = new Matrix(a);
//		Matrix B = new Matrix(b);
//		LUDecomposition LUD_A = new LUDecomposition(A);
//		if (!LUD_A.isNonsingular())
//			return ;
//		Flag_Solved = true ;
//		Matrix P = A.solve(B);
//		p = P.getArray();
//		System.out.println(world.me + "   Time : " + world.getTime() + "   print#2#   " +  p[0][0] + "   " + p[1][0]);
//	}
//	
//	public void printForTest(int ED , int RD){
//		String filename = "pathData";
//		File file = new File(filename);
//		try {
//			if (!file.exists())
//				file.createNewFile();
//			FileWriter fw = new FileWriter(filename , true);
//			fw.append(String.valueOf(ED) + " " + String.valueOf(RD) + "\n");
//			fw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public double getRealDistance(double ED){
//		if (sigma < 50)
//			return ED;
//		if (!Flag_Solved)
//			return ED;
//		if (ED <= 100)
//			return ED;
//		double RD = p[0][0] + p[1][0] * (ED / 100);
//		System.out.println(world.me + "  Time : " + world.getTime() + "   print#3#"  + "   ED  :  " + (ED / 100) + "   RD : " + RD * 100);
//		return RD * 100;
//	}
//}
