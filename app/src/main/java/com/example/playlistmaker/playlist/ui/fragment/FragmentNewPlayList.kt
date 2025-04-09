package com.example.playlistmaker.playlist.ui.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmakercompose.R
import com.example.playlistmakercompose.databinding.FragmentNewPlayListBinding
import com.example.playlistmaker.playlist.ui.viewmodel.NewPlayListViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File
import java.io.FileOutputStream

class FragmentNewPlayList : Fragment() {
    private var _binding: FragmentNewPlayListBinding? = null
    private val binding get() = _binding!!

    private var uri: Uri? = null

    companion object {
    }

    val viewModel by activityViewModel<NewPlayListViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPlayListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val source = it.getString("source")
            val playlistName = it.getString("listName", "")
            val description = it.getString("description", "")
            val image = it.getString("Image")
            val playListId = it.getString("playlistId")

            handleSource(source, playlistName, description, image, playListId.toString())
        }

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                this.uri = uri
                if (uri != null) {
                    Glide.with(this)
                        .load(uri)
                        .skipMemoryCache(true)
                        .transform(
                            CenterCrop(),
                            RoundedCorners(10),
                        )
                        .into(binding.pickerImage)
                    saveImageToPrivateStorage(uri)
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }

        binding.pickerImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.textInputName.addTextChangedListener(
            onTextChanged = { p0: CharSequence?, _, _, _ ->
                if (binding.textInputName.text.isNullOrEmpty()) {
                    binding.buttonCreate.isEnabled = false
                } else {
                    binding.buttonCreate.isEnabled = true
                }

            })

        binding.textInputDescription.addTextChangedListener(
            onTextChanged = { p0: CharSequence?, _, _, _ ->

            }
        )
    }

    private fun handleSource(
        source: String?,
        playlistName: String?,
        description: String?,
        image: String?,
        playListId: String
    ) {
        when (source) {
            "screenPlaylist" -> {
                binding.buttonCreate.setText(R.string.save)
                binding.buttonCreate.isEnabled = true
                binding.textInputName.setText(playlistName)
                binding.textInputDescription.setText(description)

                this.uri = image?.toUri()

                Glide.with(this)
                    .load(image?.toUri())
                    .placeholder(R.drawable.vector)
                    .skipMemoryCache(true)
                    .transform(
                        CenterCrop(),
                        RoundedCorners(10),
                    )
                    .into(binding.pickerImage)

                binding.buttonCreate.setOnClickListener {
                    viewModel.updatePlaylist(
                        playListId.toInt(),
                        binding.textInputName.text.toString(),
                        binding.textInputDescription.text.toString(),
                        uri
                    )
                    toast(getString(R.string.playlist_save, binding.textInputName.text.toString()))
                    findNavController().navigateUp()
                }

                binding.toolbar.setNavigationOnClickListener {
                    findNavController().navigateUp()
                }

                requireActivity().onBackPressedDispatcher.addCallback(
                    viewLifecycleOwner,
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            findNavController().navigateUp()
                        }
                    })
            }

            else -> {
                binding.buttonCreate.setText(R.string.greate)

                binding.buttonCreate.setOnClickListener {
                    viewModel.savePlayList(
                        binding.textInputName.text.toString(),
                        binding.textInputDescription.text.toString(),
                        uri
                    )
                    toast(getString(R.string.playlist_create, binding.textInputName.text.toString()))
                    findNavController().navigateUp()
                }

                binding.toolbar.setNavigationOnClickListener {
                    if (binding.textInputName.text.isNullOrEmpty() && binding.textInputDescription.text.isNullOrEmpty() && uri == null) {
                        findNavController().navigateUp()
                    } else {
                        dialog()
                    }
                }

                requireActivity().onBackPressedDispatcher.addCallback(
                    viewLifecycleOwner,
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            dialog()
                        }
                    })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveImageToPrivateStorage(uri: Uri) {
        val filePath =
            File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "myalbum")

        if (!filePath.exists()) {
            filePath.mkdirs()
        }

        val file = File(filePath, "first_cover.jpg")

        val inputStream = requireContext().contentResolver.openInputStream(uri)

        val outputStream = FileOutputStream(file)

        BitmapFactory.decodeStream(inputStream)
            .compress(Bitmap.CompressFormat.JPEG, 30, outputStream)
    }

    private fun dialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Завершить создание плейлиста?")
            .setMessage("Все несохраненные данные будут потеряны")
            .setNeutralButton("Завершить") { dialog, which ->
                findNavController().navigateUp()
            }
            .setPositiveButton("Отмена") { dialog, which ->

            }
            .show()
    }

    private fun toast(string: String) {
        Toast.makeText(requireContext(),
            string, Toast.LENGTH_SHORT).show()
    }

}
