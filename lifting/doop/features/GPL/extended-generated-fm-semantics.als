module m

abstract sig Bool {} 
one sig True, False extends Bool {}

pred isTrue[b: Bool] { b in True }
pred isFalse[b: Bool] { b in False }

one sig SPL, GPL, BASE, BENCHMARK, PROG, EDGES, WEIGHTED, BASEIMPL, SEARCHI, ALGORITHMS, NUMBER, CONNECTED, TRANSPOSE, 
MSTPRIM, MSTKRUSKAL, SHORTEST, CYCLE, STRONGLYCONNECTED, NEWCOMPOUND1, EDGEOBJECTS, GNONLYNEIGHBORS,
GNOEDGES, GENEDGES, DIRECTED, UNDIRECTED, SEARCHALG, DFS, BFS, SEARCHBASE  in Bool {}

pred semanticsFM[] {
	isTrue[SPL] and

	(isTrue[SPL] <=> isTrue[GPL]) and

    (isTrue[EDGES] <=> isTrue[GPL]) and 
    (isTrue[BASEIMPL] <=> isTrue[GPL]) and 
    (isTrue[ALGORITHMS] <=> isTrue[GPL]) and 

	(isTrue[BASE] => isTrue[GPL]) and
	(isTrue[BENCHMARK] => isTrue[GPL]) and
	(isTrue[PROG] => isTrue[GPL]) and
	(isTrue[WEIGHTED] => isTrue[GPL]) and
	(isTrue[SEARCHI] => isTrue[GPL]) and

	(isTrue[NUMBER] => isTrue[ALGORITHMS]) and
	(isTrue[CONNECTED] => isTrue[ALGORITHMS]) and
	(isTrue[TRANSPOSE] => isTrue[ALGORITHMS]) and
	(isTrue[MSTPRIM] => isTrue[ALGORITHMS]) and
	(isTrue[MSTKRUSKAL] => isTrue[ALGORITHMS]) and
	(isTrue[SHORTEST] => isTrue[ALGORITHMS]) and
	(isTrue[CYCLE] => isTrue[ALGORITHMS]) and
	(isTrue[STRONGLYCONNECTED] => isTrue[ALGORITHMS]) and

    (isTrue[NEWCOMPOUND1] <=> isTrue[BASEIMPL]) and
	(isTrue[EDGEOBJECTS] => isTrue[BASEIMPL]) and

	(isTrue[GNONLYNEIGHBORS] => isTrue[NEWCOMPOUND1]) and
	(isTrue[GNOEDGES] => isTrue[NEWCOMPOUND1]) and
	(isTrue[GENEDGES] => isTrue[NEWCOMPOUND1]) and

	(isTrue[EDGES] <=> (isTrue[DIRECTED] or isTrue[UNDIRECTED])) and 
	not(isTrue[DIRECTED] and isTrue[UNDIRECTED])

	(isTrue[SEARCHI] <=> isTrue[SEARCHALG]) and
	(isTrue[SEARCHI] <=> (isTrue[DFS] or isTrue[BFS] or isTrue[SEARCHBASE])) and 
	not(isTrue[DFS] and isTrue[BFS] and isTrue[SEARCHBASE]) and

	(isTrue[PROG] => isTrue[BENCHMARK]) and
	(isTrue[GENEDGES] => isTrue[EDGEOBJECTS]) and
	((isTrue[DFS] or isTrue[BFS] or isTrue[NUMBER] or isTrue[CONNECTED]) => isTrue[SEARCHBASE]) and
	(isTrue[CONNECTED] => isTrue[UNDIRECTED]) and
	(isTrue[STRONGLYCONNECTED] => (isTrue[DIRECTED] and isTrue[DFS] and isTrue[TRANSPOSE])) and
	(isTrue[CYCLE] => isTrue[DFS]) and

	((isTrue[MSTPRIM] or isTrue[MSTKRUSKAL]) => (isTrue[EDGEOBJECTS] and isTrue[UNDIRECTED] and isTrue[WEIGHTED])) and
	
(isTrue[SHORTEST] => (isTrue[DIRECTED] and isTrue[WEIGHTED]))

(isTrue[GNOEDGES] => isTrue[UNDIRECTED])
}

