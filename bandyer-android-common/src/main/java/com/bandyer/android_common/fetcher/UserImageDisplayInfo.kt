package com.bandyer.android_common.fetcher

import android.graphics.Bitmap

/**
 * This class represents some user visual display info.
 * A user image display info can be created with a bitmap or valid url to retrieve image or a valid resId.
 * @property userAlias user associated user alias.
 * @property resId bundle resId representing user.
 * @property imageUrl image url representing user.
 * @property imageBitmap image bitmap representing user.
 * @constructor
 */
open class UserImageDisplayInfo internal constructor(val alias: String) {

    /**
     * This class creates a UserImageDisplayIngo object wich represents some user visual display info.
     * A user image display info can be created with a bitmap or valid url to retrieve image or a valid resId.
     * @property userAlias String
     * @property resId Int?
     * @property imageUrl String?
     * @property imageBitmap Bitmap?
     */
    class Builder(private var userAlias: String) {

        private var resId: Int? = null
        private var imageUrl: String? = null
        private var imageBitmap: Bitmap? = null

        /**
         * ResId of drawable representing the user.
         * @param resId Int
         * @return Builder
         */
        fun withResId(resId: Int): UserImageDisplayInfo {
            this.imageUrl = null
            this.resId = resId
            this.imageBitmap = null
            return buildImageResIdInfo()
        }

        /**
         * ImageUrl for fetching user's visual representation.
         * @param imageUrl String
         * @return Builder
         */
        fun withImageUrl(imageUrl: String): UserImageDisplayInfo {
            this.imageUrl = imageUrl
            this.resId = null
            this.imageBitmap = null
            return buildImageUrlInfo()
        }

        /**
         * Bitmap representing the user.
         * @param bitmap Bitmap
         * @return Builder
         */
        fun withImageBitmap(bitmap: Bitmap): UserImageDisplayInfo {
            this.imageUrl = null
            this.resId = null
            this.imageBitmap = bitmap
            return buildImageBitmapInfo()
        }

        /**
         * Creates UserImageBitmapDisplayInfo object.
         * @return UserImageBitmapDisplayInfo
         */
        private fun buildImageBitmapInfo(): UserImageBitmapDisplayInfo {
            return UserImageBitmapDisplayInfo(this.userAlias, this.imageBitmap!!)
        }

        /**
         * Creates UserImageUrlDisplayInfo object.
         * @return UserImageUrlDisplayInfo
         */
        private fun buildImageUrlInfo(): UserImageUrlDisplayInfo {
            return UserImageUrlDisplayInfo(this.userAlias, this.imageUrl!!)
        }

        /**
         * Creates UserImageResIdDisplayInfo object.
         * @return UserImageResIdDisplayInfo
         */
        private fun buildImageResIdInfo(): UserImageResIdDisplayInfo {
            return UserImageResIdDisplayInfo(this.userAlias, this.resId!!)
        }
    }
}

data class UserImageBitmapDisplayInfo(val userAlias:String, val bitmap: Bitmap): UserImageDisplayInfo(userAlias)
data class UserImageUrlDisplayInfo(val userAlias:String, val url: String): UserImageDisplayInfo(userAlias)
data class UserImageResIdDisplayInfo(val userAlias:String, val resId: Int): UserImageDisplayInfo(userAlias)