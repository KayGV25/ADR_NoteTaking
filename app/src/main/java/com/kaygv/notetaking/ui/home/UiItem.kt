package com.kaygv.notetaking.ui.home

import com.google.android.gms.ads.nativead.NativeAd
import com.kaygv.notetaking.domain.model.Note

sealed class UiItem {
    data class Header(val title: String): UiItem()
    data class NoteItem(val note: Note): UiItem()
    data class AdItem(val ad: NativeAd): UiItem()
}