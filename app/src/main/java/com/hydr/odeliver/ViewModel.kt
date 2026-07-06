package com.hydr.odeliver

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.app.Activity
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import androidx.compose.ui.graphics.Color
import com.hydr.odeliver.ui.utils.toDisplayColor
import com.hydr.odeliver.ui.utils.toDisplayText

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val db = AppDatabase.getDatabase(application)
    private val userDao = db.userDao()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _shopName = MutableStateFlow("")
    val shopName: StateFlow<String> = _shopName.asStateFlow()

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio.asStateFlow()

    init {
        auth.currentUser?.uid?.let { uid ->
            loadUserFromRoom(uid)
        }
    }

    private fun loadUserFromRoom(uid: String) {
        viewModelScope.launch {
            userDao.getUserById(uid).collectLatest { user ->
                if (user != null) {
                    _name.value = user.name
                    _email.value = user.email
                    _phoneNumber.value = user.phoneNumber
                    _shopName.value = user.shopName
                    _address.value = user.address
                    _bio.value = user.bio
                } else {
                    // If not in Room, populate from Firebase and save to Room
                    auth.currentUser?.let { firebaseUser ->
                        _name.value = firebaseUser.displayName ?: ""
                        _email.value = firebaseUser.email ?: ""
                        _phoneNumber.value = firebaseUser.phoneNumber ?: ""
                        
                        // Save the initial guest/firebase info to Room so it persists
                        val initialEntity = UserEntity(
                            uid = firebaseUser.uid,
                            name = _name.value,
                            email = _email.value,
                            phoneNumber = _phoneNumber.value
                        )
                        userDao.upsertUser(initialEntity)
                    }
                }
            }
        }
    }

    fun saveUserToRoom(onComplete: () -> Unit = {}) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                val userEntity = UserEntity(
                    uid = currentUser.uid,
                    name = _name.value,
                    email = currentUser.email ?: _email.value,
                    phoneNumber = currentUser.phoneNumber ?: _phoneNumber.value,
                    shopName = _shopName.value,
                    address = _address.value,
                    bio = _bio.value
                )
                userDao.upsertUser(userEntity)
                onComplete()
            }
        }
    }

    fun onEmailChange(newEmail: String) { _email.value = newEmail }
    fun onPhoneNumberChange(newNumber: String) { _phoneNumber.value = newNumber }
    fun onPasswordChange(newPassword: String) { _password.value = newPassword }
    fun onConfirmPasswordChange(confirmedPassword: String) { _confirmPassword.value = confirmedPassword }
    fun onOtpChange(newOtp: String) { _otp.value = newOtp }
    fun onNameChange(newName: String) { _name.value = newName }
    fun onShopNameChange(newShopName: String) { _shopName.value = newShopName }
    fun onAddressChange(newAddress: String) { _address.value = newAddress }
    fun onBioChange(newBio: String) { _bio.value = newBio }

    fun signInWithEmail(onResult: (Task<AuthResult>) -> Unit) {
        auth.signInWithEmailAndPassword(_email.value, _password.value)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.user?.uid?.let { loadUserFromRoom(it) }
                }
                onResult(task)
            }
    }

    fun signUpWithEmail(onResult: (Task<AuthResult>) -> Unit) {
        auth.createUserWithEmailAndPassword(_email.value, _password.value)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserToRoom()
                }
                onResult(task)
            }
    }

    fun signInWithCredential(credential: PhoneAuthCredential, onResult: (Task<AuthResult>) -> Unit) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.user?.uid?.let { loadUserFromRoom(it) }
                }
                onResult(task)
            }
    }

    fun signInWithGoogle(idToken: String, onResult: (Task<AuthResult>) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.user?.uid?.let { loadUserFromRoom(it) }
                }
                onResult(task)
            }
    }
}

data class DashboardUiState(
    val shopName : String = "",
    val businessAddress : String = "",
    val budget: Double = 0.0,
    val spent: Double = 0.0,
    val netSpent: Double = 0.0,
    val sales: Int = 0,
    val totalSalesAmount: Double = 0.0,
    val deliveries: Int = 0,
    val pendingDeliveriesCount: Int = 0,
    val incomingCost: Double = 0.0,
    val upcomingDeliveries: List<DeliveryUiModel> = emptyList(),
    val allDeliveries: List<DeliveryUiModel> = emptyList(),
    val salesRecords: List<SaleUiModel> = emptyList()
)

data class DeliveryUiModel(
    val id: Int,
    val time: String,
    val date: String = "",
    val itemName: String,
    val customerName: String,
    val status: String,
    val statusEnum: DeliveryStatus,
    val statusColor: Color,
    val isLate: Boolean = false,
    val cost: Double = 0.0,
    val numberOfProducts: Int = 1,
    val isPricePerItem: Boolean = false,
    val isOutgoing: Boolean = true,
    val notes: String = ""
)

