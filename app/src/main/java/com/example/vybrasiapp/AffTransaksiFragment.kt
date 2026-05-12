package com.example.vybrasiapp

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment

class AffTransaksiFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_aff_komisi, container, false)
        // Sementara pakai layout komisi, nanti dibuatkan sendiri
    }
}