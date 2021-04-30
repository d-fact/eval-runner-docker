package ca.uwaterloo.cs.jgrok.test;

import java.io.File;
import java.io.PrintStream;
import java.io.FileNotFoundException;

import java.util.Scanner;
import java.util.Iterator;
import java.util.ArrayList;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.env.LoadingException;
import ca.uwaterloo.cs.jgrok.util.Timing;

public class Regress {
    private Env env;
    
    public Regress() {
        env = new Env();
    }
    
    public void executeRegression(String regressFileName) {
        Timing timing = new Timing();
        Regression regress = new Regression();
        RegressionLoader regressLoader = new RegressionLoader(regress);
        
        try {
            timing.start();
            env.out.println("Loading Regression File=" + regressFileName + " ...");
            regressLoader.load(env, Regression.getRegressHome() + File.separator + regressFileName);
            
            RegressionTest test;
            Iterator<RegressionTest> iter = regress.iterator();
            while(iter.hasNext()) {
                test = iter.next();
                String result = test.execute(env);
                env.out.println("\t" + result);
            }
            
            timing.stop();
            env.out.println("\tTotal Time=" + timing.getTime() + "sec");
        } catch(LoadingException e) {
            e.printStackTrace(System.err);
            return;
        }
    }
    
    public static void main(String[] args) {
        Regress program;
        program = new Regress();
        
        for(String regressFileName: args) {
            program.executeRegression(regressFileName);
        }
    }
    
    public static class Diff {
        private String file1;
        private String file2;
        
        /**
         * Constructor.
         */
        public Diff(String f1, String f2) {
            this.file1 = f1;
            this.file2 = f2;
        }
        
        /**
         * Compute files difference.
         * 
         * @param out the print stream to output files difference.
         * @return true if no difference is found otherwise false.
         */
        public boolean execute(PrintStream out) throws FileNotFoundException {
            boolean noDiff = true;
            Scanner s1 = null;
            Scanner s2 = null;
            
            try {
                s1 = new Scanner(new File(file1)).useDelimiter("\\n");
                s2 = new Scanner(new File(file2)).useDelimiter("\\n");
            } catch(Exception e) {
                throw new FileNotFoundException(e.getMessage());
            }
            
            ArrayList<String> list1 = new ArrayList<String>(500);
            while(s1.hasNextLine()) {
                list1.add(s1.nextLine());
            }
            
            ArrayList<String> list2 = new ArrayList<String>(500);
            while(s2.hasNextLine()) {
                list2.add(s2.nextLine());
            }
            
            // number of lines of each file
            int M = list1.size();
            int N = list2.size();
            
            // opt[i][j] = length of LCS of list1[i..M] and list2[j..N]
            int[][] opt = new int[M+1][N+1];
            
            // compute length of LCS and all subproblems via dynamic programming
            for (int i = M-1; i >= 0; i--) {
                for (int j = N-1; j >= 0; j--) {
                    if (list1.get(i).equals(list2.get(j)))
                        opt[i][j] = opt[i+1][j+1] + 1;
                    else 
                        opt[i][j] = Math.max(opt[i+1][j], opt[i][j+1]);
                }
            }
            
            // recover LCS itself and print out non-matching lines to standard output
            int i = 0, j = 0;
            while(i < M && j < N) {
                if (list1.get(i).equals(list2.get(j))) {
                    i++;
                    j++;
                } else {
                    noDiff = false;
                    if (opt[i+1][j] >= opt[i][j+1]) out.println("< " + list1.get(i++));
                    else                            out.println("> " + list2.get(j++));
                } 
            }
            
            // dump out one remainder of one string if the other is exhausted
            while(i < M || j < N) {
                noDiff = false;
                if      (i == M) out.println("> " + list2.get(j++));
                else if (j == N) out.println("< " + list1.get(i++));
            }
            
            return noDiff;
        }
    }
}
