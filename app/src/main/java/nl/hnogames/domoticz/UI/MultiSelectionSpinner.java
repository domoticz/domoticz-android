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

package nl.hnogames.domoticz.UI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class MultiSelectionSpinner extends Spinner implements OnMultiChoiceClickListener {
    String[] _items = null;
    boolean[] mSelection = null;
    String title;
    ArrayAdapter<String> simple_adapter;
    private Context mContext;

    /**
     * A spinner in which multiple items can be selected.
     * The last item in the list is used as a hint!
     *
     * @param context Context
     */
    public MultiSelectionSpinner(Context context) {
        super(context);

        simple_adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item);
        super.setAdapter(simple_adapter);
    }

    /**
     * A spinner in which multiple items can be selected.
     * The last item in the list is used as a hint!
     *
     * @param context Context
     * @param attrs   AttributeSet of
     */
    public MultiSelectionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.mContext = context;

        simple_adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item);
        super.setAdapter(simple_adapter);
    }

    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (mSelection != null && which < mSelection.length) {
            mSelection[which] = isChecked;

            simple_adapter.clear();
            simple_adapter.add(buildSelectedItemString());
        } else {
            throw new IllegalArgumentException(
                    "Argument 'which' is out of bounds.");
        }
    }

    @Override
    public boolean performClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(_items, mSelection, this);
        builder.setTitle(title);
        builder.show();
        return true;
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        throw new RuntimeException(
                "setAdapter is not supported by MultiSelectSpinner.");
    }

    @SuppressWarnings("unused")
    public void setItems(String[] items) {
        if (items != null && items.length > 0) {
            _items = items;
            mSelection = new boolean[_items.length];
            simple_adapter.clear();
            simple_adapter.add(_items[0]);
            Arrays.fill(mSelection, false);
        }
    }

    public void setItems(List<String> items) {
        if (items != null && items.size() > 0) {
            _items = items.toArray(new String[items.size()]);
            mSelection = new boolean[_items.length];
            simple_adapter.clear();
            simple_adapter.add(_items[0]);
            Arrays.fill(mSelection, false);
        }
    }

    @SuppressWarnings("unused")
    public void setSelection(String[] selection) {
        if (mSelection == null || selection.length <= 0)
            throw new IllegalArgumentException("No items in adapter");

        for (String cell : selection) {
            for (int j = 0; j < _items.length; ++j) {
                if (_items[j].equals(cell)) {
                    mSelection[j] = true;
                }
            }
        }
    }

    public void setSelection(List<String> selection) {
        if (mSelection == null || mSelection.length <= 0)
            throw new IllegalArgumentException("No items in adapter");

        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
        }
        for (String sel : selection) {
            for (int j = 0; j < _items.length; ++j) {
                if (_items[j].equals(sel)) {
                    mSelection[j] = true;
                }
            }
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    public void setSelection(int index) {
        if (mSelection == null) throw new IllegalArgumentException("No items in adapter");
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
        }
        if (index >= 0 && index < mSelection.length) {
            mSelection[index] = true;
        } else {
            throw new IllegalArgumentException("Index " + index
                    + " is out of bounds.");
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    @SuppressWarnings("unused")
    public void setSelection(int[] selectedIndices) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
        }
        for (int index : selectedIndices) {
            if (index >= 0 && index < mSelection.length) {
                mSelection[index] = true;
            } else {
                throw new IllegalArgumentException("Index " + index
                        + " is out of bounds.");
            }
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitle(int title) {
        this.title = mContext.getString(title);
    }

    public List<String> getSelectedStrings() {
        if (_items == null)
            return null;

        List<String> selection = new LinkedList<>();
        for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                selection.add(_items[i]);
            }
        }
        return selection;
    }

    @SuppressWarnings("unused")
    public List<Integer> getSelectedIndices() {
        if (_items == null)
            return null;

        List<Integer> selection = new LinkedList<>();
        for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                selection.add(i);
            }
        }
        return selection;
    }

    private String buildSelectedItemString() {
        if (_items == null)
            return null;

        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;

                sb.append(_items[i]);
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unused")
    public String getSelectedItemsAsString() {
        if (_items == null)
            return null;

        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;
                sb.append(_items[i]);
            }
        }
        return sb.toString();
    }
}