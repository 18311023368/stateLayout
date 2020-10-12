package com.yuetao.statelayout.statemanager.manager;


import com.yuetao.statelayout.statemanager.state.StateProperty;

/**
 * 状态观察者
 */
public interface StateChanger {

    /**
     * 当前需要显示的StateView
     * 非线程安全
     *
     * @param state 当前需要显示的view对应的状态
     * @return
     */
    boolean showState(String state);

    /**
     * 当前需要显示的StateView
     * 非线程安全
     *
     * @param state 当前需要显示的view对应的状态
     *         isShowCore 是否显示背景
     * @return
     */
    boolean showState(String state,boolean isShowCore);

    /**
     * 当前需要显示的StateView
     * 非线程安全
     *
     * @param state 当前需要显示的view对应的状态
     * @return
     */
    boolean showState(StateProperty state);

    /**
     * 获取当前状态
     *
     * @return
     */
    String getState();


    /**
     * 设置当前状态下的一些按钮操作回调
     *
     * @param listener
     */
    void setStateEventListener(StateEventListener listener);

}
