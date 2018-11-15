/*
 * Copyright (C) 2015 Domoticz - Mark Heinis
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package nl.hnogames.domoticz.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.card.OnActionClickListener;
import com.dexafree.materialList.card.action.TextViewAction;
import com.dexafree.materialList.card.action.WelcomeButtonAction;
import com.dexafree.materialList.listeners.OnDismissCallback;
import com.dexafree.materialList.view.MaterialListView;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.SettingsActivity;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class Wizard extends Fragment {

    private final String WELCOME = "WELCOME_CARD";
    private final String FAVORITE = "FAVORITE_CARD";
    private final String GEOFENCE = "GEOFENCE_CARD";
    private final String WEAR = "WEAR_CARD";
    private final String FILTER = "FILTER_CARD";
    private final String WIDGETS = "WIDGETS_CARD";
    private final String GRAPH = "GRAPH_CARD";
    private final String STARTUP = "STARTUP_CARD";
    private final String NOTIFICATIONS = "NOTIFICATIONS_CARD";
    private final String MULTISERVER = "MULTISERVER_CARD";
    private final String NFC = "NFC_CARD";
    private final String QRCODE = "QRCODE_CARD";
    private final String FINISH = "FINISH";
    private final String SPEECH = "SPEECH";
    private final String AUTO = "AUTO";

    private final String TAG = Wizard.class.getSimpleName();
    private final int iSettingsResultCode = 995;
    private ViewGroup root;
    private SharedPrefUtil mSharedPrefs;
    private Context context;

    @Override
    @DebugLog
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        root = (ViewGroup) inflater.inflate(R.layout.fragment_wizard, null);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_wizard);
        mSharedPrefs = new SharedPrefUtil(getActivity());

        if (mSharedPrefs.darkThemeEnabled()) {
            if ((root.findViewById(R.id.wizard_relativeLayout)) != null)
                (root.findViewById(R.id.wizard_relativeLayout)).setBackgroundColor(getResources().getColor(R.color.background_dark));
        }
        createCards();
        return root;
    }

    private void createCards() {
        context = getActivity();
        MaterialListView mListView = (MaterialListView) root.findViewById(R.id.wizard_listview);
        mListView.getItemAnimator().setAddDuration(300);
        mListView.getItemAnimator().setRemoveDuration(300);
        mListView.getAdapter().clearAll();

        mListView.setOnDismissCallback(new OnDismissCallback() {
            @Override
            @DebugLog
            public void onDismiss(@NonNull Card card, int position) {
                String cardTag = "Unknown";
                try {
                    //noinspection ConstantConditions
                    cardTag = card.getTag().toString();
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }

                Log.d(TAG, "CARD_TYPE: " + cardTag);
                mSharedPrefs.completeCard(cardTag);
                createCards();
            }
        });

        List<String> cardsToGenerate = new ArrayList<>();

        if (!mSharedPrefs.isCardCompleted(WELCOME)) cardsToGenerate.add(WELCOME);
        if (!mSharedPrefs.isCardCompleted(FAVORITE)) cardsToGenerate.add(FAVORITE);
        if (!mSharedPrefs.isCardCompleted(STARTUP)) cardsToGenerate.add(STARTUP);
        if (!mSharedPrefs.isCardCompleted(GEOFENCE)) cardsToGenerate.add(GEOFENCE);
        if (!mSharedPrefs.isCardCompleted(WEAR)) cardsToGenerate.add(WEAR);
        if (!mSharedPrefs.isCardCompleted(GRAPH)) cardsToGenerate.add(GRAPH);
        if (!mSharedPrefs.isCardCompleted(FILTER)) cardsToGenerate.add(FILTER);
        if (!mSharedPrefs.isCardCompleted(WIDGETS)) cardsToGenerate.add(WIDGETS);
        if (!mSharedPrefs.isCardCompleted(NOTIFICATIONS)) cardsToGenerate.add(NOTIFICATIONS);
        if (!mSharedPrefs.isCardCompleted(MULTISERVER)) cardsToGenerate.add(MULTISERVER);
        if (!mSharedPrefs.isCardCompleted(QRCODE)) cardsToGenerate.add(QRCODE);
        if (!mSharedPrefs.isCardCompleted(NFC)) cardsToGenerate.add(NFC);
        if (!mSharedPrefs.isCardCompleted(SPEECH)) cardsToGenerate.add(SPEECH);
        if (!mSharedPrefs.isCardCompleted(AUTO)) cardsToGenerate.add(AUTO);

        if (cardsToGenerate.size() <= 0) cardsToGenerate.add(FINISH);
        List<Card> cards = generateCards(cardsToGenerate);
        mListView.getAdapter().addAll(cards);
    }

    public List<Card> generateCards(List<String> cardsToGenerate) {
        int blueColor = ContextCompat.getColor(context, R.color.md_material_blue_600);
        int otherColor = ContextCompat.getColor(context, R.color.md_white_1000);
        int titleColorLight = Color.WHITE;
        int titleColorOther = Color.GRAY;

        if (mSharedPrefs.darkThemeEnabled()) {
            titleColorOther = Color.WHITE;
            otherColor = Color.parseColor("#3F3F3F");
        }

        List<Card> cards = new ArrayList<>();
        for (String card : cardsToGenerate) {
            if (card.equalsIgnoreCase(WELCOME)) {
                cards.add((new Card.Builder(context)
                    .setTag(WELCOME)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_welcome_card_layout)
                    .setTitle(context.getString(R.string.wizard_welcome))
                    .setTitleColor(titleColorLight)
                    .setDescription(context.getString(R.string.wizard_welcome_description))
                    .setDescriptionColor(titleColorLight)
                    .setSubtitleColor(titleColorLight)
                    .setBackgroundColor(blueColor)
                    .addAction(R.id.ok_button, new WelcomeButtonAction(context)
                        .setText(context.getString(R.string.wizard_button_nice))
                        .setTextColor(titleColorLight)
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                card.dismiss();
                            }
                        }))).endConfig().build());
            }

            if (card.equalsIgnoreCase(FAVORITE)) {
                cards.add(new Card.Builder(context)
                    .setTag(FAVORITE)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setBackgroundColor(otherColor)
                    .setTitle(context.getString(R.string.wizard_favorites))
                    .setTitleColor(titleColorOther)
                    .setDescription(context.getString(R.string.wizard_favorites_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_switches))
                        .setTextColor(ContextCompat.getColor(context, R.color.md_material_blue_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                ((MainActivity) getActivity()).changeFragment("nl.hnogames.domoticz.Fragments.Switches");
                            }
                        }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_done))
                        .setTextColor(ContextCompat.getColor(context, R.color.material_orange_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                card.dismiss();
                            }
                        }))
                    .endConfig()
                    .build());
            }
            if (card.equalsIgnoreCase(STARTUP)) {
                cards.add(new Card.Builder(context)
                    .setTag(STARTUP)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_startup))
                    .setTitleColor(titleColorOther)
                    .setBackgroundColor(otherColor)
                    .setDescription(context.getString(R.string.wizard_startup_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_settings))
                        .setTextColor(ContextCompat.getColor(context, R.color.md_material_blue_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                startActivityForResult(new Intent(context, SettingsActivity.class), iSettingsResultCode);
                            }
                        }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_done))
                        .setTextColor(ContextCompat.getColor(context, R.color.material_orange_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                card.dismiss();
                            }
                        }))
                    .endConfig()
                    .build());
            }
            if (card.equalsIgnoreCase(GEOFENCE)) {
                cards.add(new Card.Builder(context)
                    .setTag(GEOFENCE)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_geo))
                    .setTitleColor(titleColorOther)
                    .setBackgroundColor(otherColor)
                    .setDescription(context.getString(R.string.wizard_geo_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_settings))
                        .setTextColor(ContextCompat.getColor(context, R.color.md_material_blue_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                startActivityForResult(new Intent(context, SettingsActivity.class), iSettingsResultCode);
                            }
                        }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_done))
                        .setTextColor(ContextCompat.getColor(context, R.color.material_orange_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                card.dismiss();
                            }
                        }))
                    .endConfig()
                    .build());
            }
            if (card.equalsIgnoreCase(NFC)) {
                cards.add(new Card.Builder(context)
                    .setTag(NFC)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_nfc))
                    .setTitleColor(titleColorOther)
                    .setBackgroundColor(otherColor)
                    .setDescription(context.getString(R.string.wizard_nfc_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_settings))
                        .setTextColor(ContextCompat.getColor(context, R.color.md_material_blue_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                startActivityForResult(new Intent(context, SettingsActivity.class), iSettingsResultCode);
                            }
                        }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_done))
                        .setTextColor(ContextCompat.getColor(context, R.color.material_orange_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                card.dismiss();
                            }
                        }))
                    .endConfig()
                    .build());
            }
            if (card.equalsIgnoreCase(QRCODE)) {
                cards.add(new Card.Builder(context)
                    .setTag(QRCODE)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_qrcode))
                    .setBackgroundColor(otherColor)
                    .setDescription(context.getString(R.string.wizard_qrcode_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_settings))
                        .setTextColor(ContextCompat.getColor(context, R.color.md_material_blue_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                startActivityForResult(new Intent(context, SettingsActivity.class), iSettingsResultCode);
                            }
                        }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_done))
                        .setTextColor(ContextCompat.getColor(context, R.color.material_orange_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                card.dismiss();
                            }
                        }))
                    .endConfig()
                    .build());
            }
            if (card.equalsIgnoreCase(SPEECH)) {
                cards.add(new Card.Builder(context)
                    .setTag(SPEECH)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_speech))
                    .setBackgroundColor(otherColor)
                    .setDescription(context.getString(R.string.wizard_speech_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_settings))
                        .setTextColor(ContextCompat.getColor(context, R.color.md_material_blue_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                startActivityForResult(new Intent(context, SettingsActivity.class), iSettingsResultCode);
                            }
                        }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_done))
                        .setTextColor(ContextCompat.getColor(context, R.color.material_orange_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                card.dismiss();
                            }
                        }))
                    .endConfig()
                    .build());
            }
            if (card.equalsIgnoreCase(WEAR)) {
                cards.add(new Card.Builder(context)
                    .setTag(WEAR)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setTitleColor(titleColorOther)
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_wear))
                    .setTitleColor(titleColorOther)
                    .setBackgroundColor(otherColor)
                    .setDescription(context.getString(R.string.wizard_wear_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_settings))
                        .setTextColor(ContextCompat.getColor(context, R.color.md_material_blue_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                startActivityForResult(new Intent(context, SettingsActivity.class), iSettingsResultCode);
                            }
                        }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_done))
                        .setTextColor(ContextCompat.getColor(context, R.color.material_orange_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                card.dismiss();
                            }
                        }))
                    .endConfig()
                    .build());
            }
            if (card.equalsIgnoreCase(AUTO)) {
                cards.add(new Card.Builder(context)
                    .setTag(AUTO)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setBackgroundColor(otherColor)
                    .setTitle(context.getString(R.string.wizard_auto))
                    .setTitleColor(titleColorOther)
                    .setDescription(context.getString(R.string.wizard_auto_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_settings))
                        .setTextColor(ContextCompat.getColor(context, R.color.md_material_blue_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                startActivityForResult(new Intent(context, SettingsActivity.class), iSettingsResultCode);
                            }
                        }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_done))
                        .setTextColor(ContextCompat.getColor(context, R.color.material_orange_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                card.dismiss();
                            }
                        }))
                    .endConfig()
                    .build());
            }
            if (card.equalsIgnoreCase(NOTIFICATIONS)) {
                cards.add(new Card.Builder(context)
                    .setTag(NOTIFICATIONS)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_notifications))
                    .setTitleColor(titleColorOther)
                    .setBackgroundColor(otherColor)
                    .setDescription(context.getString(R.string.wizard_notifications_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_settings))
                        .setTextColor(ContextCompat.getColor(context, R.color.md_material_blue_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                startActivityForResult(new Intent(context, SettingsActivity.class), iSettingsResultCode);
                            }
                        }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_done))
                        .setTextColor(ContextCompat.getColor(context, R.color.material_orange_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                card.dismiss();
                            }
                        }))
                    .endConfig()
                    .build());
            }
            if (card.equalsIgnoreCase(MULTISERVER)) {
                cards.add(new Card.Builder(context)
                    .setTag(MULTISERVER)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_multiserver))
                    .setTitleColor(titleColorOther)
                    .setBackgroundColor(otherColor)
                    .setDescription(context.getString(R.string.wizard_multiserver_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_settings))
                        .setTextColor(ContextCompat.getColor(context, R.color.md_material_blue_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                startActivityForResult(new Intent(context, SettingsActivity.class), iSettingsResultCode);
                            }
                        }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_done))
                        .setTextColor(ContextCompat.getColor(context, R.color.material_orange_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                card.dismiss();
                            }
                        }))
                    .endConfig()
                    .build());
            }
            if (card.equalsIgnoreCase(GRAPH)) {
                cards.add(new Card.Builder(context)
                    .setTag(GRAPH)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_graph))
                    .setTitleColor(titleColorOther)
                    .setBackgroundColor(otherColor)
                    .setDescription(context.getString(R.string.wizard_graph_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_utilities))
                        .setTextColor(ContextCompat.getColor(context, R.color.md_material_blue_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                ((MainActivity) getActivity()).changeFragment("nl.hnogames.domoticz.Fragments.Utilities");
                            }
                        }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_done))
                        .setTextColor(ContextCompat.getColor(context, R.color.material_orange_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                card.dismiss();
                            }
                        }))
                    .endConfig()
                    .build());
            }
            if (card.equalsIgnoreCase(FILTER)) {
                cards.add(new Card.Builder(context)
                    .setTag(FILTER)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_filter))
                    .setTitleColor(titleColorOther)
                    .setBackgroundColor(otherColor)
                    .setDescription(context.getString(R.string.wizard_filter_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_nice))
                        .setTextColor(ContextCompat.getColor(context, R.color.md_material_blue_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                ((MainActivity) getActivity()).changeFragment("nl.hnogames.domoticz.Fragments.Switches");
                            }
                        }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_done))
                        .setTextColor(ContextCompat.getColor(context, R.color.material_orange_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                card.dismiss();
                            }
                        }))
                    .endConfig()
                    .build());
            }
            if (card.equalsIgnoreCase(WIDGETS)) {
                cards.add(new Card.Builder(context)
                    .setTag(WIDGETS)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_widgets))
                    .setTitleColor(titleColorOther)
                    .setBackgroundColor(otherColor)
                    .setDescription(context.getString(R.string.wizard_widgets_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                        .setText("")
                        .setTextColor(ContextCompat.getColor(context, R.color.md_material_blue_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                card.dismiss();

                            }
                        }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_done))
                        .setTextColor(ContextCompat.getColor(context, R.color.material_orange_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                card.dismiss();
                            }
                        }))
                    .endConfig()
                    .build());
            }
            if (card.equalsIgnoreCase(FINISH)) {
                cards.add(new Card.Builder(context)
                    .setTag(FINISH)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_menuitem))
                    .setTitleColor(titleColorOther)
                    .setBackgroundColor(otherColor)
                    .setDescription(context.getString(R.string.wizard_menuitem_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                        .setText(context.getString(R.string.wizard_button_wizard))
                        .setTextColor(ContextCompat.getColor(context, R.color.md_material_blue_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                mSharedPrefs.removeWizard();
                                ((MainActivity) getActivity()).drawNavigationMenu(null);
                                ((MainActivity) getActivity()).removeFragmentStack("nl.hnogames.domoticz.Fragments.Wizard");
                                ((MainActivity) getActivity()).changeFragment("nl.hnogames.domoticz.Fragments.Dashboard");
                            }
                        }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                        .setText("")
                        .setTextColor(ContextCompat.getColor(context, R.color.material_orange_600))
                        .setListener(new OnActionClickListener() {
                            @Override
                            @DebugLog
                            public void onActionClicked(View view, Card card) {
                                mSharedPrefs.removeWizard();
                                ((MainActivity) getActivity()).drawNavigationMenu(null);
                                ((MainActivity) getActivity()).removeFragmentStack("nl.hnogames.domoticz.Fragments.Wizard");
                                ((MainActivity) getActivity()).changeFragment("nl.hnogames.domoticz.Fragments.Dashboard");
                            }
                        }))
                    .endConfig()
                    .build());
            }

        }
        return cards;
    }
}