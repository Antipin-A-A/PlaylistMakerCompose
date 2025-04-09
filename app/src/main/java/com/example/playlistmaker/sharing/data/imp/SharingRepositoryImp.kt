package com.example.playlistmaker.sharing.data.imp

import android.content.Context
import com.example.playlistmakercompose.R
import com.example.playlistmaker.sharing.domain.api.reposytory.SharingRepository
import com.example.playlistmaker.sharing.domain.model.EmailData

class SharingRepositoryImp(val context: Context) : SharingRepository {
    override fun getShareAppLink(): String {
        return context.getString(R.string.url_share)
    }

    override fun getSupportEmailData(): EmailData {
        val email = context.getString(R.string.mail_address_user)
        val message = context.getString(R.string.string_email_text)
        val tittle = context.getString(R.string.string_email_tittle)
        return EmailData(email, message, tittle)
    }

    override fun getTermsLink(): String {
        return context.getString(R.string.url_arrow)
    }
}