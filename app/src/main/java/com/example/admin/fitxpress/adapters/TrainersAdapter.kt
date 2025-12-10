package com.example.admin.fitxpress.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.admin.fitxpress.R
import com.example.admin.fitxpress.models.Trainee
// import com.bumptech.glide.Glide

class TrainersAdapter(
    private val trainees: MutableList<Trainee>,
    private val onEditClick: (Trainee) -> Unit, //Data Source
    private val onDeleteClick: (Trainee) -> Unit,
    private val onStatusToggle: (Trainee) -> Unit
   ) : RecyclerView.Adapter<TrainersAdapter.TrainerViewHolder>() { // Inherits from RecyclerView.Adapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trainer, parent, false) //This line takes your XML layout file for a single trainee item
        return TrainerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrainerViewHolder, position: Int) {
        holder.bind(trainees[position]) //The `bind()` method is responsible for setting the actual data onto the views within that item.
    }

    override fun getItemCount(): Int = trainees.size

    fun updateTrainees(newTrainees: List<Trainee>) {
        trainees.clear()
        trainees.addAll(newTrainees)
        notifyDataSetChanged() // For simplicity. Consider DiffUtil for better performance.
    }

    //Each instance of TrainerViewHolder holds and manages the views for a single item in your RecyclerView.
    inner class TrainerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Views that are in the corrected item_trainer.xml
        private val trainerImageView: ImageView = itemView.findViewById(R.id.trainerImageView)
        private val trainerNameTextView: TextView = itemView.findViewById(R.id.trainerNameTextView)
        private val trainerSpecializationTextView: TextView = itemView.findViewById(R.id.trainerSpecializationTextView)
        private val trainerEmailTextView: TextView = itemView.findViewById(R.id.trainerEmailTextView)
        private val statusSwitch: Switch = itemView.findViewById(R.id.statusSwitch)
        private val experienceTextView: TextView = itemView.findViewById(R.id.experienceTextView)
        private val feeTextView: TextView = itemView.findViewById(R.id.feeTextView)
        private val statusBadge: TextView = itemView.findViewById(R.id.statusBadge)
        private val editButton: Button = itemView.findViewById(R.id.editButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

//This method is where the data from a single Trainee object is actually applied to the views held by this ViewHolder.
        fun bind(trainee: Trainee) {
            trainerNameTextView.text = trainee.name
            trainerSpecializationTextView.text = trainee.specialty
            trainerEmailTextView.text = trainee.email

            experienceTextView.text = trainee.experience.ifEmpty {
                itemView.context.getString(R.string.experience_not_set) // Ensure this string exists
            }

            feeTextView.text = trainee.fee.ifEmpty { // Set the fee
                itemView.context.getString(R.string.fee_not_set) // Add this string: <string name="fee_not_set">Fee N/A</string>
            }


            if (trainee.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(trainee.imageUrl)
                    .placeholder(R.drawable.placeholder_trainer)
                    .error(R.drawable.placeholder_trainer)
                    .circleCrop()
                    .into(trainerImageView)
            } else {
                trainerImageView.setImageResource(R.drawable.placeholder_trainer)
            }
            // Status Switch
            statusSwitch.setOnCheckedChangeListener(null)
            statusSwitch.isChecked = trainee.isActive
            statusSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != trainee.isActive) {
                    onStatusToggle(trainee.copy(isActive = isChecked))
                }
            }

            // Status Badge
            if (trainee.isActive) {
                statusBadge.text = itemView.context.getString(R.string.active)
                statusBadge.setBackgroundResource(R.drawable.status_background_green)
            } else {
                statusBadge.text = itemView.context.getString(R.string.inactive)
                statusBadge.setBackgroundResource(R.drawable.status_background_red)
            }

            // Action Buttons
            editButton.setOnClickListener { onEditClick(trainee) }
            deleteButton.setOnClickListener { onDeleteClick(trainee) }
        }
        }

        }

