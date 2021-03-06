 package com.example.messenger;

 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;

 import androidx.annotation.NonNull;
 import androidx.fragment.app.Fragment;
 import androidx.recyclerview.widget.LinearLayoutManager;
 import androidx.recyclerview.widget.RecyclerView;

 import com.firebase.ui.database.FirebaseRecyclerAdapter;
 import com.firebase.ui.database.FirebaseRecyclerOptions;
 import com.google.firebase.auth.FirebaseAuth;
 import com.google.firebase.database.DataSnapshot;
 import com.google.firebase.database.DatabaseError;
 import com.google.firebase.database.DatabaseReference;
 import com.google.firebase.database.FirebaseDatabase;
 import com.google.firebase.database.ValueEventListener;
 import com.squareup.picasso.Picasso;

 import de.hdodenhof.circleimageview.CircleImageView;


 public class ChatsFragment extends Fragment {

    private View PrivateChatView;
    private RecyclerView chatList;

     private DatabaseReference ChatsRef, UsersRef;
     private FirebaseAuth mAuth;
     private String currentUserID;


    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        chatList = (RecyclerView)  PrivateChatView.findViewById(R.id.chat_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));


        return PrivateChatView;
    }

     @Override
     public void onStart() {
         super.onStart();


         FirebaseRecyclerOptions<Contacts> options =
                 new FirebaseRecyclerOptions.Builder<Contacts>()
                         .setQuery(ChatsRef, Contacts.class)
                         .build();


         FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                 new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                     @Override
                     protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Contacts model)
                     {
                         final String usersIDs = getRef(position).getKey();

                         UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                             {
                                 if (dataSnapshot.hasChild("image"))
                                 {
                                     final String retImage = dataSnapshot.child("image").getValue().toString();
                                     Picasso.get().load(retImage).into(holder.profileImage);
                                 }
                                 final String retName = dataSnapshot.child("name").getValue().toString();
                                 final String retStatus = dataSnapshot.child("status").getValue().toString();

                                 holder.userName.setText(retName);
                                 holder.userStatus.setText("Last Seen: " + "\n" + "Date " + " Time");
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError databaseError) {

                             }
                         });
                     }

                     @NonNull
                     @Override
                     public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                     {
                         View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                         return new ChatsViewHolder(view);
                     }
                 };
         chatList.setAdapter(adapter);
         adapter.startListening();

     }



     public static class ChatsViewHolder extends RecyclerView.ViewHolder
     {
         CircleImageView profileImage;
         TextView userStatus, userName;


         public ChatsViewHolder(@NonNull View itemView)
         {
             super(itemView);


             profileImage = itemView.findViewById(R.id.users_profile_image);
             userStatus = itemView.findViewById(R.id.user_status);
             userName = itemView.findViewById(R.id.user_profile_name);
         }
     }
}