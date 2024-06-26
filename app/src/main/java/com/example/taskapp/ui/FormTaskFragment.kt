package com.example.taskapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.taskapp.R
import com.example.taskapp.data.model.Status
import com.example.taskapp.data.model.Task
import com.example.taskapp.databinding.FragmentFormTaskBinding
import com.example.taskapp.util.initToolbar
import com.example.taskapp.util.showBottomSheet



class FormTaskFragment : BaseFragment() {

    private var _binding: FragmentFormTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var task: Task
    private  var status: Status = Status.TODO
    private var newTask: Boolean = true

    private val args : FormTaskFragmentArgs by navArgs()



    private val viewModel:TaskViewModel by activityViewModels()





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentFormTaskBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)



        getArgs()
        initListerners()

    }

    private fun getArgs(){
        args.task.let { it->
            if(it != null){
                this.task = it

                configTask()
            }
        }
    }

    private fun initListerners() {

        binding.btnSave.setOnClickListener() {
            validateData()

        }

        binding.radioGroup.setOnCheckedChangeListener{ _,id->
            status = when(id){
                R.id.rbTodo -> Status.TODO
                R.id.rbDoing -> Status.DOING
                else -> Status.DONE
            }
        }
    }

    private fun configTask(){

        newTask = false
        status = task.status
        binding.textToolbar.setText(R.string.text_toolbar_update_form_task_fragment)

        binding.edtDescription.setText(task.description)
        setStatus()



    }

    private fun setStatus() {


        binding.radioGroup.check(
            when(task.status){
                Status.TODO -> R.id.rbTodo
                Status.DOING -> R.id.rbDoing
                else -> R.id.rbDone            }

        )
    }

    private fun validateData(){

        val description = binding.edtDescription.text.toString().trim()


        if(description.isNotEmpty()){

            hideKeyboard()

            binding.progressBar.isVisible = true

            if(newTask) task = Task()
            task.description = description
            task.status = status

            saveTask()

        }else{
           showBottomSheet(message = getString(R.string.description_empty_form_task_fragment))
        }

    }

    private fun saveTask() {
        FirebaseHelper.getDatabase()
            .child("task")
            .child(FirebaseHelper.getIdUser())
            .child(task.id)
            .setValue(task).addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        R.string.text_save_sucess_form_task_fragment,
                        Toast.LENGTH_SHORT
                    ).show()

                    if (newTask) { //Nova tarefa
                        findNavController().popBackStack()
                    } else { // Editando tarefa


                        viewModel.setUpdateTask(task)

                        binding.progressBar.isVisible = false
                    }

                } else {
                    binding.progressBar.isVisible = false
                    showBottomSheet(message = getString(R.string.error_generic))
                }

            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    }
