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
public class Main {

 static BDDFactory bdd;
 static int n = 3; // 3x3 cube
 static int k = 3; // number of moves

 public static void main(String[] args) {
	 //System.loadLibrary("buddy");
	 //System.loadLibrary("cal");
	 //System.loadLibrary("cudd");
	 
     bdd = BDDFactory.init(10000000, 1000000);
     bdd.setMaxIncrease(250000);
     
     if (args.length > 0) {
         k = Integer.parseInt(args[0]);
     }
     System.out.println(System.getProperties().getProperty("java.library.path"));
     // 6n^2 domains, one for each square.
     int[] sizes = new int[n * n * 6];
     // 6 possible colors for each domain.
     Arrays.fill(sizes, 6);
     
     //BDDDomain[] squares = 
     bdd.extDomain(sizes);
     
     List<BDDPairing> perms = allPerms();
     
     // Build cube to solve.
     BDD cube = buildInitial();
     int pp[] = {2, 5, 9, 9};
     System.out.println(pp);
     BDD start = cube.id();
     for( int i : pp ){
    	 start.replaceWith(perms.get(i));
     }
     // print cube to solve.
     printCube(start);
     
     
     
     // Search (blind shortest path search)
     BDD frontier = start.id();
     LinkedList<BDD> steps = new LinkedList<>();
     steps.push(start);
     int i = 0;
     while ( !cube.imp(frontier).isOne() ){
    	 addAll(1, perms, frontier, frontier.id());
    	 System.out.println(i);
    	 i++;
    	 steps.push(frontier.id());
     }
     
     //Extract solution
     LinkedList<BDD> reverse = new LinkedList<>();
     reverse.push(cube);
     //printCube(cube);
     steps.pop();
     for( int j = i; j > 0; --j) {
    	 BDD prev = reverse.peek();
    	 BDD next = steps.pop();
    	 //addAll(1, perms, prev, prev.id());
    	 // this is equivalent, but allows (+/-) to retrieve a permutation that works (p_i).
    	 int p_i = 0;
    	 for( BDDPairing p : perms ){
    		 BDD r = prev.id().replaceWith(p).andWith(next.id());
    		 if( !r.isZero() ){
    			 BDD sol = r.satOne();
    	    	 System.out.println(p_i);
    	    	 //printCube(sol);
    	    	 reverse.push(sol);
    	    	 
    	    	 break;
    		 }
    		 p_i++;
    		 r.free();
    	 }
    	 next.free();
     }
     
     //for( BDD b : reverse) printCube(b);
     
     System.out.println("Number of moves to solve : " + i);
 }


 
 static void addAll(int depth, List<BDDPairing> perms, BDD allConfigs, BDD c) {
     if (depth <= 0) return;
     for (BDDPairing p : perms ) {
         BDD c2 = c.replace(p);
         BDD r = c2.imp(allConfigs);
         if (!r.isOne()) {
             //printCube(c2);
             allConfigs.orWith(c2.id());
             addAll(depth-1, perms, allConfigs, c2);
         }
         r.free();
         c2.free();
     }
 }

 static BDD buildInitial() {
     BDD b = bdd.one();
     for (int k=0; k<4; ++k) {
         for (int i=0; i<n; ++i) {
             b.andWith(bdd.getDomain(k*n + i).ithVar(k));
             b.andWith(bdd.getDomain((8+k)*n - 4 + i).ithVar(k));
             b.andWith(bdd.getDomain((16+k)*n - 8 + i).ithVar(k));
         }
     }
     for (int i=0; i<8; ++i) {
         b.andWith(bdd.getDomain(4*n+i).ithVar(4));
         b.andWith(bdd.getDomain(12*n-4+i).ithVar(5));
     }
     b.andWith(bdd.getDomain(n*n*6-2).ithVar(4));
     b.andWith(bdd.getDomain(n*n*6-1).ithVar(5));
     return b;
 }

     //          20 21 22
     //          40 41 42
     //           0  1  2
     //31 51 11  12 13 14  3 43 23
     //30 50 10  19 52 15  4 44 24
     //29 49  9  18 17 16  5 45 25
     //           8  7  6
     //          48 47 46
     //          28 27 26
     //
     //          32 33 34
     //          39 53 35
     //          38 37 36
 static void printCube(BDD b) {
     System.out.println(b.toStringWithDomains());
     indent(); ps(); p(b, 20); p(b, 21); p(b, 22); newLine();
     indent(); ps(); p(b, 40); p(b, 41); p(b, 42); newLine();
     indent(); ps(); p(b,  0); p(b,  1); p(b,  2); newLine();
     p(b, 31); p(b, 51); p(b, 11); ps(); p(b, 12); p(b, 13); p(b, 14); ps(); p(b, 3); p(b, 43); p(b, 23); newLine();
     p(b, 30); p(b, 50); p(b, 10); ps(); p(b, 19); p(b, 52); p(b, 15); ps(); p(b, 4); p(b, 44); p(b, 24); newLine();
     p(b, 29); p(b, 49); p(b,  9); ps(); p(b, 18); p(b, 17); p(b, 16); ps(); p(b, 5); p(b, 45); p(b, 25); newLine();
     indent(); ps(); p(b,  8); p(b,  7); p(b,  6); newLine();
     indent(); ps(); p(b, 48); p(b, 47); p(b, 46); newLine();
     indent(); ps(); p(b, 28); p(b, 27); p(b, 26); newLine();
     newLine();
     indent(); ps(); p(b, 32); p(b, 33); p(b, 34); newLine();
     indent(); ps(); p(b, 39); p(b, 53); p(b, 35); newLine();
     indent(); ps(); p(b, 38); p(b, 37); p(b, 36); newLine();
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
     s = "   ".substring(s.length())+s;
     System.out.print(s);
 }

