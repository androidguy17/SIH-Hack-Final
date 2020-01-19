package com.varun.afinal

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.text.format.DateUtils
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.anychart.graphics.vector.Stroke
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private var tts: TextToSpeech? = null
    private var speechRecog: SpeechRecognizer? = null
    var  ref:FirebaseDatabase?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        charts()
        permission()


        button.setOnClickListener(View.OnClickListener {

            // Permission has already been granted

            speak("I am Ready")
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            speechRecog!!.startListening(intent)

        })
        initializeTextToSpeech()
        initializeSpeechRecognizer()

        firebase()

    }


    private fun speak(message: String) {
        if (Build.VERSION.SDK_INT >= 21) {
            tts!!.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            tts!!.speak(message, TextToSpeech.QUEUE_FLUSH, null)
        }
    }



    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecog = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecog?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {}
                override fun onResults(results: Bundle) {
                    val result_arr: List<String>? =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    processResult(result_arr!![0])
                }

                override fun onPartialResults(partialResults: Bundle) {}
                override fun onEvent(eventType: Int, params: Bundle) {}
            })
        }
    }

    private fun initializeTextToSpeech() {
        tts = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (tts!!.engines.size == 0) {
                Toast.makeText(
                    this@MainActivity,
                    "There were no Text to Speech Engines located in the device",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            } else {
                tts!!.language = Locale.US
                speak("Hello there, I am ready to start our conversation")
            }
        })
    }

    private fun processResult(result_message: String) {
        var result_message = result_message
        result_message = result_message.toLowerCase()


        //        Handle at least four sample cases
//        First: What is your Name?
//        Second: What is the time?
//        Third: Is the earth flat or a sphere?
//        Fourth: Open a browser and open url
        if (result_message.indexOf("what") != -1) {
            if (result_message.indexOf("your name") != -1) {
                speak("My Name is Mr.Android. Nice to meet you!")


            }
            if (result_message.indexOf("time") != -1) {
                val time_now = DateUtils.formatDateTime(
                    this,
                    Date().time,
                    DateUtils.FORMAT_SHOW_TIME
                )
                speak("The time is now: $time_now")
            }
        } else if (result_message.indexOf("earth") != -1) {
            speak("Don't be silly, The earth is a sphere. As are all other planets and celestial bodies")

            Toast.makeText(this,"you",Toast.LENGTH_LONG).show()
        } else if (result_message.indexOf("browser") != -1) {
            speak("Opening a browser right away master.")
            Toast.makeText(this,"you",Toast.LENGTH_LONG).show()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.co.in"))
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        tts!!.shutdown()
    }

    override fun onResume() {
        super.onResume()
        //        Reinitialize the recognizer and tts engines upon resuming from background such as after openning the browser
        initializeSpeechRecognizer()
        initializeTextToSpeech()
    }
    companion object {
        public const val MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1
    }


    fun firebase(){

        var ref = FirebaseDatabase.getInstance().getReference("/IR")

        ref.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
              var v = p0.getValue()
                txt.text=v.toString()
            }


        })

    }




    val REQ =420
    fun permission(){

        if(Build.VERSION.SDK_INT>=24) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO),REQ)


            }

            return


        }
        return



    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {

            REQ -> {

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show()
                } else {

                    Toast.makeText(this, "failed to access microphone", Toast.LENGTH_LONG).show()
                }
            }


        }



        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }



    fun charts(){
        val anyChartView: AnyChartView = findViewById(R.id.any_chart_view)
        anyChartView.setProgressBar(findViewById(R.id.progress_bar))
        val cartesian = AnyChart.line()
        cartesian.animation(true)
        cartesian.padding(10.0, 20.0, 5.0, 20.0)
        cartesian.crosshair().enabled(true)
        cartesian.crosshair()
            .yLabel(true) // TODO ystroke
            .yStroke(
                null as Stroke?,
                null,
                null,
                null as String?,
                null as String?
            )
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesian.title("Trend of Sales of the Most Popular Products of ACME Corp.")
        cartesian.yAxis(0).title("Number of Bottles Sold (thousands)")
        cartesian.xAxis(0).labels().padding(5.0, 5.0, 5.0, 5.0)
        val seriesData: MutableList<DataEntry> = ArrayList()
        seriesData.add(CustomDataEntry("9:00", 0.0, 0.0, 0.0))
        seriesData.add(CustomDataEntry("10:00", 6.0, 3.0, 9.8))
        seriesData.add(CustomDataEntry("1986", 3.6, 2.3, 2.8))
        seriesData.add(CustomDataEntry("1987", 7.1, 4.0, 4.1))
        seriesData.add(CustomDataEntry("1988", 8.5, 6.2, 5.1))
        seriesData.add(CustomDataEntry("1989", 9.2, 11.8, 6.5))
        seriesData.add(CustomDataEntry("1990", 10.1, 13.0, 12.5))
        seriesData.add(CustomDataEntry("1991", 11.6, 13.9, 18.0))
        seriesData.add(CustomDataEntry("1992", 16.4, 18.0, 21.0))
        seriesData.add(CustomDataEntry("1993", 18.0, 23.3, 20.3))
        seriesData.add(CustomDataEntry("1994", 13.2, 24.7, 19.2))
        seriesData.add(CustomDataEntry("1995", 12.0, 18.0, 14.4))
        seriesData.add(CustomDataEntry("1996", 3.2, 15.1, 9.2))
        seriesData.add(CustomDataEntry("1997", 4.1, 11.3, 5.9))
        seriesData.add(CustomDataEntry("1998", 6.3, 14.2, 5.2))
        seriesData.add(CustomDataEntry("1999", 9.4, 13.7, 4.7))
        seriesData.add(CustomDataEntry("2000", 11.5, 9.9, 4.2))
        seriesData.add(CustomDataEntry("2001", 13.5, 12.1, 1.2))
        seriesData.add(CustomDataEntry("2002", 14.8, 13.5, 5.4))
        seriesData.add(CustomDataEntry("2003", 16.6, 15.1, 6.3))
        seriesData.add(CustomDataEntry("2004", 18.1, 17.9, 8.9))
        seriesData.add(CustomDataEntry("2005", 17.0, 18.9, 10.1))
        seriesData.add(CustomDataEntry("2006", 16.6, 20.3, 11.5))
        seriesData.add(CustomDataEntry("2007", 14.1, 20.7, 12.2))
        seriesData.add(CustomDataEntry("2008", 15.7, 21.6, 10))
        seriesData.add(CustomDataEntry("2009", 12.0, 22.5, 8.9))

        val set = Set.instantiate()
        set.data(seriesData)
        val series1Mapping = set.mapAs("{ x: 'x', value: 'value' }")
        val series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }")
        val series3Mapping = set.mapAs("{ x: 'x', value: 'value3' }")
        val series1 = cartesian.line(series1Mapping)
        series1.name("Brandy")
        series1.hovered().markers().enabled(true)
        series1.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series1.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)
        val series2 = cartesian.line(series2Mapping)
        series2.name("Whiskey")
        series2.hovered().markers().enabled(true)
        series2.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series2.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)
        val series3 = cartesian.line(series3Mapping)
        series3.name("rohan")
        series3.hovered().markers().enabled(true)
        series3.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series3.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)
        cartesian.legend().enabled(true)
        cartesian.legend().fontSize(13.0)
        cartesian.legend().padding(0.0, 0.0, 10.0, 0.0)
        anyChartView.setChart(cartesian)
    }

    private inner class CustomDataEntry internal constructor(
        x: String?,
        value: Number?,
        value2: Number?,
        value3: Number?
    ) : ValueDataEntry(x, value) {
        init {
            setValue("value2", value2)
            setValue("value3", value3)
        }
    }

}
