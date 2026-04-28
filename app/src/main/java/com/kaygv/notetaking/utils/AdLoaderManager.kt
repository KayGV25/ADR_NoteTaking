package com.kaygv.notetaking.utils

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject

class AdLoaderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun loadAds(count: Int): List<NativeAd> =
        suspendCancellableCoroutine { cont ->

            val ads = mutableListOf<NativeAd>()
            var isDone = false

            val adLoader = AdLoader.Builder(
                context,
                AdConfig.ADS_KEY
            )
                .forNativeAd { ad ->
                    if (isDone) {
                        ad.destroy()
                        return@forNativeAd
                    }

                    ads.add(ad)

                    if (ads.size >= count) {
                        isDone = true
                        cont.resume(ads) { cause, _, _ -> }
                    }
                }
                .withAdListener(object : AdListener() {

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        if (!isDone) {
                            isDone = true
                            cont.resume(ads) { cause, _, _ -> }
                        }
                    }
                })
                .build()

            adLoader.loadAds(AdRequest.Builder().build(), count)

            cont.invokeOnCancellation {
                ads.forEach { it.destroy() }
            }
        }
}
