package cs6235.a2.submission;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import cs6235.a2.AnalysisBase;
import cs6235.a2.Query;
import polyglot.ast.Instanceof;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.spark.sets.DoublePointsToSet;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class MHPAnalysis extends AnalysisBase {
//	Map<String,String> answer = new HashMap<>();
	Set<Unit> inside = new HashSet<>();
	
	@Override
	public String getResultString() {
		String finalString = "";
		for(int i=0;i< queries.size();i++) {
			Query q = queries.get(i);
			String left = q.getLhs();
			String l="";
			for(int j=2;j<left.length();j++) {
				l+=left.charAt(j);
			}
//			String l = 
			String right = q.getRhs();
			String r="";
			for(int j=2;j<right.length();j++) {
				r+=right.charAt(j);
			}
//			System.out.println(l+" "+r);
			int lflag=0;
			int rflag=0;
			for(Unit u:inside) {
				if(u.toString().contains(l+"()")) {
					lflag=1;
				}
				if(u.toString().contains(r+"()")) {
					rflag=1;
				}
			}
			if(lflag ==1 && rflag==1) {
				finalString+="NO\n";
			}
			else {
				finalString+="YES\n";
			}
		}
//		System.out.println(inside);
		return finalString;
		
	}//getResultStringEnds

	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		
		SootClass threadClass = Scene.v().getSootClass("java.lang.Thread");
		List<SootClass> threadClasses = Scene.v().getActiveHierarchy().getSubclassesOf(threadClass);
		List<SootClass> filteredClasses = new LinkedList<SootClass>(threadClasses);
		filteredClasses.removeIf(c -> c.isLibraryClass());
		
		SootMethod mainMethod = Scene.v().getMainMethod();
		PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
		

		UnitGraph cfg = new BriefUnitGraph(mainMethod.getActiveBody());
		
		List<Unit> heads = cfg.getHeads();
		Unit head = heads.get(0);
		Stack<Unit> stack = new Stack<Unit>();
		stack.add(head);
		int monitor=0;
		HashSet<Unit> visited = new HashSet<>();
		while(!stack.isEmpty()) {
			Unit unitToProcess = stack.pop();
			
			if(unitToProcess.toString().contains("entermonitor")) {
//				System.out.println("SC:"+sc+" unit->"+unitToProcess);
				monitor=1;

			}
			else if(unitToProcess.toString().contains("exitmonitor")){
				monitor=0;
			}
			else {
				if((Stmt)unitToProcess instanceof InvokeStmt ) {
					if(monitor==1) {
						inside.add(unitToProcess);
					}
				}
				
				
			}
			List<Unit> successors = cfg.getSuccsOf(unitToProcess);
			
			if(!visited.contains(unitToProcess)) {
				stack.addAll(successors);
			}
				
			visited.add(unitToProcess);
		}
		
		for(SootClass sc : filteredClasses) {
			monitor=0;
			SootMethod run = Scene.v().getSootClass(sc.toString()).getMethodByName("run");
			UnitGraph cfg2 = new BriefUnitGraph(run.getActiveBody());
			
			List<Unit> heads2 = cfg2.getHeads();
			Unit head2 = heads2.get(0);
			Stack<Unit> stack2 = new Stack<Unit>();
			stack2.add(head2);
			HashSet<Unit> visited2 = new HashSet<>();
			
			while(!stack2.isEmpty()) {
				Unit unitToProcess = stack2.pop();
				
				if(unitToProcess.toString().contains("entermonitor")) {
//					System.out.println("SC:"+sc+" unit->"+unitToProcess);
					monitor=1;

				}
				else if(unitToProcess.toString().contains("exitmonitor")){
					monitor=0;
				}
				else {
					if((Stmt)unitToProcess instanceof InvokeStmt ) {
						if(monitor==1) {
							inside.add(unitToProcess);
						}
					}
					
					
				}
				List<Unit> successors = cfg2.getSuccsOf(unitToProcess);
				
				if(!visited2.contains(unitToProcess)) {
					stack2.addAll(successors);
				}
					
				visited2.add(unitToProcess);
			}
		}
		
		
	}//InternalTransformEnds

}//MHPAnalysisEnds
