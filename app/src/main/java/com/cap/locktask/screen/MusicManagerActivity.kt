package com.cap.locktask.screen

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cap.locktask.R
import com.cap.locktask.utils.SharedPreferencesUtils
import java.io.File

class MusicManagerActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private var mediaPlayer: MediaPlayer? = null
    private var currentlyPlayingFile: File? = null
    private var currentPlayButton: Button? = null

    private val pickAudioLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { saveMusicToInternal(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mu_activity_music_main)

        container = findViewById(R.id.musicContainer)

        findViewById<Button>(R.id.selectMusicButton).setOnClickListener {
            pickAudioLauncher.launch("audio/*")
        }
        findViewById<Button>(R.id.mu_completeButton).setOnClickListener {
            finish()
        }
        SharedPreferencesUtils.cleanUpInvalidMusicFiles(this)
        loadMusicFiles()
    }

    private fun saveMusicToInternal(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)

        // 파일 이름 추출 (원본 이름 사용)
        val fileName = getFileNameFromUri(uri)

        if (SharedPreferencesUtils.isMusicFileNameExists(this, fileName)) {
            Toast.makeText(this, "⚠️ 이미 동일한 이름의 음원이 존재합니다", Toast.LENGTH_SHORT).show()
            return
        }

        // 저장할 파일 경로 지정
        val file = File(filesDir, fileName)

        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        SharedPreferencesUtils.saveMusicFile(this, file.absolutePath)
        loadMusicFiles()
    }


    private fun getFileNameFromUri(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = it.getString(index)
                    }
                }
            }
        }

        // fallback
        if (result == null) {
            result = uri.path?.substringAfterLast('/') ?: "music_${System.currentTimeMillis()}.mp3"
        }

        return result ?: "music_${System.currentTimeMillis()}.mp3"
    }

    private fun loadMusicFiles() {
        container.removeAllViews()
        val filePaths = SharedPreferencesUtils.getSavedMusicFiles(this)

        if (filePaths.isEmpty()) {
            Toast.makeText(this, "저장된 음원이 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        filePaths.sorted().forEach { path ->
            val file = File(path)
            if (!file.exists())
            {
                return@forEach // 혹시 삭제된 파일 있을 수 있음
            }

            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(16, 16, 16, 16)
            }

            val nameTextView = TextView(this).apply {
                text = file.name
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                textSize = 16f
            }

            val playButton = Button(this).apply {
                text = "▶"
                setOnClickListener {
                    if (currentlyPlayingFile == file && mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.pause()
                        text = "▶"
                    } else {
                        mediaPlayer?.release()
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(file.absolutePath)
                            prepare()
                            start()
                            setOnCompletionListener {
                                text = "▶"
                            }
                        }
                        currentlyPlayingFile = file
                        currentPlayButton?.text = "▶"
                        currentPlayButton = this
                        text = "⏸"
                    }
                }
            }

            val deleteButton = Button(this).apply {
                text = "삭제"
                setOnClickListener {
                    if (file == currentlyPlayingFile) {
                        mediaPlayer?.release()
                        mediaPlayer = null
                        currentlyPlayingFile = null
                        currentPlayButton?.text = "▶"
                    }
                    file.delete()
                    SharedPreferencesUtils.deleteMusicFile(this@MusicManagerActivity, file.absolutePath)
                    loadMusicFiles()
                    Toast.makeText(this@MusicManagerActivity, "삭제됨", Toast.LENGTH_SHORT).show()
                }
            }

            layout.addView(nameTextView)
            layout.addView(playButton)
            layout.addView(deleteButton)
            container.addView(layout)
        }
    }


    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }
}