pred testConfiguration[] {
	//isTrue[GPL] and isTrue[Prog] and isFalse[Benchmark]
	isTrue[GPL]
}

pred verify[] {
	semanticsFM[] and testConfiguration[]
}

// run verify for 2

// pega todas as configuracoes validas!
pred semanticsFMGenerated[] { 
((((((((((((((((((((((((((((((((((((isTrue[SPL] and (isTrue[SPL] <=> isTrue[GPL])) and (isTrue[EDGES] <=> isTrue[GPL])) and (isTrue[BASEIMPL] <=> isTrue[GPL])) and (isTrue[ALGORITHMS] <=> isTrue[GPL])) and (isTrue[BASE] => isTrue[GPL])) and (isTrue[BENCHMARK] => isTrue[GPL])) and (isTrue[PROG] => isTrue[GPL])) and (isTrue[WEIGHTED] => isTrue[GPL])) and (isTrue[SEARCHI] => isTrue[GPL])) and (isTrue[NUMBER] => isTrue[ALGORITHMS])) and (isTrue[CONNECTED] => isTrue[ALGORITHMS])) and (isTrue[TRANSPOSE] => isTrue[ALGORITHMS])) and (isTrue[MSTPRIM] => isTrue[ALGORITHMS])) and (isTrue[MSTKRUSKAL] => isTrue[ALGORITHMS])) and (isTrue[SHORTEST] => isTrue[ALGORITHMS])) and (isTrue[CYCLE] => isTrue[ALGORITHMS])) and (isTrue[STRONGLYCONNECTED] => isTrue[ALGORITHMS])) and (isTrue[NEWCOMPOUND1] <=> isTrue[BASEIMPL])) and (isTrue[EDGEOBJECTS] => isTrue[BASEIMPL])) and (isTrue[GNONLYNEIGHBORS] => isTrue[NEWCOMPOUND1])) and (isTrue[GNOEDGES] => isTrue[NEWCOMPOUND1])) and (isTrue[GENEDGES] => isTrue[NEWCOMPOUND1])) and (isTrue[EDGES] <=> (isTrue[DIRECTED] or isTrue[UNDIRECTED]))) and not((isTrue[DIRECTED] and isTrue[UNDIRECTED]))) and (isTrue[SEARCHI] <=> isTrue[SEARCHALG])) and (isTrue[SEARCHI] <=> ((isTrue[DFS] or isTrue[BFS]) or isTrue[SEARCHBASE]))) and not(((isTrue[DFS] and isTrue[BFS]) and isTrue[SEARCHBASE]))) and (isTrue[PROG] => isTrue[BENCHMARK])) and (isTrue[GENEDGES] => isTrue[EDGEOBJECTS])) and ((((isTrue[DFS] or isTrue[BFS]) or isTrue[NUMBER]) or isTrue[CONNECTED]) => isTrue[SEARCHBASE])) and (isTrue[CONNECTED] => isTrue[UNDIRECTED])) and (isTrue[STRONGLYCONNECTED] => ((isTrue[DIRECTED] and isTrue[DFS]) and isTrue[TRANSPOSE]))) and (isTrue[CYCLE] => isTrue[DFS])) and ((isTrue[MSTPRIM] or isTrue[MSTKRUSKAL]) => ((isTrue[EDGEOBJECTS] and isTrue[UNDIRECTED]) and isTrue[WEIGHTED]))) and (isTrue[SHORTEST] => (isTrue[DIRECTED] and isTrue[WEIGHTED]))) and (isTrue[GNOEDGES] => isTrue[UNDIRECTED]))
}

assert verifyGenerated {
  semanticsFM[] <=> semanticsFMGenerated[]
}

check verifyGenerated for 2

