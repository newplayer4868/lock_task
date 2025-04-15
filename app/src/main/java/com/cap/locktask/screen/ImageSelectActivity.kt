package com.cap.locktask.screen


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cap.locktask.R
import com.cap.locktask.utils.SharedPreferencesUtils
import com.yalantis.ucrop.UCrop
import java.io.File

class ImageSelectActivity : AppCompatActivity() {

    private lateinit var imageGrid: GridLayout
    private val imageFiles = mutableListOf<File>()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { startCrop(it) }
    }

    private fun startCrop(sourceUri: Uri) {
        val destUri = Uri.fromFile(File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
        UCrop.of(sourceUri, destUri)
            .withAspectRatio(9f, 16f)
            .withMaxResultSize(1080, 1920)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val uri = UCrop.getOutput(data!!) ?: return
            val file = saveToInternalStorage(uri)
            imageFiles.add(file)
            refreshGrid()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(this, "크롭 실패", Toast.LENGTH_SHORT).show()
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun saveToInternalStorage(uri: Uri): File {
        val file = File(filesDir, "image_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }

        SharedPreferencesUtils.saveCroppedImagePath(this, file.absolutePath)
        return file
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.i_activity_image_select)

        imageGrid = findViewById(R.id.imageGrid)

        findViewById<Button>(R.id.addImageButton).setOnClickListener {
            if (imageFiles.size >= 4) {
                Toast.makeText(this, "이미지는 최대 4개까지 가능합니다", Toast.LENGTH_SHORT).show()
            } else {
                pickImageLauncher.launch("image/*")
            }
        }

        findViewById<Button>(R.id.completeButton).setOnClickListener {
            Toast.makeText(this, "이미지 저장 완료 (${imageFiles.size}개)", Toast.LENGTH_SHORT).show()
            finish()
        }

        SharedPreferencesUtils.loadCroppedImagePath(this)?.let { path ->

            val file = File(path)
            if (file.exists() && !imageFiles.contains(file)) {
                imageFiles.add(file)
            }

        }

        loadSavedImages()
        refreshGrid()
    }



    private fun loadSavedImages() {
        imageFiles.clear()
        filesDir.listFiles()?.filter {
            it.name.startsWith("image_") && it.name.endsWith(".jpg")
        }?.sorted()?.let {
            imageFiles.addAll(it)
        }
    }

    private fun refreshGrid() {
        imageGrid.removeAllViews()

        val inflater = LayoutInflater.from(this)
        imageFiles.forEachIndexed { index, file ->
            val itemView = inflater.inflate(R.layout.i_item_image_preview, imageGrid, false)
            val imageView = itemView.findViewById<ImageView>(R.id.imagePreview)
            val deleteBtn = itemView.findViewById<ImageButton>(R.id.deleteButton)

            imageView.setImageURI(Uri.fromFile(file))

            deleteBtn.setOnClickListener {
                file.delete()
                imageFiles.removeAt(index)

                val savedPath = SharedPreferencesUtils.loadCroppedImagePath(this)
                if (savedPath == file.absolutePath) {
                    SharedPreferencesUtils.deleteSavedCroppedImage(this)
                }

                refreshGrid()
            }

            imageGrid.addView(itemView)
        }
    }
}
