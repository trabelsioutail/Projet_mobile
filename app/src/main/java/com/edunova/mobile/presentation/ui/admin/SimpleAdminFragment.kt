package com.edunova.mobile.presentation.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.edunova.mobile.databinding.FragmentSimpleAdminBinding

class SimpleAdminFragment : Fragment() {
    
    private var _binding: FragmentSimpleAdminBinding? = null
    private val binding get() = _binding!!
    
    private var sectionTitle: String = "Gestion Admin"
    private var sectionDescription: String = "Interface d'administration"
    
    companion object {
        fun newInstance(title: String, description: String): SimpleAdminFragment {
            val fragment = SimpleAdminFragment()
            val args = Bundle()
            args.putString("title", title)
            args.putString("description", description)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sectionTitle = it.getString("title", "Gestion Admin")
            sectionDescription = it.getString("description", "Interface d'administration")
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimpleAdminBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupBackButton()
        setupContent()
    }
    
    private fun setupBackButton() {
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun setupContent() {
        binding.textViewTitle.text = sectionTitle
        binding.textViewDescription.text = sectionDescription
        
        binding.buttonAction1.setOnClickListener {
            when (sectionTitle) {
                "Gestion des Quiz" -> {
                    navigateToAllQuizzes()
                }
                else -> {
                    Toast.makeText(requireContext(), "Action 1 - $sectionTitle", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        binding.buttonAction2.setOnClickListener {
            when (sectionTitle) {
                "Gestion des Quiz" -> {
                    Toast.makeText(requireContext(), "Fonctionnalité de création de quiz à venir", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(requireContext(), "Action 2 - $sectionTitle", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        binding.buttonAction3.setOnClickListener {
            when (sectionTitle) {
                "Gestion des Quiz" -> {
                    Toast.makeText(requireContext(), "Statistiques des quiz disponibles dans la vue détaillée", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(requireContext(), "Action 3 - $sectionTitle", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // Personnaliser selon la section
        when (sectionTitle) {
            "Gestion des Quiz" -> {
                binding.buttonAction1.text = "Voir tous les quiz"
                binding.buttonAction2.text = "Créer un quiz"
                binding.buttonAction3.text = "Statistiques des quiz"
            }
            "Rapports et Analyses" -> {
                binding.buttonAction1.text = "Générer rapport d'activité"
                binding.buttonAction2.text = "Rapport des cours"
                binding.buttonAction3.text = "Rapport des utilisateurs"
            }
            "Paramètres Système" -> {
                binding.buttonAction1.text = "Configuration générale"
                binding.buttonAction2.text = "Paramètres de sécurité"
                binding.buttonAction3.text = "Maintenance"
            }
            "Sauvegarde" -> {
                binding.buttonAction1.text = "Créer une sauvegarde"
                binding.buttonAction2.text = "Restaurer sauvegarde"
                binding.buttonAction3.text = "Planifier sauvegardes"
            }
        }
    }
    
    private fun navigateToAllQuizzes() {
        try {
            val fragment = AdminQuizMenuFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack("AdminQuizMenuFromSimple")
                .commit()
        } catch (e: Exception) {
            try {
                val fragment = AdminQuizMenuFragment()
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminQuizMenuFromSimple")
                    .commit()
            } catch (ex: Exception) {
                Toast.makeText(requireContext(), "Erreur de navigation vers le menu quiz: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}