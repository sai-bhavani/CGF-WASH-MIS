package in.collectivegood.dbsibycgf.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import in.collectivegood.dbsibycgf.R;
import in.collectivegood.dbsibycgf.database.Schemas;
import in.collectivegood.dbsibycgf.support.InfoProvider;
import in.collectivegood.dbsibycgf.support.UserTypes;

public class GalleryCCListActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_IMAGE = 1;
    private ArrayList<String> list;
    private ArrayAdapter<String> adapter;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_cc_list);

        String email = InfoProvider.getCCData(this, Schemas.CCDatabaseEntry.EMAIL);
        final DatabaseReference user_type = FirebaseDatabase.getInstance()
                .getReference("user_types")
                .child(email.replaceAll("\\.", "(dot)"));

        final Bundle extras = getIntent().getExtras();
        user_type.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if (value.equals(UserTypes.USER_TYPE_ADMIN)) {
                    generateCCList(extras.getString("state").toLowerCase());
                } else if (value.equals(UserTypes.USER_TYPE_ADMIN_AP)) {
                    generateCCList("ap");
                } else if (value.equals(UserTypes.USER_TYPE_ADMIN_TL)) {
                    generateCCList("tl");
                } else {
                    ViewGallery();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void ViewGallery() {
        //noinspection ConstantConditions
        String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().toLowerCase();
        Intent intent = new Intent(GalleryCCListActivity.this, GallerySubActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("state", InfoProvider.getCCState(this));
        startActivity(intent);
        finish();
    }

    private void generateCCList(final String state) {
        final GridView gridView = (GridView) findViewById(R.id.gallery_cc_list);
        gridView.setEmptyView(findViewById(R.id.empty));

        list = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, R.layout.gallery_cc, R.id.gallery_cc_name, list);
        gridView.setAdapter(adapter);

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        DatabaseReference gallery = firebaseDatabase.getReference("gallery").child(state);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gridView.setEmptyView(findViewById(R.id.empty2));
                findViewById(R.id.empty).setVisibility(View.GONE);
            }
        }, 5000);

        gallery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                list.add(dataSnapshot.getKey().toUpperCase());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                list.remove(dataSnapshot.getKey().toUpperCase());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = list.get(position);
                Intent intent = new Intent(GalleryCCListActivity.this, GallerySubActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("state", state);
                startActivity(intent);
            }
        });
    }


}
