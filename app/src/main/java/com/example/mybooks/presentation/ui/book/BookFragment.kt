package com.example.mybooks.presentation.ui.book

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.mybooks.R
import com.example.mybooks.databinding.FragmentBookBinding
import com.example.mybooks.presentation.viewmodel.BookViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookFragment : Fragment(R.layout.fragment_book) {

    private val binding: FragmentBookBinding by viewBinding()
    private val viewModel by viewModels<BookViewModel>()
    private val args: BookFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeUI(args.isUpdate)
    }

    private fun changeUI(update: Boolean) {
        with(binding) {
            if (update) {
                tvBookTitle.setText(args.bookTitle)
                tvBookAuthor.setText(args.bookAuthor)
                btnBookDelete.visibility = View.VISIBLE
                btnBookDelete.setOnClickListener {
                    viewModel.deleteFromDatabase(args.bookUuid)
                    findNavController().popBackStack()
                }
                btnBookSave.setOnClickListener {
                    val title = tvBookTitle.text.trim()
                    val author = tvBookAuthor.text.trim()
                    when {
                        title.isEmpty() -> binding.tvBookTitle.error = "Field is empty"
                        author.isEmpty() -> binding.tvBookAuthor.error = "Field is empty"
                        else -> {
                            viewModel.updateInDatabase(
                                args.bookUuid,
                                title.toString(),
                                author.toString()
                            )
                            findNavController().popBackStack()
                        }
                    }
                }
            } else {
                btnBookDelete.visibility = View.INVISIBLE
                btnBookSave.setOnClickListener {
                    val title = tvBookTitle.text.trim()
                    val author = tvBookAuthor.text.trim()
                    when {
                        title.isEmpty() -> binding.tvBookTitle.error = getString(R.string.field_is_empty)
                        author.isEmpty() -> binding.tvBookAuthor.error = getString(R.string.field_is_empty)
                        else -> {
                            viewModel.addInDatabase(title.toString(), author.toString())
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }
}