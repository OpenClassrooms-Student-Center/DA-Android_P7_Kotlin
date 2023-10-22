package com.openclassrooms.arista.ui.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.openclassrooms.arista.R
import com.openclassrooms.arista.databinding.FragmentExerciseBinding
import com.openclassrooms.arista.domain.model.Exercise
import com.openclassrooms.arista.domain.model.ExerciseCategory
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.LocalDateTime

interface DeleteExerciseInterface {
    fun deleteExercise(exercise: Exercise?)
}
@AndroidEntryPoint
class ExerciseFragment : Fragment(), DeleteExerciseInterface {

    private var _binding: FragmentExerciseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExerciseViewModel by viewModels()
    private lateinit var exerciseAdapter: ExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        observeExercises()
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseAdapter(this)
        binding.exerciseRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.exerciseRecyclerview.adapter = exerciseAdapter
    }

    private fun observeExercises() {
        viewModel.exercisesLiveData.observe(viewLifecycleOwner) { exercises ->
            exerciseAdapter.submitList(exercises)
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener { showAddExerciseDialog() }
    }

    private fun showAddExerciseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_exercise, null)
        setupDialogViews(dialogView).also {
            AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setTitle(R.string.add_new_exercise)
                .setPositiveButton(R.string.add) { _, _ -> addExercise(it) }
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun setupDialogViews(dialogView: View): Triple<EditText, Spinner, EditText> {
        val durationEditText = dialogView.findViewById<EditText>(R.id.durationEditText)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner).apply {
            adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                ExerciseCategory.values()
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }
        val intensityEditText = dialogView.findViewById<EditText>(R.id.intensityEditText)
        return Triple(durationEditText, categorySpinner, intensityEditText)
    }

    private fun addExercise(views: Triple<EditText, Spinner, EditText>) {
        val (durationEditText, categorySpinner, intensityEditText) = views

        val durationStr = durationEditText.text.toString().trim()
        val intensityStr = intensityEditText.text.toString().trim()

        if (durationStr.isBlank() || intensityStr.isBlank()) {
            Toast.makeText(requireContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val duration = durationStr.toInt()
            val intensity = intensityStr.toInt()

            if (intensity in 1..10) {
                val category = categorySpinner.selectedItem as ExerciseCategory
                val newExercise = Exercise(LocalDateTime.now(), duration, category, intensity)
                viewModel.addNewExercise(newExercise)
            } else {
                Toast.makeText(requireContext(), R.string.intensity_should_be_between_1_and_10, Toast.LENGTH_SHORT).show()
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), R.string.invalid_input_please_enter_valid_numbers, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun deleteExercise(exercise: Exercise?) {
        exercise?.let { viewModel.deleteExercise(it) }
    }
}