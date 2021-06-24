package com.tsellami.pilot.ui.favorite

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tsellami.pilot.R
import com.tsellami.pilot.data.airport.Airport
import com.tsellami.pilot.databinding.FavoriteFragmentBinding
import com.tsellami.pilot.ui.adapter.FavoriteAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class FavoriteFragment : Fragment(R.layout.favorite_fragment), FavoriteAdapter.OnItemListener {

    private val viewModel: FavoriteViewModel by viewModels()
    private var binding: FavoriteFragmentBinding? = null
    private lateinit var favoriteAdapter: FavoriteAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FavoriteFragmentBinding.bind(view)
        favoriteAdapter = FavoriteAdapter(this)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.favoriteEvent.collect { event ->
                when(event) {
                    is FavoriteViewModel.FavoriteEvents.Loading -> {
                        binding?.apply {
                            loadingView.visibility = View.VISIBLE
                            favoriteRecyclerView.visibility = View.GONE
                            emptyFavorites.visibility = View.GONE
                        }
                    }
                    is FavoriteViewModel.FavoriteEvents.Successful -> {
                        binding?.apply {
                            loadingView.visibility = View.GONE
                            favoriteRecyclerView.visibility = View.VISIBLE
                            emptyFavorites.visibility = View.GONE
                            favoriteRecyclerView.apply {
                                adapter = favoriteAdapter
                                layoutManager = LinearLayoutManager(requireContext())
                            }
                            favoriteAdapter.submitList(event.favorites)
                        }
                    }
                    is FavoriteViewModel.FavoriteEvents.Empty -> {
                        binding?.apply {
                            loadingView.visibility = View.GONE
                            favoriteRecyclerView.visibility = View.GONE
                            emptyFavorites.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onClick(airport: Airport) {
        val action = FavoriteFragmentDirections.actionFavoriteFragmentToMetarFragment(airport.icao)
        findNavController().navigate(action)
    }

}