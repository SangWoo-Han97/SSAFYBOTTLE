package com.ssafy.ssafybottle_manager.utils

/**
 Pane Menu
 */
const val MENU_TITLE = 100
const val MENU_ORDER = 101
const val MENU_ORDER_MANAGEMENT = 102
const val MENU_SALES_MANAGEMENT = 103
const val MENU_NOTIFICATION = 104
const val MENU_SETTING = 105

/**
 Fragment
 */
const val FRAGMENT_ORDER = "OrderFragment"
const val FRAGMENT_ORDER_MANAGEMENT = "OrderManagementFragment"
const val FRAGMENT_SALES = "SalesFragment"
const val FRAGMENT_NOTIFICATION = "NotificationFragment"
const val FRAGMENT_SETTING = "SettingFragment"
const val FRAGMENT_PRODUCT_DETAIL = "productDetailFragment"

/**
 Dialog
 */
const val DIALOG_STORE_INFO = "StoreInfoDialog"

/**
 API
 */
const val BASE_URL = "http://175.198.147.122:9999/rest/"
//const val BASE_URL = "http://118.37.203.111:9999/rest/"

/**
 Retrofit Response Status
 */
const val DEFAULT = 0
const val SUCCESS = 1
const val FAILURE = 2

/**
 ViewPager2
 */
const val NUM_TABS = 3

/**
 Id
 */
const val ADMIN_ID = "admin"
const val PRODUCT_ID = "product_id"
const val ORDER_ID = "order_id"
const val USER_ID = "user_id"
const val USER_PASS = "user_pass"
const val AUTO_LOGIN = "auto_login"

/**
 ROOM
 */
const val DATABASE = "app-database"
const val COMMENT = "t_comment"
const val ORDER = "t_order"
const val PRODUCT = "t_product"
const val STAMP = "t_stamp"
const val USER = "t_user"
const val NOTIFICATION = "t_notification"
const val ORDER_DETAIL = "t_order_detail"
const val ORDER_DETAIL_PRODUCT = "t_order_detail_product"
const val RECENT_ORDER = "t_recent_order"
const val SHOPPING_ITEM = "t_shopping_item"

/**
 Default
 */
const val DEFAULT_STRING = ""
const val DEFAULT_LATITUDE = 36.10830144233874
const val DEFAULT_LONGITUDE = 128.41827450414362
const val DEFAULT_TEL = "010-1234-5678"

/**
 Beacon
 */
const val STORE_DISTANCE = 1

/**
 Token
 */
const val TOKEN = "token"

/**
Time
 */
const val UPDATE_INTERVAL = 1000 // 1초
const val FASTEST_UPDATE_INTERVAL = 500 // 0.5초