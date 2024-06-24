package com.example.heart_failure

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SimulasiFragment : Fragment() {

    private lateinit var interpreter: Interpreter
    private val mModelPath = "heart_failure.tflite"

    private lateinit var resultText: TextView
    private lateinit var edtAge: EditText
    private lateinit var edtSex: EditText
    private lateinit var edtChestPainType: EditText
    private lateinit var edtRestingBP: EditText
    private lateinit var edtCholesterol: EditText
    private lateinit var edtFastingBS: EditText
    private lateinit var edtRestingECG: EditText
    private lateinit var edtMaxHR: EditText
    private lateinit var edtExerciseAngina: EditText
    private lateinit var edtOldpeak: EditText
    private lateinit var edtSTSlope: EditText
    private lateinit var checkButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_simulasi, container, false)

        resultText = view.findViewById(R.id.txtResult)
        edtAge = view.findViewById(R.id.editAge)
        edtSex = view.findViewById(R.id.editSex)
        edtChestPainType = view.findViewById(R.id.editChestPainType)
        edtRestingBP = view.findViewById(R.id.editRestingBP)
        edtCholesterol = view.findViewById(R.id.editCholesterol)
        edtFastingBS = view.findViewById(R.id.editFastingBS)
        edtRestingECG = view.findViewById(R.id.editRestingECG)
        edtMaxHR = view.findViewById(R.id.editMaxHR)
        edtExerciseAngina = view.findViewById(R.id.editExerciseAngina)
        edtOldpeak = view.findViewById(R.id.editOldpeak)
        edtSTSlope = view.findViewById(R.id.editSTSlope)
        checkButton = view.findViewById(R.id.btnCheck)

        checkButton.setOnClickListener {
            if (validateInputs()) {
                try {
                    val result = doInference(
                        edtAge.text.toString(),
                        edtSex.text.toString(),
                        edtChestPainType.text.toString(),
                        edtRestingBP.text.toString(),
                        edtCholesterol.text.toString(),
                        edtFastingBS.text.toString(),
                        edtRestingECG.text.toString(),
                        edtMaxHR.text.toString(),
                        edtExerciseAngina.text.toString(),
                        edtOldpeak.text.toString(),
                        edtSTSlope.text.toString()
                    )
                    activity?.runOnUiThread {
                        resultText.text = if (result == 0) "Pasien Normal" else "Pasien Terkena Gagal Jantung"
                    }
                } catch (e: Exception) {
                    resultText.text = "Prediksi Gagal: ${e.message}"
                }
            } else {
                resultText.text = "Isi Semua kolom input!"
            }
        }
        initInterpreter()
        return view
    }

    private fun validateInputs(): Boolean {
        return edtAge.text.isNotEmpty() &&
                edtSex.text.isNotEmpty() &&
                edtChestPainType.text.isNotEmpty() &&
                edtRestingBP.text.isNotEmpty() &&
                edtCholesterol.text.isNotEmpty() &&
                edtFastingBS.text.isNotEmpty() &&
                edtRestingECG.text.isNotEmpty() &&
                edtMaxHR.text.isNotEmpty() &&
                edtExerciseAngina.text.isNotEmpty() &&
                edtOldpeak.text.isNotEmpty() &&
                edtSTSlope.text.isNotEmpty()
    }

    private fun initInterpreter() {
        val options = Interpreter.Options()
        options.setNumThreads(5)
        options.setUseNNAPI(true)
        interpreter = Interpreter(loadModelFile(requireContext().assets, mModelPath), options)
    }

    private fun doInference(
        input1: String, input2: String, input3: String, input4: String,
        input5: String, input6: String, input7: String, input8: String,
        input9: String, input10: String, input11: String
    ): Int {
        val inputVal = FloatArray(11)
        inputVal[0] = input1.toFloat()
        inputVal[1] = input2.toFloat()
        inputVal[2] = input3.toFloat()
        inputVal[3] = input4.toFloat()
        inputVal[4] = input5.toFloat()
        inputVal[5] = input6.toFloat()
        inputVal[6] = input7.toFloat()
        inputVal[7] = input8.toFloat()
        inputVal[8] = input9.toFloat()
        inputVal[9] = input10.toFloat()
        inputVal[10] = input11.toFloat()
        val output = Array(1) { FloatArray(2) }
        interpreter.run(inputVal, output)
        Log.e("result", (output[0].toList() + " ").toString())
        return output[0].indexOfFirst { it == output[0].maxOrNull() }
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}
