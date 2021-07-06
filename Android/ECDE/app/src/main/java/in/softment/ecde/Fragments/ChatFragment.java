package in.softment.ecde.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;

import in.softment.ecde.Adapters.ChatHomeAdapter;
import in.softment.ecde.MainActivity;
import in.softment.ecde.Models.LastMessageModel;
import in.softment.ecde.R;


public class ChatFragment extends Fragment {


    private final Context context;
    private ChatHomeAdapter chatHomeAdapter;
    private TextView message;
    private ArrayList<LastMessageModel> lastMessageModels;
    private RecyclerView recyclerView;
    public ChatFragment(Context context) {
        this.context = context;
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        lastMessageModels = new ArrayList<>();
        chatHomeAdapter = new ChatHomeAdapter(context,lastMessageModels);
        recyclerView.setAdapter(chatHomeAdapter);
        TextView readAndUnread =  view.findViewById(R.id.seeUnreadAndRead);
        readAndUnread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastMessageModels.clear();
                if (readAndUnread.getText().toString().equalsIgnoreCase(getString(R.string.unread))) {
                    lastMessageModels.addAll(LastMessageModel.getUnreadMessages());
                    chatHomeAdapter.notifyDataSetChanged();
                    if (lastMessageModels.size() < 1) {
                        message.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);

                    }
                    else {
                        message.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    readAndUnread.setText(R.string.all);
                }
                else {
                    lastMessageModels.addAll(LastMessageModel.lastMessageModels);
                    chatHomeAdapter.notifyDataSetChanged();
                    if (lastMessageModels.size() < 1) {
                        message.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);

                    }
                    else {
                        message.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    readAndUnread.setText(R.string.unread);
                }


            }
        });

        message = view.findViewById(R.id.message);

        //SearchEDITTEXT
        EditText searchEDTEXT = view.findViewById(R.id.searchEditText);
        searchEDTEXT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chatHomeAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }


    public void notifyAdapter(){
        lastMessageModels.clear();
        lastMessageModels.addAll(LastMessageModel.lastMessageModels);
        if (lastMessageModels.size() < 1) {
            message.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

        }
        else {
            message.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        chatHomeAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        ((MainActivity)context).initializeChatFragment(this);

    }
}