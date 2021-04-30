package ca.uwaterloo.cs.jgrok.fb;

public class ReplaceID {
    static TupleList pairIDs;
    static StringTable strTable;
    
    static {
        pairIDs = null;
        strTable = StringTable.instance();
    }
    
    public static void init(EdgeSet pairIDs) {
        close();
        
        if(pairIDs != null && pairIDs.size() > 0) {
            ReplaceID.pairIDs = (TupleList)pairIDs.data.clone();
            strTable.initReplace(ReplaceID.pairIDs);
        }
    }
    
    public static void close() {
        if(pairIDs != null) {
            pairIDs = null;
            strTable.closeReplace();
        }
    }
    
    public static void process(TupleSet mSet) {
        if(pairIDs != null && mSet != null) {
            Tuple t;
            TupleList tl;
            tl = mSet.data;
            
            int id;            
            int count = tl.size();
            for(int i = 0; i < count; i++) {
                t = tl.get(i);
                for(int j = 0; j < t.size(); j++) {
                    id = t.get(j);
                    t.set(j, IDManager.getReplaceID(id));
                }
            }
            
            mSet.sortLevel = -1;
        }
    }
}
