package com.example.lori.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lori.R
import com.example.lori.activities.adapters.CommentsAdapter
import com.example.lori.models.*
import com.example.lori.utils.Constants
import com.example.lori.utils.FormatUtils
import com.example.lori.utils.ImageUtils
import com.example.lori.widgets.BoldMontserratButton
import com.example.lori.widgets.BoldMontserratEditText
import com.example.lori.widgets.BoldMontserratTextView
import com.example.lori.widgets.RegularMontserratTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_product_details.*

class ProductDetailsActivity : BaseActivity(), View.OnClickListener {

    private var product: Product? = null

    private lateinit var rvComments: RecyclerView
    val adapter = CommentsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        getProductDetails(intent.getStringExtra(Constants.EXTRA_PRODUCT_ID) ?: "")

        ivAddToFav.setOnClickListener(this)
        btAddToCart.setOnClickListener(this)
        btGoToCart.setOnClickListener(this)
        btListComment.setOnClickListener(this)
        btAddComment.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivAddToFav -> {
                addToFav()
            }
            R.id.btAddToCart -> {
                addToCart()
            }
            R.id.btGoToCart -> {
                startActivity(Intent(this, CartActivity::class.java))
            }
            R.id.btListComment -> {
                getAllComments()
            }
            R.id.btAddComment -> {
                addOrUpdateComment(true)
            }
        }
    }

    private fun getProductDetails(id: String) {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.PRODUCTS)
            .document(id)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                product = documentSnapshot.toObject(Product::class.java)!!
                product!!.id = id

                ImageUtils.loadProductImage(this, product!!.image, ivProductDetailImage)
                tvProductOwnerName.text = "By owner: ${product!!.username}"
                tvProductDetailsTitle.text = product!!.title
                tvProductDetailsPrice.text = "${FormatUtils.format(num = product!!.price)} VND"
                tvProductDetailsDescription.text = product!!.description

                if (product!!.stock_quantity == 0) {
                    tvProductDetailsStockQuantity.text =
                        resources.getString(R.string.label_out_of_stock)
                    tvProductDetailsStockQuantity.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.snack_bar_error
                        )
                    )
                } else {
                    tvProductDetailsStockQuantity.text =
                        FormatUtils.format(num = product!!.stock_quantity)
                }

                if (FirebaseAuth.getInstance().currentUser!!.uid == product!!.uid || product!!.stock_quantity <= 0) {
                    hideProgressDialog()
                } else {
                    // Get products from cart via cart items
                    FirebaseFirestore.getInstance()
                        .collection(Constants.CART_ITEMS)
                        .whereEqualTo(
                            Constants.UID,
                            FirebaseAuth.getInstance().currentUser!!.uid
                        )
                        .whereEqualTo(Constants.PID, product!!.id)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            hideProgressDialog()

                            // show btAddToCart if product not exist in cart
                            if (snapshot.documents.size <= 0) {
                                btAddToCart.visibility = View.VISIBLE
                            }
                        }
                        .addOnFailureListener { e ->
                            hideProgressDialog()
                            showSnackBar(
                                resources.getString(R.string.fail_to_check_existing_cart_items),
                                true
                            )

                            Log.e(
                                javaClass.simpleName,
                                "Error while checking existing cart items",
                                e
                            )
                        }
                }

                getFavProductDetails()
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                showSnackBar(resources.getString(R.string.fail_to_get_product_details), true)

                Log.e(javaClass.simpleName, "Errors while getting product details", e)
            }
    }

    private fun getFavProductDetails() {
        FirebaseFirestore.getInstance()
            .collection(Constants.FAV_PRODUCTS)
            .whereEqualTo(Constants.UID, FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo(Constants.PID, product!!.id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                ivAddToFav.setImageResource(
                    if (querySnapshot.documents.isNotEmpty()) R.drawable.ic_favorite_24dp
                    else R.drawable.ic_favorite_border_24dp
                )
            }
            .addOnFailureListener {
                ivAddToFav.setImageResource(R.drawable.ic_favorite_border_24dp)
            }
    }

    private fun addToFav() {
        val favProduct = FavProduct(
            image = product!!.image,
            title = product!!.title,
            price = product!!.price,
            uid = FirebaseAuth.getInstance().currentUser!!.uid,
            pid = product!!.id,
            createdAt = System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection(Constants.FAV_PRODUCTS)
            .whereEqualTo(Constants.UID, FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo(Constants.PID, favProduct.pid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.forEach { documentSnapshot ->
                    if (documentSnapshot.toObject(FavProduct::class.java) != null) {
                        return@addOnSuccessListener;
                    }
                }

                FirebaseFirestore.getInstance()
                    .collection(Constants.FAV_PRODUCTS)
                    .document()
                    .set(favProduct, SetOptions.merge())
                    .addOnSuccessListener {
                        ivAddToFav.setImageResource(R.drawable.ic_favorite_24dp)
                    }
                    .addOnFailureListener {
                        ivAddToFav.setImageResource(R.drawable.ic_favorite_border_24dp)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(javaClass.simpleName, "Errors while getting favorite products", e)
            }
    }

    private fun getAllComments() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.COMMENTS)
            .whereEqualTo(Constants.PID, product!!.id)
            .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                hideProgressDialog()

                val comments = ArrayList<Comment>()
                querySnapshot.documents.forEach { documentSnapshot ->
                    val comment = documentSnapshot.toObject(Comment::class.java)!!
                    comment.id = documentSnapshot.id
                    comments.add(comment)
                }

                val lp = WindowManager.LayoutParams()
                val dialog = Dialog(this@ProductDetailsActivity)
                dialog.setContentView(R.layout.layout_dialog_list_comments)
                lp.copyFrom(dialog.window!!.attributes)
                lp.width = WindowManager.LayoutParams.MATCH_PARENT
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT
                dialog.show()
                dialog.window!!.attributes = lp

                val btCancelDialog = dialog.findViewById<BoldMontserratButton>(R.id.btCancel)
                val tvNoCommentsFound =
                    dialog.findViewById<RegularMontserratTextView>(R.id.tvNoCommentsFound)
                rvComments = dialog.findViewById(R.id.rvComments)

                btCancelDialog.setOnClickListener { dialog.dismiss() }

                adapter.listener = object : CommentsAdapter.Listener {
                    // todo update comments
                    override fun onClick(position: Int) {
                        if (comments[position].uid == FirebaseAuth.getInstance().currentUser!!.uid) {
                            addOrUpdateComment(false, adapter.currentList[position])
                        }
                    }

                    override fun onLongClick(position: Int) {
                        if (comments[position].uid == FirebaseAuth.getInstance().currentUser!!.uid) {
                            // Delete from Firebase
                            AlertDialog.Builder(this@ProductDetailsActivity)
                                .setTitle(R.string.title_delete_dialog)
                                .setMessage(R.string.label_delete_dialog)
                                .setIcon(R.drawable.ic_delete_red_24dp)
                                .setPositiveButton(resources.getString(R.string.label_yes)) { dialogInterface, _ ->
                                    showProgressDialog(resources.getString(R.string.label_please_wait))

                                    FirebaseFirestore.getInstance()
                                        .collection(Constants.COMMENTS)
                                        .document(adapter.currentList[position].id)
                                        .delete()
                                        .addOnSuccessListener {
                                            hideProgressDialog()

                                            // Delete from RecyclerView
                                            val list = adapter.currentList.toMutableList()
                                            list.removeAt(position)
                                            adapter.submitList(list)
                                        }
                                        .addOnFailureListener { e ->
                                            hideProgressDialog()
                                            showSnackBar(
                                                resources.getString(R.string.fail_to_delete_comment),
                                                true
                                            )
                                            Log.e(
                                                javaClass.simpleName,
                                                "Errors while deleting comments",
                                                e
                                            )
                                        }
                                    dialogInterface.dismiss()
                                }
                                .setNegativeButton(resources.getString(R.string.label_no)) { dialogInterface, _ ->
                                    dialogInterface.dismiss()
                                }
                                .setCancelable(false)
                                .create()
                                .show()
                        }
                    }
                }
                rvComments.adapter = adapter
                rvComments.setHasFixedSize(true)
                rvComments.layoutManager = LinearLayoutManager(this)

                if (comments.size > 0) {
                    rvComments.visibility = View.VISIBLE
                    tvNoCommentsFound.visibility = View.GONE
                    adapter.submitList(comments)
                } else {
                    rvComments.visibility = View.GONE
                    tvNoCommentsFound.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                showSnackBar(resources.getString(R.string.fail_to_list_all_comments), true)
                Log.e(javaClass.simpleName, "Errors while listing all comments", e)
            }
    }

    private fun addOrUpdateComment(addNew: Boolean = true, comment: Comment? = null) {
        val lp = WindowManager.LayoutParams()
        val dialog = Dialog(this@ProductDetailsActivity)
        dialog.setContentView(R.layout.layout_dialog_add_comments)
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.show()
        dialog.window!!.attributes = lp

        val tvTitleDialog = dialog.findViewById<BoldMontserratTextView>(R.id.tvTitle)
        val spCommentStarDialog = dialog.findViewById<Spinner>(R.id.spCommentStar)
        val etCommentContentDialog =
            dialog.findViewById<BoldMontserratEditText>(R.id.etCommentContent)
        val btSubmitDialog = dialog.findViewById<BoldMontserratButton>(R.id.btSubmit)
        val btCancelDialog = dialog.findViewById<BoldMontserratButton>(R.id.btCancel)

        spCommentStarDialog.adapter = ArrayAdapter(
            this@ProductDetailsActivity,
            android.R.layout.simple_list_item_1,
            mutableListOf(5, 4, 3, 2, 1)
        )

        if (addNew) {
            tvTitleDialog.setText(R.string.title_add_comments)
        } else if (!addNew && comment != null) {
            tvTitleDialog.setText(R.string.title_update_comments)
            etCommentContentDialog.setText(comment.content)
        }

        btCancelDialog.setOnClickListener { dialog.dismiss() }
        btSubmitDialog.setOnClickListener {
            showProgressDialog(resources.getString(R.string.label_please_wait))

            if (addNew) {
                FirebaseFirestore.getInstance()
                    .collection(Constants.USERS)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val user = querySnapshot.documents[0].toObject(User::class.java)!!
                        val newComment = Comment(
                            content = etCommentContentDialog.text.toString(),
                            start = spCommentStarDialog.selectedItem as Int,
                            userEmail = FirebaseAuth.getInstance().currentUser!!.email.toString(),
                            userImage = user.image,
                            uid = FirebaseAuth.getInstance().currentUser!!.uid,
                            pid = product!!.id,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                        )

                        FirebaseFirestore.getInstance()
                            .collection(Constants.COMMENTS)
                            .document()
                            .set(newComment, SetOptions.merge())
                            .addOnSuccessListener {
                                hideProgressDialog()
                                showSnackBar(
                                    resources.getString(R.string.success_to_add_comment),
                                    false
                                )
                                dialog.dismiss()
                            }
                            .addOnFailureListener { e ->
                                hideProgressDialog()
                                showSnackBar(
                                    resources.getString(R.string.fail_to_add_comment),
                                    true
                                )
                                dialog.dismiss()
                                Log.e(javaClass.simpleName, "Errors while adding comments", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        dialog.dismiss()
                        Log.e(javaClass.simpleName, "Errors while getting user details", e)
                    }
            } else if (!addNew && comment != null) {
                comment.content = etCommentContentDialog.text.toString()
                comment.start = spCommentStarDialog.selectedItem as Int
                comment.updatedAt = System.currentTimeMillis()

                FirebaseFirestore.getInstance()
                    .collection(Constants.COMMENTS)
                    .document(comment.id)
                    .set(comment, SetOptions.merge())
                    .addOnSuccessListener {
                        hideProgressDialog()
                        showSnackBar(
                            resources.getString(R.string.success_to_update_comment),
                            false
                        )
                        dialog.dismiss()
                        rvComments.adapter = adapter
                    }
                    .addOnFailureListener { e ->
                        hideProgressDialog()
                        showSnackBar(
                            resources.getString(R.string.fail_to_update_comment),
                            true
                        )
                        dialog.dismiss()
                        Log.e(javaClass.simpleName, "Errors while updating comments", e)
                    }
            }
        }
    }

    private fun addToCart() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        val cartItem = CartItem(
            title = product!!.title,
            price = product!!.price,
            image = product!!.image,
            cart_quantity = Constants.DEFAULT_CART_QUANTITY,
            uid = FirebaseAuth.getInstance().uid!!,
            pid = product!!.id
        )

        FirebaseFirestore.getInstance()
            .collection(Constants.CART_ITEMS)
            .document()
            .set(cartItem, SetOptions.merge())
            .addOnSuccessListener {
                hideProgressDialog()
                showSnackBar(resources.getString(R.string.success_to_add_products_to_cart), false)

                btAddToCart.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                showSnackBar(resources.getString(R.string.fail_to_add_products_to_cart), true)

                Log.e(javaClass.simpleName, "Errors while saving cart items", e)
            }
    }
}
