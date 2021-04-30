package ca.uwaterloo.cs.jgrok.fb;

import java.util.Arrays;

class OperationRel {
    
    /**
     * Composes two tuple lists. If <code>l_a</code> has no duplicates
     * and <code>l_b</code> has no duplicates, the result list contains
     * no duplicates. The composition key will be deleted.
     *
     * <p><pre>
     * condition:
     *    l_a schema = A B C N D
     *    l_b schema = P N Q
     * operation:
     *    l_r =  composition(l_a, 3, l_b, 1);
     * result:
     *    l_r schema = A B C D P Q
     * </pre>
     */
    static TupleList composition(TupleList l_a, int col_a,
                                 TupleList l_b, int col_b) {
        return composition(l_a, col_a, false, l_b, col_b, false);
    }
    
    /**
     * Composes two tuple lists. If <code>l_a</code> has no duplicates
     * and <code>l_b</code> has no duplicates, the result list contains
     * no duplicates.
     *
     * <p><pre>
     * condition:
     *    l_a schema = A B C N D
     *    l_b schema = P N Q
     * operation:
     *    l_r =  compositionRel(l_a, 3, l_b, 1);
     * result:
     *    l_r schema = A B C N D P Q
     * </pre>
     */
    static TupleList compositionRel(TupleList l_a, int col_a,
                                    TupleList l_b, int col_b) {
        return compositionRel(l_a, col_a, false, l_b, col_b, false);
    }
    
    /**
     * Composes two tuple lists. If <code>l_a</code> has no duplicates
     * and <code>l_b</code> has no duplicates, the result list contains
     * no duplicates. The composition key will be deleted.
     *
     * @param sorted_a indicates if <code>l_a</code> was sorted into
     * ascending order in column <code>col_a</code>.
     * @param sorted_b indicates if <code>l_b</code> was sorted into
     * ascending order in column <code>col_b</code>.
     * 
     * <p><pre>
     * condition:
     *    l_a schema = A B C N D
     *    l_b schema = P N Q
     * operation:
     *    l_r =  compositionRel(l_a, 3, boolean, l_b, 1, boolean);
     * result:
     *    l_r schema = A B C D P Q
     * </pre>
     */
    static TupleList composition(TupleList l_a, int col_a, boolean sorted_a,
                                 TupleList l_b, int col_b, boolean sorted_b) {
        int count1 = l_a.size();
        int count2 = l_b.size();
        
        TupleList l_r = new TupleList();
        if(count1 == 0 || count2 == 0) return l_r;
        
        TupleList l_1, l_2;
        if(sorted_a) {
            l_1 = l_a;
        } else {
            l_1 = new TupleList(l_a.size());
            RadixSorter.sort(l_a, col_a, l_1);
        }
        
        if(sorted_b) {
            l_2 = l_b;
        } else {
            l_2 = new TupleList(l_b.size());
            RadixSorter.sort(l_b, col_b, l_2);
        }
        
        int[] leftcols_a = new int[l_1.get(0).size() - 1];
        for(int c = 0; c < leftcols_a.length; c++) {
            if(c < col_a) leftcols_a[c] = c;
            else leftcols_a[c] = c+1;
        }
        
        int[] leftcols_b = new int[l_2.get(0).size() - 1];
        for(int c = 0; c < leftcols_b.length; c++) {
            if(c < col_b) leftcols_b[c] = c;
            else leftcols_b[c] = c+1;
        }
        
        Tuple t1, t2, tmp;
        int i = 0, j = 0, v1, v2;
        for(; i < count1; i++) {
            t1 = (Tuple)l_1.get(i);
            v1 = t1.get(col_a);
            for(; j < count2; j++) {
                t2 = (Tuple)l_2.get(j);
                v2 = t2.get(col_b);
                if(v1 == v2) {
                    int k = j;
                    while(k < count2) {
                        tmp = (Tuple)l_2.get(k);
                        if(v1 == tmp.get(col_b)) {
                            l_r.add(new TupleImpl(t1.get(leftcols_a), tmp.get(leftcols_b)));
                            k++;
                        } else break;
                    }
                    break;
                } else if(v1 < v2) {
                    break;
                }
            }
         }
        
        return l_r;
    }
    