 static void checkPerm(int[] perm) {
     int[] p2 = new int[perm.length];
     System.arraycopy(perm, 0, p2, 0, p2.length);
     Arrays.sort(p2);
     for (int i=0; i<p2.length; ++i) {
         if (p2[i] != i)
             throw new InternalError(i+" != "+p2[i]);
     }
 }

 static void dumpPerm(int[] perm) {
     System.out.println("Permutation:");
     for (int i=0; i<perm.length; ++i) {
         System.out.println(i+" -> "+perm[i]);
     }
     System.out.println();
 }

 static void buildPerm(Collection<BDDPairing> perms, int[] perm) {
     //dumpPerm(perm);
     checkPerm(perm);

     BDDDomain[] dorig = new BDDDomain[perm.length];
     for (int i = 0; i < dorig.length; ++i) {
         dorig[i] = bdd.getDomain(i);
     }
     BDDDomain[] dperm = new BDDDomain[perm.length];
     for (int i=0; i<perm.length; ++i) {
         dperm[i] = bdd.getDomain(perm[i]);
     }
     BDDPairing pair = bdd.makePair();
     pair.set(dorig, dperm);
     perms.add(pair);
     pair = bdd.makePair();
     pair.set(dperm, dorig);
     perms.add(pair);
 }
 static ArrayList<BDDPairing> allPerms(){
	 
	 /*
	 int[] px = { 0,  1,  2,  3,  4,  5,  6,  7,  8,  9,
             10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
             20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
             30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
             40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
             50, 51, 52, 53 };
	 */
	 
	 ArrayList<BDDPairing> perms = new ArrayList<BDDPairing>(12);
     //          20 21 22
     //          40 41 42
     //           0  1  2
     //31 51 11  12 13 14  3 43 23
     //30 50 10  19 52 15  4 44 24
     //29 49  9  18 17 16  5 45 25
     //           8  7  6
     //          48 47 46
     //          28 27 26
     //
     //          32 33 34
     //          39 53 35
     //          38 37 36

     int i;
     
     // rotate top, torque up
     int[] p1 = new int[54];
     for (i = 0; i < 4 * n; ++i) {
         int k = (i + n) % (4 * n);
         p1[i] = k;
     }
     for (int j = 0 ; j < 4 * n - 4; ++i, ++j) {
         int k = 4 * n + ((j + n - 1) % (4 * n - 4));
         p1[i] = k;
     }
     for ( ; i < p1.length; ++i) {
         int k = i;
         p1[i] = k;
     }
     buildPerm(perms, p1);
     
     // rotate bottom, torque up
     int[] p3 = new int[54];
     for (i = 0; i < 8 * n - 4; ++i) {
         int k = i;
         p3[i] = k;
     }
     for (int j = 0 ; j < 4 * n; ++i, ++j) {
         int k = 8 * n - 4 + ((j + n) % (4 * n));
         p3[i] = k;
     }
     for (int j = 0 ; j < 4 * n - 4; ++i, ++j) {
         int k = 12 * n - 4 + ((j + n - 1) % (4 * n - 4));
         p3[i] = k;
     }
     for ( ; i < p3.length; ++i) {
         int k = i;
         p3[i] = k;
     }
     buildPerm(perms, p3);
     
     // rotate 41 face, torque north
     int[] p5 = { 2, 42, 22, 34,  4,  5,  6,  7,  8,  9,
                 10, 14,  3, 43, 23, 15, 16, 17, 18, 19,
                  0, 40, 20, 32, 24, 25, 26, 27, 28, 29,
                 30, 12, 11, 51, 31, 35, 36, 37, 38, 39,
                  1, 41, 21, 33, 44, 45, 46, 47, 48, 49,
                 50, 13, 52, 53 };
     buildPerm(perms, p5);
     
     // rotate 44 face, torque east
     int[] p7 = { 0,  1, 16,  5, 45, 25, 36,  7,  8,  9,
                 10, 11, 12, 13,  6, 46, 26, 17, 18, 19,
                 20, 21, 14,  3, 43, 23, 34, 27, 28, 29,
                 30, 31, 32, 33,  2, 42, 22, 37, 38, 39,
                 40, 41, 15,  4, 44, 24, 35, 47, 48, 49,
                 50, 51, 52, 53 };
     buildPerm(perms, p7);

     // rotate 47 face, torque south
     int[] p9 = { 0,  1,  2,  3,  4, 18,  8, 48, 28, 38,
                 10, 11, 12, 13, 14, 15,  9, 49, 29, 19,
                 20, 21, 22, 23, 24, 16,  6, 46, 26, 36,
                 30, 31, 32, 33, 34, 35,  5, 45, 25, 39,
                 40, 41, 42, 43, 44, 17,  7, 47, 27, 37,
                 50, 51, 52, 53 };
     buildPerm(perms, p9);

     // rotate 50 face, torque west
     int[] p11= {32,  1,  2,  3,  4,  5,  6,  7, 12, 11,
                 51, 31, 20, 13, 14, 15, 16, 17,  0, 40,
                 38, 21, 22, 23, 24, 25, 26, 27, 18,  9,
                 49, 29, 28, 33, 34, 35, 36, 37,  8, 48,
                 39, 41, 42, 43, 44, 45, 46, 47, 19, 10,
                 50, 30, 52, 53 };
     buildPerm(perms, p11);
     
     return perms;
 }
 
}
