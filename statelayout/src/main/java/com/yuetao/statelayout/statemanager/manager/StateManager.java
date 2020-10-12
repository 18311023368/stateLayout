package com.yuetao.statelayout.statemanager.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


import com.yuetao.statelayout.statemanager.loader.StateLoader;
import com.yuetao.statelayout.statemanager.loader.StateRepository;
import com.yuetao.statelayout.statemanager.state.BaseState;
import com.yuetao.statelayout.statemanager.state.CoreState;
import com.yuetao.statelayout.statemanager.state.IState;
import com.yuetao.statelayout.statemanager.state.StateProperty;

import java.util.Iterator;

/**
 * StateManager  管理各个状态的切换
 */
public class StateManager implements StateViewManager, StateLoader, StateChanger {
    protected Context context;

    /**
     * 整体View模板
     */
    private ViewGroup overallView;

    private StateRepository stateRepository;

    private IState currentState;

    private StateEventListener listener;

    protected StateManager(Context context) {
        this.context = context;

    }

    public static StateManager newInstance(Context context, StateRepository repository) {
        StateManager stateManager = new StateManager(context);
        stateManager.stateRepository = repository;
        return stateManager;
    }

    public static StateManager newInstance(Context context, StateRepository repository, ViewGroup overallView) {
        StateManager stateManager = new StateManager(context);
        stateManager.stateRepository = repository;
        stateManager.overallView = overallView;
        return stateManager;
    }

    @Override
    public void setStateEventListener(StateEventListener listener) {
        this.listener = listener;

        Iterator<IState> iterator = stateRepository.getStateMap().values().iterator();
        IState stateChanger = null;
        while (iterator.hasNext()) {
            stateChanger = iterator.next();
            //将按钮事件添加到监听中
            stateChanger.setStateEventListener(listener);
        }
    }


    /**
     * 根据viewState，Item自己去查找
     *
     * @param state
     */
    @Override
    public boolean showState(String state) {
        CommonShowState commonShowState = new CommonShowState(state).invoke();
        if (commonShowState.is()) return false;
        IState iState = commonShowState.getIState();
        if (currentState != null) {
            if (currentState.getState().equals(state)) {
                return true;
            }
//            StateViewHelper.hideStater(stateRepository.get(CoreState.STATE));

            StateViewHelper.hideStater(currentState);
        }
        currentState = iState;
        return true;
    }

    @Override
    public boolean showState(String state, boolean isShowCore) {
        CommonShowState commonShowState = new CommonShowState(state).invoke();
        if (commonShowState.is()) return false;
        IState iState = commonShowState.getIState();
        if (currentState != null) {
            if (currentState.getState().equals(state)) {
                return true;
            }
            //有些情况需要显示 带背景的Loading例如登录注册
            if (isShowCore) {
                if (!state.equals(IState.LAODING) ) {
                    StateViewHelper.hideStater(stateRepository.get(CoreState.STATE));
                }
                if (!currentState.getState().equals(CoreState.STATE)) {
                    StateViewHelper.hideStater(currentState);
                }
            } else {
                StateViewHelper.hideStater(currentState);

            }
        }
        currentState = iState;
        return true;
    }


    /**
     * 根据viewState，Item自己去查找
     *
     * @param state
     */
    @Override
    public boolean showState(StateProperty state) {
        boolean result = showState(state.getState());
        if (result) {
            IState baseStater = stateRepository.get(state.getState());
            baseStater.setViewProperty(state);
        }
        return result;
    }

    @Override
    public ViewGroup getOverallView() {
        return overallView;
    }

    public void setOverallView(ViewGroup parent) {
        overallView = parent;
    }


    @Override
    public View setContentView(int layoutId) {

        if (overallView == null) {
            overallView = new FrameLayout(context);
            overallView.setLayoutParams(
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        View view = LayoutInflater.from(context).inflate(layoutId, overallView, false);
        //注册核心view的State
        addState(new CoreState(view));
        showState(CoreState.STATE);

        return overallView;
    }

    @Override
    public View setContentView(View view) {
        if (overallView == null) {

            overallView = new FrameLayout(context);
            overallView.setLayoutParams(
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        //注册核心view的State
        addState(new CoreState(view));
        showState(CoreState.STATE);
        return overallView;
    }

    @Override
    public View getContentView() {
        return getStateView(CoreState.STATE);
    }


    public String getState() {
        return currentState == null ? BaseState.STATE : currentState.getState();
    }


    /**
     * 注册一个状态改变器，如果有重复的状态改变器，则不添加
     *
     * @param stater
     */
    @Override
    public boolean addState(IState stater) {
        if (stater != null) {
            stater.setStateEventListener(listener);
            //如果存在替换流程，需要将之前的StateView移除
            if (!TextUtils.isEmpty(stater.getState())) {
                removeState(stater.getState());
            }
            return stateRepository.addState(stater);
        }

        return false;
    }


    /**
     * 移除对应的状态加载器
     */
    @Override
    public boolean removeState(String state) {
        if (stateRepository == null) {
            return false;
        }
        View stateView = getStateView(state);
        //移除对应状态的同时，也需要移除对应的View
        if (stateView != null) {
            overallView.removeView(stateView);
        }
        return stateRepository.removeState(state);
    }


    public void removeAllState() {
        if (stateRepository != null) {
            stateRepository.clear();
        }
        overallView.removeAllViews();
    }

    public View getStateView(String state) {

        IState stateChanger = stateRepository.get(state);
        if (null != stateChanger) {
            return stateChanger.getView();
        }

        return null;
    }

    public IState getState(String state) {
        return stateRepository.get(state);
    }


    public IState getCurrentState() {
        return currentState;
    }

    public StateRepository getStateRepository() {

        return stateRepository;
    }

    public void setStateRepository(StateRepository stateRepository) {

        if (stateRepository == null) {
            return;
        }

        stateRepository.addState(this.stateRepository.get(CoreState.STATE));

        this.stateRepository = stateRepository;
    }

    /**
     * destory或者destoryView的时候，调用
     */
    public void onDestoryView() {
        overallView = null;
        currentState = null;
        if (stateRepository != null) {
            stateRepository.clear();
        }
    }

    private class CommonShowState {
        private boolean myResult;
        private String mState;
        private IState mIState;

        public CommonShowState(String state) {
            mState = state;
        }

        boolean is() {
            return myResult;
        }

        public IState getIState() {
            return mIState;
        }

        public CommonShowState invoke() {
            if (overallView == null || TextUtils.isEmpty(mState)) {
                myResult = true;
                return this;
            }

            mIState = stateRepository.get(mState);
            if (mIState == null) {
                Log.w("StateManager", "没有注册对应的" + mState + "State，需要调用addStater()进行注册");
                myResult = true;
                return this;
            }
            mIState.setStateEventListener(listener);
            boolean isSuccess = StateViewHelper.showStater(context, overallView, mIState);
            if (!isSuccess) {
                //如果state的View为null等则切换状态不成功
                myResult = true;
                return this;
            }
            myResult = false;
            return this;
        }
    }

}
