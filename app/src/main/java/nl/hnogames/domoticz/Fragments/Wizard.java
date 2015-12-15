/*
 * Copyright (C) 2015 Domoticz
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package nl.hnogames.domoticz.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.SettingsActivity;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.app.DomoticzFragment;

public class Wizard extends DomoticzFragment implements DomoticzFragmentListener
{

    private final String WELCOME = "WELCOME_CARD";
    private final String FAVORITE = "FAVORITE_CARD";
    private final String GEOFENCE = "GEOFENCE_CARD";
    private final String WEAR = "WEAR_CARD";
    private final String FILTER = "FILTER_CARD";
    private final String WIDGETS = "WIDGETS_CARD";
    private final String GRAPH = "GRAPH_CARD";
    private final String STARTUP = "STARTUP_CARD";

    private ViewGroup root;
    private SharedPrefUtil mSharedPrefs;

    private final int iSettingsResultCode = 995;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        root = (ViewGroup) inflater.inflate(R.layout.fragment_wizard, null);
        mSharedPrefs = new SharedPrefUtil(getActivity());

        getActionBar().setTitle(R.string.title_wizard);
        createCards();

        return root;
    }

    private void createCards() {
        final Context context = getActivity();
        MaterialListView mListView = (MaterialListView) root.findViewById(R.id.wizard_listview);
        mListView.getItemAnimator().setAddDuration(300);
        mListView.getItemAnimator().setRemoveDuration(300);
        mListView.getAdapter().clearAll();

        mListView.setOnDismissCallback(new OnDismissCallback() {
            @Override
            public void onDismiss(Card card, int position) {
                Log.d("CARD_TYPE", card.getTag().toString());
                mSharedPrefs.completeCard(card.getTag().toString());
                createCards();
            }
        });

        List<Card> cards = new ArrayList<>();
        if (!mSharedPrefs.isCardCompleted(WELCOME)) {
            cards.add((new Card.Builder(context)
                    .setTag(WELCOME)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_welcome_card_layout)
                    .setTitle(context.getString(R.string.wizard_welcome))
                    .setTitleColor(Color.WHITE)
                    .setDescription(context.getString(R.string.wizard_welcome_description))
                    .setDescriptionColor(Color.WHITE)
                    .setSubtitleColor(Color.WHITE)
                    .setBackgroundColor(context.getResources().getColor(R.color.md_material_blue_600))
                    .addAction(R.id.ok_button, new WelcomeButtonAction(context)
                            .setText(context.getString(R.string.wizard_button_nice))
                            .setTextColor(Color.WHITE)
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    card.dismiss();
                                }
                            }))).endConfig().build());
        }

        if (!mSharedPrefs.isCardCompleted(FAVORITE)) {
            cards.add(new Card.Builder(context)
                    .setTag(FAVORITE)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_favorites))
                    .setDescription(context.getString(R.string.wizard_favorites_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                            .setText(context.getString(R.string.wizard_button_switches))
                            .setTextColor(context.getResources().getColor(R.color.md_material_blue_600))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    ((MainActivity) getActivity()).changeFragment("nl.hnogames.domoticz.Fragments.Switches");
                                }
                            }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                            .setText(context.getString(R.string.wizard_button_done))
                            .setTextColor(context.getResources().getColor(R.color.material_orange_600))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    card.dismiss();
                                }
                            }))
                    .endConfig()
                    .build());
        }

        if (!mSharedPrefs.isCardCompleted(STARTUP)) {
            cards.add(new Card.Builder(context)
                    .setTag(STARTUP)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_startup))
                    .setDescription(context.getString(R.string.wizard_startup_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                            .setText(context.getString(R.string.wizard_button_settings))
                            .setTextColor(context.getResources().getColor(R.color.md_material_blue_600))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    startActivityForResult(new Intent(context, SettingsActivity.class), iSettingsResultCode);
                                }
                            }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                            .setText(context.getString(R.string.wizard_button_done))
                            .setTextColor(context.getResources().getColor(R.color.material_orange_600))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    card.dismiss();
                                }
                            }))
                    .endConfig()
                    .build());
        }

        if (!mSharedPrefs.isCardCompleted(GEOFENCE)) {
            cards.add(new Card.Builder(context)
                    .setTag(GEOFENCE)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_geo))
                    .setDescription(context.getString(R.string.wizard_geo_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                            .setText(context.getString(R.string.wizard_button_settings))
                            .setTextColor(context.getResources().getColor(R.color.md_material_blue_600))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    startActivityForResult(new Intent(context, SettingsActivity.class), iSettingsResultCode);
                                }
                            }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                            .setText(context.getString(R.string.wizard_button_done))
                            .setTextColor(context.getResources().getColor(R.color.material_orange_600))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    card.dismiss();
                                }
                            }))
                    .endConfig()
                    .build());
        }

        if (!mSharedPrefs.isCardCompleted(WEAR)) {
            cards.add(new Card.Builder(context)
                    .setTag(WEAR)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_wear))
                    .setDescription(context.getString(R.string.wizard_wear_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                            .setText(context.getString(R.string.wizard_button_settings))
                            .setTextColor(context.getResources().getColor(R.color.md_material_blue_600))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    startActivityForResult(new Intent(context, SettingsActivity.class), iSettingsResultCode);
                                }
                            }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                            .setText(context.getString(R.string.wizard_button_done))
                            .setTextColor(context.getResources().getColor(R.color.material_orange_600))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    card.dismiss();
                                }
                            }))
                    .endConfig()
                    .build());
        }

        if (!mSharedPrefs.isCardCompleted(GRAPH)) {
            cards.add(new Card.Builder(context)
                    .setTag(GRAPH)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_graph))
                    .setDescription(context.getString(R.string.wizard_graph_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                            .setText(context.getString(R.string.wizard_button_utilities))
                            .setTextColor(context.getResources().getColor(R.color.md_material_blue_600))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    ((MainActivity) getActivity()).changeFragment("nl.hnogames.domoticz.Fragments.Utilities");
                                }
                            }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                            .setText(context.getString(R.string.wizard_button_done))
                            .setTextColor(context.getResources().getColor(R.color.material_orange_600))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    card.dismiss();
                                }
                            }))
                    .endConfig()
                    .build());
        }

        if (!mSharedPrefs.isCardCompleted(FILTER)) {
            cards.add(new Card.Builder(context)
                    .setTag(FILTER)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_filter))
                    .setDescription(context.getString(R.string.wizard_filter_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                            .setText(context.getString(R.string.wizard_button_nice))
                            .setTextColor(context.getResources().getColor(R.color.md_material_blue_600))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    ((MainActivity) getActivity()).changeFragment("nl.hnogames.domoticz.Fragments.Switches");
                                }
                            }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                            .setText(context.getString(R.string.wizard_button_done))
                            .setTextColor(context.getResources().getColor(R.color.material_orange_600))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    card.dismiss();
                                }
                            }))
                    .endConfig()
                    .build());
        }

        if (!mSharedPrefs.isCardCompleted(WIDGETS)) {
            cards.add(new Card.Builder(context)
                    .setTag(WIDGETS)
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_widgets))
                    .setDescription(context.getString(R.string.wizard_widgets_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                            .setText("")
                            .setTextColor(context.getResources().getColor(R.color.md_material_blue_600))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    card.dismiss();

                                }
                            }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                            .setText(context.getString(R.string.wizard_button_done))
                            .setTextColor(context.getResources().getColor(R.color.material_orange_600))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    card.dismiss();
                                }
                            }))
                    .endConfig()
                    .build());
        }

        if (cards.size() <= 0) {
            cards.add(new Card.Builder(context)
                    .setTag("FINISH")
                    .setDismissible()
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_basic_buttons_card)
                    .setTitle(context.getString(R.string.wizard_menuitem))
                    .setDescription(context.getString(R.string.wizard_menuitem_description))
                    .addAction(R.id.left_text_button, new TextViewAction(context)
                            .setText(context.getString(R.string.wizard_button_wizard))
                            .setTextColor(context.getResources().getColor(R.color.md_material_blue_600))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    mSharedPrefs.removeWizard();
                                    ((MainActivity) getActivity()).removeFragmentStack("nl.hnogames.domoticz.Fragments.Wizard");
                                    ((MainActivity) getActivity()).buildScreen();
                                    ((MainActivity) getActivity()).changeFragment("nl.hnogames.domoticz.Fragments.Dashboard");
                                }
                            }))
                    .addAction(R.id.right_text_button, new TextViewAction(context)
                            .setText("")
                            .setTextColor(context.getResources().getColor(R.color.material_orange_600))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    mSharedPrefs.removeWizard();
                                    ((MainActivity) getActivity()).removeFragmentStack("nl.hnogames.domoticz.Fragments.Wizard");
                                    ((MainActivity) getActivity()).buildScreen();
                                    ((MainActivity) getActivity()).changeFragment("nl.hnogames.domoticz.Fragments.Dashboard");
                                }
                            }))
                    .endConfig()
                    .build());
        }

        mListView.getAdapter().addAll(cards);
    }

    @Override
    public void onConnectionOk() {}

}

