package com.zen.alchan.ui.profile

import com.zen.alchan.data.entitiy.AppSetting
import com.zen.alchan.data.repository.UserRepository
import com.zen.alchan.data.response.ProfileData
import com.zen.alchan.data.response.anilist.User
import com.zen.alchan.helper.enums.Source
import com.zen.alchan.helper.extensions.applyScheduler
import com.zen.alchan.helper.extensions.sendMessage
import com.zen.alchan.ui.base.BaseViewModel
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import type.MediaListStatus

class SharedProfileViewModel(
    private val userRepository: UserRepository
) : BaseViewModel() {

    private val _userAndAppSetting = BehaviorSubject.createDefault(User.EMPTY_USER to AppSetting.EMPTY_APP_SETTING)
    val userAndAppSetting: Observable<Pair<User, AppSetting>>
        get() = _userAndAppSetting

    private val _profileData = BehaviorSubject.createDefault(ProfileData.EMPTY_PROFILE_DATA)
    val profileData: Observable<ProfileData>
        get() = _profileData

    private val _animeCompletedCount = BehaviorSubject.createDefault(0)
    val animeCompletedCount: Observable<Int>
        get() = _animeCompletedCount

    private val _mangaCompletedCount = BehaviorSubject.createDefault(0)
    val mangaCompletedCount: Observable<Int>
        get() = _mangaCompletedCount

    private val _followingCount = BehaviorSubject.createDefault(0)
    val followingCount: Observable<Int>
        get() = _followingCount

    private val _followersCount = BehaviorSubject.createDefault(0)
    val followersCount: Observable<Int>
        get() = _followersCount

    var userId = 0

    override fun loadData() {
        load {
            loadUserData()
        }
    }

    fun reloadData() {
        loadUserData(true)
    }

    private fun loadUserData(isReloading: Boolean = false) {
        _loading.onNext(true)

        if (userId == 0) {
            disposables.add(
                userRepository.getViewer()
                    .zipWith(userRepository.getAppSetting()) { user, appSetting ->
                        return@zipWith user to appSetting
                    }
                    .applyScheduler()
                    .subscribe(
                        { (user, appSetting) ->
                            loadProfileData(user.id, if (isReloading) Source.NETWORK else null)
                            _userAndAppSetting.onNext(user to appSetting)
                        },
                        {
                            _loading.onNext(false)
                            _error.onNext(it.sendMessage())
                            state = State.ERROR
                        }
                    )
            )
        } else {
            // TODO: update this to be able to get user data of other user as well
        }
    }

    private fun loadProfileData(userId: Int, source: Source?) {
        disposables.add(
            userRepository.getProfileData(userId, source = source)
                .applyScheduler()
                .doFinally {
                    _loading.onNext(false)
                }
                .subscribe(
                    {
                        _profileData.onNext(it)

                        _animeCompletedCount.onNext(
                            it.user.statistics.anime.statuses.find { anime ->
                                anime.status == MediaListStatus.COMPLETED
                            }?.count ?: 0
                        )
                        _mangaCompletedCount.onNext(
                            it.user.statistics.manga.statuses.find { manga ->
                                manga.status == MediaListStatus.COMPLETED
                            }?.count ?: 0
                        )
                        _followingCount.onNext(it.following.pageInfo.total)
                        _followersCount.onNext(it.followers.pageInfo.total)
                        state = State.LOADED
                    },
                    {
                        _error.onNext(it.sendMessage())
                        state = State.ERROR
                    }
                )
        )
    }
}