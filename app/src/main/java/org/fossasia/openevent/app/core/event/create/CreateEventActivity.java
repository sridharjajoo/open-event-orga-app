package org.fossasia.openevent.app.core.event.create;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.badoualy.stepperindicator.StepperIndicator;

import org.fossasia.openevent.app.R;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class CreateEventActivity extends AppCompatActivity implements HasSupportFragmentInjector {

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingInjector;

    @BindView(R.id.pager)
    ViewPager pager;

    @BindView(R.id.stepper_indicator)
    StepperIndicator indicator;

    public static final String EVENT_ID = "event_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event_activity);
        ButterKnife.bind(this);

        assert pager != null;
        pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

        indicator.setViewPager(pager, pager.getAdapter().getCount());

        indicator.addOnStepClickListener(step -> pager.setCurrentItem(step, true));
        indicator.setCurrentStep(0);

        long id = getIntent().getLongExtra(EVENT_ID, -1);

        if (savedInstanceState == null && id != -1) {
            Fragment fragment = CreateEventFragment.newInstance(id);
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, fragment)
                .commit();
        }

        pager.setOffscreenPageLimit(3);
//        submit.setOnClickListener(v -> {
//            pager.setCurrentItem(getItem(+1), true);
//        });
    }

    private int getItem(int i) {
        return pager.getCurrentItem() + i;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingInjector;
    }
}
