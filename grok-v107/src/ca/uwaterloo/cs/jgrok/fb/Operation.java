package ca.uwaterloo.cs.jgrok.fb;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * In the Operation class, we use "rel" to refer to a binary relation,
 * i.e., a relation with two columns (DOM and RNG); and we use "n-r" to
 * refer to a nary-relation, i.e., a relation with more than two columns.
 *
 * @author JingweiWu
 */
class Operation {
    
    /**
     * <pre>
     * Expressions:
     *     set + set : set
     *     rel + rel : rel
     *     n-r + n-r : n-r
     * Precondition:
     *     none
     * Postcondition:
     *     A tuple list that may contain duplicate tuples,
     *     and it is not sorted.
     * </pre>
     */
    static TupleList union(TupleList list1, TupleList list2) {
        TupleList l;
        l = new TupleList(list1.size());
        
        l.addAll(list1);
        if(list1 != list2) l.addAll(list2);
        
        return l;
    }
    
    /**
     * <pre>
     * Expressions:
     *     set - set : set
     *     rel - rel : rel
     *     n-r - n-r : n-r
     * Precondition:
     *     list1 is sorted into ascending order
     *     list2 is sorted into ascending order
     * Postcondition:
     *     A correct difference that may contain duplicate tuples.
     *     All tuples in the list are sorted into ascending order.
     * </pre>
     */
    static TupleList difference(TupleList list1, TupleList list2) {
        Tuple t1, t2;
        int count1 = list1.size();
        int count2 = list2.size();
        
        int i=0, j=0, val;
        boolean i_add = true;
        TupleList l = new TupleList();
        for(; i < count1; i++) {
            i_add = true;
            t1 = list1.get(i);
            for(; j < count2; j++) {
                t2 = list2.get(j);
                val = t1.compareTo(t2);
                if(val < 0) {
                    if(i_add) l.add(t1);
                    else j--;
                    break;
                } else if(val == 0) {
                    i_add = false;
                }
            }
            
            if(j >= count2) {
                if(!i_add) {
                    i++;
                    for(; i < count1; i++) {
                        t2 = list1.get(i);
                        val = t1.compareTo(t2);
                        if(val != 0) break;
                    }
                }
                for(; i < count1; i++) {
                    l.add(list1.get(i));
                }
                break;
            }
         }
        
        return l;
    }
    
    /**
     * <pre>
     * Expressions:
     *     set ^ set : set
     *     rel ^ rel : rel
     *     n-r ^ n-r : n-r
     * Precondition:
     *     list1 is sorted into ascending order
     *     list2 is sorted into ascending order
     * Postcondition:
     *     A correct intersection that may contain duplicate tuples.
     *     All tuples in the list are into ascending order.
      * </pre>
     */
    static TupleList intersection(TupleList list1, TupleList list2) {
        Tuple t1, t2;
        int count1 = list1.size();
        int count2 = list2.size();
        
        int i=0, j=0, val;
        TupleList l = new TupleList();
        for(; i < count1; i++) {
            t1 = list1.get(i);
            for(; j < count2; j++) {
                t2 = list2.get(j);
                val = t1.compareTo(t2);
                if(val == 0) {
                    l.add(t1);
                    j++;
                    break;
                } else if(val < 0) {
                    break;
                }
            }
         }
        
        return l;
    }
    
    /**
     * <pre>
     * Expressions:
     *     set o set : rel
     *     set o rel : rel
     *     rel o set : rel
     *     rel o rel : rel
     * Comments:
     *     set o set <=> id (set ^ set)
     *     set o rel <=> (id set) o rel
     *     rel o set <=> rel o (id set) 
     * Precondition:
     *     list1 is sorted into ascending order in RNG
     *     list2 is sorted into ascending order in DOM
     * Postcondition:
     *     A tuple list not sorted.
     * </pre>
     */
    static TupleList composition(TupleList list1, TupleList list2) {
        Tuple t1, t2, tmp;
        int count1 = list1.size();
        int count2 = list2.size();
        
        TupleList l = new TupleList();
        if(count1 == 0 || count2 == 0) return l;
        
        int i = 0, j = 0, k, v1, v2;
        
        for(; i < count1; i++) {
            t1 = list1.get(i);
            v1 = t1.getRng();
            for(; j < count2; j++) {
                t2 = list2.get(j);
                v2 = t2.getDom();
                if(v1 == v2) {
                    k = j;
                    while(k < count2) {
                        tmp = list2.get(k);
                        if(v1 == tmp.getDom()) {
                            l.add(new Tuple4Edge(t1.getDom(), tmp.getRng()));
                            k++;
                        } else break;
                    }
                    break;
                } else if(v1 < v2) {
                    break;
                }
            }
         }
        
        return l;
    }
    
