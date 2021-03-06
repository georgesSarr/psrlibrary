package com.paydunya.paydunyapsrlibrary.Fragments;

import android.os.Build;
import android.os.Bundle;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.paydunya.paydunyapsrlibrary.Common.BaseActivity;
import com.paydunya.paydunyapsrlibrary.Common.BaseFragment;
import com.paydunya.paydunyapsrlibrary.Constants.EndPointsConstants;
import com.paydunya.paydunyapsrlibrary.Managers.PaymentManager;
import com.paydunya.paydunyapsrlibrary.Network.ApiServices;
import com.paydunya.paydunyapsrlibrary.Network.Entities.ResponseModel;
import com.paydunya.paydunyapsrlibrary.Network.RetroFitBuilder;
import com.paydunya.paydunyapsrlibrary.R;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OmFragment extends BaseFragment {
    public OmFragment() {
        // Required empty public constructor
    }

    private String phoneNumber;
    private String authorisationCode;

    private TextInputLayout inputPhone;
    private TextInputLayout inputCode;
    private Button btnPayIn;
    private PaymentManager paymentManager;


    private ApiServices apiServices;
    private Call<ResponseModel<Object>> callTransferOm;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_om, container, false);

        inputPhone = view.findViewById(R.id.input_phone);
        inputCode = view.findViewById(R.id.input_code);
        btnPayIn = view.findViewById(R.id.btn_pay_in);
        paymentManager = PaymentManager.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            inputPhone.getEditText().addTextChangedListener(new PhoneNumberFormattingTextWatcher("SN"));
        }

        if(paymentManager.getCheckoutInvoiceModel().getToken() != null){
            Toast.makeText(getActivity(), paymentManager.getCheckoutInvoiceModel().getToken(), Toast.LENGTH_LONG).show();
        }

        btnPayIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = inputPhone.getEditText().getText().toString().trim();
                authorisationCode = inputCode.getEditText().getText().toString().trim();
                if(checkForm()){
                    sendPayment();
                }

            }
        });

        return view;
    }

    private void sendPayment(){
        String phoneReformat = phoneNumber.replaceAll("\\s", "");
        apiServices = RetroFitBuilder.createService(ApiServices.class, EndPointsConstants.PAYDUNYA_SOFTPAY_BASE_URL, getContext());
        callTransferOm = apiServices.OMSPayment(phoneReformat, authorisationCode, paymentManager.getCheckoutInvoiceModel().getToken());
        showCustomProgressAlertDialog("Transfert en cours");
        callTransferOm.enqueue(new Callback<ResponseModel<Object>>() {
            @Override
            public void onResponse(Call<ResponseModel<Object>> call, Response<ResponseModel<Object>> response) {
                hideCustomProgressAlertDialog();
                if(response.isSuccessful()){
                    if(response.body().isSuccess()){
                        showAlertDialog(response.body().getMessage(), "OK", false, "", "success", new BaseActivity.AlertDialogListener() {
                            @Override
                            public void onConfirm() {
                                getActivity().finish();
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
                    } else {
                        showAlertDialog(response.body().getMessage(), "REESSAYER", true, "QUITTER", "warning", new BaseActivity.AlertDialogListener() {
                            @Override
                            public void onConfirm() {
                            }

                            @Override
                            public void onCancel() {
                                getActivity().finish();
                            }
                        });
                    }
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        showAlertDialog(jObjError.getString("message"), "REESSAYER", true, "QUITTER", "warning", new BaseActivity.AlertDialogListener() {
                            @Override
                            public void onConfirm() {

                            }

                            @Override
                            public void onCancel() {
                                getActivity().finish();
                            }
                        });
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseModel<Object>> call, Throwable t) {
                hideCustomProgressAlertDialog();
                showAlertDialog("Une erreur s'est produite, veuillez réessayer plus tard", "OK", false, "", "warning", new BaseActivity.AlertDialogListener() {
                    @Override
                    public void onConfirm() {
                        getActivity().finish();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        });


    }

    private boolean checkForm(){
        if (phoneNumber.equals("")) {
            Toast.makeText(getActivity(), "Veuillez saisir le numéro de téléphone", Toast.LENGTH_SHORT).show();
            return false;
        } else if (authorisationCode.equals("")) {
            Toast.makeText(getActivity(), "Veuillez saisir le code reçu", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


}
