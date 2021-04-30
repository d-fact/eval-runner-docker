// grok scripts for semantic history slicing on facts with un-versionized names
//#+INSERTION

outputFile = $1;

if ( $# > 2 ) {
    print "Support 1 test name now.\n";
    return; 
} else {
    if ( $# == 1 ) {
        cov = rng Coverage;
    } else {
        testName = $2;
        print "find covered entities of test:";
        print testName;
        cov = {testName} . Coverage;
    }
}

if ( #cov == 0 ) {
    print "No matching test coverage data.\n";
    return;
}

test_and_comp_file = outputFile + ".test_comp";
hunk_file = outputFile + ".hunk";
all = outputFile + ".all";

// explicitly clean output file 
print "" >> all;
print "" >> hunk_file;
print "" >> test_and_comp_file;

func_set = cov . (inv @name) . @commit
// print "// Functional Set"
// print func_set;

print "Size of coverage entites: " #cov;
print "Size of functional commits: " #func_set;

all_deps = call + contain + reference
dep_entities = cov . all_deps+;

// print dep_entities

// comp_set = (dom Insert + dom Delete) ^ dep_entities . (inv @name_hash) . @version_b
comp_set = dep_entities . (inv @name) . @commit;

print "Size of compilation entites: " #dep_entities
print "Size of compilation commits: " #comp_set
// print "// Compilation Set" 
// print comp_set
func_and_comp = func_set + comp_set
included_del_need_insert = set
included_del_need_insert = func_and_comp . (inv Delete) . (Insert + Update + Delete)

print "included del need insert" #included_del_need_insert
print included_del_need_insert

print func_and_comp >>> all
print included_del_need_insert >>> all
print (func_and_comp + included_del_need_insert) >> test_and_comp_file;

hunk_cmt = (func_and_comp + included_del_need_insert) . (HunkDep+);
print "Size of hunk set: " #hunk_cmt
// print "// Hunk Set" >>> all 
print hunk_cmt >>> all
print hunk_cmt >>> hunk_file

print "Finished"
