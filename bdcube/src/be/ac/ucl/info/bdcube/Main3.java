package be.ac.ucl.info.bdcube;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.BuDDyFactory;
import net.sf.javabdd.CUDDFactory;


public class Main3 {
	static BDDFactory bdd;
	static int n = 2; // 3x3 cube
	static int k = 3; // number of moves
	static BDD init;
	static BDD trans;
	static BDD var_x, var_prim;
	static BDD hard;
	static BDDPairing prim2x, x2prim;
	static List<Pair<String, BDD>> moves;
	
	static {
		bdd = BuDDyFactory.init(80000000, 2000000);
		bdd.setMaxIncrease(    5000000);

		// 6n^2 domains, one for each square.
		int[] sizes = new int[7 * 2 * 2];
		// 6 possible colors for each domain.
		Arrays.fill(sizes, 7);
		Arrays.fill(sizes, 7, 14, 3);
		Arrays.fill(sizes, 21, 28, 3);

		bdd.extDomain(sizes);
		System.out.println(bdd.varNum());
		
		hard = bdd.zero();
		try {
			hard = bdd.load("hard_2x2_cube.sav");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int[] initial = {0, 1, 2, 3, 4, 5, 6, 0, 0, 0, 0, 0, 0, 0};
		
		init = bdd.one();
		for (int k = 0; k < initial.length; ++k) {
			init.andWith(bdd.getDomain(k).ithVar((k < 7)? k : 1));
		}
		
		var_x = bdd.getDomain(0).set();
		for (int i = 0; i < initial.length; i++) {
			var_x.andWith(bdd.getDomain(i).set());
		}
		var_prim = bdd.getDomain(initial.length).set();
		for (int i =0; i < initial.length; i++) {
			var_prim.andWith(bdd.getDomain(initial.length + i).set());
		}
		
		build_trans();

		BDDDomain[] dorig = new BDDDomain[14];
	    for (int i = 0; i < dorig.length; ++i) {
	       dorig[i] = bdd.getDomain(i);
	    }
	    BDDDomain[] dperm = new BDDDomain[14];
	    for (int i=0; i<dperm.length; ++i) {
	        dperm[i] = bdd.getDomain(dperm.length + i);
	    }
		prim2x = bdd.makePair();
		prim2x.set(dperm, dorig);
		x2prim = bdd.makePair();
		x2prim.set(dorig, dperm);
	}
	
	public static void main(String[] args) {
		
		// Print initial cube
		//System.out.println("This is the initial cube.");
		//printCube(init);
		
		// Print all accessible states in one step.
		//System.out.println("The six following states are accessible with exactly one move.");
		//print_all(trans.relprod(init, vars).replace(swap));
		
		BDD prems = bdd.one();
		int[] initial = {1, 0, 2, 3, 4, 5, 6, 2, 0, 1, 1, 1, 1, 1};
		for (int k = 0; k < initial.length; ++k) {
			prems.andWith(bdd.getDomain(k).ithVar(initial[k]));
		}
		print_all(prems);
		
		BDD prev, next; // old, new ne marche pas, new étant réservé ;-)
		int j=0;
		Stack<BDD> stack = new Stack<>();
		prev= bdd.zero();
		next = prems.id();
		while( !init.imp(next).isOne() ){
			prev = next.id();
			stack.add(prev);
			next = next.orWith(trans.relprod(next, var_x).replace(prim2x));
			j++;
			System.out.println(">>> " + j + " " + (int)next.satCount(var_x) + " " + next.nodeCount());
		}
		System.out.println("God number is : " + j);

		prev = init.id();
		
		List<Pair<String, BDD>> states = new ArrayList<Pair<String, BDD>>();
		List<Pair<String, BDD>>	new_states = new ArrayList<Pair<String, BDD>>();
		states.add(new Pair<String, BDD>("", init.id()));
		for(; j > 0; j--){
			next = stack.pop();
			
			for( Pair<String, BDD> state : states){
				for( Pair<String, BDD> move : moves){
					BDD suite = move.r.relprod(state.r.replace(x2prim), var_prim).and(next);
					if(!suite.isZero()){
						new_states.add(new Pair<String, BDD>(state.l + move.l, suite));
					}
				}
			}
			states = new_states;
			new_states = new ArrayList<Pair<String, BDD>>();
			//prev.satOne(var_x, true).printSetWithDomains();
			//prev = trans.relprod(prev.replace(x2prim), var_prim).andWith(next);
			System.out.println(states.size());
		}
		Set<String> set = new HashSet<>();
		for( Pair<String, BDD> state : states){
			set.add(state.l);
		}
		for( String s : set){
			System.out.println(s);
		}
		
	}
	
	public static void bruteforce() {
		/*
		 * This block iterates over all the possible states.
		 * Can take some time.
		 */
		
		BDD prev, next; // old, new ne marche pas, new étant réservé ;-)
		int j=0;
		Stack<BDD> stack = new Stack<>();
		prev= bdd.zero();init.id();
		next = init.id();
		while( !prev.biimp(next).isOne() ){
			prev = next.id();
			stack.add(prev);
			next = next.orWith(trans.relprod(next, var_x).replace(prim2x));
			j++;
			System.out.println(">>> " + j + " " + (int)next.satCount(var_x) + " " + next.nodeCount());
		}
		System.out.println("God number is : " + (j-1));
		
		//hard = stack.pop().andWith(stack.pop().not()).satOne();
		
		//Here is one of the hard cubes, that require 14 moves to solve.
		//print_all(hard.satOne());

	}
	
	private static void print_all(BDD c) {
		BDD cube, configs = c.id();
		System.out.println("---------------------------------------");
		while( !(cube = configs.satOne()).isZero() ){
			printCube(cube);
			//cube.printSetWithDomains();
			configs.andWith(cube.not());
		}
	}
	
	public static void build_trans() {
		int[] p1r =     {1, 2, 3, 0, 4, 5, 6, 1,-1, 1,-1, 0, 0, 0};
		int[] p1 =      {3, 0, 1, 2, 4, 5, 6, 1,-1, 1,-1, 0, 0, 0};
		int[] p2r =     {0, 2, 5, 3, 4, 6, 1, 0, 1,-1, 0, 0, 1,-1};
		int[] p2 =      {0, 6, 1, 3, 4, 2, 5, 0, 1,-1, 0, 0, 1,-1};
		int[] p3r =     {0, 1, 3, 4, 5, 2, 6, 0, 0, 0, 0, 0, 0, 0};
		int[] p3 =      {0, 1, 5, 2, 3, 4, 6, 0, 0, 0, 0, 0, 0, 0};
		
		BDD perm;
		trans = bdd.zero();
		moves = new ArrayList<>(6);
		
		perm = bdd.zero();
		build_perm(perm, p1);
		moves.add(new Pair<String, BDD>("F", perm));
		perm = bdd.zero();
		build_perm(perm, p1r);
		moves.add(new Pair<String, BDD>("F'", perm));
		perm = bdd.zero();
		build_perm(perm, p2);
		moves.add(new Pair<String, BDD>("R'", perm));
		perm = bdd.zero();
		build_perm(perm, p2r);
		moves.add(new Pair<String, BDD>("R", perm));
		perm = bdd.zero();
		build_perm(perm, p3r);
		moves.add(new Pair<String, BDD>("B", perm));
		perm = bdd.zero();
		build_perm(perm, p3);
		moves.add(new Pair<String, BDD>("B'", perm));
		
		for( Pair<String, BDD> p : moves){
			trans.orWith(p.r.id());
		}
		
	}

	private static void build_perm(BDD trans, int[] p1) {
		BDD temp = bdd.one();
		for (int i = 0; i < p1.length; i++) {
			BDD step;
			if( i < 7 ){
				step = bdd.getDomain(i).buildEquals(bdd.getDomain(p1.length + p1[i]));
			}else{
				BDD a = bdd.getDomain(i).buildAdd(bdd.getDomain(p1.length + p1[i-7] +7),p1[i]);
				BDD n = bdd.getDomain(p1.length + p1[i-7] +7).domain();
				if ( p1[i] != 0 ){
					BDD b;
					BDDDomain dperm = bdd.getDomain(p1.length + p1[i-7] +7);
					if(p1[i] == 1){
						b = bdd.getDomain(i).buildAdd(dperm,p1[i]-3).andWith(dperm.ithVar(0));
					} else {
						b = bdd.getDomain(i).buildAdd(dperm,p1[i]+3).andWith(dperm.ithVar(2));
					}
					a.orWith(b);
				}
				step = a.andWith(n);
			}
			temp.andWith(step);
		}
		trans.orWith(temp);
	}
	
	static void printCube(BDD b) {
		// 
		//       12 13                                jaune
		//        0  1      
		// 19  7  8  9  2 14                 vert  |  orange  |  bleu
		// 18  6 11 10  3 15
		//        5  4                                blanc
		//       17 16
		//                  
		//       20 21                                rouge
		//       23 22
		//
		b.printSetWithDomains();
		indent();		  ps();p(b,4,1);p(b,5,2);						newLine();
		indent();		  ps();p(b,7,2);p(b,6,1);						newLine();
		p(b,4,2);p(b,7,1);ps();p(b,7,0);p(b,6,0);ps();p(b,6,2);p(b,5,1);newLine();
		p(b,3,1);p(b,0,2);ps();p(b,0,0);p(b,1,0);ps();p(b,1,1);p(b,2,2);newLine();
		indent();		  ps();p(b,0,1);p(b,1,2);						newLine();
		indent();		  ps();p(b,3,2);p(b,2,1);						newLine();
																		newLine();
		indent();		  ps();p(b,4,0);p(b,5,0);						newLine();
		indent();		  ps();p(b,3,0);p(b,2,0);						newLine();
	}

	static void ps() {
		System.out.print(' ');
	}

	static void indent() {
		for (int i = 0; i < n; ++i) {
			System.out.print("  ");
		}
	}

	static void newLine() {
		System.out.println();
	}

	private static final int ORANGE=0, GREEN=1, WHITE=2, BLUE=3, YELLOW=4, RED = 5;
	private static final int[][] cubies = {
	//private static final int ORANGE='o', GREEN='g', WHITE='w', BLUE='b', YELLOW='y', RED='r';
	//private static final char[][] cubies = {
		{ORANGE, WHITE, GREEN},
		{ORANGE, BLUE, WHITE},
		{RED, WHITE, BLUE},
		{RED, GREEN, WHITE},
		{RED, YELLOW, GREEN},
		{RED, BLUE, YELLOW},
		{ORANGE, YELLOW, BLUE},
		{ORANGE, GREEN, YELLOW}
	};
	
	static void p(BDD b, int cubie, int rot) {
		
		BDDDomain dom;
		
		dom = bdd.getDomain(cubie);
		int v = (cubie == 7) ? 7: b.scanVar(dom).intValue();
		
		
		dom = bdd.getDomain(v+7);
		int r = (cubie == 7) ? 1: b.scanVar(dom).intValue();
		
		String s = "" + cubies[v][(rot + r + 6 -1) % 3];
		s = "  ".substring(s.length()) + s;
		System.out.print(s);
	}
	
	public static class Pair<L,R> {
	    private L l;
	    private R r;
	    public Pair(L l, R r){
	        this.l = l;
	        this.r = r;
	    }
	    public L getL(){ return l; }
	    public R getR(){ return r; }
	    public void setL(L l){ this.l = l; }
	    public void setR(R r){ this.r = r; }
	}
}
