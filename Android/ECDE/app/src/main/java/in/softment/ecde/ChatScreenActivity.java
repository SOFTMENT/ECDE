package in.softment.ecde;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import in.softment.ecde.Adapters.LiveChatAdapter;
import in.softment.ecde.Models.AllMessagesModel;
import in.softment.ecde.Models.LastMessageModel;
import in.softment.ecde.Models.UserModel;
import in.softment.ecde.Utils.ProgressHud;
import in.softment.ecde.Utils.Services;

public class ChatScreenActivity extends AppCompatActivity {

    private LiveChatAdapter liveChatAdapter;
    private List<AllMessagesModel> chatModels;
    private EditText editText;
    RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private String uid;
    private String senderId;
    private String senderName;
    private String senderImage;
    private String senderToken;
    private ImageView more;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getCurrentUser().getUid();

        senderId = getIntent().getStringExtra("sellerId");
        senderImage = getIntent().getStringExtra("sellerImage");
        senderName = getIntent().getStringExtra("sellerName");
        senderToken = getIntent().getStringExtra("sellerToken");



        //MoreImage
        more = findViewById(R.id.more);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }
        });

        TextView name = findViewById(R.id.name);
        CircleImageView profile_image = findViewById(R.id.profile_image);

        name.setText(senderName);
        Glide.with(this).load(senderImage).placeholder(R.drawable.placeholder1).into(profile_image);

        Map<String,Object> isReadMap = new HashMap<>();
        isReadMap.put("isRead",true);
        FirebaseFirestore.getInstance().collection("Chats").document(uid).collection("LastMessage").document(senderId).update(isReadMap);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        chatModels = new ArrayList<>();

        final ImageView sent = findViewById(R.id.sent);
        editText = findViewById(R.id.message);
        sent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sMessage = editText.getText().toString().trim();

                if (!sMessage.isEmpty()) {
                    editText.setText("");
                    sentMessage(sMessage, uid);
                }
                else {
                    editText.requestFocus();
                    editText.setError("Empty");
                }
            }
        });

        recyclerView = findViewById(R.id.recyclerview);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        liveChatAdapter = new LiveChatAdapter(this,chatModels,uid);
        recyclerView.setAdapter(liveChatAdapter);

        getAllChats();
    }

    private void getAllChats() {
        ProgressHud.show(this,"Loading...");

        FirebaseFirestore.getInstance().collection("Chats").document(uid).collection(senderId).orderBy("date").addSnapshotListener((value, error) -> {
            ProgressHud.dialog.dismiss();
           if (error == null) {
                chatModels.clear();
                if (value != null && !value.isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                        AllMessagesModel allMessagesModel = documentSnapshot.toObject(AllMessagesModel.class);
                        chatModels.add(allMessagesModel);

                    }
                }


               if (chatModels.size() > 0) {
                   recyclerView.post(new Runnable() {
                       @Override
                       public void run() {
                           recyclerView.scrollToPosition(chatModels.size() - 1);

                       }
                   });
               }

                liveChatAdapter.notifyDataSetChanged();

            }

        });


    }





    private void sentMessage(String sMessage, String uid) {
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("message",sMessage);
        hashMap.put("senderUid",uid);
        String messageId = FirebaseFirestore.getInstance().collection("Chats").document().getId();
        hashMap.put("messageId",messageId);
        hashMap.put("senderImage",UserModel.data.getProfileImage());
        hashMap.put("date",new Date());
        hashMap.put("senderName", UserModel.data.fullName);


        FirebaseFirestore.getInstance().collection("Chats").document(uid).collection(senderId).document(messageId).set(hashMap).addOnCompleteListener(task -> {
            FirebaseFirestore.getInstance().collection("Chats").document(senderId).collection(uid).document(messageId).set(hashMap);

            HashMap<String,Object> hashMap12 = new HashMap<>();
            hashMap12.put("message",sMessage);
            hashMap12.put("senderUid",senderId);
            hashMap12.put("isRead",true);
            hashMap12.put("senderImage",senderImage);
            hashMap12.put("date",new Date());
            hashMap12.put("senderName", senderName);
            hashMap12.put("senderToken",senderToken);

            FirebaseFirestore.getInstance().collection("Chats").document(uid).collection("LastMessage").document(senderId).set(hashMap12);

            HashMap<String,Object> hashMap1 = new HashMap<>();
            hashMap1.put("message",sMessage);
            hashMap1.put("senderUid",uid);
            hashMap1.put("isRead",false);
            hashMap1.put("senderImage",UserModel.data.getProfileImage());
            hashMap1.put("date",new Date());
            hashMap1.put("senderName", UserModel.data.getFullName());
            hashMap1.put("senderToken", UserModel.data.token);
            FirebaseFirestore.getInstance().collection("Chats").document(senderId).collection("LastMessage").document(uid).set(hashMap1);

            Services.sentPushNotification(ChatScreenActivity.this,senderName,sMessage,senderToken);
        });
    }

    private void showPopupMenu(){
        PopupMenu popupMenu = new PopupMenu(this,more);
        popupMenu.getMenuInflater().inflate(R.menu.chatscreen_popup_menu,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.report) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatScreenActivity.this);
                View view1 = getLayoutInflater().inflate(R.layout.report_reason_layout,null);
                AlertDialog alertDialog = builder.create();
                alertDialog.setView(view1);
                final EditText message = view1.findViewById(R.id.entermessage);
                CardView report = view1.findViewById(R.id.sentnotification);
                report.setOnClickListener(v -> {

                    String sMessage = message.getText().toString().trim();
                    if (!sMessage.equals("")) {
                        alertDialog.dismiss();
                        reportUser(sMessage);
                    }
                    else {
                        Services.showCenterToast(ChatScreenActivity.this,getString(R.string.enter_reason));
                    }

                });

                alertDialog.show();

            }
            return true;
        });
        popupMenu.show();
    }


    private void reportUser(String reason){
        Map<String, String> map = new HashMap<>();
        map.put("reason",reason);
        map.put("userid",senderId);
        map.put("username",senderName);
        FirebaseFirestore.getInstance().collection("Report").document().set(map);

        Services.showDialog(this,getString(R.string.reported),getString(R.string.thanks_for_report));
    }

}
