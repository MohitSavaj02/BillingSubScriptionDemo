package com.techvelen.calldetail.anycall.numbers.allhistory;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
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
    TextView txtActive;
    ArrayList<SkuDetails> skuDataList = new ArrayList<>();

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
        txtActive = findViewById(R.id.txtActive);


        btn_1.setOnClickListener(v -> {
            SkuDetails sku = null;
            for (SkuDetails skuDetails : skuDataList) {
                if (skuDetails.getSku().equals("subbronze_1")) {
                    sku = skuDetails;
                }
            }
            launchBilling(sku);

        });

        btn_6.setOnClickListener(v -> {
            SkuDetails sku = null;
            for (SkuDetails skuDetails : skuDataList) {
                if (skuDetails.getSku().equals("subsilver_6")) {
                    sku = skuDetails;
                }
            }
            launchBilling(sku);

        });

        btn_12.setOnClickListener(v -> {
            SkuDetails sku = null;
            for (SkuDetails skuDetails : skuDataList) {
                if (skuDetails.getSku().equals("subgold_12")) {
                    sku = skuDetails;
                }
            }
            launchBilling(sku);

        });
    }

    private void initInAppBilling() {
        billingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases()
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    setBillingData();
                    getActiveSubscription();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                billingClient.startConnection(this);
            }
        });
    }

    private void getActiveSubscription() {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS, (billingResult, list) -> {
            runOnUiThread(() -> setData(list));
        });
    }

    public void setData(List<Purchase> list) {
        List<String> skuList = new ArrayList<>();
        skuList.add("subbronze_1");
        skuList.add("subsilver_6");
        skuList.add("subgold_12");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Active Plans");
        stringBuilder.append("\n\n");
        for (Purchase purchase : list) {
            if (purchase.isAutoRenewing() && skuList.contains(purchase.getSkus().get(0))) {
                stringBuilder.append(purchase.getSkus().get(0));
                stringBuilder.append("\n");
            }
            txtActive.setText(stringBuilder.toString());
        }
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
            skuDataList.clear();
            if (skuDetailsList != null && skuDetailsList.size() > 0) {
                runOnUiThread(() -> {
                    for (SkuDetails skuDetails : skuDetailsList) {
                        skuDataList.add(skuDetails);
                    }
                    setButtonData(skuDataList);
                });
            }
        });
    }

    private void setButtonData(ArrayList<SkuDetails> skuDataList) {
        btn_1.setText(skuDataList.get(0).getPrice());
        btn_6.setText(skuDataList.get(1).getPrice());
        btn_12.setText(skuDataList.get(2).getPrice());
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