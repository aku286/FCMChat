package com.android.aku.fcmchat.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.aku.fcmchat.R;
import com.android.aku.fcmchat.models.User;

import java.util.List;

/**
 * Used for displaying the User list of the registered accounts
 */
public class UserListingRecyclerAdapter extends RecyclerView.Adapter<UserListingRecyclerAdapter.ViewHolder> implements View.OnClickListener{
    private List<User> mUsers;
    private CustomOnItemClickListener listener;

    public UserListingRecyclerAdapter(List<User> users, CustomOnItemClickListener listener) {
        this.mUsers = users;
        this.listener = listener;
    }

    public void add(User user) {
        mUsers.add(user);
        notifyItemInserted(mUsers.size() - 1);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_user_listing, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = mUsers.get(position);

        if (user.email != null){
            String alphabet = user.email.substring(0, 1);
    
            holder.txtUsername.setText(user.email);
            holder.txtUserAlphabet.setText(alphabet);
          }
    }

    @Override
    public int getItemCount() {
        if (mUsers != null) {
            return mUsers.size();
        }
        return 0;
    }

    public User getUser(int position) {
        return mUsers.get(position);
    }

    @Override
    public void onClick(View v) {
        listener.onItemClicked(v);
    }

    public interface CustomOnItemClickListener{
        public void onItemClicked(View v);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtUserAlphabet, txtUsername;

        ViewHolder(View itemView) {
            super(itemView);
            txtUserAlphabet = (TextView) itemView.findViewById(R.id.text_view_user_alphabet);
            txtUsername = (TextView) itemView.findViewById(R.id.text_view_username);
        }
    }
}
