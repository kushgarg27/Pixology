import android.content.Context
import android.content.SharedPreferences
import com.pixel.pixology.utills.Constants.PREFS_TOKEN_FILE
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {
    private var editor: SharedPreferences.Editor? = null

    private var prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_TOKEN_FILE, Context.MODE_PRIVATE)


//    fun getCityName(): String {
//        return prefs.getString(saveCity, null).toString()
//    }
//
//    //City Name Coming Soon
//    fun cityNameCinema(cityCinema: String) {
//        editor = prefs.edit()
//        editor?.putString(saveCity, cityCinema)
//        editor?.apply()
//    }
//
//    fun saveId(id: Int) {
//        editor = prefs.edit()
//        editor?.putString(saveUserid, id.toString())
//        editor?.apply()
//    }
//
//    fun getId(): String {
//        return prefs.getString(saveUserid, "").toString()
//    }
//
//
//    fun isLogin(value: Boolean) {
//        prefs.edit().putBoolean(IS_LOGIN, value).apply()
//
//    }
//
//    fun getIsLogin(): Boolean = prefs.getBoolean(IS_LOGIN, false)
//
//
//    fun saveIsAddressFill(value: Boolean) {
//        prefs.edit().putBoolean(isAddressFill, value).apply()
//
//    }
//
//    fun getIsAddressFill(): Boolean = prefs.getBoolean(isAddressFill, false)
//
//
//    fun getIAddressFill(): Boolean = prefs.getBoolean(IS_LOGIN, false)
//
//
//    fun saveEmail(email: String) {
//        editor = prefs.edit()
//        editor?.putString(saveEmailId, email)
//        editor?.apply()
//    }
//
//    fun getEmail(): String {
//        return prefs.getString(saveEmailId, null).toString()
//    }
//
//    fun saveToken(token: String) {
//        editor = prefs.edit()
//        editor?.putString(saveApiToken, token)
//        editor?.apply()
//    }
//
//    fun getToken(): String {
//        return prefs.getString(saveApiToken, null).toString()
//
//    }
//
//
//    fun saveUserType(userType: String) {
//        editor = prefs.edit()
//        editor?.putString(saveUserTypeP, userType)
//        editor?.apply()
//    }
//
//    fun getUserType(): String {
//        return prefs.getString(saveUserTypeP, null).toString()
//    }
//
//    fun saveUserName(userName: String) {
//        editor = prefs.edit()
//        editor?.putString(saveUserNameP, userName)
//        editor?.apply()
//    }
//
//    fun getUserName(): String {
//        return prefs.getString(saveUserNameP, null).toString()
//    }
//
//    fun saveUserMobile(userMobile: String) {
//        editor = prefs.edit()
//        editor?.putString(saveUserMobileP, userMobile)
//        editor?.apply()
//    }
//
//    fun getUserMobile(): String {
//        return prefs.getString(saveUserMobileP, null).toString()
//    }
//
//    fun getAppLang(): String {
//        return prefs.getString(saveAppLangP, null).toString()
//    }
//
//    //City Name Coming Soon
//    fun saveAppLang(appLang: String) {
//        editor = prefs.edit()
//        editor?.putString(saveAppLangP, appLang)
//        editor?.apply()
//    }
//
//    fun clearData(requireActivity: Activity) {
//        isLogin(false)
//        editor = prefs.edit()
//        editor?.apply()
//        editor?.commit()
//        val intent = Intent(requireActivity, OnBoardingActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//        requireActivity.startActivity(intent)
//        requireActivity.finish()
//    }
}