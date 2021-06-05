module m

abstract sig Bool {} 
one sig True, False extends Bool {}

pred isTrue[b: Bool] { b in True }
pred isFalse[b: Bool] { b in False }

one sig Lampiro, Timing, Motorola, Screensaver, Send_debug1, UI, derivatives, TLS, Compression, BXMPP, Glider, not_plain_socket, 
Bludeno, x_notTiming, x_notUI, x_notBXMPP, x_notCompression, x_notTLS, x_notGlider, x_not_Bludeno in Bool {}

pred semanticsFM[] {

	isTrue[Lampiro] and

    (isTrue[derivatives] <=> isTrue[Lampiro]) and 

    (isTrue[Timing] => isTrue[Lampiro]) and 
    (isTrue[Motorola] => isTrue[Lampiro]) and 
    (isTrue[Screensaver] => isTrue[Lampiro]) and 
    (isTrue[Send_debug1] => isTrue[Lampiro]) and 
    (isTrue[UI] => isTrue[Lampiro]) and 
    (isTrue[TLS] => isTrue[Lampiro]) and 
    (isTrue[Compression] => isTrue[Lampiro]) and 
    (isTrue[BXMPP] => isTrue[Lampiro]) and 
    (isTrue[Glider] => isTrue[Lampiro]) and 
    (isTrue[not_plain_socket] => isTrue[Lampiro]) and 
    (isTrue[Bludeno] => isTrue[Lampiro]) and 

    (isTrue[x_notTiming] => isTrue[derivatives]) and 
    (isTrue[x_notUI] => isTrue[derivatives]) and 
    (isTrue[x_notBXMPP] => isTrue[derivatives]) and 
    (isTrue[x_notCompression] => isTrue[derivatives]) and 
    (isTrue[x_notTLS] => isTrue[derivatives]) and 
    (isTrue[x_notGlider] => isTrue[derivatives]) and 
    (isTrue[x_not_Bludeno] => isTrue[derivatives]) and 

    (isTrue[x_notTiming] <=> not(isTrue[Timing])) and 
    (isTrue[x_notUI] <=> not(isTrue[UI])) and 
    (isTrue[x_notBXMPP] <=> not(isTrue[BXMPP])) and 
    (isTrue[x_notCompression] <=> not(isTrue[Compression])) and 
    (isTrue[x_notTLS] <=> not(isTrue[TLS])) and 
    (isTrue[x_notGlider] <=> not(isTrue[Glider])) and 
    (isTrue[x_not_Bludeno] <=> not(isTrue[Bludeno])) 
}

pred testConfiguration[] {
	//isTrue[GPL] and isTrue[Prog] and isFalse[Benchmark]
	isTrue[Lampiro]
}

pred verify[] {
	semanticsFM[] and testConfiguration[]
}

run verify for 2
