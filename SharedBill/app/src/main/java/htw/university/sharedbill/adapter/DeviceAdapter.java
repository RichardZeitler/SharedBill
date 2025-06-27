package htw.university.sharedbill.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DeviceAdapter extends ArrayAdapter<String> {
    private int connectedIndex = -1;

    public DeviceAdapter(@NonNull Context context) {
        super(context, android.R.layout.simple_list_item_1, new ArrayList<>());
    }

    public void setConnectedIndex(int index) {
        connectedIndex = index;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = view.findViewById(android.R.id.text1);

        if (position == connectedIndex) {
            textView.setTextColor(Color.GREEN);
        } else {
            textView.setTextColor(Color.BLACK);
        }

        return view;
    }
}

