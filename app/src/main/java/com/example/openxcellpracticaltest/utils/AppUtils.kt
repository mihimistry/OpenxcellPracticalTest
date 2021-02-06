package com.example.openxcellpracticaltest.utils

import android.content.Context
import android.net.ConnectivityManager
import com.kaopiz.kprogresshud.KProgressHUD

class AppUtils {
    companion object {
        private var progressHUD: KProgressHUD? = null

        fun showProgress(context: Context, title: String, desc: String?, cancelable: Boolean) {
            progressHUD =
                KProgressHUD(context).setLabel(title).setCancellable(cancelable)
                    .setAnimationSpeed(2)
                    .setDimAmount(0.5f)
            if (desc == null) progressHUD?.setDetailsLabel("please wait") else progressHUD?.setDetailsLabel(
                desc
            )
            if (isNetworkAvailable(context)) progressHUD?.show()
        }

        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }


        fun dismissProgress() {
            progressHUD?.let {
                if (it.isShowing) it.dismiss()
            }
        }
    }
}