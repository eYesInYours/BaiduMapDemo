package com.example.demobaidumap.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Patterns;
import android.widget.Toast;

import com.example.demobaidumap.data.LoginRepository;
import com.example.demobaidumap.data.Result;
import com.example.demobaidumap.data.model.LoggedInUser;
import com.example.demobaidumap.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }


    public void login(String username, String password) {
        // can be launched in a separate asynchronous job
        Result<LoggedInUser> result = loginRepository.login(username, password);

        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
        } else {
            loginResult.setValue(new LoginResult(R.string.login_failed));
        }
    }

    // 数据校验的显示
    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    // 手机号校验，这里的username表示手机号
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }

        if(username.length() != 11){
            return false;
        }

        String regx = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";
        Pattern pattern = Pattern.compile(regx);
        Matcher matcher = pattern.matcher(username);
        boolean bool = matcher.matches();

        if(!bool) return false;

//        if (username.contains("@")) {
//            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
//        } else {
//            return !username.trim().isEmpty();
//        }
        return true;
    }

    // A placeholder password validation check
    // 密码校验
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}