package com.tsellami.pilot.ui.metar

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.tsellami.pilot.R
import com.tsellami.pilot.databinding.MetarFragmentBinding
import com.tsellami.pilot.ui.adapter.MetarDataAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MetarFragment : Fragment(R.layout.metar_fragment) {

    private val viewModel: MetarViewModel by viewModels()
    private var binding: MetarFragmentBinding? = null
    private val args: MetarFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MetarFragmentBinding.bind(view)
        viewModel.airport.observe(viewLifecycleOwner, {
            binding?.apply {
                favoriteImageView.setImageResource(viewModel.setFavoriteImage())
            }
        })
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.setInitialData(args.icao)
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.queryEvent.collect { event ->
                when (event) {
                    is MetarViewModel.QueryEvent.NotStarted -> {
                        binding?.apply {
                            metarDataLayout.visibility = View.GONE
                            warningLayout.visibility = View.VISIBLE
                            loadingView.visibility = View.GONE
                            retry.visibility = View.GONE
                            bigTitle.text = getString(R.string.look_for_the_flight_weather)
                            smallTitle.text = getString(R.string.by_providing_the_icao_code_or_the_airport_s_name)
                            screenImage.setImageResource(R.drawable.ic_starting_screen)
                        }
                    }
                    is MetarViewModel.QueryEvent.NotFound -> {
                        binding?.apply {
                            metarDataLayout.visibility = View.GONE
                            warningLayout.visibility = View.VISIBLE
                            loadingView.visibility = View.GONE
                            retry.visibility = View.GONE
                            bigTitle.text = getString(R.string.no_airport_found)
                            smallTitle.text = getString(R.string.please_check_again)
                            screenImage.setImageResource(R.drawable.ic_no_airport_found)
                        }
                    }
                    is MetarViewModel.QueryEvent.Retrieved -> {
                        binding?.apply {
                            metarDataLayout.visibility = View.VISIBLE
                            warningLayout.visibility = View.GONE
                            loadingView.visibility = View.GONE
                            metarDataLayout.apply {
                                airportName.text = viewModel.airport.value?.name
                                airportIcao.text = event.metarData.icao
                                rawData.text = event.metarData.rawData
                            }
                            metarRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                            metarRecyclerView.adapter =
                                MetarDataAdapter(event.metarData.convertDataToList())
                        }
                    }
                    is MetarViewModel.QueryEvent.Loading -> {
                        binding?.apply {
                            metarDataLayout.visibility = View.GONE
                            warningLayout.visibility = View.GONE
                            loadingView.visibility = View.VISIBLE
                        }
                    }
                    is MetarViewModel.QueryEvent.Error -> {
                        binding?.apply {
                            metarDataLayout.visibility = View.GONE
                            warningLayout.visibility = View.VISIBLE
                            loadingView.visibility = View.GONE
                            retry.visibility = View.VISIBLE
                            bigTitle.text = getString(R.string.error_occured)
                            smallTitle.text = getString(R.string.please_check_again)
                            screenImage.setImageResource(R.drawable.ic_error)
                        }
                    }
                }
            }
        }
        binding?.apply {
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.refreshMetarDataManually()
                swipeRefreshLayout.isRefreshing = false
            }
            retry.setOnClickListener {
                viewModel.retrieveMetarData(viewModel.query.value!!)
            }
            favoriteImageView.setOnClickListener {
                viewModel.editFavorite()
            }
        }
        setHasOptionsMenu(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_fragment_metar, menu)
        val queryView = menu.findItem(R.id.action_search).actionView as SearchView
        queryView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                queryView.clearFocus()
                query?.let {
                    viewModel.query.postValue(it.trim())
                    viewModel.retrieveMetarData(it.trim())
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean = true
        })
    }

}