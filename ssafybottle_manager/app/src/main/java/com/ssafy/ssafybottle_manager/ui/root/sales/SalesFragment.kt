package com.ssafy.ssafybottle_manager.ui.root.sales

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.findFragment
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.ssafy.ssafybottle_manager.R
import com.ssafy.ssafybottle_manager.application.MainViewModel
import com.ssafy.ssafybottle_manager.data.dto.product.ProductSalesDto
import com.ssafy.ssafybottle_manager.databinding.FragmentSalesBinding
import com.ssafy.ssafybottle_manager.ui.adapter.ProductSalesAdapter
import com.ssafy.ssafybottle_manager.utils.FRAGMENT_PRODUCT_DETAIL
import com.ssafy.ssafybottle_manager.utils.toMoney

class SalesFragment : Fragment() {
    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var productSalesAdapter: ProductSalesAdapter

    private var productDetailFragment: ProductDetailFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initChildFragment()
        initPieChart()
        initBarChart()
        initData()
        initAdapter()
        observeData()
        otherListeners()
    }

    private fun initChildFragment() {
        childFragmentManager.let {
            productDetailFragment =
                it.findFragmentByTag(FRAGMENT_PRODUCT_DETAIL) as ProductDetailFragment?
        }

        childFragmentManager.beginTransaction().apply {
            if (productDetailFragment == null) {
                productDetailFragment = ProductDetailFragment()
                add(
                    R.id.fragmentcontainer_sales_productdetail,
                    productDetailFragment!!,
                    FRAGMENT_PRODUCT_DETAIL
                )
            }

            hide(productDetailFragment!!)
            commit()
        }
    }

    private fun initData() {
        mainViewModel.getProductSales()
    }

    private fun initAdapter() {
        productSalesAdapter = ProductSalesAdapter().apply {
            onItemClickListener = productSalesItemClickListener
        }
        binding.recyclerSales.apply {
            adapter = productSalesAdapter
        }
    }

    private val productSalesItemClickListener: (View, Int) -> Unit = { _, position ->
        showProductDetailFragment()
        productDetailFragment!!.getProductDetail(mainViewModel.productSalesList.value!![position].productId)
    }

    private fun showProductDetailFragment() {
        childFragmentManager.beginTransaction().apply {
            if (!productDetailFragment!!.isVisible) {
                setCustomAnimations(R.anim.from_right, R.anim.to_left)
            }
            show(productDetailFragment!!)
            commit()
        }
        binding.fragmentcontainerSalesProductdetail.visibility = View.VISIBLE
    }

    fun hideProductDetailFragment() {
        childFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.from_left, R.anim.to_right)
            hide(productDetailFragment!!)
            commit()
        }
    }

    private fun observeData() {
        mainViewModel.productSalesList.observe(viewLifecycleOwner) {
            analyzeData(it)
            productSalesAdapter.apply {
                productSales = it.sortedByDescending { p -> p.rating }
                notifyDataSetChanged()
            }
        }
    }

    private fun analyzeData(salesList: List<ProductSalesDto>) {
        var totalBeverage = 0
        var totalDessert = 0

        salesList.forEach {
            if (it.type == "coffee") {
                totalBeverage += it.sales
            } else {
                totalDessert += it.sales
            }
        }

        setDataTotalCost(totalBeverage, totalDessert)
        setDataPieChart(totalBeverage, totalDessert)
        setDataBarChart(salesList.sortedByDescending { it.sales })
    }

    private fun setDataTotalCost(totalBeverage: Int, totalDessert: Int) {
        binding.apply {
            textSalesBeverage.text = "${toMoney(totalBeverage)}???"
            textSalesDessert.text = "${toMoney(totalDessert)}???"
            textSalesCost.text = "${toMoney(totalBeverage + totalDessert)}???"
        }
    }

    private fun setDataPieChart(totalBeverage: Int, totalDessert: Int) {
        val pieEntries = mutableListOf<PieEntry>()
        pieEntries.add(PieEntry(totalBeverage.toFloat(), "??????"))
        pieEntries.add(PieEntry(totalDessert.toFloat(), "?????????"))

        val pieDataSet = PieDataSet(pieEntries, "")
        pieDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        val pieData = PieData(pieDataSet)
        pieData.setValueTextSize(14f)

        binding.piechartSales.data = pieData
        binding.piechartSales.animateXY(3000, 3000)
    }

    private fun setDataBarChart(salesList: List<ProductSalesDto>) {
        val productNames = salesList.map { it.name }

        val barEntries = mutableListOf<BarEntry>()
        salesList.forEachIndexed { index, product ->
            barEntries.add(BarEntry((index + 1).toFloat(), product.sales.toFloat()))
        }

        val barDataSet = BarDataSet(barEntries, "")
        barDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        val barData = BarData(barDataSet)
        barData.setValueTextSize(14f)

        binding.barchartSales.apply {
            xAxis.valueFormatter = MyXAxisFormatter(productNames)
            data = barData
            setVisibleXRangeMaximum(7f)
            animateY(3000) // ??????????????? ???????????? ??????????????? ??????
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun otherListeners() {
        binding.refreshSales.setOnRefreshListener {
            initData()
            binding.refreshSales.isRefreshing = false
        }
        binding.barchartSales.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    binding.refreshSales.isEnabled = false
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    binding.refreshSales.isEnabled = true
                }
            }
//            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
        binding.piechartSales.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    binding.refreshSales.isEnabled = false
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    binding.refreshSales.isEnabled = true
                }
            }
            false
        }
    }

    private fun initPieChart() {
        binding.piechartSales.apply {
            description.isEnabled = false
        }
    }

    private fun initBarChart() {
        binding.barchartSales.apply {
            description.isEnabled = false // description ????????????
            setPinchZoom(false) // ????????? ?????? ??????
            setDrawGridBackground(false) //???????????? ????????????
            axisRight.isEnabled = false // ????????? Y?????? ???????????? ??????.
            setTouchEnabled(true) // ????????? ???????????? ?????? ???????????? ??????

            axisLeft.run {
                setDrawGridLines(false) //?????? ?????? ??????
                setDrawAxisLine(false) // ??? ????????? ??????
            }
            xAxis.run {
                position = XAxis.XAxisPosition.BOTTOM//X?????? ??????????????? ??????.
                setDrawGridLines(false) // ??????
                granularity = 1f
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class MyXAxisFormatter(private val productNames: List<String>) : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return productNames[value.toInt() - 1]
        }
    }
}