package be.ac.ucl.info.bdcube;

//RubiksCube.java, created Jan 29, 2003 9:50:57 PM by jwhaley
//Copyright (C) 2003 John Whaley
//Licensed under the terms of the GNU LGPL; see COPYING for details.

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;

/**
 * RubiksCube
 * 
 * @author jwhaley
 */
public class Main2 {

	static BDDFactory bdd;
	static int n = 2; // 3x3 cube
	static int k = 3; // number of moves

	public static void main(String[] args) {

		bdd = BDDFactory.init(100000000, 30000000);
		bdd.setMaxIncrease(250000);

		if (args.length > 0) {
			k = Integer.parseInt(args[0]);
		}
		System.out.println(System.getProperties().getProperty(
				"java.library.path"));
		// 6n^2 domains, one for each square.
		int[] sizes = new int[n * n * 6 * 2];
		// 6 possible colors for each domain.
		Arrays.fill(sizes, 6);

		// BDDDomain[] squares =
		bdd.extDomain(sizes);
		//bdd.autoReorder(BDDFactory.REORDER_SIFTITE);

		List<BDDPairing> perms = allPerms();

		
		// Build cube to solve.
		BDD explored = buildInitial();
		BDD prev = bdd.one();
		
		int[] p1 = {  6,  7,  0,  1,  2,  3,  4,  5, 11,  8,  9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 }; // up face, clockwise.
		int[] p3 = {  0, 10,  3, 15, 22,  5,  6,  7,  8,  4, 16, 11, 12,  9,  2, 14, 21, 17, 18, 19, 20,  1, 13, 23 }; // right face 
		int[] p5 = {  0,  1,  2, 11,  5, 17, 23,  7,  8,  9,  6, 18, 12, 13, 14, 10,  4, 16, 22, 19, 20, 21,  3, 15 }; // front face.
		
		BDD trans = bdd.one();
		for( int i=0; i<p1.length; ++i){
			BDD eq = bdd.getDomain(i).buildEquals(bdd.getDomain(p1[i] + p1.length));
			trans.andWith(eq);
			System.out.println(i);
		}
		
		
		/*
		int j =0;
		while( !prev.equals(explored) ){
			prev.free();
			prev = explored.id();
			addAll(1,perms, explored, prev);
			j++;
			System.out.println(">>> " + explored.nodeCount() + " " + explored.allsat().size());
		}
		
		System.out.println(">>> " + j);
		/*
		int pp[] = { 2, 5, 9, 9 };
		//System.out.println(pp.toString());
		BDD start = cube.id();
		for (int i : pp) {
			printCube(start);
			start.replaceWith(perms.get(i));
		}
		// print cube to solve.
		printCube(start);

		// Search (blind shortest path search)
		BDD frontier = start.id();
		LinkedList<BDD> steps = new LinkedList<>();
		steps.push(start);
		int i = 0;
		while (!cube.imp(frontier).isOne()) {
			addAll(1, perms, frontier, frontier.id());
			System.out.println(i);
			i++;
			steps.push(frontier.id());
		}

		// Extract solution
		LinkedList<BDD> reverse = new LinkedList<>();
		reverse.push(cube);
		// printCube(cube);
		steps.pop();
		for (int j = i; j > 0; --j) {
			BDD prev = reverse.peek();
			BDD next = steps.pop();
			// addAll(1, perms, prev, prev.id());
			// this is equivalent, but allows (+/-) to retrieve a permutation
			// that works (p_i).
			int p_i = 0;
			for (BDDPairing p : perms) {
				BDD r = prev.id().replaceWith(p).andWith(next.id());
				if (!r.isZero()) {
					BDD sol = r.satOne();
					System.out.println(p_i);
					// printCube(sol);
					reverse.push(sol);

					break;
				}
				p_i++;
				r.free();
			}
			next.free();
		}

		// for( BDD b : reverse) printCube(b);

		System.out.println("Number of moves to solve : " + i);
		//*/
	}

	static void addAll(int depth, List<BDDPairing> perms, BDD allConfigs, BDD c) {
		if (depth <= 0)
			return;
		for (BDDPairing p : perms) {
			BDD c2 = c.replace(p);
			BDD r = c2.imp(allConfigs);
			if (!r.isOne()) {
				// printCube(c2);
				allConfigs.orWith(c2.id());
				addAll(depth - 1, perms, allConfigs, c2);
			}
			r.free();
			c2.free();
		}
	}

	static BDD buildInitial() {
		
		int[] initial = { 1, 1, 2, 2, 3, 3, 4, 4, 0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 5, 5 };
		BDD b = bdd.one();
		for (int k = 0; k < initial.length; ++k) {
			b.andWith(bdd.getDomain(k).ithVar(initial[k]));
		}
		return b;
	}

