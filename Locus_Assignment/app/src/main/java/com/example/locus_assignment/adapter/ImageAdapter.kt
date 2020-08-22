package com.example.locus_assignment.adapter

import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.locus_assignment.R
import com.example.locus_assignment.model.ImageResponseItem
import com.example.locus_assignment.view.MainActivity
import kotlinx.android.synthetic.main.single_item.view.*
import java.util.ArrayList

class ImageAdapter(
    private val context: MainActivity,
    private val itemList: List<ImageResponseItem>,
    private val onSaveListener: SaveTextListener
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {


    private lateinit var mPreviewIv : ImageView
    private lateinit var mCloseIv : ImageView
    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun showCorrespondingUI(imageRes: ImageResponseItem, pos: Int) {

            val type = imageRes.type
            // Condition to show the item of PHOTO type
            when (type) {
                "PHOTO" -> {
                    itemView.photo_layout.visibility = View.VISIBLE
                    itemView.choice_layout.visibility = View.GONE
                    itemView.comment_layout.visibility = View.GONE

                    val photoTitle: TextView    = itemView.findViewById(R.id.photo_title)
                    mPreviewIv                  = itemView.findViewById(R.id.mPreviewIv)
                    mCloseIv                    = itemView.findViewById(R.id.mCloseIv)

                    photoTitle.text = imageRes.title

                    val imageURI = context.imageDataList.get(pos).imageUri
                    imageURI?.setImage(imageURI) ?:setPlaceholder()
                    mPreviewIv.setOnClickListener {
                        context.imageDataList[pos]?.imageUri?.showLargeImage(context.imageDataList[pos]?.imageUri!!) ?: onSaveListener.openCamera(pos)
                    }
                    mCloseIv.setOnClickListener {
                            onSaveListener.clearDrawable(pos, mCloseIv)
                    }
                }
                // Condition to show the item of SINGLE_CHOICE type
                "SINGLE_CHOICE" -> {
                    itemView.photo_layout.visibility = View.GONE
                    itemView.choice_layout.visibility = View.VISIBLE
                    itemView.comment_layout.visibility = View.GONE

                    val radioGroup: RadioGroup = itemView.findViewById(R.id.radio_group)
                    val choiceTitle: TextView = itemView.findViewById(R.id.choice_title)

                    choiceTitle.text = imageRes.title
                    val options = imageRes.dataMap.options
                    options?.let {
                        if (!options.isEmpty()) {
                            createRadioButton(pos, options, radioGroup)
                        }
                    }
                }
                // Condition to show the item of COMMENT type
                "COMMENT" -> {
                    itemView.photo_layout.visibility = View.GONE
                    itemView.choice_layout.visibility = View.GONE
                    itemView.comment_layout.visibility = View.VISIBLE

                    val toggle: ToggleButton = itemView.findViewById(R.id.toggle)
                    val comment: EditText = itemView.findViewById(R.id.comment)
//                    comment.focusable(false)
                    toggle.isChecked = context.imageDataList[pos]!!.toggle
                    comment.setText(context.imageDataList[pos]!!.comment)
                    val isToggleOn = context.imageDataList[pos]!!.toggle
                    if (isToggleOn)
                        comment.visibility = View.VISIBLE
                    else comment.visibility = View.GONE

                    toggle.setOnCheckedChangeListener { compoundButton, isChecked ->
                        if (!isChecked) {
                            comment.visibility = View.GONE
                            context.imageDataList[pos]!!.toggle = false
                        } else {
                            comment.visibility = View.VISIBLE
                            context.imageDataList[pos]!!.toggle = true
                        }
                    }
                    comment.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(p0: Editable?) {
                            if (!p0!!.isEmpty()) {
                                context.imageDataList[pos]!!.comment = p0.toString()
                            }
                        }

                        override fun beforeTextChanged(
                            p0: CharSequence?,
                            p1: Int,
                            p2: Int,
                            p3: Int
                        ) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }
                    })
                }
            }
        }
    }



    private fun Uri.showLargeImage(imageUri: Uri){
        context.showLargeImage(imageUri)
    }

    // Dynamically create RadioButton and add to Radio Group
    private fun createRadioButton(pos: Int, options: ArrayList<String>, radioGroup: RadioGroup) {
        val radioButton = arrayOfNulls<RadioButton>(options.size)
        radioGroup.removeAllViews()
        for (i in 0..options.size - 1) {
            radioButton[i] = RadioButton(context)
            radioButton[i]?.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            radioButton[i]?.setText(options[i])
            radioButton[i]?.id = i
            if (context.imageDataList[pos]!!.choice == i) {
                radioButton[i]?.isChecked = true
            }

            radioGroup.addView(radioButton[i])

        }
        radioGroup.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { radioGroup, checkId ->
                context.imageDataList[pos]!!.choice = checkId
                val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkId)
                checkedRadioButton?.let {
                    context.imageDataList[pos]!!.comment =    it.text.toString()
                }

            }
        )
    }
    // set Image to ImageView
    private fun Uri?.setImage(imageUri: Uri){
        mPreviewIv.setImageURI(imageUri)
        mCloseIv.visibility = View.VISIBLE
    }

    // Set the default placeholder to ImageView
    private fun setPlaceholder() {
        mPreviewIv.setImageResource(R.drawable.placeholder)
        mCloseIv.visibility = View.GONE
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        hasStableIds()
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.single_item, parent, false)
        return ImageViewHolder(itemView)
    }

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {

        val currentItem = itemList[position]
        holder.showCorrespondingUI(currentItem, position)
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    // Create interface to pass the callback to mainActivity
    interface SaveTextListener {
        fun openCamera(position: Int)

        fun clearDrawable(position: Int, mCloseIv: ImageView)
    }
}


