package com.example.demobaidumap.data;

import com.example.demobaidumap.data.model.LoggedInUser;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 * 需要偏远数据来源的鉴定和用户信息
 *      并且保持登录状态以及用户证书信息的内存缓存的类
 */
public class LoginRepository {

    private static volatile LoginRepository instance;

    private LoginDataSource dataSource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // 如果用户数据缓存在本地，建议加密
    private LoggedInUser user = null;

    // private constructor : singleton access
    // 私有构造器：单例对象访问
    private LoginRepository(LoginDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static LoginRepository getInstance(LoginDataSource dataSource) {
        if (instance == null) {
            instance = new LoginRepository(dataSource);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        user = null;
        dataSource.logout();
    }

    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
        // If user credentials will be cached in local storage, it is recommended it be encrypted
    }

    public Result<LoggedInUser> login(String username, String password) {
        // handle login
        // dataSource用户数据模板
        Result<LoggedInUser> result = dataSource.login(username, password);
        if (result instanceof Result.Success) {
            // 设置成功登录的用户数据
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
        }
        return result;
    }
}