    /**
     * @param tupleCanBeLoop whether a binary tuple can be a loop.
     * <p>If tupleCanBeLoop is <code>true</code>, all the following
     * situations are permitted:
     * <pre>
     *     x x   o   x x   =>   x x x
     *     x x   o   x y   =>   x x y
     *     y x   o   x x   =>   y x x
     *     x y   o   y x   =>   x y x
     *     x y   o   y z   =>   x y z
     * </pre>
     * <p>If tupleCanBeLoop is <code>false</code>, only the following
     * situations are permitted:
     * <pre>
     *     x y   o   y x   =>   x y x
     *     x y   o   y z   =>   x y z
     * </pre>
     *
     * <pre>
     * Expressions:
     *     rel o rel : rel
     * Precondition:
     *     list1 is sorted into ascending order in RNG
     *     list2 is sorted into ascending order in DOM
     * Postcondition:
     *     A tertiary tuple list not sorted.
     * Example:
     *                          a x l
     *                          a x m
     *     a x       x l        b x l
     *     b x       x m        b x m
     *     b y   o   y m   =>   b y m
     *     c y       y n        b y n
     *     d z       z o        c y m
     *                          c y n
     *                          d z o
     * </pre>
     */
    static TupleList tertiary(TupleList list1,
                              TupleList list2,
                              boolean tupleCanBeLoop) {
        Tuple t1, t2, tmp;
        int count1 = list1.size();
        int count2 = list2.size();
        
        TupleList l = new TupleList();
        if(count1 == 0 || count2 == 0) return l;
        
        int i = 0, j = 0, k, v1, v2;
        int[] dat = new int[3];
        
        for(; i < count1; i++) {
            t1 = list1.get(i);
            v1 = t1.getRng();
            for(; j < count2; j++) {
                t2 = list2.get(j);
                v2 = t2.getDom();
                if(v1 == v2) {
                    k = j;
                    while(k < count2) {
                        tmp = list2.get(k);
                        if(v1 == tmp.getDom()) {
                            dat[0] = t1.getDom();
                            dat[1] = v1;
                            dat[2] = tmp.getRng();
                            
                            if(tupleCanBeLoop) {
                                l.add(new TupleImpl(dat));
                            } else {
                                if(dat[0] != dat[1] && dat[1] != dat[2])
                                    l.add(new TupleImpl(dat));
                            }
                            
                            k++;
                        } else break;
                    }
                    break;
                } else if(v1 < v2) {
                    break;
                }
            }
         }
        
        return l;
    }
    
    /**
     * <pre>
     * Expressions:
     *     inv set : set
     *     inv rel : rel
     *     inv n-r : n-r
     * Precondition:
     *     none
     * Postcondition:
     *     none
     * </pre>
     */
    static TupleList inverse(TupleList list) {
        int count = list.size();
        TupleList l = new TupleList(count);
        
        for(int i = 0; i < count; i++) {
            l.add((list.get(i)).getInverse());
        }
        
        return l;
    }
    
    /**
     * <pre>
     * Expressions:
     *     NodeSet = dom TupleSet
     * Precondition:
     *     list is sorted into ascending order in DOM
     * Postcondition:
     *     A correct domain set without any duplicate tuples.
     *     All tuples in the domain set are in ascending order.
     * </pre>
     */
    static TupleList domainOf(TupleList list) {
        int dom, next;
        int count = list.size();
        TupleList l = new TupleList();
        
        if(count > 0) {
            dom = list.get(0).getDom();
            l.add(new Tuple4Node(dom));
            
            for(int i = 1; i < count; i++) {
                next = list.get(i).getDom();
                if(dom != next) {
                    l.add(new Tuple4Node(next));
                    dom = next;
                }
            }
        }
        
        return l;
    }
    
