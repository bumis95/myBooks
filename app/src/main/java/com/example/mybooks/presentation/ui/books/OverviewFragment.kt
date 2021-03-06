package com.example.mybooks.presentation.ui.books

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.mybooks.R
import com.example.mybooks.databinding.FragmentOverviewBinding
import com.example.mybooks.domain.entity.Book
import com.example.mybooks.presentation.adapter.BookAdapter
import com.example.mybooks.presentation.viewmodel.OverviewViewModel
import com.example.mybooks.util.AuthState
import com.example.mybooks.util.UiState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OverviewFragment : Fragment(R.layout.fragment_overview) {

    private val binding: FragmentOverviewBinding by viewBinding()
    private val viewModel by viewModels<OverviewViewModel>()
    private val bookAdapter = BookAdapter { openBookDetail(it.uuid, it.title, it.author) }
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                findNavController().navigate(R.id.action_overviewFragment_to_settingsFragment)
                true
            }
            R.id.menu_logout -> {
                viewModel.logOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initStates()
    }

    private fun initUI() {
        with(binding) {
            rvBooks.adapter = bookAdapter
            fbAddBook.setOnClickListener {
                findNavController().navigate(R.id.action_overviewFragment_to_bookFragment)
            }
        }
    }

    private fun initStates() {
        with(binding) {
            viewLifecycleOwner.lifecycleScope.launch {
                with(viewModel) {
                    launch {
                        authState.collect { authState ->
                            when (authState) {
                                is AuthState.NotLoggedIn -> findNavController().navigate(R.id.loginFragment)
                                is AuthState.LoggedIn -> {}
                                is AuthState.Failed -> {}
                            }
                        }
                    }
                    launch {
                        uiState.collect { uiState ->
                            when (uiState) {
                                is UiState.Loading -> pbProgress.visibility = View.VISIBLE
                                is UiState.Success -> {
                                    pbProgress.visibility = View.GONE
                                    checkEmptyList(uiState.data)
                                    bookAdapter.submitList(uiState.data)
                                }
                                is UiState.Failed -> pbProgress.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkEmptyList(data: List<Book>) {
        with(binding) {
            if (data == emptyList<Book>()) {
                snackbar =
                    Snackbar.make(requireView(), getString(R.string.add_some_new_books), Snackbar.LENGTH_INDEFINITE)
                snackbar?.show()
                ivError.visibility = View.VISIBLE
            } else {
                if (snackbar != null) {
                    snackbar?.dismiss()
                }
                ivError.visibility = View.GONE
            }
        }
    }

    private fun openBookDetail(uuid: String, title: String, author: String) {
        val action = OverviewFragmentDirections
            .actionOverviewFragmentToBookFragment(
                bookUuid = uuid,
                bookTitle = title,
                bookAuthor = author,
                isUpdate = true
            )
        findNavController().navigate(action)
    }
}