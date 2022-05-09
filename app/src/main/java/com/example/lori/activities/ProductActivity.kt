package com.example.lori.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lori.R
import com.example.lori.activities.adapters.CommentAdapter
import com.example.lori.models.*
import com.example.lori.utils.Constants
import com.example.lori.utils.FormatUtils
import com.example.lori.utils.ImageUtils
import com.example.lori.widgets.BoldMontserratButton
import com.example.lori.widgets.BoldMontserratTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_product.*
import kotlinx.android.synthetic.main.layout_dialog_add_comments.*
import kotlinx.android.synthetic.main.layout_dialog_list_comments.*

class ProductActivity : BaseActivity(), View.OnClickListener {
    private var product: Product? = null
    private var productOwnerId = ""

    private lateinit var rvComments: RecyclerView
    val adapter = CommentAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        getProduct(intent.getStringExtra(Constants.EXTRA_PRODUCT_ID) ?: "")
        productOwnerId = intent.getStringExtra(Constants.EXTRA_PRODUCT_OWNER_ID) ?: ""

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
                startActivity(Intent(this, CartItemActivity::class.java))
            }
            R.id.btListComment -> {
                getAllComments()
            }
            R.id.btAddComment -> {
                createOrUpdateComment(true)
            }
        }
    }

    private fun getProduct(id: String) {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.PRODUCTS)
            .document(id)
            .get()
            .addOnSuccessListener { doc ->
                product = doc.toObject(Product::class.java)!!
                product!!.id = id

                ImageUtils.loadProductImage(this, product!!.image, ivProductDetailImage)
                FirebaseFirestore.getInstance()
                    .collection(Constants.USERS)
                    .document(product!!.uid)
                    .get()
                    .addOnSuccessListener { doc1 ->
                        val user = doc1.toObject(User::class.java)!!
                        tvProductOwnerName.text =
                            "${product!!.username}\n${user.mobile}\n${user.email}"
                    }
                    .addOnFailureListener {
                        tvProductOwnerName.text = product!!.username
                    }
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
                    // todo get products from cart via cart items
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

                            // todo show btAddToCart if product not exist in cart
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

                getFavProduct()
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                showSnackBar(resources.getString(R.string.fail_to_get_product_details), true)

                Log.e(javaClass.simpleName, "Errors while getting product", e)
            }
    }

    private fun getFavProduct() {
        FirebaseFirestore.getInstance()
            .collection(Constants.FAV_PRODUCTS)
            .whereEqualTo(Constants.UID, FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo(Constants.PID, product!!.id)
            .get()
            .addOnSuccessListener { query ->
                ivAddToFav.setImageResource(
                    if (query.documents.isNotEmpty()) R.drawable.ic_favorite_24dp
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
            product_owner_id = productOwnerId, // seller id
            createdAt = System.currentTimeMillis(),
        )

        FirebaseFirestore.getInstance()
            .collection(Constants.FAV_PRODUCTS)
            .whereEqualTo(Constants.UID, FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo(Constants.PID, favProduct.pid)
            .get()
            .addOnSuccessListener { query ->
                query.documents.forEach { doc ->
                    if (doc.toObject(FavProduct::class.java) != null) {
                        return@addOnSuccessListener
                    }
                }

                FirebaseFirestore.getInstance()
                    .collection(Constants.FAV_PRODUCTS)
                    .document()
                    .set(favProduct, SetOptions.merge())
                    .addOnSuccessListener {
                        showSnackBar(resources.getString(R.string.success_to_add_fav_product), false)
                        ivAddToFav.setImageResource(R.drawable.ic_favorite_24dp)
                    }
                    .addOnFailureListener {
                        showSnackBar(resources.getString(R.string.fail_to_add_fav_product), true)
                        ivAddToFav.setImageResource(R.drawable.ic_favorite_border_24dp)
                    }
            }
            .addOnFailureListener { e ->
                showSnackBar(resources.getString(R.string.fail_to_add_fav_product), false)
                Log.e(javaClass.simpleName, "Errors while getting fav products", e)
            }
    }

    private fun getAllComments() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.COMMENTS)
            .whereEqualTo(Constants.PID, product!!.id)
            .orderBy(Constants.CREATED_AT, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { query ->
                hideProgressDialog()

                val comments = ArrayList<Comment>()
                query.documents.forEach { doc ->
                    val comment = doc.toObject(Comment::class.java)!!
                    comment.id = doc.id
                    comments.add(comment)
                }

                val lp = WindowManager.LayoutParams()
                val dialog = Dialog(this@ProductActivity)
                dialog.setContentView(R.layout.layout_dialog_list_comments)
                lp.copyFrom(dialog.window!!.attributes)
                lp.width = WindowManager.LayoutParams.MATCH_PARENT
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT
                dialog.show()
                dialog.window!!.attributes = lp

                dialog.findViewById<BoldMontserratButton>(R.id.btCancel).setOnClickListener { dialog.dismiss() }

                adapter.listener = object : CommentAdapter.Listener {
                    // todo update comments
                    override fun onClick(position: Int) {
                        if (comments[position].uid == FirebaseAuth.getInstance().currentUser!!.uid) {
                            createOrUpdateComment(false, adapter.currentList[position])
                        }
                    }

                    override fun onLongClick(position: Int) {
                        if (comments[position].uid == FirebaseAuth.getInstance().currentUser!!.uid) {
                            // todo delete product from Firebase
                            AlertDialog.Builder(this@ProductActivity)
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

                                            // todo delete product from RecyclerView
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
                rvComments = dialog.rvComments
                rvComments.adapter = adapter
                rvComments.setHasFixedSize(true)
                rvComments.layoutManager = LinearLayoutManager(this)

                if (comments.size > 0) {
                    rvComments.visibility = View.VISIBLE
                    dialog.tvNoCommentsFound.visibility = View.GONE
                    adapter.submitList(comments)
                } else {
                    rvComments.visibility = View.GONE
                    dialog.tvNoCommentsFound.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                showSnackBar(resources.getString(R.string.fail_to_list_all_comments), true)
                Log.e(javaClass.simpleName, "Errors while getting all comments", e)
            }
    }

    private fun createOrUpdateComment(addNew: Boolean = true, comment: Comment? = null) {
        val lp = WindowManager.LayoutParams()
        val dialog = Dialog(this@ProductActivity)
        dialog.setContentView(R.layout.layout_dialog_add_comments)
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.show()
        dialog.window!!.attributes = lp

        val tvTitleDialog = dialog.findViewById<BoldMontserratTextView>(R.id.tvTitle)

        dialog.spCommentStar.adapter = ArrayAdapter(
            this@ProductActivity,
            android.R.layout.simple_list_item_1,
            mutableListOf(5, 4, 3, 2, 1)
        )

        if (addNew) {
            tvTitleDialog.setText(R.string.title_add_comments)
        } else if (!addNew && comment != null) {
            tvTitleDialog.setText(R.string.title_update_comments)
            dialog.etCommentContent.setText(comment.content)
        }

        dialog.findViewById<BoldMontserratButton>(R.id.btCancel).setOnClickListener { dialog.dismiss() }
        dialog.btSubmit.setOnClickListener {
            showProgressDialog(resources.getString(R.string.label_please_wait))

            if (addNew) {
                FirebaseFirestore.getInstance()
                    .collection(Constants.USERS)
                    .get()
                    .addOnSuccessListener { query ->
                        query.documents.forEach { item ->
                            if (item.id == FirebaseAuth.getInstance().currentUser!!.uid) {
                                val user = item.toObject(User::class.java)!!
                                val newComment = Comment(
                                    content = dialog.etCommentContent.text.toString(),
                                    start = dialog.spCommentStar.selectedItem as Int,
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
                                        Log.e(javaClass.simpleName, "Errors while creating comments", e)
                                    }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        dialog.dismiss()
                        Log.e(javaClass.simpleName, "Errors while getting user details", e)
                    }
            } else if (!addNew && comment != null) {
                comment.content = dialog.etCommentContent.text.toString()
                comment.start = dialog.spCommentStar.selectedItem as Int
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
            pid = product!!.id,
            product_owner_id = productOwnerId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
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

                Log.e(javaClass.simpleName, "Errors while creating cart items", e)
            }
    }
}