    /**
     * <pre>
     * Expressions:
     *     NodeSet = rng TupleSet
     * Precondition:
     *     list is sorted into ascending order in RNG
     * Postcondition:
     *     A correct range set without any duplicate tuples.
     *     All tuples in the range set are in ascending order.
     * </pre>
     */
    static TupleList rangeOf(TupleList list) {
        int rng, next;
        int count = list.size();
        TupleList l = new TupleList();
        
        if(count > 0) {
            rng = list.get(0).getRng();
            l.add(new Tuple4Node(rng));
            
            for(int i = 1; i < count; i++) {
                next = list.get(i).getRng();
                if(rng != next) {
                    l.add(new Tuple4Node(next));
                    rng = next;
                }
            }
        }
        
        return l;
    }
    
    /**
     * <pre>
     * Expressions:
     *     NodeSet = ent TupleSet
     * Precondition:
     *     none
     * Postcondition:
     *     A tuple list that is sorted into ascending order and
     *     has no duplicates.
     * </pre>
     */
    static TupleList entityOf(TupleList list) {
        int count;
        TupleList l;
        ArrayList<Tuple> a_list;
        
        count = list.size();
        if(count > 0) {
            l = new TupleList(list.size());
            a_list = l.getList();
            
            for(int i = 0; i < count; i++) {
                Tuple t = list.get(i);
                for(int j = 0; j < t.size(); j++) {
                    a_list.add(new Tuple4Node(t.get(j)));
                }
            }
            
            l.sort_removeDuplicates();
        } else {
            l = new TupleList();
        }
        
        return l;
    }
    
    /**
     * <pre>
     * Expressions:
     *     EdgeSet = id TupleSet
     * Precondition:
     *     none.
     * Postcondition:
     *     A tuple list that is sorted into ascending order and
     *     has no duplicates.
     * </pre>
     */
    static TupleList id(TupleList list) {
        Tuple t;
        int count = list.size();
        TupleList l = new TupleList(count);
        
        for(int i = 0; i < count; i++) {
            t = list.get(i);
            for(int j = 0; j < t.size(); j++) {
                l.add(new Tuple4Edge(t.get(j), t.get(j)));
            }
        }
        
        l.sort(0);
        l.removeDuplicates();        
        return l;
    }
    
    /**
     * <pre>
     * Expressions:
     *     reach(set, rel) : rel
     * Precondition:
     *     list1 sorted into ascending order.
     *     list2 sorted into ascending order in DOM.
     * Postcondition:
     *     No duplicate tuples exist in the result list. 
     *     Tuples in the result are sorted into ascending order.
     * </pre>
     */
    static TupleList reach(TupleList list1, TupleList list2) {
        TupleList base;
        TupleList result;
        TupleList partial;
        TupleList partialBase;
        TupleList partialSorted;
        
        result = new TupleList(512);
        base = new TupleList(list1.size());
        base.addAll(list1);
        partialBase = base;
        
        while(partialBase.size() > 0) {
            partial = composition(partialBase, list2);
            
            // Recalculate base
            partialSorted = new TupleList(partial.size());
            RadixSorter.sort(partial, 1, partialSorted);
            partialBase = diffUnion(rangeOf(partialSorted), base);
            
            // Recalculate result.
            partial = new TupleList(partialSorted.size());
            RadixSorter.sort(partialSorted, 0, partial);
            partial.removeDuplicates();
            diffUnion(partial, result);
        }
        
        return result;
    }
    
    /**
     * <pre>
     * Expressions:
     *     unclosure(rel) : rel
     * Precondition:
     *     none
     * Postcondition:
     *     A tuple list that is sorted into ascending order and
     *     has no duplicates.
     * </pre>
     */
    static TupleList unclosure(TupleList list) {
        int rngCol = 1;
        TupleList shadow;
        TupleList r_1, r_n;
        
        // Sort list in DOM.RNG.
        list.sort_removeDuplicates();
        
        // set r_1 and r_n
        r_1 = difference(list, id(list));
        r_n = r_1;
        
        // Shadow is sorted into ascending order in RNG.
        shadow = new TupleList(r_n.size());
        RadixSorter.sort(r_n, rngCol, shadow);
        
        while(r_n.size() > 0) {
            r_n = composition(shadow, r_1);
            r_n.sort_removeDuplicates();
            r_1 = difference(r_1, r_n);
            
            shadow = new TupleList(r_n.size());
            RadixSorter.sort(r_n, rngCol, shadow);
        }
        
        return r_1;
    }
    
