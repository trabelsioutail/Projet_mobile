package com.edunova.mobile.presentation.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class CourseDetailPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    
    override fun getItemCount(): Int = 4
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CourseContentTabFragment()
            1 -> CourseStudentsTabFragment()
            2 -> CourseQuizzesTabFragment()
            3 -> CourseStatisticsTabFragment()
            else -> CourseContentTabFragment()
        }
    }
}

// Fragment pour les contenus du cours
class CourseContentTabFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(android.R.layout.simple_list_item_1, container, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        
        textView.text = """
📚 Contenus du Cours

📖 Chapitre 1: Introduction à JavaScript ES6+
   • Variables (let, const)
   • Fonctions fléchées
   • Template literals

📖 Chapitre 2: Programmation Asynchrone
   • Promises
   • Async/Await
   • Fetch API

📖 Chapitre 3: Modules et Classes
   • Import/Export
   • Classes ES6
   • Héritage

📖 Chapitre 4: Outils Modernes
   • Webpack
   • Babel
   • ESLint

📹 5 vidéos • 📄 12 documents • ⏱️ 8h de contenu

➕ Ajouter du contenu
        """.trimIndent()
        
        textView.textSize = 14f
        textView.setPadding(32, 32, 32, 32)
        
        return view
    }
}

// Fragment pour les étudiants
class CourseStudentsTabFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(android.R.layout.simple_list_item_1, container, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        
        textView.text = """
👥 Étudiants Inscrits (24)

🟢 Ahmed Ben Ali - Actif
   📊 Progression: 85% • 📝 Quiz: 4/5

🟢 Fatima Zahra - Active  
   📊 Progression: 92% • 📝 Quiz: 5/5

🟡 Mohamed Tounsi - Modéré
   📊 Progression: 67% • 📝 Quiz: 3/5

🟢 Leila Mansouri - Active
   📊 Progression: 78% • 📝 Quiz: 4/5

🔴 Karim Hadj - Inactif
   📊 Progression: 23% • 📝 Quiz: 1/5

📈 Statistiques:
• Taux de réussite: 78%
• Temps moyen: 6.2h
• Note moyenne: 16.4/20

📧 Envoyer message groupé
👥 Gérer les inscriptions
        """.trimIndent()
        
        textView.textSize = 14f
        textView.setPadding(32, 32, 32, 32)
        
        return view
    }
}

// Fragment pour les quiz du cours
class CourseQuizzesTabFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(android.R.layout.simple_list_item_1, container, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        
        textView.text = """
📝 Quiz du Cours (3)

✅ Quiz 1: Variables et Fonctions
   📊 18 soumissions • ⭐ Note moyenne: 17.2/20
   📅 Créé le 15 Jan 2024 • ⏱️ 15 min

✅ Quiz 2: Programmation Asynchrone  
   📊 16 soumissions • ⭐ Note moyenne: 15.8/20
   📅 Créé le 22 Jan 2024 • ⏱️ 20 min

🟡 Quiz 3: Modules (Brouillon)
   📝 En préparation • 10 questions
   📅 Publication prévue: 30 Jan 2024

📈 Performance Globale:
• Taux de participation: 89%
• Temps moyen par quiz: 12 min
• Taux de réussite: 82%

➕ Créer un nouveau quiz
📊 Voir toutes les statistiques
📋 Exporter les résultats
        """.trimIndent()
        
        textView.textSize = 14f
        textView.setPadding(32, 32, 32, 32)
        
        return view
    }
}

// Fragment pour les statistiques
class CourseStatisticsTabFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(android.R.layout.simple_list_item_1, container, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        
        textView.text = """
📊 Statistiques Détaillées

👥 ENGAGEMENT
• 24 étudiants inscrits (+3 cette semaine)
• 89% taux d'activité (↗️ +5%)
• 6.2h temps moyen passé
• 156 connexions cette semaine

📈 PROGRESSION
• 78% taux de complétion moyen
• 85% des étudiants ont terminé Ch.1
• 67% des étudiants ont terminé Ch.2
• 45% des étudiants ont terminé Ch.3

📝 ÉVALUATIONS
• 3 quiz publiés
• 52 soumissions totales
• 16.8/20 note moyenne globale
• 82% taux de réussite (>12/20)

⏱️ ACTIVITÉ RÉCENTE
• 12 nouvelles soumissions aujourd'hui
• 8 étudiants connectés maintenant
• 5 messages non lus
• 2 demandes d'aide en attente

📊 Voir graphiques détaillés
📋 Générer rapport PDF
📧 Envoyer résumé par email
        """.trimIndent()
        
        textView.textSize = 14f
        textView.setPadding(32, 32, 32, 32)
        
        return view
    }
}