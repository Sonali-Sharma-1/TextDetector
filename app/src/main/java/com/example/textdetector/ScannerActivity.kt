package com.example.textdetector

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.textdetector.databinding.ActivityScannerBinding
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import java.lang.StringBuilder

class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var imageBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListener()
    }

    /**
     * Set listener to handle button clicks.
     */
    private fun setListener() {
        binding.btnSnap.setOnClickListener {
            if (ifCameraPermissionGranted())
                openCamera()
            else
                requestPermissionForCamera()
        }

        binding.btnDetect.setOnClickListener {
            detectTextFromImage()
        }
    }


    /**
     * Start camera on the app.
     */
    private fun openCamera() =
        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_IMAGE_CAPTURE)


    /**
     * Extract the image captured by camera and set it in UI.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
            binding.captureImage.setImageBitmap(data?.extras?.get("data") as Bitmap)
    }

    /**
     * Detect Text from Image.
     */
    private fun detectTextFromImage() {
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result: Task<Text> =
            textRecognizer.process(InputImage.fromBitmap(imageBitmap, 0)).addOnSuccessListener {
                val stringBuilderText = StringBuilder()
                for (block in it.textBlocks) {
                    val blockText = block.text
                    for (line in block.lines) {
                        for (element in line.elements) {
                            val elementText = element.text
                            stringBuilderText.append(elementText)
                        }
                        binding.txtResult.text = blockText
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Fail to detect text from image", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Check if camera permission is granted or not.
     */
    private fun ifCameraPermissionGranted(): Boolean = (ContextCompat.checkSelfPermission(
        applicationContext,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED)

    /**
     * Request camera permission if not granted.
     */
    private fun requestPermissionForCamera() {
        val PERMISSION_CODE = 200
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            PERMISSION_CODE
        )
    }

    /**
     * Show success/failure toast according to permission given.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Granted....", Toast.LENGTH_SHORT).show()
            openCamera()
        } else
            Toast.makeText(this, "Permission Denied....", Toast.LENGTH_SHORT).show()
    }
}