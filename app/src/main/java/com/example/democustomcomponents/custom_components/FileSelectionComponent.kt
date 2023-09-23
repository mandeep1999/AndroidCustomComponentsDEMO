package com.example.democustomcomponents.custom_components

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.OpenableColumns
import android.util.AttributeSet
import android.view.LayoutInflater
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.example.democustomcomponents.R
import com.example.democustomcomponents.databinding.CustomFileSelectionBinding


class FileSelectionComponent : ConstraintLayout {

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null, 0)

    private val mimeTypes = arrayOf(
        "application/pdf",
        "application/msword",
        "application/ms-doc",
        "application/doc",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )

    private var activity: AppCompatActivity? = null
    private var fileSelectedCallback: ((uri: Uri?) -> Unit)? = null
    private var fileSelectedUriLiveData: MutableLiveData<Uri?> = MutableLiveData()

    private var binding: CustomFileSelectionBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.custom_file_selection,
        this,
        true
    )

    private var getContent: ActivityResultLauncher<Array<String>>? = null

    private fun initialiseLauncher(
        fileSelectedCallback: ((uri: Uri?) -> Unit)?,
    ) {
        getContent =
            activity?.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                if (uri != null) {
                    fileSelectedCallback?.invoke(uri)
                    fileSelectedUriLiveData.postValue(uri)
                }
            }
    }

    fun configure(titleText: String?, titleTextAppearance: Int?, subTitleText: String?, subTitleTextAppearance: Int?, selectionBoxIcon: Drawable?, subTitleDescriptionIcon: Drawable?, supportedFileTypes: List<String>?){
        setTitleText(text = titleText, textAppearance =  titleTextAppearance)
        setSubtitleText(text = subTitleText, textAppearance = subTitleTextAppearance)
        selectionBoxIcon?.let { setSelectionBoxIcon(drawable = it) }
        subTitleDescriptionIcon?.let { setSubtitleIcon(drawable = it) }
        setSupportedFilesText(supportedFileTypes)
    }

    fun initialise(supportedFileTypes: Array<String>?, appCompatActivity: AppCompatActivity, fileSelectedCallback: ((uri: Uri?) -> Unit)?){
        this.fileSelectedCallback = fileSelectedCallback
        this.activity = appCompatActivity
        initialiseLauncher(fileSelectedCallback)
        initialiseViews(supportedFileTypes)
        initialiseObservers()
    }

    private fun initialiseViews(supportedFileTypes: Array<String>?) {
        binding.selectionBox.setOnClickListener {
            if((supportedFileTypes?.size ?: 0) > 0){
                getContent?.launch(supportedFileTypes)
            } else {
                getContent?.launch(mimeTypes)
            }
        }
        binding.clearIcon.setOnClickListener {
            fileSelectedUriLiveData.postValue(null)
            this.fileSelectedCallback?.invoke(null)
        }
    }

    private fun initialiseObservers(){
        activity?.let {
            fileSelectedUriLiveData.observe(it) {uri ->
                if(uri == null){
                    binding.selectedFileCl.visibility = GONE
                }
                else {
                    binding.selectedFileCl.visibility = VISIBLE
                    setFileName(uri)
                }
            }
        }
    }

    private fun setFileName(uri: Uri){
        val fileName = getFileName(uri) + "." + getFileExtension(uri)
        binding.fileNameTextView.text = fileName
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        val contentResolver: ContentResolver = context.contentResolver

        // Get the file name from the content resolver
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                fileName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }

        return fileName
    }

    private fun getFileExtension( uri: Uri): String? {
        val contentResolver = context.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        var extension: String? = null

        // Get the MIME type from the content resolver
        val mimeType = contentResolver.getType(uri)
        if (mimeType != null) {
            extension = mimeTypeMap.getExtensionFromMimeType(mimeType)
        }
        return extension
    }



    private fun setTitleText(text: String?, textAppearance: Int?){
        if(text == null){
            binding.selectionBoxTitle.visibility = GONE
        }else {
            binding.selectionBoxTitle.visibility = VISIBLE
            binding.selectionBoxTitle.text = text
            textAppearance?.let {
                binding.selectionBoxTitle.setTextAppearance(it)
            }
        }
    }

    private fun setSubtitleText(text: String?, textAppearance: Int?){
        if(text == null){
            binding.selectionBoxSubtitle.visibility = GONE
        }else {
            binding.selectionBoxSubtitle.visibility = VISIBLE
            binding.selectionBoxSubtitle.text = text
            textAppearance?.let {
                binding.selectionBoxSubtitle.setTextAppearance(it)
            }
        }
    }

    private fun setSelectionBoxIcon(drawable: Drawable){
        binding.selectionBoxIcon.background = drawable
    }

    private fun setSubtitleIcon(drawable: Drawable){
        binding.selectionBoxSubtitleIcon.background = drawable
    }

    private fun setSupportedFilesText(list: List<String>?){
        if(list == null){
            binding.supportedFileTypesTextView.visibility = GONE
        }else {
            val supportedFileTypesString = "Supported File Types " + list.joinToString(",")
            binding.supportedFileTypesTextView.visibility = VISIBLE
            binding.supportedFileTypesTextView.text = supportedFileTypesString
        }
    }


}