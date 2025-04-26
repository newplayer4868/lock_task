package ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.cap.locktask.R
import android.graphics.drawable.ColorDrawable
import android.graphics.Color
import android.widget.Button
import android.widget.TextView
import model.Preset

class PopupDialogFragment(private val preset: Preset) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_popup)

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        dialog.findViewById<Button>(R.id.closeButton)?.setOnClickListener {
            dismiss()
        }

        val infoTextView = dialog.findViewById<TextView>(R.id.presetInfoTextView)
        infoTextView?.text = buildPresetInfo(preset)

        return dialog
    }

    fun getAppNameFromPackage(context: Context, packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName // 실패하면 그냥 패키지명 리턴
        }
    }

    private fun buildPresetInfo(preset: Preset): String {
        val name = preset.name
        val lockType = preset.lockType ?: "설정되지 않음"
        val startTime = preset.startTime ?: "설정되지 않음"
        val endTime = preset.endTime ?: "설정되지 않음"
        val time =preset.Time?:"설정되지 않음"
        val description=preset.description?:"설정되지 않음"

        val appNames = preset.selectedApps?.joinToString(", ") { packageName ->
            getAppNameFromPackage(requireContext(), packageName)
        } ?: "설정되지 않음"

        if(lockType=="목적지 잠금 해제")
        {
            return """
        프리셋 이름: $name
        잠금 형태: $lockType
        활성화 기간: $startTime ~ $endTime
        목적지 머물 시간: $time
        설정 어플: $appNames
        잠금 설명: $description
        
    """.trimIndent()
        }
        if(lockType=="목적지 잠금"||lockType=="기간 잠금")
        {
            return """
        프리셋 이름: $name
        잠금 형태: $lockType
        활성화 기간: $startTime ~ $endTime
        설정 어플: $appNames
        
        잠금 설명: $description
    """.trimIndent()
        }

        if (lockType=="어플 사용량 잠금")
        {
            return """
        프리셋 이름: $name
        잠금 형태: $lockType
        어플 사용량: $time
        설정 어플: $appNames
        
        잠금 설명: $description
    """.trimIndent()
        }

        if (lockType=="어플 할당량 잠금")
        {
            return """
        프리셋 이름: $name
        잠금 형태: $lockType        
        어플 할당량: $time
        설정 어플: $appNames
       
        잠금 설명: $description
    """.trimIndent()
        }

        return """
        프리셋 이름: $name
        잠금 형태: $lockType
        잠금 설명: $description  
        설정 어플: $appNames
    """.trimIndent()
    }

}

