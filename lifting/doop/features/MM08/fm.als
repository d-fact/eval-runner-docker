module m

abstract sig Bool {} 
one sig True, False extends Bool {}

pred isTrue[b: Bool] { b in True }
pred isFalse[b: Bool] { b in False }

one sig NewCompound1, MobilMedia, Media, includeSorting, includeFavourites, includeCopyPhoto, Derivatives, includePrivacy, 
includePhotoAlbum, includeSmsFeature, capturePhoto, Photo, Music, includeVideo, captureVideo, simulatePlayVideo, Video, 
includeMMAPI, x_CopyPhotoOrSMS, x_PhotoAlbumOrMusic, x_NotPhotoAlbum, x_notMisc, x_MusicAndVideo, 
x_NotMusic, x_PhotoAlbumAndVideoOrMusic, x_CapturePhotoOrVideo, x_NotPrivacy, x_SMSOrCapturePhoto, x_misc,
x_MusicOrVideo, x_SMSOrCapturePhotoOrCaptureVideo, x_PhotoAlbumOrMusicOrVideo, x_CopyPhotoOrSMSOrCapturePhoto,
x_notSimulatePlayVideo in Bool {}

pred semanticsFM[] {
	isTrue[MobilMedia] and
	(isTrue[MobilMedia] <=> isTrue[NewCompound1]) and

    (isTrue[Media] <=> isTrue[MobilMedia]) and 
    (isTrue[Derivatives] <=> isTrue[MobilMedia]) and 

	(isTrue[includeSorting] => isTrue[MobilMedia]) and
	(isTrue[includeFavourites] => isTrue[MobilMedia]) and
	(isTrue[includeCopyPhoto] => isTrue[MobilMedia]) and
	(isTrue[includePrivacy] => isTrue[MobilMedia]) and

	(isTrue[Media] <=> (isTrue[Photo] or isTrue[Music] or isTrue[Video])) and 

    (isTrue[Photo] <=> isTrue[includePhotoAlbum]) and 
	(isTrue[includeSmsFeature] => isTrue[Photo]) and
	(isTrue[capturePhoto] => isTrue[Photo]) and

    (isTrue[Music] <=> isTrue[includeMMAPI]) and 

    (isTrue[Video] <=> isTrue[includeVideo]) and 
	(isTrue[captureVideo] => isTrue[Video]) and
	(isTrue[simulatePlayVideo] => isTrue[Video]) and

	(isTrue[x_CopyPhotoOrSMS] => isTrue[Derivatives]) and
	(isTrue[x_PhotoAlbumOrMusic] => isTrue[Derivatives]) and
	(isTrue[x_NotPhotoAlbum] => isTrue[Derivatives]) and
	(isTrue[x_notMisc] => isTrue[Derivatives]) and
	(isTrue[x_MusicAndVideo] => isTrue[Derivatives]) and
	(isTrue[x_NotMusic] => isTrue[Derivatives]) and
	(isTrue[x_PhotoAlbumAndVideoOrMusic] => isTrue[Derivatives]) and
	(isTrue[x_CapturePhotoOrVideo] => isTrue[Derivatives]) and
	(isTrue[x_NotPrivacy] => isTrue[Derivatives]) and
	(isTrue[x_SMSOrCapturePhoto] => isTrue[Derivatives]) and
	(isTrue[x_misc] => isTrue[Derivatives]) and
	(isTrue[x_MusicOrVideo] => isTrue[Derivatives]) and
	(isTrue[x_SMSOrCapturePhotoOrCaptureVideo] => isTrue[Derivatives]) and
	(isTrue[x_PhotoAlbumOrMusicOrVideo] => isTrue[Derivatives]) and
	(isTrue[x_CopyPhotoOrSMSOrCapturePhoto] => isTrue[Derivatives]) and
	(isTrue[x_notSimulatePlayVideo] => isTrue[Derivatives]) and

	(isTrue[x_CopyPhotoOrSMS] <=> (isTrue[includeCopyPhoto] or isTrue[includeSmsFeature]))
	(isTrue[x_PhotoAlbumOrMusic] <=> (isTrue[includePhotoAlbum] or isTrue[includeMMAPI]))
	(isTrue[x_NotPhotoAlbum] <=> not (isTrue[includePhotoAlbum]))
	(isTrue[x_NotMusic] <=> not(isTrue[includeMMAPI])) 
	(isTrue[x_misc] <=> (isTrue[includeMMAPI] and isTrue[includePhotoAlbum] or isTrue[includeMMAPI] and isTrue[includeVideo] or isTrue[includeVideo] and isTrue[includePhotoAlbum]))
	(isTrue[x_notMisc] <=> not(isTrue[x_misc]))
	(isTrue[x_PhotoAlbumAndVideoOrMusic] <=> (isTrue[includeMMAPI] and isTrue[includePhotoAlbum] or isTrue[includePhotoAlbum] and isTrue[includeVideo]))
	(isTrue[x_CapturePhotoOrVideo] <=> (isTrue[capturePhoto] or isTrue[captureVideo]))
	(isTrue[x_NotPrivacy] <=> not(isTrue[includePrivacy]))
	(isTrue[x_MusicOrVideo] <=> (isTrue[includeMMAPI] or isTrue[includeVideo]))
	(isTrue[x_SMSOrCapturePhotoOrCaptureVideo] <=> (isTrue[includeSmsFeature] or isTrue[capturePhoto] or isTrue[captureVideo]))
	(isTrue[x_PhotoAlbumOrMusicOrVideo] <=> (isTrue[includePhotoAlbum] or isTrue[includeMMAPI] or isTrue[includeVideo]))
	(isTrue[x_CopyPhotoOrSMSOrCapturePhoto] <=> (isTrue[includeCopyPhoto] or isTrue[includeSmsFeature] or isTrue[capturePhoto]))
	(isTrue[x_notSimulatePlayVideo] <=> not(isTrue[simulatePlayVideo]))
	(isTrue[x_MusicAndVideo] <=> (isTrue[includeMMAPI] and isTrue[includeVideo]))
	(isTrue[x_SMSOrCapturePhoto] <=> (isTrue[includeSmsFeature] or isTrue[capturePhoto]))

}

pred testConfiguration[] {
	//isTrue[GPL] and isTrue[Prog] and isFalse[Benchmark]
	isTrue[MobilMedia]
}

pred verify[] {
	semanticsFM[] and testConfiguration[]
}

run verify for 2

