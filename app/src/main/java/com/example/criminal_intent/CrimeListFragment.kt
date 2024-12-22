package com.example.criminal_intent

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.example.criminal_intent.database.Crime
import com.example.criminal_intent.databinding.FragmentCrimeListBinding
import java.util.UUID

class CrimeListFragment: Fragment(), MenuProvider {

    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this)[CrimeListViewModel::class.java]
    }
    private var callbacks: Callbacks? = null
    private var adapter: CrimeAdapter? = CrimeAdapter(callbacks)
    private var _binding: FragmentCrimeListBinding? = null
    private val binding: FragmentCrimeListBinding
        get() = _binding!!
    private lateinit var crimeRecyclerView: RecyclerView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrimeListBinding.inflate(inflater, container, false)
        crimeRecyclerView = binding.rvCrimeList
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        setAdapter()
        crimeListViewModel.crimeLiveData.observe(
            viewLifecycleOwner
        ) { crimes ->
            crimes?.let {
                adapter!!.submitList(crimes)
                if (crimes.isEmpty()) binding.tvEmptyList.visibility = View.VISIBLE
                else binding.tvEmptyList.visibility = View.GONE
            }
        }
        requireActivity().let {
            (it as? AppCompatActivity)?.setSupportActionBar(binding.toolbarFragmentCrimeList)
            it.addMenuProvider(this@CrimeListFragment, viewLifecycleOwner)
        }
        return binding.root
    }


    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId ) {
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> false
        }
    }

    private fun setAdapter() {
        adapter = CrimeAdapter(callbacks)
        crimeRecyclerView.adapter = adapter
    }

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }

}