    /**
     * <pre>
     * Expressions:
     *     rel + : rel  (transitive closure)
     *     rel * : rel  (reflective transitive closure)
     * Precondition:
     *     none
     * Postcondition:
     *     A correct transitive closure without any duplicate tuples.
     *     Tuples in the transitive closure are sorted into ascending order.
     * </pre>
     */
    static TupleList closure(TupleList list, boolean reflective) {
        int rngCol = 1;
        TupleList base;
        TupleList result;
        TupleList partial;
        TupleList partialSorted;
        
        // Sort list in DOM.RNG.
        list.sort_removeDuplicates();
        
        // Initiate the result.
        result = new TupleList(list.size());
        result.addAll(list);
        
        // If reflective, add id relation.
        if(reflective) {
            diffUnion(id(list), result);
        }
        
        // base is sorted into ascending order in RNG.
        base = new TupleList(list.size());
        RadixSorter.sort(list, rngCol, base);
        
        partialSorted = list;
        while(partialSorted.size() > 0) {
            partial = composition(base, partialSorted);
            partial.sort_removeDuplicates();
            partialSorted = diffUnion(partial, result);
        }
        
        return result;
    }
    
    /**
     * <pre>
     * Expressions:
     *     none
     * Precondition:
     *     none
     * Postcondition:
     *     A path transitive closure without any duplicate tuples.
     *     Tuples in the path closure are sorted into ascending order.
     * 
     *     a b c  => A path starting at 'a' and ending at 'c', where
     *               (a b) is a primitive edge, and (b c) is a path.
     *     a 0 b  => (a b) is a primitive edge. 
     *
     * </pre>
     */
    static TupleList pathClosure(TupleList list) {
        int rngCol = 1;
        TupleList base;
        TupleList result;
        TupleList partial;
        TupleList partialSorted;
        
        // Sort list in DOM.RNG.
        list.sort_removeDuplicates();
        
        Tuple t;
        int size = list.size();
        int[] dat = new int[3];
        
        // Initiate the result.
        result = new TupleList(size);
        for(int i = 0; i < size; i++) {
            t = list.get(i);
            dat[0] = t.getDom();
            dat[1] = 0;         // This is a primitive edge.
            dat[2] = t.getRng();
            result.add(new TupleImpl(dat));
        }
        
        // base is sorted into ascending order in RNG.
        base = new TupleList(list.size());
        RadixSorter.sort(list, rngCol, base);
        
        partialSorted = list;
        while(partialSorted.size() > 0) {
            partial = tertiary(base, partialSorted, false);
            partial.sort_removeDuplicates();
            partialSorted = diffUnion(partial, result);
        }
        
        return result;
    }
    
    /**
     * Compute the difference of list1 and list2 and add the
     * difference to the list2.
     * <pre>
     * Expressions
     *     diff = list1 - list2
     *     list2 = list2 + diff
     * Precondition:
     *     list1 sorted into ascending order without duplicates
     *     list2 sorted into ascending order without duplicates
     * Postcondition:
     *     The diff is returned. It is sorted into ascending order
     *     and it has no duplicates.
     *     list2 is sorted into ascending order and has no duplicates.
     * </pre>
     */
    static TupleList diffUnion(TupleList list1, TupleList list2) {
        Tuple t1, t2;
        int count1 = list1.size();
        int count2 = list2.size();
        
        int i=0, j=0, val;
        TupleList l = new TupleList(count1);
        LinkedList<Tuple> linked = new LinkedList<Tuple>();
        
        for(; i < count1; i++) {
            t1 = list1.get(i);
            for(; j < count2; j++) {
                t2 = list2.get(j);
                val = t1.compareTo(t2);
                if(val < 0) {
                    l.add(t1);
                    linked.add(t1);
                    break;
                } else if(val == 0) {
                    linked.add(t2);
                    j++;
                    break;
                }
                linked.add(t2);
            }
            
            if(j == count2) {
                if(j > 0) {
                    t2 = list2.get(j-1);
                    val = t1.compareTo(t2);
                    if(val == 0) i++;
                }
                for(; i < count1; i++) {
                    t1 = list1.get(i);
                    l.add(t1);
                    linked.add(t1);
                }
                break;
            }
         }
        
        for(; j < count2; j++) {
            linked.add(list2.get(j));
        }
        
        list2.setList(new ArrayList<Tuple>(linked.size()));
        Iterator<Tuple> iter = linked.iterator();
        while(iter.hasNext()) {
            list2.add(iter.next());
        }
        
        return l;
    }
    
