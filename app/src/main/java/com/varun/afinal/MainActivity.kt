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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.token.view.*
import kotlinx.android.synthetic.main.token2.view.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private var tts: TextToSpeech? = null
    private var speechRecog: SpeechRecognizer? = null



   var Humidity:String?=null
    var Temp:String?=null
    var AirQuality:String?=null
    var IR:String ?=null
    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutManager= GridLayoutManager(this, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (position) {
                    0 -> 1
                    1 -> 1
                    2 ->1
                    3 ->1
                    else -> 2
                }
            }
        }
        recyclerview.layoutManager = layoutManager

        recyclerview.adapter=adapter





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

        firebaseIR()
        firebaseTemp()
        firebaseAirQuality()
        firebaseHumidity()

        adapter.add(Token1())
        adapter.add(Token2())
        adapter.add(Token3())
        adapter.add(Token4())





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
                //speak("Hello there, I am ready to start our conversation")
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

    fun firebasePushIR(){


        var refout =FirebaseDatabase.getInstance().getReference("LDROUT")

        if (IR != null) {
            if(IR!!.toInt()>800){
                refout.setValue(1)

            }

            if(IR!!.toInt()<200){

                refout.setValue(0)
            }
        }
    }
    fun firebasePushTemp(){


        var refout =FirebaseDatabase.getInstance().getReference("TEMPOUT")

        if (Temp != null) {
            if(Temp!!.toFloat()>40){
                refout.setValue(1)

            }

            if(Temp!!.toFloat()<20){

                refout.setValue(0)
            }
        }
    }
    fun firebasePushHumid(){


        var refout =FirebaseDatabase.getInstance().getReference("HUMIDOUT")

        if (Humidity != null) {
            if(Humidity!!.toFloat()>40){
                refout.setValue(1)

            }

            if(Humidity!!.toFloat()<20){

                refout.setValue(0)
            }
        }


    }
    fun firebasePushAir(){


        var refout =FirebaseDatabase.getInstance().getReference("AIROUR")

        if (AirQuality != null) {
            if(AirQuality!!.toInt()>700){
                refout.setValue(1)

            }

            if(AirQuality!!.toInt()<699){

                refout.setValue(0)
            }
        }

    }



    fun firebaseIR(){

        var ref = FirebaseDatabase.getInstance().getReference("/LDR")

        ref.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
               IR = p0.getValue().toString()

                firebasePushIR()

            }


        })


    }
    fun firebaseTemp(){
        var ref = FirebaseDatabase.getInstance().getReference("/Temp")

        ref.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                Temp = p0.getValue().toString()




                firebasePushTemp()

            }


        })


    }
    fun firebaseHumidity(){

        var ref = FirebaseDatabase.getInstance().getReference("/Humidity")

        ref.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                Humidity = p0.getValue().toString()

                firebasePushHumid()
            }


        })

    }
    fun firebaseAirQuality(){

        var ref = FirebaseDatabase.getInstance().getReference("/Air Quality")

        ref.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                AirQuality = p0.getValue().toString()

                firebasePushAir()
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


    inner class Token1(): Item<GroupieViewHolder>(){


        var ref = FirebaseDatabase.getInstance().getReference("/Temp")

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            ref.addValueEventListener(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                   var temp = p0.getValue()


                    viewHolder.itemView.heading.text = "Temperature Reading"
                    viewHolder.itemView.num.text = temp.toString()
                }


            })





        }

        override fun getLayout(): Int {
            return R.layout.token
        }


    }





    inner class Token2(): Item<GroupieViewHolder>(){

        var ref = FirebaseDatabase.getInstance().getReference("/LDR")

        override fun getLayout(): Int {
            return R.layout.token2
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            ref.addValueEventListener(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                   var temp = p0.getValue()
                    viewHolder.itemView.heading2.text = "Light Sensor"
                    viewHolder.itemView.num2.text = temp.toString()
                }


            })





        }


    }

    inner class Token3(): Item<GroupieViewHolder>(){


        var ref = FirebaseDatabase.getInstance().getReference("/Humidity")


            override fun getLayout(): Int {
                return R.layout.token2
            }

            override fun bind(viewHolder: GroupieViewHolder, position: Int) {

                ref.addValueEventListener(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        var temp = p0.getValue()
                        viewHolder.itemView.heading2.text = "Humidity Reading"
                        viewHolder.itemView.num2.text = temp.toString()
                    }


                })





            }

        }




    inner class Token4(): Item<GroupieViewHolder>(){

        var ref = FirebaseDatabase.getInstance().getReference("/Air Quality")
        override fun getLayout(): Int {
            return R.layout.token
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            ref.addValueEventListener(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    var temp = p0.getValue()
                    viewHolder.itemView.heading.text = "Air Quality"
                    viewHolder.itemView.num.text = temp.toString()
                }


            })

        }


    }



}
