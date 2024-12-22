package com.example.criminal_intent

import android.app.Activity
import android.content.Intent
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.example.criminal_intent.database.Crime
import com.example.criminal_intent.databinding.FragmentCrimeBinding
import java.util.Calendar
import java.util.Locale
import java.util.UUID

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val NEW_DATE = "new_date"
private const val NEW_TIME = "new_time"

class CrimeFragment: Fragment() {

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this)[CrimeDetailViewModel::class.java]
    }
    private var _binding: FragmentCrimeBinding? = null
    private val binding: FragmentCrimeBinding
        get() = _binding!!
    private lateinit var etCrimeTitle: EditText
    private lateinit var btnCrimeDate: Button
    private lateinit var btnCrimeTime: Button
    private lateinit var cbCrimeSolved: CheckBox
    private lateinit var btnSendReport: Button
    private lateinit var btnChooseSuspect: Button
    private lateinit var crime: Crime
    private val dateFormat = DateFormat.getInstanceForSkeleton(DateFormat.YEAR_ABBR_MONTH_WEEKDAY_DAY)
    private lateinit var pickContactLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                arguments?.getSerializable(ARG_CRIME_ID, UUID::class.java) as UUID
            else
                arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrimeBinding.inflate(inflater, container, false)
        etCrimeTitle = binding.etCrimeTitle
        btnCrimeDate = binding.btnCrimeDate
        btnCrimeTime = binding.btnCrimeTime
        cbCrimeSolved = binding.cbCrimeSolved
        btnSendReport = binding.btnSendReport
        btnChooseSuspect = binding.btnSuspect
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(viewLifecycleOwner) { crime ->
            crime?.let {
                this.crime = crime
                updateUI()
            }
        }
        pickContactLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when {
                result.resultCode != Activity.RESULT_OK -> return@registerForActivityResult

                result.resultCode == Activity.RESULT_OK && result.data != null -> {
                    val contactUri: Uri? = result.data!!.data
                    val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                    val cursor = requireActivity().contentResolver
                        .query(contactUri!!, queryFields, null, null, null)
                    cursor?.use {
                        if (it.count == 0) return@registerForActivityResult

                        it.moveToFirst()
                        val suspect = it.getString(0)
                        crime.suspect = suspect
                        crimeDetailViewModel.saveCrime(crime)
                        btnChooseSuspect.text = suspect
                    }

                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        btnChooseSuspect.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                pickContactLauncher.launch(pickContactIntent)
            }

        }

        btnSendReport.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject))
            }.also { intent: Intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        etCrimeTitle.addTextChangedListener(titleWatcher)

        cbCrimeSolved.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        btnCrimeDate.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        btnCrimeTime.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                show(this@CrimeFragment.parentFragmentManager, DIALOG_TIME)
            }
        }

        setFragmentResultListener(NEW_DATE) { _, result ->
            val year = result.getInt("year")
            val month = result.getInt("month")
            val day = result.getInt("day")
            val currentCalendar = Calendar.getInstance().apply {
                time = crime.date
            }
            val hours = currentCalendar.get(Calendar.HOUR_OF_DAY)
            val minutes = currentCalendar.get(Calendar.MINUTE)
            val calendar = Calendar.getInstance().apply {
                set(year, month, day, hours, minutes)
            }
            crime.date = calendar.time
            updateUI()
        }

        setFragmentResultListener(NEW_TIME) { _, result ->
            val hours = result.getInt("hours")
            val minutes = result.getInt("minutes")
            val currentCalendar = Calendar.getInstance().apply {
                time = crime.date
            }
            val year = currentCalendar.get(Calendar.YEAR)
            val month = currentCalendar.get(Calendar.MONTH)
            val day = currentCalendar.get(Calendar.DATE)
            val calendar = Calendar.getInstance().apply {
                set(year, month, day, hours, minutes)
            }
            crime.date = calendar.time
            updateUI()
        }

    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    private fun updateUI() {
        etCrimeTitle.setText(crime.title)
        btnCrimeDate.text = dateFormat.format(crime.date)
        btnCrimeTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(crime.date)
        cbCrimeSolved.isChecked = crime.isSolved
        if (crime.suspect.isNotEmpty()) {
            btnChooseSuspect.text = crime.suspect
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved)
                getString(R.string.crime_report_solved)
             else
                getString(R.string.crime_report_unsolved)
        val dateString = android.text.format.DateFormat.format("EEE, MMM, dd", crime.date).toString()
        val suspect = if (crime.suspect.isBlank())
                getString(R.string.crime_report_no_suspect)
            else
                getString(R.string.crime_report_suspect, crime.suspect)
        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    companion object {

        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }

    }

}