package com.example.locus_assignment.model

import android.net.Uri


data class ImageData(
    var choice : Int,
    var toggle:Boolean,
    var comment: String,
    var imageUri : Uri?)