    /**
     * <pre>
     * Expressions:
     *     rel ! : rel
     * Precondition:
     *     none
     * Postcondition:
     *     A correct transitive closure without any duplicate tuples.
     *     Tuples in the transitive closure are sorted into ascending order.
     * </pre>
     */
    static TupleList symmetricClosure(TupleList list) {
        //??? Operation.symmetricClosure
        System.out.println("Not implemented yet: Operation.symmetricClosure()");
        return new TupleList();
    }
    
    /**
     * <pre>
     * Expressions:
     *     set <= set : boolean
     *     rel <= rel : boolean
     *     n-r <= n-r : boolean
     * Precondition:
     *     list1 is sorted into ascending order, no duplicates.
     *     list2 is sorted into ascending order, no duplicates.
     * </pre>
     */
    static boolean subsetOf(TupleList list1, TupleList list2) {
        int cmp = comparison(list1, list2);
        if(cmp == -1 || cmp == 0) return true;
        else return false;
    }
    
    /**
     * <pre>
     * Expressions:
     *     set cmp set : int
     *     rel cmp rel : int
     *     n-r cmp n-r : int
     * Precondition:
     *     list1 is sorted into ascending order, no duplicates.
     *     list2 is sorted into ascending order, no duplicates.
     * Postcondition:
     *    -1     <=   list1 is subset of list2
     *     0     ==   list1 is equal to list2
     *     1     >=   list2 is subset of list1
     *     3     !=   list1 is not subset of list2 and vice versa.
     * </pre>
     */
    static int comparison(TupleList list1, TupleList list2) {
        TupleList diff1 = difference(list1, list2);
        TupleList diff2 = difference(list2, list1);
        
        if(diff1.size() == 0) {
            if(diff2.size() == 0) return 0;
            else return -1;
        } else {
            if(diff2.size() == 0) return 1;
            else return 3;
        }
    }
    
    /**
     * <pre>
     * Expressions:
     *     localof(rel) : rel 
     * Precondition:
     *     list is sorted into ascending order in RNG
     * Postcondition:
     *     A correct localof relation without any duplicate tuples.
     *     All tuples in localof are sorted into ascending order in DOM.
     * </pre>
     */
    static TupleList localof(TupleList list) {
        int ind;
        int rng, next;
        int count = list.size();
        TupleList l = new TupleList();
        
        if(count > 0) {
            ind = 0;
            rng = list.get(0).getRng();
            for(int i = 1; i < count; i++) {
                next = list.get(i).getRng();
                if(rng == next) {
                    ind = -1;
                    continue;
                }

                if(ind != -1) {
                    l.add(new Tuple4Edge(rng, list.get(ind).getDom()));
                }
                
                ind = i;
                rng = next;
            }

            if(ind != -1) {
                l.add(new Tuple4Edge(rng, list.get(ind).getDom()));
            }
        }
        return l;
    }

    /**
     * <pre>
     * Expressions:
     *     outdegree(rel) : rel 
     * Precondition:
     *     list is sorted into ascending order in DOM
     * Postcondition:
     *     A correct outdegree relation without any duplicate tuples.
     *     All tuples in outdegree are sorted into ascending order in DOM.
     * Example:
     *     Input:
     *            x a
     *            x b
     *            y l        
     *            y m
     *            y n
     *     Output:
     *            x 2
     *            y 3
     * </pre>
     */
    static TupleList outdegree(TupleList list) {
        int deg;
        int dom, next;
        int count = list.size();
        TupleList l = new TupleList();
        
        if(count > 0) {
            deg = 1;
            dom = list.get(0).getDom();
            for(int i = 1; i < count; i++) {
                next = list.get(i).getDom();
                if(dom == next) {
                    deg++;
                    continue;
                }
                
                l.add(new Tuple4Edge(dom, IDManager.getID(""+deg)));
                deg = 1;
                dom = next;
            }
            
            l.add(new Tuple4Edge(dom, IDManager.getID(""+deg)));
        }
        return l;
    }
    
