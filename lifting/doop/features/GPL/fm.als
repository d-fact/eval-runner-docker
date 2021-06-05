module m

abstract sig Bool {} 
one sig True, False extends Bool {}

pred isTrue[b: Bool] { b in True }
pred isFalse[b: Bool] { b in False }

one sig GPL, Base, Benchmark, Prog, Edges, Weighted, BaseImpl, SearchI, Algorithms, Number, Connected, Transpose, 
MSTPrim, MSTKruskal, Shortest, Cycle, StronglyConnected, NewCompound1, EdgeObjects, GN_OnlyNeighbors,
G_NoEdges, GEN_Edges, Directed, Undirected, SearchAlg, DFS, BFS, SearchBase  in Bool {}

pred semanticsFM[] {
	isTrue[GPL] and

    (isTrue[Edges] <=> isTrue[GPL]) and 
    (isTrue[BaseImpl] <=> isTrue[GPL]) and 
    (isTrue[Algorithms] <=> isTrue[GPL]) and 

	(isTrue[Base] => isTrue[GPL]) and
	(isTrue[Benchmark] => isTrue[GPL]) and
	(isTrue[Prog] => isTrue[GPL]) and
	(isTrue[Weighted] => isTrue[GPL]) and
	(isTrue[SearchI] => isTrue[GPL]) and

	(isTrue[Number] => isTrue[Algorithms]) and
	(isTrue[Connected] => isTrue[Algorithms]) and
	(isTrue[Transpose] => isTrue[Algorithms]) and
	(isTrue[MSTPrim] => isTrue[Algorithms]) and
	(isTrue[MSTKruskal] => isTrue[Algorithms]) and
	(isTrue[Shortest] => isTrue[Algorithms]) and
	(isTrue[Cycle] => isTrue[Algorithms]) and
	(isTrue[StronglyConnected] => isTrue[Algorithms]) and

    (isTrue[NewCompound1] <=> isTrue[BaseImpl]) and
	(isTrue[EdgeObjects] => isTrue[BaseImpl]) and

	(isTrue[GN_OnlyNeighbors] => isTrue[NewCompound1]) and
	(isTrue[G_NoEdges] => isTrue[NewCompound1]) and
	(isTrue[GEN_Edges] => isTrue[NewCompound1]) and

	(isTrue[Edges] <=> (isTrue[Directed] or isTrue[Undirected])) and 
	not(isTrue[Directed] and isTrue[Undirected])

	(isTrue[SearchI] <=> isTrue[SearchAlg]) and
	(isTrue[SearchI] <=> (isTrue[DFS] or isTrue[BFS] or isTrue[SearchBase])) and 
	not(isTrue[DFS] and isTrue[BFS] and isTrue[SearchBase]) and

	(isTrue[Prog] => isTrue[Benchmark]) and
	(isTrue[GEN_Edges] => isTrue[EdgeObjects]) and
	((isTrue[DFS] or isTrue[BFS] or isTrue[Number] or isTrue[Connected]) => isTrue[SearchBase]) and
	(isTrue[Connected] => isTrue[Undirected]) and
	(isTrue[StronglyConnected] => (isTrue[Directed] and isTrue[DFS] and isTrue[Transpose])) and
	(isTrue[Cycle] => isTrue[DFS]) and
	((isTrue[MSTPrim] or isTrue[MSTKruskal]) => (isTrue[EdgeObjects] and isTrue[Undirected] and isTrue[Weighted])) and
	(isTrue[Shortest] => (isTrue[Directed] and isTrue[Weighted]))
}

pred testConfiguration[] {
	//isTrue[GPL] and isTrue[Prog] and isFalse[Benchmark]
	isTrue[GPL]
}

pred verify[] {
	semanticsFM[] and testConfiguration[]
}

// pega todas as configuracoes validas!
run verify for 2