data class SaleUiModel(
    val id: Int,
    val customerName: String,
    val productNumber: String,
    val price: Double,
    val quantity: String,
    val date: String,
    val time: String
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val deliveryDao = db.deliveryDao()
    private val userDao = db.userDao()
    private val saleDao = db.saleDao()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            observeUser(currentUser.uid)
            startDataObservation(currentUser.uid)
        } else {
            _uiState.value = _uiState.value.copy(
                shopName = "Guest Account",
                businessAddress = "Guest Location"
            )
        }
    }

    private fun observeUser(uid: String) {
        viewModelScope.launch {
            userDao.getUserById(uid).collectLatest { user ->
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        shopName = user.shopName.ifEmpty { "New Shop" },
                        businessAddress = user.address.ifEmpty { "Add Address" },
                        budget = user.budget
                    )
                } else {
                    val currentUser = auth.currentUser
                    _uiState.value = _uiState.value.copy(
                        shopName = currentUser?.displayName ?: "Welcome",
                        businessAddress = "Setting up..."
                    )
                }
            }
        }
    }

    private fun startDataObservation(uid: String) {
        viewModelScope.launch {
            combine(
                deliveryDao.getDeliveriesByUser(uid),
                saleDao.getSalesByUser(uid)
            ) { deliveries, sales ->
                val mappedDeliveries = deliveries.map {
                    DeliveryUiModel(
                        id = it.id,
                        time = it.time,
                        date = it.date,
                        itemName = it.itemName,
                        customerName = it.customerName,
                        status = it.status.toDisplayText(),
                        statusEnum = it.status,
                        statusColor = it.status.toDisplayColor(),
                        isLate = it.wasLate,
                        cost = it.cost,
                        numberOfProducts = it.numberOfProducts,
                        isPricePerItem = it.isPricePerItem,
                        isOutgoing = it.isOutgoing,
                        notes = it.notes
                    )
                }

                val mappedSales = sales.map {
                    SaleUiModel(
                        id = it.id,
                        customerName = it.customerName,
                        productNumber = it.productNumber,
                        price = it.price,
                        quantity = it.quantity,
                        date = it.date,
                        time = it.time
                    )
                }

                // Revenue = Sales Records + Outgoing Deliveries (that are Delivered)
                val salesRevenue = sales.sumOf { it.price }
                val deliveredOutgoing = deliveries.filter { it.isOutgoing && it.status == DeliveryStatus.DELIVERED }
                val deliveryRevenue = deliveredOutgoing.sumOf { if (it.isPricePerItem) it.cost * it.numberOfProducts else it.cost }
                
                val totalRevenue = salesRevenue + deliveryRevenue
                val totalSalesCount = sales.size + deliveredOutgoing.size

                // Expenses = Incoming deliveries that have been DELIVERED
                val totalExpenses = deliveries
                    .filter { !it.isOutgoing && it.status == DeliveryStatus.DELIVERED }
                    .sumOf { if (it.isPricePerItem) it.cost * it.numberOfProducts else it.cost }
                
                // Total cost of ALL incoming deliveries (Inventory value)
                val incomingCost = deliveries
                    .filter { !it.isOutgoing }
                    .sumOf { if (it.isPricePerItem) it.cost * it.numberOfProducts else it.cost }

                val pendingCount = deliveries.count { it.status != DeliveryStatus.DELIVERED && it.status != DeliveryStatus.CANCELLED }
                
                _uiState.value.copy(
                    allDeliveries = mappedDeliveries,
                    upcomingDeliveries = mappedDeliveries.filter { 
                        it.statusEnum != DeliveryStatus.DELIVERED && it.statusEnum != DeliveryStatus.CANCELLED
                    },
                    salesRecords = mappedSales,
                    deliveries = deliveries.size,
                    sales = totalSalesCount,
                    spent = totalExpenses,
                    totalSalesAmount = totalRevenue,
                    incomingCost = incomingCost,
                    pendingDeliveriesCount = pendingCount,
                    netSpent = totalRevenue - totalExpenses
                )
            }.collectLatest { newState ->
                _uiState.value = newState
            }
        }
    }

    fun addDelivery(
        time: String,
        date: String,
        itemName: String,
        customerName: String,
        cost: Double,
        status: DeliveryStatus,
        numberOfProducts: Int = 1,
        isPricePerItem: Boolean = false,
        isOutgoing: Boolean = true,
        notes: String = ""
    ) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            val delivery = DeliveryEntity(
                uid = currentUser.uid,
                time = time,
                date = date,
                itemName = itemName,
                customerName = customerName,
                cost = cost,
                numberOfProducts = numberOfProducts,
                isPricePerItem = isPricePerItem,
                isOutgoing = isOutgoing,
                notes = notes,
                status = status
            )
            deliveryDao.upsertDelivery(delivery)
        }
    }

    fun addSale(
        customerName: String,
        productNumber: String,
        price: Double,
        quantity: String,
        date: String,
        time: String
    ) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            val sale = SaleEntity(
                uid = currentUser.uid,
                customerName = customerName,
                productNumber = productNumber,
                price = price,
                quantity = quantity,
                date = date,
                time = time
            )
            saleDao.upsertSale(sale)
        }
    }

    fun deleteSale(id: Int) {
        viewModelScope.launch {
            saleDao.deleteSaleById(id)
        }
    }

    fun updateBudget(newBudget: Double) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            val user = userDao.getUserById(currentUser.uid).firstOrNull()
            user?.let {
                val updatedUser = it.copy(budget = newBudget)
                userDao.upsertUser(updatedUser)
            }
        }
    }

    fun updateDelivery(delivery: DeliveryUiModel, newStatus: DeliveryStatus, wasLate: Boolean, isOutgoing: Boolean? = null) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            val entity = DeliveryEntity(
                id = delivery.id,
                uid = currentUser.uid,
                time = delivery.time,
                date = delivery.date,
                itemName = delivery.itemName,
                customerName = delivery.customerName,
                cost = delivery.cost,
                numberOfProducts = delivery.numberOfProducts,
                isPricePerItem = delivery.isPricePerItem,
                isOutgoing = isOutgoing ?: delivery.isOutgoing,
                notes = delivery.notes,
                status = newStatus,
                wasLate = wasLate
            )
            deliveryDao.upsertDelivery(entity)
        }
    }

    fun deleteDelivery(id: Int) {
        viewModelScope.launch {
            deliveryDao.deleteDeliveryById(id)
        }
    }
}
