module m

abstract sig Bool {} 
one sig True, False extends Bool {}

pred isTrue[b: Bool] { b in True }
pred isFalse[b: Bool] { b in False }

one sig SPL, PrevaylerSPL, Snapshot, Monitor, Censor, GZip, Replication in Bool {}

pred semanticsFM[] {
	isTrue[SPL] <=> isTrue[PrevaylerSPL] and
	isTrue[Replication] => isTrue[PrevaylerSPL] and
	isTrue[GZip] => isTrue[PrevaylerSPL] and
	isTrue[Censor] => isTrue[PrevaylerSPL] and
	isTrue[Monitor] => isTrue[PrevaylerSPL] and
	isTrue[Snapshot] => isTrue[PrevaylerSPL]
}

pred testConfiguration[] {
	//isTrue[GPL] and isTrue[Prog] and isFalse[Benchmark]
	isTrue[SPL]
}

pred verify[] {
	semanticsFM[] and testConfiguration[]
}

// pega todas as configuracoes validas!
run verify for 2
