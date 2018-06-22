package org.fossasia.openevent.app.core.auth.login;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.fossasia.openevent.app.R;
import org.fossasia.openevent.app.common.Constants;
import org.fossasia.openevent.app.common.mvp.view.BaseFragment;
import org.fossasia.openevent.app.core.auth.SharedViewModel;
import org.fossasia.openevent.app.core.auth.forgot.request.ForgotPasswordFragment;
import org.fossasia.openevent.app.core.auth.signup.SignUpFragment;
import org.fossasia.openevent.app.core.main.MainActivity;
import org.fossasia.openevent.app.databinding.LoginFragmentBinding;
import org.fossasia.openevent.app.ui.ViewUtils;
import org.fossasia.openevent.app.utils.EncryptionUtils;

import java.util.ArrayList;
import java.util.Set;

import javax.inject.Inject;

import br.com.ilhasoft.support.validation.Validator;

import static org.fossasia.openevent.app.ui.ViewUtils.showView;

public class LoginFragment extends BaseFragment implements LoginView {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private LoginViewModel loginFragmentViewModel;
    private LoginFragmentBinding binding;
    private Validator validator;
    private SharedViewModel sharedViewModel;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.login_fragment, container, false);
        loginFragmentViewModel = ViewModelProviders.of(this, viewModelFactory).get(LoginViewModel.class);
        sharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        sharedViewModel.getEmail().observe(this, email -> binding.getLogin().setEmail(email));
        validator = new Validator(binding);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        loginFragmentViewModel.getLoginStatus().observe(this, (loginStatus) -> handleIntent());

        String url = binding.url.baseUrl.getText().toString().trim();
        loginFragmentViewModel.setBaseUrl(url, binding.url.overrideUrl.isChecked());
        loginFragmentViewModel.getProgress().observe(this, this::showProgress);
        loginFragmentViewModel.getError().observe(this, this::showError);
        loginFragmentViewModel.getEmailList().observe(this, this::attachEmails);

        binding.setLogin(loginFragmentViewModel.getLogin());
        binding.btnLogin.setOnClickListener(view -> {
            if (!validator.validate())
                return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                handleEncryption();
            }

            loginFragmentViewModel.getLogin().setEmail(binding.emailDropdown.getText().toString());
            loginFragmentViewModel.getLogin().setPassword(binding.userPassword.getText().toString());

            ViewUtils.hideKeyboard(view);
            loginFragmentViewModel.login();

        });
        binding.signUpLink.setOnClickListener(view -> openSignUpPage());
        binding.forgotPasswordLink.setOnClickListener(view -> openForgotPasswordPage());
    }

    @Override
    public void onResume() {
        super.onResume();
        String decryptedEmail = null, decryptedPassword = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            decryptedEmail = EncryptionUtils.decryptString(getActivity(), loginFragmentViewModel.getUserEmail());
            decryptedPassword = EncryptionUtils.decryptString(getActivity(), loginFragmentViewModel.getUserPassword());
        }
        binding.userPassword.setText(decryptedPassword);
        binding.emailDropdown.setText(decryptedEmail);
    }

    public void handleIntent() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handleEncryption() {
        String encryptedUserName = EncryptionUtils.encryptString(getActivity(), binding.emailDropdown.getText().toString());
        String encryptedPassword = EncryptionUtils.encryptString(getActivity(), binding.userPassword.getText().toString());

        loginFragmentViewModel.setUserEmail(Constants.PREF_USER_EMAIL, encryptedUserName);
        loginFragmentViewModel.setUserPassword(Constants.PREF_USER_PASSWORD, encryptedPassword);
    }

    @Override
    protected int getTitle() {
        return R.string.login;
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.emailDropdown.setAdapter(null);
    }

    private void openSignUpPage() {
        sharedViewModel.setEmail(binding.getLogin().getEmail());
        getFragmentManager().beginTransaction()
            .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_from_right)
            .replace(R.id.fragment_container, new SignUpFragment())
            .commit();
    }

    private void openForgotPasswordPage() {
        sharedViewModel.setEmail(binding.getLogin().getEmail());
        getFragmentManager().beginTransaction()
            .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_from_right)
            .replace(R.id.fragment_container, new ForgotPasswordFragment())
            .commit();
    }

    @Override
    public void showError(String error) {
        ViewUtils.hideKeyboard(binding.getRoot());
        ViewUtils.showSnackbar(binding.getRoot(), error);
    }

    @Override
    public void onSuccess(String message) {
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }

    @Override
    public void showProgress(boolean show) {
        showView(binding.progressBar, show);
    }

    @Override
    public void attachEmails(Set<String> emails) {
        binding.emailDropdown.setAdapter(
            new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<>(emails))
        );
    }
}
