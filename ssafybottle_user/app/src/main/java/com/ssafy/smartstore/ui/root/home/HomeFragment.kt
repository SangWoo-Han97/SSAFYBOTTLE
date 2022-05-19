package com.ssafy.smartstore.ui.root.home

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.smartstore.R
import com.ssafy.smartstore.application.SmartStoreApplication.Companion.isOrder
import com.ssafy.smartstore.databinding.FragmentHomeBinding
import com.ssafy.smartstore.ui.adapter.OnItemClickListener
import com.ssafy.smartstore.ui.adapter.PromotionAdapter
import com.ssafy.smartstore.ui.adapter.RecentOrderAdapter
import com.ssafy.smartstore.utils.ORDER_ID
import com.ssafy.smartstore.utils.getUserId

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var recentOrderAdapter: RecentOrderAdapter
    private lateinit var promotionAdapter: PromotionAdapter

    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initAdapter()
        registerObserver()
    }

    private fun initData() {
        userId = getUserId()
        viewModel.getRecentOrders(userId)
    }

    private fun initAdapter() {
        recentOrderAdapter = RecentOrderAdapter().apply {
            this.onItemClickListener = this@HomeFragment.itemClickListener
        }
        binding.recyclerHomeRecentorder.apply {
            adapter = recentOrderAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }

        promotionAdapter = PromotionAdapter()
        binding.recyclerHomePromotion.apply {
            adapter = promotionAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }
    }

    private val itemClickListener = object : OnItemClickListener {
        override fun onItemClick(view: View, position: Int) {
            showShippingListDialog { dialog, _ ->
                viewModel.recentOrders.value!![position].let {
                    viewModel.insertRecentOrder(
                        it.o_id,
                        it.user_id
                    )
                }
                dialog.dismiss()
            }
        }
    }

    private fun showShippingListDialog(listener: DialogInterface.OnClickListener) {
        AlertDialog
            .Builder(requireContext())
            .setTitle("장바구니")
            .setMessage("최근 주문 목록을 장바구니에 담겠습니까?")
            .setPositiveButton("확인", listener)
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun registerObserver() {
        viewModel.recentOrders.observe(viewLifecycleOwner) {
            recentOrderAdapter.apply {
                recentOrders = it
                notifyDataSetChanged()
            }
        }
        viewModel.isSuccess.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "최근 주문을 장바구니에 담았습니다.", Toast.LENGTH_SHORT).show()
                requireParentFragment().findNavController()
                    .navigate(R.id.action_rootFragment_to_shoppingListFragment)
                viewModel.isSuccess.value = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}