    /**
     * Composes two tuple lists. If <code>l_a</code> has no duplicates
     * and <code>l_b</code> has no duplicates, the result list contains
     * no duplicates.
     *
     * @param sorted_a indicates if <code>l_a</code> was sorted into
     * ascending order in column <code>col_a</code>.
     * @param sorted_b indicates if <code>l_b</code> was sorted into
     * ascending order in column <code>col_b</code>.
     * 
     * <p><pre>
     * condition:
     *    l_a schema = A B C N D
     *    l_b schema = P N Q
     * operation:
     *    l_r =  compositionRel(l_a, 3, boolean, l_b, 1, boolean);
     * result:
     *    l_r schema = A B C N D P Q
     * </pre>
     */
    static TupleList compositionRel(TupleList l_a, int col_a, boolean sorted_a,
                                    TupleList l_b, int col_b, boolean sorted_b) {
        int count1 = l_a.size();
        int count2 = l_b.size();
        
        TupleList l_r = new TupleList();
        if(count1 == 0 || count2 == 0) return l_r;
        
        TupleList l_1, l_2;
        if(sorted_a) {
            l_1 = l_a;
        } else {
            l_1 = new TupleList(l_a.size());
            RadixSorter.sort(l_a, col_a, l_1);
        }
        
        if(sorted_b) {
            l_2 = l_b;
        } else {
            l_2 = new TupleList(l_b.size());
            RadixSorter.sort(l_b, col_b, l_2);
        }
        
        int[] cols = new int[l_2.get(0).size() - 1];
        for(int c = 0; c < cols.length; c++) {
            if(c < col_b) cols[c] = c;
            else cols[c] = c+1;
        }
        
        Tuple t1, t2, tmp;
        int i = 0, j = 0, k, v1, v2;
        for(; i < count1; i++) {
            t1 = l_1.get(i);
            v1 = t1.get(col_a);
            for(; j < count2; j++) {
                t2 = l_2.get(j);
                v2 = t2.get(col_b);
                if(v1 == v2) {
                    k = j;
                    while(k < count2) {
                        tmp = l_2.get(k);
                        if(v1 == tmp.get(col_b)) {
                            l_r.add(new TupleImpl(t1.toArray(), tmp.get(cols)));
                            k++;
                        } else break;
                    }
                    break;
                } else if(v1 < v2) {
                    break;
                }
            }
         }
        
        return l_r;
    }
    
    /**
     * Composes two tuple lists. If <code>l_a</code> has no duplicates
     * and <code>l_b</code> has no duplicates, the result list contains
     * no duplicates. The composition key will be deleted.
     *
     * @param sorted_a indicates if <code>l_a</code> was sorted into
     * ascending order in columns <code>cols_a</code>.
     * @param sorted_b indicates if <code>l_b</code> was sorted into
     * ascending order in columns <code>cols_b</code>.
     * 
     * <p><pre>
     * condition:
     *    l_a schema = A B C N D M E
     *    l_b schema = P N Q R M S
     * operation:
     *    l_r =  compositionRel(l_a, [3, 5], boolean, l_b, [1, 4], boolean);
     * result:
     *    l_r schema = A B C D E P Q R S
     * </pre>
     */
    static TupleList composition(TupleList l_a, int[] cols_a, boolean sorted_a,
                                 TupleList l_b, int[] cols_b, boolean sorted_b) {
        int count1 = l_a.size();
        int count2 = l_b.size();
        
        TupleList l_r = new TupleList();
        if(count1 == 0 || count2 == 0) return l_r;
        if(cols_a.length != cols_b.length) return l_r;
        
        TupleList l_1, l_2, l_tmp;
        if(sorted_a) {
            l_1 = l_a;
        } else {
            l_tmp = l_a;
            l_1 = new TupleList();
            for(int k = cols_a.length-1; k >= 0; k--) {
                RadixSorter.sort(l_tmp, cols_a[k], l_1);
                l_tmp = l_1;
            }
        }
        
        if(sorted_b) {
            l_2 = l_b;
        } else {
            l_tmp = l_b;
            l_2 = new TupleList();
            for(int k = cols_b.length-1; k >= 0; k--) {
                RadixSorter.sort(l_tmp, cols_b[k], l_2);
                l_tmp = l_2;
            }
        }
        
        int index;
        
        int[] cols_a_clone = (int[])cols_a.clone();
        Arrays.sort(cols_a_clone);
        
        int[] leftcols_a = new int[l_1.get(0).size() - cols_a.length];
        index = 0;
        for(int k = 0; k < l_1.get(0).size(); k++) {
            if(Arrays.binarySearch(cols_a_clone, k) < 0) {
                leftcols_a[index] = k;
                index++;
            }
        }
        
        int[] cols_b_clone = (int[])cols_b.clone();
        Arrays.sort(cols_b_clone);
        
        int[] leftcols_b = new int[l_2.get(0).size() - cols_b.length];
        index = 0;
        for(int k = 0; k < l_2.get(0).size(); k++) {
            if(Arrays.binarySearch(cols_b_clone, k) < 0) {
                leftcols_b[index] = k;
                index++;
            }
        }
        
        Tuple t1, t2, t_tmp;
        Tuple t_v1, t_v2;
        int i = 0, j = 0;
        int compare;
        
        for(; i < count1; i++) {
            t1 = (Tuple)l_1.get(i);
            t_v1 = new TupleImpl(t1.get(cols_a), false);
            for(; j < count2; j++) {
                t2 = (Tuple)l_2.get(j);
                t_v2 = new TupleImpl(t2.get(cols_b), false);
                compare = t_v1.compareTo(t_v2);
                if(compare == 0) {
                    int k = j;
                    while(k < count2) {
                        t_tmp = (Tuple)l_2.get(k);
                        if(t_v1.compareTo(new TupleImpl(t_tmp.get(cols_b), false)) == 0) {
                            if(leftcols_b.length == 0) l_r.add(new TupleImpl(t1.get(leftcols_a)));
                            else l_r.add(new TupleImpl(t1.get(leftcols_a), t_tmp.get(leftcols_b)));
                            k++;
                        } else break;
                    }
                    break;
                } else if(compare < 0) {
                    break;
                }
            }
         }
        
        return l_r;
    }
    
