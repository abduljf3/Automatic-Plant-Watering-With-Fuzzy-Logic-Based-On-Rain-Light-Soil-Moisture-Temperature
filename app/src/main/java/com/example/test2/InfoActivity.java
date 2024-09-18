package com.example.test2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class InfoActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RuleAdapter ruleAdapter;
    private List<Rule> ruleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.Info);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                Intent homeIntent = new Intent(InfoActivity.this, MainActivity.class);
                startActivity(homeIntent);
                return true;
            } else if (itemId == R.id.Info) {
                return true;
            } else if (itemId == R.id.navigation_analytics) {
                Intent aboutMeIntent = new Intent(InfoActivity.this, AboutMeActivity.class);
                startActivity(aboutMeIntent);
                return true;
            } else if (itemId == R.id.navigation_settings) {
                finishAffinity(); // Exit the application
                return true;
            }
            return false;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // Two columns

        ruleList = new ArrayList<>();
        // Add your 18 rules here
        ruleList.add(new Rule("Rule 1 (0)", "Not Rain, Wet Soil, Cold Temp:", "Fan Off, Pump Off"));
        ruleList.add(new Rule("Rule 2 (5)", "Not Rain, Wet Soil, Normal Temp:", "Fan Off, Pump Off"));
        ruleList.add(new Rule("Rule 3 (10)", "Not Rain, Wet Soil, Hot Temp:", "Fan On, Pump Off"));
        ruleList.add(new Rule("Rule 4 (15)", "Not Rain, Normal Soil, Cold Temp:", "Fan Off, Pump Off"));
        ruleList.add(new Rule("Rule 5 (20)", "Not Rain, Normal Soil, Normal Temp:", "Fan Off, Pump Off"));
        ruleList.add(new Rule("Rule 6 (25)", "Not Rain, Normal Soil, Hot Temp:", "Fan On, Pump Off"));
        ruleList.add(new Rule("Rule 7 (30)", "Not Rain, Dry Soil, Cold Temp:", "Fan Off, Pump n"));
        ruleList.add(new Rule("Rule 8 (35)", "Not Rain, Dry Soil, Normal Temp:", "Fan Off, Pump On"));
        ruleList.add(new Rule("Rule 9 (40)", "Not Rain, Dry Soil, Hot Temp:", "Fan Off, Pump On"));
        ruleList.add(new Rule("Rule 10 (45)", "Rain, Wet Soil, Cold Temp:", "Fan Off, Pump Off"));
        ruleList.add(new Rule("Rule 11 (50)", "Rain, Wet Soil, Normal Temp:", "Fan Off, Pump Off"));
        ruleList.add(new Rule("Rule 12 (55)", "Rain, Wet Soil, Hot Temp:", "Fan On, Pump On"));
        ruleList.add(new Rule("Rule 13 (60)", "Rain, Normal Soil, Cold Temp:", "Fan Off, Pump Off"));
        ruleList.add(new Rule("Rule 14 (65)", "Rain, Normal Soil, Normal Temp:", "Fan Off, Pump Off"));
        ruleList.add(new Rule("Rule 15 (70)", "Rain, Normal Soil, Hot Temp:", "Fan On, Pump Off"));
        ruleList.add(new Rule("Rule 16 (75)", "Rain, Dry Soil, Cold Temp:", "Fan Off, Pump Off"));
        ruleList.add(new Rule("Rule 17 (80)", "Rain, Dry Soil, Normal Temp:", "Fan Off, Pump Off"));
        ruleList.add(new Rule("Rule 18 (85)", "Rain, Dry Soil, Hot Temp:", "Fan On, Pump Off"));

        // Add the rest of the rules...

        ruleAdapter = new RuleAdapter(ruleList);
        recyclerView.setAdapter(ruleAdapter);
    }

    // Inner data class Rule
    public static class Rule {
        private String ruleNumber;
        private String ruleCondition;
        private String ruleAction;

        public Rule(String ruleNumber, String ruleCondition, String ruleAction) {
            this.ruleNumber = ruleNumber;
            this.ruleCondition = ruleCondition;
            this.ruleAction = ruleAction;
        }

        public String getRuleNumber() {
            return ruleNumber;
        }

        public String getRuleCondition() {
            return ruleCondition;
        }

        public String getRuleAction() {
            return ruleAction;
        }
    }

    // Inner adapter class RuleAdapter
    public class RuleAdapter extends RecyclerView.Adapter<RuleAdapter.RuleViewHolder> {
        private List<Rule> ruleList;

        public RuleAdapter(List<Rule> ruleList) {
            this.ruleList = ruleList;
        }

        @NonNull
        @Override
        public RuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rule, parent, false);
            return new RuleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RuleViewHolder holder, int position) {
            Rule rule = ruleList.get(position);
            holder.tvRuleNumber.setText(rule.getRuleNumber());
            holder.tvRuleCondition.setText(rule.getRuleCondition());
            holder.tvRuleAction.setText(rule.getRuleAction());
        }

        @Override
        public int getItemCount() {
            return ruleList.size();
        }

        class RuleViewHolder extends RecyclerView.ViewHolder {
            TextView tvRuleNumber, tvRuleCondition, tvRuleAction;

            public RuleViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRuleNumber = itemView.findViewById(R.id.tvRuleNumber);
                tvRuleCondition = itemView.findViewById(R.id.tvRuleCondition);
                tvRuleAction = itemView.findViewById(R.id.tvRuleAction);
            }
        }
    }
}
