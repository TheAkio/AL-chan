package com.zen.alchan.data.repository

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import com.apollographql.apollo.api.Response
import com.zen.alchan.data.datasource.AuthDataSource
import com.zen.alchan.data.localstorage.LocalStorage
import com.zen.alchan.data.network.Converter
import com.zen.alchan.data.network.Resource
import com.zen.alchan.helper.AndroidUtility
import com.zen.alchan.helper.Utility
import com.zen.alchan.helper.libs.SingleLiveEvent
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class AuthRepositoryImpl(private val localStorage: LocalStorage, private val authDataSource: AuthDataSource) : AuthRepository {

    override val appColorTheme: Int
        get() = AndroidUtility.getAppColorThemeRes(localStorage.appColorTheme)

    // this single live event can be used by both splash and login activities
    // because there won't be a case where both observers are alive
    private val _viewerDataResponse = SingleLiveEvent<Resource<ViewerQuery.Data>>()
    override val viewerDataResponse: LiveData<Resource<ViewerQuery.Data>>
        get() = _viewerDataResponse

    override val isLoggedIn: Boolean
        get() = localStorage.bearerToken != null

    override val shouldRetrieveViewerData: Boolean
        get() = localStorage.viewerData == null || Utility.timeDiffMoreThanOneDay(localStorage.viewerDataLastRetrieved)

    override fun setBearerToken(accessToken: String) {
        localStorage.bearerToken = accessToken
    }

    @SuppressLint("CheckResult")
    override fun getViewerData() {
        _viewerDataResponse.postValue(Resource.Loading())

        authDataSource.getViewerData().subscribeWith(object : Observer<Response<ViewerQuery.Data>> {
            override fun onSubscribe(d: Disposable) { }

            override fun onNext(t: Response<ViewerQuery.Data>) {
                if (t.hasErrors()) {
                    _viewerDataResponse.postValue(Resource.Error(t.errors()[0].message()!!))
                } else {
                    localStorage.viewerData = Converter.convertUser(t.data()?.Viewer())
                    localStorage.viewerDataLastRetrieved = Utility.getCurrentTimestamp()
                    _viewerDataResponse.postValue(Resource.Success(t.data()!!))
                }
            }

            override fun onError(e: Throwable) {
                _viewerDataResponse.postValue(Resource.Error(e.localizedMessage))
            }

            override fun onComplete() { }
        })
    }
}