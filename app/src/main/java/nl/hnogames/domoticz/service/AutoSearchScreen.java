package nl.hnogames.domoticz.service;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.Row;
import androidx.car.app.model.SearchTemplate;
import androidx.car.app.model.Template;
import java.util.function.Consumer;
import nl.hnogames.domoticz.R;

/**
 * Search screen for Android Auto
 * Provides a search interface for filtering devices
 */
public class AutoSearchScreen extends Screen {
    private final Consumer<String> onSearchSubmitted;
    private String searchText;
    public AutoSearchScreen(@NonNull CarContext carContext, Consumer<String> onSearchSubmitted, String initialSearchText) {
        super(carContext);
        this.onSearchSubmitted = onSearchSubmitted;
        this.searchText = initialSearchText != null ? initialSearchText : "";
    }
    @NonNull
    @Override
    public Template onGetTemplate() {
        SearchTemplate.Builder searchBuilder = new SearchTemplate.Builder(new SearchTemplate.SearchCallback() {
            @Override
            public void onSearchTextChanged(@NonNull String text) {
                searchText = text;
                invalidate();
            }
            @Override
            public void onSearchSubmitted(@NonNull String text) {
                AutoSearchScreen.this.onSearchSubmitted.accept(text);
                getScreenManager().pop();
            }
        })
                .setHeaderAction(Action.BACK)
                .setSearchHint(getCarContext().getString(R.string.search_items))
                .setShowKeyboardByDefault(false)
                .setInitialSearchText(searchText);
        // Add action buttons
        ItemList.Builder listBuilder = new ItemList.Builder();
        // Add clear search option if there's text
        if (!searchText.isEmpty()) {
            listBuilder.addItem(new Row.Builder()
                    .setTitle(getCarContext().getString(R.string.clear_filter))
                    .setOnClickListener(() -> {
                        AutoSearchScreen.this.onSearchSubmitted.accept("");
                        getScreenManager().pop();
                    })
                    .build());
        }
        // Add apply search option
        listBuilder.addItem(new Row.Builder()
                .setTitle(getCarContext().getString(R.string.ok))
                .setOnClickListener(() -> {
                    AutoSearchScreen.this.onSearchSubmitted.accept(searchText);
                    getScreenManager().pop();
                })
                .build());
        searchBuilder.setItemList(listBuilder.build());
        return searchBuilder.build();
    }
}
