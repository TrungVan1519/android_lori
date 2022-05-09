package com.example.lori.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.lori.R
import com.example.lori.models.SoldProduct
import com.example.lori.utils.Constants
import com.example.lori.utils.FormatUtils
import com.example.lori.utils.ImageUtils
import kotlinx.android.synthetic.main.activity_sold_product.*
import java.text.SimpleDateFormat
import java.util.*

class SoldProductActivity : AppCompatActivity() {
    private lateinit var soldProduct: SoldProduct

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sold_product)

        soldProduct = intent.getParcelableExtra(Constants.EXTRA_SOLD_PRODUCT_DETAILS)!!
        tvOrderDetailsId.text = soldProduct.oid

        val cal = Calendar.getInstance()
        cal.timeInMillis = soldProduct.order_datetime
        tvOrderDetailsDate.text =
            SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(cal.time)

        ImageUtils.loadProductImage(this, soldProduct.image, ivSoldProductImage)
        tvSoldProductName.text = soldProduct.title
        tvSoldProductPrice.text = "${FormatUtils.format(num = soldProduct.price)} VND"
        tvSoldProductQuantity.text = soldProduct.sold_quantity.toString()

        tvMyOrderDetailsAddressType.text = soldProduct.address.type
        tvMyOrderDetailsFullName.text = soldProduct.address.name
        tvMyOrderDetailsAddress.text =
            "${soldProduct.address.address} - ${soldProduct.address.zipCode}"
        tvMyOrderDetailsAdditionalNote.text = soldProduct.address.additionalNote
        tvMyOrderDetailsAdditionalNote.visibility =
            if (soldProduct.address.additionalNote.isNotEmpty()) View.VISIBLE else View.GONE
        tvMyOrderDetailsOtherDetails.text = soldProduct.address.otherDetails
        tvMyOrderDetailsOtherDetails.visibility =
            if (soldProduct.address.otherDetails.isNotEmpty()) View.VISIBLE else View.GONE
        tvMyOrderDetailsMobileNumber.text = soldProduct.address.mobile
        
        tvOrderDetailsSubTotal.text = "${FormatUtils.format(num = soldProduct.subTotalAmount)} VND"
        tvOrderDetailsShippingCharge.text =
            "${FormatUtils.format(num = Constants.SHIPPING_CHARGE)} VND"
        tvOrderDetailsTotalAmount.text = "${FormatUtils.format(num = soldProduct.totalAmount)} VND"
    }
}
