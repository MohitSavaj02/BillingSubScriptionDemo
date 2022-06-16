package com.techvelen.calldetail.anycall.numbers.allhistory;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {
    private BillingClient billingClient;
    Button btn_1;
    Button btn_6;
    Button btn_12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initInAppBilling();
        initView();
    }

    private void initView() {
        btn_1 = findViewById(R.id.btn_1);
        btn_6 = findViewById(R.id.btn_6);
        btn_12 = findViewById(R.id.btn_12);
    }

    private void initInAppBilling() {
        billingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases()
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                Log.e("The_Wolf", "onBillingSetupFinished: " + billingResult.getResponseCode());
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    setBillingData();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.e("The_Wolf", "onBillingServiceDisconnected: ");
            }
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Toast.makeText(this, "Already Active", Toast.LENGTH_SHORT).show();
        }
    }

    void handlePurchase(Purchase purchase) {
        purchase.getAccountIdentifiers().getObfuscatedProfileId();
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            updateToken(purchase);
        }
    }

    private void updateToken(Purchase purchase) {
        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
            billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                @Override
                public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                }
            });
        }
    }

    private void setBillingData() {
        List<String> skuList = new ArrayList<>();
        skuList.add("subbronze_1");
        skuList.add("subsilver_6");
        skuList.add("subgold_12");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        billingClient.querySkuDetailsAsync(params.build(), (billingResult, skuDetailsList) -> {
            if (skuDetailsList != null && skuDetailsList.size() > 0) {
                for (SkuDetails skuDetails : skuDetailsList) {
                    if (skuDetails.getSku().equals("subbronze_1")) {
                        btn_1.setOnClickListener(v -> {
                            launchBilling(skuDetails);
                        });
                    } else if (skuDetails.getSku().equals("subsilver_6")) {
                        btn_6.setOnClickListener(v -> {
                            launchBilling(skuDetails);
                        });

                    } else if (skuDetails.getSku().equals("subgold_12")) {
                        btn_12.setOnClickListener(v -> {
                            launchBilling(skuDetails);
                        });
                    }
                }
            }
        });
    }

    public void launchBilling(SkuDetails skuDetails) {
        if (skuDetails == null) {
            Toast.makeText(this, "Service Not Available", Toast.LENGTH_SHORT).show();
            return;
        }
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        billingClient.launchBillingFlow(MainActivity.this, billingFlowParams).getResponseCode();
    }
}