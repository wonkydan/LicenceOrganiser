package gregory.dan.licenceorganiser;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import gregory.dan.licenceorganiser.UI.PointRecyclerViewAdapter;
import gregory.dan.licenceorganiser.Unit.Inspection;
import gregory.dan.licenceorganiser.Unit.OutstandingPoints;
import gregory.dan.licenceorganiser.Unit.Unit;
import gregory.dan.licenceorganiser.Unit.viewModels.MyViewModel;

import static gregory.dan.licenceorganiser.AddInspectionActivity.INSPECTION_EXTRA;

public class ViewInspectionActivity extends AppCompatActivity implements PointRecyclerViewAdapter.ListItemClickListener{

    private final static String TITLE_START = "Inspection carried out on: ";
    private final static String INSPECTED_BY_TEXT = "Inspection conducted by: ";

    @BindView(R.id.view_inspection_date_title_text)TextView mInspectionDate;
    @BindView(R.id.view_inspection_points_recycler_view)RecyclerView mRecyclerView;
    @BindView(R.id.inspected_by_text_view) TextView mInspectedByTextView;
    private long mInspectionIdFromIntent;
    private List<OutstandingPoints> mOutstandingPoints = new ArrayList<>();

    private PointRecyclerViewAdapter mRecyclerViewAdapter;

    private MyViewModel myViewModel;

    //firebase
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_inspection);

        Intent intent = getIntent();
        if(!intent.hasExtra(INSPECTION_EXTRA)){
            finish();
        }

        mFirebaseAuth = FirebaseAuth.getInstance();
        mUser = mFirebaseAuth.getCurrentUser();

        mInspectionIdFromIntent = intent.getLongExtra(INSPECTION_EXTRA, 1);
        ButterKnife.bind(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewAdapter = new PointRecyclerViewAdapter(this);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        myViewModel = ViewModelProviders.of(this).get(MyViewModel.class);
        LiveData<DataSnapshot> points = myViewModel.getAllUnitPoints();
        points.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if(dataSnapshot != null){
                    if(mOutstandingPoints != null){
                        mOutstandingPoints.clear();
                    }
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        OutstandingPoints point = data.getValue(OutstandingPoints.class);
                        if(point.inspectionId == mInspectionIdFromIntent) {
                            mOutstandingPoints.add(point);
                        }
                    }
                    mRecyclerViewAdapter.setPoints(mOutstandingPoints);
                }
            }
        });

        new GetInspectionAsyncTask(myViewModel).execute(mInspectionIdFromIntent);

        String inspectedByText = INSPECTED_BY_TEXT + mUser.getDisplayName();
        mInspectedByTextView.setText(inspectedByText);

        String inspectionDate = TITLE_START;
        mInspectionDate.setText(inspectionDate);
    }

    private void setTitleText(long date){
        Date mDate = new Date(date);
        String newInspectedDate = new SimpleDateFormat("dd-MMMM-YYYY", Locale.getDefault()).format(mDate);
        String completeTitle = TITLE_START + newInspectedDate;
        mInspectionDate.setText(completeTitle);
    }

    @Override
    public void completedClick(int item) {
        OutstandingPoints point = mOutstandingPoints.get(item);
        point.complete = 1;
        myViewModel.insertToFirebase(point);
    }

    private class GetInspectionAsyncTask extends AsyncTask<Long, Void, Inspection>{
        MyViewModel viewModel;

        GetInspectionAsyncTask(MyViewModel myViewModel){
            viewModel = myViewModel;
        }

        @Override
        protected Inspection doInBackground(Long... id) {
            return viewModel.getInspection(id[0]);
        }

        @Override
        protected void onPostExecute(Inspection inspection) {
            setTitleText(inspection.inspectionDate);
        }
    }
}
