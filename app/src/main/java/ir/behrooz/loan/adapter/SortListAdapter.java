package ir.behrooz.loan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import ir.behrooz.loan.R;
import ir.behrooz.loan.common.FontChangeCrawler;
import ir.behrooz.loan.model.SortModel;

/**
 * Created by Behrooz Mohamadi on 16/10/29.
 */
public class SortListAdapter extends ArrayAdapter<SortModel> {

    private static class ViewHolder {
        TextView title;
        CheckBox checkBox;
    }

    public SortListAdapter(Context context, List<SortModel> sortModels) {
        super(context, R.layout.list_item_person_search, sortModels);
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        final SortModel sortModel = getItem(position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_person_search, parent, false);
            new FontChangeCrawler(getContext().getAssets()).replaceFonts((ViewGroup) convertView);
            viewHolder.title =  convertView.findViewById(R.id.fullName);
            viewHolder.checkBox =  convertView.findViewById(R.id.checkBox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.title.setText(sortModel.getTitle());
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortModel.setChecked(viewHolder.checkBox.isChecked());
            }
        });
        return convertView;
    }
}