    /**
     * Composes two tuple lists. If <code>l_a</code> has no duplicates
     * and <code>l_b</code> has no duplicates, the result list contains
     * no duplicates.
     *
     * @param sorted_a indicates if <code>l_a</code> was sorted into
     * ascending order in columns <code>cols_a</code>.
     * @param sorted_b indicates if <code>l_b</code> was sorted into
     * ascending order in columns <code>cols_b</code>.
     * 
     * <p><pre>
     * condition:
     *    l_a schema = A B C N D M E
     *    l_b schema = P N Q R M S
     * operation:
     *    l_r =  compositionRel(l_a, [3, 5], boolean, l_b, [1, 4], boolean);
     * result:
     *    l_r schema = A B C N D M E P Q R S
     * </pre>
     */
    static TupleList compositionRel(TupleList l_a, int[] cols_a, boolean sorted_a,
                                    TupleList l_b, int[] cols_b, boolean sorted_b) {
        int count1 = l_a.size();
        int count2 = l_b.size();
        
        TupleList l_r = new TupleList();
        if(count1 == 0 || count2 == 0) return l_r;
        if(cols_a.length != cols_b.length) return l_r;
        
        TupleList l_1, l_2, l_tmp;
        if(sorted_a) {
            l_1 = l_a;
        } else {
            l_tmp = l_a;
            l_1 = new TupleList();
            for(int k = cols_a.length-1; k >= 0; k--) {
                RadixSorter.sort(l_tmp, cols_a[k], l_1);
                l_tmp = l_1;
            }
        }
        
        if(sorted_b) {
            l_2 = l_b;
        } else {
            l_tmp = l_b;
            l_2 = new TupleList();
            for(int k = cols_b.length-1; k >= 0; k--) {
                RadixSorter.sort(l_tmp, cols_b[k], l_2);
                l_tmp = l_2;
            }
        }
        
        int[] cols_b_clone = (int[])cols_b.clone();
        Arrays.sort(cols_b_clone);
        
        int[] cols = new int[l_2.get(0).size() - cols_b.length];
        int index = 0;
        for(int k = 0; k < l_2.get(0).size(); k++) {
            if(Arrays.binarySearch(cols_b_clone, k) < 0) {
                cols[index] = k;
                index++;
            }
        }
        
        Tuple t1, t2, t_tmp;
        Tuple t_v1, t_v2;
        int i = 0, j = 0;
        int compare;
        
        for(; i < count1; i++) {
            t1 = (Tuple)l_1.get(i);
            t_v1 = new TupleImpl(t1.get(cols_a), false);
            for(; j < count2; j++) {
                t2 = (Tuple)l_2.get(j);
                t_v2 = new TupleImpl(t2.get(cols_b), false);
                compare = t_v1.compareTo(t_v2);
                if(compare == 0) {
                    int k = j;
                    while(k < count2) {
                        t_tmp = (Tuple)l_2.get(k);
                        if(t_v1.compareTo(new TupleImpl(t_tmp.get(cols_b), false)) == 0) {
                            if(cols.length == 0) l_r.add(new TupleImpl(t1.toArray()));
                            else l_r.add(new TupleImpl(t1.toArray(), t_tmp.get(cols)));
                            k++;
                        } else break;
                    }
                    break;
                } else if(compare < 0) {
                    break;
                }
            }
         }
        
        return l_r;
    }
    
