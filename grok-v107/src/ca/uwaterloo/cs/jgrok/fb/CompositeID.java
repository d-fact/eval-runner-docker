package ca.uwaterloo.cs.jgrok.fb;

public class CompositeID {
    
    public static NodeSet getEnclosingIDs(NodeSet set) {
        NodeSet comIDs;
        NodeSet result;
        TupleList t_l;
        
        result = new NodeSet();
        if(set == null || set.size() == 0)
            return result;
        
        set.trySort(0);
        t_l = set.data;
        comIDs = IDManager.getAllCompositeIDs();
        
        int aID;
        int comID;
        int[] sonIDs;
        
        int count = comIDs.size();
        for(int i = 0; i < count; i++) {
            comID = comIDs.data.get(i).get(0);
            sonIDs = IDManager.parse(comID);
            for(int k = 0; k < sonIDs.length; k++) {
                aID = sonIDs[k];
                if(BinarySearch.search(t_l, aID, 0) > -1) {
                    result.add(comID);
                }
                if(IDManager.isComposite(aID)) {
                    furtherDown(result, t_l, aID);
                }
            }
        }
        
        return result;
    }
    
    private static void furtherDown(NodeSet result, TupleList t_l, int comID) {
        int aID;
        int[] sonIDs;
        
        sonIDs = IDManager.parse(comID);
        for(int k = 0; k < sonIDs.length; k++) {
            aID = sonIDs[k];
            if(BinarySearch.search(t_l, aID, 0) > -1) {
                result.add(comID);
            }
            if(IDManager.isComposite(aID)) {
                furtherDown(result, t_l, aID);
            }
        }
    }
}
