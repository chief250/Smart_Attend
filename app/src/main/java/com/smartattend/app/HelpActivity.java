package com.smartattend.app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartattend.app.adapter.HelpFaqAdapter;

import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends AppCompatActivity {

    private RecyclerView rvFaq;
    private HelpFaqAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.help);
        }

        rvFaq = findViewById(R.id.rvFaq);
        rvFaq.setLayoutManager(new LinearLayoutManager(this));

        List<HelpFaqAdapter.FaqItem> faqItems = buildFaqList();
        adapter = new HelpFaqAdapter(faqItems);
        rvFaq.setAdapter(adapter);
    }

    private List<HelpFaqAdapter.FaqItem> buildFaqList() {
        List<HelpFaqAdapter.FaqItem> list = new ArrayList<>();

        list.add(new HelpFaqAdapter.FaqItem(
                getString(R.string.faq_q1),
                getString(R.string.faq_a1)
        ));
        list.add(new HelpFaqAdapter.FaqItem(
                getString(R.string.faq_q2),
                getString(R.string.faq_a2)
        ));
        list.add(new HelpFaqAdapter.FaqItem(
                getString(R.string.faq_q3),
                getString(R.string.faq_a3)
        ));
        list.add(new HelpFaqAdapter.FaqItem(
                getString(R.string.faq_q4),
                getString(R.string.faq_a4)
        ));
        list.add(new HelpFaqAdapter.FaqItem(
                getString(R.string.faq_q5),
                getString(R.string.faq_a5)
        ));

        return list;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