	static void printCube(BDD b) {
		// 
		//       12 13                                right
		//        0  1      
		// 19  7  8  9  2 14                 left  |    up   |  right 
		// 18  6 11 10  3 15
		//        5  4                                front
		//       17 16
		//                  
		//       20 21                              <-mottob<-
		//       23 22
		//
		System.out.println(b.toStringWithDomains());
		indent();		  ps();p(b, 12);p(b, 13);						newLine();
		indent();		  ps();p(b,  0);p(b,  1);						newLine();
		p(b, 19);p(b,  7);ps();p(b,  8);p(b,  9);ps();p(b,  2);p(b, 14);newLine();
		p(b, 18);p(b,  6);ps();p(b, 11);p(b, 10);ps();p(b,  3);p(b, 15);newLine();
		indent();		  ps();p(b,  5);p(b,  4);						newLine();
		indent();		  ps();p(b, 17);p(b, 16);						newLine();
																		newLine();
		indent();		  ps();p(b, 20);p(b, 21);						newLine();
		indent();		  ps();p(b, 23);p(b, 22);						newLine();
	}

	static void ps() {
		System.out.print(' ');
	}

	static void indent() {
		for (int i = 0; i < n; ++i) {
			System.out.print("   ");
		}
	}

	static void newLine() {
		System.out.println();
	}

	static void p(BDD b, int d) {
		BDDDomain dom = bdd.getDomain(d);
		int v = b.scanVar(dom).intValue();
		String s = Integer.toString(v);
		s = "   ".substring(s.length()) + s;
		System.out.print(s);
	}

	static void checkPerm(int[] perm) {
		int[] p2 = new int[perm.length];
		System.arraycopy(perm, 0, p2, 0, p2.length);
		Arrays.sort(p2);
		for (int i = 0; i < p2.length; ++i) {
			if (p2[i] != i)
				throw new InternalError(i + " != " + p2[i]);
		}
	}

	static void dumpPerm(int[] perm) {
		System.out.println("Permutation:");
		for (int i = 0; i < perm.length; ++i) {
			System.out.println(i + " -> " + perm[i]);
		}
		System.out.println();
	}

	static void buildPerm(Collection<BDDPairing> perms, int[] perm) {
		// dumpPerm(perm);
		checkPerm(perm);

		BDDDomain[] dorig = new BDDDomain[perm.length];
		for (int i = 0; i < dorig.length; ++i) {
			dorig[i] = bdd.getDomain(i);
		}
		BDDDomain[] dperm = new BDDDomain[perm.length];
		for (int i = 0; i < perm.length; ++i) {
			dperm[i] = bdd.getDomain(/*perm.length + */perm[i]);
		}
		BDDPairing pair = bdd.makePair();
		pair.set(dorig, dperm);
		perms.add(pair);
		pair = bdd.makePair();
		pair.set(dperm, dorig);
		perms.add(pair);
	}

	static ArrayList<BDDPairing> allPerms() {

		/*
		 * int[] px = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
		 * 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
		 * 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
		 * 50, 51, 52, 53 };
		 */

		// 
		//       12 13                                right
		//        0  1      
		// 19  7  8  9  2 14                 left  |    up   |  right 
		// 18  6 11 10  3 15
		//        5  4                                front
		//       17 16
		//                  
		//       20 21                              <-mottob<-
		//       23 22
		//
		//int[] px = {  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 };

		ArrayList<BDDPairing> perms = new ArrayList<BDDPairing>(12);
		
		int[] p1 = {  6,  7,  0,  1,  2,  3,  4,  5, 11,  8,  9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 }; // up face, clockwise.
		int[] p2 = {  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 18, 19, 12, 13, 14, 15, 16, 17, 23, 20, 21, 22 }; // bottom face, clockwise.
		int[] p3 = {  0, 10,  3, 15, 22,  5,  6,  7,  8,  4, 16, 11, 12,  9,  2, 14, 21, 17, 18, 19, 20,  1, 13, 23 }; // right face 
		int[] p4 = { 11,  1,  2,  3,  4, 23, 18,  6,  5,  9, 10, 17,  8, 13, 14, 15, 16, 20, 19,  7, 0, 21, 22, 12 }; // left face
		int[] p5 = {  0,  1,  2, 11,  5, 17, 23,  7,  8,  9,  6, 18, 12, 13, 14, 10,  4, 16, 22, 19, 20, 21,  3, 15 }; // front face.
		int[] p6 = {  1, 13, 21,  3,  4,  5,  6,  9,  2, 14, 10, 11,  0, 12, 20, 15, 16, 17, 18,  8,  7, 19, 22, 23 }; // rear face.
		
		buildPerm(perms, p1);
		//buildPerm(perms, p2);
		buildPerm(perms, p3);
		//buildPerm(perms, p4);
		buildPerm(perms, p5);
		//buildPerm(perms, p6);

		return perms;
	}

}
