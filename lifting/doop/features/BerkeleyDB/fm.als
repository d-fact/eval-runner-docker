module m

abstract sig Bool {} 
one sig True, False extends Bool {}

pred isTrue[b: Bool] { b in True }
pred isFalse[b: Bool] { b in False }

one sig SPL, Logging, ConcurrTrans, Persistance, Statistics, BTree, Ops, Memory_Budget, Logging_Finer, Logging_Config, 
Logging_Severe, Logging_Evictor, Logging_Cleaner, Logging_Recovery, Logging_DbLogHandler, Logging_ConsoleHandler, 
Logging_Info, Logging_Base, Logging_FileHandler, Logging_Fine, Logging_Finest, Latches, Transactions, CheckLeaks, FSync, 
Checksum, IIO, Environment_Locking, Checkpointer, DiskFullErro, FileHandleCache, IICleaner, SynchronizedIO, IO, OldIO, 
NIOAccess, DirectNIO, NewIO, ChunkedNIO, NIO, CP_Bytes, CP_Time, Checkpointer_Daemon, CleanerDaemon, Cleaner, 
LookAHEADCache, INCompressor, IEvictor, Verifier, Critical_Eviction, EvictorDaemon, Evictor, DeleteOp, RenameOp, TruncateOp in Bool {}

pred semanticsFM[] {
	isTrue[SPL] 

    (isTrue[ConcurrTrans] <=> isTrue[SPL]) and 
    (isTrue[Persistance] <=> isTrue[SPL]) and 
    (isTrue[BTree] <=> isTrue[SPL]) and 
    (isTrue[Ops] <=> isTrue[SPL]) and 

	(isTrue[Logging] => isTrue[SPL]) and
	(isTrue[Statistics] => isTrue[SPL]) and
	(isTrue[Memory_Budget] => isTrue[SPL]) and

	(isTrue[Logging_Finer] => isTrue[Logging]) and
	(isTrue[Logging_Config] => isTrue[Logging]) and
	(isTrue[Logging_Severe] => isTrue[Logging]) and
	(isTrue[Logging_Evictor] => isTrue[Logging]) and
	(isTrue[Logging_Cleaner] => isTrue[Logging]) and
	(isTrue[Logging_Recovery] => isTrue[Logging]) and
	(isTrue[Logging_DbLogHandler] => isTrue[Logging]) and
	(isTrue[Logging_ConsoleHandler] => isTrue[Logging]) and
	(isTrue[Logging_Info] => isTrue[Logging]) and
	(isTrue[Logging_FileHandler] => isTrue[Logging]) and
	(isTrue[Logging_Fine] => isTrue[Logging]) and
	(isTrue[Logging_Finest] => isTrue[Logging]) and
	(isTrue[Logging_Base] <=> isTrue[Logging]) and

	(isTrue[Latches] => isTrue[ConcurrTrans]) and
	(isTrue[Transactions] => isTrue[ConcurrTrans]) and
	(isTrue[CheckLeaks] => isTrue[ConcurrTrans]) and
	(isTrue[FSync] => isTrue[ConcurrTrans]) and


	(isTrue[Checksum] => isTrue[Persistance]) and
	(isTrue[Environment_Locking] => isTrue[Persistance]) and
	(isTrue[DiskFullErro] => isTrue[Persistance]) and
	(isTrue[FileHandleCache] => isTrue[Persistance]) and
	(isTrue[IIO] <=> isTrue[Persistance]) and
	(isTrue[Checkpointer] <=> isTrue[Persistance]) and
	(isTrue[IICleaner] <=> isTrue[Persistance]) and

	(isTrue[IIO] <=> (isTrue[OldIO] or isTrue[NewIO])) and 
	not(isTrue[OldIO] and isTrue[NewIO])

	(isTrue[SynchronizedIO] => isTrue[OldIO]) and
	(isTrue[IO] <=> isTrue[OldIO]) and

	(isTrue[DirectNIO] => isTrue[NewIO]) and
	(isTrue[NIOAccess] <=> isTrue[NewIO]) and

	(isTrue[NIOAccess] <=> (isTrue[ChunkedNIO] or isTrue[NIO])) and 
	not(isTrue[ChunkedNIO] and isTrue[NIO])

	(isTrue[CP_Bytes] => isTrue[Checkpointer]) and
	(isTrue[CP_Time] => isTrue[Checkpointer]) and
	(isTrue[Checkpointer_Daemon] => isTrue[Checkpointer]) and

	(isTrue[CleanerDaemon] => isTrue[IICleaner]) and
	(isTrue[Cleaner] <=> isTrue[IICleaner]) and
	(isTrue[LookAHEADCache] => isTrue[IICleaner]) and

	(isTrue[INCompressor] => isTrue[BTree]) and
	(isTrue[IEvictor] => isTrue[BTree]) and
	(isTrue[Verifier] => isTrue[BTree]) and

	(isTrue[Critical_Eviction] => isTrue[IEvictor]) and
	(isTrue[EvictorDaemon] => isTrue[IEvictor]) and
	(isTrue[Evictor] <=> isTrue[IEvictor]) and

	(isTrue[DeleteOp] => isTrue[Ops]) and
	(isTrue[RenameOp] => isTrue[Ops]) and
	(isTrue[TruncateOp] => isTrue[Ops]) and

	((isTrue[Evictor] or isTrue[EvictorDaemon] or isTrue[LookAHEADCache]) => isTrue[Memory_Budget])
	(isTrue[Critical_Eviction] => isTrue[INCompressor])
	(isTrue[CP_Bytes] => isTrue[CP_Time])
	(isTrue[DeleteOp] => (isTrue[Evictor] and isTrue[INCompressor] and isTrue[Memory_Budget]))
	(isTrue[Memory_Budget] => (isTrue[Evictor] and isTrue[Latches]))
	(isTrue[TruncateOp] => isTrue[DeleteOp])
	(isTrue[Verifier] => isTrue[INCompressor])

}

pred testConfiguration[] {
	isTrue[SPL]
}

pred verify[] {
	semanticsFM[] and testConfiguration[]
}

run verify for 2