    /**
     * <pre>
     * Expressions:
     *     set . relation
     * Precondition:
     *     s_l is sorted into ascending order in Column 0
     *     r_l is sorted into ascending order in Column 0
     * Postcondition:
     *     A tuple list sorted into ascending order,
     *     and it has no duplicates.
     * </pre>
     */
    static TupleList forwardProjection(TupleList s_l, TupleList r_l) {
        Tuple t1, t2;
        int count1 = s_l.size();
        int count2 = r_l.size();
        
        TupleList l = new TupleList();
        if(count1 == 0 || count2 == 0) return l;
        
        int i = 0, j = 0, v1, v2;
        int dim = r_l.get(0).size() - 1;
        
        if(dim == 2) {
            for(; i < count1; i++) {
                t1 = s_l.get(i);
                v1 = t1.getRng();
                for(; j < count2; j++) {
                    t2 = r_l.get(j);
                    v2 = t2.getDom();
                    
                    if(v1 == v2) l.add(new Tuple4Edge(t2.get(1), t2.get(2)));
                    else if(v1 < v2) break;
                }
            }
        } else {
            int[] dims = new int[dim];
            for(int k = 0; k < dim; k++) {
                dims[k] = k+1;
            }
            
            for(; i < count1; i++) {
                t1 = s_l.get(i);
                v1 = t1.getRng();
                for(; j < count2; j++) {
                    t2 = r_l.get(j);
                    v2 = t2.getDom();
                    
                    if(v1 == v2) l.add(new TupleImpl(t2.get(dims), false));
                    else if(v1 < v2) break;
                }
            }
        }
        
        l.sort_removeDuplicates();
        return l;
    }
    
    /**
     * <pre>
     * Expressions:
     *     relation . set
     * Precondition:
     *     r_l is sorted into ascending order in the last column
     *     s_l is sorted into ascending order in Column 0
     * Postcondition:
     *     A tuple list sorted into ascending order,
     *     and it has no duplicates.
     * </pre>
     */
    static TupleList backwardProjection(TupleList r_l, TupleList s_l) {
        Tuple t1, t2;
        int count1 = r_l.size();
        int count2 = s_l.size();
        
        TupleList l = new TupleList();
        if(count1 == 0 || count2 == 0) return l;
        
        int i = 0, j = 0, v1, v2;
        int dim = r_l.get(0).size() - 1;
        
        if(dim == 2) {
            for(; j < count2; j++) {
                t2 = s_l.get(j);
                v2 = t2.getDom();
                for(; i < count1; i++) {
                    t1 = r_l.get(i);
                    v1 = t1.getRng();
                    
                    if(v2 == v1) l.add(new Tuple4Edge(t1.get(0), t1.get(1)));
                    else if(v2 < v1) break;
                }
            }
        } else {
            int[] dims = new int[dim];
            for(int k = 0; k < dim; k++) {
                dims[k] = k;
            }
            
            for(; j < count2; j++) {
                t2 = s_l.get(j);
                v2 = t2.getDom();
                for(; i < count1; i++) {
                    t1 = r_l.get(i);
                    v1 = t1.getRng();
                    
                    if(v2 == v1) l.add(new TupleImpl(t1.get(dims), false));
                    else if(v2 < v1) break;
                }
            }
        }
        
        l.sort_removeDuplicates();
        return l;
    }
}
