package com.anvipus.explore.ui.xml

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.anvipus.explore.databinding.MainActivityBinding
import com.anvipus.explore.ui.xml.MainFragment
import com.anvipus.explore.ui.xml.MainViewModel
import com.anvipus.core.utils.closeKeyboard
import com.anvipus.core.utils.hide
import com.anvipus.core.utils.show
import com.anvipus.core.utils.resDrawable
import com.anvipus.explore.R
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasAndroidInjector {

    private lateinit var mDataBinding: MainActivityBinding
    // Required for Dagger AndroidInject
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mainViewModel: MainViewModel by viewModels { viewModelFactory }

    lateinit var layoutToolbar: ConstraintLayout
    lateinit var toolbar: Toolbar
    lateinit var toolbarTitle: TextView
    lateinit var toolbarLogo: ImageView
    lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        layoutToolbar = mDataBinding.layoutToolbar.toolbarParent
        toolbar = mDataBinding.layoutToolbar.toolbar
        toolbarTitle = mDataBinding.layoutToolbar.tvTitle
        toolbarLogo = mDataBinding.layoutToolbar.ivLogoSpin

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)
        toolbar.setNavigationOnClickListener {
            closeKeyboard()
            navController.navigateUp() || super.onSupportNavigateUp()
        }
        navController.addOnDestinationChangedListener { _, _, _ ->
            toolbar.navigationIcon = resDrawable(com.anvipus.core.R.drawable.ic_back_arrow_dark)
        }
    }

    fun setTitleToolbar(title: String) {
        with(mDataBinding){
            layoutToolbar.tvTitle.show()
            layoutToolbar.ivLogoSpin.hide()
            layoutToolbar.lineToolbar.show()
            layoutToolbar.tvTitle.text = title
        }
    }

    fun hideTitleToolbar() {
        with(mDataBinding){
            layoutToolbar.tvTitle.hide()
            layoutToolbar.ivLogoSpin.show()
            layoutToolbar.lineToolbar.hide()

        }

    }


}