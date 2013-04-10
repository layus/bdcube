package be.ac.ucl.info.bdcube;



import java.util.Arrays;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;


public class Main3 {
	static BDDFactory bdd;
	static int n = 2; // 3x3 cube
	static int k = 3; // number of moves
	
	public static void main(String[] args) {

		bdd = BDDFactory.init(10000000, 1000000);
		bdd.setMaxIncrease(250000);

		if (args.length > 0) {
			k = Integer.parseInt(args[0]);
		}
		System.out.println(System.getProperties().getProperty(
				"java.library.path"));
		// 6n^2 domains, one for each square.
		int[] sizes = new int[7 * 2 * 2];
		// 6 possible colors for each domain.
		Arrays.fill(sizes, 7);
		Arrays.fill(sizes, 7, 14, 3);
		Arrays.fill(sizes, 21, 28, 3);

		// BDDDomain[] squares =
		bdd.extDomain(sizes);
		bdd.autoReorder(BDDFactory.REORDER_SIFTITE);
		
		int[] initial = {0, 1, 2, 3, 4, 5, 6, 0, 0, 0, 0, 0, 0, 0};
		
		BDD init = bdd.one();
		for (int k = 0; k < initial.length; ++k) {
			init.andWith(bdd.getDomain(k).ithVar((k < 7)? k : 0));
		}
		
		BDD trans = build_trans();
		
		BDD vars = bdd.getDomain(0).set();
		for (int i = 0; i < initial.length; i++) {
			vars.andWith(bdd.getDomain(i).set());
		}
		System.out.println(vars.allsat().size());
		
		BDDPairing swap = bdd.makePair();
		
			BDDDomain[] dorig = new BDDDomain[initial.length];
		    for (int i = 0; i < dorig.length; ++i) {
		       dorig[i] = bdd.getDomain(i);
		    }
		    BDDDomain[] dperm = new BDDDomain[initial.length];
		    for (int i=0; i<dperm.length; ++i) {
		        dperm[i] = bdd.getDomain(dperm.length + i);
		    }
			swap.set(dperm, dorig);
		
		
		init.printSetWithDomains();
		BDD step = trans.relprod(init, vars).replace(swap).satOne();
		
		step.printSetWithDomains();

	}
	
	public static BDD build_trans() {
		int[] p1 =      {3, 0, 1, 2, 4, 5, 6, 1, 2, 1, 2, 0, 0, 0};
		int[] p2 =      {0, 6, 1, 3, 4, 2, 5, 0, 1, 2, 0, 0, 1, 2};
		int[] p3 =      {0, 1, 5, 2, 3, 4, 6, 0, 0, 0, 0, 0, 0, 0};
		
		BDD trans = bdd.zero();
		build_perm(trans, p1);
		build_perm(trans, p2);
		build_perm(trans, p3);
		
		return trans;
	}

	private static void build_perm(BDD trans, int[] p1) {
		BDD temp = bdd.one();
		for (int i = 0; i < p1.length; i++) {
			BDD step;
			if( i < 7 ){
				step = bdd.getDomain(i).buildEquals(bdd.getDomain(p1.length + p1[i]));
			}else{
				step = bdd.getDomain(i).buildAdd(bdd.getDomain(p1.length + i), p1[i]);
			}
			temp.andWith(step);
		}
		trans.orWith(temp);
		
		temp = bdd.one();
		for (int i = 0; i < p1.length; i++) {
			BDD step;
			if( i < 7 ){
				step = bdd.getDomain(p1.length+i).buildEquals(bdd.getDomain(p1[i]));
			}else{
				step = bdd.getDomain(p1.length+i).buildAdd(bdd.getDomain(i), p1[i]);
			}
			temp.andWith(step);
		}
		trans.orWith(temp);
		System.out.println("Coucou");
	}
	
	

}
