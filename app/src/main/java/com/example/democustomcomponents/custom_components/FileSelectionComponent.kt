package com.example.democustomcomponents.custom_components

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.AttributeSet
import android.view.LayoutInflater
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.example.democustomcomponents.R
import com.example.democustomcomponents.databinding.CustomFileSelectionBinding
import java.util.Locale


class FileSelectionComponent : ConstraintLayout {

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs, defStyle)
    }

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

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.FileSelectionComponent, defStyle, 0)
        this.setTitleText(attributes.getString(R.styleable.FileSelectionComponent_title_text))
        this.setTitleTextColor(
            attributes.getColor(
                R.styleable.FileSelectionComponent_title_text_color,
                Color.parseColor("#d3d3d3")
            )
        )
        this.setTitleTextFontSize(
            attributes.getFloat(
                R.styleable.FileSelectionComponent_title_text_size,
                20.0f
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.setTitleTextTypeFace(attributes.getFont(R.styleable.FileSelectionComponent_title_text_typeface))
        }
        this.setDescriptionText(attributes.getString(R.styleable.FileSelectionComponent_description_text))
        this.setDescriptionTextColor(
            attributes.getColor(
                R.styleable.FileSelectionComponent_description_text_color,
                Color.parseColor("#d3d3d3")
            )
        )
        this.setDescriptionTextFontSize(
            attributes.getFloat(
                R.styleable.FileSelectionComponent_description_text_size,
                18.0f
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.setDescriptionTextTypeFace(attributes.getFont(R.styleable.FileSelectionComponent_description_text_typeface))
        }
        attributes.getDrawable(R.styleable.FileSelectionComponent_selection_box_background)
            ?.let { this.setSelectionBoxBackground(it) }
        attributes.getDrawable(R.styleable.FileSelectionComponent_selection_box_icon)?.let {
            this.setSelectionBoxIcon(it)
        }
        attributes.getDrawable(R.styleable.FileSelectionComponent_description_icon)?.let {
            this.setSelectionBoxDescriptionIcon(it)
        }
        this.setSupportedFilesTextString(attributes.getString(R.styleable.FileSelectionComponent_supported_types_text))
        attributes.recycle()
    }

    fun initialise(
        supportedFileTypes: Array<String>?,
        appCompatActivity: AppCompatActivity,
        fileSelectedCallback: ((uri: Uri?) -> Unit)?
    ) {
        this.fileSelectedCallback = fileSelectedCallback
        this.activity = appCompatActivity
        initialiseLauncher(fileSelectedCallback)
        initialiseViews(supportedFileTypes)
        initialiseObservers()
    }

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

    private fun initialiseViews(supportedFileTypes: Array<String>?) {
        binding.selectionBox.setOnClickListener {
            if ((supportedFileTypes?.size ?: 0) > 0) {
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

    private fun initialiseObservers() {
        activity?.let {
            fileSelectedUriLiveData.observe(it) { uri ->
                if (uri == null) {
                    binding.selectedFileCl.visibility = GONE
                } else {
                    binding.selectedFileCl.visibility = VISIBLE
                    setFileName(uri)
                }
            }
        }
    }

    private fun setFileName(uri: Uri) {
        val fileName = getFileName(uri)
        binding.fileNameTextView.text = fileName?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        }
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

    fun setTitleText(text: String?) {
        if (text == null)
            binding.selectionBoxTitle.visibility = GONE
        text?.let {
            binding.selectionBoxTitle.visibility = VISIBLE
            binding.selectionBoxTitle.text = it
        }
    }

    fun setTitleTextColor(color: Int) {
        binding.selectionBoxTitle.setTextColor(color)
    }

    fun setTitleTextTypeFace(tf: Typeface?) {
        binding.selectionBoxTitle.typeface = tf
    }

    fun setTitleTextFontSize(textSize: Float) {
        binding.selectionBoxTitle.textSize = textSize
    }

    fun setDescriptionText(text: String?) {
        if (text == null)
            binding.selectionBoxDescriptionTextView.visibility = GONE
        text?.let {
            binding.selectionBoxDescriptionTextView.visibility = VISIBLE
            binding.selectionBoxDescriptionTextView.text = it
        }
    }

    fun setDescriptionTextColor(color: Int) {
        binding.selectionBoxDescriptionTextView.setTextColor(color)
    }

    fun setDescriptionTextTypeFace(tf: Typeface?) {
        binding.selectionBoxDescriptionTextView.typeface = tf
    }

    fun setDescriptionTextFontSize(textSize: Float) {
        binding.selectionBoxDescriptionTextView.textSize = textSize
    }

    fun setSelectionBoxIcon(drawable: Drawable) {
        binding.selectionBoxIcon.setImageDrawable(drawable)
    }

    fun setSelectionBoxDescriptionIcon(drawable: Drawable) {
        binding.selectionBoxDescriptionIcon.setImageDrawable(drawable)
    }

    fun setSelectionBoxBackground(drawable: Drawable) {
        binding.selectionBox.background = drawable
    }

    fun setSupportedFilesTextList(list: List<String>?) {
        if (list == null) {
            binding.supportedFileTypesTextView.visibility = GONE
        } else {
            val supportedFileTypesString = "Supported File Types " + list.joinToString(",")
            binding.supportedFileTypesTextView.visibility = VISIBLE
            binding.supportedFileTypesTextView.text = supportedFileTypesString
        }
    }

    private fun setSupportedFilesTextString(types: String?) {
        if (types == null) {
            binding.supportedFileTypesTextView.visibility = GONE
        } else {
            binding.supportedFileTypesTextView.visibility = VISIBLE
            binding.supportedFileTypesTextView.text = types
        }
    }

}