package nl.hnogames.domoticz;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import nl.hnogames.domoticz.Fragments.Dashboard;

public class PlanActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        String selectedPlan = bundle.getString("PLANNAME");
        int selectedPlanID = bundle.getInt("PLANID");
        this.setTitle(selectedPlan);

        Dashboard dash = new Dashboard();
        dash.selectedPlan(selectedPlanID, selectedPlan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(android.R.id.content, dash);
        tx.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}