    /**
     * <pre>
     * Expressions:
     *     set . rel : set
     * Precondition:
     *     s_l is sorted into ascending order in RNG
     *     r_l is sorted into ascending order in DOM
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
        
        for(; i < count1; i++) {
            t1 = s_l.get(i);
            v1 = t1.getRng();
            for(; j < count2; j++) {
                t2 = r_l.get(j);
                v2 = t2.getDom();
                
                if(v1 == v2) l.add(new Tuple4Node(t2.getRng()));
                else if(v1 < v2) break;
            }
         }
        
        l.sort_removeDuplicates();
        return l;
    }
    
    /**
     * <pre>
     * Expressions:
     *     rel . set : set
     * Precondition:
     *     r_l is sorted into ascending order in RNG
     *     s_l is sorted into ascending order in DOM
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
        
        for(; j < count2; j++) {
            t2 = s_l.get(j);
            v2 = t2.getDom();
            for(; i < count1; i++) {
                t1 = r_l.get(i);
                v1 = t1.getRng();
                
                if(v2 == v1) l.add(new Tuple4Node(t1.getDom()));
                else if(v2 < v1) break;
            }
         }
        
        l.sort_removeDuplicates();
        return l;
    }
    
    /**
     * <pre>
     * Expressions:
     *     set X set : rel
     * Precondition:
     *     s_l is a set list, ascending order in DOM, no duplicate
     *     d_l is a set list, ascending order in DOM, no duplicate
     * Postcondition:
     *     A tuple list sorted into ascending order in DOM, and it
     *     does not have any duplicates.
     * </pre>
     */
    static TupleList crossProduct(TupleList s_l, TupleList d_l) {
        Tuple t1, t2;
        int count1 = s_l.size();
        int count2 = d_l.size();
        
        TupleList l = new TupleList();
        if(count1 == 0 || count2 == 0) return l;
        
        int i, j;
        int v1, v2;
        
        for(i = 0; i < count1; i++) {
            t1 = s_l.get(i);
            v1 = t1.getDom();
            
            for(j = 0; j < count2; j++) {
                t2 = d_l.get(j);
                v2 = t2.getDom();
                l.add(new Tuple4Edge(v1, v2));
            }
         }
        
        return l;
    }

    /**
     * <pre>
     * Expressions:
     *     delset(rel, set) : rel 
     * Precondition:
     *     rel sorted into ascending order in DOM
     *     set sorted into ascending order
     * Postcondition:
     *     none
     * </pre>
     */
    static TupleList delset(TupleList list, TupleList set) {
        int count = list.size();
        int setCount = set.size();
        TupleList l = new TupleList();
        
        if(count > 0) {
            
            Tuple t;
            int i = 0, j = 0;
            int dom, rng, val=-1;
            boolean i_add = true;
            
            for(; i < count; i++) {
                i_add = true;
                t = list.get(i);
                dom = t.getDom();
                for(; j < setCount; j++) {
                    val = set.get(j).getDom();
                    if(dom < val) {
                        if(i_add) l.add(new Tuple4Edge(dom, t.getRng()));
                        else j--;
                        break;
                    } else if(dom == val) {
                        i_add = false;
                    }
                }

                if(j >= setCount) {
                    if(!i_add) {
                        i++;
                        for(; i < count; i++) {
                            t = list.get(i);
                            dom = t.getDom();
                            if(dom != val) break;
                        }
                    }
                    for(; i < count; i++) {
                        t = list.get(i);
                        l.add(new Tuple4Edge(t.getDom(), t.getRng()));
                    }
                    break;
                }
            }
            
            TupleList tmp;
            tmp = new TupleList(l.size());
            RadixSorter.sort(l, 1, tmp);
            count = l.size();
            l.clear();
            
            i = 0; j = 0;
            for(; i < count; i++) {
                i_add = true;
                t = tmp.get(i);
                rng = t.getRng();
                for(; j < setCount; j++) {
                    val = set.get(j).getDom();
                    if(rng < val) {
                        if(i_add) l.add(new Tuple4Edge(t.getDom(), rng));
                        else j--;
                        break;
                    } else if(rng == val) {
                        i_add = false;
                    }
                }

                if(j >= setCount) {
                    if(!i_add) {
                        i++;
                        for(; i < count; i++) {
                            t = tmp.get(i);
                            rng = t.getRng();
                            if(rng != val) break;
                        }
                    }
                    for(; i < count; i++) {
                        t = tmp.get(i);
                        l.add(new Tuple4Edge(t.getDom(), t.getRng()));
                    }
                    break;
                }
            }  
            
        }
        
        return l;
    }
}
