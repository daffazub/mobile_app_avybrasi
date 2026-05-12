package com.example.vybrasiapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class AdminDashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuTambahAffiliate  = view.findViewById<MaterialCardView>(R.id.adminMenuTambahAffiliate)
        val menuDaftarAffiliate  = view.findViewById<MaterialCardView>(R.id.adminMenuDaftarAffiliate)
        val menuKatalog          = view.findViewById<MaterialCardView>(R.id.adminMenuKatalog)
        val menuTransaksi        = view.findViewById<MaterialCardView>(R.id.adminMenuTransaksi)

        menuTambahAffiliate.setOnClickListener {
            startActivity(Intent(requireContext(), TambahAffiliateActivity::class.java))
        }

        menuDaftarAffiliate.setOnClickListener {
            startActivity(Intent(requireContext(), AffiliateListActivity::class.java))
        }

        menuKatalog.setOnClickListener {
            startActivity(Intent(requireContext(), KatalogActivity::class.java))
        }

        menuTransaksi.setOnClickListener {
            Toast.makeText(requireContext(), "Laporan Transaksi — Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }
}