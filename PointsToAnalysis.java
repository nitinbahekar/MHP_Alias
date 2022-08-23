package cs6235.a1.submission;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import cs6235.a1.AnalysisBase;
import cs6235.a1.Query;
import soot.Body;
import soot.Local;
import soot.RefLikeType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

public class PointsToAnalysis extends AnalysisBase {
	
	Set<String> abstractReferences = new HashSet();
	HashMap<SootMethod,HashMap<String,Set<String>>> rho = new HashMap();
	HashMap<SootMethod,HashMap<String,Set<String>>> sigma = new HashMap();
	
	
	@Override
	public String getResultString() {
		// TODO Auto-generated method stub
		String overall = "";
		for(Map.Entry<String, List<Query>>  entry: queriesMap.entrySet()) {
			String MN = entry.getKey();
			List<Query> l = queriesMap.get(MN);
			for(Query x:l) {
//				System.out.println(MN+" "+ x.toString());
				String lhs = x.getLhs();
				String rhs = x.getRhs();
				String arr[] = MN.split("\\.");
				
				SootClass currClass = Scene.v().getSootClass(arr[0]);
				SootMethod currMethod = currClass.getMethodByName(arr[1]);
				
				HashMap<String,Set<String>> tempMap = rho.getOrDefault(currMethod, new HashMap<String,Set<String>>());
				
				Set<String> left = tempMap.getOrDefault(lhs, new HashSet<String>());
				Set<String> right = tempMap.getOrDefault(rhs, new HashSet<String>());
				
				left.retainAll(right);
				if(left.size()==0) {
//					System.out.println("NO");
					overall += "NO\n";
				}
				else {
//					System.out.println("YES");
					overall += "YES\n";
				}
			}
			
		}
		return overall;
	}
	
	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		// TODO Auto-generated method stub
		
		
		
		Queue<SootMethod> WL = new LinkedList();
		SootClass mainClass = Scene.v().getMainClass();
		SootMethod mainMethod = mainClass.getMethodByName("main");
		
		WL.add(mainMethod);
		
		while(!WL.isEmpty()) {
			SootMethod currentMethod = WL.poll();
//			System.out.println(currentMethod.getActiveBody());
//			System.out.println("\n************************\n");
			for(Unit unit  : currentMethod.getActiveBody().getUnits()) {
				Stmt stmt = (Stmt) unit;
//				System.out.println(stmt);
				if(stmt instanceof DefinitionStmt ) {
					DefinitionStmt ds = (DefinitionStmt) stmt;
					Value rhs = ds.getRightOp();
					Value lhs = ds.getLeftOp();
					if(rhs instanceof  NewExpr) {
//						System.out.println(lhs+" "+rhs+" - This is newExpr statement");
						abstractReferences.add(lhs.toString()+currentMethod);
					
						
					}
					
					if(abstractReferences.contains(rhs.toString()+currentMethod)){
						
						HashMap<String,Set<String>> tempMap = rho.getOrDefault(currentMethod, new HashMap<String,Set<String>>());
						Set<String> tempSet = tempMap.getOrDefault(lhs, new HashSet<String>());
						tempSet.add(rhs.toString()+currentMethod);
						tempMap.put(lhs.toString(), tempSet);
						rho.put(currentMethod, tempMap);
					}
					if(lhs instanceof Local && rhs instanceof Local) {
//						System.out.println("lhs rhs local ahe -> "+lhs.toString() + rhs.toString());
						HashMap<String, Set<String>> locallocalmap = rho.getOrDefault(currentMethod, new HashMap<String,Set<String>>());
						Set<String> locallocalset1	= locallocalmap.getOrDefault(lhs.toString(), new HashSet<String>());
						Set<String> locallocalset2 = locallocalmap.getOrDefault(rhs.toString(), new HashSet<String>());
						locallocalset1.addAll(locallocalset2);
						locallocalmap.put(lhs.toString(), locallocalset1);
						rho.put(currentMethod, locallocalmap);
					}
				} 
				if (stmt instanceof InvokeStmt ) {
//					System.out.println("INVOKE STMT");
				}
				else if (stmt instanceof ReturnStmt) {
//					System.out.println("RETUERN STMT");
				}
				else if (stmt instanceof AssignStmt) {
//					System.out.println("ASSIGNMENT STMT");
				}
				else if(stmt instanceof IdentityStmt) {
//					System.out.println("IDENTITY STMT");
				}
				
			}
			HashMap<String,Set<String>> tempMap = rho.getOrDefault(currentMethod, new HashMap<String,Set<String>>());
			for(Map.Entry<String, Set<String>> entry: tempMap.entrySet()) {
//				System.out.println(entry.getKey()+" map mdhe ahe:"+entry.getValue());
			}
//			System.out.println(abstractReferences);
		}//WL ends
		
		
		
	}
	
	
}
