package xyz.drean.chatbot

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.item_message_bot.view.*
import kotlinx.android.synthetic.main.item_message_user.view.*

private const val VIEW_TYPE_MY_MESSAGE = 1
private const val VIEW_TYPE_BOT_MESSAGE = 2

class MessageAdapter(private val messages: ArrayList<Message>, private val context: Context) : RecyclerView.Adapter<MessageViewHolder>() {

    // private val messages: ArrayList<Message> = ArrayList()

    fun addMessage(message: Message){
        messages.add(message)
        notifyDataSetChanged()
        saveMessage(message)
    }

    fun saveMessage(message: Message) {
        val db = FirebaseFirestore.getInstance()
        db.collection("messages").add(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]

        return if(message.user == "user") {
            VIEW_TYPE_MY_MESSAGE
        } else {
            VIEW_TYPE_BOT_MESSAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return if(viewType == VIEW_TYPE_MY_MESSAGE) {
            UserMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_user, parent, false))
        } else {
            BotMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_bot, parent, false))
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    inner class UserMessageViewHolder (view: View) : MessageViewHolder(view) {
        private var messageText: TextView = view.user_message

        override fun bind(message: Message) {
            messageText.text = message.message
        }
    }

    inner class BotMessageViewHolder (view: View) : MessageViewHolder(view) {
        private var messageText: TextView = view.bot_message

        override fun bind(message: Message) {
            messageText.text = message.message
        }
    }
}

open class MessageViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    open fun bind(message: Message) {}
}