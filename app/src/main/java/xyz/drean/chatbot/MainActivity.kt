package xyz.drean.chatbot

import ai.api.AIConfiguration
import ai.api.AIListener
import ai.api.android.AIService
import ai.api.model.AIError
import ai.api.model.AIResponse
import android.Manifest
import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_main.*
import java.security.AccessController.getContext
import java.util.*
import android.speech.RecognizerIntent
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.support.v4.os.HandlerCompat.postDelayed





class MainActivity : AppCompatActivity(), AIListener, TextToSpeech.OnInitListener {
    private lateinit var messages: ArrayList<Message>
    private var db: FirebaseFirestore? = null
    private lateinit var llm: LinearLayoutManager
    private lateinit var messageList: RecyclerView
    private lateinit var adapter: MessageAdapter
    private lateinit var service: AIService

    private val accesToken = "13c53bfc06634e44860fc72369a19b5b"
    private val REQUEST = 200

    var leer: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageList = findViewById(R.id.message_list)
        messages = ArrayList()
        db = FirebaseFirestore.getInstance()

        leer = TextToSpeech(this, this)

        init()
        initAdapter()
        //getData()

        settingBot()
        getPermission()

        micButton.setOnClickListener {
            service.startListening()
        }
    }

    override fun onInit(status: Int) {
    }

    override fun onResult(result: AIResponse?) {
        val listen = result?.result
        val listener = listen?.resolvedQuery
        val answer = listen?.fulfillment?.speech
        addMessage(listener!!, "user")

        Handler().postDelayed( {
            addMessage(answer!!, "bot")
            speak(answer)
            messageList.scrollToPosition(adapter.itemCount - 1)
        }, 1000)

        messageList.scrollToPosition(adapter.itemCount - 1)
    }

    private fun speak(answer: String?){
        leer?.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null)
    }


    private fun addMessage(message: String, user: String) {
        adapter.addMessage(
            Message(message, user, System.currentTimeMillis().toString())
        )
    }

    override fun onListeningStarted() {
        micButton.setColorFilter(resources.getColor(R.color.mic_active))
    }

    override fun onAudioLevel(level: Float) {
        // esto de aqui deberia mostrar el level del microfono
        micButton.setSoundLevel(level)
    }

    override fun onError(error: AIError?) {

    }

    override fun onListeningCanceled() {

    }

    override fun onListeningFinished() {
        micButton.setColorFilter(resources.getColor(R.color.mic_desactive))
    }

    private fun init() {
        llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        messageList.layoutManager = llm
    }

    private fun initAdapter() {
        adapter = MessageAdapter(messages, this)
        messageList.adapter = adapter
    }

    private fun settingBot(){
        val configuracion = ai.api.android.AIConfiguration(accesToken, AIConfiguration.SupportedLanguages.Spanish,
            ai.api.android.AIConfiguration.RecognitionEngine.System)
        service = AIService.getService(this, configuracion)
        service.setListener(this)
    }

    private fun getPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST)
        }
    }

    private fun getData() {
        messages.clear()
        db!!.collection("messages")
            .orderBy("time")
            .limit(20)
            .addSnapshotListener { value, e ->
                for (doc in value!!) {
                    val o = doc.toObject(Message::class.java)
                    messages.add(o)
                    initAdapter()
                }
            }
    }
}
