package com.fuelware.app.fw_manager.models;

public class ForgotPasswordModel {
    private String token;
    private String password;
    private String mobile;
    private long otp;

    public void setOtp(long otp) {
        this.otp = otp;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
