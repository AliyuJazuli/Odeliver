package com.hydr.odeliver

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.graphics.Color
import com.hydr.odeliver.ui.utils.toDisplayColor
import com.hydr.odeliver.ui.utils.toDisplayText
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import java.util.UUID

const val DEFAULT_USER_ID = "default_user"

data class NavigationUiState(
    val isProfileComplete: Boolean = false,
    val isOnboardingCompleted: Boolean = false,
    val isInitialized: Boolean = false
)

data class DashboardUiState(
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val shopName : String = "",
    val businessAddress : String = "",
    val bio: String = "",
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
    val salesRecords: List<SaleUiModel> = emptyList(),
    val notifications: List<NotificationUiModel> = emptyList()
)

enum class NotificationType { DELIVERY_REMINDER, WEEKLY_SUMMARY, MONTHLY_SUMMARY }

data class NotificationUiModel(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val metadata: String? = null // e.g., "WEEKLY" or "MONTHLY"
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

fun DeliveryEntity.toUiModel() = DeliveryUiModel(
    id = id,
    time = time,
    date = date,
    itemName = itemName,
    customerName = customerName,
    status = status.toDisplayText(),
    statusEnum = status,
    statusColor = status.toDisplayColor(),
    isLate = wasLate,
    cost = cost,
    numberOfProducts = numberOfProducts,
    isPricePerItem = isPricePerItem,
    isOutgoing = isOutgoing,
    notes = notes
)

fun SaleEntity.toUiModel() = SaleUiModel(
    id = id,
    customerName = customerName,
    productNumber = productNumber,
    price = price,
    quantity = quantity,
    date = date,
    time = time
)

@HiltViewModel
class HomeViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val deliveryDao = db.deliveryDao()
    private val userDao = db.userDao()
    private val saleDao = db.saleDao()

    // Base flows
    private val _deliveries = deliveryDao.getDeliveriesByUser(DEFAULT_USER_ID).distinctUntilChanged()
    private val _sales = saleDao.getAllSalesByUser(DEFAULT_USER_ID).distinctUntilChanged()

    // Mapped UI models (Cached with stateIn)
    private val mappedDeliveries = _deliveries.map { deliveries ->
        deliveries.map { it.toUiModel() }
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val mappedSales = _sales.map { sales ->
        sales.filter { !it.isSoftDeleted }.map { it.toUiModel() }
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Financial Metrics
    private val financials = combine(_deliveries, _sales) { deliveries, sales ->
        val salesRevenue = sales.sumOf { it.price }
        val deliveredOutgoing = deliveries.filter { it.isOutgoing && it.status == DeliveryStatus.DELIVERED }
        val deliveryRevenue = deliveredOutgoing.sumOf { if (it.isPricePerItem) it.cost * it.numberOfProducts else it.cost }
        
        val totalRevenue = salesRevenue + deliveryRevenue
        val totalExpenses = deliveries
            .filter { !it.isOutgoing && it.status == DeliveryStatus.DELIVERED }
            .sumOf { if (it.isPricePerItem) it.cost * it.numberOfProducts else it.cost }
        
        val incomingCost = deliveries
            .filter { !it.isOutgoing }
            .sumOf { if (it.isPricePerItem) it.cost * it.numberOfProducts else it.cost }

        val activeSalesCount = sales.count { !it.isSoftDeleted }
        val totalSalesCount = activeSalesCount + deliveredOutgoing.size
        
        Triple(totalRevenue, totalExpenses, Triple(incomingCost, totalSalesCount, deliveries.size))
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Lazily, Triple(0.0, 0.0, Triple(0.0, 0, 0)))

    // Navigation state flow
    val navigationState: StateFlow<NavigationUiState> = userDao.getUserById(DEFAULT_USER_ID)
        .distinctUntilChanged()
        .map { user ->
            NavigationUiState(
                isProfileComplete = user != null && user.name.isNotEmpty() && user.name != "My Business",
                isOnboardingCompleted = user?.isOnboardingCompleted ?: false,
                isInitialized = true
            )
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, NavigationUiState())

    // Memoization variables for notifications
    private var lastDeliveriesForNotifications: List<DeliveryUiModel>? = null
    private var lastGeneratedNotifications: List<NotificationUiModel> = emptyList()

    // Final UI State
    val uiState: StateFlow<DashboardUiState> = combine(
        mappedDeliveries,
        mappedSales,
        financials,
        userDao.getUserById(DEFAULT_USER_ID).distinctUntilChanged()
    ) { deliveries, sales, fin, user ->
        val (revenue, expenses, stats) = fin
        val (incomingCost, totalSalesCount, totalDeliveries) = stats
        
        DashboardUiState(
            name = user?.name ?: "",
            email = user?.email ?: "",
            phoneNumber = user?.phoneNumber ?: "",
            shopName = user?.shopName ?: "",
            businessAddress = user?.address ?: "",
            bio = user?.bio ?: "",
            budget = user?.budget ?: 0.0,
            spent = expenses,
            totalSalesAmount = revenue,
            incomingCost = incomingCost,
            sales = totalSalesCount,
            deliveries = totalDeliveries,
            netSpent = revenue - expenses,
            allDeliveries = deliveries,
            upcomingDeliveries = deliveries.filter { it.statusEnum != DeliveryStatus.DELIVERED && it.statusEnum != DeliveryStatus.CANCELLED },
            salesRecords = sales,
            pendingDeliveriesCount = deliveries.count { it.statusEnum != DeliveryStatus.DELIVERED && it.statusEnum != DeliveryStatus.CANCELLED },
            notifications = generateNotifications(deliveries)
        )
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Eagerly, DashboardUiState())

    // Exposed flows for Profile editing
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _shopName = MutableStateFlow("")
    val shopName: StateFlow<String> = _shopName.asStateFlow()

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio.asStateFlow()
    
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private fun ensureDefaultUser() {
        viewModelScope.launch {
            val user = userDao.getUserById(DEFAULT_USER_ID).firstOrNull()
            if (user == null) {
                userDao.upsertUser(UserEntity(
                    uid = DEFAULT_USER_ID, 
                    name = "", 
                    shopName = "",
                    address = "",
                    email = "",
                    phoneNumber = "",
                    bio = "",
                    isOnboardingCompleted = false
                ))
            } else {
                _name.value = user.name
                _shopName.value = user.shopName
                _address.value = user.address
                _bio.value = user.bio
                _email.value = user.email
                _phoneNumber.value = user.phoneNumber
            }
        }
    }

    init {
        ensureDefaultUser()
    }

    private fun generateNotifications(deliveries: List<DeliveryUiModel>): List<NotificationUiModel> {
        if (deliveries == lastDeliveriesForNotifications) {
            return lastGeneratedNotifications
        }
        val newNotifications = mutableListOf<NotificationUiModel>()
        val sdf = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        val todayStr = sdf.format(Date())
        
        deliveries.filter { it.date == todayStr && it.statusEnum != DeliveryStatus.DELIVERED && it.statusEnum != DeliveryStatus.CANCELLED }
            .forEach { delivery ->
                newNotifications.add(
                    NotificationUiModel(
                        id = "delivery_${delivery.id}",
                        title = if (delivery.isOutgoing) "Outgoing Delivery Today" else "Incoming Delivery Today",
                        message = "${delivery.itemName} ${if (delivery.isOutgoing) "to" else "from"} ${delivery.customerName} at ${delivery.time}",
                        type = NotificationType.DELIVERY_REMINDER
                    )
                )
            }
        
        newNotifications.add(NotificationUiModel(id = "weekly_summary", title = "Weekly Performance Summary", message = "Your weekly report is ready.", type = NotificationType.WEEKLY_SUMMARY, metadata = "WEEKLY"))
        newNotifications.add(NotificationUiModel(id = "monthly_summary", title = "Monthly Business Overview", message = "See how your shop performed this month.", type = NotificationType.MONTHLY_SUMMARY, metadata = "MONTHLY"))
        
        lastDeliveriesForNotifications = deliveries
        lastGeneratedNotifications = newNotifications
        return newNotifications
    }

    // Profile updates
    fun onNameChange(newName: String) { _name.value = newName }
    fun onShopNameChange(newShopName: String) { _shopName.value = newShopName }
    fun onAddressChange(newAddress: String) { _address.value = newAddress }
    fun onBioChange(newBio: String) { _bio.value = newBio }
    fun onEmailChange(newEmail: String) { _email.value = newEmail }
    fun onPhoneNumberChange(newNumber: String) { _phoneNumber.value = newNumber }

    fun completeOnboarding() {
        viewModelScope.launch {
            val user = userDao.getUserById(DEFAULT_USER_ID).firstOrNull()
            if (user != null) {
                userDao.upsertUser(user.copy(isOnboardingCompleted = true))
            } else {
                userDao.upsertUser(UserEntity(
                    uid = DEFAULT_USER_ID,
                    isOnboardingCompleted = true
                ))
            }
        }
    }

    fun saveUser(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val currentUser = userDao.getUserById(DEFAULT_USER_ID).firstOrNull()
            val userEntity = UserEntity(
                uid = DEFAULT_USER_ID,
                name = _name.value,
                email = _email.value,
                phoneNumber = _phoneNumber.value,
                shopName = _shopName.value,
                address = _address.value,
                bio = _bio.value,
                budget = uiState.value.budget,
                isOnboardingCompleted = currentUser?.isOnboardingCompleted ?: true
            )
            userDao.upsertUser(userEntity)
            onComplete()
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
        viewModelScope.launch {
            val delivery = DeliveryEntity(
                uid = DEFAULT_USER_ID,
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
        viewModelScope.launch {
            val sale = SaleEntity(
                uid = DEFAULT_USER_ID,
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

    fun updateSale(
        id: Int,
        customerName: String,
        productNumber: String,
        price: Double,
        quantity: String,
        date: String,
        time: String
    ) {
        viewModelScope.launch {
            val sale = SaleEntity(
                id = id,
                uid = DEFAULT_USER_ID,
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

    fun deleteSale(id: Int, reverseTransaction: Boolean = false) {
        viewModelScope.launch {
            if (reverseTransaction) {
                saleDao.deleteSaleById(id)
            } else {
                saleDao.softDeleteSaleById(id)
            }
        }
    }

    fun updateBudget(newBudget: Double) {
        viewModelScope.launch {
            val user = userDao.getUserById(DEFAULT_USER_ID).firstOrNull()
            user?.let {
                val updatedUser = it.copy(budget = newBudget)
                userDao.upsertUser(updatedUser)
            }
        }
    }

    fun updateDelivery(delivery: DeliveryUiModel, newStatus: DeliveryStatus, wasLate: Boolean, isOutgoing: Boolean? = null) {
        viewModelScope.launch {
            val entity = DeliveryEntity(
                id = delivery.id,
                uid = DEFAULT_USER_ID,
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

    fun clearAllDeliveries() {
        viewModelScope.launch {
            deliveryDao.clearAllDeliveries()
        }
    }

    fun clearAllSales() {
        viewModelScope.launch {
            saleDao.clearAllSales()
        }
    }

    fun deleteAccount(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            deliveryDao.clearAllDeliveries()
            saleDao.clearAllSales()
            userDao.clearAllUsers()
            ensureDefaultUser()
            onComplete()
        }
    }
}
